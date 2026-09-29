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

import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.OptColumnMapping;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.pride.jmztab2.model.AbundanceColumn;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab2.model.MZBoolean;
import uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.model.OptionColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException;

/**
 * <p>
 * SMLLineParser class.</p>
 *
 * @author qingwei
 * @author nilshoffmann
 * @since 10/02/13
 *
 */
public class SMLLineParser extends MZTabDataLineParser<SmallMoleculeSummary> {

    private SmallMoleculeSummary smallMoleculeSummary;

    /**
     * <p>
     * Constructor for SMLLineParser.</p>
     *
     * @param context a
     * {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @param factory a {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory}
     * object.
     * @param positionMapping a
     * {@link uk.ac.ebi.pride.jmztab2.utils.parser.PositionMapping} object.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param errorList a
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList} object.
     */
    public SMLLineParser(MZTabParserContext context, MZTabColumnFactory factory,
        PositionMapping positionMapping,
        Metadata metadata, MZTabErrorList errorList) {
        super(context, factory, positionMapping, metadata, errorList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int checkData() {

        IMZTabColumn column;
        String columnName;
        String target;
        int physicalPosition;
        smallMoleculeSummary = new SmallMoleculeSummary();

        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            column = positionMapping.get(physicalPosition);

            if (column != null) {
                columnName = column.getName();
                target = items[physicalPosition];
                if (column instanceof ISmallMoleculeColumn) {
                    SmallMoleculeColumn.Stable stableColumn = SmallMoleculeColumn.Stable.
                        forName(columnName);
                    switch (stableColumn) {
                        case ADDUCT_IONS:
                            List<String> adductIons = checkStringList(
                                column, target, MZTabConstants.BAR);
                            checkRegexMatches(errorList, lineNumber,
                                SmallMoleculeSummary.JSON_PROPERTY_ADDUCT_IONS,
                                MZTabConstants.REGEX_ADDUCT, adductIons);
                            smallMoleculeSummary.adductIons(adductIons);
                            break;
                        case BEST_ID_CONFIDENCE_MEASURE:
                            smallMoleculeSummary.bestIdConfidenceMeasure(
                                checkParameter(column, target, true));
                            break;
                        case BEST_ID_CONFIDENCE_VALUE:
                            smallMoleculeSummary.bestIdConfidenceValue(
                                checkDouble(column, target));
                            break;
                        case CHEMICAL_FORMULA:
                            smallMoleculeSummary.chemicalFormula(
                                checkStringList(column, target,
                                    MZTabConstants.BAR));
                            break;
                        case CHEMICAL_NAME:
                            smallMoleculeSummary.chemicalName(checkStringList(
                                column, target, MZTabConstants.BAR));
                            break;
                        case DATABASE_IDENTIFIER:
                            smallMoleculeSummary.databaseIdentifier(
                                checkStringList(column, target,
                                    MZTabConstants.BAR));
                            break;
                        case INCHI:
                            smallMoleculeSummary.inchi(checkStringList(column,
                                target, MZTabConstants.BAR));
                            break;
                        case RELIABILITY:
                            smallMoleculeSummary.reliability(checkString(column,
                                target, false));
                            break;
                        case SMF_ID_REFS:
                            smallMoleculeSummary.smfIdRefs(checkIntegerList(
                                column, target, MZTabConstants.BAR));
                            break;
                        case SMILES:
                            smallMoleculeSummary.smiles(checkSmiles(column,
                                target));
                            break;
                        case SML_ID:
                            smallMoleculeSummary.smlId(checkInteger(column,
                                target, false));
                            break;
                        case THEOR_NEUTRAL_MASS:
                            smallMoleculeSummary.theoreticalNeutralMass(
                                checkDoubleList(column, target));
                            break;
                        case URI:
                            smallMoleculeSummary.uri(
                                checkUriList(column, target,
                                    MZTabConstants.BAR));
                            break;

                    }

                } else if (column instanceof AbundanceColumn) {
                    if (columnName.startsWith(
                        SmallMoleculeSummary.JSON_PROPERTY_ABUNDANCE_ASSAY)) {
                        smallMoleculeSummary.addAbundanceAssayItem(checkDouble(
                            column, target));
                    } else if (columnName.startsWith(
                        SmallMoleculeSummary.JSON_PROPERTY_ABUNDANCE_STUDY_VARIABLE)) {
                        smallMoleculeSummary.addAbundanceStudyVariableItem(
                            checkDouble(column, target));
                    } else if (columnName.startsWith(
                        SmallMoleculeSummary.JSON_PROPERTY_ABUNDANCE_VARIATION_STUDY_VARIABLE)) {
                        smallMoleculeSummary.
                            addAbundanceVariationStudyVariableItem(checkDouble(
                                column, target));
                    }
                } else if (column instanceof OptionColumn) {
                    if (columnName.startsWith(MZTabConstants.OPT_PREFIX)) {
                        Class dataType = column.getDataType();
                        OptColumnMapping optColMapping = new OptColumnMapping();
                        optColMapping.identifier(columnName.substring(
                            MZTabConstants.OPT_PREFIX.length()));
                        if (dataType.equals(String.class)) {
                            optColMapping.value(checkString(column, target));
                        } else if (dataType.equals(Double.class)) {
                            optColMapping.value(Double.toString(checkDouble(
                                column, target)));
                        } else if (dataType.equals(MZBoolean.class)) {
                            optColMapping.value(Boolean.toString(checkMZBoolean(
                                column, target).
                                toBoolean()));
                        }
                        smallMoleculeSummary.addOptItem(optColMapping);
                    }
                }
            }
        }

        checkItemNumbers(errorList, lineNumber, smallMoleculeSummary.
            getDatabaseIdentifier(),
            SmallMoleculeSummary.JSON_PROPERTY_DATABASE_IDENTIFIER,
            smallMoleculeSummary.getChemicalFormula(),
            SmallMoleculeSummary.JSON_PROPERTY_CHEMICAL_FORMULA);
        checkItemNumbers(errorList, lineNumber, smallMoleculeSummary.
            getDatabaseIdentifier(),
            SmallMoleculeSummary.JSON_PROPERTY_DATABASE_IDENTIFIER,
            smallMoleculeSummary.getSmiles(),
            SmallMoleculeSummary.JSON_PROPERTY_SMILES);
        checkItemNumbers(errorList, lineNumber, smallMoleculeSummary.
            getDatabaseIdentifier(),
            SmallMoleculeSummary.JSON_PROPERTY_DATABASE_IDENTIFIER,
            smallMoleculeSummary.getInchi(),
            SmallMoleculeSummary.JSON_PROPERTY_INCHI);
        checkItemNumbers(errorList, lineNumber, smallMoleculeSummary.
            getDatabaseIdentifier(),
            SmallMoleculeSummary.JSON_PROPERTY_DATABASE_IDENTIFIER,
            smallMoleculeSummary.getChemicalName(),
            SmallMoleculeSummary.JSON_PROPERTY_CHEMICAL_NAME);
        checkItemNumbers(errorList, lineNumber, smallMoleculeSummary.
            getDatabaseIdentifier(),
            SmallMoleculeSummary.JSON_PROPERTY_DATABASE_IDENTIFIER,
            smallMoleculeSummary.getUri(),
            SmallMoleculeSummary.JSON_PROPERTY_URI);
        checkItemNumbers(errorList, lineNumber, smallMoleculeSummary.
            getDatabaseIdentifier(),
            SmallMoleculeSummary.JSON_PROPERTY_DATABASE_IDENTIFIER,
            smallMoleculeSummary.getTheoreticalNeutralMass(),
            SmallMoleculeSummary.JSON_PROPERTY_THEORETICAL_NEUTRAL_MASS);
        return physicalPosition;
    }

    protected void checkRegexMatches(MZTabErrorList errorList, int lineNumber,
        String elementPropertyName,
        String regularExpression, List<String> elements) {
        if (!elements.isEmpty()) {
            Pattern p = Pattern.compile(regularExpression);
            for (int i = 0; i < elements.size(); i++) {
                String element = elements.get(i);
                if(!"null".equals(element)) {
                    Matcher m = p.matcher(element);
                    if (!m.matches()) {
                        errorList.add(new MZTabError(FormatErrorType.RegexMismatch,
                            lineNumber, elementPropertyName, element,
                            "" + (i + 1), regularExpression));
                    }
                }
            }
        }

    }

    protected void checkItemNumbers(MZTabErrorList errorList, int lineNumber,
        List<?> reference, String referencePropertyName,
        List<?> toCheck, String toCheckPropertyName) throws MZTabErrorOverflowException {
        //check that array types have same element number
        if (!toCheck.isEmpty() && reference.size() != toCheck.size()) {
            errorList.add(new MZTabError(LogicalErrorType.ItemNumberMismatch,
                lineNumber, toCheckPropertyName, "" + toCheck.
                size(), referencePropertyName, "" + reference.
                size()));
        }
    }

    /**
     * <p>
     * getRecord.</p>
     *
     * @return a {@link org.lifstools.mztab2.model.SmallMoleculeSummary} object.
     */
    @Override
    public SmallMoleculeSummary getRecord() {

        if (smallMoleculeSummary == null) {
            smallMoleculeSummary = new SmallMoleculeSummary();
        }
        return smallMoleculeSummary;
    }
}
