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

import java.util.Comparator;

/**
 * Position of a column within a table section. Positions are ordered by
 * {@link #order()}, then {@link #index()}, then {@link #elementId()}, all
 * compared numerically, so there is no upper limit on the number of columns.
 *
 * @param order the column's order within its section, e.g. its 1-based
 * position in the header line.
 * @param index the {@code [n]} index of indexed columns such as
 * {@code id_confidence_measure[n]}, 0 if not applicable.
 * @param elementId the id of the assay or study variable the column refers
 * to, 0 if not applicable.
 * @author nilshoffmann
 * @since 2.1
 */
public record ColumnPosition(int order, int index, int elementId) implements Comparable<ColumnPosition> {

    private static final Comparator<ColumnPosition> ORDERING = Comparator
        .comparingInt(ColumnPosition::order)
        .thenComparingInt(ColumnPosition::index)
        .thenComparingInt(ColumnPosition::elementId);

    /**
     * Creates a new column position.
     *
     * @throws IllegalArgumentException if any component is negative.
     */
    public ColumnPosition {
        if (order < 0 || index < 0 || elementId < 0) {
            throw new IllegalArgumentException(
                "Column position components must not be negative: " + order + "." + index + "." + elementId);
        }
    }

    /**
     * Creates a position without index and element id.
     *
     * @param order the column's order within its section.
     * @return the column position.
     */
    public static ColumnPosition of(int order) {
        return new ColumnPosition(order, 0, 0);
    }

    /**
     * Creates a position.
     *
     * @param order the column's order within its section.
     * @param index the column's index, 0 if not applicable.
     * @param elementId the referenced element's id, 0 if not applicable.
     * @return the column position.
     */
    public static ColumnPosition of(int order, int index, int elementId) {
        return new ColumnPosition(order, index, elementId);
    }

    @Override
    public int compareTo(ColumnPosition other) {
        return ORDERING.compare(this, other);
    }

    @Override
    public String toString() {
        return order + "." + index + "." + elementId;
    }
}
