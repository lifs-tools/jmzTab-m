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

import de.isas.mztab2.io.serialization.Serializers;
import de.isas.mztab2.model.IndexedElementAdapter;

/**
 * Define a column header which used in {@link uk.ac.ebi.pride.jmztab2.model.Section#Protein_Header}, {@link uk.ac.ebi.pride.jmztab2.model.Section#Peptide_Header},
 * {@link uk.ac.ebi.pride.jmztab2.model.Section#PSM_Header}, or {@link uk.ac.ebi.pride.jmztab2.model.Section#Small_Molecule_Header}. There are two kinds of columns: stable column
 * and optional column. Stable column has stable position and header name, while optional column not.
 * {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory} used to create and maintain these column objects.
 *
 * @see MZTabColumnFactory
 * @see SmallMoleculeColumn
 * @see SmallMoleculeFeatureColumn
 * @see SmallMoleculeEvidenceColumn
 * @see OptionColumn
 * @see ParameterOptionColumn
 * @see AbundanceColumn
 * @author qingwei
 * @author nilshoffmann
 * @since 23/05/13
 * 
 */
public class MZTabColumn implements IMZTabColumn {
    private final String name;
    private String order;
    private Integer id;
    private String header;
    private String logicPosition;
    private Class dataType;
    private boolean optional;

    private IndexedElementAdapter element;

    /**
     * Create a column header object. Default, the column header keep the same value with name, and logical position keep
     * the same value with order.
     *
     * @param name define a stable name for column. For optional column, only set stable part for name.
     * @param dataType define the data type for column.
     * @param optional if false the column is stable type, otherwise is optional column.
     * @param order internal order. Every non {@link uk.ac.ebi.pride.jmztab2.model.OptionColumn} has stable order. Column order is used to maintain the
     *              logical position in {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory}
     */
    public MZTabColumn(String name, Class dataType, boolean optional, String order) {
        this(name, dataType, optional, order, null);
    }

    /**
     * Create a column header object. Default, the column header keep the same value with name, and logical position keep
     * the same value with order.
     *
     * @param name define a stable name for column. For optional column, only set stable part for name.
     * @param dataType define the data type for column.
     * @param optional if false the column is stable type, otherwise is optional column.
     * @param order internal order. Every non {@link uk.ac.ebi.pride.jmztab2.model.OptionColumn} has stable order. Column order is used to maintain the
     *              logical position in {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory}
     * @param id incremental index used for some optional columns like best_search_engine_score[1], best_search_engine_score[2]
     */
    public MZTabColumn(String name, Class dataType, boolean optional, String order, Integer id) {
        if (MZTabStringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Column name should not empty.");
        }
        this.name = name;

        if (dataType == null) {
            throw new NullPointerException("Column data type should not set null!");
        }
        this.dataType = dataType;
        this.optional = optional;
        this.order = order;
        this.id = id;

        this.header = generateHeader(name);
        this.logicPosition = generateLogicalPosition();
    }

    private String generateHeader(String name) {
        StringBuilder sb = new StringBuilder();

        sb.append(name);
        if (id != null) {
            sb.append("[").append(id).append("]");
        }

        return sb.toString();
    }

    private String generateLogicalPosition() {
        StringBuilder sb = new StringBuilder();

        sb.append(order);
        if (id != null) {
            // generate id string which length is 2. Eg. 12, return 12; 1, return 01
            sb.append(String.format("%02d", id));
        } else {
            sb.append("00");
        }

        if (element != null) {
            sb.append(String.format("%02d", element.getId()));
        } else {
            sb.append("00");
        }

        return sb.toString();
    }


