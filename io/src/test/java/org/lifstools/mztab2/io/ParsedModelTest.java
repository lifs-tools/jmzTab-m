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
import java.util.List;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * The parser builds the model unless a data line has an error; warnings and
 * infos on data lines must not depend on the reporting level.
 */
public class ParsedModelTest {

    private static final String BASE_FILE = "/metabolomics/lipidomics-example.mzTab";

    @TempDir
    Path tempDir;

    @ParameterizedTest
    @EnumSource(MZTabErrorType.Level.class)
    void warningsOnDataLinesKeepTheModel(MZTabErrorType.Level level) throws IOException {
        // an id_format the validator cannot check: a warning on every evidence row
        Path file = variant("[MS, MS:1000768, Thermo nativeID format, ]",
            "[MS, MS:1000824, no nativeID format, ]");
        MzTabFileParser parser = new MzTabFileParser(file.toFile());
        MZTabErrorList errors = parser.parse(new ByteArrayOutputStream(), level, 1000);
        if (level != MZTabErrorType.Level.Error) {
            assertEquals(4, errors.getErrorList().stream().
                filter(e -> e.getType() == LogicalErrorType.SpectraIdFormatNotSupported).
                count(), () -> errors.toString());
        }
        assertNotNull(parser.getMZTabFile(), () -> errors.toString());
        assertEquals(4, parser.getMZTabFile().getSmallMoleculeEvidence().size());
    }

    @ParameterizedTest
    @EnumSource(MZTabErrorType.Level.class)
    void errorsOnDataLinesDropTheModel(MZTabErrorType.Level level) throws IOException {
        // a mass that is not a number in the first feature row
        Path file = variant("\t650.6432\t", "\tabc\t");
        MzTabFileParser parser = new MzTabFileParser(file.toFile());
        MZTabErrorList errors = parser.parse(new ByteArrayOutputStream(), level, 1000);
        assertTrue(errors.hasErrors(), () -> errors.toString());
        assertNull(parser.getMZTabFile());
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
        "MTD\tms_run[1]-instrument_ref\t, 2065, instrument[1]-name",
        "MTD\tassay[1]-sample_ref\t, 2066, sample[1]"})
    void unreferencedElementsAreErrorsThatKeepTheModel(String removedLine, int code,
            String key) throws IOException {
        Path file = withoutLine(removedLine);
        MzTabFileParser parser = new MzTabFileParser(file.toFile());
        MZTabErrorList errors = parser.parse(new ByteArrayOutputStream(),
            MZTabErrorType.Level.Info, 1000);
        List<MZTabError> found = errors.getErrorList().stream().
            filter(e -> e.getType().getCode() == code).
            collect(Collectors.toList());
        assertEquals(1, found.size(), () -> errors.toString());
        assertEquals(MZTabErrorType.Level.Error, found.get(0).getType().getLevel());
        assertEquals(key, found.get(0).getLocation().getMetadataKey());
        assertNotNull(parser.getMZTabFile(), "a missing reference must not drop the model");
    }

    private Path withoutLine(String prefix) throws IOException {
        String text;
        try (InputStream is = getClass().getResourceAsStream(BASE_FILE)) {
            text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        String kept = text.lines().
            filter(line -> !line.startsWith(prefix)).
            collect(Collectors.joining("\n", "", "\n"));
        assertTrue(kept.length() < text.length(), prefix);
        Path file = tempDir.resolve("without.mzTab");
        Files.writeString(file, kept, StandardCharsets.UTF_8);
        return file;
    }

    private Path variant(String from, String to) throws IOException {
        String text;
        try (InputStream is = getClass().getResourceAsStream(BASE_FILE)) {
            assertNotNull(is, "Missing test resource " + BASE_FILE);
            text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(text.contains(from), from);
        Path file = tempDir.resolve("variant.mzTab");
        Files.writeString(file, text.replaceFirst(java.util.regex.Pattern.quote(from),
            java.util.regex.Matcher.quoteReplacement(to)), StandardCharsets.UTF_8);
        return file;
    }
}
