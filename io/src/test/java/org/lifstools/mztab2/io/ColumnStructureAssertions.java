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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.lifstools.mztab2.model.MzTab;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Parses an mzTab file and fails on any error that indicates a broken
 * header-to-column mapping. Row-level logical errors unrelated to columns
 * are tolerated, since the regression fixtures only need to exercise
 * column positions.
 */
public final class ColumnStructureAssertions {

    private static final Set<MZTabErrorType> COLUMN_STRUCTURE_ERRORS = Set.of(
        LogicalErrorType.HeaderLine,
        LogicalErrorType.HeaderNotValid,
        LogicalErrorType.ColumnNotValid,
        LogicalErrorType.NotDefineInHeader,
        FormatErrorType.CountMatch);

    private ColumnStructureAssertions() {
    }

    public static MzTab parseWithoutColumnErrors(Path file) throws IOException {
        MzTabFileParser parser = new MzTabFileParser(file.toFile());
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        MZTabErrorList errors = parser.parse(log, MZTabErrorType.Level.Error, 100_000);
        List<MZTabError> structural = errors.getErrorList().stream()
            .filter(e -> COLUMN_STRUCTURE_ERRORS.contains(e.getType()))
            .toList();
        assertTrue(structural.isEmpty(), () -> "Column structure errors: " + structural);
        MzTab mzTab = parser.getMZTabFile();
        assertNotNull(mzTab, () -> "Parsing aborted:\n" + log.toString(StandardCharsets.UTF_8));
        return mzTab;
    }
}
