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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.lifstools.mztab2.cvmapping.local.ResolutionException.Reason;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Format;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Latest;
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
 * &lt;key&gt;/&lt;version&gt;/           a release: the file as downloaded, its index terms-v&lt;n&gt;.idx
 *                            (n: {@link TermIndex#VERSION}), release.json
 * &lt;key&gt;/latest.json          the latest release, with the time it was looked up
 * &lt;key&gt;/releases.json        the release index, with the time it was read
 * _imports/&lt;hash&gt;/           an OWL import: the file and release.json; hash is the
 *                            first 16 hex digits of the SHA-256 of its IRI
 * </pre>
 * <p>
 * A release folder is built as {@code <key>/.tmp-<uuid>/}, with release.json
 * written last, and appears by one atomic rename. A folder without
 * release.json is not an entry: if it holds only the vocabulary file, under
 * the name a download would give it, it is a pre-filled release and is
 * adopted in place; anything else is never moved or deleted, and the
 * release fails until it is cleared. If another process renamed its copy first,
 * the loser deletes its own and uses the winner's. Temporary entries left
 * for a day by a killed process are removed when the next release of their
 * folder is built. In one JVM, one lock per
 * release folder makes concurrent requests for a release download it once.
 * The paths in release.json are relative to its folder, so a cache folder
 * can be copied elsewhere, or pre-filled with folders from another cache.</p>
 *
 * <p>
 * The term index of an OWL release holds its own terms and the terms of
 * every {@code owl:imports} read with it, recursively; each import is also
 * downloaded, checked and cached on its own. An import IRI on
 * {@code http://purl.obolibrary.org} is fetched over https; release.json
 * records the IRI as written, and the URL it came from.</p>
 *
 * <p>
 * Offline, nothing is downloaded: a release not in the cache is
 * {@link Reason#OFFLINE_NOT_CACHED}, and {@link #latest} and
 * {@link #releases} answer from their cached files whatever their age.
 * Without such a file, {@link #newestCached} names the newest release the
 * cache holds.</p>
 *
 * <p>
 * What a release index holds, and which release is the latest, is read over
 * the network by a {@link ReleaseIndexReader}; this class keeps its answers.</p>
 */
public final class VocabularyCache {

    /** How long a looked-up latest release, or a release index, is kept. */
    public static final Duration DEFAULT_LATEST_TTL = Duration.ofHours(24);

    static final URI GITHUB_API = URI.create("https://api.github.com");

    /** How long a temporary entry stays untouched before it counts as left over. */
    static final Duration STALE_TEMPORARY = Duration.ofHours(24);

    private static final Logger LOG = LoggerFactory.getLogger(VocabularyCache.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    /** One lock per cache file or folder, for every cache of the process. */
    private static final Map<Path, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    private static final String RELEASE_JSON = "release.json";
    /** OWL gives no parent names. */
    private static final BiConsumer<String, String> NO_NAMES = (id, name) -> {
    };
    /**
     * The index file of a release: its name names the index version, so that
     * builds of different versions sharing one cache each keep their own and
     * none rebuilds another's. An older build's index ({@code terms.idx},
     * {@code terms-v2.idx}) is left where it is and never read.
     */
    static final String INDEX = "terms-v" + TermIndex.VERSION + ".idx";
    /** Any build's index file, which a pre-filled release may hold beside its file. */
    private static final Pattern ANY_INDEX = Pattern.compile("terms(-v[0-9]+)?\\.idx");
    private static final String LATEST_JSON = "latest.json";
    private static final String RELEASES_JSON = "releases.json";
    private static final String IMPORTS = "_imports";
    private static final String TEMPORARY = ".tmp-";
    private static final String UNVERSIONED = "unversioned";
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");
    private static final String OBO_PURL = "http://purl.obolibrary.org/obo/";
    private static final String OBO_PURL_HOST = "purl.obolibrary.org";
    private static final Pattern IMPORT_VERSION = Pattern.compile("v?\\d[A-Za-z0-9._-]*");
    private static final int BUFFER_SIZE = 1 << 16;

    private final Path root;
    private final Downloader downloader;
    private final boolean offline;
    private final Duration latestTtl;
    private final Clock clock;
    private final ReleaseIndexReader reader;

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
        this.reader = new ReleaseIndexReader(downloader, githubApi, githubToken);
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
            if (offline && !Files.exists(folder)) {
                throw new ResolutionException(Reason.OFFLINE_NOT_CACHED,
                    "downloads are off, and the cache holds no copy in " + folder);
            }
            ReentrantLock lock = lock(folder);
            try {
                cached = readRelease(folder);
                if (cached.isEmpty()) {
                    if (Files.exists(folder)) {
                        cached = Optional.of(adopt(e, version, url, keyFolder, folder));
                    } else if (offline) {
                        throw new ResolutionException(Reason.OFFLINE_NOT_CACHED,
                            "downloads are off, and the cache holds no copy in " + folder);
                    } else {
                        cached = Optional.of(build(e, version, url, keyFolder, folder));
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        CachedRelease release = cached.get();
        // the resources file's checksum first: a refused release gets no index (minor 2 of the Task 11 review)
        String known = e.knownSha256().get(version);
        if (known != null && !known.equalsIgnoreCase(release.sha256())) {
            throw new ResolutionException(Reason.CHECKSUM_MISMATCH, "the cached copy in " + folder
                + " has SHA-256 " + release.sha256() + ", but the resources file expects " + known
                + "; remove the folder to download it again");
        }
        if (!TermIndex.isCurrent(release.index())) {
            reindex(e, release, url, keyFolder, folder);
        }
        return release;
    }

    private CachedRelease build(VocabularyEntry e, String version, URI url, Path keyFolder, Path folder)
        throws ResolutionException {
        Path temporary = createTemporaryFolder(keyFolder);
        try {
            Path original = temporary.resolve(expectedFileName(e, url));
            Fetched fetched = download(url, original, e.key() + " " + version, "");
            checkKnownSha256(e, version, fetched.sha256(), "the file at " + url);
            List<CachedRelease> imports = writeIndex(e.location().format(), original, url, temporary.resolve(INDEX),
                false);
            writeJson(temporary.resolve(RELEASE_JSON),
                releaseJson(e, version, url, fetched.location(), fetched.sha256(), original.getFileName().toString(),
                    imports, folder));
            publish(temporary, folder);
            return readRelease(folder).orElseThrow(() -> new ResolutionException(Reason.WRITE_FAILED,
                "the release folder " + folder + " vanished after it was written"));
        } finally {
            deleteQuietly(temporary);
        }
    }

    /**
     * Builds this version's index of a cached release that lacks it, e.g. one
     * an older build wrote (Ruling R-AB), from the cached file and its cached
     * imports: no download (it works offline), and release.json, with its
     * SHA-256, stays as it is. First the cached file and each import are
     * checked against the SHA-256 release.json records; a file that differs
     * or is missing is a corrupt cache entry, a CHECKSUM_MISMATCH, and is
     * neither downloaded again nor deleted (Ruling R-N). The new index is
     * built in a temporary folder and moved in atomically, under the
     * release's lock; nothing else in the folder is touched, including the
     * index files of other versions.
     */
    private void reindex(VocabularyEntry e, CachedRelease release, URI url, Path keyFolder, Path folder)
        throws ResolutionException {
        ReentrantLock lock = lock(folder);
        try {
            if (TermIndex.isCurrent(release.index())) {
                // another thread rebuilt it
                return;
            }
            checkCached(release.original(), release.sha256(), folder);
            for (CachedRelease i : release.imports()) {
                checkCached(i.original(), i.sha256(), folder);
            }
            OptionalInt old = TermIndex.version(release.index());
            LOG.info("Building the index {} of {} {} from the cached {}: {}", INDEX, e.key(), release.version(),
                release.original(), old.isPresent()
                    ? "the one there was written with index version " + old.getAsInt()
                    : Files.exists(release.index()) ? "the one there cannot be read"
                    : "it was cached by a build of another index version");
            Path temporary = createTemporaryFolder(keyFolder);
            try {
                List<CachedRelease> imports = writeIndex(e.location().format(), release.original(), url,
                    temporary.resolve(INDEX), true);
                if (!sameImports(imports, release.imports())) {
                    throw new ResolutionException(Reason.CHECKSUM_MISMATCH, "the cached " + release.original()
                        + " now reads the imports " + describe(imports) + ", but its " + folder.resolve(RELEASE_JSON)
                        + " records " + describe(release.imports()) + "; remove the folder to download it again");
                }
                Files.move(temporary.resolve(INDEX), release.index(), StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException x) {
                throw writeFailed(x);
            } finally {
                deleteQuietly(temporary);
            }
        } finally {
            lock.unlock();
        }
    }

    /** A cached file of a release must be there and have the SHA-256 its release.json records. */
    private static void checkCached(Path file, String recorded, Path folder) throws ResolutionException {
        String corrupt = "; the cache entry is corrupt: remove the folder " + folder + " to download it again";
        if (!Files.isRegularFile(file)) {
            throw new ResolutionException(Reason.CHECKSUM_MISMATCH, "the cached file " + file + " is missing, but "
                + folder.resolve(RELEASE_JSON) + " records it with SHA-256 " + recorded + corrupt);
        }
        String sha256 = sha256Of(file);
        if (!sha256.equalsIgnoreCase(recorded)) {
            throw new ResolutionException(Reason.CHECKSUM_MISMATCH, "the cached file " + file + " has SHA-256 "
                + sha256 + ", but " + folder.resolve(RELEASE_JSON) + " records " + recorded + corrupt);
        }
    }

    private static boolean sameImports(List<CachedRelease> read, List<CachedRelease> recorded) {
        return describe(read).equals(describe(recorded));
    }

    private static List<String> describe(List<CachedRelease> imports) {
        return imports.stream().map(i -> i.source() + " " + i.sha256().toLowerCase(Locale.ROOT)).toList();
    }

    /**
     * Adopts a folder without release.json (Ruling R-N): a pre-filled release
     * is the vocabulary file alone, under the name a download would give it
     * (index files, {@code terms.idx} or {@code terms-v<n>.idx}, that an
     * interrupted adoption or another build left may sit beside it; this
     * build's is replaced, the others are left as they are). Its index and
     * release.json are built in a temporary folder and moved in,
     * release.json last. Anything else is left untouched and fails.
     * Works offline, as long as an OWL release's imports are cached.
     */
    private CachedRelease adopt(VocabularyEntry e, String version, URI url, Path keyFolder, Path folder)
        throws ResolutionException {
        String name = expectedFileName(e, url);
        Path original = folder.resolve(name);
        List<String> names;
        try (Stream<Path> files = Files.list(folder)) {
            names = files.map(p -> p.getFileName().toString()).toList();
        } catch (IOException x) {
            throw notAnEntry(folder, name);
        }
        boolean prefilled = Files.isRegularFile(original) && names.stream()
            .allMatch(n -> n.equals(name) || ANY_INDEX.matcher(n).matches() && Files.isRegularFile(folder.resolve(n)));
        if (!prefilled) {
            throw notAnEntry(folder, name);
        }
        LOG.info("Adopting the pre-filled {} as {} {}", original, e.key(), version);
        String sha256 = sha256Of(original);
        checkKnownSha256(e, version, sha256, "the pre-filled file " + original);
        Path temporary = createTemporaryFolder(keyFolder);
        try {
            List<CachedRelease> imports = writeIndex(e.location().format(), original, url, temporary.resolve(INDEX),
                false);
            writeJson(temporary.resolve(RELEASE_JSON),
                releaseJson(e, version, url, null, sha256, name, imports, folder));
            try {
                Files.move(temporary.resolve(INDEX), folder.resolve(INDEX), StandardCopyOption.ATOMIC_MOVE);
                Files.move(temporary.resolve(RELEASE_JSON), folder.resolve(RELEASE_JSON),
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException x) {
                throw writeFailed(x);
            }
            return readRelease(folder).orElseThrow(() -> new ResolutionException(Reason.WRITE_FAILED,
                "the release folder " + folder + " has no " + RELEASE_JSON + " after it was written"));
        } finally {
            deleteQuietly(temporary);
        }
    }

    /** A folder at a release's place that is neither an entry nor a pre-filled release. */
    private ResolutionException notAnEntry(Path folder, String name) {
        return new ResolutionException(offline ? Reason.OFFLINE_NOT_CACHED : Reason.WRITE_FAILED,
            notAnEntryMessage(folder, "the vocabulary file " + name));
    }

    private static String notAnEntryMessage(Path folder, String file) {
        return "the folder " + folder + " holds no " + RELEASE_JSON + " and is not a pre-filled release;"
            + " remove it, or leave only " + file + " in it";
    }

    /** The name a release's file gets: the URL's last path segment if it makes a safe name. */
    private static String expectedFileName(VocabularyEntry e, URI url) {
        return fileName(url, "release." + e.location().format().extension());
    }

    private static void checkKnownSha256(VocabularyEntry e, String version, String sha256, String what)
        throws ResolutionException {
        String known = e.knownSha256().get(version);
        if (known != null && !known.equalsIgnoreCase(sha256)) {
            throw new ResolutionException(Reason.CHECKSUM_MISMATCH, what + " has SHA-256 " + sha256
                + ", but the resources file expects " + known);
        }
    }

    private static String sha256Of(Path file) throws ResolutionException {
        MessageDigest digest = sha256();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int n = in.read(buffer); n >= 0; n = in.read(buffer)) {
                digest.update(buffer, 0, n);
            }
        } catch (IOException x) {
            throw new ResolutionException(Reason.PARSE_FAILED, "the file " + file + " cannot be read: "
                + message(x), x);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * Writes the index of a release and its imports; the imports read.
     *
     * @param cachedImportsOnly true to fail, not download, if an import is not cached
     */
    private List<CachedRelease> writeIndex(Format format, Path original, URI url, Path index,
        boolean cachedImportsOnly) throws ResolutionException {
        try (TermIndexWriter writer = TermIndexWriter.open(index)) {
            List<CachedRelease> imports = readTerms(format, original, url, writer, cachedImportsOnly);
            commit(writer);
            return imports;
        } catch (IOException x) {
            // opening or closing the writer
            throw writeFailed(x);
        }
    }

    /**
     * The release.json of a release, its paths relative to {@code folder}. A
     * download (a final URL) records that URL and its time; a pre-filled
     * release (no final URL) {@code "prefilled": true} and the time it was
     * adopted.
     */
    private ObjectNode releaseJson(VocabularyEntry e, String version, URI url, URI finalUrl, String sha256,
        String original, List<CachedRelease> imports, Path folder) {
        ObjectNode json = JSON.createObjectNode();
        json.put("key", e.key());
        json.put("version", version);
        json.put("source", url.toString());
        if (finalUrl != null) {
            json.put("finalUrl", finalUrl.toString());
        }
        json.put("sha256", sha256);
        json.put("original", original);
        ArrayNode list = json.putArray("imports");
        for (CachedRelease i : imports) {
            ObjectNode item = list.addObject();
            item.put("key", i.key());
            item.put("version", i.version());
            item.put("source", i.source().toString());
            item.put("sha256", i.sha256());
            item.put("original", relative(folder, i.original()));
        }
        if (finalUrl != null) {
            json.put("downloaded", clock.instant().toString());
        } else {
            json.put("prefilled", true);
            json.put("adopted", clock.instant().toString());
        }
        return json;
    }

    /**
     * Reads the terms of a release, and of its imports if it is OWL, into one
     * writer.
     *
     * @return the imports read
     */
    private List<CachedRelease> readTerms(Format format, Path file, URI url, TermIndexWriter writer,
        boolean cachedImportsOnly) throws ResolutionException {
        List<String> direct = new ArrayList<>();
        parse(format, file, writer, writer::parentName, direct::add, v -> {
        }, "the file from " + url);
        List<CachedRelease> imports = new ArrayList<>();
        Set<String> read = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>(direct);
        while (!queue.isEmpty()) {
            String iri = queue.poll();
            // the cycle guard: each IRI is read once
            if (read.add(iri)) {
                List<String> next = new ArrayList<>();
                imports.add(readImport(iri, writer, next::add, cachedImportsOnly));
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
    private CachedRelease readImport(String iri, Consumer<TermRecord> writer, Consumer<String> imports,
        boolean cachedImportsOnly) throws ResolutionException {
        URI uri = importUri(iri);
        Path folder = root.resolve(IMPORTS).resolve(sha256(iri.getBytes(StandardCharsets.UTF_8)).substring(0, 16));
        Optional<CachedRelease> cached;
        ReentrantLock lock = lock(folder);
        try {
            cached = readImportEntry(folder, uri);
            if (cached.isEmpty()) {
                if (cachedImportsOnly) {
                    throw new ResolutionException(Reason.CHECKSUM_MISMATCH, "import " + iri + " is not cached in "
                        + folder + ", and the index of a cached release is built from cached files only");
                }
                return downloadImport(iri, uri, folder, writer, imports);
            }
        } finally {
            lock.unlock();
        }
        parse(Format.OWL_RDFXML, cached.get().original(), writer, NO_NAMES, imports, v -> {
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
            Fetched fetched = download(fetchUri(uri), original, "import " + iri, "import " + iri + ": ");
            List<String> versionIris = new ArrayList<>();
            parse(Format.OWL_RDFXML, original, writer, NO_NAMES, imports, versionIris::add, "import " + iri);
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

    /**
     * Where an import is fetched from (Ruling R-AG): an OBO PURL IRI,
     * {@code http://purl.obolibrary.org/...}, over https, as the PURL server
     * serves both and a release is cached and checksummed for good; any other
     * IRI as it is written. The IRI itself stays the import's key and source.
     */
    static URI fetchUri(URI iri) {
        if ("http".equalsIgnoreCase(iri.getScheme()) && OBO_PURL_HOST.equalsIgnoreCase(iri.getHost())
            && iri.getPort() == -1) {
            return URI.create("https" + iri.toString().substring(iri.getScheme().length()));
        }
        return iri;
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
    static Optional<String> purlVersion(String iri) {
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

    /**
     * The release of a folder, if it has a release.json. Its index is this
     * build's {@link #INDEX}, whatever the {@code "index"} that older builds
     * wrote into release.json names.
     */
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
            inCache(folder, text(j, "original", folder)), folder.resolve(INDEX), imports));
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
    private static void parse(Format format, Path file, Consumer<TermRecord> sink,
        BiConsumer<String, String> parentNames, Consumer<String> imports, Consumer<String> versionIris,
        String what) throws ResolutionException {
        try (InputStream in = open(file, format)) {
            if (format == Format.OWL_RDFXML) {
                new OwlRdfXmlReader().read(in, sink, imports, versionIris);
            } else {
                new OboReader().read(in, sink, v -> {
                }, parentNames);
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
     * kept answer is used whatever its age. Online, if looking it up again
     * fails to download (the source is down, or GitHub's rate limit is
     * reached), a kept answer is used whatever its age, with a warning that
     * names its age; the kept file stays as it was.
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
                return ReleaseIndexReader.newest(e.scheme(), releases(e));
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
            Optional<String> latest;
            try {
                latest = lookUpLatest(e);
            } catch (ResolutionException x) {
                if (x.reason() != Reason.DOWNLOAD_FAILED || kept.isEmpty()) {
                    throw x;
                }
                Optional<String> stale = version(kept.get().json());
                LOG.warn("The latest release of {} cannot be looked up, so {}, looked up {} ago, is used: {}",
                    e.key(), stale.orElse("none"), age(kept.get().time()), x.getMessage());
                return stale;
            }
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
            return ReleaseIndexReader.ordered(e.scheme(), listed.releases());
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
            List<Release> releases = reader.releases(e);
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

    /**
     * The newest release of a vocabulary that the cache holds (Ruling R-AF),
     * for an offline run that has no kept latest release: a folder
     * {@code <key>/<version>/} whose name fits the vocabulary's scheme and
     * that holds a release.json, or the file a download would give it (a
     * pre-filled release). Nothing is downloaded, written or removed, and the
     * answer is not kept: it is no latest release its source named.
     *
     * @param e the vocabulary
     * @return the newest such release, or empty if the cache holds none
     */
    Optional<String> newestCached(VocabularyEntry e) {
        Path keyFolder = keyFolder(e);
        if (!Files.isDirectory(keyFolder)) {
            return Optional.empty();
        }
        List<Release> held = new ArrayList<>();
        try (DirectoryStream<Path> folders = Files.newDirectoryStream(keyFolder, Files::isDirectory)) {
            for (Path folder : folders) {
                String version = folder.getFileName().toString();
                if (!e.scheme().accepts(version)) {
                    continue;
                }
                URI url;
                try {
                    url = e.location().url(version);
                } catch (IllegalArgumentException x) {
                    continue;
                }
                if (isEntry(folder) || Files.isRegularFile(folder.resolve(expectedFileName(e, url)))) {
                    held.add(new Release(version, Optional.empty()));
                }
            }
        } catch (IOException x) {
            LOG.debug("The releases in {} cannot be listed: {}", keyFolder, message(x));
            return Optional.empty();
        }
        return ReleaseIndexReader.newest(e.scheme(), held);
    }

    private Optional<String> lookUpLatest(VocabularyEntry e) throws ResolutionException {
        return e.latest() instanceof Latest.NewestRelease
            ? ReleaseIndexReader.newest(e.scheme(), releases(e))
            : reader.latest(e);
    }

    // ---- the latest.json and releases.json files ----

    private record Kept(JsonNode json, Instant time) {
    }

    private ObjectNode keptJson(String definition) {
        ObjectNode json = JSON.createObjectNode();
        json.put("definition", definition);
        json.put("time", clock.instant().toString());
        json.put("ttl", latestTtl.toString());
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

    /** How long ago a kept answer was looked up, in whole hours, or minutes below an hour. */
    private String age(Instant time) {
        Duration age = Duration.between(time, clock.instant());
        return age.toHours() >= 1 ? age.toHours() + " hours" : Math.max(0, age.toMinutes()) + " minutes";
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
            removeStaleTemporaries(parent);
            return Files.createDirectory(parent.resolve(TEMPORARY + UUID.randomUUID()));
        } catch (IOException e) {
            throw writeFailed(e);
        }
    }

    /**
     * Removes the {@code .tmp-*} entries of a folder that have not changed
     * for {@link #STALE_TEMPORARY}: a process that was killed while it
     * downloaded leaves them. The age is told by the file system's clock, not
     * the cache's. A download still running after that long, with no new
     * file in its folder, would lose its folder and fail with WRITE_FAILED.
     */
    private static void removeStaleTemporaries(Path parent) {
        Instant limit = Instant.now().minus(STALE_TEMPORARY);
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(parent, TEMPORARY + "*")) {
            for (Path entry : entries) {
                try {
                    if (Files.getLastModifiedTime(entry).toInstant().isBefore(limit)) {
                        LOG.info("Removing {}, left by an interrupted download", entry);
                        deleteQuietly(entry);
                    }
                } catch (IOException e) {
                    // removed meanwhile by another process, or unreadable: left as it is
                }
            }
        } catch (IOException e) {
            LOG.debug("The temporary entries of {} cannot be listed: {}", parent, e.getMessage());
        }
    }

    /**
     * Renames a complete temporary folder onto its entry. If the entry exists,
     * another process won: its copy is used. A folder in the way that is no
     * entry (no release.json) is never moved or deleted: it may be someone's
     * copy in progress.
     */
    private static void publish(Path temporary, Path folder) throws ResolutionException {
        try {
            Files.move(temporary, folder, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            if (isEntry(folder)) {
                return;
            }
            if (Files.isDirectory(folder)) {
                throw new ResolutionException(Reason.WRITE_FAILED,
                    notAnEntryMessage(folder, "the vocabulary file"), e);
            }
            throw writeFailed(e);
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
