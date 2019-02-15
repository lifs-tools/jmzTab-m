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

import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.IndexedElement;
import de.isas.mztab2.model.StudyVariable;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * If the data exporter wishes to report only final results for 'Summary' files (i.e. following averaging over replicates),
 * then these MUST be reported as quantitative values in the columns associated with the study_variable[1-n] (e.g.
 * abundance_study_variable[1]). mzTab allows the reporting of abundance, standard deviation, and standard error
 * for any study_variable. The unit of values in the abundance column MUST be specified in the metadata section of the mzTab file.
 * The reported values SHOULD represent the final result of the performed data analysis. The exact meaning of the values will
 * thus depend on the used analysis pipeline and quantitation method and is not expected to be comparable across multiple mzTab files.
 *
 * @author qingwei
 * @author nilshoffmann
 * @since 23/05/13
 * 
 */
public class AbundanceColumn extends MZTabColumn {
    public enum Field {
        ABUNDANCE_ASSAY          ("abundance_assay",              Double.class,    1),
        ABUNDANCE_STUDY_VARIABLE ("abundance_study_variable",     Double.class,    2),
        ABUNDANCE_VARIATION_STUDY_VARIABLE ("abundance_variation_study_variable",        Double.class,    3);

        private String name;
        private Class columnType;
        private int position;

        Field(String name, Class columnType, int position) {
            this.name = name;
            this.columnType = columnType;
            this.position = position;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Generate a abundance column:
     * The column header is: {Section}_{Field#name()}_{IndexedElement[id]}
     * The column data type: {Field#columnType()}
     * The column position: always most right side, calculated by offset.
     */
    private AbundanceColumn(Section section, Field field, IndexedElement element, int offset) {
        super(field.name, field.columnType, true, offset + field.position + "");
        setElement(element);
    }

    /**
     * Generate a abundance optional column as measured in the given assay.The column header like
     * protein_abundance_assay[1-n], the position always stay the most right of the tabled section,
     * and the data type is Double.
     *
     * @param section SHOULD be {@link uk.ac.ebi.pride.jmztab2.model.Section#Protein}, {@link uk.ac.ebi.pride.jmztab2.model.Section#Peptide}, {@link uk.ac.ebi.pride.jmztab2.model.Section#PSM},
     *                or {@link uk.ac.ebi.pride.jmztab2.model.Section#Small_Molecule}.
     * @param assay SHOULD not be null.
     * @param offset Normally the last column's position in header, {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory#getColumnMapping()},
     * @return an abundance optional column as measured in the given assay.
     */
    public static MZTabColumn createOptionalColumn(Section section, Assay assay, int offset) {
        if (section.isComment() || section.isMetadata()) {
            throw new IllegalArgumentException("Section should be Protein, Peptide, PSM or SmallMolecule.");
        }
        if (assay == null) {
            throw new NullPointerException("Assay should not be null!");
        }

        return new AbundanceColumn(Section.toDataSection(section), Field.ABUNDANCE_ASSAY, assay, offset);
    }

    /**
     * Generate an abundance optional column as measured in the given study variable.
     * The header can be one of abundance_study_variable[1], abundance_coeffvar_study_variable[1].
     * The position always stay the most right of the tabled section, and the data type is Double.
     *
     * @param section SHOULD be {@link uk.ac.ebi.pride.jmztab2.model.Section#Protein}, {@link uk.ac.ebi.pride.jmztab2.model.Section#Peptide}, {@link uk.ac.ebi.pride.jmztab2.model.Section#PSM},
     *                or {@link uk.ac.ebi.pride.jmztab2.model.Section#Small_Molecule}.
     * @param studyVariable SHOULD not be null.
     * @param order position in header for the new columns {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory#getColumnMapping()}
     * @return an abundance optional column as measured in the given study variable.
     * @param columnHeader a processed column header with abundance_ removed.
     * 
     */
    public static SortedMap<String, MZTabColumn> createOptionalColumns(Section section, StudyVariable studyVariable, String columnHeader, String order) {
        if (section.isComment() || section.isMetadata()) {
            throw new IllegalArgumentException("Section should be Protein, Peptide, PSM, SmallMolecule, SmallMoleculeFeature or SmallMoleculeEvidence.");
        }
        if (studyVariable == null) {
            throw new NullPointerException("Study Variable should not be null!");
        }

        //In this case we know the real position in which the column need to star, so the offset is one less
        int offset = new Integer(order)-1;

        SortedMap<String, MZTabColumn> columns = new TreeMap<>();
        Section dataSection = Section.toDataSection(section);

        AbundanceColumn column;
        if(columnHeader.startsWith("study_variable")) {
            column = new AbundanceColumn(dataSection, Field.ABUNDANCE_STUDY_VARIABLE, studyVariable, offset);
            columns.put(column.getLogicPosition(), column);
        } else if (columnHeader.startsWith("variation_study_variable")) {
            column = new AbundanceColumn(dataSection, Field.ABUNDANCE_VARIATION_STUDY_VARIABLE, studyVariable, offset);
            columns.put(column.getLogicPosition(), column);
        } else {
            throw new IllegalArgumentException("column header "+columnHeader+" is not allowed for abundance definition!");
        }

        return columns;
    }

    /**
     * <p>createOptionalColumns.</p>
     *
     * @param section a {@link uk.ac.ebi.pride.jmztab2.model.Section} object.
     * @param studyVariable a {@link de.isas.mztab2.model.StudyVariable} object.
     * @param lastOrder a {@link java.lang.Integer} object.
     * @return a {@link java.util.SortedMap} object.
     */
    public static SortedMap<String, MZTabColumn> createOptionalColumns(Section section, StudyVariable studyVariable, Integer lastOrder) {
        if (section.isComment() || section.isMetadata()) {
            throw new IllegalArgumentException("Section should be Protein, Peptide, PSM or SmallMolecule.");
        }
        if (studyVariable == null) {
            throw new NullPointerException("Study Variable should not be null!");
        }

        //In this case we know the real position in which the column need to star, so the offset is one less

        SortedMap<String, MZTabColumn> columns = new TreeMap<>();
        Section dataSection = Section.toDataSection(section);

        AbundanceColumn column;
        column = new AbundanceColumn(dataSection, Field.ABUNDANCE_STUDY_VARIABLE, studyVariable, lastOrder);
        columns.put(column.getLogicPosition(), column);
        column = new AbundanceColumn(dataSection, Field.ABUNDANCE_VARIATION_STUDY_VARIABLE, studyVariable, lastOrder);
        columns.put(column.getLogicPosition(), column);

        return columns;
    }
}
