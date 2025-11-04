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

import org.lifstools.mztab2.model.IndexedElement;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import static org.lifstools.mztab2.model.SmallMoleculeFeature.Properties.*;
import org.lifstools.mztab2.model.StringList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Define the stable columns which have stable order in the small molecule
 * feature header line.
 *
 * To create optional column mappings, see
 * {@link uk.ac.ebi.pride.jmztab2.model.OptColumnMappingBuilder}.
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

    /**
     * Stable {@link SmallMoleculeFeatureColumn} definition templates.
     */
    public static enum Stable {
        SMF_ID(smfId.toUpper(), Integer.class, false, "01"),
        SME_ID_REFS(smeIdRefs.toUpper(), StringList.class, true, "02"),
        SME_ID_REF_AMBIGUITY_CODE("SME_ID_REF_ambiguity_code", Integer.class,
            true, "03"),
        ADDUCT_ION(
            adductIon, String.class, true, "04"),
        ISOTOPOMER(isotopomer, String.class, true, "05"),
        EXP_MASS_TO_CHARGE(
            expMassToCharge, Double.class, false, "06"),
        Integer charge = (someValue == null || someValue.isEmpty()) ? null : Integer.valueOf(someValue);
        CHARGE(charge, Integer.class, false, "07"),
        RETENTION_TIME_IN_SECONDS(retentionTimeInSeconds, Double.class, true,
            "08"),
        RETENTION_TIME_IN_SECONDS_START(retentionTimeInSecondsStart,
            Double.class, true, "09"),
        RETENTION_TIME_IN_SECONDS_END(retentionTimeInSecondsEnd, Double.class,
            true, "10");

        private final ISmallMoleculeFeatureColumn column;

        private Stable(SmallMoleculeFeature.Properties property,
            Class columnType, boolean optional,
            String order) {
            this.column = new SmallMoleculeFeatureColumn(property.
                getPropertyName(), columnType, optional,
                order);
        }

        private Stable(String name, Class columnType, boolean optional,
            String order) {
            this.column = new SmallMoleculeFeatureColumn(name, columnType,
                optional,
                order);
        }

        private Stable(String name, Class columnType, boolean optional,
            String order, Integer id) {
            this.column = new SmallMoleculeFeatureColumn(name, columnType,
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
        public static Stable forName(String name) throws IllegalArgumentException {
            Stable s = Arrays.stream(Stable.values()).
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
         * Returns a new {@link ISmallMoleculeFeatureColumn} instance for the
         * given stable column template.
         *
         * @param s the small molecule feature stable column template.
         * @return a new small molecule feature column instance
         * {@link SmallMoleculeFeatureColumn}.
         */
        public static ISmallMoleculeFeatureColumn columnFor(
            SmallMoleculeFeatureColumn.Stable s) {
            return new SmallMoleculeFeatureColumn(s.column.getName(), s.column.
                getDataType(), s.column.isOptional(), s.column.getOrder());
        }

        /**
         * Returns a new {@link ISmallMoleculeFeatureColumn} instance for the
         * given stable column name.
         *
         * @param name the small molecule feature stable column template name
         * (lower case).
         * @return a new small molecule feature column instance
         * {@link SmallMoleculeFeatureColumn}.
         * @throws IllegalArgumentException for unknown column names.
         */
        public static ISmallMoleculeFeatureColumn columnFor(String name) throws IllegalArgumentException {
            return columnFor(forName(name));
        }

        /**
         * Returns all stable {@link ISmallMoleculeFeatureColumn} templates.
         *
         * @return the stable small molecule feature columns templates.
         */
        public static List<ISmallMoleculeFeatureColumn> columns() {
            return Arrays.stream(Stable.values()).
                map((s) ->
                {
                    return new SmallMoleculeFeatureColumn(s.column.getName(),
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
        final SmallMoleculeFeatureColumn other = (SmallMoleculeFeatureColumn) obj;
        return Objects.equals(this.column, other.column);
    }

    @Override
    public String toString() {
        return "SmallMoleculeFeatureColumn{" + "column=" + column + '}';
    }
    
}
