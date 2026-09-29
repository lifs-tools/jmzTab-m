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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.StudyVariable;

/**
 * This is a static factory class which used to generate a couple of MZTabColumn
 * objects, and organizes them into "position, MZTabColumn" pairs.
 * Currently, mzTab table including three kinds of columns:
 * <ol>
 * <li>
 * Stable column with stable order: header name, data type, position and
 * order are stable in these columns. All of them are defined in
 * {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn}, {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn},
 * and {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn}.
 * </li>
 * <li>
 * Optional column with stable order: column name, data type and order are
 * defined in the {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn},
 * {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn}, and
 * {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn}. But header
 * name, position dynamically depend on {@link IndexedElement}.
 * </li>
 * <li>
 * Optional columns which are placed at the end of a table-based section. There
 * are three types of optional column:
 * {@link uk.ac.ebi.pride.jmztab2.model.AbundanceColumn}, {@link uk.ac.ebi.pride.jmztab2.model.OptionColumn}
 * and {@link uk.ac.ebi.pride.jmztab2.model.ParameterOptionColumn}, which always
 * are added at the end of the table. These optional columns have no stable
 * column name, data type or order. In this factory, we use
 * {@link #addOptionalColumn(String, Class)} to create
 * {@link uk.ac.ebi.pride.jmztab2.model.OptionColumn}; and
 * {@link #addOptionalColumn(org.lifstools.mztab2.model.IndexedElement, java.lang.String, java.lang.Class)}
 * or {@link #addOptionalColumn(IndexedElement, Parameter, Class)} to create
 * {@link uk.ac.ebi.pride.jmztab2.model.ParameterOptionColumn}.
 * </li>
 * </ol>
 *
 * @author qingwei
 * @author nilshoffmann
 * @since 23/05/13
 *
 */
public class MZTabColumnFactory {

    private final SortedMap<ColumnPosition, IMZTabColumn> stableColumnMapping = new TreeMap<>();
    private final SortedMap<ColumnPosition, IMZTabColumn> optionalColumnMapping = new TreeMap<>();
    private final SortedMap<ColumnPosition, IMZTabColumn> abundanceColumnMapping = new TreeMap<>();
    private final SortedMap<ColumnPosition, IMZTabColumn> columnMapping = new TreeMap<>();
    private final Map<String, IMZTabColumn> headerIndex = new HashMap<>();

    private Section section;

    private MZTabColumnFactory() {
    }

    /**
     * Retrieves the MZTabColumnFactory accordingly to the {@link #section}
     *
     * @param section SHOULD be
     * {@link uk.ac.ebi.pride.jmztab2.model.Section#Small_Molecule_Header},
     * {@link uk.ac.ebi.pride.jmztab2.model.Section#Small_Molecule_Feature_Header}
     * or
     * {@link uk.ac.ebi.pride.jmztab2.model.Section#Small_Molecule_Evidence_Header}.
     * @return a {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory}
     * object.
     */
    public static MZTabColumnFactory getInstance(Section section) {
        section = Section.toHeaderSection(section);

        if (section == null) {
            throw new IllegalArgumentException(
                "Section should use Protein_Header, Peptide_Header, PSM_Header, Small_Molecule_Header, Small_Molecule_Feature_Header, or Small_Molecule_Evidence_Header.");
        }

        MZTabColumnFactory factory = new MZTabColumnFactory();
        factory.section = section;

        return factory;
    }

    /**
     * Stable (non-optional) columns, keyed by position.
     *
     * The returned view is unmodifiable. Registered columns themselves must
     * not be mutated (e.g. via {@code setOrder}, {@code setElement} or
     * {@code setHeader}), since that would desynchronize the column's
     * position from this map's keys and from the header index used for
     * lookups.
     *
     * @return an unmodifiable view.
     */
    public SortedMap<ColumnPosition, IMZTabColumn> getStableColumnMapping() {
        return Collections.unmodifiableSortedMap(stableColumnMapping);
    }

    /**
     * Optional columns (abundance, opt_, cv opt_, id_confidence_measure and
     * stable columns flagged optional), keyed by position.
     *
     * The returned view is unmodifiable. Registered columns themselves must
     * not be mutated (e.g. via {@code setOrder}, {@code setElement} or
     * {@code setHeader}), since that would desynchronize the column's
     * position from this map's keys and from the header index used for
     * lookups.
     *
     * @return an unmodifiable view.
     */
    public SortedMap<ColumnPosition, IMZTabColumn> getOptionalColumnMapping() {
        return Collections.unmodifiableSortedMap(optionalColumnMapping);
    }

