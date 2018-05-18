package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab1_1.model.MZBoolean;
import uk.ac.ebi.pride.jmztab1_1.model.Section;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.LogicalErrorType;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.ColumnParameterMapping;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.StudyVariable;
import java.util.Arrays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabColumn;

import static uk.ac.ebi.pride.jmztab1_1.model.MZTabUtils.parseParam;

/**
 * A couple of common method used to parse a header line into {@link uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory} structure.
 * 
 * NOTICE: {@link uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory} maintain a couple of {@link MZTabColumn} which have internal logical
 * position and order. In physical mzTab file, we allow user not obey this logical position organized way,
 * and provide their date with own order. In order to distinguish them, we use physical position (a positive
 * integer) to record the column location in mzTab file. And use {@link uk.ac.ebi.pride.jmztab1_1.utils.parser.PositionMapping} structure the maintain
 * the mapping between them.
 *
 * @author qingwei
 * @see SMHLineParser
 * @see SMFLineParser
 * @see SMELineParser
 * @since 11/02/13
 * 
 */
public abstract class MZTabHeaderLineParser extends MZTabLineParser {

    protected MZTabColumnFactory factory;
    protected Metadata metadata;

    /**
     * Parse a header line into {@link uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory} structure.
     *
     * @param context the parser context, keeping dynamic state and lookup associations.
     * @param factory  SHOULD NOT set null
     * @param metadata SHOULD NOT set null
     */
    protected MZTabHeaderLineParser(MZTabParserContext context, MZTabColumnFactory factory, Metadata metadata) {
        super(context);
        if (factory == null) {
            throw new NullPointerException("Header line should be parsed first!");
        }
        this.factory = factory;

        if (metadata == null) {
            throw new NullPointerException("Metadata should be created first!");
        }
        this.metadata = metadata;
    }

    /**
     * {@inheritDoc}
     *
     * Parse a header line into {@link MZTabColumnFactory} structure. There are several steps in this method:
     * Step 1: {@link #parseColumns()} focus on validate and parse all columns. 
     * Step 2: {@link #refine()}
     */
    public void parse(int lineNumber, String line, MZTabErrorList errorList) throws MZTabException {
        super.parse(lineNumber, line, errorList);

        int offset = parseColumns();
        if (offset != items.length) {
            this.errorList.add(new MZTabError(LogicalErrorType.HeaderLine, lineNumber, section.getName(), "" + offset, "" + items.length));
        }

        refine();

    }

    /**
     * This methods delegates to the subclasses the parsing of the columns. All of the columns are defined in 
     * {@link uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeColumn}, {@link uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeFeatureColumn}, or {@link uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeEvidenceColumn}.
     *
     * @return the next physical index of column available after the parsing.
     * @throws uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException if any structural or logical errors are encountered that prohibit further processing.
     */
    protected abstract int parseColumns() throws MZTabException;


    /**
     * Some validate operation need to be done after the whole {@link uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory} created.
     * Thus, user can add them, and called at the end of the
     * {@link #parse(int, String, MZTabErrorList)} method.
     *
     * @throws uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException if any structural or logical errors are encountered that prohibit further processing.
     */
    protected abstract void refine() throws MZTabException;


