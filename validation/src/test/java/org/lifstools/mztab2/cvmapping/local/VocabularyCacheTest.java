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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.lifstools.mztab2.cvmapping.local.ResolutionException.Reason;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Format;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Latest;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Location;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseDates;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseIndex;
import org.slf4j.LoggerFactory;

@Timeout(60)
public class VocabularyCacheTest {

    private static final String PURL = "http://purl.obolibrary.org/";
    private static final String MS_REPO = "HUPO-PSI/psi-ms-CV";
    private static final String MS_PATH = "/ms/v4.1.200/psi-ms.obo";
    private static final String TAGS = "/api/repos/" + MS_REPO + "/tags";
    private static final String GITHUB_RELEASES = "/api/repos/" + MS_REPO + "/releases";
    private static final Duration DAY = Duration.ofHours(24);

    @TempDir
    Path temp;

    private Server server;
    private Path root;
    private MutableClock clock;
    private byte[] miniMs;

    @BeforeEach
    public void start() throws IOException {
        server = new Server();
        root = temp.resolve("cache");
        clock = new MutableClock(Instant.parse("2026-11-11T10:00:00Z"));
        miniMs = resource("/cv/mini-ms.obo");
    }

    @AfterEach
    public void stop() {
        server.close();
    }

    // ---- the tests of the brief ----

    @Test
    public void aReleaseIsDownloadedOnceThenServedFromTheCache() throws Exception {
        server.file(MS_PATH, miniMs);
        VocabularyCache cache = cache(false);

        CachedRelease first = cache.get(ms(Map.of()), "4.1.200");
        CachedRelease second = cache.get(ms(Map.of()), "4.1.200");
        CachedRelease third = cache(false).get(ms(Map.of()), "4.1.200");

        assertEquals(1, server.hits(MS_PATH));
        assertEquals(first, second);
        assertEquals(first, third);
        Path folder = root.resolve("MS").resolve("4.1.200");
        assertEquals("MS", first.key());
        assertEquals("4.1.200", first.version());
        assertEquals(URI.create(server.base() + MS_PATH), first.source());
        assertEquals(sha256(miniMs), first.sha256());
        assertEquals(folder.resolve("psi-ms.obo"), first.original());
        assertEquals(folder.resolve("terms.idx"), first.index());
        assertEquals(List.of(), first.imports());
        assertArrayEquals(miniMs, Files.readAllBytes(first.original()));
        assertTrue(Files.isRegularFile(folder.resolve("release.json")));
        assertEquals("instrument model", TermIndexStore.open(first).term("MS:1000031").orElseThrow().label());
        assertNoTemporaryFolders();
    }

    @Test
    public void aKnownChecksumThatDiffersIsRefused() throws Exception {
        server.file(MS_PATH, miniMs);
        String wrong = "0".repeat(64);

        ResolutionException e = assertThrows(ResolutionException.class,
            () -> cache(false).get(ms(Map.of("4.1.200", wrong)), "4.1.200"));

        assertEquals(Reason.CHECKSUM_MISMATCH, e.reason());
        assertTrue(e.getMessage().contains(wrong), e.getMessage());
        assertTrue(e.getMessage().contains(sha256(miniMs)), e.getMessage());
        assertFalse(Files.exists(root.resolve("MS").resolve("4.1.200")));
        assertNoTemporaryFolders();
        // the right checksum is accepted
        CachedRelease r = cache(false).get(ms(Map.of("4.1.200", sha256(miniMs))), "4.1.200");
        assertEquals(sha256(miniMs), r.sha256());
    }

    @Test
    public void offlineWithoutACachedCopyIsOfflineNotCached() {
        server.file(MS_PATH, miniMs);
        VocabularyCache offline = cache(true);

        assertEquals(Reason.OFFLINE_NOT_CACHED,
            assertThrows(ResolutionException.class, () -> offline.get(ms(Map.of()), "4.1.200")).reason());
        assertEquals(Reason.OFFLINE_NOT_CACHED,
            assertThrows(ResolutionException.class, () -> offline.latest(ms(Map.of()))).reason());
        assertEquals(Reason.OFFLINE_NOT_CACHED,
            assertThrows(ResolutionException.class, () -> offline.releases(ms(Map.of()))).reason());
        assertEquals(0, server.totalHits());
    }