    /**
     * Abundance columns, keyed by position.
     *
     * The returned view is unmodifiable. Registered columns themselves must
     * not be mutated (e.g. via {@code setOrder}, {@code setElement} or
     * {@code setHeader}), since that would desynchronize the column's
     * position from this map's keys and from the header index used for
     * lookups.
     *
     * @return an unmodifiable view.
     */
    public SortedMap<ColumnPosition, IMZTabColumn> getAbundanceColumnMapping() {
        return Collections.unmodifiableSortedMap(abundanceColumnMapping);
    }

    /**
     * All columns, keyed by position.
     *
     * The returned view is unmodifiable. Registered columns themselves must
     * not be mutated (e.g. via {@code setOrder}, {@code setElement} or
     * {@code setHeader}), since that would desynchronize the column's
     * position from this map's keys and from the header index used for
     * lookups.
     *
     * @return an unmodifiable view.
     */
    public SortedMap<ColumnPosition, IMZTabColumn> getColumnMapping() {
        return Collections.unmodifiableSortedMap(columnMapping);
    }

    /**
     * The order following the last registered column, or 1 if empty.
     *
     * @return the next free order.
     */
    public int nextOrder() {
        return columnMapping.isEmpty() ? 1 : columnMapping.lastKey().order() + 1;
    }

    private ColumnPosition register(IMZTabColumn column) {
        ColumnPosition position = column.getPosition();
        IMZTabColumn other = columnMapping.get(position);
        if (other != null) {
            throw new IllegalArgumentException("Position " + position + " for column '" + column.getHeader() + "' is already assigned to '" + other.getHeader() + "'");
        }
        String headerKey = column.getHeader().trim().toLowerCase(Locale.ROOT);
        if (headerIndex.containsKey(headerKey)) {
            throw new IllegalArgumentException("Column header '" + column.getHeader() + "' is already defined");
        }
        if (column.isOptional()) {
            optionalColumnMapping.put(position, column);
        } else {
            stableColumnMapping.put(position, column);
        }
        if (column instanceof AbundanceColumn) {
            abundanceColumnMapping.put(position, column);
        }
        columnMapping.put(position, column);
        headerIndex.put(headerKey, column);
        return position;
    }

    /**
     * Register a stable column at the given order.
     *
     * NOTICE: the column's order is set to {@code order} before it is
     * registered. If registration then fails with
     * {@link IllegalArgumentException}, the passed-in {@code column} keeps
     * that new order regardless.
     *
     * @param column a stable column instance, e.g. from
     * {@link SmallMoleculeColumn.Stable#columnFor(String)}.
     * @param order the column's order within its section.
     * @return the column's position.
     * @throws IllegalArgumentException if the position or header is taken.
     */
    public ColumnPosition addStableColumn(IMZTabColumn column, int order) {
        column.setOrder(order);
        return register(column);
    }

    /**
     * Add a global {@link OptionColumn} (opt_global_{name}) at
     * {@link #nextOrder()}.
     *
     * @param name SHOULD NOT be empty.
     * @param columnType SHOULD NOT be empty.
     * @return the column's position.
     */
    public ColumnPosition addOptionalColumn(String name, Class columnType) {
        return addOptionalColumn(name, columnType, nextOrder());
    }

    /**
     * Add a global {@link OptionColumn} (opt_global_{name}).
     *
     * @param name SHOULD NOT be empty.
     * @param columnType SHOULD NOT be empty.
     * @param order the column's order within its section.
     * @return the column's position.
     */
    public ColumnPosition addOptionalColumn(String name, Class columnType, int order) {
        return register(new OptionColumn(null, name, columnType, order));
    }

    /**
     * Add an {@link OptionColumn} for an indexed element, e.g.
     * opt_assay[1]_{name}, at {@link #nextOrder()}.
     *
     * @param <T> the type of the columnEntity.
     * @param columnEntity SHOULD NOT be empty.
     * @param name SHOULD NOT be empty.
     * @param columnType SHOULD NOT be empty.
     * @return the column's position.
     */
    public <T extends Object> ColumnPosition addOptionalColumn(T columnEntity, String name, Class columnType) {
        return addOptionalColumn(columnEntity, name, columnType, nextOrder());
    }

    /**
     * Add an {@link OptionColumn} for an indexed element, e.g.
     * opt_assay[1]_{name}.
     *
     * @param <T> the type of the columnEntity.
     * @param columnEntity SHOULD NOT be empty.
     * @param name SHOULD NOT be empty.
     * @param columnType SHOULD NOT be empty.
     * @param order the column's order within its section.
     * @return the column's position.
     */
    public <T extends Object> ColumnPosition addOptionalColumn(T columnEntity, String name, Class columnType, int order) {
        return register(new OptionColumn(columnEntity, name, columnType, order));
    }

    /**
     * Add a global {@link ParameterOptionColumn}
     * (opt_global_cv_{accession}_{name}) at {@link #nextOrder()}.
     *
     * @param param SHOULD NOT empty.
     * @param columnType SHOULD NOT empty.
     * @return the column's position.
     */
    public ColumnPosition addOptionalColumn(Parameter param, Class columnType) {
        return addOptionalColumn(param, columnType, nextOrder());
    }

