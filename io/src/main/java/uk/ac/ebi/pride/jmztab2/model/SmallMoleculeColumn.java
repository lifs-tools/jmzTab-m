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
import de.isas.mztab2.model.SmallMoleculeSummary;
import static de.isas.mztab2.model.SmallMoleculeSummary.Properties.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Define the stable columns and optional columns which have stable order in
 * small molecule header line. Refactored to be an enum.
 *
 * @author qingwei
 * @author Nils Hoffmann
 * @since 23/05/13
 * 
 */
public class SmallMoleculeColumn implements ISmallMoleculeColumn {

    private final IMZTabColumn column;

    SmallMoleculeColumn(String name, Class dataType, boolean optional,
        String order) {
        this.column = new MZTabColumn(name, dataType, optional, order);
    }

    SmallMoleculeColumn(String name, Class dataType, boolean optional,
        String order, Integer id) {
        this.column = new MZTabColumn(name, dataType, optional, order, id);
    }

    public static enum Stable implements ISmallMoleculeColumn {
        SML_ID(smlId.toUpper(), String.class, false, "01"),
        SMF_ID_REFS(smfIdRefs.toUpper(), SplitList.class, false, "02"),
        DATABASE_IDENTIFIER(databaseIdentifier, SplitList.class, false, "03"),
        CHEMICAL_FORMULA(chemicalFormula, String.class, false, "04"),
        SMILES(smiles,
            SplitList.class, false, "05"),
        INCHI(inchi,
            SplitList.class, false, "06"),
        CHEMICAL_NAME(
            chemicalName, SplitList.class, false, "07"),
        URI(uri,
            java.net.URI.class, false, "08"),
        THEOR_NEUTRAL_MASS(
            theoreticalNeutralMass, Double.class, false, "09"),
        ADDUCT_IONS(
            adductIons, SplitList.class, false, "10"),
        RELIABILITY(
            reliability, String.class, false, "11"),
        BEST_ID_CONFIDENCE_MEASURE(
            bestIdConfidenceMeasure, Parameter.class, false, "12"),
        BEST_ID_CONFIDENCE_VALUE(
            bestIdConfidenceValue, Double.class, false, "13");

        private final ISmallMoleculeColumn column;

        private Stable(SmallMoleculeSummary.Properties property, Class columnType, boolean optional,
            String order, Integer id) {
            this.column = new SmallMoleculeColumn(property.getPropertyName(), columnType, optional,
                order, id);
        }
        
        private Stable(SmallMoleculeSummary.Properties property, Class columnType, boolean optional,
            String order) {
            this.column = new SmallMoleculeColumn(property.getPropertyName(), columnType, optional,
                order);
        }
        
        private Stable(String name, Class columnType, boolean optional,
            String order) {
            this.column = new SmallMoleculeColumn(name, columnType, optional,
                order);
        }

        private Stable(String name, Class columnType, boolean optional,
            String order, Integer id) {
            this.column = new SmallMoleculeColumn(name, columnType, optional,
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
            return Arrays.stream(Stable.values()).
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

    private static Map<String, ISmallMoleculeColumn> columns = new LinkedHashMap<>();

    private static Map<String, ISmallMoleculeColumn> optionalColumns = new LinkedHashMap<>();

    /**
     * <p>optional.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param columnType a {@link java.lang.Class} object.
     * @param optional a boolean.
     * @param order a {@link java.lang.String} object.
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeColumn} object.
     */
    public static ISmallMoleculeColumn optional(String name, Class columnType,
        boolean optional,
        String order, Integer id) {
        if (optionalColumns.containsKey(name)) {
            return optionalColumns.get(name);
        }
        ISmallMoleculeColumn c = new SmallMoleculeColumn(name, columnType,
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
