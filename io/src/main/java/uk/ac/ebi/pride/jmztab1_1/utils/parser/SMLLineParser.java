package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab1_1.model.ISmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab1_1.model.MZBoolean;
import uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab1_1.model.OptionColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab1_1.model.AbundanceColumn;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.SmallMoleculeSummary;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;


/**
 * <p>SMLLineParser class.</p>
 *
 * @author qingwei
 * @since 10/02/13
 * 
 */
public class SMLLineParser extends MZTabDataLineParser<SmallMoleculeSummary> {

    private SmallMoleculeSummary smallMoleculeSummary;

    /**
     * <p>Constructor for SMLLineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab1_1.utils.parser.MZTabParserContext} object.
     * @param factory a {@link uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory} object.
     * @param positionMapping a {@link uk.ac.ebi.pride.jmztab1_1.utils.parser.PositionMapping} object.
     * @param metadata a {@link de.isas.mztab1_1.model.Metadata} object.
     * @param errorList a {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList} object.
     */
    public SMLLineParser(MZTabParserContext context, MZTabColumnFactory factory, PositionMapping positionMapping,
                         Metadata metadata, MZTabErrorList errorList) {
        super(context, factory, positionMapping, metadata, errorList);
    }

    /** {@inheritDoc} */
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
                            smallMoleculeSummary.bestIdConfidenceMeasure(checkParameter(column, target, true));
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
                        case INCHI:
                            smallMoleculeSummary.inchi(checkStringList(column, target, MZTabConstants.BAR));
                            break;
                        case RELIABILITY:
                            smallMoleculeSummary.reliability(checkString(column, target));
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

    /**
     * <p>getRecord.</p>
     *
     * @return a {@link de.isas.mztab1_1.model.SmallMoleculeSummary} object.
     */
    public SmallMoleculeSummary getRecord() {

        if(smallMoleculeSummary == null){
            smallMoleculeSummary = new SmallMoleculeSummary();//(factory, metadata);
        }
        return smallMoleculeSummary;
    }
}