    /**
     * Add a global {@link ParameterOptionColumn}
     * (opt_global_cv_{accession}_{name}).
     *
     * @param param SHOULD NOT empty.
     * @param columnType SHOULD NOT empty.
     * @param order the column's order within its section.
     * @return the column's position.
     */
    public ColumnPosition addOptionalColumn(Parameter param, Class columnType, int order) {
        return register(new ParameterOptionColumn(null, param, columnType, order));
    }

    /**
     * Add a {@link ParameterOptionColumn} for an indexed element, e.g.
     * opt_assay[1]_cv_{accession}_{name}, at {@link #nextOrder()}.
     *
     * @param <T> the type of the columnEntity.
     * @param columnEntity SHOULD NOT empty.
     * @param param SHOULD NOT empty.
     * @param columnType SHOULD NOT empty.
     * @return the column's position.
     */
    public <T extends Object> ColumnPosition addOptionalColumn(T columnEntity, Parameter param, Class columnType) {
        return addOptionalColumn(columnEntity, param, columnType, nextOrder());
    }

    /**
     * Add a {@link ParameterOptionColumn} for an indexed element, e.g.
     * opt_assay[1]_cv_{accession}_{name}.
     *
     * @param <T> the type of the columnEntity.
     * @param columnEntity SHOULD NOT empty.
     * @param param SHOULD NOT empty.
     * @param columnType SHOULD NOT empty.
     * @param order the column's order within its section.
     * @return the column's position.
     */
    public <T extends Object> ColumnPosition addOptionalColumn(T columnEntity, Parameter param, Class columnType, int order) {
        return register(new ParameterOptionColumn(columnEntity, param, columnType, order));
    }

    /**
     * Add an abundance_assay[id] column.
     *
     * @param assay a {@link org.lifstools.mztab2.model.Assay} object.
     * @param order the column's order within its section.
     * @return the column's position.
     */
    public ColumnPosition addAbundanceOptionalColumn(Assay assay, int order) {
        return register(AbundanceColumn.createOptionalColumn(section, assay, order));
    }

    /**
     * Add an abundance_study_variable[id] or
     * abundance_variation_study_variable[id] column.
     *
     * @param studyVariable SHOULD NOT empty.
     * @param columnHeader the column header without the 'abundance_' prefix.
     * @param order the column's order within its section.
     * @return the column's position.
     */
    public ColumnPosition addAbundanceOptionalColumn(StudyVariable studyVariable, String columnHeader, int order) {
        return register(AbundanceColumn.createOptionalColumn(section, studyVariable, columnHeader, order));
    }

    /**
     * Add an id_confidence_measure[index] column at {@link #nextOrder()}.
     *
     * @param parameter a {@link org.lifstools.mztab2.model.Parameter} object.
     * @param index a {@link java.lang.Integer} object.
     * @param columnType the class of values in this column.
     * @return the column's position.
     */
    public ColumnPosition addIdConfidenceMeasureColumn(Parameter parameter, Integer index, Class columnType) {
        return addIdConfidenceMeasureColumn(parameter, index, columnType, nextOrder());
    }

    /**
     * Add an id_confidence_measure[index] column.
     *
     * @param parameter a {@link org.lifstools.mztab2.model.Parameter} object.
     * @param index a {@link java.lang.Integer} object.
     * @param columnType the class of values in this column.
     * @param order the column's order within its section.
     * @return the column's position.
     */
    public ColumnPosition addIdConfidenceMeasureColumn(Parameter parameter, Integer index, Class columnType, int order) {
        if (section != Section.Small_Molecule_Evidence_Header && section != Section.Small_Molecule_Evidence) {
            throw new IllegalArgumentException(
                "Section should be SmallMoleculeEvidence, but is " + section.getName());
        }
        if (parameter == null) {
            throw new NullPointerException("Parameter should not be null!");
        }
        return register(new MZTabColumn("id_confidence_measure", columnType, true, order, index));
    }

    /**
     * Numbers all columns 1..n in position order.
     *
     * @return a {@link java.util.SortedMap} object with the offsets for each
     * column.
     */
    public SortedMap<Integer, IMZTabColumn> getOffsetColumnsMap() {
        SortedMap<Integer, IMZTabColumn> map = new TreeMap<>();

        int offset = 1;
        for (IMZTabColumn column : columnMapping.values()) {
            map.put(offset++, column);
        }

        return map;
    }

    /**
     * Query the column by its full header, case-insensitive, e.g.
     * {@code abundance_assay[1]}.
     *
     * @param header the column header to use as the search key.
     * @return a {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn} object or
     * null.
     */
    public IMZTabColumn findColumnByHeader(String header) {
        return headerIndex.get(header.trim().toLowerCase(Locale.ROOT));
    }
}
