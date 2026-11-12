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

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * The terms of one vocabulary release, read from an immutable, memory-mapped
 * index file ({@code terms-v<version>.idx} in the cache) written by
 * {@link TermIndexWriter}.
 *
 * <p>
 * The layout, big-endian:</p>
 * <pre>
 * magic "JMZTIDX2" (8 bytes) | int index version | int termCount | int relationCount
 * relation names (int length + UTF-8)
 * long[termCount] record offsets, sorted by obo id (UTF-8 bytes, unsigned)
 * records: id, label (int length + UTF-8), byte obsolete,
 *          per relation: unsigned short count + int[count] term ordinals
 * </pre>
 * <p>
 * The parents are ordinals into the sorted offset table, so a walk needs no
 * string search. The index walks every relation named in its header: the
 * reader that produced the terms decided which relations are hierarchical.</p>
 *
 * <p>
 * Safe for use by several threads. An index cannot be closed (Ruling R-AI):
 * the index of a cached release is owned by {@link TermIndexStore}, which
 * shares one per file with every validation run and thread of the process
 * and keeps it for the life of the process, so no user could end it without
 * ending it for all. Its mapping is released by the garbage collector once
 * the index is unreachable ({@code Arena.ofAuto()}): a native image supports
 * closing a mapping that several threads share only with an experimental
 * build option. An index opened with {@link #open} outside the store is
 * likewise released when it is no longer referenced.</p>
 */
public final class TermIndex {

    static final byte[] MAGIC = "JMZTIDX2".getBytes(StandardCharsets.US_ASCII);
    /** The magic of the layout before index versions, which counts as version 1. */
    private static final byte[] MAGIC_1 = "JMZTIDX1".getBytes(StandardCharsets.US_ASCII);

    /**
     * The index version (Ruling R-AB): the file layout and the rules that
     * decided its ancestors, those of the readers ({@link OboReader#HIERARCHICAL},
     * {@link OwlRdfXmlReader#HIERARCHICAL_PROPERTIES},
     * {@link OwlRdfXmlReader#NOT_PARENTS}) and of {@link TermIndexWriter} (a
     * parent without a stanza is a stub term). The cache rebuilds an index of
     * another version from its cached file. Bump it with any change of these
     * rules, together with {@link #READER_FINGERPRINT}; {@code TermIndexTest}
     * also pins, per version, the bytes of the indexes of fixed fixture files,
     * so that any change in what the readers or the writer write needs a new
     * version too. The cache keeps each version in a file of its own,
     * {@code terms-v<version>.idx}, so builds of different versions can share
     * one cache. Version 1: the layout {@code JMZTIDX1}; 2: the version in the
     * header, part_of as {@code BFO:0000050}, no {@code owl:Thing} parent,
     * stub parents; 3: a stub parent named by the OBO {@code ! name} comment
     * of its parent line.
     */
    static final int VERSION = 3;

    /**
     * The fingerprint of the reader rules of {@link #VERSION} (the first 16 hex
     * digits of the SHA-256 of their canonical text): {@code TermIndexTest}
     * compares it with the readers' sets and pins it per version, so a rule
     * change without a new version fails.
     */
    static final String READER_FINGERPRINT = "9b132577b8052951";

    /**
     * The index version an index file was written with.
     *
     * @param file an index file
     * @return the version (1 for the layout before versions); empty if the file
     * is missing, unreadable or no term index
     */
    static OptionalInt version(Path file) {
        byte[] head = new byte[MAGIC.length + 4];
        int read;
        try (InputStream in = Files.newInputStream(file)) {
            read = in.readNBytes(head, 0, head.length);
        } catch (IOException e) {
            return OptionalInt.empty();
        }
        return version(head, read);
    }

    /** The index version the first {@code read} bytes of a file name, if they are a term index header. */
    private static OptionalInt version(byte[] head, int read) {
        if (read >= MAGIC_1.length && Arrays.equals(Arrays.copyOf(head, MAGIC_1.length), MAGIC_1)) {
            return OptionalInt.of(1);
        }
        if (read >= MAGIC.length + 4 && Arrays.equals(Arrays.copyOf(head, MAGIC.length), MAGIC)) {
            return OptionalInt.of(ByteBuffer.wrap(head, MAGIC.length, 4).getInt());
        }
        return OptionalInt.empty();
    }

    /**
     * @param file an index file
     * @return true if it exists and was written with {@link #VERSION}
     */
    static boolean isCurrent(Path file) {
        return version(file).equals(OptionalInt.of(VERSION));
    }

    static final int MAX_PARENTS = 0xFFFF;
    static final ValueLayout.OfByte BYTE = ValueLayout.JAVA_BYTE;
    static final ValueLayout.OfShort SHORT = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
    static final ValueLayout.OfInt INT = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
    static final ValueLayout.OfLong LONG = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);

    private final Path file;
    private final MemorySegment segment;
    private final int termCount;
    private final List<String> relations;
    private final long tableStart;

    private TermIndex(Path file, MemorySegment segment, int termCount, List<String> relations,
        long tableStart) {
        this.file = file;
        this.segment = segment;
        this.termCount = termCount;
        this.relations = relations;
        this.tableStart = tableStart;
    }

    /**
     * Maps an index file. A validation run gets its indexes from
     * {@link TermIndexStore}, which shares them; this maps a private one.
     *
     * @param file a file written by {@link TermIndexWriter}
     * @return the index
     * @throws IOException if the file cannot be read or is not a term index
     */
    public static TermIndex open(Path file) throws IOException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            MemorySegment segment = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size(),
                Arena.ofAuto());
            long size = segment.byteSize();
            long headerSize = MAGIC.length + 12L;
            // the version from the mapped bytes themselves, not from a second read of the file
            byte[] head = segment.asSlice(0, Math.min(size, MAGIC.length + 4L)).toArray(BYTE);
            OptionalInt version = version(head, head.length);
            if (version.isPresent() && version.getAsInt() != VERSION) {
                throw new IOException(file + " is a term index of index version " + version.getAsInt()
                    + ", but this build reads version " + VERSION + "; the cache rebuilds it");
            }
            if (size < headerSize || !Arrays.equals(segment.asSlice(0, MAGIC.length).toArray(BYTE), MAGIC)) {
                throw new IOException(file + " is not a term index");
            }
            int termCount = segment.get(INT, MAGIC.length + 4L);
            int relationCount = segment.get(INT, MAGIC.length + 8L);
            if (termCount < 0 || relationCount < 0) {
                throw new IOException(file + " is not a term index: negative counts");
            }
            if (relationCount > (size - headerSize) / 4) {
                throw new IOException(file + " is not a term index: truncated header");
            }
            List<String> relations = new ArrayList<>(relationCount);
            long position = headerSize;
            for (int r = 0; r < relationCount; r++) {
                if (position + 4 > size) {
                    throw new IOException(file + " is not a term index: truncated header");
                }
                int length = segment.get(INT, position);
                if (length < 0 || position + 4 + length > size) {
                    throw new IOException(file + " is not a term index: truncated header");
                }
                relations.add(utf8(segment, position + 4, length));
                position += 4L + length;
            }
            if (position + 8L * termCount > size) {
                throw new IOException(file + " is not a term index: truncated offset table");
            }
            if (recordsEnd(segment, position, termCount, relationCount) != size) {
                throw new IOException(file + " is not a term index: truncated records");
            }
            return new TermIndex(file, segment, termCount, List.copyOf(relations), position);
        }
    }

    /**
     * Where the records end, if the first record starts right after the offset
     * table and the last one lies within the file; -1 otherwise. The writer
     * writes the records in table order, so the last record ends the file.
     */
    private static long recordsEnd(MemorySegment segment, long tableStart, int termCount,
        int relationCount) {
        long size = segment.byteSize();
        long recordsStart = tableStart + 8L * termCount;
        if (termCount == 0) {
            return recordsStart;
        }
        long first = segment.get(LONG, tableStart);
        long position = segment.get(LONG, tableStart + 8L * (termCount - 1));
        if (first != recordsStart || position < first) {
            return -1;
        }
        for (int field = 0; field < 2; field++) {
            if (position + 4 > size) {
                return -1;
            }
            int length = segment.get(INT, position);
            if (length < 0) {
                return -1;
            }
            position += 4L + length;
        }
        position += 1;
        for (int r = 0; r < relationCount; r++) {
            if (position + 2 > size) {
                return -1;
            }
            position += 2 + 4L * Short.toUnsignedInt(segment.get(SHORT, position));
        }
        return position;
    }

    /**
     * Looks up a term.
     *
     * @param oboId the obo id, e.g. {@code MS:1000073}; case-sensitive
     * @return the term, or empty if the release has no such term
     * @throws IllegalStateException if a stored parent is not a term of the
     * index: the file is corrupt
     */
    public Optional<TermRecord> term(String oboId) {
        int ordinal = ordinal(oboId);
        return ordinal < 0 ? Optional.empty() : Optional.of(record(ordinal));
    }

    /**
     * The transitive ancestors of a term over every relation of the index,
     * breadth-first, without duplicates and without the term itself. A cycle
     * ends the walk where it closes.
     *
     * @param oboId the obo id; case-sensitive
     * @return the ancestors, nearest first; empty for an unknown id or a root
     * @throws IllegalStateException if a stored parent is not a term of the
     * index: the file is corrupt
     */
    public List<TermRecord> ancestors(String oboId) {
        int start = ordinal(oboId);
        if (start < 0) {
            return List.of();
        }
        Set<Integer> seen = new HashSet<>();
        seen.add(start);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        List<TermRecord> ancestors = new ArrayList<>();
        while (!queue.isEmpty()) {
            long position = skipIdLabelAndObsolete(offset(queue.poll()));
            for (int r = 0; r < relations.size(); r++) {
                int count = Short.toUnsignedInt(segment.get(SHORT, position));
                position += 2;
                for (int p = 0; p < count; p++) {
                    int parent = parentOrdinal(position);
                    position += 4;
                    if (seen.add(parent)) {
                        queue.add(parent);
                        ancestors.add(record(parent));
                    }
                }
            }
        }
        return ancestors;
    }

    /**
     * The number of terms.
     *
     * @return the term count
     */
    public int size() {
        return termCount;
    }

    @Override
    public String toString() {
        return "TermIndex[" + file + ", " + termCount + " terms, relations " + relations + "]";
    }

    static String utf8(MemorySegment segment, long position, int length) {
        return new String(segment.asSlice(position, length).toArray(BYTE), StandardCharsets.UTF_8);
    }

    /** A stored parent ordinal; one outside the table means the file is corrupt. */
    private int parentOrdinal(long position) {
        int ordinal = segment.get(INT, position);
        if (ordinal < 0 || ordinal >= termCount) {
            throw new IllegalStateException(file + " is corrupt: the parent ordinal " + ordinal
                + " at byte " + position + " is not one of its " + termCount + " terms");
        }
        return ordinal;
    }

    private long offset(int ordinal) {
        return segment.get(LONG, tableStart + 8L * ordinal);
    }

    /** Binary search over the offset table, comparing UTF-8 bytes unsigned. */
    private int ordinal(String oboId) {
        MemorySegment key = MemorySegment.ofArray(oboId.getBytes(StandardCharsets.UTF_8));
        long keyLength = key.byteSize();
        int low = 0;
        int high = termCount - 1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            long position = offset(middle);
            int length = segment.get(INT, position);
            long idStart = position + 4;
            long mismatch = MemorySegment.mismatch(segment, idStart, idStart + length, key, 0, keyLength);
            int comparison;
            if (mismatch < 0) {
                return middle;
            } else if (mismatch == length) {
                comparison = -1;
            } else if (mismatch == keyLength) {
                comparison = 1;
            } else {
                comparison = Byte.toUnsignedInt(segment.get(BYTE, idStart + mismatch))
                    - Byte.toUnsignedInt(key.get(BYTE, mismatch));
            }
            if (comparison < 0) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        return -1;
    }

    private long skipIdLabelAndObsolete(long position) {
        long labelPosition = position + 4 + segment.get(INT, position);
        return labelPosition + 4 + segment.get(INT, labelPosition) + 1;
    }

    private String id(int ordinal) {
        long position = offset(ordinal);
        return utf8(segment, position + 4, segment.get(INT, position));
    }

    private TermRecord record(int ordinal) {
        long position = offset(ordinal);
        int idLength = segment.get(INT, position);
        String id = utf8(segment, position + 4, idLength);
        position += 4 + idLength;
        int labelLength = segment.get(INT, position);
        String label = utf8(segment, position + 4, labelLength);
        position += 4 + labelLength;
        boolean obsolete = segment.get(BYTE, position++) != 0;
        Map<String, List<String>> parents = new LinkedHashMap<>();
        for (String relation : relations) {
            int count = Short.toUnsignedInt(segment.get(SHORT, position));
            position += 2;
            if (count > 0) {
                List<String> ids = new ArrayList<>(count);
                for (int p = 0; p < count; p++) {
                    ids.add(id(parentOrdinal(position)));
                    position += 4;
                }
                parents.put(relation, ids);
            }
        }
        return new TermRecord(id, label, obsolete, parents);
    }
}
