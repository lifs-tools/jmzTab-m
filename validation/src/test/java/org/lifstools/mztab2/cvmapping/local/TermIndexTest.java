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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TermIndexTest {

    @TempDir
    Path dir;

    private TermIndex miniMsIndex() throws IOException {
        Path file = dir.resolve("terms.idx");
        writeMiniMs(file);
        return TermIndex.open(file);
    }

    /** Writes the index of mini-ms.obo as the cache does: its terms, and the names its parent lines give. */
    static void writeMiniMs(Path file) throws IOException {
        try (TermIndexWriter writer = TermIndexWriter.open(file);
            InputStream in = TermIndexTest.class.getResourceAsStream("/cv/mini-ms.obo")) {
            new OboReader().read(in, writer, v -> { }, writer::parentName);
            writer.commit();
        }
    }

    /** Writes the index of mini-msio.owl and its import mini-obi.owl into one file, as the cache does (R-E). */
    static void writeMiniMsio(Path file) throws IOException {
        try (TermIndexWriter writer = TermIndexWriter.open(file)) {
            for (String owl : List.of("/cv/mini-msio.owl", "/cv/mini-obi.owl")) {
                try (InputStream in = TermIndexTest.class.getResourceAsStream(owl)) {
                    new OwlRdfXmlReader().read(in, writer, i -> { }, v -> { });
                }
            }
            writer.commit();
        }
    }

    private static List<String> ids(List<TermRecord> terms) {
        return terms.stream().map(TermRecord::id).toList();
    }

    private static TermRecord term(String id, String label, boolean obsolete,
        Map<String, List<String>> parents) {
        return new TermRecord(id, label, obsolete, parents);
    }

    @Test
    public void roundTripsEveryRecord() throws IOException {
        Map<String, TermRecord> terms = OboReaderTest.readMiniMs();
        try (TermIndex index = miniMsIndex()) {
            // UO:0000000, a parent without a stanza, is a stub term of its own (Ruling R-AA), named as
            // mini-ms.obo's "is_a: UO:0000000 ! unit" names it (minor 8 of the Task 11 review)
            assertEquals(terms.size() + 1, index.size());
            for (TermRecord t : terms.values()) {
                assertEquals(Optional.of(t), index.term(t.id()), t.id());
            }
            assertEquals(Optional.of(term("UO:0000000", "unit", false, Map.of())), index.term("UO:0000000"));
        }
    }

    @Test
    public void ancestorsFollowIsAAndPartOfButNotHasUnits() throws IOException {
        try (TermIndex index = miniMsIndex()) {
            assertEquals(List.of("MS:1000008", "MS:1000458", "MS:1000463", "MS:0000000"),
                ids(index.ancestors("MS:1000073")));
            assertEquals(List.of("MS:1000494", "MS:1000483", "MS:1000031", "MS:1000463",
                "MS:0000000"), ids(index.ancestors("MS:1001742")));
            assertEquals(List.of("MS:1000455", "MS:0000000"),
                ids(index.ancestors("MS:1000744")));
            assertEquals(Optional.of(term("MS:1000008", "ionization type", false,
                Map.of("part_of", List.of("MS:1000458")))),
                index.ancestors("MS:1000073").stream().findFirst());
            assertEquals(List.of(), index.ancestors("MS:0000000"));
        }
    }

    @Test
    public void cyclesEndTheWalkAndAParentWithoutAStanzaIsAnAncestor() throws IOException {
        try (TermIndex index = miniMsIndex()) {
            assertEquals(List.of("MS:9999902", "MS:0000000"), ids(index.ancestors("MS:9999901")));
            assertEquals(List.of("MS:9999901", "MS:0000000"), ids(index.ancestors("MS:9999902")));
            // MS:1000040 is_a UO:0000000, which has no stanza in the vocabulary (Ruling R-AA)
            assertEquals(List.of("UO:0000000"), ids(index.ancestors("MS:1000040")));
            assertEquals(Map.of("is_a", List.of("UO:0000000")), index.term("MS:1000040").get().parentsByRelation());
        }
    }

    @Test
    public void parentsWithoutAStanzaAreStubTermsThatPassTheOpenChecks() throws IOException {
        Path file = dir.resolve("terms.idx");
        // the stubs sort first and last, so the record-end check reads a stub
        TermIndexWriter.write(List.of(
            term("M:1", "one", false, Map.of("is_a", List.of("ZZ:9", "A:0"), "part_of", List.of("M:2"))),
            term("M:2", "two", false, Map.of("part_of", List.of("ZZ:9")))), file);
        try (TermIndex index = TermIndex.open(file)) {
            assertEquals(4, index.size());
            assertEquals(Optional.of(term("ZZ:9", "ZZ:9", false, Map.of())), index.term("ZZ:9"));
            assertEquals(Optional.of(term("A:0", "A:0", false, Map.of())), index.term("A:0"));
            assertEquals(Set.of("ZZ:9", "A:0", "M:2"), Set.copyOf(ids(index.ancestors("M:1"))));
            assertEquals(List.of(), index.ancestors("ZZ:9"));
        }
        // the first non-empty name a parent line gives a stub is its label; a term keeps its own name
        Path named = dir.resolve("named.idx");
        try (TermIndexWriter writer = TermIndexWriter.open(named)) {
            writer.parentName("ZZ:9", "");
            writer.accept(term("M:1", "one", false, Map.of("is_a", List.of("ZZ:9", "A:0"),
                "part_of", List.of("M:2"))));
            writer.parentName("ZZ:9", "nine");
            writer.parentName("M:2", "not two");
            writer.accept(term("M:2", "two", false, Map.of("part_of", List.of("ZZ:9"))));
            writer.parentName("ZZ:9", "other");
            writer.parentName("Q:1", "no one's parent");
            writer.commit();
        }
        try (TermIndex index = TermIndex.open(named)) {
            assertEquals(4, index.size());
            assertEquals(Optional.of(term("ZZ:9", "nine", false, Map.of())), index.term("ZZ:9"));
            assertEquals(Optional.of(term("A:0", "A:0", false, Map.of())), index.term("A:0"));
            assertEquals("two", index.term("M:2").orElseThrow().label());
            assertEquals(Optional.empty(), index.term("Q:1"));
        }
        try (Stream<Path> left = Files.list(dir)) {
            assertEquals(Set.of("terms.idx", "named.idx"),
                left.map(f -> f.getFileName().toString()).collect(Collectors.toSet()), "no temporary file is left");
        }
        Path truncated = dir.resolve("truncated.idx");
        byte[] whole = Files.readAllBytes(file);
        Files.write(truncated, Arrays.copyOf(whole, whole.length - 1));
        IOException e = assertThrows(IOException.class, () -> TermIndex.open(truncated));
        assertTrue(e.getMessage().contains("not a term index: truncated records"), e.getMessage());
    }

    /**
     * The reader rules of each index version (Ruling R-AB): a version stands
     * for one set of rules, so changing a rule means a new version here and
     * in {@link TermIndex#VERSION}.
     */
    private static final Map<Integer, String> READER_FINGERPRINTS = Map.of(2, "9b132577b8052951",
        3, "9b132577b8052951");

    /**
     * The fingerprint of the index files of each index version built from
     * fixed fixtures, an OBO file with a parent without a stanza and an OWL
     * file with an import (minor 4 of the Task 11 review): whatever changes
     * what the readers or the writer put into an index changes it, and needs
     * a new {@link TermIndex#VERSION}. Version 2 had none.
     */
    private static final Map<Integer, String> INDEX_FINGERPRINTS = Map.of(3, "198f8d2686123bab");

    /** The SHA-256 of the fixture indexes, mini-ms.obo's then mini-msio.owl's with mini-obi.owl. */
    private String fixtureIndexes(String name) throws IOException {
        Path obo = dir.resolve(name + "-obo.idx");
        Path owl = dir.resolve(name + "-owl.idx");
        writeMiniMs(obo);
        writeMiniMsio(owl);
        return sha256(Files.readAllBytes(obo)) + " " + sha256(Files.readAllBytes(owl));
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void theIndexBytesAreTheSameEachTime() throws IOException {
        assertEquals(fixtureIndexes("first"), fixtureIndexes("second"));
    }

    @Test
    public void theIndexVersionCoversWhatTheReadersAndTheWriterWrite() throws IOException {
        String shas = fixtureIndexes("pinned");
        String fingerprint = fingerprint(shas);
        assertEquals(INDEX_FINGERPRINTS.get(TermIndex.VERSION), fingerprint, "the index of the fixtures changed ("
            + shas + "): bump TermIndex.VERSION and add it to INDEX_FINGERPRINTS with " + fingerprint
            + "; if the index was meant to stay the same, find what changed it");
        assertEquals(INDEX_FINGERPRINTS.size(), Set.copyOf(INDEX_FINGERPRINTS.values()).size(),
            "two index versions with the same fixture index");
    }

    /** The reader rules that decide an index's ancestors, as one canonical text. */
    static String readerRules() {
        return "obo.hierarchical=" + new TreeSet<>(OboReader.HIERARCHICAL)
            + ";owl.hierarchicalProperties=" + new TreeSet<>(OwlRdfXmlReader.HIERARCHICAL_PROPERTIES)
            + ";owl.notParents=" + new TreeSet<>(OwlRdfXmlReader.NOT_PARENTS);
    }

    /** The first 16 hex digits of the SHA-256 of the text. */
    static String fingerprint(String rules) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(rules.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void theIndexVersionCoversTheReaderRules() {
        String fingerprint = fingerprint(readerRules());
        assertEquals(TermIndex.READER_FINGERPRINT, fingerprint, "the reader rules changed (" + readerRules()
            + "): bump TermIndex.VERSION, set READER_FINGERPRINT to " + fingerprint
            + " and add the new version to READER_FINGERPRINTS");
        assertEquals(READER_FINGERPRINTS.get(TermIndex.VERSION), TermIndex.READER_FINGERPRINT,
            "READER_FINGERPRINT changed without a new TermIndex.VERSION");
    }

    @Test
    public void anIndexRecordsItsVersionAndAnOlderOneIsRefused() throws IOException {
        Path file = dir.resolve("terms.idx");
        TermIndexWriter.write(OboReaderTest.readMiniMs().values(), file);
        assertEquals(OptionalInt.of(TermIndex.VERSION), TermIndex.version(file));
        assertTrue(TermIndex.isCurrent(file));

        // the layout before index versions: magic JMZTIDX1, then the counts
        Path old = dir.resolve("old.idx");
        Files.write(old, ByteBuffer.allocate(16).put("JMZTIDX1".getBytes(StandardCharsets.US_ASCII))
            .putInt(0).putInt(0).array());
        assertEquals(OptionalInt.of(1), TermIndex.version(old));
        assertFalse(TermIndex.isCurrent(old));
        IOException e = assertThrows(IOException.class, () -> TermIndex.open(old));
        assertTrue(e.getMessage().contains("index version 1"), e.getMessage());

        Path text = dir.resolve("text.idx");
        Files.writeString(text, "format-version: 1.2\n", StandardCharsets.UTF_8);
        assertEquals(OptionalInt.empty(), TermIndex.version(text));
        assertEquals(OptionalInt.empty(), TermIndex.version(dir.resolve("missing.idx")));
        assertFalse(TermIndex.isCurrent(dir.resolve("missing.idx")));
    }

    @Test
    public void anUnknownIdIsEmpty() throws IOException {
        try (TermIndex index = miniMsIndex()) {
            for (String id : List.of("MS:0000001", "MS:100007", "MS:10000730", "A:1", "ZZ:9",
                "", "UO:0000001", "ms:1000073")) {
                assertEquals(Optional.empty(), index.term(id), id);
                assertEquals(List.of(), index.ancestors(id), id);
            }
        }
    }

    @Test
    public void duplicateIdsAreMerged() throws IOException {
        Path file = dir.resolve("terms.idx");
        TermIndexWriter.write(List.of(
            term("X:1", "", false, Map.of("is_a", List.of("X:2"))),
            term("X:2", "two", false, Map.of()),
            term("X:1", "one", false, Map.of("is_a", List.of("X:3", "X:2"),
                "part_of", List.of("X:2"))),
            term("X:3", "three", false, Map.of()),
            term("X:1", "other", true, Map.of("is_a", List.of("X:2")))), file);
        try (TermIndex index = TermIndex.open(file)) {
            assertEquals(3, index.size());
            assertEquals(Optional.of(term("X:1", "one", true, Map.of(
                "is_a", List.of("X:2", "X:3"), "part_of", List.of("X:2")))), index.term("X:1"));
            assertEquals(List.of("X:2", "X:3"), ids(index.ancestors("X:1")));
        }
    }

    @Test
    public void anUncommittedWriterLeavesNothingBehind() throws IOException {
        Path file = dir.resolve("terms.idx");
        try (TermIndexWriter writer = TermIndexWriter.open(file)) {
            writer.accept(term("X:1", "one", false, Map.of()));
        }
        assertFalse(Files.exists(file));
        try (Stream<Path> left = Files.list(dir)) {
            assertEquals(List.of(), left.toList());
        }
    }

    @Test
    public void aClosedIndexAnswersNoMore() throws IOException {
        TermIndex index = miniMsIndex();
        index.close();
        assertThrows(IllegalStateException.class, () -> index.term("MS:1000073"));
        assertThrows(IllegalStateException.class, () -> index.ancestors("MS:1000073"));
        assertThrows(IllegalStateException.class, index::size);
    }

    @Test
    public void anEmptyIndexHasNoTerms() throws IOException {
        Path file = dir.resolve("terms.idx");
        TermIndexWriter.write(List.of(), file);
        try (TermIndex index = TermIndex.open(file)) {
            assertEquals(0, index.size());
            assertEquals(Optional.empty(), index.term("MS:1000073"));
        }
    }

    @Test
    public void aCommitReplacesAnExistingIndex() throws IOException {
        Path file = dir.resolve("terms.idx");
        TermIndexWriter.write(List.of(term("X:1", "one", false, Map.of())), file);
        TermIndexWriter.write(List.of(term("Y:1", "why", false, Map.of())), file);
        try (TermIndex index = TermIndex.open(file)) {
            assertEquals(Optional.empty(), index.term("X:1"));
            assertEquals("why", index.term("Y:1").get().label());
        }
        try (Stream<Path> left = Files.list(dir)) {
            assertEquals(List.of(file), left.toList());
        }
    }

    @Test
    public void anIndexGetsTheFolderDefaultPermissions() throws IOException {
        assumeTrue(dir.getFileSystem().supportedFileAttributeViews().contains("posix"));
        Path file = dir.resolve("terms.idx");
        TermIndexWriter.write(List.of(term("X:1", "one", false, Map.of())), file);
        Path plain = Files.createFile(dir.resolve("plain"));
        assertEquals(Files.getPosixFilePermissions(plain), Files.getPosixFilePermissions(file));
    }

    @Test
    public void aTruncatedIndexIsRejected() throws IOException {
        Path file = dir.resolve("terms.idx");
        TermIndexWriter.write(OboReaderTest.readMiniMs().values(), file);
        byte[] whole = Files.readAllBytes(file);
        for (int cut : List.of(1, 3, 40)) {
            Path truncated = dir.resolve("truncated-" + cut + ".idx");
            Files.write(truncated, Arrays.copyOf(whole, whole.length - cut));
            IOException e = assertThrows(IOException.class, () -> TermIndex.open(truncated));
            assertTrue(e.getMessage().contains("not a term index: truncated records"), e.getMessage());
        }
        Path extended = dir.resolve("extended.idx");
        Files.write(extended, Arrays.copyOf(whole, whole.length + 4));
        assertThrows(IOException.class, () -> TermIndex.open(extended));
    }

    @Test
    public void aHeaderClaimingTooManyRelationsIsRejected() throws IOException {
        Path file = dir.resolve("terms.idx");
        ByteBuffer header = ByteBuffer.allocate(20).put("JMZTIDX2".getBytes(StandardCharsets.US_ASCII))
            .putInt(TermIndex.VERSION).putInt(0).putInt(Integer.MAX_VALUE);
        Files.write(file, header.array());
        IOException e = assertThrows(IOException.class, () -> TermIndex.open(file));
        assertTrue(e.getMessage().contains("truncated header"), e.getMessage());
    }

    @Test
    public void aParentOrdinalOutsideTheTableIsACorruptIndex() throws IOException {
        Path file = dir.resolve("terms.idx");
        TermIndexWriter.write(List.of(term("X:1", "one", false, Map.of("is_a", List.of("X:2"))),
            term("X:2", "two", false, Map.of())), file);
        byte[] bytes = Files.readAllBytes(file);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        // header: magic, version, counts, one relation name "is_a"; then two offsets
        long firstRecord = buffer.getLong(8 + 4 + 4 + 4 + 4 + 4);
        // X:1's record: id, label, obsolete byte, is_a count, then the parent ordinal
        int ordinalAt = (int) firstRecord + 4 + 3 + 4 + 3 + 1 + 2;
        assertEquals(1, buffer.getInt(ordinalAt));
        buffer.putInt(ordinalAt, 99);
        Files.write(file, bytes);
        try (TermIndex index = TermIndex.open(file)) {
            IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> index.ancestors("X:1"));
            assertTrue(e.getMessage().contains("is corrupt"), e.getMessage());
            assertThrows(IllegalStateException.class, () -> index.term("X:1"));
            assertEquals("two", index.term("X:2").get().label());
        }
    }

    @Test
    public void aFileThatIsNotATermIndexIsRejected() throws IOException {
        Path file = dir.resolve("terms.idx");
        Files.writeString(file, "format-version: 1.2\n", StandardCharsets.UTF_8);
        IOException e = assertThrows(IOException.class, () -> TermIndex.open(file));
        assertTrue(e.getMessage().contains("not a term index"), e.getMessage());
    }

    /**
     * A generated vocabulary of 3,000,000 terms: a root with a fan of chains of
     * 30 terms each (is_a); every odd chain is also part_of its even neighbour,
     * one level up. Ids are unpadded, so the sort order is not the generation
     * order.
     */
    @Test
    public void aLargeIndexOpensFastAndAnswersFast() throws IOException {
        final int count = 3_000_000;
        final int depth = 30;
        Path file = dir.resolve("terms.idx");
        long writeStart = System.nanoTime();
        try (TermIndexWriter writer = TermIndexWriter.open(file)) {
            for (int i = 0; i < count; i++) {
                Map<String, List<String>> parents = new LinkedHashMap<>();
                if (i > 0) {
                    int chain = (i - 1) / depth;
                    int position = (i - 1) % depth;
                    parents.put("is_a", List.of("T:" + (position == 0 ? 0 : i - 1)));
                    if (chain % 2 == 1 && position > 0) {
                        parents.put("part_of", List.of("T:" + (i - depth - 1)));
                    }
                }
                writer.accept(term("T:" + i, "term " + i, false, parents));
            }
            writer.commit();
        }
        long writeMillis = (System.nanoTime() - writeStart) / 1_000_000;

        long openStart = System.nanoTime();
        TermIndex index = TermIndex.open(file);
        long openMillis = (System.nanoTime() - openStart) / 1_000_000;
        try (index) {
            assertEquals(count, index.size());
            Random random = new Random(20261111L);
            int calls = 10_000;
            long answered = 0;
            long walkStart = System.nanoTime();
            for (int n = 0; n < calls; n++) {
                answered += index.ancestors("T:" + random.nextInt(count)).size();
            }
            double perCallMillis = (System.nanoTime() - walkStart) / 1e6 / calls;
            System.out.printf("terms.idx: %d terms, %d bytes; write %d ms; open %d ms; "
                + "ancestors %.4f ms per call (%d ancestors in all); max heap %d MB%n",
                count, Files.size(file), writeMillis, openMillis, perCallMillis, answered,
                Runtime.getRuntime().maxMemory() / (1024 * 1024));

            assertTrue(openMillis < 200, "open took " + openMillis + " ms");
            assertTrue(perCallMillis < 5, "ancestors took " + perCallMillis + " ms per call");
            // the deepest term of an odd chain: 29 in its own chain, 29 in its neighbour, the root
            int deepOdd = 1 + depth + depth - 1;
            Set<String> expected = Stream.concat(
                Stream.iterate(deepOdd - 1, i -> i > depth, i -> i - 1),
                Stream.concat(Stream.iterate(depth - 1, i -> i >= 1, i -> i - 1), Stream.of(0)))
                .map(i -> "T:" + i).collect(Collectors.toSet());
            List<String> ancestors = ids(index.ancestors("T:" + deepOdd));
            assertEquals(expected, Set.copyOf(ancestors));
            assertEquals(expected.size(), ancestors.size());
            assertEquals(List.of("T:" + (deepOdd - 1), "T:" + (deepOdd - depth - 1)),
                ancestors.subList(0, 2));
        }
    }
}
