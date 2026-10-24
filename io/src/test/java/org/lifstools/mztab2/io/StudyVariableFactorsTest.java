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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * study_variable[1-n]-factors (mzTab-M 2.0) is validated and reported for
 * files that declare mzTab-M 2.1 (#186); the 2.1 model does not keep it.
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

    /** The base file with the given version and a line inserted after the line with the given prefix. */
    private static String withLine(String version, String after, String inserted) throws IOException {
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
                sb.append(inserted).append('\n');
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
    void factorsOfA20FileAreAcceptedWithoutMessages() throws IOException {
        MZTabErrorList errors = parse(withLine("2.0.0-M", "MTD\tstudy_variable[1]-description", FACTORS));
        assertTrue(ofType(errors, LogicalErrorType.StudyVariableFactorsRemoved).isEmpty(), errors.toString());
        assertTrue(ofType(errors, FormatErrorType.ParamList).isEmpty(), errors.toString());
        assertNotNull(parser.getMZTabFile(), errors.toString());
    }

    @Test
    void factorsOfA21FileAreReportedWhereverTheVersionLineIs() throws IOException {
        // the factors line comes before the mzTab-version line
        String version = "MTD\tmzTab-version\t2.1.0-M\n";
        String text = withLine("2.1.0-M", "MTD\tmzTab-version", FACTORS);
        assertTrue(text.contains(version + FACTORS + "\n"));
        MZTabErrorList errors = parse(text.replace(version + FACTORS + "\n", FACTORS + "\n" + version));
        List<MZTabError> removed = ofType(errors, LogicalErrorType.StudyVariableFactorsRemoved);
        assertEquals(1, removed.size(), errors.toString());
        assertEquals(MZTabErrorType.Level.Warn, removed.get(0).getType().getLevel());
        assertEquals("study_variable[1]-factors", removed.get(0).getLocation().getMetadataKey());
    }

    @Test
    void aMalformedFactorsValueIsReported() throws IOException {
        MZTabErrorList errors = parse(withLine("2.0.0-M", "MTD\tstudy_variable[1]-description",
            "MTD\tstudy_variable[1]-factors\tnot a parameter"));
        assertEquals(1, ofType(errors, FormatErrorType.ParamList).size(), errors.toString());
    }

    @Test
    void aStudyVariablePropertyBeforeItsNameLineIsKept() throws IOException {
        // the description of study_variable[1] comes before its name line
        String text = withLine("2.0.0-M", "MTD\tmzTab-ID",
            "MTD\tstudy_variable[1]-description\tread before the name");
        String moved = text.lines().
            filter(line -> !line.startsWith("MTD\tstudy_variable[1]-description\tsphingolipid")).
            collect(Collectors.joining("\n", "", "\n"));
        assertTrue(moved.length() < text.length());
        MZTabErrorList errors = parse(moved);
        assertNotNull(parser.getMZTabFile(), errors.toString());
        assertEquals(1, parser.getMZTabFile().getMetadata().getStudyVariable().size());
        assertEquals("read before the name",
            parser.getMZTabFile().getMetadata().getStudyVariable().get(0).getDescription());
    }
}
