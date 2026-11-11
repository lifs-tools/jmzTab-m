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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.cvmapping.local.DeclaredVersion.Kind;

public class DeclaredVersionTest {

    private static final List<Release> RELEASES = List.of(
            new Release("4.1.100", Optional.of(LocalDate.of(2022, 6, 1))),
            new Release("4.1.150", Optional.of(LocalDate.of(2023, 10, 1))),
            new Release("4.1.200", Optional.of(LocalDate.of(2025, 1, 15))));

    private static DeclaredVersion of(String declared) {
        return DeclaredVersion.of(declared, VersionScheme.DOTTED, RELEASES);
    }

    @Test
    public void aReleaseNameIsUsedAsIs() {
        DeclaredVersion v = of("4.1.150");
        assertEquals(Kind.RELEASE, v.kind());
        assertEquals(Optional.of("4.1.150"), v.normalised());
    }

    @Test
    public void aDateBeforeTheFirstReleaseIsNotRecognised() {
        DeclaredVersion v = of("20-06-2018");
        assertEquals(Kind.NOT_RECOGNISED, v.kind());
        assertEquals(Optional.empty(), v.normalised());
    }

    @Test
    public void anOboDateHeaderBecomesTheReleaseCurrentThen() {
        DeclaredVersion v = of("16:10:2023 11:38");
        assertEquals(Kind.FROM_DATE, v.kind());
        assertEquals(Optional.of("4.1.150"), v.normalised());
        assertEquals("16:10:2023 11:38", v.original());
    }

    @Test
    public void aDateAfterTheLastReleaseBecomesTheLastRelease() {
        DeclaredVersion v = of("14-06-2024");
        assertEquals(Kind.FROM_DATE, v.kind());
        assertEquals(Optional.of("4.1.150"), v.normalised());
        assertEquals(Optional.of("4.1.200"), of("2025-01-15").normalised());
    }

    @Test
    public void aBareYearIsNotRecognised() {
        assertEquals(Kind.NOT_RECOGNISED, of("2020").kind());
        assertEquals(Kind.NOT_RECOGNISED, of("v4").kind());
    }

    @Test
    public void emptyAndNullAreUndeclared() {
        assertEquals(Kind.NONE, of("").kind());
        assertEquals(Kind.NONE, of("  ").kind());
        assertEquals(Kind.NONE, of(null).kind());
        assertEquals(Optional.empty(), of(null).normalised());
    }

    @Test
    public void aValueOfTheSchemeThatIsNoReleaseStaysARelease() {
        DeclaredVersion v = of("4.1.999");
        assertEquals(Kind.RELEASE, v.kind());
        assertEquals(Optional.of("4.1.999"), v.normalised());
    }

    @Test
    public void anIsoDateUnderTheIsoDateSchemeIsARelease() {
        List<Release> rs = List.of(new Release("2023-05-25", Optional.of(LocalDate.of(2023, 5, 25))));
        DeclaredVersion v = DeclaredVersion.of("2024-01-01", VersionScheme.ISO_DATE, rs);
        assertEquals(Kind.RELEASE, v.kind());
        assertEquals(Optional.of("2024-01-01"), v.normalised());
    }

    @Test
    public void releasesWithoutDatesGiveNoRelease() {
        List<Release> rs = List.of(new Release("4.1.100", Optional.empty()));
        assertEquals(Kind.NOT_RECOGNISED,
                DeclaredVersion.of("14-06-2024", VersionScheme.DOTTED, rs).kind());
    }

    @Test
    public void aDotSeparatedDateIsADateUnlessTheSchemeTakesItAsARelease() {
        List<Release> rs = List.of(new Release("229", Optional.of(LocalDate.of(2025, 1, 1))));
        DeclaredVersion v = DeclaredVersion.of("20.03.2026", VersionScheme.RELEASE_NUMBER, rs);
        assertEquals(Kind.FROM_DATE, v.kind());
        assertEquals(Optional.of("229"), v.normalised());
        // under DOTTED the same text fits the scheme and is taken as a release name
        assertEquals(Kind.RELEASE, of("20.03.2026").kind());
    }
}