    /**
     * {@inheritDoc}
     *
     * Get the column name. For stable column, name and header are same. But for optional column, name is part
     * of its header. For example, optional column which header is search_engine_score_ms_run[1-n], and its name
     * is search_engine_score. Besides this, ms_run[1-n] is kind of {@link #element}
     *
     * Notice: this design pattern not fit for {@link AbundanceColumn}, {@link OptionColumn} and {@link ParameterOptionColumn}.
     * These optional columns need be generated by calling {@link MZTabColumnFactory} 's methods.
     * @see #getHeader()
     * @see #setElement(IndexedElement)
     * @see #getHeader()
     * @see #setElement(IndexedElement)
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * Get the column internal order. For stable column, order and logical position are same. But for optional column,
     * the logical position need to be calculated by concatenating order and index element id. For example, optional column
     * search_engine_score_ms_run[2] in Protein section, its order is 09, and the logical position is 092. Because the
     * element ms_run[2] 's index is 2.
     *
     * Notice: this design pattern not fit for {@link AbundanceColumn}, {@link OptionColumn} and {@link ParameterOptionColumn}.
     * These optional columns need be generated by calling {@link MZTabColumnFactory} 's methods.
     * @see #getLogicPosition()
     */
    @Override
    public String getOrder() {
        return order;
    }

    /*
     * Allows to reassign the order in case the file doesn't follow the recommended order
     */
    /** {@inheritDoc} */
    @Override
    public void setOrder(String order) {
        //As the order change, the logic position need to be regenerated.
        this.order = order;
        this.logicPosition = generateLogicalPosition();

    }

    /**
     * {@inheritDoc}
     *
     * Get the column name. For stable column, name and header are same. While for optional column, name is part
     * of its header. For example, optional column which header is search_engine_score_ms_run[1-n], and its name
     * is search_engine_score.  Besides this, ms_run[1-n] is kind of {@link #element}
     *
     * Notice: this design pattern not fit for {@link AbundanceColumn}, {@link OptionColumn} and {@link ParameterOptionColumn}.
     * These optional columns need be generated by calling {@link MZTabColumnFactory} 's methods.
     * @see #getName()
     * @see #setElement(IndexedElement)
     * @see #getName()
     * @see #setElement(IndexedElement)
     */
    @Override
    public String getHeader() {
        return header;
    }

    /** {@inheritDoc} */
    @Override
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * {@inheritDoc}
     *
     * Get the column logical position. For stable column, order and logical position are same. But for optional column,
     * the logical position need to calculate by concatenate order and index element id. For example, optional column
     * search_engine_score_ms_run[2] in Protein section, its order is 09, and the logical position is 092. Because the
     * element ms_run[2] 's index is 2.
     *
     *<p>Notice: this design pattern not fit for {@link AbundanceColumn}, {@link OptionColumn} and {@link ParameterOptionColumn}.
     * These optional columns need be generated by calling {@link MZTabColumnFactory} 's methods.</p>
     *
     *<p>Notice: in {@link MZTabColumnFactory}, we use logical position to maintain the logical consistence within the {@link de.isas.mztab2.model.MzTab} file.
     * During the process of parsing mzTab file, we create a mapping between physical position and internal logical position.</p>
     * @see #getOrder()
     */
    @Override
    public String getLogicPosition() {
        generateLogicalPosition();
        return logicPosition;
    }

    /** {@inheritDoc} */
    @Override
    public void setLogicPosition(String logicPosition) {
        this.logicPosition = logicPosition;
    }

    /**
     * {@inheritDoc}
     *
     * Get the column data type Class.
     */
    @Override
    public Class getDataType() {
        return dataType;
    }

    /**
     * {@inheritDoc}
     *
     * Judge this column belong to stable column or optional column.
     */
    @Override
    public boolean isOptional() {
        return optional;
    }

    /**
     * {@inheritDoc}
     *
     * Indexed element used in optional column header and logical position definition.
     * In stable column, the return is null.
     *
     * Notice: this design pattern not fit for {@link AbundanceColumn}, {@link OptionColumn} and {@link ParameterOptionColumn}.
     * These optional columns need be generated by calling {@link MZTabColumnFactory} 's methods.
     * @see #getHeader()
     * @see #getLogicPosition()
     * @see #getHeader()
     * @see #getLogicPosition()
     */
    @Override
    public IndexedElementAdapter getElement() {
        return element;
    }

