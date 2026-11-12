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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class VersionSchemeTest {

    @Test
    public void dottedVersionsAreNotOrderedAsStrings() {
        assertTrue("4.1.9".compareTo("4.1.10") > 0, "as strings 4.1.9 is greater");
        assertTrue(VersionScheme.DOTTED.compare("4.1.9", "4.1.10").get() < 0);
        assertTrue(VersionScheme.DOTTED.compare("4.1.10", "4.1.9").get() > 0);
    }

    @Test
    public void dottedVersionsCompareNumericallyPerComponent() {
        assertTrue(VersionScheme.DOTTED.compare("4.1.30", "4.1.200").get() < 0);
        assertTrue(VersionScheme.DOTTED.compare("4.1.200", "4.1.30").get() > 0);
    }

    @Test
    public void missingTrailingDottedComponentsCountAsZero() {
        assertFalse(VersionScheme.DOTTED.compare("4.1", "4.1.0").get() < 0);
        assertEquals(Optional.of(0), VersionScheme.DOTTED.compare("4.1", "4.1.0"));
    }

    @Test
    public void isoDatesCompareChronologically() {
        assertTrue(VersionScheme.ISO_DATE.compare("2023-05-25", "2022-12-01").get() > 0);
    }

    @Test
    public void releaseNumbersCompareNumerically() {
        assertTrue(VersionScheme.RELEASE_NUMBER.compare("230", "99").get() > 0);
    }

    @Test
    public void valuesOfAnotherSchemeDoNotCompare() {
        assertEquals(Optional.empty(), VersionScheme.DOTTED.compare("4.1.30", "2023-05-25"));
    }

    @Test
    public void acceptsOnlyValuesOfItsOwnForm() {
        assertTrue(VersionScheme.DOTTED.accepts("4.1"));
        assertFalse(VersionScheme.DOTTED.accepts("2020"));
        assertFalse(VersionScheme.DOTTED.accepts("4.1."));
        assertFalse(VersionScheme.DOTTED.accepts(null));
        assertTrue(VersionScheme.RELEASE_NUMBER.accepts("230"));
        assertFalse(VersionScheme.RELEASE_NUMBER.accepts("2.3"));
        assertTrue(VersionScheme.ISO_DATE.accepts("2023-05-25"));
        assertFalse(VersionScheme.ISO_DATE.accepts("2023-02-30"));
    }
}
