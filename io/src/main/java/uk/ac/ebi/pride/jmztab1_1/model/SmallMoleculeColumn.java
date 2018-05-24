package uk.ac.ebi.pride.jmztab1_1.model;

import de.isas.mztab1_1.model.IndexedElement;
import de.isas.mztab1_1.model.Parameter;
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
        SML_ID("SML_ID", String.class, false, "01"),
        SMF_ID_REFS("SMF_ID_REFS", SplitList.class, false, "02"),
        DATABASE_IDENTIFIER("database_identifier", SplitList.class, false, "03"),
        CHEMICAL_FORMULA(
            "chemical_formula", String.class, false, "04"),
        SMILES("smiles",
            SplitList.class, false, "05"),
        INCHI("inchi",
            SplitList.class, false, "06"),
        CHEMICAL_NAME(
            "chemical_name", SplitList.class, false, "07"),
        URI("uri",
            java.net.URI.class, false, "08"),
        THEOR_NEUTRAL_MASS(
            "theoretical_neutral_mass", Double.class, false, "09"),
        ADDUCT_IONS(
            "adduct_ions", SplitList.class, false, "10"),
        RELIABILITY(
            "reliability", String.class, false, "11"),
        BEST_ID_CONFIDENCE_MEASURE(
            "best_id_confidence_measure", Parameter.class, false, "12"),
        BEST_ID_CONFIDENCE_VALUE(
            "best_id_confidence_value", Double.class, false, "13");

        private final ISmallMoleculeColumn column;

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
     * @return a {@link uk.ac.ebi.pride.jmztab1_1.model.ISmallMoleculeColumn} object.
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