    @Test
    public void offlineWithACachedCopyWorks() throws Exception {
        server.file(MS_PATH, miniMs);
        serveMsTags(List.of("v4.1.200"));
        server.json(GITHUB_RELEASES, page -> "[]");
        VocabularyCache online = cache(false);
        CachedRelease cached = online.get(ms(Map.of()), "4.1.200");
        online.latest(ms(Map.of()));
        online.releases(ms(Map.of()));
        int hits = server.totalHits();

        // long after the latest answer's time to live
        clock.advance(Duration.ofDays(30));
        VocabularyCache offline = new VocabularyCache(root, failingDownloader(), true, DAY, clock,
            server.api(), Optional.empty());

        assertEquals(cached, offline.get(ms(Map.of()), "4.1.200"));
        assertEquals(Optional.of("4.1.200"), offline.latest(ms(Map.of())));
        assertEquals(List.of(new Release("4.1.200", Optional.empty())), offline.releases(ms(Map.of())));
        assertEquals(hits, server.totalHits());
    }

    @Test
    public void anInterruptedDownloadLeavesNoEntry() throws Exception {
        AtomicBoolean interrupt = new AtomicBoolean(true);
        server.route(MS_PATH, x -> {
            if (interrupt.getAndSet(false)) {
                x.sendResponseHeaders(200, miniMs.length);
                OutputStream out = x.getResponseBody();
                out.write(miniMs, 0, miniMs.length / 2);
                out.flush();
                // a handler that fails makes the server drop the connection
                throw new IOException("the server breaks off the download");
            } else {
                send(x, 200, miniMs);
            }
        });
        VocabularyCache cache = cache(false);

        ResolutionException e = assertThrows(ResolutionException.class,
            () -> cache.get(ms(Map.of()), "4.1.200"));

        assertEquals(Reason.DOWNLOAD_FAILED, e.reason());
        assertFalse(Files.exists(root.resolve("MS").resolve("4.1.200")));
        assertNoTemporaryFolders();
        CachedRelease r = cache.get(ms(Map.of()), "4.1.200");
        assertEquals(sha256(miniMs), r.sha256());
        assertEquals(2, server.hits(MS_PATH));
    }

    @Test
    public void aStalledDownloadTimesOut() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        server.route(MS_PATH, x -> {
            x.sendResponseHeaders(200, miniMs.length);
            OutputStream out = x.getResponseBody();
            out.write(miniMs, 0, 100);
            out.flush();
            try {
                release.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            throw new IOException("the stalled download ends");
        });
        VocabularyCache cache = new VocabularyCache(root, Downloader.http(Duration.ofMillis(500)), false, DAY,
            clock, server.api(), Optional.empty());
        try {
            long start = System.nanoTime();
            ResolutionException e = assertThrows(ResolutionException.class,
                () -> cache.get(ms(Map.of()), "4.1.200"));

            assertEquals(Reason.DOWNLOAD_FAILED, e.reason());
            assertTrue(e.getMessage().contains("no data for 500 ms"), e.getMessage());
            assertTrue(Duration.ofNanos(System.nanoTime() - start).compareTo(Duration.ofSeconds(10)) < 0);
            assertFalse(Files.exists(root.resolve("MS").resolve("4.1.200")));
            assertNoTemporaryFolders();
        } finally {
            release.countDown();
        }
    }