    /**
     * Refine optional columns and check, whether they were properly defined.
     * These re-validate operation will called in {@link #refine()} method.
     *
     * @param columnHeader a {@link java.lang.String} object.
     * @throws uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException if any structural or logical errors are encountered that prohibit further processing.
     */
    protected void refineOptionalColumn(String columnHeader) throws MZTabException {
        if (factory.findColumnByHeader(columnHeader) == null) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInHeader, lineNumber, columnHeader));
        }
    }

    /**
     * <p>fromIndexToOrder.</p>
     *
     * @param index a {@link java.lang.Integer} object.
     * @return a {@link java.lang.String} object.
     */
    protected String fromIndexToOrder(Integer index) {
        return String.format("%02d", index);
    }

    /**
     * Additional columns can be added to the end of the protein table. These column headers MUST start with the prefix "opt_".
     * Column names MUST only contain the following characters: 'A'-'Z', 'a'-'z', '0'-'9', '_', '-', '[', ']', and ':'.
     * 
     * the format: opt_{IndexedElement[id]}_{value}. Spaces within the parameter's name MUST be replaced by '_'.
     *
     * @param nameLabel a {@link java.lang.String} object.
     * @return a boolean.
     * @throws uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException if any structural or logical errors are encountered that prohibit further processing.
     */
    protected boolean checkOptColumnName(String nameLabel) throws MZTabException {
        nameLabel = nameLabel.trim();

        String regexp = "opt_((assay|study_variable|ms_run)\\[(\\w+)\\]|global)_([A-Za-z0-9_\\-\\[\\]:\\.]+)";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(nameLabel);

        Integer id;
        String object_id;
        String value;
        MZTabError error;
        if (matcher.find()) {
            object_id = matcher.group(1);
            value = matcher.group(4);

            Parameter param = null;
            if (value.startsWith("cv_")) {
                param = checkCVParamOptColumnName(nameLabel, value);
            }

            Class dataType = getDataType(param);

            if (object_id.contains("global")) {
                if (param == null) {
                    factory.addOptionalColumn(value, dataType);
                } else {
                    factory.addOptionalColumn(param, dataType);
                }
            } else {
                id = parseIndex(nameLabel, matcher.group(3));

                if (object_id.contains("assay")) {
                    Assay element = context.getAssayMap().get(id);
                    // not found assay_id in metadata.
                    if (element == null) {
                        error = new MZTabError(LogicalErrorType.AssayNotDefined, lineNumber, nameLabel);
                        throw new MZTabException(error);
                    } else if (param == null) {
                        factory.addOptionalColumn(element, value, dataType);
                    } else {
                        factory.addOptionalColumn(element, param, dataType);
                    }
                } else if (object_id.contains("study_variable")) {
                    StudyVariable element = context.getStudyVariableMap().get(id);
                    // not found study_variable_id in metadata.
                    if (element == null) {
                        error = new MZTabError(LogicalErrorType.StudyVariableNotDefined, lineNumber, nameLabel);
                        throw new MZTabException(error);
                    } else if (param == null) {
                        factory.addOptionalColumn(element, value, dataType);
                    } else {
                        factory.addOptionalColumn(element, param, dataType);
                    }
                } else if (object_id.contains("ms_run")) {
                    // not found ms_run_id in metadata.
                    MsRun element = context.getMsRunMap().get(id);
                    if (element == null) {
                        error = new MZTabError(LogicalErrorType.MsRunNotDefined, lineNumber, nameLabel);
                        throw new MZTabException(error);
                    } else if (param == null) {
                        factory.addOptionalColumn(element, value, dataType);
                    } else {
                        factory.addOptionalColumn(element, param, dataType);
                    }
                }
            }

            return true;
        } else {
            throw new MZTabException(new MZTabError(FormatErrorType.OptionalCVParamColumn, lineNumber, nameLabel));
        }
    }

    /**
     * An kind of {@link CVParamOptionColumn} which use CV parameter accessions in following the format:
     * opt_{OBJECT_ID}_cv_{accession}_{parameter name}. Spaces within the parameter' s name MUST be replaced by '_'.
     */
    private Parameter checkCVParamOptColumnName(String nameLabel, String valueLabel) throws MZTabException {
        nameLabel = nameLabel.trim();
        valueLabel = valueLabel.trim();

        String regexp = "cv(_([A-Za-z0-9\\-\\[\\]:\\.]+))?(_([A-Za-z0-9_\\-\\[\\]:\\.]+)*)";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(valueLabel);

        Parameter param;
        if (!matcher.find() || matcher.end() != valueLabel.length()) {
            throw new MZTabException(new MZTabError(FormatErrorType.OptionalCVParamColumn, lineNumber, nameLabel));
        } else {
            String accession = matcher.group(2);
            String name = matcher.group(4);
            if (name == null || name.trim().length() == 0) {
                throw new MZTabException(new MZTabError(FormatErrorType.OptionalCVParamColumn, lineNumber, nameLabel));
            }

            param = matcher.group(4) == null ? null : new Parameter().cvAccession(accession).name(name);
        }

        return param;
    }

    /**
     * Some {@link CVParamOptionColumn}, their data type have defined. Currently, we provide two {@link Parameter}
     * which defined in the mzTab specification. One is "emPAI value" (MS:1001905), data type is Double;
     * another is "decoy peptide" (MS:1002217), the data type is Boolean (0/1). Besides them, "opt_" start optional
     * column data type is String.
     *
     * @see #checkOptColumnName(String)
     */
    private Class getDataType(Parameter param) {
        Class dataType;

        if (param == null) {
            dataType = String.class;
        } else if (param.getCvAccession().equals("MS:1001905")) {
            dataType = Double.class;
        } else if (param.getCvAccession().equals("MS:1002217")) {
            dataType = MZBoolean.class;
        } else if (param.getCvAccession().equals("PRIDE:0000303")) {
            dataType = MZBoolean.class;
        } else {
            dataType = String.class;
        }

        return dataType;
    }

    /**
     * <p>checkAbundanceColumns.</p>
     *
     * @param offset a int.
     * @param order a {@link java.lang.String} object.
     * @return a int.
     * @throws uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException if any structural or logical errors are encountered that prohibit further processing.
     */
    protected int checkAbundanceColumns(int offset, String order) throws MZTabException {
        String headerString = items[offset];
        if (headerString.contains("abundance_assay")) {
            checkAbundanceAssayColumn(headerString, order);
            return offset;
        } else if (headerString.contains("abundance_study_variable") || headerString.contains("abundance_coeffvar_study_variable")) {
            checkAbundanceStudyVariableColumns(headerString, order);
            return offset;
        } else {
            MZTabError error = new MZTabError(FormatErrorType.AbundanceColumn, lineNumber, headerString);
            throw new MZTabException(error);
        }
    }

    /**
     * Check (protein|peptide|smallmolecule)_abundance is correct, and return object value label.
     * For example, protein_abundance_std_error_study_variable[id], return study_variable[id].
     */
    private String checkAbundanceSection(String abundanceHeader) throws MZTabException {
        abundanceHeader = abundanceHeader.trim().toLowerCase();

        Pattern pattern = Pattern.compile("abundance_(.+)");
        Matcher matcher = pattern.matcher(abundanceHeader);

        if (matcher.find()) {
//            String sectionName = matcher.group(1);
//            if (sectionName != null &&
//                    !(sectionName.equals(Section.Protein.getName()) && section != Section.Protein_Header) &&
//                    !(sectionName.equals(Section.Peptide.getName()) && section != Section.Peptide_Header) &&
//                    !(sectionName.equals(Section.Small_Molecule.getName()) && section != Section.Small_Molecule_Header)) {
                return matcher.group(1);
//            }

//            MZTabError error = new MZTabError(FormatErrorType.AbundanceColumn, lineNumber, abundanceHeader);
//            throw new MZTabException(error);
        } else {
            MZTabError error = new MZTabError(FormatErrorType.AbundanceColumn, lineNumber, abundanceHeader);
            throw new MZTabException(error);
        }
    }

    private void checkAbundanceAssayColumn(String abundanceHeader, String order) throws MZTabException {
        String valueLabel = checkAbundanceSection(abundanceHeader);

        Pattern pattern = Pattern.compile("assay\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(valueLabel);
        if (!matcher.find()) {
            MZTabError error = new MZTabError(FormatErrorType.AbundanceColumn, lineNumber, abundanceHeader);
            throw new MZTabException(error);
        }

        int id = parseIndex(abundanceHeader, matcher.group(1));
        Assay assay = context.getAssayMap().get(id);
        if (assay == null) {
            MZTabError error = new MZTabError(LogicalErrorType.AssayNotDefined, lineNumber, abundanceHeader);
            throw new MZTabException(error);
        }

        factory.addAbundanceOptionalColumn(assay, order);
    }


    private void checkAbundanceStudyVariableColumns(String header,
                                                    String order) throws MZTabException {
        header = header.trim().toLowerCase();

        if (!header.contains("abundance_study_variable") && !header.contains("abundance_coeffvar_study_variable")) {
            MZTabError error = new MZTabError(FormatErrorType.AbundanceColumn, lineNumber, header);
            throw new MZTabException(error);
        } else {
            StudyVariable abundanceStudyVariable = checkAbundanceStudyVariableColumn(header);

            //adds both abundance_study_variable and abundance_coeffvar_study_variable columns
            factory.addAbundanceOptionalColumn(abundanceStudyVariable, checkAbundanceSection(header), order);

        }
    }

    /**
     * Check XXXX_abundance_study_variable[id], XXXX_abundance_stdev_study_variable[id], XXXX_abundance_std_error_study_variable[id]
     * column header. If parse error, stop validate and raise {@link MZTabException}.
     */
    private StudyVariable checkAbundanceStudyVariableColumn(String abundanceHeader) throws MZTabException {
        String valueLabel = checkAbundanceSection(abundanceHeader);

        Pattern pattern = Pattern.compile("study_variable\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(valueLabel);
        if (!matcher.find()) {
            MZTabError error = new MZTabError(FormatErrorType.AbundanceColumn, lineNumber, abundanceHeader);
            throw new MZTabException(error);
        }

        int id = parseIndex(abundanceHeader, matcher.group(1));
        StudyVariable studyVariable = context.getStudyVariableMap().get(id);
        if (studyVariable == null) {
            MZTabError error = new MZTabError(LogicalErrorType.StudyVariableNotDefined, lineNumber, abundanceHeader);
            throw new MZTabException(error);
        }

        return studyVariable;
    }

    /**
     * Parse header to a index id number.
     * If exists parse error, stop validate and throw {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException} directly.
     *
     * @param header a {@link java.lang.String} object.
     * @param id a {@link java.lang.String} object.
     * @return a int.
     * @throws uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException if any structural or logical errors are encountered that prohibit further processing.
     */
    protected int parseIndex(String header, String id) throws MZTabException {
        try {
            Integer index = Integer.parseInt(id);
            if (index < 1) {
                throw new NumberFormatException();
            }

            return index;
        } catch (NumberFormatException e) {
            MZTabError error = new MZTabError(LogicalErrorType.IdNumber, lineNumber, header, id);
            throw new MZTabException(error);
        }
    }

    /**
     * <p>Getter for the field <code>factory</code>.</p>
     *
     * @return a {@link uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory} object.
     */
    public MZTabColumnFactory getFactory() {
        return factory;
    }
}
