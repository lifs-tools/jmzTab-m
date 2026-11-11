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

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
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
import java.util.Set;

/**
 * The terms of one vocabulary release, read from an immutable, memory-mapped
 * {@code terms.idx} file written by {@link TermIndexWriter}.
 *
 * <p>
 * The layout, big-endian:</p>
 * <pre>
 * magic "JMZTIDX1" (8 bytes) | int termCount | int relationCount | relation names (int length + UTF-8)
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
 * Safe for use by several threads; using it after {@link #close()} throws
 * {@link IllegalStateException}. The mapping is released by the garbage
 * collector once the index is unreachable, not by {@link #close()}: a native
 * image supports closing a mapping that several threads share only with an
 * experimental build option.</p>
 */
public final class TermIndex implements Closeable {

    static final byte[] MAGIC = "JMZTIDX1".getBytes(StandardCharsets.US_ASCII);
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

    private volatile boolean closed;

    private TermIndex(Path file, MemorySegment segment, int termCount, List<String> relations,
        long tableStart) {
        this.file = file;
        this.segment = segment;
        this.termCount = termCount;
        this.relations = relations;
        this.tableStart = tableStart;
    }

    /**
     * Maps an index file.
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
            long headerSize = MAGIC.length + 8L;
            if (size < headerSize || !Arrays.equals(segment.asSlice(0, MAGIC.length).toArray(BYTE), MAGIC)) {
                throw new IOException(file + " is not a term index");
            }
            int termCount = segment.get(INT, MAGIC.length);
            int relationCount = segment.get(INT, MAGIC.length + 4L);
            if (termCount < 0 || relationCount < 0) {
                throw new IOException(file + " is not a term index: negative counts");
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
            return new TermIndex(file, segment, termCount, List.copyOf(relations), position);
        }
    }

    /**
     * Looks up a term.
     *
     * @param oboId the obo id, e.g. {@code MS:1000073}; case-sensitive
     * @return the term, or empty if the release has no such term
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
                    int parent = segment.get(INT, position);
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
        checkOpen();
        return termCount;
    }

    /** Ends the use of this index; the mapping goes once the index is unreachable. */
    @Override
    public void close() {
        closed = true;
    }

    @Override
    public String toString() {
        return "TermIndex[" + file + ", " + termCount + " terms, relations " + relations + "]";
    }

    static String utf8(MemorySegment segment, long position, int length) {
        return new String(segment.asSlice(position, length).toArray(BYTE), StandardCharsets.UTF_8);
    }

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException(this + " is closed");
        }
    }

    private long offset(int ordinal) {
        return segment.get(LONG, tableStart + 8L * ordinal);
    }

    /** Binary search over the offset table, comparing UTF-8 bytes unsigned. */
    private int ordinal(String oboId) {
        checkOpen();
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
                    ids.add(id(segment.get(INT, position)));
                    position += 4;
                }
                parents.put(relation, ids);
            }
        }
        return new TermRecord(id, label, obsolete, parents);
    }
}