    @Test
    public void concurrentResolutionsDownloadOnce() throws Exception {
        server.route(MS_PATH, x -> {
            sleep(300);
            send(x, 200, miniMs);
        });
        VocabularyCache cache = cache(false);
        int threads = 8;
        CountDownLatch ready = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<CachedRelease>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                // half the threads use an engine of their own on the same folder
                VocabularyCache engine = i % 2 == 0 ? cache : cache(false);
                results.add(pool.submit(() -> {
                    ready.countDown();
                    ready.await();
                    return engine.get(ms(Map.of()), "4.1.200");
                }));
            }
            List<CachedRelease> releases = new ArrayList<>();
            for (Future<CachedRelease> f : results) {
                releases.add(f.get(30, TimeUnit.SECONDS));
            }
            assertEquals(1, server.hits(MS_PATH));
            TermIndex index = TermIndexStore.open(releases.get(0));
            for (CachedRelease r : releases) {
                assertEquals(releases.get(0), r);
                assertSame(index, TermIndexStore.open(r));
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void aFailedWriteLeavesNoEntryAndNamesTheReason() throws Exception {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
        server.file(MS_PATH, miniMs);
        Files.createDirectories(root);
        Files.setPosixFilePermissions(root, PosixFilePermissions.fromString("r-xr-xr-x"));
        try {
            assumeFalse(Files.isWritable(root), "the user can write to a read-only folder");

            ResolutionException e = assertThrows(ResolutionException.class,
                () -> cache(false).get(ms(Map.of()), "4.1.200"));

            assertEquals(Reason.WRITE_FAILED, e.reason());
            assertTrue(e.getMessage().contains(root.resolve("MS").toString()), e.getMessage());
            assertTrue(e.getMessage().contains("AccessDenied"), e.getMessage());
            assertFalse(e.getMessage().endsWith("."), e.getMessage());
            assertFalse(Files.exists(root.resolve("MS")));
        } finally {
            Files.setPosixFilePermissions(root, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
        // a read-only release folder: the temporary folder cannot be created
        Path key = Files.createDirectories(root.resolve("MS"));
        Files.setPosixFilePermissions(key, PosixFilePermissions.fromString("r-xr-xr-x"));
        try {
            ResolutionException e = assertThrows(ResolutionException.class,
                () -> cache(false).get(ms(Map.of()), "4.1.200"));
            assertEquals(Reason.WRITE_FAILED, e.reason());
            try (Stream<Path> files = Files.list(key)) {
                assertEquals(List.of(), files.toList());
            }
        } finally {
            Files.setPosixFilePermissions(key, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }

    @Test
    public void latestIsCachedForItsTtl() throws Exception {
        serveMsTags(List.of("v4.1.200", "v4.2.15", "v4.2.9", "nightly"));
        VocabularyCache cache = cache(false);

        assertEquals(Optional.of("4.2.15"), cache.latest(ms(Map.of())));
        assertEquals(Optional.of("4.2.15"), cache.latest(ms(Map.of())));
        clock.advance(Duration.ofHours(23));
        assertEquals(Optional.of("4.2.15"), cache(false).latest(ms(Map.of())));
        assertEquals(1, server.hits(TAGS));
        assertTrue(Files.isRegularFile(root.resolve("MS").resolve("latest.json")));

        serveMsTags(List.of("v4.1.200", "v4.2.15", "v4.2.16"));
        clock.advance(Duration.ofHours(2));
        assertEquals(Optional.of("4.2.16"), cache.latest(ms(Map.of())));
        assertEquals(2, server.hits(TAGS));
    }

    @Test
    public void anOwlReleaseCachesItsImports() throws Exception {
        byte[] msio = resource("/cv/mini-msio.owl");
        String ro = PURL + "obo/ro/releases/2024-04-24/ro-core.owl";
        // mini-obi imports ro-core, which imports mini-obi again: a cycle
        byte[] obi = new String(resource("/cv/mini-obi.owl"), StandardCharsets.UTF_8)
            .replace("<owl:Ontology rdf:about=\"http://purl.obolibrary.org/obo/mini-obi.owl\"/>",
                "<owl:Ontology rdf:about=\"http://purl.obolibrary.org/obo/mini-obi.owl\">"
                + "<owl:imports rdf:resource=\"" + ro + "\"/></owl:Ontology>")
            .getBytes(StandardCharsets.UTF_8);
        byte[] roCore = owl("http://purl.obolibrary.org/obo/ro.owl", PURL + "obo/mini-obi.owl",
            "http://purl.obolibrary.org/obo/RO_0002000", "boundary of");
        server.file("/msio/v1.0.1/msio.owl", msio);
        server.file("/msio/v1.0.2/msio.owl", msio);
        server.file("/obo/mini-obi.owl", obi);
        server.file("/obo/ro/releases/2024-04-24/ro-core.owl", roCore);
        VocabularyCache cache = new VocabularyCache(root, purlToServer(), false, DAY, clock, server.api(),
            Optional.empty());

        CachedRelease r = cache.get(msio(), "1.0.1");

        assertEquals(2, r.imports().size(), r.imports().toString());
        CachedRelease obiImport = r.imports().get(0);
        assertEquals("mini-obi", obiImport.key());
        assertEquals("unversioned", obiImport.version());
        assertEquals(URI.create(PURL + "obo/mini-obi.owl"), obiImport.source());
        assertEquals(sha256(obi), obiImport.sha256());
        assertNull(obiImport.index());
        assertEquals(List.of(), obiImport.imports());
        assertImportFolder(obiImport, PURL + "obo/mini-obi.owl", obi);
        CachedRelease roImport = r.imports().get(1);
        assertEquals("ro", roImport.key());
        assertEquals("2024-04-24", roImport.version());
        assertImportFolder(roImport, ro, roCore);
        // one index of the release and everything it imports
        TermIndex index = TermIndexStore.open(r);
        assertEquals("material processing", index.term("OBI:0000094").orElseThrow().label());
        assertEquals(List.of("OBI:0000011"),
            index.ancestors("OBI:0000094").stream().map(TermRecord::id).toList());
        assertEquals("boundary of", index.term("RO:0002000").orElseThrow().label());
        assertEquals("kept thing", index.term("MSIO:0000003").orElseThrow().label());
        assertEquals(1, server.hits("/obo/mini-obi.owl"));
        assertEquals(1, server.hits("/obo/ro/releases/2024-04-24/ro-core.owl"));
        // another release reads the cached imports
        CachedRelease next = cache.get(msio(), "1.0.2");
        assertEquals(r.imports(), next.imports());
        assertEquals(1, server.hits("/obo/mini-obi.owl"));
        assertTrue(TermIndexStore.open(next).term("RO:0002000").isPresent());
        // and the cached release reads its imports back
        assertEquals(r, cache(true).get(msio(), "1.0.1"));
    }

    @Test
    public void anImportThatCannotBeDownloadedFailsTheRelease() throws Exception {
        server.file("/msio/v1.0.1/msio.owl", resource("/cv/mini-msio.owl"));
        VocabularyCache cache = new VocabularyCache(root, purlToServer(), false, DAY, clock, server.api(),
            Optional.empty());

        ResolutionException missing = assertThrows(ResolutionException.class, () -> cache.get(msio(), "1.0.1"));

        assertEquals(Reason.NO_SUCH_RELEASE, missing.reason());
        assertTrue(missing.getMessage().startsWith("import " + PURL + "obo/mini-obi.owl: "), missing.getMessage());
        assertFalse(Files.exists(root.resolve("MSIO").resolve("1.0.1")));
        server.route("/obo/mini-obi.owl", x -> send(x, 500, new byte[0]));
        ResolutionException failed = assertThrows(ResolutionException.class, () -> cache.get(msio(), "1.0.1"));
        assertEquals(Reason.DOWNLOAD_FAILED, failed.reason());
        assertTrue(failed.getMessage().contains(PURL + "obo/mini-obi.owl"), failed.getMessage());
        assertFalse(Files.exists(root.resolve("MSIO").resolve("1.0.1")));
        assertNoTemporaryFolders();
    }

    // ---- what each kind of index and latest needs ----

    @Test
    public void aMissingReleaseIsNoSuchRelease() throws IOException {
        ResolutionException e = assertThrows(ResolutionException.class,
            () -> cache(false).get(ms(Map.of()), "9.9.9"));

        assertEquals(Reason.NO_SUCH_RELEASE, e.reason());
        assertTrue(e.getMessage().contains("/ms/v9.9.9/psi-ms.obo"), e.getMessage());
        assertFalse(e.getMessage().endsWith("."), e.getMessage());
        assertFalse(Files.exists(root.resolve("MS").resolve("9.9.9")));
        assertNoTemporaryFolders();
    }

    @Test
    public void aServerErrorIsDownloadFailed() {
        server.route(MS_PATH, x -> send(x, 503, "busy".getBytes(StandardCharsets.UTF_8)));

        ResolutionException e = assertThrows(ResolutionException.class,
            () -> cache(false).get(ms(Map.of()), "4.1.200"));

        assertEquals(Reason.DOWNLOAD_FAILED, e.reason());
        assertTrue(e.getMessage().contains("503"), e.getMessage());
    }

    @Test
    public void aVersionThatIsNotAPathSegmentIsRefused() {
        VocabularyCache cache = cache(false);
        for (String version : List.of("../4.1.200", "4.1/200", "..", "")) {
            ResolutionException e = assertThrows(ResolutionException.class,
                () -> cache.get(ms(Map.of()), version));
            assertEquals(Reason.NO_SUCH_RELEASE, e.reason(), version);
        }
        assertEquals(0, server.totalHits());
        assertFalse(Files.exists(root));
    }

    @Test
    public void anHtmlPageIsAParseFailure() throws IOException {
        // OBI's /releases/ PURL answered 200 with a SourceForge page
        server.file(MS_PATH, "<!DOCTYPE html>\n<html><body>moved</body></html>".getBytes(StandardCharsets.UTF_8));

        ResolutionException e = assertThrows(ResolutionException.class,
            () -> cache(false).get(ms(Map.of()), "4.1.200"));

        assertEquals(Reason.PARSE_FAILED, e.reason());
        assertTrue(e.getMessage().contains("not an OBO file"), e.getMessage());
        assertFalse(Files.exists(root.resolve("MS").resolve("4.1.200")));
        assertNoTemporaryFolders();
    }

    @Test
    public void aDirectoryListingNamesTheReleasesAndAnyVersionDownloads() throws Exception {
        server.route("/chebi/archive/", x -> send(x, 200, ("<html><body>"
            + "<a href=\"../\">Parent</a> <a href=\"rel245/\">rel245/</a> <a href='rel256/'>rel256/</a>"
            + " <a href=\"/pub/databases/chebi/archive/rel250/\">rel250/</a> <a href=\"README\">README</a>"
            + " <a href=\"relx/\">relx/</a></body></html>").getBytes(StandardCharsets.UTF_8)));
        byte[] gz = gzip(miniMs);
        server.file("/chebi/200/chebi.obo.gz", gz);
        VocabularyCache cache = cache(false);

        assertEquals(List.of(new Release("245", Optional.empty()), new Release("250", Optional.empty()),
            new Release("256", Optional.empty())), cache.releases(chebi()));
        // NewestRelease takes the newest of the listing
        assertEquals(Optional.of("256"), cache.latest(chebi()));
        assertEquals(1, server.hits("/chebi/archive/"));
        // 200 is not listed, but its URL serves it: the gzipped file is kept as it came
        CachedRelease r = cache.get(chebi(), "200");
        assertArrayEquals(gz, Files.readAllBytes(r.original()));
        assertEquals("chebi.obo.gz", r.original().getFileName().toString());
        assertEquals(sha256(gz), r.sha256());
        assertTrue(TermIndexStore.open(r).term("MS:1000031").isPresent());
    }

    @Test
    public void githubTagsArePagedAndDatedByTheirGithubReleases() throws Exception {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            tags.add("v4.0." + i);
        }
        tags.add("v4.1.200");
        tags.add("nightly");
        serveMsTags(tags);
        server.json(GITHUB_RELEASES, page -> page == 1
            ? "[{\"tag_name\": \"v4.1.200\", \"published_at\": \"2025-01-15T23:30:00Z\"},"
            + " {\"tag_name\": \"v4.0.1\", \"draft\": true, \"published_at\": null}]"
            : "[]");

        List<Release> releases = cache(false).releases(ms(Map.of()));

        assertEquals(101, releases.size());
        assertEquals(new Release("4.0.0", Optional.empty()), releases.get(0));
        assertEquals(new Release("4.0.1", Optional.empty()), releases.get(1));
        assertEquals(new Release("4.0.10", Optional.empty()), releases.get(10));
        assertEquals(new Release("4.1.200", Optional.of(LocalDate.of(2025, 1, 15))), releases.get(100));
        assertEquals(2, server.hits(TAGS));
    }

    @Test
    public void isoDateVersionsAreTheirOwnDates() throws Exception {
        server.json("/api/repos/obo/uo/tags", page -> page == 1
            ? "[{\"name\": \"v2026-10-12\"}, {\"name\": \"v2023-05-23\"}, {\"name\": \"vv2016-06-10\"}]" : "[]");
        VocabularyEntry uo = uo(new ReleaseIndex.GithubTags("obo/uo", "v{version}", ReleaseDates.VERSION),
            new Latest.GithubLatestTag("obo/uo", "v{version}"));
        VocabularyCache cache = cache(false);

        assertEquals(List.of(new Release("2023-05-23", Optional.of(LocalDate.of(2023, 5, 23))),
            new Release("2026-10-12", Optional.of(LocalDate.of(2026, 10, 12)))), cache.releases(uo));
        assertEquals(Optional.of("2026-10-12"), cache.latest(uo));
        // the listed kind gives its own releases, and is cached like the others
        List<Release> listed = List.of(new Release("2020-01-01", Optional.empty()));
        assertEquals(listed, cache.releases(uo(new ReleaseIndex.Listed(listed), new Latest.NewestRelease())));
    }

    @Test
    public void aHeaderLatestIsTheVersionInTheFileHeader() throws Exception {
        server.file("/uo.obo", ("format-version: 1.2\ndata-version: releases/2026-10-12\nontology: uo\n\n"
            + "[Term]\nid: UO:0000000\nname: unit\n").getBytes(StandardCharsets.UTF_8));
        server.file("/uo.owl", owlWithVersion(PURL + "obo/uo/releases/2026-09-01/uo.owl"));
        server.file("/plain.obo", "format-version: 1.2\ndata-version: 2026-08-01\n".getBytes(StandardCharsets.UTF_8));
        server.file("/none.obo", "format-version: 1.2\n\n[Term]\nid: UO:1\n".getBytes(StandardCharsets.UTF_8));
        VocabularyCache cache = cache(false);
        ReleaseIndex none = new ReleaseIndex.Listed(List.of());

        assertEquals(Optional.of("2026-10-12"), cache.latest(uo(none, header("/uo.obo"))));
        assertEquals(Optional.of("2026-09-01"),
            cache.latest(uo(Format.OWL_RDFXML, "UO2", none, header("/uo.owl"))));
        assertEquals(Optional.of("2026-08-01"), cache.latest(uo(Format.OBO, "UO3", none, header("/plain.obo"))));
        assertEquals(Optional.empty(), cache.latest(uo(Format.OBO, "UO4", none, header("/none.obo"))));
    }

    @Test
    public void theGithubTokenIsSentOnlyToTheApi() throws Exception {
        serveMsTags(List.of("v4.1.200"));
        server.file(MS_PATH, miniMs);
        VocabularyCache cache = new VocabularyCache(root, Downloader.http(Duration.ofSeconds(10)), false, DAY,
            clock, server.api(), Optional.of("s3cret"));

        cache.latest(ms(Map.of()));
        cache.get(ms(Map.of()), "4.1.200");

        assertEquals(List.of("Bearer s3cret"), server.authorizations(TAGS));
        assertEquals(List.of(), server.authorizations(MS_PATH));
        assertEquals(List.of("jmzTab-m"), server.userAgents(MS_PATH));
    }

    @Test
    public void theDownloadIsLoggedWithItsSize() throws Exception {
        server.file(MS_PATH, miniMs);
        Logger logger = (Logger) LoggerFactory.getLogger(VocabularyCache.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            cache(false).get(ms(Map.of()), "4.1.200");
        } finally {
            logger.detachAppender(appender);
        }
        String expected = String.format(java.util.Locale.ROOT, "Downloading MS 4.1.200 (%.1f kB) from %s",
            miniMs.length / 1000.0, server.base() + MS_PATH);
        assertTrue(appender.list.stream().anyMatch(e -> e.getLevel() == ch.qos.logback.classic.Level.INFO
            && e.getFormattedMessage().equals(expected)), appender.list.toString());
    }

    @Test
    public void aFolderWithoutReleaseJsonIsReplaced() throws Exception {
        server.file(MS_PATH, miniMs);
        Path stale = Files.createDirectories(root.resolve("MS").resolve("4.1.200"));
        Files.writeString(stale.resolve("psi-ms.obo"), "half");

        CachedRelease r = cache(false).get(ms(Map.of()), "4.1.200");

        assertArrayEquals(miniMs, Files.readAllBytes(r.original()));
        assertTrue(Files.isRegularFile(stale.resolve("release.json")));
        assertNoTemporaryFolders();
    }

    @Test
    public void theLoserOfARenameRaceUsesTheWinnersEntry() throws Exception {
        server.file(MS_PATH, miniMs);
        Path elsewhere = temp.resolve("winner");
        CachedRelease built = new VocabularyCache(elsewhere, Downloader.http(Duration.ofSeconds(10)), false, DAY,
            clock, server.api(), Optional.empty()).get(ms(Map.of()), "4.1.200");
        Path winner = built.original().getParent();
        Files.writeString(winner.resolve("marker"), "won");
        Downloader http = Downloader.http(Duration.ofSeconds(10));
        // another process finishes the same release while this one downloads
        Downloader racing = uri -> {
            copyTree(winner, root.resolve("MS").resolve("4.1.200"));
            return http.get(uri);
        };

        CachedRelease r = new VocabularyCache(root, racing, false, DAY, clock, server.api(), Optional.empty())
            .get(ms(Map.of()), "4.1.200");

        assertTrue(Files.exists(r.original().getParent().resolve("marker")));
        assertEquals(root.resolve("MS").resolve("4.1.200").resolve("psi-ms.obo"), r.original());
        assertNoTemporaryFolders();
    }

    @Test
    public void aCacheFolderCanBeCopiedElsewhere() throws Exception {
        server.file(MS_PATH, miniMs);
        cache(false).get(ms(Map.of()), "4.1.200");
        Path copy = temp.resolve("copy");
        copyTree(root, copy);

        CachedRelease r = new VocabularyCache(copy, failingDownloader(), true, DAY, clock, server.api(),
            Optional.empty()).get(ms(Map.of()), "4.1.200");

        assertEquals(copy.resolve("MS").resolve("4.1.200").resolve("psi-ms.obo"), r.original());
        assertTrue(TermIndexStore.open(r).term("MS:1000031").isPresent());
    }

    @Test
    public void theDefaultRootIsTheUserCacheFolder() {
        Path root = VocabularyCache.defaultRoot();
        String xdg = System.getenv("XDG_CACHE_HOME");
        Path base = xdg != null && !xdg.isBlank() && Path.of(xdg).isAbsolute()
            ? Path.of(xdg) : Path.of(System.getProperty("user.home"), ".cache");
        assertEquals(base.resolve("jmztabm").resolve("cv"), root);
    }

    // ---- fixtures ----

    private VocabularyCache cache(boolean offline) {
        return new VocabularyCache(root, Downloader.http(Duration.ofSeconds(10)), offline, DAY, clock, server.api(),
            Optional.empty());
    }

    private VocabularyEntry ms(Map<String, String> knownSha256) {
        return new VocabularyEntry("MS", Set.of("MS", "PSI-MS"), "MS",
            new Location(Format.OBO, Optional.of(server.base() + "/ms/v{version}/psi-ms.obo"), Optional.empty()),
            VersionScheme.DOTTED, new ReleaseIndex.GithubTags(MS_REPO, "v{version}", ReleaseDates.GITHUB_RELEASES),
            new Latest.GithubLatestTag(MS_REPO, "v{version}"), knownSha256);
    }

    private VocabularyEntry msio() {
        return new VocabularyEntry("MSIO", Set.of("MSIO"), "MSIO",
            new Location(Format.OWL_RDFXML, Optional.of(server.base() + "/msio/v{version}/msio.owl"),
                Optional.empty()),
            VersionScheme.DOTTED, new ReleaseIndex.Listed(List.of()), new Latest.NewestRelease(), Map.of());
    }

    private VocabularyEntry chebi() {
        return new VocabularyEntry("CHEBI", Set.of("CHEBI"), "CHEBI",
            new Location(Format.OBO_GZ, Optional.of(server.base() + "/chebi/{version}/chebi.obo.gz"),
                Optional.empty()),
            VersionScheme.RELEASE_NUMBER,
            new ReleaseIndex.DirectoryListing(URI.create(server.base() + "/chebi/archive/"), "rel{version}/",
                ReleaseDates.NONE),
            new Latest.NewestRelease(), Map.of());
    }

    private VocabularyEntry uo(ReleaseIndex releases, Latest latest) {
        return uo(Format.OBO, "UO", releases, latest);
    }

    private VocabularyEntry uo(Format format, String key, ReleaseIndex releases, Latest latest) {
        return new VocabularyEntry(key, Set.of(key), key,
            new Location(format, Optional.of(server.base() + "/uo/{version}/uo." + format.extension()),
                Optional.empty()),
            VersionScheme.ISO_DATE, releases, latest, Map.of());
    }

    private Latest header(String path) {
        return new Latest.Header(URI.create(server.base() + path));
    }

    private void serveMsTags(List<String> tags) {
        server.json(TAGS, page -> {
            int from = Math.min((page - 1) * 100, tags.size());
            int to = Math.min(page * 100, tags.size());
            return tags.subList(from, to).stream().map(t -> "{\"name\": \"" + t + "\"}")
                .reduce((a, b) -> a + "," + b).map(s -> "[" + s + "]").orElse("[]");
        });
    }

    /** A downloader that fetches the purl.obolibrary.org IRIs from the test server. */
    private Downloader purlToServer() {
        Downloader http = Downloader.http(Duration.ofSeconds(10));
        return uri -> http.get(URI.create(uri.toString().replace(PURL, server.base() + "/")));
    }

    private static Downloader failingDownloader() {
        return uri -> {
            throw new AssertionError("no download expected: " + uri);
        };
    }

    private void assertImportFolder(CachedRelease imported, String iri, byte[] content) throws Exception {
        String hash = sha256(iri.getBytes(StandardCharsets.UTF_8)).substring(0, 16);
        Path folder = root.resolve("_imports").resolve(hash);
        assertEquals(folder, imported.original().getParent());
        assertArrayEquals(content, Files.readAllBytes(imported.original()));
        assertTrue(Files.isRegularFile(folder.resolve("release.json")));
    }

    private void assertNoTemporaryFolders() throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            List<Path> temporary = files.filter(p -> p.getFileName().toString().startsWith(".tmp-")).toList();
            assertEquals(List.of(), temporary);
        }
    }

    private static byte[] owl(String ontology, String imports, String classIri, String label) {
        return ("<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
            + " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
            + " xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
            + "<owl:Ontology rdf:about=\"" + ontology + "\"><owl:imports rdf:resource=\"" + imports + "\"/>"
            + "</owl:Ontology>\n<owl:Class rdf:about=\"" + classIri + "\"><rdfs:label>" + label
            + "</rdfs:label></owl:Class>\n</rdf:RDF>\n").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] owlWithVersion(String versionIri) {
        return ("<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
            + " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
            + "<owl:Ontology rdf:about=\"http://purl.obolibrary.org/obo/uo.owl\"><owl:versionIRI rdf:resource=\""
            + versionIri + "\"/></owl:Ontology>\n</rdf:RDF>\n").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] resource(String name) throws IOException {
        try (InputStream in = VocabularyCacheTest.class.getResourceAsStream(name)) {
            return in.readAllBytes();
        }
    }

    private static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(data);
        }
        return bytes.toByteArray();
    }