    /**
     * {@inheritDoc}
     *
     * Indexed element used in optional column header and logical position definition.
     * In stable column, the return is null.
     *
     * Notice: this design pattern not fit for {@link AbundanceColumn}, {@link OptionColumn} and {@link ParameterOptionColumn}.
     * These optional columns need be generated by calling {@link MZTabColumnFactory} 's methods.
     * @see #getHeader()
     * @see #getLogicPosition()
     * @see #getHeader()
     * @see #getLogicPosition()
     */
    public void setElement(IndexedElementAdapter element) {
        if (element == null) {
            throw new NullPointerException("Can not set null indexed element for optional column!");
        }
        this.element = element;

        this.logicPosition = generateLogicalPosition();
        StringBuilder sb = new StringBuilder();
        if(this instanceof AbundanceColumn) {
            sb.append(this.header).append("[").
            append(element.getId()).
            append("]");
        } else {
            sb.append(this.header).append("_").append(Serializers.getReference(element, element.getId()));
        }
        this.header = sb.toString();
    }

    /**
     * Create a optional column for {@link Section#Protein_Header}, {@link Section#Peptide_Header},
     * {@link Section#PSM_Header}, {@link Section#Small_Molecule_Header}, {@link Section#Small_Molecule_Feature_Header},
     * or {@link Section#Small_Molecule_Evidence_Header}.
     *
     * Notice: this function is only used to create stable order optional column, such as  num_psms_ms_run[1-n] and so on.
     * Not used to create {@link AbundanceColumn}, {@link OptionColumn} and {@link ParameterOptionColumn}.
     * These optional columns need be generated by calling {@link MZTabColumnFactory} 's methods.
     *
     * @see MZTabColumnFactory#addOptionalColumn(MZTabColumn, MsRun)
     */
    static IMZTabColumn createOptionalColumn(Section section, IMZTabColumn column, Integer id, IndexedElementAdapter element) {
        if (! column.isOptional()) {
            throw new IllegalArgumentException(column + " is not optional column!");
        }

        IMZTabColumn optionColumn = null;
        switch (section) {
//            case Protein_Header:
//                optionColumn = new ProteinColumn(column.getName(), column.getDataType(), column.isOptional(), column.getOrder(), id);
//                break;
//            case Peptide_Header:
//                optionColumn = new PeptideColumn(column.getName(), column.getDataType(), column.isOptional(), column.getOrder(), id);
//                break;
//            case PSM_Header:
//                optionColumn = new PSMColumn(column.getName(), column.getDataType(), column.isOptional(), column.getOrder(), id);
//                break;
            case Small_Molecule_Header:
                optionColumn = new SmallMoleculeColumn(column.getName(), column.getDataType(), column.isOptional(), column.getOrder(), id);
                break;
            case Small_Molecule_Feature_Header:
                optionColumn = new SmallMoleculeFeatureColumn(column.getName(), column.getDataType(), column.isOptional(), column.getOrder(), id);
                break;
            case Small_Molecule_Evidence_Header:
                optionColumn = new SmallMoleculeEvidenceColumn(column.getName(), column.getDataType(), column.isOptional(), column.getOrder(), id);
                break;
        }

        if (optionColumn != null && element != null) {
            optionColumn.setElement(element);
        }

        return optionColumn;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "MZTabColumn{" +
                "header='" + header + '\'' +
                ", logicPosition='" + logicPosition + '\'' +
                ", dataType=" + dataType +
                ", optional=" + optional +
                '}';
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MZTabColumn column = (MZTabColumn) o;

        if (optional != column.optional) return false;
        if (dataType != null ? !dataType.equals(column.dataType) : column.dataType != null) return false;
        return (header != null ? header.equals(column.header) : column.header == null) && (logicPosition != null ? logicPosition.equals(column.logicPosition) : column.logicPosition == null);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = header != null ? header.hashCode() : 0;
        result = 31 * result + (logicPosition != null ? logicPosition.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        result = 31 * result + (optional ? 1 : 0);
        return result;
    }
}
