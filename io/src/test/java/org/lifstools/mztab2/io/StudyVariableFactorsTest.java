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
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.StudyVariable;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * study_variable[1-n]-factors (mzTab-M 2.0) is read into the model, written
 * back, and reported for files that declare mzTab-M 2.1 (#186).
 */
public class StudyVariableFactorsTest {

    private static final String BASE_FILE = "/metabolomics/lipidomics-example.mzTab";
    private static final String FACTORS = "MTD\tstudy_variable[1]-factors\t[, , strain, C57BL/6]|[, , dose, 0.5 mg]";

    @TempDir
    Path tempDir;

    private MzTabFileParser parser;

    private MZTabErrorList parse(String text) throws IOException {
        Path file = tempDir.resolve("factors.mzTab");
        Files.writeString(file, text, StandardCharsets.UTF_8);
        parser = new MzTabFileParser(file.toFile());
        return parser.parse(new ByteArrayOutputStream(), MZTabErrorType.Level.Info, 1000);
    }

    /** The base file with the factors line after the given line prefix. */
    private static String withFactors(String version, String after, String factors) throws IOException {
        String text;
        try (InputStream is = StudyVariableFactorsTest.class.getResourceAsStream(BASE_FILE)) {
            text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\r?\n")) {
            if (line.startsWith("MTD\tmzTab-version\t")) {
                line = "MTD\tmzTab-version\t" + version;
            }
            sb.append(line).append('\n');
            if (line.startsWith(after)) {
                sb.append(factors).append('\n');
            }
        }
        return sb.toString();
    }

    private static List<MZTabError> ofType(MZTabErrorList errors, MZTabErrorType type) {
        return errors.getErrorList().stream().
            filter(e -> e.getType() == type).
            collect(Collectors.toList());
    }

    @Test
    void factorsOfA20FileAreReadWithoutMessagesAndWrittenBack() throws IOException {
        MZTabErrorList errors = parse(withFactors("2.0.0-M", "MTD\tstudy_variable[1]-description", FACTORS));
        assertTrue(ofType(errors, LogicalErrorType.StudyVariableFactorsRemoved).isEmpty(), errors.toString());
        assertTrue(ofType(errors, FormatErrorType.ParamList).isEmpty(), errors.toString());
        MzTab mzTab = parser.getMZTabFile();
        assertNotNull(mzTab, errors.toString());
        StudyVariable sv = mzTab.getMetadata().getStudyVariable().get(0);
        assertEquals(2, sv.getFactors().size());
        assertEquals("strain", sv.getFactors().get(0).getName());
        assertEquals("0.5 mg", sv.getFactors().get(1).getValue());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(bytes, StandardCharsets.UTF_8)) {
            new MzTabNonValidatingWriter().write(writer, mzTab);
        }
        String written = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(written.contains("MTD\tstudy_variable[1]-factors\t"), written);
        assertTrue(written.contains("dose"), written);
    }

    @Test
    void factorsOfA21FileAreKeptAndReportedWhereverTheVersionLineIs() throws IOException {
        // the factors line comes before the mzTab-version line
        String version = "MTD\tmzTab-version\t2.1.0-M\n";
        String text = withFactors("2.1.0-M", "MTD\tmzTab-version", FACTORS);
        assertTrue(text.contains(version + FACTORS + "\n"));
        MZTabErrorList errors = parse(text.replace(version + FACTORS + "\n", FACTORS + "\n" + version));
        List<MZTabError> removed = ofType(errors, LogicalErrorType.StudyVariableFactorsRemoved);
        assertEquals(1, removed.size(), errors.toString());
        assertEquals(MZTabErrorType.Level.Warn, removed.get(0).getType().getLevel());
        assertEquals("study_variable[1]-factors", removed.get(0).getLocation().getMetadataKey());
        assertTrue(removed.get(0).getMessage().contains("2.1.0-M"), removed.get(0).getMessage());
    }

    @Test
    void aMalformedFactorsValueIsReported() throws IOException {
        MZTabErrorList errors = parse(withFactors("2.0.0-M", "MTD\tstudy_variable[1]-description",
            "MTD\tstudy_variable[1]-factors\tnot a parameter"));
        assertEquals(1, ofType(errors, FormatErrorType.ParamList).size(), errors.toString());
    }
}
