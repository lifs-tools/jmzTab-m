package uk.ac.ebi.pride.jmztab.utils.parser;

import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.SmallMoleculeSummary;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ebi.pride.jmztab.model.MZTabUtils.parseString;
import static uk.ac.ebi.pride.jmztab.model.SmallMoleculeColumn.*;

/**
 * @author qingwei
 * @since 10/02/13
 */
public class SMLLineParser extends MZTabDataLineParser<SmallMoleculeSummary> {

    private SmallMoleculeSummary smallMoleculeSummary;

    public SMLLineParser(MZTabParserContext context, MZTabColumnFactory factory, PositionMapping positionMapping,
                         Metadata metadata, MZTabErrorList errorList) {
        super(context, factory, positionMapping, metadata, errorList);
    }

    @Override
    protected int checkData() {

        IMZTabColumn column;
        String columnName;
        String target;
        int physicalPosition;
        String logicalPosition;
        smallMoleculeSummary = new SmallMoleculeSummary();//(factory, metadata);

        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            logicalPosition = positionMapping.get(physicalPosition);
            column = factory.getColumnMapping().get(logicalPosition);

            if (column != null) {
                columnName = column.getName();
                target = items[physicalPosition];
                if (column instanceof ISmallMoleculeColumn) {
                    SmallMoleculeColumn.Stable stableColumn = SmallMoleculeColumn.Stable.forName(columnName);
                    switch(stableColumn) {
                        case ADDUCT_IONS:
                            smallMoleculeSummary.adductIons(checkStringList(column, target, MZTabConstants.BAR));
                            break;
                        case BEST_ID_CONFIDENCE_MEASURE:
                            smallMoleculeSummary.bestIdConfidenceMeasure(checkParameter(column, target));
                            break;
                        case BEST_ID_CONFIDENCE_VALUE:
                            smallMoleculeSummary.bestIdConfidenceValue(checkDouble(column, target));
                            break;
                        case CHEMICAL_FORMULA:
                            smallMoleculeSummary.chemicalFormula(checkStringList(column, target, MZTabConstants.BAR));
                            break;
                        case CHEMICAL_NAME:
                            smallMoleculeSummary.chemicalName(checkStringList(column, target, MZTabConstants.BAR));
                            break;
                        case DATABASE_IDENTIFIER:
                            smallMoleculeSummary.databaseIdentifier(checkStringList(column, target, MZTabConstants.BAR));
                            break;
                        case EXP_MASS_TO_CHARGE:
                            smallMoleculeSummary.expMassToCharge(checkDouble(column, target));
                            break;
                        case INCHI:
                            smallMoleculeSummary.inchi(checkStringList(column, target, MZTabConstants.BAR));
                            break;
                        case RELIABILITY:
                            smallMoleculeSummary.reliability(checkString(column, target));
                            break;
                        case RETENTION_TIME:
                            smallMoleculeSummary.retentionTime(checkDouble(column, target));
                            break;
                        case SMF_ID_REFS:
                            smallMoleculeSummary.smfIdRefs(checkStringList(column, target, MZTabConstants.BAR));
                            break;
                        case SMILES:
                            smallMoleculeSummary.smiles(checkSmiles(column, target));
                            break;
                        case SML_ID:
                            smallMoleculeSummary.smlId(checkString(column, target));
                            break;
                        case THEOR_NEUTRAL_MASS:
                            smallMoleculeSummary.theoreticalNeutralMass(
                                checkDoubleList(column, target));
                            break;
                        case URI:
                            smallMoleculeSummary.uri(
                                checkStringList(column, target, MZTabConstants.BAR));
                            break;
                            
                    }

                } else if (column instanceof AbundanceColumn) {
                    //Double check, the column name should contain
                    if (columnName.startsWith("abundance_assay")) {
                        smallMoleculeSummary.addAbundanceAssayItem(checkDouble(column, target));
                    }else if(columnName.startsWith("abundance_study_variable")) {
                        smallMoleculeSummary.addAbundanceStudyVariableItem(checkDouble(column, target));
                    }else if(columnName.startsWith("abundance_coeffvar_study_variable")) {
                        smallMoleculeSummary.addAbundanceCoeffvarStudyVariableItem(checkDouble(column, target));
                    }
                } else if (column instanceof OptionColumn) {
                    //Double check, the column name should opt
                    if (columnName.startsWith("opt_")) {
                        Class dataType = column.getDataType();
                        OptColumnMapping optColMapping = new OptColumnMapping();
                        optColMapping.identifier(columnName.substring("opt_".length()));
                        if (dataType.equals(String.class)) {
                            optColMapping.value(checkString(column, target));
                        } else if (dataType.equals(Double.class)) {
                            optColMapping.value(Double.toString(checkDouble(column, target)));
                        } else if (dataType.equals(MZBoolean.class)) {
                            optColMapping.value(Boolean.toString(checkMZBoolean(column, target).toBoolean()));
                        }
                        smallMoleculeSummary.addOptItem(optColMapping);
                    }
                }
            }
        }

        return physicalPosition;
    }

    public SmallMoleculeSummary getRecord() {

        if(smallMoleculeSummary == null){
            smallMoleculeSummary = new SmallMoleculeSummary();//(factory, metadata);
        }
        return smallMoleculeSummary;
    }

    /**
     * As these two ontologies are not applicable to small molecules, so-called CHEMMODs can also be defined.
     * CHEMMODs MUST NOT be used if the modification can be reported using a PSI-MOD or UNIMOD accession.
     * Mass deltas MUST NOT be used for CHEMMODs if the delta can be expressed through a known chemical formula .
     */
//    protected SplitList<Modification> checkModifications(MZTabColumn column, String target) {
//        SplitList<Modification> modificationList = super.checkModifications(section, column, target);
//
//        for (Modification mod : modificationList) {
//            if (mod.getType() == Modification.Type.CHEMMOD) {
//                if (target.contains("-MOD:") || target.contains("-UNIMOD:")) {
//                    errorList.add(new MZTabError(LogicalErrorType.CHEMMODS, lineNumber, column.getHeader(), mod.toString()));
//                }
//
//                if (parseChemmodAccession(mod.getAccession()) == null) {
//                    errorList.add(new MZTabError(FormatErrorType.CHEMMODSAccession, lineNumber, column.getHeader(), mod.toString()));
//                    return null;
//                }
//            }
//        }
//
//        return modificationList;
//    }
//
//    private String parseChemmodAccession(String accession) {
//        accession = parseString(accession);
//
//        Pattern pattern = Pattern.compile("[+-](\\d+(.\\d+)?)?|(([A-Z][a-z]*)(\\d*))?");
//        Matcher matcher = pattern.matcher(accession);
//
//        if (matcher.find()) {
//            return accession;
//        } else {
//            return null;
//        }
//    }
}
