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

import de.isas.mztab2.model.IndexedElementAdapter;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.SmallMoleculeEvidence;
import static de.isas.mztab2.model.SmallMoleculeEvidence.Properties.*;
import de.isas.mztab2.model.StringList;
//import de.isas.mztab2.model.StringList;
import java.util.Arrays;
import java.util.List;
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
        String order) {
        this.column = new MZTabColumn(name, dataType, optional, order);
    }

    SmallMoleculeEvidenceColumn(String name, Class dataType, boolean optional,
        String order, Integer id) {
        this.column = new MZTabColumn(name, dataType, optional, order, id);
    }

    /**
     * Stable {@link SmallMoleculeEvidenceColumn} definition templates.
     */
    public static enum Stable {
        SME_ID(smeId.toUpper(), Integer.class, false, "01"),
        EVIDENCE_INPUT_ID(evidenceInputId, Integer.class, false, "02"),
        DATABASE_IDENTIFIER(databaseIdentifier, String.class, false, "03"),
        CHEMICAL_FORMULA(
            chemicalFormula, String.class, true, "04"),
        SMILES(smiles,
            String.class, true, "05"),
        INCHI(inchi,
            String.class, true, "06"),
        CHEMICAL_NAME(
            chemicalName, String.class, true, "07"),
        URI(uri,
            java.net.URI.class, true, "08"),
        DERIVATIZED_FORM(derivatizedForm, String.class, true, "09"),
        ADDUCT_ION(
            adductIon, String.class, false, "10"),
        EXP_MASS_TO_CHARGE(
            expMassToCharge, Double.class, false, "11"),
        CHARGE(charge, Integer.class, false, "12"),
        THEORETICAL_MASS_TO_CHARGE(theoreticalMassToCharge, Double.class,
            false, "13"),
        SPECTRA_REF(spectraRef, StringList.class, false, "14"),
        IDENTIFICATION_METHOD(identificationMethod, Parameter.class, false,
            "15"),
        MS_LEVEL(msLevel, Parameter.class, false, "16"),
        RANK(rank, Integer.class, false, "17");

        private final ISmallMoleculeEvidenceColumn column;

        private Stable(SmallMoleculeEvidence.Properties property,
            Class columnType, boolean optional,
            String order) {
            this.column = new SmallMoleculeEvidenceColumn(property.
                getPropertyName(), columnType, optional,
                order);
        }

        private Stable(String name, Class columnType, boolean optional,
            String order) {
            this.column = new SmallMoleculeEvidenceColumn(name, columnType,
                optional,
                order);
        }

        private Stable(String name, Class columnType, boolean optional,
            String order, Integer id) {
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
    public IndexedElementAdapter getElement() {
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
    public String getLogicPosition() {
        return this.column.getLogicPosition();
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
    public String getOrder() {
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
    public void setLogicPosition(String logicPosition) {
        this.column.setLogicPosition(logicPosition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOrder(String order) {
        this.column.setOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setElement(IndexedElementAdapter element) {
        this.column.setElement(element);
    }
}
