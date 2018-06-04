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

import de.isas.mztab2.model.IndexedElement;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.StringList;
import de.isas.mztab2.model.SmallMoleculeEvidence;
import static de.isas.mztab2.model.SmallMoleculeEvidence.Properties.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Define the stable columns and optional columns which have stable order in
 * small molecule feature header line.
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

    public static enum Stable implements ISmallMoleculeEvidenceColumn {
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
        // FIXME check whether this works (indexed columns), may require custom serializer
//        ID_CONFIDENCE_MEASURE("id_confidence_measure", StringList.class, true, "18");

        private final ISmallMoleculeEvidenceColumn column;

        private Stable(SmallMoleculeEvidence.Properties property, Class columnType, boolean optional,
            String order, Integer id) {
            this.column = new SmallMoleculeEvidenceColumn(property.getPropertyName(), columnType, optional,
                order, id);
        }
        
        private Stable(SmallMoleculeEvidence.Properties property, Class columnType, boolean optional,
            String order) {
            this.column = new SmallMoleculeEvidenceColumn(property.getPropertyName(), columnType, optional,
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

        public ISmallMoleculeColumn getColumn() {
            return this.column;
        }

        @Override
        public Class<?> getDataType() {
            return this.column.getDataType();
        }

        @Override
        public IndexedElement getElement() {
            return this.column.getElement();
        }

        @Override
        public String getHeader() {
            return this.column.getHeader();
        }

        @Override
        public String getLogicPosition() {
            return this.column.getLogicPosition();
        }

        @Override
        public String getName() {
            return this.column.getName();
        }

        @Override
        public String getOrder() {
            return this.column.getOrder();
        }

        @Override
        public boolean isOptional() {
            return this.column.isOptional();
        }

        @Override
        public void setHeader(String header) {
            this.column.setHeader(header);
        }

        @Override
        public void setLogicPosition(String logicPosition) {
            this.column.setLogicPosition(logicPosition);
        }

        @Override
        public void setOrder(String order) {
            this.column.setOrder(order);
        }

        public static Stable forName(String name) {
            return Arrays.stream(SmallMoleculeEvidenceColumn.Stable.values()).
                filter((v) ->
                    v.getColumn().
                        getName().
                        equals(name)).
                findFirst().
                orElseThrow(() ->
                    new IllegalArgumentException("Unknown key:" + name));
        }

        @Override
        public void setElement(IndexedElement element) {
            this.column.setElement(element);
        }
    };

    private static Map<String, ISmallMoleculeEvidenceColumn> optionalColumns = new LinkedHashMap<>();

    /**
     * <p>optional.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param columnType a {@link java.lang.Class} object.
     * @param optional a boolean.
     * @param order a {@link java.lang.String} object.
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeEvidenceColumn} object.
     */
    public static ISmallMoleculeEvidenceColumn optional(String name,
        Class columnType,
        boolean optional,
        String order, Integer id) {
        if (optionalColumns.containsKey(name)) {
            return optionalColumns.get(name);
        }
        ISmallMoleculeEvidenceColumn c = new SmallMoleculeEvidenceColumn(name,
            columnType,
            optional, order, id);
        optionalColumns.put(name, c);
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getDataType() {
        return this.column.getDataType();
    }

    /** {@inheritDoc} */
    @Override
    public IndexedElement getElement() {
        return this.column.getElement();
    }

    /** {@inheritDoc} */
    @Override
    public String getHeader() {
        return this.column.getHeader();
    }

    /** {@inheritDoc} */
    @Override
    public String getLogicPosition() {
        return this.column.getLogicPosition();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return this.column.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getOrder() {
        return this.column.getOrder();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOptional() {
        return this.column.isOptional();
    }

    /** {@inheritDoc} */
    @Override
    public void setHeader(String header) {
        this.column.setHeader(header);
    }

    /** {@inheritDoc} */
    @Override
    public void setLogicPosition(String logicPosition) {
        this.column.setLogicPosition(logicPosition);
    }

    /** {@inheritDoc} */
    @Override
    public void setOrder(String order) {
        this.column.setOrder(order);
    }

    /** {@inheritDoc} */
    @Override
    public void setElement(IndexedElement element) {
        this.column.setElement(element);
    }
}
