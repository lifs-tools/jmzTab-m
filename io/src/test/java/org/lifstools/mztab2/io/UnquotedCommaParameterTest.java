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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * A parameter name or value with commas must be quoted (mzTab-M 2.0:
 * {@code [, , "a, b", ]}). Unquoted, the parameter has more than four fields.
 * Before, such an instrument name ended the validation with "Column
 * "instrument[1]" is an empty cell" (Error-2000), and the lines after it were
 * not checked. Now each such parameter is reported at its line as an invalid
 * parameter (Error-1012) or parameter list (Error-1013), and the validation
 * goes on.
 */
public class UnquotedCommaParameterTest {

    private static final String BASE_FILE = "/metabolomics/minimal-m-2.0.mztab";
    private static final String INSTRUMENT_NAME
        = "MTD\tinstrument[1]-name\t[MS, MS:1000449, LTQ Orbitrap, ]";
    private static final String SAMPLE_PROCESSING
        = "MTD\tsample_processing[1]\t[MSIO, MSIO:0000148, high performance liquid chromatography, ]";
    private static final String FRAGMENTATION_METHOD
        = "MTD\tms_run[1]-fragmentation_method\t[MS, MS:1000133, CID, ]";
    private static final String UNQUOTED_NAME
        = "qtof 9000 (example vendor, city, country)";

    @TempDir
    Path tempDir;

    @Test
    void baseFileHasNoErrors() throws IOException {
        assertEquals(List.of(), errorsAndWarnings(parse(base())));
    }

    @Test
    void unquotedCommasInTheInstrumentNameAreAnInvalidParameter() throws IOException {
        String line = "MTD\tinstrument[1]-name\t[MS, MS:1002584, " + UNQUOTED_NAME + ", ]";
        String text = replace(base(), INSTRUMENT_NAME, line);
        List<MZTabError> errors = errorsAndWarnings(parse(text));
        assertEquals(1, errors.size(), errors::toString);
        MZTabError error = errors.get(0);
        assertEquals(FormatErrorType.Param, error.getType(), errors::toString);
        assertEquals(lineOf(text, line), error.getLineNumber());
        assertEquals("instrument[1]-name", error.getLocation().getMetadataKey());
        assertTrue(error.getMessage().contains("instrument[1]-name"), error.getMessage());
        assertTrue(error.getMessage().contains(UNQUOTED_NAME), error.getMessage());
        assertTrue(error.getMessage().contains("put a name or value that contains commas in double quotes"),
            error.getMessage());
    }

    @Test
    void quotedNameIsValid() throws IOException {
        MzTabFileParser parser = parse(replace(base(), INSTRUMENT_NAME,
            "MTD\tinstrument[1]-name\t[MS, MS:1002584, \"" + UNQUOTED_NAME + "\", ]"));
        assertEquals(List.of(), errorsAndWarnings(parser));
        assertNotNull(parser.getMZTabFile());
        assertEquals(UNQUOTED_NAME, parser.getMZTabFile().getMetadata().
            getInstrument().get(0).getName().getName());
    }

    @Test
    void userParamsWithUnquotedCommasAreReportedAtTheirLines() throws IOException {
        String sp = "MTD\tsample_processing[1]\t[, , extraction (methanol, chloroform, water), ]";
        String fm = "MTD\tms_run[1]-fragmentation_method[1]\t[, , CID, 35 eV, ]";
        String text = replace(replace(base(), SAMPLE_PROCESSING, sp),
            FRAGMENTATION_METHOD, fm);
        List<MZTabError> errors = errorsAndWarnings(parse(text));
        assertEquals(2, errors.size(), errors::toString);
        assertEquals(FormatErrorType.ParamList, errors.get(0).getType(), errors::toString);
        assertEquals(lineOf(text, sp), errors.get(0).getLineNumber());
        assertTrue(errors.get(0).getMessage().contains("sample_processing[1]"),
            errors.get(0).getMessage());
        assertEquals(FormatErrorType.Param, errors.get(1).getType(), errors::toString);
        assertEquals(lineOf(text, fm), errors.get(1).getLineNumber());
        assertTrue(errors.get(1).getMessage().contains("ms_run[1]-fragmentation_method[1]"),
            errors.get(1).getMessage());
    }

