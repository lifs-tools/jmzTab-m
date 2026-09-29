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
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.model.Section;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Parse and validate Small Molecule Evidence header line into a {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory}.
 *
 * @author nilshoffmann
 * @since 11/09/17
 * 
 */
public class SEHLineParser extends MZTabHeaderLineParser {

    /**
     * <p>Constructor for SEHLineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     */
    public SEHLineParser(MZTabParserContext context, Metadata metadata) {
        super(context, MZTabColumnFactory.getInstance(Section.Small_Molecule_Evidence_Header), metadata);
    }

    /** {@inheritDoc} */
    @Override
    protected int parseColumns() throws MZTabException {
        String header;
        int physicalPosition;
        ISmallMoleculeColumn column;

        //Iterates through the tokens in the small molecule evidence header.
        //The 1-based physical position of each column is its order.
        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            column = null;
            header = items[physicalPosition];
            if (header.startsWith(SmallMoleculeEvidence.JSON_PROPERTY_ID_CONFIDENCE_MEASURE)) {
                checkIdConfidenceMeasure(header, physicalPosition);
            } else if (header.startsWith(MZTabConstants.OPT_PREFIX)) {
                checkOptColumnName(header, physicalPosition);
            } else {
                try {
                    column = SmallMoleculeEvidenceColumn.Stable.columnFor(header);
                } catch(IllegalArgumentException ex) {
                    throw new MZTabException(new MZTabError(LogicalErrorType.ColumnNotValid,lineNumber,header,section.getName()));
                }
            }

            if (column != null) {
                factory.addStableColumn(column, physicalPosition);
            }
        }
        return physicalPosition;
    }

    private void checkIdConfidenceMeasure(String header, int order) throws MZTabException {
        String valueLabel = header;

        Pattern pattern = Pattern.compile(SmallMoleculeEvidence.JSON_PROPERTY_ID_CONFIDENCE_MEASURE+MZTabConstants.REGEX_INDEXED_VALUE);
        Matcher matcher = pattern.matcher(valueLabel);
        if (!matcher.find()) {
            MZTabError error = new MZTabError(FormatErrorType.StableColumn, lineNumber, header);
            throw new MZTabException(error);
        }

        int id = parseIndex(header, matcher.group(1));
        List<Parameter> measures = metadata.getIdConfidenceMeasure();
        if (measures == null || id > measures.size()) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, header));
        }
        factory.addIdConfidenceMeasureColumn(measures.get(id - 1), id, Double.class, order);
    }

    /**
     * {@inheritDoc}
     *
     * The following optional columns are mandatory:
     * 1. id_confidence_measure[1-n]
     * 
     * NOTICE: this method will be called at end of parse() function.
     * @see MZTabHeaderLineParser#parse(int, String, MZTabErrorList)
     * @see MZTabHeaderLineParser#parse(int, String, MZTabErrorList)
     */
    @Override
    protected void refine() throws MZTabException {
        //mandatory columns
        List<String> mandatoryColumnHeaders = new ArrayList<>();
        for(ISmallMoleculeColumn col:SmallMoleculeEvidenceColumn.Stable.columns()) {
            mandatoryColumnHeaders.add(col.getName());
        }

        IntStream.range(0, metadata.getIdConfidenceMeasure().size()).
        forEachOrdered(i ->
        {
            mandatoryColumnHeaders.add(SmallMoleculeEvidence.JSON_PROPERTY_ID_CONFIDENCE_MEASURE+"["+(i+1)+"]");
        });

        for (String columnHeader : mandatoryColumnHeaders) {
            if (factory.findColumnByHeader(columnHeader) == null) {
                throw new MZTabException(new MZTabError(FormatErrorType.StableColumn, lineNumber, columnHeader));
            }
        }
    }
}
