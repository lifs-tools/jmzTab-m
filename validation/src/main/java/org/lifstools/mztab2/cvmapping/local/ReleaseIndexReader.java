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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.lifstools.mztab2.cvmapping.local.ResolutionException.Reason;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Format;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Latest;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseDates;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads, over the network, what releases a vocabulary has and which is its
 * latest, for the {@link VocabularyCache}, which keeps the answers.
 *
 * <ul>
 * <li>GitHub tags and the dates of their GitHub releases, from the GitHub API,
 * paged 100 at a time, with an {@code Authorization: Bearer} token if one is
 * given;</li>
 * <li>the links of an HTTP directory index;</li>
 * <li>the version an unversioned file declares in its header.</li>
 * </ul>
 * <p>
 * It keeps no state between calls.</p>
 */
final class ReleaseIndexReader {

    private static final Logger LOG = LoggerFactory.getLogger(ReleaseIndexReader.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern HREF = Pattern.compile("href\\s*=\\s*[\"']([^\"']*)[\"']",
        Pattern.CASE_INSENSITIVE);
    private static final int GITHUB_PAGE_SIZE = 100;
    private static final int MAX_PAGES = 100;
    private static final int MAX_INDEX_BYTES = 16 << 20;
    private static final int BUFFER_SIZE = 1 << 16;

    private final Downloader downloader;
    private final URI githubApi;
    private final Optional<String> githubToken;

    /**
     * @param downloader fetches the pages and files
     * @param githubApi the base URL of the GitHub API, without a final slash
     * @param githubToken sent as {@code Authorization: Bearer} to the GitHub API
     */
    ReleaseIndexReader(Downloader downloader, URI githubApi, Optional<String> githubToken) {
        this.downloader = Objects.requireNonNull(downloader, "downloader");
        String api = githubApi.toString();
        this.githubApi = URI.create(api.endsWith("/") ? api.substring(0, api.length() - 1) : api);
        this.githubToken = Objects.requireNonNull(githubToken, "githubToken");
    }

    /**
     * Reads the release index of an entry.
     *
     * @param e the vocabulary
     * @return its releases whose names fit the scheme, each once, oldest first,
     * with their dates if known
     * @throws ResolutionException DOWNLOAD_FAILED
     */
    List<Release> releases(VocabularyEntry e) throws ResolutionException {
        return ordered(e.scheme(), readIndex(e));
    }

    /**
     * Looks up the latest release of an entry whose latest is a GitHub tag or
     * a file header.
     *
     * @param e the vocabulary
     * @return the latest release, or empty if none can be named
     * @throws ResolutionException DOWNLOAD_FAILED or PARSE_FAILED
     * @throws IllegalArgumentException for {@link Latest.NewestRelease}, which
     * is the newest of {@link #releases}, as the caller keeps them
     */
    Optional<String> latest(VocabularyEntry e) throws ResolutionException {
        Latest latest = e.latest();
        if (latest instanceof Latest.GithubLatestTag tag) {
            return newest(e.scheme(), githubTags(tag.repo()).stream()
                .map(tag::versionOf).flatMap(Optional::stream).map(v -> new Release(v, Optional.empty())).toList());
        }
        if (latest instanceof Latest.Header header) {
            return headerVersion(header.url(), e.location().format(), e.scheme());
        }
        throw new IllegalArgumentException(e.key() + ": the newest release is read from the release index");
    }

    private List<Release> readIndex(VocabularyEntry e) throws ResolutionException {
        ReleaseIndex index = e.releases();
        List<Release> releases = new ArrayList<>();
        if (index instanceof ReleaseIndex.GithubTags tags) {
            Map<String, LocalDate> published = tags.dates() == ReleaseDates.GITHUB_RELEASES
                ? githubReleaseDates(tags.repo()) : Map.of();
            for (String tag : githubTags(tags.repo())) {
                Optional<String> version = tags.versionOf(tag);
                if (version.isPresent()) {
                    releases.add(new Release(version.get(),
                        date(tags.dates(), version.get(), Optional.ofNullable(published.get(tag)))));
                }
            }
        } else if (index instanceof ReleaseIndex.DirectoryListing listing) {
            for (String link : directoryLinks(listing.url())) {
                Optional<String> version = listing.versionOf(link);
                if (version.isPresent()) {
                    releases.add(new Release(version.get(), date(listing.dates(), version.get(), Optional.empty())));
                }
            }
        } else if (index instanceof ReleaseIndex.Listed listed) {
            releases.addAll(listed.releases());
        }
        return releases;
    }

    private static Optional<LocalDate> date(ReleaseDates dates, String version, Optional<LocalDate> published) {
        return switch (dates) {
            case GITHUB_RELEASES -> published;
            case VERSION -> {
                try {
                    yield Optional.of(LocalDate.parse(version));
                } catch (DateTimeParseException x) {
                    yield Optional.empty();
                }
            }
            case LISTED, NONE -> Optional.empty();
        };
    }

    /** The releases that fit the scheme, each once, oldest first. */
    static List<Release> ordered(VersionScheme scheme, List<Release> releases) {
        Map<String, Release> byVersion = new LinkedHashMap<>();
        for (Release r : releases) {
            if (scheme.accepts(r.version())) {
                byVersion.putIfAbsent(r.version(), r);
            }
        }
        List<Release> ordered = new ArrayList<>(byVersion.values());
        ordered.sort(Comparator.comparing(Release::version, (a, b) -> scheme.compare(a, b).orElseThrow()));
        return List.copyOf(ordered);
    }

    static Optional<String> newest(VersionScheme scheme, List<Release> releases) {
        List<Release> ordered = ordered(scheme, releases);
        return ordered.isEmpty() ? Optional.empty() : Optional.of(ordered.get(ordered.size() - 1).version());
    }

    // ---- GitHub, directory listings and headers ----

    private List<String> githubTags(String repo) throws ResolutionException {
        List<String> tags = new ArrayList<>();
        for (JsonNode tag : githubPages(repo, "tags")) {
            String name = tag.path("name").asText(null);
            if (name != null) {
                tags.add(name);
            }
        }
        return tags;
    }

    /** The published_at date (UTC) of each published GitHub release, by tag. */
    private Map<String, LocalDate> githubReleaseDates(String repo) throws ResolutionException {
        Map<String, LocalDate> dates = new HashMap<>();
        for (JsonNode release : githubPages(repo, "releases")) {
            String tag = release.path("tag_name").asText(null);
            String published = release.path("published_at").asText(null);
            if (tag != null && published != null && !release.path("draft").asBoolean(false)) {
                try {
                    dates.putIfAbsent(tag, OffsetDateTime.parse(published).withOffsetSameInstant(ZoneOffset.UTC)
                        .toLocalDate());
                } catch (DateTimeParseException x) {
                    LOG.debug("{}: release {} has an unreadable published_at {}", repo, tag, published);
                }
            }
        }
        return dates;
    }

    /** Every item of a paged GitHub list ({@code per_page=100}), page by page until one is short. */
    private List<JsonNode> githubPages(String repo, String list) throws ResolutionException {
        List<JsonNode> items = new ArrayList<>();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/vnd.github+json");
        githubToken.ifPresent(t -> headers.put("Authorization", "Bearer " + t));
        for (int page = 1; page <= MAX_PAGES; page++) {
            URI uri = URI.create(githubApi + "/repos/" + repo + "/" + list + "?per_page=" + GITHUB_PAGE_SIZE
                + "&page=" + page);
            JsonNode answer;
            try {
                answer = JSON.readTree(fetchSmall(uri, headers));
            } catch (IOException e) {
                throw new ResolutionException(Reason.DOWNLOAD_FAILED, "the GitHub " + list + " of " + repo
                    + " cannot be read from " + uri + ": " + VocabularyCache.message(e) + rateLimitHint(e), e);
            }
            if (!answer.isArray()) {
                throw new ResolutionException(Reason.DOWNLOAD_FAILED,
                    "the GitHub " + list + " of " + repo + " at " + uri + " is not a JSON list");
            }
            answer.forEach(items::add);
            if (answer.size() < GITHUB_PAGE_SIZE) {
                return items;
            }
        }
        LOG.warn("The GitHub {} of {} have more than {} pages; the rest is not read", list, repo, MAX_PAGES);
        return items;
    }

    private String rateLimitHint(IOException e) {
        String m = String.valueOf(e.getMessage());
        return githubToken.isEmpty() && (m.startsWith("HTTP 403 ") || m.startsWith("HTTP 429 "))
            ? "; set GITHUB_TOKEN to raise GitHub's rate limit" : "";
    }

    /** The links of an HTTP directory index, each reduced to its last segment (rel245/). */
    private List<String> directoryLinks(URI url) throws ResolutionException {
        String html;
        try {
            html = new String(fetchSmall(url, Map.of()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ResolutionException(Reason.DOWNLOAD_FAILED,
                "the release listing at " + url + " cannot be read: " + VocabularyCache.message(e), e);
        }
        List<String> links = new ArrayList<>();
        Matcher m = HREF.matcher(html);
        while (m.find()) {
            String link = m.group(1);
            int end = link.endsWith("/") ? link.length() - 1 : link.length();
            links.add(link.substring(link.lastIndexOf('/', end - 1) + 1));
        }
        return links;
    }

    private byte[] fetchSmall(URI uri, Map<String, String> headers) throws IOException {
        try (InputStream in = downloader.get(uri, headers)) {
            byte[] bytes = in.readNBytes(MAX_INDEX_BYTES + 1);
            if (bytes.length > MAX_INDEX_BYTES) {
                throw new IOException(
                    "the answer is larger than " + VocabularyCache.size(OptionalLong.of(MAX_INDEX_BYTES)));
            }
            return bytes;
        }
    }

    /** Stops a reader once the header has been read. */
    private static final class HeaderRead extends RuntimeException {

        private static final long serialVersionUID = 1L;

        HeaderRead() {
            super(null, null, false, false);
        }
    }

    /** A read failure of the network stream, told apart from a parse failure. */
    private static final class NetworkFailure extends RuntimeException {

        private static final long serialVersionUID = 1L;

        NetworkFailure(IOException cause) {
            super(cause);
        }
    }

    /**
     * The version an unversioned file declares in its header (data-version or
     * owl:versionIRI): the value if the scheme accepts it, else its last path
     * segment the scheme accepts. Only the header is read.
     */
    private Optional<String> headerVersion(URI url, Format format, VersionScheme scheme)
        throws ResolutionException {
        List<String> values = new ArrayList<>();
        Consumer<TermRecord> stop = t -> {
            throw new HeaderRead();
        };
        InputStream network;
        try {
            network = downloader.get(url);
        } catch (Downloader.NotFoundException e) {
            throw new ResolutionException(Reason.DOWNLOAD_FAILED,
                "the latest release's file is not found at " + url, e);
        } catch (IOException e) {
            throw new ResolutionException(Reason.DOWNLOAD_FAILED, "the latest release's file at " + url
                + " cannot be read: " + VocabularyCache.message(e), e);
        }
        try (network) {
            InputStream in = new FilterInputStream(new BufferedInputStream(network, BUFFER_SIZE)) {
                @Override
                public int read() throws IOException {
                    try {
                        return super.read();
                    } catch (IOException e) {
                        throw new NetworkFailure(e);
                    }
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    try {
                        return super.read(b, off, len);
                    } catch (IOException e) {
                        throw new NetworkFailure(e);
                    }
                }
            };
            if (format == Format.OBO_GZ) {
                in = new GZIPInputStream(in, BUFFER_SIZE);
            }
            if (format == Format.OWL_RDFXML) {
                new OwlRdfXmlReader().read(in, stop, i -> {
                }, values::add);
            } else {
                new OboReader().read(in, stop, values::add);
            }
        } catch (HeaderRead done) {
            // the first term: the header is over
        } catch (NetworkFailure e) {
            throw new ResolutionException(Reason.DOWNLOAD_FAILED, "the latest release's file at " + url
                + " cannot be read: " + VocabularyCache.message((IOException) e.getCause()), e);
        } catch (IOException e) {
            if (values.isEmpty()) {
                throw new ResolutionException(Reason.PARSE_FAILED, "the latest release's file at " + url
                    + " is not " + format + ": " + VocabularyCache.message(e), e);
            }
        }
        for (String value : values) {
            if (scheme.accepts(value)) {
                return Optional.of(value);
            }
            String[] segments = value.split("/");
            for (int i = segments.length - 1; i >= 0; i--) {
                if (scheme.accepts(segments[i])) {
                    return Optional.of(segments[i]);
                }
            }
        }
        LOG.warn("The header of {} names no {} version: {}", url, scheme, values);
        return Optional.empty();
    }
}
