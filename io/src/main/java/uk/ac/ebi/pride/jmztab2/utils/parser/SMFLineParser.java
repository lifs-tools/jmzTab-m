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
import uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.model.MZBoolean;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab2.model.OptionColumn;
import uk.ac.ebi.pride.jmztab2.model.AbundanceColumn;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.OptColumnMapping;
import de.isas.mztab2.model.SmallMoleculeFeature;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;


/**
 * <p>SMFLineParser class.</p>
 *
 * @author nilshoffmann
 * @since 11/09/17
 * 
 */
public class SMFLineParser extends MZTabDataLineParser<SmallMoleculeFeature> {

    private SmallMoleculeFeature smallMoleculeFeature;

    /**
     * <p>Constructor for SMFLineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @param factory a {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory} object.
     * @param positionMapping a {@link uk.ac.ebi.pride.jmztab2.utils.parser.PositionMapping} object.
     * @param metadata a {@link de.isas.mztab2.model.Metadata} object.
     * @param errorList a {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList} object.
     */
    public SMFLineParser(MZTabParserContext context, MZTabColumnFactory factory, PositionMapping positionMapping,
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
        smallMoleculeFeature = new SmallMoleculeFeature();

        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            logicalPosition = positionMapping.get(physicalPosition);
            column = factory.getColumnMapping().get(logicalPosition);

            if (column != null) {
                columnName = column.getName();
                target = items[physicalPosition];
                if (column instanceof ISmallMoleculeFeatureColumn) {
                    SmallMoleculeFeatureColumn.Stable stableColumn = SmallMoleculeFeatureColumn.Stable.forName(columnName);
                    switch(stableColumn) {
                        case ADDUCT_ION:
                            smallMoleculeFeature.adductIon(checkString(column,
                                target));
                            break;
                        case CHARGE:
                            smallMoleculeFeature.charge(checkInteger(column,
                                target));
                            break;
                        case EXP_MASS_TO_CHARGE:
                            smallMoleculeFeature.expMassToCharge(
                                checkDouble(column, target));
                            break;
                        case ISOTOPOMER:
                            smallMoleculeFeature.isotopomer(checkParameter(
                                column, target, true));
                            break;
                        case RETENTION_TIME_IN_SECONDS:
                            smallMoleculeFeature.retentionTimeInSeconds(checkDouble(
                                column, target));
                            break;
                        case RETENTION_TIME_IN_SECONDS_END:
                            smallMoleculeFeature.retentionTimeInSecondsEnd(checkDouble(
                                column, target));
                            break;
                        case RETENTION_TIME_IN_SECONDS_START:
                            smallMoleculeFeature.retentionTimeInSecondsStart(checkDouble(
                                column, target));
                            break;
                        case SME_ID_REFS:
                            smallMoleculeFeature.smeIdRefs(checkIntegerList(
                                column, target, MZTabConstants.BAR));
                            break;
                        case SME_ID_REF_AMBIGUITY_CODE:
                            smallMoleculeFeature.smeIdRefAmbiguityCode(checkInteger(
                                column, target));
                            break;
                        case SMF_ID:
                            smallMoleculeFeature.smfId(checkInteger(
                                column, target));
                            break;
                    }

                } else if (column instanceof AbundanceColumn) {
                    if (columnName.startsWith(SmallMoleculeFeature.Properties.abundanceAssay.getPropertyName())) {
                        smallMoleculeFeature.addAbundanceAssayItem(checkDouble(column, target));
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
                        smallMoleculeFeature.addOptItem(optColMapping);
                    }
                }
            }
        }

        return physicalPosition;
    }

    /** {@inheritDoc} */
    @Override
    public SmallMoleculeFeature getRecord() {

        if(smallMoleculeFeature == null){
            smallMoleculeFeature = new SmallMoleculeFeature();//(factory, metadata);
        }
        return smallMoleculeFeature;
    }
}
