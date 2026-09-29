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
package uk.ac.ebi.pride.jmztab2.model;

import java.util.List;
import java.util.TreeMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ColumnPositionTest {

    @Test
    public void comparesOrderNumerically() {
        assertTrue(ColumnPosition.of(100).compareTo(ColumnPosition.of(99)) > 0);
        assertTrue(ColumnPosition.of(1000).compareTo(ColumnPosition.of(999)) > 0);
        assertTrue(ColumnPosition.of(9).compareTo(ColumnPosition.of(10)) < 0);
    }

    @Test
    public void comparesIndexThenElementIdWithinSameOrder() {
        assertTrue(ColumnPosition.of(16, 2, 0).compareTo(ColumnPosition.of(16, 1, 0)) > 0);
        assertTrue(ColumnPosition.of(5, 0, 10).compareTo(ColumnPosition.of(5, 0, 9)) > 0);
        assertTrue(ColumnPosition.of(6, 0, 0).compareTo(ColumnPosition.of(5, 99, 99)) > 0);
    }

    @Test
    public void sortsNumericallyAsTreeMapKey() {
        TreeMap<ColumnPosition, String> map = new TreeMap<>();
        for (int order : new int[]{1000, 100, 99, 10, 9}) {
            map.put(ColumnPosition.of(order), "c" + order);
        }
        assertEquals(List.of("c9", "c10", "c99", "c100", "c1000"), List.copyOf(map.values()));
    }

    @Test
    public void equalPositionsAreEqual() {
        assertEquals(0, ColumnPosition.of(3, 1, 2).compareTo(ColumnPosition.of(3, 1, 2)));
        assertEquals(ColumnPosition.of(3, 1, 2), ColumnPosition.of(3, 1, 2));
        assertEquals(ColumnPosition.of(3, 1, 2).hashCode(), ColumnPosition.of(3, 1, 2).hashCode());
        assertEquals(ColumnPosition.of(7, 0, 0), ColumnPosition.of(7));
    }

    @Test
    public void rejectsNegativeComponents() {
        assertThrows(IllegalArgumentException.class, () -> ColumnPosition.of(-1));
        assertThrows(IllegalArgumentException.class, () -> ColumnPosition.of(1, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> ColumnPosition.of(1, 0, -1));
    }

    @Test
    public void rendersAsDottedTriple() {
        assertEquals("12.0.3", ColumnPosition.of(12, 0, 3).toString());
    }
}
