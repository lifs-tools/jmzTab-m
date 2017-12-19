package uk.ac.ebi.pride.jmztab.utils.parser;

import de.isas.mztab1_1.model.Metadata;
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
public class SMLLineParser extends MZTabDataLineParser {

    private SmallMoleculeSummary smallMoleculeSummary;

    public SMLLineParser(MZTabColumnFactory factory, PositionMapping positionMapping,
                         Metadata metadata, MZTabErrorList errorList) {
        super(factory, positionMapping, metadata, errorList);
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
                if (column instanceof SmallMoleculeColumn) {

                    if (columnName.equals(IDENTIFIER.getName())) {
                        smallMoleculeSummary.setDatabaseIdentifier(checkIdentifier(column, target));
                    } else if (columnName.equals(CHEMICAL_FORMULA.getName())) {
                        smallMoleculeSummary.setChemicalFormula(checkChemicalFormula(column, target));
                    } else if (columnName.equals(SMILES.getName())) {
                        smallMoleculeSummary.setSmiles(checkSmiles(column, target));
                    } else if (columnName.equals(INCHI_KEY.getName())) {
                        smallMoleculeSummary.setInchi(checkInchiKey(column, target));
                    } else if (columnName.equals(DESCRIPTION.getName())) {
                        smallMoleculeSummary.setChemicalName(checkDescription(column, target));
                    } else if (columnName.equals(EXP_MASS_TO_CHARGE.getName())) {
                        smallMoleculeSummary.setExpMassToCharge(checkExpMassToCharge(column, target));
                    } else if (columnName.equals(CALC_MASS_TO_CHARGE.getName())) {
                        smallMoleculeSummary.setTheorNeutralMass(checkCalcMassToCharge(column, target));
                    } else if (columnName.equals(CHARGE.getName())) {
                        smallMoleculeSummary.setCharge(checkCharge(column, target));
                    } else if (columnName.equals(RETENTION_TIME.getName())) {
                        smallMoleculeSummary.setRetentionTime(checkRetentionTime(column, target));
                    } else if (columnName.equals(TAXID.getName())) {
                        smallMoleculeSummary.setTaxid(checkTaxid(column, target));
                    } else if (columnName.equals(SPECIES.getName())) {
                        smallMoleculeSummary.setSpecies(checkSpecies(column, target));
                    } else if (columnName.equals(DATABASE.getName())) {
                        smallMoleculeSummary.setDatabase(checkDatabase(column, target));
                    } else if (columnName.equals(DATABASE_VERSION.getName())) {
                        smallMoleculeSummary.setDatabaseVersion(checkDatabaseVersion(column, target));
                    } else if (columnName.equals(RELIABILITY.getName())) {
                        smallMoleculeSummary.setReliability(checkReliability(column, target));
                    } else if (columnName.equals(URI.getName())) {
                        smallMoleculeSummary.setURI(checkURI(column, target));
                    } else if (columnName.equals(SPECTRA_REF.getName())) {
                        smallMoleculeSummary.setSpectraRef(checkSpectraRef(column, target));
                    } else if (columnName.equals(SEARCH_ENGINE.getName())) {
                        smallMoleculeSummary.setSearchEngine(checkSearchEngine(column, target));
                    } else if (columnName.startsWith(BEST_SEARCH_ENGINE_SCORE.getName())) {
                        int id = loadBestSearchEngineScoreId(column.getHeader());
                        smallMoleculeSummary.setBestSearchEngineScore(id, checkBestSearchEngineScore(column, target));
                    } else if (columnName.startsWith(SEARCH_ENGINE_SCORE.getName())) {
                        int id = loadSearchEngineScoreId(column.getHeader());
                        MsRun msRun = (MsRun) column.getElement();
                        smallMoleculeSummary.setSearchEngineScore(id, msRun, checkSearchEngineScore(column, target));
                    } else if (columnName.equals(MODIFICATIONS.getName())) {
                        smallMoleculeSummary.setModifications(checkModifications(column, target));
                    }

                } else if (column instanceof AbundanceColumn) {
                    //Double check, the column name should contain
                    if (columnName.contains("abundance")) {
                        smallMoleculeSummary.setValue(logicalPosition, checkDouble(column, target));
                    }
                } else if (column instanceof OptionColumn) {
                    //Double check, the column name should opt
                    if (columnName.startsWith("opt_")) {
                        Class dataType = column.getDataType();
                        if (dataType.equals(String.class)) {
                            smallMoleculeSummary.setValue(column.getLogicPosition(), checkString(column, target));
                        } else if (dataType.equals(Double.class)) {
                            smallMoleculeSummary.setValue(column.getLogicPosition(), checkDouble(column, target));
                        } else if (dataType.equals(MZBoolean.class)) {
                            smallMoleculeSummary.setValue(column.getLogicPosition(), checkMZBoolean(column, target));
                        }
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