    private static String sha256(byte[] data) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
    }

    private static void copyTree(Path from, Path to) {
        try (Stream<Path> files = Files.walk(from)) {
            for (Path p : files.toList()) {
                Path target = to.resolve(from.relativize(p).toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(p, target);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void send(HttpExchange x, int status, byte[] body) throws IOException {
        x.sendResponseHeaders(status, body.length);
        try (OutputStream out = x.getResponseBody()) {
            out.write(body);
        }
    }

    private static final class MutableClock extends Clock {

        private volatile Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    /** A local HTTP server: routes by path, counts the requests and keeps their headers. */
    private static final class Server implements AutoCloseable {

        interface Route {
            void handle(HttpExchange exchange) throws IOException;
        }

        interface Page {
            String json(int page);
        }

        private final HttpServer http;
        private final ExecutorService executor = Executors.newCachedThreadPool();
        private final Map<String, Route> routes = new ConcurrentHashMap<>();
        private final Map<String, List<HttpExchange>> requests = new ConcurrentHashMap<>();
        private final Map<String, List<String>> authorizations = new ConcurrentHashMap<>();
        private final Map<String, List<String>> userAgents = new ConcurrentHashMap<>();
        private final AtomicInteger total = new AtomicInteger();

        Server() throws IOException {
            http = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
            http.createContext("/", x -> {
                String path = x.getRequestURI().getPath();
                total.incrementAndGet();
                requests.computeIfAbsent(path, p -> new CopyOnWriteArrayList<>()).add(x);
                authorizations.computeIfAbsent(path, p -> new CopyOnWriteArrayList<>());
                String auth = x.getRequestHeaders().getFirst("Authorization");
                if (auth != null) {
                    authorizations.get(path).add(auth);
                }
                userAgents.computeIfAbsent(path, p -> new CopyOnWriteArrayList<>())
                    .add(String.valueOf(x.getRequestHeaders().getFirst("User-Agent")));
                Route route = routes.get(path);
                try {
                    if (route == null) {
                        send(x, 404, "not found".getBytes(StandardCharsets.UTF_8));
                    } else {
                        route.handle(x);
                    }
                } finally {
                    x.close();
                }
            });
            http.setExecutor(executor);
            http.start();
        }

        String base() {
            return "http://127.0.0.1:" + http.getAddress().getPort();
        }

        URI api() {
            return URI.create(base() + "/api");
        }

        void route(String path, Route route) {
            routes.put(path, route);
        }

        void file(String path, byte[] body) {
            route(path, x -> send(x, 200, body));
        }

        void json(String path, Page pages) {
            route(path, x -> {
                String query = String.valueOf(x.getRequestURI().getQuery());
                int page = 1;
                for (String part : query.split("&")) {
                    if (part.startsWith("page=")) {
                        page = Integer.parseInt(part.substring(5));
                    }
                }
                x.getResponseHeaders().add("Content-Type", "application/json");
                send(x, 200, pages.json(page).getBytes(StandardCharsets.UTF_8));
            });
        }

        int hits(String path) {
            return requests.getOrDefault(path, List.of()).size();
        }

        int totalHits() {
            return total.get();
        }

        List<String> authorizations(String path) {
            return authorizations.getOrDefault(path, List.of());
        }

        List<String> userAgents(String path) {
            return userAgents.getOrDefault(path, List.of());
        }

        @Override
        public void close() {
            http.stop(0);
            executor.shutdownNow();
        }
    }
}
