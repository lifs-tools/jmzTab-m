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
package uk.ac.ebi.pride.jmztab2.utils.parser;

import uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab2.model.MZBoolean;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn.Stable;
import uk.ac.ebi.pride.jmztab2.model.OptionColumn;
import uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeEvidenceColumn;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.OptColumnMapping;
import de.isas.mztab2.model.SmallMoleculeEvidence;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;


/**
 * <p>SMELineParser class.</p>
 *
 * @author nilshoffmann
 * @since 11/09/17
 * 
 */
public class SMELineParser extends MZTabDataLineParser<SmallMoleculeEvidence> {

    private SmallMoleculeEvidence smallMoleculeEvidence;

    /**
     * <p>Constructor for SMELineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @param factory a {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory} object.
     * @param positionMapping a {@link uk.ac.ebi.pride.jmztab2.utils.parser.PositionMapping} object.
     * @param metadata a {@link de.isas.mztab2.model.Metadata} object.
     * @param errorList a {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList} object.
     */
    public SMELineParser(MZTabParserContext context, MZTabColumnFactory factory, PositionMapping positionMapping,
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
        smallMoleculeEvidence = new SmallMoleculeEvidence();

        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            logicalPosition = positionMapping.get(physicalPosition);
            column = factory.getColumnMapping().get(logicalPosition);

            if (column != null) {
                columnName = column.getName();
                target = items[physicalPosition];
                if (column instanceof ISmallMoleculeEvidenceColumn) {
                    Stable stableColumn = SmallMoleculeEvidenceColumn.Stable.forName(columnName);
                    switch(stableColumn) {
                        case ADDUCT_ION:
                            smallMoleculeEvidence.adductIon(checkString(column,
                                target));
                            break;
                        case CHARGE:
                            smallMoleculeEvidence.charge(checkInteger(column,
                                checkData(column, target, false)));
                            break;
                        case CHEMICAL_FORMULA:
                            smallMoleculeEvidence.chemicalFormula(
                                checkString(column, target));
                            break;
                        case CHEMICAL_NAME:
                            smallMoleculeEvidence.chemicalName(checkString(column, target));
                            break;
                        case DATABASE_IDENTIFIER:
                            smallMoleculeEvidence.databaseIdentifier(checkString(column, target, true));
                            break;
                        case DERIVATIZED_FORM:
                            smallMoleculeEvidence.derivatizedForm(checkParameter(column, target, true));
                            break;
                        case EVIDENCE_INPUT_ID:
                            smallMoleculeEvidence.evidenceInputId(checkString(column, target, false));
                            break;
                        case EXP_MASS_TO_CHARGE:
                            smallMoleculeEvidence.expMassToCharge(checkDouble(column, target, false));
                            break;
                        case IDENTIFICATION_METHOD:
                            smallMoleculeEvidence.identificationMethod(checkParameter(column, target, false));
                            break;
                        case INCHI:
                            smallMoleculeEvidence.inchi(checkString(column, target));
                            break;
                        case MS_LEVEL:
                            smallMoleculeEvidence.msLevel(checkParameter(column, target, false));
                            break;
                        case RANK:
                            smallMoleculeEvidence.rank(checkInteger(column, target, false));
                            break;
                        case SME_ID:
                            smallMoleculeEvidence.smeId(checkInteger(column, target, false));
                            break;
                        case SMILES:
                            smallMoleculeEvidence.smiles(checkString(column, target));
                            break;
                        case SPECTRA_REF:
                            smallMoleculeEvidence.spectraRef(checkSpectraRef(context, column, target, false));
                            break;
                        case THEORETICAL_MASS_TO_CHARGE:
                            smallMoleculeEvidence.theoreticalMassToCharge(checkDouble(column, target, false));
                            break;
                        case URI:
                            smallMoleculeEvidence.uri(checkURI(column, target));
                            break;
                    }

                } else if (column instanceof OptionColumn) {
                   if (columnName.startsWith(MZTabConstants.OPT_PREFIX)) {
                        Class dataType = column.getDataType();
                        OptColumnMapping optColMapping = new OptColumnMapping();
                        optColMapping.identifier(columnName.substring(MZTabConstants.OPT_PREFIX.length()));
                        if (dataType.equals(String.class)) {
                            optColMapping.value(checkString(column, target));
                        } else if (dataType.equals(Double.class)) {
                            optColMapping.value(Double.toString(checkDouble(column, target)));
                        } else if (dataType.equals(MZBoolean.class)) {
                            optColMapping.value(Boolean.toString(checkMZBoolean(column, target).toBoolean()));
                        }
                        smallMoleculeEvidence.addOptItem(optColMapping);
                   }
                } else if (column.getName().equals(SmallMoleculeEvidence.Properties.idConfidenceMeasure.getPropertyName())) {
                    smallMoleculeEvidence.addIdConfidenceMeasureItem(checkDouble(column, target));
                }
            }
        }

        return physicalPosition;
    }

    /** {@inheritDoc} */
    @Override
    public SmallMoleculeEvidence getRecord() {

        if(smallMoleculeEvidence == null){
            smallMoleculeEvidence = new SmallMoleculeEvidence();
        }
        return smallMoleculeEvidence;
    }
}
