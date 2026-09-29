/*
 * Copyright 2018 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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

/**
 * <p>IMZTabColumn interface.</p>
 *
 * @author nilshoffmann
 * @since 2.0
 *
 */
public interface IMZTabColumn {

    /**
     * Get the column data type Class.
     *
     * @return a {@link java.lang.Class} object.
     */
    Class<?> getDataType();

    /**
     * Indexed element (assay, study variable, ms run) this column refers to,
     * or null for columns without element.
     *
     * @return the indexed element or null.
     */
    Object getElement();

    /**
     * Get the column header as it appears in the header line, e.g.
     * {@code abundance_assay[3]} or {@code opt_global_mzmin}.
     *
     * @return the column header.
     */
    String getHeader();

    /**
     * Get the column position within its section. Positions are compared
     * numerically by order, then index, then element id; there is no upper
     * limit on the number of columns.
     *
     * @see ColumnPosition
     * @return the column position.
     */
    ColumnPosition getPosition();

    /**
     * Get the column name. For stable columns, name and header are the same.
     * For indexed columns, the name is the stable part of the header, e.g.
     * {@code abundance_assay} for {@code abundance_assay[3]}.
     *
     * @return the column name.
     */
    String getName();

    /**
     * Get the column order within its section. When parsing, this is the
     * 1-based physical position of the column in the header line.
     *
     * @return the column order.
     */
    int getOrder();

    /**
     * Judge this column belong to stable column or optional column.
     *
     * @return a boolean.
     */
    boolean isOptional();

    /**
     * <p>setHeader.</p>
     *
     * @param header a {@link java.lang.String} object.
     */
    void setHeader(String header);

    /**
     * Reassigns the order, e.g. when a file does not follow the recommended
     * column order. The position is recomputed.
     *
     * @param order the new order.
     */
    void setOrder(int order);

    /**
     * Sets the indexed element used in the header and position of this
     * column. The position is recomputed.
     *
     * @param element SHOULD NOT set null.
     */
    void setElement(Object element);

}
