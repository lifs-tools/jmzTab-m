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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Writes a {@link TermIndex} file from a stream of {@link TermRecord}s.
 *
 * <p>
 * The records are spilled to a temporary file next to the target as they
 * come; only the ids and their spill offsets stay on the heap. {@link #commit()}
 * sorts the ids, resolves the parent ids to ordinals, writes the index to a
 * second temporary file and moves it onto the target atomically. Closing an
 * uncommitted writer deletes both temporary files and leaves the target as it
 * was.</p>
 *
 * <p>
 * A term given more than once is merged: its label is the first non-empty
 * one, its parents are the union per relation, and it is obsolete if any of
 * its records says so. A parent that is not among the terms, one named in
 * the file without a stanza of its own, is kept as a stub term (Ruling
 * R-AA): its id, as label the first name a parent line gives it
 * ({@link #parentName}, e.g. an OBO {@code ! name} comment) or else its id,
 * not obsolete, no parents. OLS lists such
 * parents too (PRIDE's BFO:0000001), and leaving one out could hide a rule
 * term's ancestor.</p>
 *
 * <p>
 * Not thread-safe.</p>
 */
@Slf4j
public final class TermIndexWriter implements Consumer<TermRecord>, Closeable {

    private static final int BUFFER_SIZE = 1 << 16;
    private static final int TABLE_CHUNK_LONGS = 8192;

    private final Path target;
    private final Path spill;
    private final Path output;
    private final DataOutputStream spillOut;
    private final ByteArrayOutputStream recordBytes = new ByteArrayOutputStream();
    private final DataOutputStream record = new DataOutputStream(recordBytes);
    private final Map<String, Integer> relations = new LinkedHashMap<>();
    private List<Entry> entries = new ArrayList<>();
    private Path names;
    private DataOutputStream namesOut;
    private long spillSize;
    private boolean finished;

    /** A record's id and where its spilled form starts. */
    private record Entry(String id, long spillOffset) {
    }

    private TermIndexWriter(Path target) throws IOException {
        this.target = target.toAbsolutePath();
        Path dir = this.target.getParent();
        String name = this.target.getFileName().toString();
        this.spill = createTemporary(dir, name, ".spill");
        Path out = null;
        try {
            out = createTemporary(dir, name, ".tmp");
            this.spillOut = new DataOutputStream(new BufferedOutputStream(
                Files.newOutputStream(spill), BUFFER_SIZE));
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(spill);
            if (out != null) {
                Files.deleteIfExists(out);
            }
            throw e;
        }
        this.output = out;
    }

    /**
     * Creates an empty file with the default permissions, which, unlike
     * {@link Files#createTempFile}, lets other users of a shared cache read the
     * index it becomes.
     */
    private static Path createTemporary(Path dir, String name, String suffix) throws IOException {
        return Files.createFile(dir.resolve(name + "." + UUID.randomUUID() + suffix));
    }

    /**
     * Opens a writer whose {@link #commit()} creates or replaces {@code target}.
     *
     * @param target the index file; its folder must exist
     * @return a writer that accepts the terms
     * @throws IOException if the temporary files cannot be created
     */
    public static TermIndexWriter open(Path target) throws IOException {
        return new TermIndexWriter(target);
    }

    /**
     * Writes the terms to {@code target} atomically.
     *
     * @param terms the terms
     * @param target the index file; its folder must exist
     * @throws IOException if the index cannot be written
     */
    public static void write(Iterable<TermRecord> terms, Path target) throws IOException {
        try (TermIndexWriter writer = open(target)) {
            for (TermRecord term : terms) {
                writer.accept(term);
            }
            writer.commit();
        }
    }

    /**
     * Adds a term.
     *
     * @param term the term
     * @throws UncheckedIOException if the spill file cannot be written
     * @throws IllegalStateException after {@link #commit()} or {@link #close()}
     */
    @Override
    public void accept(TermRecord term) {
        if (finished) {
            throw new IllegalStateException("the writer for " + target + " is finished");
        }
        try {
            recordBytes.reset();
            writeString(record, term.id());
            writeString(record, term.label());
            record.writeBoolean(term.obsolete());
            record.writeInt(term.parentsByRelation().size());
            for (Map.Entry<String, List<String>> parents : term.parentsByRelation().entrySet()) {
                record.writeInt(relations.computeIfAbsent(parents.getKey(), r -> relations.size()));
                record.writeInt(parents.getValue().size());
                for (String parent : parents.getValue()) {
                    writeString(record, parent);
                }
            }
            recordBytes.writeTo(spillOut);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot spill a term for " + target, e);
        }
        entries.add(new Entry(term.id(), spillSize));
        spillSize += recordBytes.size();
    }

    /**
     * Gives the name a file's parent line gives a parent, e.g. {@code unit}
     * for {@code is_a: UO:0000000 ! unit}. A parent without a stanza of its
     * own gets the first non-empty name given for it as its label (Ruling
     * R-AA), else its id; the names of terms with a stanza are not used. The
     * names are spilled to a temporary file, as the records are.
     *
     * @param id the parent id
     * @param name the name given for it; ignored if empty
     * @throws UncheckedIOException if the spill file cannot be written
     * @throws IllegalStateException after {@link #commit()} or {@link #close()}
     */
    public void parentName(String id, String name) {
        if (finished) {
            throw new IllegalStateException("the writer for " + target + " is finished");
        }
        if (name.isEmpty()) {
            return;
        }
        try {
            if (namesOut == null) {
                names = createTemporary(target.getParent(), target.getFileName().toString(), ".names");
                namesOut = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(names),
                    BUFFER_SIZE));
            }
            writeString(namesOut, id);
            writeString(namesOut, name);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot spill a parent name for " + target, e);
        }
    }

    /**
     * Writes the index and moves it onto the target. The writer is finished
     * afterwards.
     *
     * @throws IOException if the index cannot be written; the target is then
     * unchanged
     */
    public void commit() throws IOException {
        if (finished) {
            throw new IllegalStateException("the writer for " + target + " is finished");
        }
        finished = true;
        spillOut.close();
        if (namesOut != null) {
            namesOut.close();
        }
        entries.sort((a, b) -> compareIds(a.id(), b.id()));
        int[] groupStarts = groupStarts(entries);
        int groupCount = groupStarts.length - 1;
        String[] termIds = new String[groupCount];
        for (int g = 0; g < groupCount; g++) {
            termIds[g] = entries.get(groupStarts[g]).id();
        }
        List<String> relationOrder = List.copyOf(relations.keySet());
        List<byte[]> relationNames = relationOrder.stream()
            .map(r -> r.getBytes(StandardCharsets.UTF_8)).toList();
        long headerSize = TermIndex.MAGIC.length + 4L + 4L + 4L;
        for (byte[] relationName : relationNames) {
            headerSize += 4L + relationName.length;
        }
        try (Arena arena = Arena.ofConfined();
            FileChannel spilled = FileChannel.open(spill, StandardOpenOption.READ);
            FileChannel out = FileChannel.open(output, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            MemorySegment spillSegment = spilled.map(FileChannel.MapMode.READ_ONLY, 0, spillSize, arena);
            Set<String> stubs = parentsWithoutStanza(spillSegment, termIds);
            Map<String, String> stubNames = stubNames(stubs);
            String[] ids = merge(termIds, stubs);
            int termCount = ids.length;
            long dataStart = headerSize + 8L * termCount;
            long[] offsets = new long[termCount];
            out.position(dataStart);
            OutputStream records = new BufferedOutputStream(Channels.newOutputStream(out), BUFFER_SIZE);
            long position = dataStart;
            int group = 0;
            for (int t = 0; t < termCount; t++) {
                offsets[t] = position;
                if (group < groupCount && termIds[group].equals(ids[t])) {
                    writeTerm(spillSegment, entries.subList(groupStarts[group], groupStarts[group + 1]), ids,
                        relationOrder);
                    group++;
                } else {
                    writeStub(ids[t], stubNames.getOrDefault(ids[t], ids[t]), relationOrder.size());
                }
                recordBytes.writeTo(records);
                position += recordBytes.size();
            }
            records.flush();
            writeHeader(out, termCount, relationNames);
            writeTable(out, headerSize, offsets);
            out.force(true);
        }
        entries = null;
        Files.move(output, target, StandardCopyOption.ATOMIC_MOVE);
        Files.deleteIfExists(spill);
        if (names != null) {
            Files.deleteIfExists(names);
        }
    }

    /**
     * Deletes the temporary files. Without a {@link #commit()} before, the
     * target is left as it was.
     *
     * @throws IOException if a temporary file cannot be deleted
     */
    @Override
    public void close() throws IOException {
        finished = true;
        entries = null;
        try {
            spillOut.close();
            if (namesOut != null) {
                namesOut.close();
            }
        } finally {
            Files.deleteIfExists(spill);
            Files.deleteIfExists(output);
            if (names != null) {
                Files.deleteIfExists(names);
            }
        }
    }

    /**
     * Orders obo ids as their UTF-8 bytes compare, unsigned: by code point.
     * {@link TermIndex} searches its table in this order.
     */
    static int compareIds(String a, String b) {
        int common = Math.min(a.length(), b.length());
        for (int i = 0; i < common; i++) {
            char x = a.charAt(i);
            char y = b.charAt(i);
            if (x != y) {
                boolean xs = Character.isSurrogate(x);
                if (xs != Character.isSurrogate(y)) {
                    // a surrogate stands for a code point above every other char
                    return xs ? 1 : -1;
                }
                return x - y;
            }
        }
        return a.length() - b.length();
    }

    /** The parent ids, over all spilled records, that are none of the terms. */
    private Set<String> parentsWithoutStanza(MemorySegment spilled, String[] termIds) {
        Set<String> stubs = new TreeSet<>(TermIndexWriter::compareIds);
        for (Entry entry : entries) {
            SpillReader in = new SpillReader(spilled, entry.spillOffset());
            in.string();
            in.string();
            in.bool();
            int relationCount = in.integer();
            for (int r = 0; r < relationCount; r++) {
                in.integer();
                int parentCount = in.integer();
                for (int p = 0; p < parentCount; p++) {
                    String parent = in.string();
                    if (Arrays.binarySearch(termIds, parent, TermIndexWriter::compareIds) < 0) {
                        stubs.add(parent);
                    }
                }
            }
        }
        if (!stubs.isEmpty()) {
            log.debug("{}: {} parents without a stanza of their own are kept as stub terms, e.g. {}",
                target.getFileName(), stubs.size(), stubs.iterator().next());
        }
        return stubs;
    }

    /** The sorted term ids and the sorted stub ids, in one sorted array. */
    private static String[] merge(String[] termIds, Set<String> stubs) {
        String[] ids = new String[termIds.length + stubs.size()];
        int t = 0;
        int i = 0;
        for (String stub : stubs) {
            while (t < termIds.length && compareIds(termIds[t], stub) < 0) {
                ids[i++] = termIds[t++];
            }
            ids[i++] = stub;
        }
        while (t < termIds.length) {
            ids[i++] = termIds[t++];
        }
        return ids;
    }

    /** The first name given for each stub, from the names spill file. */
    private Map<String, String> stubNames(Set<String> stubs) throws IOException {
        Map<String, String> found = new HashMap<>();
        if (namesOut == null || stubs.isEmpty()) {
            return found;
        }
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(names),
            BUFFER_SIZE))) {
            while (found.size() < stubs.size()) {
                int length;
                try {
                    length = in.readInt();
                } catch (EOFException end) {
                    break;
                }
                String id = new String(in.readNBytes(length), StandardCharsets.UTF_8);
                String name = new String(in.readNBytes(in.readInt()), StandardCharsets.UTF_8);
                if (stubs.contains(id)) {
                    found.putIfAbsent(id, name);
                }
            }
        }
        return found;
    }

    /** A parent without a stanza: the name its parent lines give it, else its id; not obsolete, no parents. */
    private void writeStub(String id, String label, int relationCount) throws IOException {
        recordBytes.reset();
        writeString(record, id);
        writeString(record, label);
        record.writeBoolean(false);
        for (int r = 0; r < relationCount; r++) {
            record.writeShort(0);
        }
    }

    /** The first entry of each distinct id, then the entry count. */
    private static int[] groupStarts(List<Entry> sorted) {
        int[] starts = new int[sorted.size() + 1];
        int groups = 0;
        for (int i = 0; i < sorted.size(); i++) {
            if (i == 0 || !sorted.get(i).id().equals(sorted.get(i - 1).id())) {
                starts[groups++] = i;
            }
        }
        starts[groups] = sorted.size();
        return Arrays.copyOf(starts, groups + 1);
    }

    /** Merges the spilled records of one id into {@link #recordBytes}, in the index layout. */
    private void writeTerm(MemorySegment spilled, List<Entry> records, String[] ids,
        List<String> relationNames) throws IOException {
        String id = records.get(0).id();
        String label = "";
        boolean obsolete = false;
        List<Set<String>> parents = new ArrayList<>(relationNames.size());
        for (int r = 0; r < relationNames.size(); r++) {
            parents.add(new LinkedHashSet<>());
        }
        for (Entry entry : records) {
            SpillReader in = new SpillReader(spilled, entry.spillOffset());
            in.string();
            String recordLabel = in.string();
            if (label.isEmpty()) {
                label = recordLabel;
            }
            obsolete |= in.bool();
            int relationCount = in.integer();
            for (int r = 0; r < relationCount; r++) {
                Set<String> relationParents = parents.get(in.integer());
                int parentCount = in.integer();
                for (int p = 0; p < parentCount; p++) {
                    relationParents.add(in.string());
                }
            }
        }
        recordBytes.reset();
        writeString(record, id);
        writeString(record, label);
        record.writeBoolean(obsolete);
        for (int r = 0; r < parents.size(); r++) {
            int[] ordinals = new int[parents.get(r).size()];
            int count = 0;
            for (String parent : parents.get(r)) {
                int ordinal = Arrays.binarySearch(ids, parent, TermIndexWriter::compareIds);
                if (ordinal < 0) {
                    throw new IllegalStateException("the " + relationNames.get(r) + " parent " + parent + " of "
                        + id + " is neither a term nor a stub of " + target);
                }
                ordinals[count++] = ordinal;
            }
            if (count > TermIndex.MAX_PARENTS) {
                throw new IOException(id + " has " + count + " " + relationNames.get(r)
                    + " parents; a term index holds at most " + TermIndex.MAX_PARENTS);
            }
            record.writeShort(count);
            for (int p = 0; p < count; p++) {
                record.writeInt(ordinals[p]);
            }
        }
    }

    private void writeHeader(FileChannel out, int termCount, List<byte[]> relationNames)
        throws IOException {
        int size = TermIndex.MAGIC.length + 12;
        for (byte[] name : relationNames) {
            size += 4 + name.length;
        }
        ByteBuffer header = ByteBuffer.allocate(size);
        header.put(TermIndex.MAGIC).putInt(TermIndex.VERSION).putInt(termCount).putInt(relationNames.size());
        for (byte[] name : relationNames) {
            header.putInt(name.length).put(name);
        }
        writeFully(out, header.flip(), 0);
    }

    private static void writeTable(FileChannel out, long tableStart, long[] offsets) throws IOException {
        ByteBuffer chunk = ByteBuffer.allocate(8 * TABLE_CHUNK_LONGS);
        long position = tableStart;
        for (int from = 0; from < offsets.length; from += TABLE_CHUNK_LONGS) {
            chunk.clear();
            int to = Math.min(offsets.length, from + TABLE_CHUNK_LONGS);
            for (int i = from; i < to; i++) {
                chunk.putLong(offsets[i]);
            }
            chunk.flip();
            int length = chunk.remaining();
            writeFully(out, chunk, position);
            position += length;
        }
    }

    private static void writeFully(FileChannel out, ByteBuffer buffer, long position) throws IOException {
        long at = position;
        while (buffer.hasRemaining()) {
            at += out.write(buffer, at);
        }
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    /** Reads one spilled record. */
    private static final class SpillReader {

        private final MemorySegment segment;
        private long position;

        private SpillReader(MemorySegment segment, long position) {
            this.segment = segment;
            this.position = position;
        }

        private int integer() {
            int value = segment.get(TermIndex.INT, position);
            position += 4;
            return value;
        }

        private boolean bool() {
            return segment.get(TermIndex.BYTE, position++) != 0;
        }

        private String string() {
            int length = integer();
            String value = TermIndex.utf8(segment, position, length);
            position += length;
            return value;
        }
    }
}
