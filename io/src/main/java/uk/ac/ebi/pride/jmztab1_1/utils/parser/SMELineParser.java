package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab1_1.model.MZBoolean;
import uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab1_1.model.OptionColumn;
import uk.ac.ebi.pride.jmztab1_1.model.ISmallMoleculeEvidenceColumn;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.SmallMoleculeEvidence;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ebi.pride.jmztab1_1.model.MZTabUtils.parseString;
import static uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeColumn.*;

/**
 * @author nils.hoffmann
 * @since 11/09/17
 */
public class SMELineParser extends MZTabDataLineParser<SmallMoleculeEvidence> {

    private SmallMoleculeEvidence smallMoleculeEvidence;

    public SMELineParser(MZTabParserContext context, MZTabColumnFactory factory, PositionMapping positionMapping,
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
        smallMoleculeEvidence = new SmallMoleculeEvidence();//(factory, metadata);

        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            logicalPosition = positionMapping.get(physicalPosition);
            column = factory.getColumnMapping().get(logicalPosition);

            if (column != null) {
                columnName = column.getName();
                target = items[physicalPosition];
                if (column instanceof ISmallMoleculeEvidenceColumn) {
                    SmallMoleculeEvidenceColumn.Stable stableColumn = SmallMoleculeEvidenceColumn.Stable.forName(columnName);
                    switch(stableColumn) {
                        case ADDUCT_ION:
                            smallMoleculeEvidence.adductIon(checkString(column,
                                target));
                            break;
                        case CHARGE:
                            smallMoleculeEvidence.charge(checkInteger(column,
                                target));
                            break;
                        case CHEMICAL_FORMULA:
                            smallMoleculeEvidence.chemicalFormula(
                                checkString(column, target));
                            break;
                        case CHEMICAL_NAME:
                            smallMoleculeEvidence.chemicalName(checkString(column, target));
                            break;
                        case DATABASE_IDENTIFIER:
                            smallMoleculeEvidence.databaseIdentifier(checkString(column, target));
                            break;
                        case DERIVATIZED_FORM:
                            smallMoleculeEvidence.derivatizedForm(checkParameter(column, target));
                            break;
                        case EVIDENCE_UNIQUE_ID:
                            smallMoleculeEvidence.databaseIdentifier(checkString(column, target));
                            break;
                        case EXP_MASS_TO_CHARGE:
                            smallMoleculeEvidence.expMassToCharge(checkDouble(column, target));
                            break;
                        case IDENTIFICATION_METHOD:
                            smallMoleculeEvidence.identificationMethod(checkParameter(column, target));
                            break;
                        case INCHI:
                            smallMoleculeEvidence.inchi(checkString(column, target));
                            break;
                        case MS_LEVEL:
                            smallMoleculeEvidence.msLevel(checkParameter(column, target));
                            break;
                        case RANK:
                            smallMoleculeEvidence.rank(checkInteger(column, target));
                            break;
                        case SME_ID:
                            smallMoleculeEvidence.smeId(checkString(column, target));
                            break;
                        case SMILES:
                            smallMoleculeEvidence.smiles(checkString(column, target));
                            break;
                        case SPECTRA_REF:
                            smallMoleculeEvidence.spectraRef(checkSpectraRef(context, column, target));
                            break;
                        case THEORETICAL_MASS_TO_CHARGE:
                            smallMoleculeEvidence.theoreticalMassToCharge(checkDouble(column, target));
                            break;
                        case URI:
                            smallMoleculeEvidence.uri(checkURI(column, target).toASCIIString());
                            break;
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
                        smallMoleculeEvidence.addOptItem(optColMapping);
                   }
                }
            }
        }

        return physicalPosition;
    }

    @Override
    public SmallMoleculeEvidence getRecord() {

        if(smallMoleculeEvidence == null){
            smallMoleculeEvidence = new SmallMoleculeEvidence();//factory, metadata);
        }
        return smallMoleculeEvidence;
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
