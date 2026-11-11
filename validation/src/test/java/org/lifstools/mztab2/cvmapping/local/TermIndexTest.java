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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TermIndexTest {

    @TempDir
    Path dir;

    private TermIndex miniMsIndex() throws IOException {
        Path file = dir.resolve("terms.idx");
        TermIndexWriter.write(OboReaderTest.readMiniMs().values(), file);
        return TermIndex.open(file);
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
            assertEquals(terms.size(), index.size());
            for (TermRecord t : terms.values()) {
                // a parent that is not in the vocabulary is not stored
                Map<String, List<String>> kept = new LinkedHashMap<>();
                t.parentsByRelation().forEach((relation, parents) -> {
                    List<String> known = parents.stream().filter(terms::containsKey).toList();
                    if (!known.isEmpty()) {
                        kept.put(relation, known);
                    }
                });
                TermRecord expected = term(t.id(), t.label(), t.obsolete(), kept);
                assertEquals(Optional.of(expected), index.term(t.id()), t.id());
            }
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
    public void cyclesAndDanglingParentsEndTheWalk() throws IOException {
        try (TermIndex index = miniMsIndex()) {
            assertEquals(List.of("MS:9999902", "MS:0000000"), ids(index.ancestors("MS:9999901")));
            assertEquals(List.of("MS:9999901", "MS:0000000"), ids(index.ancestors("MS:9999902")));
            // MS:1000040 is_a UO:0000000, which is not in the vocabulary
            assertEquals(List.of(), index.ancestors("MS:1000040"));
            assertEquals(Map.of(), index.term("MS:1000040").get().parentsByRelation());
        }
    }

    @Test
    public void anUnknownIdIsEmpty() throws IOException {
        try (TermIndex index = miniMsIndex()) {
            for (String id : List.of("MS:0000001", "MS:100007", "MS:10000730", "A:1", "ZZ:9",
                "", "UO:0000000", "ms:1000073")) {
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
