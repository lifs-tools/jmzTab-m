package uk.ac.ebi.pride.jmztab.model;

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

    @Override
    public void setElement(IndexedElement element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        //    INCHI_KEY("inchi_key", SplitList.class, false, "04");
        CHEMICAL_NAME(
                "chemical_name", SplitList.class, false, "07"),
        URI("uri",
                java.net.URI.class, false, "08"),
        THEOR_NEUTRAL_MASS(
                "theoretical_neutral_mass", Double.class, false, "09"),
        EXP_MASS_TO_CHARGE(
                "exp_mass_to_charge", Double.class, false, "10"),
        RETENTION_TIME(
                "retention_time", Double.class, false, "11"),
        ADDUCT_IONS(
                "adduct_ions", SplitList.class, false, "12"),
        RELIABILITY(
                "reliability", String.class, false, "13"),
        BEST_ID_CONFIDENCE_MEASURE(
                "best_id_confidence_measure", Parameter.class, false, "14"),
        BEST_ID_CONFIDENCE_VALUE(
                "best_id_confidence_value", Double.class, false, "15");

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

        public static ISmallMoleculeColumn forName(String name) {
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

    private static Map<String, ISmallMoleculeColumn> optionalColumns = new LinkedHashMap<>();

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
        this.setHeader(header);
    }

    @Override
    public void setLogicPosition(String logicPosition) {
        setLogicPosition(logicPosition);
    }

    @Override
    public void setOrder(String order) {
        setOrder(order);
    }

}
