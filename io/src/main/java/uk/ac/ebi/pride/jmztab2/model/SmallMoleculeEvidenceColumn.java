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

import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import org.lifstools.mztab2.model.StringList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Define the stable columns which have stable order in the small molecule
 * evidence header line. Refactored to contain an enum for stable columns.
 *
 * @author nilshoffmann
 * @since 11/09/17
 *
 */
public class SmallMoleculeEvidenceColumn implements ISmallMoleculeEvidenceColumn {

    private final IMZTabColumn column;

    SmallMoleculeEvidenceColumn(String name, Class dataType, boolean optional,
        int order) {
        this.column = new MZTabColumn(name, dataType, optional, order);
    }

    SmallMoleculeEvidenceColumn(String name, Class dataType, boolean optional,
        int order, Integer id) {
        this.column = new MZTabColumn(name, dataType, optional, order, id);
    }

    /**
     * Stable {@link SmallMoleculeEvidenceColumn} definition templates.
     */
    public static enum Stable {
        SME_ID(SmallMoleculeEvidence.JSON_PROPERTY_SME_ID.toUpperCase(), Integer.class, false, 1),
        EVIDENCE_INPUT_ID(SmallMoleculeEvidence.JSON_PROPERTY_EVIDENCE_INPUT_ID, Integer.class, false, 2),
        DATABASE_IDENTIFIER(SmallMoleculeEvidence.JSON_PROPERTY_DATABASE_IDENTIFIER, String.class, false, 3),
        CHEMICAL_FORMULA(
            SmallMoleculeEvidence.JSON_PROPERTY_CHEMICAL_FORMULA, String.class, true, 4),
        SMILES(SmallMoleculeEvidence.JSON_PROPERTY_SMILES,
            String.class, true, 5),
        INCHI(SmallMoleculeEvidence.JSON_PROPERTY_INCHI,
            String.class, true, 6),
        CHEMICAL_NAME(
            SmallMoleculeEvidence.JSON_PROPERTY_CHEMICAL_NAME, String.class, true, 7),
        URI(SmallMoleculeEvidence.JSON_PROPERTY_URI,
            java.net.URI.class, true, 8),
        DERIVATIZED_FORM(SmallMoleculeEvidence.JSON_PROPERTY_DERIVATIZED_FORM, String.class, true, 9),
        ADDUCT_ION(
            SmallMoleculeEvidence.JSON_PROPERTY_ADDUCT_ION, String.class, false, 10),
        EXP_MASS_TO_CHARGE(
            SmallMoleculeEvidence.JSON_PROPERTY_EXP_MASS_TO_CHARGE, Double.class, false, 11),
        CHARGE(SmallMoleculeEvidence.JSON_PROPERTY_CHARGE, Integer.class, false, 12),
        THEORETICAL_MASS_TO_CHARGE(SmallMoleculeEvidence.JSON_PROPERTY_THEORETICAL_MASS_TO_CHARGE, Double.class,
            false, 13),
        SPECTRA_REF(SmallMoleculeEvidence.JSON_PROPERTY_SPECTRA_REF, StringList.class, false, 14),
        IDENTIFICATION_METHOD(SmallMoleculeEvidence.JSON_PROPERTY_IDENTIFICATION_METHOD, Parameter.class, false,
            15),
        MS_LEVEL(SmallMoleculeEvidence.JSON_PROPERTY_MS_LEVEL, Parameter.class, false, 16),
        RANK(SmallMoleculeEvidence.JSON_PROPERTY_RANK, Integer.class, false, 17);

        private final ISmallMoleculeEvidenceColumn column;

        private Stable(String name, Class columnType, boolean optional,
            int order) {
            this.column = new SmallMoleculeEvidenceColumn(name, columnType,
                optional,
                order);
        }

        private Stable(String name, Class columnType, boolean optional,
            int order, Integer id) {
            this.column = new SmallMoleculeEvidenceColumn(name, columnType,
                optional,
                order, id);
        }

        /**
         * Returns a stable column instance template.
         *
         * @param name the column name (lower case).
         * @return the stable column instance template.
         * @throws IllegalArgumentException for unknown column names.
         */
        public static SmallMoleculeEvidenceColumn.Stable forName(String name) throws IllegalArgumentException {
            SmallMoleculeEvidenceColumn.Stable s = Arrays.stream(
                SmallMoleculeEvidenceColumn.Stable.values()).
                filter((v) ->
                    v.column.
                        getName().
                        equals(name)).
                findFirst().
                orElseThrow(() ->
                    new IllegalArgumentException("Unknown key:" + name));
            return s;
        }

        /**
         * Returns a new {@link ISmallMoleculeEvidenceColumn} instance for the
         * given stable column template.
         *
         * @param s the small molecule evidence stable column template.
         * @return a new small molecule column instance
         * {@link SmallMoleculeEvidenceColumn}.
         */
        public static ISmallMoleculeEvidenceColumn columnFor(
            SmallMoleculeEvidenceColumn.Stable s) {
            return new SmallMoleculeEvidenceColumn(s.column.getName(), s.column.
                getDataType(), s.column.isOptional(), s.column.getOrder());
        }

       /**
         * Returns a new {@link ISmallMoleculeEvidenceColumn} instance for the
         * given stable column name.
         *
         * @param name the small molecule stable column template name (lower
         * case).
         * @return a new small molecule column instance
         * {@link SmallMoleculeEvidenceColumn}.
         * @throws IllegalArgumentException for unknown column names.
         */
        public static ISmallMoleculeEvidenceColumn columnFor(String name) throws IllegalArgumentException {
            return columnFor(forName(name));
        }

        /**
         * Returns all stable {@link SmallMoleculeEvidenceColumn} templates.
         *
         * @return the stable small molecule columns templates.
         */
        public static List<ISmallMoleculeEvidenceColumn> columns() {
            return Arrays.stream(SmallMoleculeEvidenceColumn.Stable.values()).
                map((s) ->
                {
                    return new SmallMoleculeEvidenceColumn(s.column.getName(),
                        s.column.getDataType(), s.column.isOptional(), s.column.
                        getOrder());
                }).
                collect(Collectors.toList());
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDataType() {
        return this.column.getDataType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getElement() {
        return this.column.getElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeader() {
        return this.column.getHeader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ColumnPosition getPosition() {
        return this.column.getPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.column.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return this.column.getOrder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOptional() {
        return this.column.isOptional();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeader(String header) {
        this.column.setHeader(header);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOrder(int order) {
        this.column.setOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setElement(Object element) {
        this.column.setElement(element);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SmallMoleculeEvidenceColumn other = (SmallMoleculeEvidenceColumn) obj;
        return Objects.equals(this.column, other.column);
    }

    @Override
    public String toString() {
        return "SmallMoleculeEvidenceColumn{" + "column=" + column + '}';
    }
    
}
