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
import de.isas.mztab2.model.SmallMoleculeSummary;
import static de.isas.mztab2.model.SmallMoleculeSummary.Properties.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Define the stable columns which have stable order in the small molecule header
 * line.
 *
 *  * To create optional column mappings, see
 * {@link uk.ac.ebi.pride.jmztab2.model.OptColumnMappingBuilder}.
 *
 * @author qingwei
 * @author nilshoffmann
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

    /**
     * Stable {@link SmallMoleculeColumn} definition templates.
     */
    public static enum Stable {
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

        private Stable(SmallMoleculeSummary.Properties property,
            Class columnType, boolean optional,
            String order) {
            this.column = new SmallMoleculeColumn(property.getPropertyName(),
                columnType, optional,
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

        /**
         * Returns a stable column instance template.
         *
         * @param name the column name (lower case).
         * @return the stable column instance template.
         * @throws IllegalArgumentException for unknown column names.
         */
        public static SmallMoleculeColumn.Stable forName(String name) throws IllegalArgumentException {
            SmallMoleculeColumn.Stable s = Arrays.stream(
                SmallMoleculeColumn.Stable.values()).
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
         * Returns a new {@link ISmallMoleculeColumn} instance for the
         * given stable column template.
         *
         * @param s the small molecule stable column template.
         * @return a new small molecule column instance
         * {@link SmallMoleculeColumn}.
         */
        public static ISmallMoleculeColumn columnFor(Stable s) {
            return new SmallMoleculeColumn(s.column.getName(), s.column.
                getDataType(), s.column.isOptional(), s.column.getOrder());
        }

        /**
         * Returns a new {@link ISmallMoleculeColumn} instance for the
         * given stable column name.
         *
         * @param name the small molecule stable column template name (lower
         * case).
         * @return a new small molecule column instance
         * {@link SmallMoleculeColumn}.
         * @throws IllegalArgumentException for unknown column names.
         */
        public static ISmallMoleculeColumn columnFor(String name) throws IllegalArgumentException {
            return columnFor(forName(name));
        }

        /**
         * Returns all stable {@link ISmallMoleculeColumn} templates.
         *
         * @return the stable small molecule columns templates.
         */
        public static List<ISmallMoleculeColumn> columns() {
            return Arrays.stream(SmallMoleculeColumn.Stable.values()).
                map((s) ->
                {
                    return new SmallMoleculeColumn(s.column.getName(), s.column.
                        getDataType(), s.column.isOptional(), s.column.
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
