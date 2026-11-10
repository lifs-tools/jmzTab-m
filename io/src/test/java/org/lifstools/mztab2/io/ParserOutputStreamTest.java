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
package org.lifstools.mztab2.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * The parser writes its messages to the caller's stream and leaves it open,
 * also after an error that ends the validation. Before, it closed the stream
 * there, so the CLI, which passes System.out, printed neither the other
 * messages, e.g. the other missing columns (#244), nor its summary.
 */
public class ParserOutputStreamTest {

    @TempDir
    Path tempDir;

    @Test
    void streamStaysOpenAndListsAllMessagesAfterAFatalError() throws IOException {
        String text;
        try (InputStream in = getClass().getResourceAsStream(
            "/metabolomics/lipidomics-example.mzTab")) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        List<String> missing = List.of("smiles", "inchi", "reliability");
        String variant = Arrays.stream(text.split("\n", -1)).
            map(line -> line.startsWith("SMH\t")
                ? Arrays.stream(line.split("\t", -1)).
                    filter(cell -> !missing.contains(cell)).
                    collect(Collectors.joining("\t"))
                : line).
            collect(Collectors.joining("\n"));
        Path file = tempDir.resolve("missing-columns.mzTab");
        Files.writeString(file, variant, StandardCharsets.UTF_8);

        CloseRecordingStream out = new CloseRecordingStream();
        MZTabErrorList errors = new MzTabFileParser(file.toFile()).
            parse(out, MZTabErrorType.Level.Info, 500);

        assertEquals(3, errors.getErrorList().stream().
            filter(e -> e.getType() == FormatErrorType.StableColumn).count(),
            () -> errors.toString());
        assertFalse(out.closed, "the parser closed the caller's stream");
        String written = out.toString(StandardCharsets.UTF_8);
        for (String column : missing) {
            assertTrue(written.contains("Mandatory column \"" + column + "\""),
                written);
        }
    }

    /**
     * Like a closed System.out: what is written after close is lost.
     */
    private static final class CloseRecordingStream extends ByteArrayOutputStream {

        private boolean closed;

        @Override
        public synchronized void write(int b) {
            if (!closed) {
                super.write(b);
            }
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            if (!closed) {
                super.write(b, off, len);
            }
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