    @Test
    void allUnquotedParametersOfAFileAreReported() throws IOException {
        // the corpus case: the instrument name no longer ends the validation
        String name = "MTD\tinstrument[1]-name\t[MS, MS:1002584, " + UNQUOTED_NAME + ", ]";
        String sp = "MTD\tsample_processing[1]\t[, , extraction (methanol, chloroform, water), ]";
        String fm = "MTD\tms_run[1]-fragmentation_method[1]\t[, , CID, 35 eV, ]";
        String text = replace(replace(replace(base(), INSTRUMENT_NAME, name),
            SAMPLE_PROCESSING, sp), FRAGMENTATION_METHOD, fm);
        List<MZTabError> errors = errorsAndWarnings(parse(text));
        assertEquals(List.of(
            FormatErrorType.ParamList.getCode() + "@" + lineOf(text, sp),
            FormatErrorType.Param.getCode() + "@" + lineOf(text, name),
            FormatErrorType.Param.getCode() + "@" + lineOf(text, fm)),
            errors.stream().
                map(e -> e.getType().getCode() + "@" + e.getLineNumber()).
                collect(Collectors.toList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "MTD\tms_run[3]-fragmentation_method[1]\t[, , CID, 35 eV, ]",
        "MTD\tsoftware[3]\t[, , my tool, version 1, build 2]"})
    void invalidParameterOnTheFirstLineOfAnElementDoesNotEndTheValidation(String line) throws IOException {
        // ms_run[3] and software[3] do not exist before this line; a second
        // invalid parameter after it shows that the validation goes on
        String later = "MTD\tsample[2]-custom[1]\t[, , extraction, 4 degrees, dark]";
        String text = replace(replace(base(), FRAGMENTATION_METHOD,
            FRAGMENTATION_METHOD + "\n" + line),
            "MTD\tsample[2]-description\tHealthy control samples.",
            "MTD\tsample[2]-description\tHealthy control samples.\n" + later);
        List<MZTabError> errors = errorsAndWarnings(parse(text));
        assertEquals(List.of(
            FormatErrorType.Param.getCode() + "@" + lineOf(text, line),
            FormatErrorType.Param.getCode() + "@" + lineOf(text, later)),
            errors.stream().
                map(e -> e.getType().getCode() + "@" + e.getLineNumber()).
                collect(Collectors.toList()));
    }

    @Test
    void psiMsNameOfTheInstrumentIsValid() throws IOException {
        MzTabFileParser parser = parse(replace(base(), INSTRUMENT_NAME,
            "MTD\tinstrument[1]-name\t[MS, MS:1002584, TripleTOF 5600+, ]"));
        assertEquals(List.of(), errorsAndWarnings(parser));
        assertEquals("TripleTOF 5600+", parser.getMZTabFile().getMetadata().
            getInstrument().get(0).getName().getName());
    }

    private String base() throws IOException {
        try (InputStream in = getClass().getResourceAsStream(BASE_FILE)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String replace(String text, String original, String replacement) {
        assertEquals(1, text.split(java.util.regex.Pattern.quote(original), -1).length - 1,
            "the file must contain " + original + " once");
        return text.replace(original, replacement);
    }

    private static int lineOf(String text, String line) {
        List<String> lines = Arrays.asList(text.split("\r?\n", -1));
        int index = lines.indexOf(line);
        assertTrue(index >= 0, "no line " + line);
        return index + 1;
    }

    private MzTabFileParser parse(String text) throws IOException {
        Path file = tempDir.resolve("variant.mztab");
        Files.writeString(file, text, StandardCharsets.UTF_8);
        MzTabFileParser parser = new MzTabFileParser(file.toFile());
        // an error that ends the validation is in the error list, too
        parser.parse(new ByteArrayOutputStream(), MZTabErrorType.Level.Info, 500);
        return parser;
    }

    private static List<MZTabError> errorsAndWarnings(MzTabFileParser parser) {
        return parser.getErrorList().getErrorList().stream().
            filter(e -> e.getType().getLevel() != MZTabErrorType.Level.Info).
            collect(Collectors.toList());
    }
}
