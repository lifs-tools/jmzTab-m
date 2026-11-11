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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.lifstools.mztab2.cvmapping.local.ResolutionException.Reason;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Format;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Latest;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseDates;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The local cache of vocabulary releases (design §4.6): downloads a release
 * once, checks its SHA-256, builds its term index and keeps both, immutable,
 * in a folder that several processes and threads may share.
 *
 * <p>
 * The layout under the root:</p>
 * <pre>
 * &lt;key&gt;/&lt;version&gt;/           a release: the file as downloaded, terms.idx, release.json
 * &lt;key&gt;/latest.json          the latest release, with the time it was looked up
 * &lt;key&gt;/releases.json        the release index, with the time it was read
 * _imports/&lt;hash&gt;/           an OWL import: the file and release.json; hash is the
 *                            first 16 hex digits of the SHA-256 of its IRI
 * </pre>
 * <p>
 * A release folder is built as {@code <key>/.tmp-<uuid>/}, with release.json
 * written last, and appears by one atomic rename. A folder without
 * release.json is not an entry. If another process renamed its copy first,
 * the loser deletes its own and uses the winner's. In one JVM, one lock per
 * release folder makes concurrent requests for a release download it once.
 * The paths in release.json are relative to its folder, so a cache folder
 * can be copied elsewhere, or pre-filled with folders from another cache.</p>
 *
 * <p>
 * The term index of an OWL release holds its own terms and the terms of
 * every {@code owl:imports} read with it, recursively; each import is also
 * downloaded, checked and cached on its own.</p>
 *
 * <p>
 * Offline, nothing is downloaded: a release not in the cache is
 * {@link Reason#OFFLINE_NOT_CACHED}, and {@link #latest} and
 * {@link #releases} answer from their cached files whatever their age.</p>
 */
public final class VocabularyCache {

    /** How long a looked-up latest release, or a release index, is kept. */
    public static final Duration DEFAULT_LATEST_TTL = Duration.ofHours(24);

    static final URI GITHUB_API = URI.create("https://api.github.com");

    private static final Logger LOG = LoggerFactory.getLogger(VocabularyCache.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    /** One lock per cache file or folder, for every cache of the process. */
    private static final Map<Path, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    private static final String RELEASE_JSON = "release.json";
    private static final String INDEX = "terms.idx";
    private static final String LATEST_JSON = "latest.json";
    private static final String RELEASES_JSON = "releases.json";
    private static final String IMPORTS = "_imports";
    private static final String TEMPORARY = ".tmp-";
    private static final String UNVERSIONED = "unversioned";
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");
    private static final Pattern HREF = Pattern.compile("href\\s*=\\s*[\"']([^\"']*)[\"']",
        Pattern.CASE_INSENSITIVE);
    private static final String OBO_PURL = "http://purl.obolibrary.org/obo/";
    private static final Pattern IMPORT_VERSION = Pattern.compile("v?\\d[A-Za-z0-9._-]*");
    private static final int GITHUB_PAGE_SIZE = 100;
    private static final int MAX_PAGES = 100;
    private static final int MAX_INDEX_BYTES = 16 << 20;
    private static final int BUFFER_SIZE = 1 << 16;

    private final Path root;
    private final Downloader downloader;
    private final boolean offline;
    private final Duration latestTtl;
    private final Clock clock;
    private final URI githubApi;
    private final Optional<String> githubToken;

    /**
     * A cache that asks api.github.com for GitHub tags and releases, with the
     * token in the environment variable {@code GITHUB_TOKEN} if it is set.
     *
     * @param root the cache folder; created when the first release is stored
     * @param downloader fetches the files
     * @param offline true to download nothing
     * @param latestTtl how long a latest release and a release index are kept;
     * null for {@link #DEFAULT_LATEST_TTL}
     */
    public VocabularyCache(Path root, Downloader downloader, boolean offline, Duration latestTtl) {
        this(root, downloader, offline, latestTtl, Clock.systemUTC(), GITHUB_API,
            Optional.ofNullable(System.getenv("GITHUB_TOKEN")).filter(t -> !t.isBlank()));
    }

    /**
     * @param clock the time source for the latest and release index files
     * @param githubApi the base URL of the GitHub API
     * @param githubToken sent as {@code Authorization: Bearer} to the GitHub API
     */
    VocabularyCache(Path root, Downloader downloader, boolean offline, Duration latestTtl, Clock clock,
        URI githubApi, Optional<String> githubToken) {
        this.root = root.toAbsolutePath().normalize();
        this.downloader = Objects.requireNonNull(downloader, "downloader");
        this.offline = offline;
        this.latestTtl = latestTtl == null ? DEFAULT_LATEST_TTL : latestTtl;
        this.clock = Objects.requireNonNull(clock, "clock");
        String api = githubApi.toString();
        this.githubApi = URI.create(api.endsWith("/") ? api.substring(0, api.length() - 1) : api);
        this.githubToken = Objects.requireNonNull(githubToken, "githubToken");
    }

    /**
     * The default cache folder: {@code $XDG_CACHE_HOME/jmztabm/cv}, else
     * {@code ~/.cache/jmztabm/cv}.
     *
     * @return the folder
     */
    public static Path defaultRoot() {
        String xdg = System.getenv("XDG_CACHE_HOME");
        Path base = xdg != null && !xdg.isBlank() && Path.of(xdg).isAbsolute()
            ? Path.of(xdg)
            : Path.of(System.getProperty("user.home"), ".cache");
        return base.resolve("jmztabm").resolve("cv");
    }

    // ---- releases ----

    /**
     * A release, from the cache or downloaded into it. Any version is tried at
     * the entry's location, whether or not its release index lists it.
     *
     * @param e the vocabulary
     * @param version the release name
     * @return the cached release
     * @throws ResolutionException if the release cannot be had: NO_SUCH_RELEASE
     * (404, or a name that is not one path segment), DOWNLOAD_FAILED,
     * CHECKSUM_MISMATCH, PARSE_FAILED, WRITE_FAILED or OFFLINE_NOT_CACHED
     */
    public CachedRelease get(VocabularyEntry e, String version) throws ResolutionException {
        URI url;
        try {
            url = e.location().url(version);
        } catch (IllegalArgumentException x) {
            throw new ResolutionException(Reason.NO_SUCH_RELEASE, "\"" + version
                + "\" is not a release name: it must be one URL path segment of letters, digits, '.', '_' and '-'");
        }
        Path keyFolder = keyFolder(e);
        Path folder = keyFolder.resolve(version);
        Optional<CachedRelease> cached = readRelease(folder);
        if (cached.isEmpty()) {
            if (offline) {
                throw new ResolutionException(Reason.OFFLINE_NOT_CACHED,
                    "downloads are off, and the cache holds no copy in " + folder);
            }
            ReentrantLock lock = lock(folder);
            try {
                cached = readRelease(folder);
                if (cached.isEmpty()) {
                    cached = Optional.of(build(e, version, url, keyFolder, folder));
                }
            } finally {
                lock.unlock();
            }
        }
        CachedRelease release = cached.get();
        String known = e.knownSha256().get(version);
        if (known != null && !known.equalsIgnoreCase(release.sha256())) {
            throw new ResolutionException(Reason.CHECKSUM_MISMATCH, "the cached copy in " + folder
                + " has SHA-256 " + release.sha256() + ", but the resources file expects " + known
                + "; remove the folder to download it again");
        }
        return release;
    }

    private CachedRelease build(VocabularyEntry e, String version, URI url, Path keyFolder, Path folder)
        throws ResolutionException {
        Path temporary = createTemporaryFolder(keyFolder);
        try {
            Path original = temporary.resolve(fileName(url, "release." + e.location().format().extension()));
            Fetched fetched = download(url, original, e.key() + " " + version, "");
            String known = e.knownSha256().get(version);
            if (known != null && !known.equalsIgnoreCase(fetched.sha256())) {
                throw new ResolutionException(Reason.CHECKSUM_MISMATCH, "the file at " + url + " has SHA-256 "
                    + fetched.sha256() + ", but the resources file expects " + known);
            }
            List<CachedRelease> imports;
            try (TermIndexWriter writer = TermIndexWriter.open(temporary.resolve(INDEX))) {
                imports = readTerms(e.location().format(), original, url, writer);
                commit(writer);
            } catch (IOException x) {
                // opening or closing the writer
                throw writeFailed(x);
            }
            ObjectNode json = JSON.createObjectNode();
            json.put("key", e.key());
            json.put("version", version);
            json.put("source", url.toString());
            json.put("finalUrl", fetched.location().toString());
            json.put("sha256", fetched.sha256());
            json.put("original", original.getFileName().toString());
            json.put("index", INDEX);
            ArrayNode list = json.putArray("imports");
            for (CachedRelease i : imports) {
                ObjectNode item = list.addObject();
                item.put("key", i.key());
                item.put("version", i.version());
                item.put("source", i.source().toString());
                item.put("sha256", i.sha256());
                item.put("original", relative(folder, i.original()));
                item.putNull("index");
            }
            json.put("downloaded", clock.instant().toString());
            writeJson(temporary.resolve(RELEASE_JSON), json);
            publish(temporary, folder);
            return readRelease(folder).orElseThrow(() -> new ResolutionException(Reason.WRITE_FAILED,
                "the release folder " + folder + " vanished after it was written"));
        } finally {
            deleteQuietly(temporary);
        }
    }

    /**
     * Reads the terms of a release, and of its imports if it is OWL, into one
     * writer.
     *
     * @return the imports read
     */
    private List<CachedRelease> readTerms(Format format, Path file, URI url, Consumer<TermRecord> writer)
        throws ResolutionException {
        List<String> direct = new ArrayList<>();
        parse(format, file, writer, direct::add, v -> {
        }, "the file from " + url);
        List<CachedRelease> imports = new ArrayList<>();
        Set<String> read = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>(direct);
        while (!queue.isEmpty()) {
            String iri = queue.poll();
            // the cycle guard: each IRI is read once
            if (read.add(iri)) {
                List<String> next = new ArrayList<>();
                imports.add(readImport(iri, writer, next::add));
                queue.addAll(next);
            }
        }
        return imports;
    }

    // ---- imports ----

    /**
     * Reads one import into the writer, from the cache or downloaded into it,
     * and reports the imports it declares.
     */
    private CachedRelease readImport(String iri, Consumer<TermRecord> writer, Consumer<String> imports)
        throws ResolutionException {
        URI uri = importUri(iri);
        Path folder = root.resolve(IMPORTS).resolve(sha256(iri.getBytes(StandardCharsets.UTF_8)).substring(0, 16));
        Optional<CachedRelease> cached;
        ReentrantLock lock = lock(folder);
        try {
            cached = readImportEntry(folder, uri);
            if (cached.isEmpty()) {
                return downloadImport(iri, uri, folder, writer, imports);
            }
        } finally {
            lock.unlock();
        }
        parse(Format.OWL_RDFXML, cached.get().original(), writer, imports, v -> {
        }, "import " + iri);
        return cached.get();
    }

    /** Downloads an import into its folder, reading it into the writer on the way. */
    private CachedRelease downloadImport(String iri, URI uri, Path folder, Consumer<TermRecord> writer,
        Consumer<String> imports) throws ResolutionException {
        if (offline) {
            throw new ResolutionException(Reason.OFFLINE_NOT_CACHED,
                "downloads are off, and the cache holds no copy of import " + iri + " in " + folder);
        }
        Path temporary = createTemporaryFolder(folder.getParent());
        try {
            Path original = temporary.resolve(fileName(uri, "import.owl"));
            Fetched fetched = download(uri, original, "import " + iri, "import " + iri + ": ");
            List<String> versionIris = new ArrayList<>();
            parse(Format.OWL_RDFXML, original, writer, imports, versionIris::add, "import " + iri);
            String[] keyAndVersion = importKeyAndVersion(iri, versionIris);
            ObjectNode json = JSON.createObjectNode();
            json.put("key", keyAndVersion[0]);
            json.put("version", keyAndVersion[1]);
            json.put("iri", iri);
            json.put("finalUrl", fetched.location().toString());
            json.put("sha256", fetched.sha256());
            json.put("original", original.getFileName().toString());
            json.put("downloaded", clock.instant().toString());
            writeJson(temporary.resolve(RELEASE_JSON), json);
            publish(temporary, folder);
        } finally {
            deleteQuietly(temporary);
        }
        return readImportEntry(folder, uri).orElseThrow(() -> new ResolutionException(Reason.WRITE_FAILED,
            "the import folder " + folder + " vanished after it was written"));
    }

    private static URI importUri(String iri) throws ResolutionException {
        try {
            URI uri = new URI(iri);
            String scheme = String.valueOf(uri.getScheme()).toLowerCase(Locale.ROOT);
            if (scheme.equals("http") || scheme.equals("https")) {
                return uri;
            }
        } catch (java.net.URISyntaxException e) {
            // reported below
        }
        throw new ResolutionException(Reason.DOWNLOAD_FAILED, "import " + iri + ": not an http(s) IRI");
    }

    /**
     * The key and version of an import (Ruling R-L): the OBO ontology id of a
     * {@code purl.obolibrary.org/obo/} IRI, else a slug of its host and path;
     * the version segment of a versioned PURL, else of its
     * {@code owl:versionIRI}, else the versionIRI itself, else
     * {@code unversioned}.
     */
    static String[] importKeyAndVersion(String iri, List<String> versionIris) {
        String key;
        Optional<String> version = purlVersion(iri);
        if (iri.startsWith(OBO_PURL)) {
            String[] segments = iri.substring(OBO_PURL.length()).split("/");
            key = segments.length == 1 ? segments[0].replaceFirst("\\.(owl|obo)$", "") : segments[0];
        } else {
            URI uri = URI.create(iri);
            key = (String.valueOf(uri.getHost()) + uri.getRawPath()).replaceAll("[^A-Za-z0-9._-]+", "_")
                .replaceAll("^_+|_+$", "");
        }
        if (version.isEmpty() && !versionIris.isEmpty()) {
            String versionIri = versionIris.get(0);
            version = Optional.of(purlVersion(versionIri).orElse(versionIri));
        }
        return new String[] {key, version.orElse(UNVERSIONED)};
    }

    /** The version of {@code obo/<id>/releases/<v>/<file>} or {@code obo/<id>/<v>/<file>}. */
    private static Optional<String> purlVersion(String iri) {
        if (!iri.startsWith(OBO_PURL)) {
            return Optional.empty();
        }
        String[] segments = iri.substring(OBO_PURL.length()).split("/");
        String candidate = null;
        if (segments.length == 4 && segments[1].equals("releases")) {
            candidate = segments[2];
        } else if (segments.length == 3) {
            candidate = segments[1];
        }
        return candidate != null && IMPORT_VERSION.matcher(candidate).matches()
            ? Optional.of(candidate) : Optional.empty();
    }

    private Optional<CachedRelease> readImportEntry(Path folder, URI iri) throws ResolutionException {
        Optional<JsonNode> json = readJsonIfPresent(folder.resolve(RELEASE_JSON));
        if (json.isEmpty()) {
            return Optional.empty();
        }
        JsonNode j = json.get();
        return Optional.of(new CachedRelease(text(j, "key", folder), text(j, "version", folder), iri,
            text(j, "sha256", folder), inCache(folder, text(j, "original", folder)), null, List.of()));
    }

    // ---- reading release.json ----

    private Optional<CachedRelease> readRelease(Path folder) throws ResolutionException {
        Optional<JsonNode> json = readJsonIfPresent(folder.resolve(RELEASE_JSON));
        if (json.isEmpty()) {
            return Optional.empty();
        }
        JsonNode j = json.get();
        List<CachedRelease> imports = new ArrayList<>();
        for (JsonNode i : j.path("imports")) {
            imports.add(new CachedRelease(text(i, "key", folder), text(i, "version", folder),
                uri(text(i, "source", folder), folder), text(i, "sha256", folder),
                inCache(folder, text(i, "original", folder)), null, List.of()));
        }
        return Optional.of(new CachedRelease(text(j, "key", folder), text(j, "version", folder),
            uri(text(j, "source", folder), folder), text(j, "sha256", folder),
            inCache(folder, text(j, "original", folder)), inCache(folder, text(j, "index", folder)), imports));
    }

    private Optional<JsonNode> readJsonIfPresent(Path file) throws ResolutionException {
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(JSON.readTree(file.toFile()));
        } catch (IOException e) {
            throw new ResolutionException(Reason.PARSE_FAILED, "the cache file " + file + " cannot be read: "
                + message(e), e);
        }
    }

    private static String text(JsonNode node, String field, Path folder) throws ResolutionException {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual()) {
            throw new ResolutionException(Reason.PARSE_FAILED,
                "the cache file " + folder.resolve(RELEASE_JSON) + " has no " + field);
        }
        return value.asText();
    }

    private static URI uri(String value, Path folder) throws ResolutionException {
        try {
            return new URI(value);
        } catch (java.net.URISyntaxException e) {
            throw new ResolutionException(Reason.PARSE_FAILED,
                "the cache file " + folder.resolve(RELEASE_JSON) + " has an invalid URL: " + value);
        }
    }

    /** A path of release.json, relative to its folder, that must stay in the cache. */
    private Path inCache(Path folder, String relative) throws ResolutionException {
        Path path = folder.resolve(relative).normalize();
        if (!path.startsWith(root)) {
            throw new ResolutionException(Reason.PARSE_FAILED,
                "the cache file " + folder.resolve(RELEASE_JSON) + " names a file outside the cache: " + relative);
        }
        return path;
    }

    private static String relative(Path folder, Path file) {
        return folder.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/");
    }

    // ---- downloading and parsing ----

    private record Fetched(String sha256, URI location) {
    }

    /**
     * Downloads into {@code target}, computing the SHA-256 on the way.
     *
     * @param what the INFO log's name of the download, e.g. MS 4.1.200
     * @param prefix put before the reason messages
     */
    private Fetched download(URI url, Path target, String what, String prefix) throws ResolutionException {
        InputStream in;
        try {
            in = downloader.get(url);
        } catch (Downloader.NotFoundException e) {
            String redirected = e.uri() == null || e.uri().equals(url) ? "" : " (redirected to " + e.uri() + ")";
            throw new ResolutionException(Reason.NO_SUCH_RELEASE, prefix + "not found at " + url + redirected, e);
        } catch (IOException e) {
            throw new ResolutionException(Reason.DOWNLOAD_FAILED,
                prefix + "the download from " + url + " failed: " + message(e), e);
        }
        URI location = url;
        OptionalLong length = OptionalLong.empty();
        if (in instanceof Downloader.Download d) {
            location = d.location();
            length = d.contentLength();
        }
        LOG.info("Downloading {} ({}) from {}", what, size(length), location);
        MessageDigest digest = sha256();
        long count = 0;
        try (OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int n;
                try {
                    n = in.read(buffer);
                } catch (IOException e) {
                    throw new ResolutionException(Reason.DOWNLOAD_FAILED,
                        prefix + "the download from " + location + " broke off after " + count + " bytes: "
                        + message(e), e);
                }
                if (n < 0) {
                    break;
                }
                digest.update(buffer, 0, n);
                out.write(buffer, 0, n);
                count += n;
            }
        } catch (IOException e) {
            throw writeFailed(e);
        } finally {
            closeQuietly(in);
        }
        if (length.isPresent() && length.getAsLong() != count) {
            throw new ResolutionException(Reason.DOWNLOAD_FAILED, prefix + "the download from " + location
                + " ended after " + count + " of " + length.getAsLong() + " bytes");
        }
        force(target);
        return new Fetched(HexFormat.of().formatHex(digest.digest()), location);
    }

    /** Reads a file of the given format into the sink; OWL also reports its imports and versionIRIs. */
    private static void parse(Format format, Path file, Consumer<TermRecord> sink, Consumer<String> imports,
        Consumer<String> versionIris, String what) throws ResolutionException {
        try (InputStream in = open(file, format)) {
            if (format == Format.OWL_RDFXML) {
                new OwlRdfXmlReader().read(in, sink, imports, versionIris);
            } else {
                new OboReader().read(in, sink, v -> {
                });
            }
        } catch (UncheckedIOException e) {
            // the index writer's spill file
            throw writeFailed(e.getCause());
        } catch (IOException e) {
            throw new ResolutionException(Reason.PARSE_FAILED, what + " is not " + format + ": " + message(e), e);
        }
    }

    private static InputStream open(Path file, Format format) throws IOException {
        InputStream in = new BufferedInputStream(Files.newInputStream(file), BUFFER_SIZE);
        if (format != Format.OBO_GZ) {
            return in;
        }
        try {
            return new GZIPInputStream(in, BUFFER_SIZE);
        } catch (IOException e) {
            in.close();
            throw e;
        }
    }

    /** The URL's last path segment, if it makes a safe file name, else the fallback. */
    private static String fileName(URI url, String fallback) {
        String path = String.valueOf(url.getPath());
        String name = path.substring(path.lastIndexOf('/') + 1);
        return SAFE_NAME.matcher(name).matches() && !name.equals(INDEX) && !name.equals(RELEASE_JSON)
            ? name : fallback;
    }

    static String size(OptionalLong length) {
        if (length.isEmpty()) {
            return "size unknown";
        }
        long n = length.getAsLong();
        if (n < 1000) {
            return n + " bytes";
        }
        String[] units = {"kB", "MB", "GB", "TB"};
        double value = n / 1000.0;
        int unit = 0;
        while (value >= 1000 && unit < units.length - 1) {
            value /= 1000;
            unit++;
        }
        return String.format(Locale.ROOT, "%.1f %s", value, units[unit]);
    }

    // ---- latest and the release index ----

    /**
     * The latest release of a vocabulary, as a concrete version. The answer
     * is kept in {@code <key>/latest.json} for the time to live; offline, the
     * kept answer is used whatever its age.
     *
     * @param e the vocabulary
     * @return the latest release, or empty if none can be named (no tag, no
     * listed release, or a header without a version)
     * @throws ResolutionException DOWNLOAD_FAILED, PARSE_FAILED or
     * OFFLINE_NOT_CACHED
     */
    public Optional<String> latest(VocabularyEntry e) throws ResolutionException {
        Path file = keyFolder(e).resolve(LATEST_JSON);
        String definition = e.scheme() + " " + e.latest()
            + (e.latest() instanceof Latest.NewestRelease ? " of " + e.releases() : "");
        Optional<Kept> kept = readKept(file, definition);
        if (offline) {
            if (kept.isPresent()) {
                return version(kept.get().json());
            }
            if (e.latest() instanceof Latest.NewestRelease) {
                return newest(e.scheme(), releases(e));
            }
            throw new ResolutionException(Reason.OFFLINE_NOT_CACHED,
                "downloads are off, and the cache holds no latest release in " + file);
        }
        if (kept.isPresent() && fresh(kept.get())) {
            return version(kept.get().json());
        }
        ReentrantLock lock = lock(file);
        try {
            kept = readKept(file, definition);
            if (kept.isPresent() && fresh(kept.get())) {
                return version(kept.get().json());
            }
            Optional<String> latest = lookUpLatest(e);
            ObjectNode json = keptJson(definition);
            json.put("version", latest.orElse(null));
            keep(file, json);
            return latest;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The releases of a vocabulary, oldest first by its scheme, each with its
     * date if known. Read like {@link #latest} and kept in
     * {@code <key>/releases.json}; a listed index is answered as it is.
     *
     * @param e the vocabulary
     * @return the releases whose names fit the scheme
     * @throws ResolutionException DOWNLOAD_FAILED or OFFLINE_NOT_CACHED
     */
    public List<Release> releases(VocabularyEntry e) throws ResolutionException {
        if (e.releases() instanceof ReleaseIndex.Listed listed) {
            return ordered(e.scheme(), listed.releases());
        }
        Path file = keyFolder(e).resolve(RELEASES_JSON);
        String definition = e.scheme() + " " + e.releases();
        Optional<Kept> kept = readKept(file, definition);
        if (offline) {
            return releasesOf(kept.orElseThrow(() -> new ResolutionException(Reason.OFFLINE_NOT_CACHED,
                "downloads are off, and the cache holds no release index in " + file)).json(), file);
        }
        if (kept.isPresent() && fresh(kept.get())) {
            return releasesOf(kept.get().json(), file);
        }
        ReentrantLock lock = lock(file);
        try {
            kept = readKept(file, definition);
            if (kept.isPresent() && fresh(kept.get())) {
                return releasesOf(kept.get().json(), file);
            }
            List<Release> releases = ordered(e.scheme(), readReleaseIndex(e));
            ObjectNode json = keptJson(definition);
            ArrayNode list = json.putArray("releases");
            for (Release r : releases) {
                ObjectNode item = list.addObject();
                item.put("version", r.version());
                item.put("date", r.date().map(LocalDate::toString).orElse(null));
            }
            keep(file, json);
            return releases;
        } finally {
            lock.unlock();
        }
    }

    private Optional<String> lookUpLatest(VocabularyEntry e) throws ResolutionException {
        Latest latest = e.latest();
        if (latest instanceof Latest.GithubLatestTag tag) {
            return newest(e.scheme(), githubTags(tag.repo()).stream()
                .map(tag::versionOf).flatMap(Optional::stream).map(v -> new Release(v, Optional.empty())).toList());
        }
        if (latest instanceof Latest.Header header) {
            return headerVersion(header.url(), e.location().format(), e.scheme());
        }
        return newest(e.scheme(), releases(e));
    }

    private List<Release> readReleaseIndex(VocabularyEntry e) throws ResolutionException {
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
    private static List<Release> ordered(VersionScheme scheme, List<Release> releases) {
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

    private static Optional<String> newest(VersionScheme scheme, List<Release> releases) {
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
                    + " cannot be read from " + uri + ": " + message(e) + rateLimitHint(e), e);
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
                "the release listing at " + url + " cannot be read: " + message(e), e);
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
                throw new IOException("the answer is larger than " + size(OptionalLong.of(MAX_INDEX_BYTES)));
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
            throw new ResolutionException(Reason.DOWNLOAD_FAILED,
                "the latest release's file at " + url + " cannot be read: " + message(e), e);
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
            throw new ResolutionException(Reason.DOWNLOAD_FAILED,
                "the latest release's file at " + url + " cannot be read: " + message((IOException) e.getCause()), e);
        } catch (IOException e) {
            if (values.isEmpty()) {
                throw new ResolutionException(Reason.PARSE_FAILED,
                    "the latest release's file at " + url + " is not " + format + ": " + message(e), e);
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

    // ---- the latest.json and releases.json files ----

    private record Kept(JsonNode json, Instant time) {
    }

    private ObjectNode keptJson(String definition) {
        ObjectNode json = JSON.createObjectNode();
        json.put("definition", definition);
        json.put("time", clock.instant().toString());
        return json;
    }

    /** The kept answer, if there is one for this definition of the entry. */
    private static Optional<Kept> readKept(Path file, String definition) {
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            JsonNode json = JSON.readTree(file.toFile());
            if (!definition.equals(json.path("definition").asText(null))) {
                return Optional.empty();
            }
            return Optional.of(new Kept(json, Instant.parse(json.path("time").asText())));
        } catch (IOException | DateTimeParseException e) {
            LOG.warn("The cache file {} cannot be read and is ignored: {}", file, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean fresh(Kept kept) {
        return clock.instant().isBefore(kept.time().plus(latestTtl));
    }

    private static Optional<String> version(JsonNode json) {
        JsonNode v = json.get("version");
        return v == null || v.isNull() ? Optional.empty() : Optional.of(v.asText());
    }

    private static List<Release> releasesOf(JsonNode json, Path file) throws ResolutionException {
        List<Release> releases = new ArrayList<>();
        for (JsonNode r : json.path("releases")) {
            String version = r.path("version").asText(null);
            if (version == null) {
                throw new ResolutionException(Reason.PARSE_FAILED,
                    "the cache file " + file + " has a release without a version");
            }
            JsonNode date = r.get("date");
            try {
                releases.add(new Release(version,
                    date == null || date.isNull() ? Optional.empty() : Optional.of(LocalDate.parse(date.asText()))));
            } catch (DateTimeParseException e) {
                throw new ResolutionException(Reason.PARSE_FAILED, "the cache file " + file + " has an invalid date: "
                    + date.asText(), e);
            }
        }
        return List.copyOf(releases);
    }

    /** Replaces a kept file atomically; a failure only costs the next lookup. */
    private static void keep(Path file, JsonNode json) {
        Path temporary = file.resolveSibling(TEMPORARY + UUID.randomUUID() + ".json");
        try {
            Files.createDirectories(file.getParent());
            writeJson(temporary, json);
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | ResolutionException e) {
            LOG.warn("The cache file {} cannot be written: {}", file, e.getMessage());
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // nothing more to do
            }
        }
    }

    // ---- folders, files and locks ----

    private Path keyFolder(VocabularyEntry e) {
        if (!SAFE_NAME.matcher(e.key()).matches()) {
            throw new IllegalArgumentException("not a vocabulary key that fits a folder name: " + e.key());
        }
        return root.resolve(e.key());
    }

    private static ReentrantLock lock(Path path) {
        ReentrantLock lock = LOCKS.computeIfAbsent(path, p -> new ReentrantLock());
        lock.lock();
        return lock;
    }

    private static Path createTemporaryFolder(Path parent) throws ResolutionException {
        try {
            Files.createDirectories(parent);
            return Files.createDirectory(parent.resolve(TEMPORARY + UUID.randomUUID()));
        } catch (IOException e) {
            throw writeFailed(e);
        }
    }

    /**
     * Renames a complete temporary folder onto its entry. If the entry exists,
     * another process won: its copy is used. A folder in the way that is no
     * entry (no release.json) is moved aside and removed first.
     */
    private static void publish(Path temporary, Path folder) throws ResolutionException {
        try {
            Files.move(temporary, folder, StandardCopyOption.ATOMIC_MOVE);
            return;
        } catch (IOException e) {
            if (isEntry(folder)) {
                return;
            }
            if (!Files.isDirectory(folder)) {
                throw writeFailed(e);
            }
        }
        // a stale folder that is no entry: an interrupted copy into the cache
        Path aside = folder.resolveSibling(TEMPORARY + UUID.randomUUID());
        try {
            Files.move(folder, aside, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            if (isEntry(folder)) {
                return;
            }
            throw writeFailed(e);
        }
        if (isEntry(aside)) {
            // it became an entry meanwhile: put it back and use it
            try {
                Files.move(aside, folder, StandardCopyOption.ATOMIC_MOVE);
                return;
            } catch (IOException e) {
                // fall through to ours
            }
        }
        LOG.info("Replacing {}, a folder without {}", folder, RELEASE_JSON);
        deleteQuietly(aside);
        try {
            Files.move(temporary, folder, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            if (!isEntry(folder)) {
                throw writeFailed(e);
            }
        }
    }

    private static boolean isEntry(Path folder) {
        return Files.isRegularFile(folder.resolve(RELEASE_JSON));
    }

    private static void writeJson(Path file, JsonNode json) throws ResolutionException {
        try {
            Files.write(file, JSON.writerWithDefaultPrettyPrinter().writeValueAsBytes(json),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            force(file);
        } catch (IOException e) {
            throw writeFailed(e);
        }
    }

    /** Puts the file on disk before the rename makes it visible. */
    private static void force(Path file) throws ResolutionException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(true);
        } catch (IOException e) {
            throw writeFailed(e);
        }
    }

    private static void commit(TermIndexWriter writer) throws ResolutionException {
        try {
            writer.commit();
        } catch (IOException e) {
            throw writeFailed(e);
        }
    }

    private static void closeQuietly(InputStream in) {
        try {
            in.close();
        } catch (IOException e) {
            LOG.debug("Closing a download failed: {}", e.getMessage());
        }
    }

    private static void deleteQuietly(Path folder) {
        if (!Files.exists(folder)) {
            return;
        }
        try (Stream<Path> files = Files.walk(folder)) {
            for (Path p : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
        } catch (IOException | UncheckedIOException e) {
            LOG.warn("The temporary folder {} cannot be removed: {}", folder, e.getMessage());
        }
    }

    private static ResolutionException writeFailed(IOException e) {
        return new ResolutionException(Reason.WRITE_FAILED, "the cache folder cannot be written: " + message(e), e);
    }

    /** An I/O failure's message; a file system failure without a reason gets its kind. */
    static String message(IOException e) {
        String m = e.getMessage();
        if (e instanceof FileSystemException fs && fs.getReason() == null) {
            return m + ": " + e.getClass().getSimpleName();
        }
        return m == null || m.isBlank() ? e.getClass().getSimpleName() : m;
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("every JVM has SHA-256", e);
        }
    }

    private static String sha256(byte[] data) {
        return HexFormat.of().formatHex(sha256().digest(data));
    }
}
