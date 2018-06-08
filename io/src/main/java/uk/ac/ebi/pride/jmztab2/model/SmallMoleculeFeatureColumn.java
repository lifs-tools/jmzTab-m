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
import de.isas.mztab2.model.SmallMoleculeFeature;
import static de.isas.mztab2.model.SmallMoleculeFeature.Properties.*;
import de.isas.mztab2.model.StringList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Define the stable columns and optional columns which have stable order in small molecule feature header line.
 *
 * @author nilshoffmann
 * @since 11/09/17
 * 
 */
public class SmallMoleculeFeatureColumn implements ISmallMoleculeFeatureColumn {

    private final IMZTabColumn column;

    SmallMoleculeFeatureColumn(String name, Class dataType, boolean optional,
            String order) {
        this.column = new MZTabColumn(name, dataType, optional, order);
    }

    SmallMoleculeFeatureColumn(String name, Class dataType, boolean optional,
            String order, Integer id) {
        this.column = new MZTabColumn(name, dataType, optional, order, id);
    }

    public static enum Stable implements ISmallMoleculeFeatureColumn {
        SMF_ID(smfId.toUpper(), Integer.class, false, "01"),
        SME_ID_REFS(smeIdRefs.toUpper(), StringList.class, true, "02"),
        SME_ID_REF_AMBIGUITY_CODE("SME_ID_REF_ambiguity_code", Integer.class, true, "03"),
        ADDUCT_ION(
                adductIon, String.class, true, "04"),
        ISOTOPOMER(isotopomer, String.class, true, "05"),
        EXP_MASS_TO_CHARGE(
                expMassToCharge, Double.class, false, "06"),
        CHARGE(charge, Integer.class, false, "07"),
        RETENTION_TIME_IN_SECONDS(retentionTimeInSeconds, Double.class, true, "08"),
        RETENTION_TIME_IN_SECONDS_START(retentionTimeInSecondsStart, Double.class, true, "09"),
        RETENTION_TIME_IN_SECONDS_END(retentionTimeInSecondsEnd, Double.class, true, "10");

        private final ISmallMoleculeFeatureColumn column;

        private Stable(SmallMoleculeFeature.Properties property, Class columnType, boolean optional,
            String order, Integer id) {
            this.column = new SmallMoleculeFeatureColumn(property.getPropertyName(), columnType, optional,
                order, id);
        }
        
        private Stable(SmallMoleculeFeature.Properties property, Class columnType, boolean optional,
            String order) {
            this.column = new SmallMoleculeFeatureColumn(property.getPropertyName(), columnType, optional,
                order);
        }
        
        private Stable(String name, Class columnType, boolean optional,
                String order) {
            this.column = new SmallMoleculeFeatureColumn(name, columnType, optional,
                    order);
        }

        private Stable(String name, Class columnType, boolean optional,
                String order, Integer id) {
            this.column = new SmallMoleculeFeatureColumn(name, columnType, optional,
                    order, id);
        }

        public ISmallMoleculeFeatureColumn getColumn() {
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

        public static Stable forName(String name) throws MZTabException {
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

    private static Map<String, ISmallMoleculeFeatureColumn> optionalColumns = new LinkedHashMap<>();

    /**
     * <p>optional.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param columnType a {@link java.lang.Class} object.
     * @param optional a boolean.
     * @param order a {@link java.lang.String} object.
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeFeatureColumn} object.
     */
    public static ISmallMoleculeFeatureColumn optional(String name, Class columnType,
            boolean optional,
            String order, Integer id) {
        if (optionalColumns.containsKey(name)) {
            return optionalColumns.get(name);
        }
        ISmallMoleculeFeatureColumn c = new SmallMoleculeFeatureColumn(name, columnType,
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
