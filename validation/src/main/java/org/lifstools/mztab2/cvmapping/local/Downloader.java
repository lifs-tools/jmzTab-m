/*
 * Copyright 2026 LIFS Tools Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifstools.mztab2.cvmapping.local;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

/**
 * Fetches files for the {@link VocabularyCache}: vocabulary releases, OWL
 * imports and release indexes.
 */
public interface Downloader {

    /**
     * Opens a file for reading.
     *
     * @param uri the URL
     * @return the file's content; the caller closes it. The HTTP downloader
     * returns a {@link Download}, which also tells the final URL and the size.
     * @throws NotFoundException if there is no file at the URL (HTTP 404)
     * @throws IOException if the file cannot be fetched
     */
    InputStream get(URI uri) throws IOException;

    /**
     * Opens a file with extra request headers, such as an
     * {@code Authorization} for an API. The HTTP downloader drops them when
     * a redirect leaves the URL's origin; this default ignores them.
     *
     * @param uri the URL
     * @param headers the extra headers
     * @return the file's content; the caller closes it
     * @throws NotFoundException if there is no file at the URL (HTTP 404)
     * @throws IOException if the file cannot be fetched
     */
    default InputStream get(URI uri, Map<String, String> headers) throws IOException {
        return get(uri);
    }

    /**
     * A downloader over HTTP(S), with the User-Agent {@code jmzTab-m}. It
     * follows up to ten redirects. It does not follow one from https to http:
     * it asks the same host, path and query over https instead, follows that
     * if it answers with a file or a redirect, and otherwise refuses the
     * downgrade. A 404 is a
     * {@link NotFoundException}; any other status but 2xx is an
     * {@link IOException} that names the status.
     *
     * @param timeout the time to connect, to wait for the answer's headers,
     * and to wait for each further piece of the content
     * @return the downloader
     */
    static Downloader http(Duration timeout) {
        return new HttpDownloader(timeout);
    }

    /** No file at the URL: HTTP 404. */
    final class NotFoundException extends IOException {

        private static final long serialVersionUID = 1L;

        /** The URL that answered 404. */
        private final URI uri;

        /**
         * Creates the exception.
         *
         * @param uri the URL that answered 404, after any redirects
         */
        public NotFoundException(URI uri) {
            super("HTTP 404 from " + uri);
            this.uri = uri;
        }

        /**
         * The URL that answered.
         *
         * @return the URL that answered 404, after any redirects
         */
        public URI uri() {
            return uri;
        }
    }

    /** The content of a download, with the URL it came from and its size if known. */
    final class Download extends FilterInputStream {

        private final URI location;
        private final OptionalLong contentLength;

        /**
         * Wraps a download.
         *
         * @param in the content
         * @param location the URL the content came from, after redirects
         * @param contentLength the size the server announced, if any
         */
        public Download(InputStream in, URI location, OptionalLong contentLength) {
            super(in);
            this.location = Objects.requireNonNull(location, "location");
            this.contentLength = Objects.requireNonNull(contentLength, "contentLength");
        }

        /**
         * The final URL.
         *
         * @return the URL the content came from, after redirects
         */
        public URI location() {
            return location;
        }

        /**
         * The announced size.
         *
         * @return the size the server announced in Content-Length, if any
         */
        public OptionalLong contentLength() {
            return contentLength;
        }
    }
}

/** {@link Downloader#http(Duration)}. */
final class HttpDownloader implements Downloader {

    private static final int MAX_REDIRECTS = 10;
    private static final Set<Integer> REDIRECTS = Set.of(301, 302, 303, 307, 308);
    private static final String USER_AGENT = "jmzTab-m";

    private final Duration timeout;
    private final HttpClient client;
    /** Maps the URL a request names to the one that is sent; the identity, except in tests that have no TLS. */
    private final UnaryOperator<URI> wire;

