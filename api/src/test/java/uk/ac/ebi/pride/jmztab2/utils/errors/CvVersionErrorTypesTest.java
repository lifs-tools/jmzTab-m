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
package uk.ac.ebi.pride.jmztab2.utils.errors;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType.Level;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The cross-check types that report vocabulary versions that cannot be
 * resolved, are substituted or are declared oddly.
 */
class CvVersionErrorTypesTest {

    private record Row(MZTabErrorType type, int code, Level level, String... args) {
    }

    @Test
    void codesLevelsAndArguments() {
        List<Row> rows = List.of(
            new Row(CrossCheckErrorType.CvVersionUnavailable, 3017, Level.Error, "MS", "4.1.99", "the release does not exist"),
            new Row(CrossCheckErrorType.CvVersionSubstituted, 3018, Level.Warn, "MS", "4.1.99", "the checksum differs", "4.1.100", "the bundled copy"),
            new Row(CrossCheckErrorType.CvVersionOlderThanMapping, 3019, Level.Warn, "UO", "2020-01-01", "2023-05-01"),
            new Row(CrossCheckErrorType.CvVersionsNotComparable, 3020, Level.Warn, "UO", "vX", "2023-05-01"),
            new Row(CrossCheckErrorType.CvVersionNotRecognised, 3021, Level.Warn, "UO", "latest-ish"),
            new Row(CrossCheckErrorType.CvVersionNotDeclared, 3022, Level.Info, "UO", "2023-05-01"),
            new Row(CrossCheckErrorType.CvDeclaredTwice, 3023, Level.Warn, "UO", "2023-05-01", "2020-01-01"));
        for (Row row : rows) {
            assertEquals(row.code(), row.type().getCode());
            assertEquals(row.level(), row.type().getLevel(), "level of " + row.code());
            String message = new MZTabError(row.type(), -1, row.args()).getMessage();
            for (String arg : row.args()) {
                assertTrue(message.contains(arg), "message of " + row.code() + " lacks '" + arg + "': " + message);
            }
        }
    }
}
