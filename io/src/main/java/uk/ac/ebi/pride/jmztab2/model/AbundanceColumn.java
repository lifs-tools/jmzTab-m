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
import org.lifstools.mztab2.model.StudyVariable;
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

    /**
     * Abundance column kinds. {@link #toString()} returns the column name.
     */
    public enum Field {
        ABUNDANCE_ASSAY("abundance_assay", Double.class),
        ABUNDANCE_STUDY_VARIABLE("abundance_study_variable", Double.class),
        ABUNDANCE_VARIATION_STUDY_VARIABLE("abundance_variation_study_variable", Double.class);

        private final String name;
        private final Class columnType;

        Field(String name, Class columnType) {
            this.name = name;
            this.columnType = columnType;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Generate an abundance column. The header is {Field#name}[element id],
     * the data type is Double.
     */
    private AbundanceColumn(Field field, Object element, int order) {
        super(field.name, field.columnType, true, order);
        setElement(element);
    }

    /**
     * Generate an abundance optional column as measured in the given assay,
     * with header abundance_assay[id].
     *
     * @param section SHOULD be a small molecule table section.
     * @param assay SHOULD not be null.
     * @param order the column's order within its section, e.g. its 1-based
     * header position.
     * @return an abundance optional column as measured in the given assay.
     */
    public static MZTabColumn createOptionalColumn(Section section, Assay assay, int order) {
        checkSection(section);
        if (assay == null) {
            throw new NullPointerException("Assay should not be null!");
        }
        return new AbundanceColumn(Field.ABUNDANCE_ASSAY, assay, order);
    }

    /**
     * Generate an abundance optional column for the given study variable.
     *
     * @param section SHOULD be a small molecule table section.
     * @param studyVariable SHOULD not be null.
     * @param columnHeader the column header with the leading "abundance_"
     * removed; must start with "study_variable" or "variation_study_variable".
     * @param order the column's order within its section, e.g. its 1-based
     * header position.
     * @return the abundance column.
     */
    public static MZTabColumn createOptionalColumn(Section section, StudyVariable studyVariable, String columnHeader, int order) {
        checkSection(section);
        if (studyVariable == null) {
            throw new NullPointerException("Study Variable should not be null!");
        }
        if (columnHeader.startsWith("study_variable")) {
            return new AbundanceColumn(Field.ABUNDANCE_STUDY_VARIABLE, studyVariable, order);
        } else if (columnHeader.startsWith("variation_study_variable")) {
            return new AbundanceColumn(Field.ABUNDANCE_VARIATION_STUDY_VARIABLE, studyVariable, order);
        }
        throw new IllegalArgumentException("column header " + columnHeader + " is not allowed for abundance definition!");
    }

    /**
     * Generate abundance_study_variable[id] at {@code lastOrder + 1} and
     * abundance_variation_study_variable[id] at {@code lastOrder + 2}.
     *
     * @param section SHOULD be a small molecule table section.
     * @param studyVariable SHOULD not be null.
     * @param lastOrder the order of the column preceding the new columns.
     * @return both columns keyed by position.
     */
    public static SortedMap<ColumnPosition, MZTabColumn> createOptionalColumns(Section section, StudyVariable studyVariable, int lastOrder) {
        MZTabColumn abundance = createOptionalColumn(section, studyVariable, "study_variable", lastOrder + 1);
        MZTabColumn variation = createOptionalColumn(section, studyVariable, "variation_study_variable", lastOrder + 2);
        SortedMap<ColumnPosition, MZTabColumn> columns = new TreeMap<>();
        columns.put(abundance.getPosition(), abundance);
        columns.put(variation.getPosition(), variation);
        return columns;
    }

    private static void checkSection(Section section) {
        if (section.isComment() || section.isMetadata()) {
            throw new IllegalArgumentException("Section should be SmallMolecule, SmallMoleculeFeature or SmallMoleculeEvidence.");
        }
    }
}
