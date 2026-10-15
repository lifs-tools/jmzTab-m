/*
 * Copyright 2026 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Tests the format check of ms_run[1-n]-usi_identifier
 * (lifs-tools/jmzTab-m#138, HUPO-PSI/mzTab-M#18).
 */
public class MsRunUsiIdentifierTest {

    private static final String BASE_FILE = "/metabolomics/minimal-m-2.0.mztab";

    @TempDir
    Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {
        "mzspec:PXD000561:Adult_Frontalcortex_bRP_Elite_85_f09",
        "mzspec:PXD000561:Adult_Frontalcortex_bRP_Elite_85_f09.RAW",
        "mzspec:MSV000086206:peak/mzml/S_N1.mzML"})
    public void testValidMsRunIdentifierIsAccepted(String usiIdentifier) throws IOException {
        assertTrue(usiIdentifierWarnings(usiIdentifier).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "PXD000561:Adult_Frontalcortex_bRP_Elite_85_f09",
        "mzspec:PXD000561",
        "https://www.ebi.ac.uk/pride/archive/projects/PXD000561"})
    public void testInvalidMsRunIdentifierIsReportedAsWarning(String usiIdentifier) throws IOException {
        List<MZTabError> warnings = usiIdentifierWarnings(usiIdentifier);
        assertEquals(1, warnings.size(), () -> "Expected one warning, got: " + warnings);
        assertEquals(MZTabErrorType.Level.Warn, warnings.get(0).getType().getLevel());
        assertTrue(warnings.get(0).getMessage().contains(usiIdentifier));
    }

    private List<MZTabError> usiIdentifierWarnings(String usiIdentifier) throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(BASE_FILE)) {
            assertNotNull(is, "Missing test resource " + BASE_FILE);
            for (String line : new String(is.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                lines.add(line);
                if (line.startsWith("MTD\tms_run[1]-location\t")) {
                    lines.add("MTD\tms_run[1]-usi_identifier\t" + usiIdentifier);
                }
            }
        }
        Path file = tempDir.resolve("usi-identifier.mztab");
        Files.write(file, lines, StandardCharsets.UTF_8);

        MzTabFileParser parser = new MzTabFileParser(file.toFile());
        MZTabErrorList errors = parser.parse(new ByteArrayOutputStream(), MZTabErrorType.Level.Info, 1000);
        return errors.getErrorList().stream()
            .filter(e -> e.getType() == LogicalErrorType.MsRunUsiIdentifierInvalid)
            .toList();
    }
}
