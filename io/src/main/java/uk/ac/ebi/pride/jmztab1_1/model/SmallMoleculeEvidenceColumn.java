package uk.ac.ebi.pride.jmztab1_1.model;

import de.isas.mztab1_1.model.IndexedElement;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.StringList;
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
        SME_ID("SME_ID", Integer.class, false, "01"),
        EVIDENCE_UNIQUE_ID("evidence_unique_ID", Integer.class, false, "02"),
        DATABASE_IDENTIFIER("database_identifier", String.class, false, "03"),
        CHEMICAL_FORMULA(
            "chemical_formula", String.class, true, "04"),
        SMILES("smiles",
            String.class, true, "05"),
        INCHI("inchi",
            String.class, true, "06"),
        //    INCHI_KEY("inchi_key", SplitList.class, false, "04");
        CHEMICAL_NAME(
            "chemical_name", String.class, true, "07"),
        URI("uri",
            java.net.URI.class, true, "08"),
        DERIVATIZED_FORM("derivatized_form", String.class, true, "09"),
        ADDUCT_ION(
            "adduct_ion", String.class, false, "10"),
        EXP_MASS_TO_CHARGE(
            "exp_mass_to_charge", Double.class, false, "11"),
        CHARGE("charge", Integer.class, false, "12"),
        THEORETICAL_MASS_TO_CHARGE("theoretical_mass_to_charge", Double.class,
            false, "13"),
        SPECTRA_REF("spectra_ref", StringList.class, false, "14"),
        IDENTIFICATION_METHOD("identification_method", Parameter.class, false,
            "15"),
        MS_LEVEL("ms_level", Parameter.class, false, "16"),
        RANK("rank", Integer.class, false, "17");
        // FIXME check whether this works (indexed columns), may require custom serializer
//        ID_CONFIDENCE_MEASURE("id_confidence_measure", StringList.class, true, "18");

        private final ISmallMoleculeEvidenceColumn column;

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
     * @return a {@link uk.ac.ebi.pride.jmztab1_1.model.ISmallMoleculeEvidenceColumn} object.
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
