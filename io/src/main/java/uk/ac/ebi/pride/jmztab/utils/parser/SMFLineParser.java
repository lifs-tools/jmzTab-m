package uk.ac.ebi.pride.jmztab.utils.parser;

import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.SmallMoleculeFeature;
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
 * @author nils.hoffmann
 * @since 11/09/17
 */
public class SMFLineParser extends MZTabDataLineParser {

    private SmallMoleculeFeature smallMoleculeFeature;

    public SMFLineParser(MZTabColumnFactory factory, PositionMapping positionMapping,
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
        smallMoleculeFeature = new SmallMoleculeFeature();//(factory, metadata);

        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            logicalPosition = positionMapping.get(physicalPosition);
            column = factory.getColumnMapping().get(logicalPosition);

            if (column != null) {
                columnName = column.getName();
                target = items[physicalPosition];
                if (column instanceof SmallMoleculeColumn) {

                    if (columnName.equals(IDENTIFIER.getName())) {
                        smallMoleculeFeature.setDatabaseIdentifier(checkIdentifier(column, target));
                    } else if (columnName.equals(CHEMICAL_FORMULA.getName())) {
                        smallMoleculeFeature.setChemicalFormula(checkChemicalFormula(column, target));
                    } else if (columnName.equals(SMILES.getName())) {
                        smallMoleculeFeature.setSmiles(checkSmiles(column, target));
                    } else if (columnName.equals(INCHI_KEY.getName())) {
                        smallMoleculeFeature.setInchi(checkInchiKey(column, target));
                    } else if (columnName.equals(DESCRIPTION.getName())) {
                        smallMoleculeFeature.setChemicalName(checkDescription(column, target));
                    } else if (columnName.equals(EXP_MASS_TO_CHARGE.getName())) {
                        smallMoleculeFeature.setExpMassToCharge(checkExpMassToCharge(column, target));
                    } else if (columnName.equals(CALC_MASS_TO_CHARGE.getName())) {
                        smallMoleculeFeature.setTheorNeutralMass(checkCalcMassToCharge(column, target));
                    } else if (columnName.equals(CHARGE.getName())) {
                        smallMoleculeFeature.setCharge(checkCharge(column, target));
                    } else if (columnName.equals(RETENTION_TIME.getName())) {
                        smallMoleculeFeature.setRetentionTime(checkRetentionTime(column, target));
                    } else if (columnName.equals(TAXID.getName())) {
                        smallMoleculeFeature.setTaxid(checkTaxid(column, target));
                    } else if (columnName.equals(SPECIES.getName())) {
                        smallMoleculeFeature.setSpecies(checkSpecies(column, target));
                    } else if (columnName.equals(DATABASE.getName())) {
                        smallMoleculeFeature.setDatabase(checkDatabase(column, target));
                    } else if (columnName.equals(DATABASE_VERSION.getName())) {
                        smallMoleculeFeature.setDatabaseVersion(checkDatabaseVersion(column, target));
                    } else if (columnName.equals(RELIABILITY.getName())) {
                        smallMoleculeFeature.setReliability(checkReliability(column, target));
                    } else if (columnName.equals(URI.getName())) {
                        smallMoleculeFeature.setURI(checkURI(column, target));
                    } else if (columnName.equals(SPECTRA_REF.getName())) {
                        smallMoleculeFeature.setSpectraRef(checkSpectraRef(column, target));
                    } else if (columnName.equals(SEARCH_ENGINE.getName())) {
                        smallMoleculeFeature.setSearchEngine(checkSearchEngine(column, target));
                    } else if (columnName.startsWith(BEST_SEARCH_ENGINE_SCORE.getName())) {
                        int id = loadBestSearchEngineScoreId(column.getHeader());
                        smallMoleculeFeature.setBestSearchEngineScore(id, checkBestSearchEngineScore(column, target));
                    } else if (columnName.startsWith(SEARCH_ENGINE_SCORE.getName())) {
                        int id = loadSearchEngineScoreId(column.getHeader());
                        MsRun msRun = (MsRun) column.getElement();
                        smallMoleculeFeature.setSearchEngineScore(id, msRun, checkSearchEngineScore(column, target));
                    } else if (columnName.equals(MODIFICATIONS.getName())) {
                        smallMoleculeFeature.setModifications(checkModifications(column, target));
                    }

                } else if (column instanceof AbundanceColumn) {
                    //Double check, the column name should contain
                    if (columnName.contains("abundance")) {
                        smallMoleculeFeature.setValue(logicalPosition, checkDouble(column, target));
                    }
                } else if (column instanceof OptionColumn) {
                    //Double check, the column name should opt
                    if (columnName.startsWith("opt_")) {
                        Class dataType = column.getDataType();
                        if (dataType.equals(String.class)) {
                            smallMoleculeFeature.setValue(column.getLogicPosition(), checkString(column, target));
                        } else if (dataType.equals(Double.class)) {
                            smallMoleculeFeature.setValue(column.getLogicPosition(), checkDouble(column, target));
                        } else if (dataType.equals(MZBoolean.class)) {
                            smallMoleculeFeature.setValue(column.getLogicPosition(), checkMZBoolean(column, target));
                        }
                    }
                }
            }
        }

        return physicalPosition;
    }

    public SmallMoleculeFeature getRecord() {

        if(smallMoleculeFeature == null){
            smallMoleculeFeature = new SmallMoleculeFeature();//(factory, metadata);
        }
        return smallMoleculeFeature;
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
