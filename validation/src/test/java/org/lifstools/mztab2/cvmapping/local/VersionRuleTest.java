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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.cvmapping.local.VersionChoice.Reason;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * The rows of design §4.4's table, and versions that cannot be compared.
 */
public class VersionRuleTest {

    private static final List<Release> RELEASES = List.of(
        new Release("4.1.100", Optional.of(LocalDate.parse("2023-01-10"))),
        new Release("4.1.150", Optional.of(LocalDate.parse("2024-03-01"))),
        new Release("4.1.200", Optional.of(LocalDate.parse("2025-01-15"))));

    private static VersionChoice choose(String declared, String mapping) {
        return VersionRule.choose("MS", DeclaredVersion.of(declared, VersionScheme.DOTTED, RELEASES),
            Optional.ofNullable(mapping), VersionScheme.DOTTED);
    }

    @Test
    public void aFileVersionTheSameAsOrNewerThanTheMappingsIsUsedAsIs() {
        assertChoice(choose("4.1.150", "4.1.150"), "4.1.150", Reason.FILE);
        assertChoice(choose("4.1.200", "4.1.150"), "4.1.200", Reason.FILE);
        // a date is the release current on that date
        assertChoice(choose("14-06-2024", "4.1.100"), "4.1.150", Reason.FILE_DATE);
    }

    @Test
    public void aFileVersionOlderThanTheMappingsUsesTheMappingsWithAWarning() {
        VersionChoice c = choose("4.1.100", "4.1.150");

        assertEquals("4.1.150", c.version());
        assertEquals(Reason.MAPPING, c.reason());
        assertEquals(1, c.messages().size());
        MZTabError warning = c.messages().get(0);
        assertEquals(CrossCheckErrorType.CvVersionOlderThanMapping, warning.getType());
        assertTrue(warning.getMessage().contains("\"MS\" is declared in version \"4.1.100\", which is older than"
            + " version \"4.1.150\""), warning.getMessage());
    }

    @Test
    public void noFileVersionUsesTheMappingsSilently() {
        assertChoice(choose(null, "4.1.150"), "4.1.150", Reason.MAPPING);
        assertChoice(choose("  ", "4.1.150"), "4.1.150", Reason.MAPPING);
    }

    @Test
    public void anUnrecognisedFileVersionUsesTheMappingsWithAWarning() {
        VersionChoice c = choose("2020", "4.1.150");

        assertEquals("4.1.150", c.version());
        assertEquals(Reason.MAPPING, c.reason());
        assertEquals(1, c.messages().size());
        MZTabError warning = c.messages().get(0);
        assertEquals(CrossCheckErrorType.CvVersionNotRecognised, warning.getType());
        assertTrue(warning.getMessage().contains("\"MS\" is declared with the version \"2020\""),
            warning.getMessage());
    }

    @Test
    public void aFileVersionWithoutAMappingVersionIsUsedAsIs() {
        assertChoice(choose("4.1.100", null), "4.1.100", Reason.FILE);
        assertChoice(choose("16:10:2023 11:38", null), "4.1.100", Reason.FILE_DATE);
    }

    @Test
    public void neitherVersionIsTheLatestAndItsMessageWaitsForTheConcreteVersion() {
        VersionChoice c = choose("", null);

        assertNull(c.version());
        assertEquals(Reason.LATEST, c.reason());
        // the resolver adds Info-3022 once the latest is a concrete version
        assertEquals(List.of(), c.messages());
        // a value that names nothing is undeclared, with its warning
        VersionChoice unrecognised = choose("v-final", null);
        assertNull(unrecognised.version());
        assertEquals(Reason.LATEST, unrecognised.reason());
        assertEquals(List.of(CrossCheckErrorType.CvVersionNotRecognised),
            unrecognised.messages().stream().map(MZTabError::getType).toList());
    }

    @Test
    public void versionsThatCannotBeComparedUseTheFilesWithAWarning() {
        VersionChoice c = choose("4.1.200", "2024-01-01");

        assertEquals("4.1.200", c.version());
        assertEquals(Reason.FILE, c.reason());
        assertEquals(1, c.messages().size());
        MZTabError warning = c.messages().get(0);
        assertEquals(CrossCheckErrorType.CvVersionsNotComparable, warning.getType());
        assertTrue(warning.getMessage().contains("\"MS\" is declared in version \"4.1.200\", which cannot be"
            + " compared with version \"2024-01-01\""), warning.getMessage());
    }

    private static void assertChoice(VersionChoice c, String version, Reason reason) {
        assertEquals(version, c.version());
        assertEquals(reason, c.reason());
        assertEquals(List.of(), c.messages());
    }
}