    HttpDownloader(Duration timeout) {
        this(timeout, UnaryOperator.identity());
    }

    HttpDownloader(Duration timeout, UnaryOperator<URI> wire) {
        this.wire = Objects.requireNonNull(wire, "wire");
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        // redirects are followed by hand: no https to http, and no extra headers to another origin
        this.client = HttpClient.newBuilder()
            .connectTimeout(timeout)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    }

    @Override
    public InputStream get(URI uri) throws IOException {
        return get(uri, Map.of());
    }

    @Override
    public InputStream get(URI uri, Map<String, String> headers) throws IOException {
        URI current = uri;
        Map<String, String> extra = headers;
        HttpResponse<InputStream> pending = null;
        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            HttpResponse<InputStream> response = pending != null ? pending : send(current, extra);
            pending = null;
            int status = response.statusCode();
            if (REDIRECTS.contains(status)) {
                response.body().close();
                URI next = redirect(current, status, response);
                if (isDowngrade(current, next)) {
                    URI upgraded = upgrade(next);
                    pending = tryUpgrade(upgraded, sameOrigin(current, upgraded) ? extra : Map.of());
                    if (pending == null) {
                        throw new IOException("redirect from " + current + " to " + next + " refused: https to http");
                    }
                    next = upgraded;
                }
                if (!sameOrigin(current, next)) {
                    extra = Map.of();
                }
                current = next;
            } else if (status == 404) {
                response.body().close();
                throw new NotFoundException(current);
            } else if (status / 100 != 2) {
                response.body().close();
                throw new IOException("HTTP " + status + " from " + current);
            } else {
                return new Download(response.body(), current,
                    response.headers().firstValueAsLong("Content-Length"));
            }
        }
        throw new IOException("more than " + MAX_REDIRECTS + " redirects from " + uri);
    }

    private HttpResponse<InputStream> send(URI uri, Map<String, String> headers) throws IOException {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(wire.apply(uri))
                .timeout(timeout)
                .header("User-Agent", USER_AGENT)
                .GET();
            headers.forEach(request::header);
            // the request timeout ends with the headers: the body gets an idle timeout of its own
            return client.send(request.build(), info -> new IdleTimeoutBody(timeout));
        } catch (IllegalArgumentException e) {
            throw new IOException("cannot request " + uri + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            InterruptedIOException interrupted = new InterruptedIOException("interrupted while requesting " + uri);
            interrupted.initCause(e);
            throw interrupted;
        }
    }

    private static URI redirect(URI from, int status, HttpResponse<?> response) throws IOException {
        String location = response.headers().firstValue("Location")
            .orElseThrow(() -> new IOException("HTTP " + status + " without a Location from " + from));
        URI next;
        try {
            next = from.resolve(location.strip());
        } catch (IllegalArgumentException e) {
            throw new IOException("HTTP " + status + " from " + from + " to an invalid URL: " + location, e);
        }
        String scheme = String.valueOf(next.getScheme()).toLowerCase(java.util.Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new IOException("redirect from " + from + " to " + next + " refused: not http(s)");
        }
        return next;
    }

    /** A redirect from https to plain http. */
    static boolean isDowngrade(URI from, URI next) {
        return "https".equalsIgnoreCase(from.getScheme()) && "http".equalsIgnoreCase(next.getScheme());
    }

    /** The URL of a plain-http redirect target over https: the same host, path and query. */
    static URI upgrade(URI http) {
        String authority = http.getRawAuthority();
        if (http.getPort() == 80) {
            // the http default port is not the https one
            authority = http.getRawUserInfo() == null ? http.getHost() : http.getRawUserInfo() + "@" + http.getHost();
        }
        StringBuilder url = new StringBuilder("https://").append(authority);
        if (http.getRawPath() != null) {
            url.append(http.getRawPath());
        }
        if (http.getRawQuery() != null) {
            url.append('?').append(http.getRawQuery());
        }
        return URI.create(url.toString());
    }

    /** Whether an upgraded request answered: with a file or with a redirect. */
    static boolean answered(int status) {
        return status / 100 == 2 || REDIRECTS.contains(status);
    }

    /** The answer of the upgraded URL if it answered, else null (the downgrade is then refused). */
    private HttpResponse<InputStream> tryUpgrade(URI upgraded, Map<String, String> headers) {
        HttpResponse<InputStream> response;
        try {
            response = send(upgraded, headers);
        } catch (IOException e) {
            return null;
        }
        if (answered(response.statusCode())) {
            return response;
        }
        try {
            response.body().close();
        } catch (IOException ignored) {
            // the answer is refused anyway
        }
        return null;
    }

    private static boolean sameOrigin(URI a, URI b) {
        return String.valueOf(a.getScheme()).equalsIgnoreCase(String.valueOf(b.getScheme()))
            && String.valueOf(a.getHost()).equalsIgnoreCase(String.valueOf(b.getHost()))
            && a.getPort() == b.getPort();
    }

    /**
     * A response body read as a stream that fails with an
     * {@link HttpTimeoutException} when no data comes for the timeout, where
     * {@code BodyHandlers.ofInputStream()} would wait forever.
     */
    private static final class IdleTimeoutBody implements HttpResponse.BodySubscriber<InputStream> {

        private static final Object END = new Object();

        private final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        private final CompletableFuture<Flow.Subscription> subscription = new CompletableFuture<>();
        private final Duration timeout;

        IdleTimeoutBody(Duration timeout) {
            this.timeout = timeout;
        }

        @Override
        public CompletionStage<InputStream> getBody() {
            return CompletableFuture.completedStage(new Body());
        }

        @Override
        public void onSubscribe(Flow.Subscription s) {
            if (!subscription.complete(s)) {
                s.cancel();
                return;
            }
            s.request(1);
        }

        @Override
        public void onNext(List<ByteBuffer> buffers) {
            queue.add(buffers);
        }

        @Override
        public void onError(Throwable error) {
            queue.add(error);
        }

        @Override
        public void onComplete() {
            queue.add(END);
        }

        private void cancel() {
            subscription.thenAccept(Flow.Subscription::cancel);
        }

        private final class Body extends InputStream {

            private Iterator<ByteBuffer> buffers = List.<ByteBuffer>of().iterator();
            private ByteBuffer current;
            private boolean ended;
            private boolean closed;

            @Override
            public int read() throws IOException {
                byte[] one = new byte[1];
                return read(one, 0, 1) < 0 ? -1 : one[0] & 0xff;
            }

            @Override
            @SuppressWarnings("unchecked")
            public int read(byte[] b, int off, int len) throws IOException {
                Objects.checkFromIndexSize(off, len, b.length);
                if (len == 0) {
                    return 0;
                }
                while (current == null || !current.hasRemaining()) {
                    if (buffers.hasNext()) {
                        current = buffers.next();
                        continue;
                    }
                    if (closed) {
                        throw new IOException("the download is closed");
                    }
                    if (ended) {
                        return -1;
                    }
                    Object next;
                    try {
                        next = queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new InterruptedIOException("interrupted while downloading");
                    }
                    if (next == null) {
                        close();
                        throw new HttpTimeoutException("no data for " + timeout.toMillis() + " ms");
                    }
                    if (next == END) {
                        ended = true;
                        return -1;
                    }
                    if (next instanceof Throwable error) {
                        ended = true;
                        throw error instanceof IOException io ? io : new IOException(error.getMessage(), error);
                    }
                    buffers = ((List<ByteBuffer>) next).iterator();
                    subscription.join().request(1);
                }
                int n = Math.min(len, current.remaining());
                current.get(b, off, n);
                return n;
            }

            @Override
            public void close() {
                if (!closed) {
                    closed = true;
                    cancel();
                }
            }
        }
    }
}
