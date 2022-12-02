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

import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.IndexedElement;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.StudyVariable;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This is a static factory class which used to generate a couple of MZTabColumn
 * objects, and organizes them into "logicalPosition, MZTabColumn" pairs.
 * Currently, mzTab table including three kinds of columns:
 * <ol>
 * <li>
 * Stable column with stable order: header name, data type, logical position and
 * order are stable in these columns. All of them are defined in
 * {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn}, {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn},
 * and {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn}.
 * </li>
 * <li>
 * Optional column with stable order: column name, data type and order are
 * defined in the {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn},
 * {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn}, and
 * {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn}. But header
 * name, logical position dynamically depend on {@link IndexedElement}.
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

    private final SortedMap<String, IMZTabColumn> stableColumnMapping = new TreeMap<>();
    private final SortedMap<String, IMZTabColumn> optionalColumnMapping = new TreeMap<>();
    private final SortedMap<String, IMZTabColumn> abundanceColumnMapping = new TreeMap<>();
    private final SortedMap<String, IMZTabColumn> columnMapping = new TreeMap<>();

    private Section section;

    private MZTabColumnFactory() {
    }

    /**
     * Retrieves the MZTabColumnFactory accordingly to the {@link #section}
     *
     * @param section SHOULD be
     * {@link uk.ac.ebi.pride.jmztab2.model.Section#Protein_Header}, {@link uk.ac.ebi.pride.jmztab2.model.Section#Peptide_Header} {@link uk.ac.ebi.pride.jmztab2.model.Section#PSM_Header}
     * or {@link uk.ac.ebi.pride.jmztab2.model.Section#Small_Molecule_Header}.
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
     * Get stable columns mapping. Key is logical position, and value is
     * MZTabColumn object. Stable column with stable order: header name, data
     * type, logical position and order are stable in these columns. All of them
     * have been defined in null     {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn}, {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn},
     * {@link uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn}.
     *
     * @return a {@link java.util.SortedMap} object.
     */
    public SortedMap<String, IMZTabColumn> getStableColumnMapping() {
        return stableColumnMapping;
    }

    /**
     * Get all optional columns, including option column with stable order and
     * name, abundance columns, optional columns and cv param optional columns.
     * Key is logical position, and value is MZTabColumn object.
     *
     * @see AbundanceColumn
     * @see OptionColumn
     * @see ParameterOptionColumn
     * @return a {@link java.util.SortedMap} object.
     */
    public SortedMap<String, IMZTabColumn> getOptionalColumnMapping() {
        return optionalColumnMapping;
    }

    /**
     * Get all columns in the factory. In this class, we maintain the following
     * constraint at any time:
     *
     * @return a {@link java.util.SortedMap} object.
     */
    public SortedMap<String, IMZTabColumn> getColumnMapping() {
        return columnMapping;
    }

    /**
     * Extract the order from logical position. Normally, the order is coming
     * from top two characters of logical position. For example, logical
     * position is 092, then the order number is 9.
     */
    private String getColumnOrder(String position) {
        return position.substring(0, 2);
    }
    
    private void checkOptionalColumn(IMZTabColumn column) throws IllegalArgumentException {
        if(optionalColumnMapping.containsKey(column.getLogicPosition())) {
            throw new IllegalArgumentException("Key " + column.getLogicPosition() + " for column " + column.getName() + " is already assigned to: " + optionalColumnMapping.get(column.getLogicPosition()).getName());
        }
        optionalColumnMapping.put(column.getLogicPosition(), column);
        if(columnMapping.containsKey(column.getLogicPosition())) {
            throw new IllegalArgumentException("Key " + column.getLogicPosition() + " for column " + column.getName() + " is already assigned to: " + columnMapping.get(column.getLogicPosition()).getName());
        }
        columnMapping.put(column.getLogicPosition(), column);
    }
    
    private void checkAbundanceOptionalColumn(IMZTabColumn column) throws IllegalArgumentException {
        if(abundanceColumnMapping.containsKey(column.getLogicPosition())) {
            throw new IllegalArgumentException("Key " + column.getLogicPosition() + " for column " + column.getName() + " is already assigned to: " + abundanceColumnMapping.get(column.getLogicPosition()).getName());
        }
        abundanceColumnMapping.put(column.getLogicPosition(), column);
    }

    private String addOptionColumn(IMZTabColumn column) {

        checkOptionalColumn(column);

        return column.getLogicPosition();
    }

    private String addOptionColumn(IMZTabColumn column, String order) {

        column.setOrder(order);
        checkOptionalColumn(column);

        return column.getLogicPosition();
    }

    /**
     * Add global {@link uk.ac.ebi.pride.jmztab2.model.OptionColumn} into
     * {@link #optionalColumnMapping} and {@link #columnMapping}. The header
     * like: opt_global_{name}
     *
     * @param name SHOULD NOT be empty.
     * @param columnType SHOULD NOT be empty.
     * @return the column's logic position.
     */
    public String addOptionalColumn(String name, Class columnType) {
        IMZTabColumn column = new OptionColumn(null, name, columnType,
            Integer.parseInt(getColumnOrder(columnMapping.lastKey())));
        return addOptionColumn(column);
    }

    /**
     * Add {@link uk.ac.ebi.pride.jmztab2.model.OptionColumn} followed by an
     * indexed element (study variable, assay, ms run) into
     * {@link #optionalColumnMapping} and {@link #columnMapping}. The header
     * will look like: opt_study_variable[1]_{name} for a study variable
     *
     * @param <T> the type of the columnEntity.
     * @param columnEntity SHOULD NOT be empty.
     * @param name SHOULD NOT be empty.
     * @param columnType SHOULD NOT be empty.
     * @return the column's logic position.
     */
    public <T extends Object> String addOptionalColumn(T columnEntity,
        String name, Class columnType) {
        IMZTabColumn column = new OptionColumn(columnEntity, name, columnType,
            Integer.parseInt(getColumnOrder(columnMapping.lastKey())));
        return addOptionColumn(column);
    }

    /**
     * Add global {@link uk.ac.ebi.pride.jmztab2.model.ParameterOptionColumn}
     * into {@link #optionalColumnMapping} and {@link #columnMapping}. The
     * header like: opt_global_cv_{accession}_{parameter name}
     *
     * @param param SHOULD NOT empty.
     * @param columnType SHOULD NOT empty.
     * @return the column's logic position.
     */
    public String addOptionalColumn(Parameter param, Class columnType) {
        IMZTabColumn column = new ParameterOptionColumn(null, param, columnType,
            Integer.parseInt(getColumnOrder(columnMapping.lastKey())));
        return addOptionColumn(column);
    }

    /**
     * Add {@link uk.ac.ebi.pride.jmztab2.model.ParameterOptionColumn} followed
     * by an indexed element (study variable, assay, ms run) into
     * {@link #optionalColumnMapping} and {@link #columnMapping}. The header
     * will look like: opt_assay[1]_cv_{accession}_{parameter name} for an
     * assay.
     *
     * @param <T> the type of the columnEntity.
     * @param columnEntity SHOULD NOT empty.
     * @param param SHOULD NOT empty.
     * @param columnType SHOULD NOT empty.
     * @return the column's logic position.
     */
    public <T extends Object> String addOptionalColumn(T columnEntity,
        Parameter param, Class columnType) {
        IMZTabColumn column = new ParameterOptionColumn(columnEntity, param,
            columnType, Integer.parseInt(getColumnOrder(columnMapping.lastKey())));
        return addOptionColumn(column);
    }

    /**
     * <p>
     * addAbundanceOptionalColumn.</p>
     *
     * @param assay a {@link org.lifstools.mztab2.model.Assay} object.
     * @param order the order string for this column.
     * @return the column's logic position.
     */
    public String addAbundanceOptionalColumn(Assay assay, String order) {
        IMZTabColumn column = AbundanceColumn.createOptionalColumn(section,
            assay, Integer.parseInt(order));
        checkAbundanceOptionalColumn(column);
        return addOptionColumn(column, order);
    }

    /**
     * Add an {@link uk.ac.ebi.pride.jmztab2.model.AbundanceColumn} into
     * {@link uk.ac.ebi.pride.jmztab2.model.AbundanceColumn}, {@link #optionalColumnMapping}
     * and {@link #columnMapping}. The header can be one of
     * abundance_study_variable[1], abundance_coeffvar_study_variable[1].
     *
     * @see
     * AbundanceColumn#createOptionalColumns(uk.ac.ebi.pride.jmztab2.model.Section,
     * org.lifstools.mztab2.model.StudyVariable, java.lang.String, java.lang.String)
     * @param studyVariable SHOULD NOT empty.
     * @param columnHeader the column header without the 'abundance_' prefix.
     * @param order the order string for this column.
     * @return the column's logic position.
     */
    public String addAbundanceOptionalColumn(StudyVariable studyVariable,
        String columnHeader, String order) {
        SortedMap<String, MZTabColumn> columns = AbundanceColumn.
            createOptionalColumns(section, studyVariable, columnHeader, order);
        for(IMZTabColumn col:columns.values()) {
            checkAbundanceOptionalColumn(col);
            checkOptionalColumn(col);
        }
        return columns.lastKey();
    }

    /**
     * <p>
     * addIdConfidenceMeasureColumn.</p>
     *
     * @param parameter a {@link org.lifstools.mztab2.model.Parameter} object.
     * @param index a {@link java.lang.Integer} object.
     * @param columnType the class of values in this column.
     * @return the column's logic position.
     */
    public String addIdConfidenceMeasureColumn(Parameter parameter,
        Integer index, Class columnType) {
        if (section != Section.Small_Molecule_Evidence_Header && section != Section.Small_Molecule_Evidence) {
            throw new IllegalArgumentException(
                "Section should be SmallMoleculeEvidence, but is " + section.
                    getName());
        }
        if (parameter == null) {
            throw new NullPointerException("Parameter should not be null!");
        }

        SortedMap<String, MZTabColumn> columns = new TreeMap<>();

        MZTabColumn column = new MZTabColumn("id_confidence_measure", columnType,
            false, Integer.parseInt(getColumnOrder(columnMapping.lastKey())) + "",
            index);

        columns.put(column.getLogicPosition(), column);
        for(IMZTabColumn col : columns.values()) {
            checkOptionalColumn(col);
        }
        return columns.lastKey();
    }

    /**
     * The offset record the position of MZTabColumn in header line. For
     * example, protein header line, the relationships between Logical Position,
     * MZTabColumn, offset and order are like following structure: Logical
     * Position MZTabColumn offset order "01" accession 1 01 "02" description 2
     * 02 ...... "08" best_search_engine_score 8 08 "091"
     * search_engine_score_ms_run[1] 9 09 "092" search_engine_score_ms_run[2] 10
     * 09 "10" reliability 11 10 "111" num_psms_ms_run[1] 12 11 "112"
     * num_psms_ms_run[2] 13 11
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
     * Query the MZTabColumn in factory, based on column header with
     * case-insensitive. Notice: for optional columns, header name maybe
     * flexible. For example, num_psms_ms_run[1]. At this time, user SHOULD BE
     * provide the full header name to query MZTabColumn. If just provide
     * num_psms_ms_run, return null.
     *
     * @param header the column header to use as the search key.
     * @return a {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn} object or
     * null.
     */
    public IMZTabColumn findColumnByHeader(String header) {
        header = header.trim();

        for (IMZTabColumn column : columnMapping.values()) {
            if (header.equalsIgnoreCase(column.getHeader())) {
                return column;
            }
        }

        return null;
    }
}
