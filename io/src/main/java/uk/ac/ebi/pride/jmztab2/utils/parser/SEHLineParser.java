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
import uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab2.model.Section;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.SmallMoleculeEvidence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;

/**
 * Parse and validate Small Molecule Evidence header line into a {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory}.
 *
 * @author nilshoffmann
 * @since 11/09/17
 * 
 */
public class SEHLineParser extends MZTabHeaderLineParser {

    private static final Logger logger = LoggerFactory.getLogger(SEHLineParser.class);
    private Map<Integer, String> physPositionToOrder;


    /**
     * <p>Constructor for SEHLineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @param metadata a {@link de.isas.mztab2.model.Metadata} object.
     */
    public SEHLineParser(MZTabParserContext context, Metadata metadata) {
        super(context, MZTabColumnFactory.getInstance(Section.Small_Molecule_Evidence_Header), metadata);
    }

    /** {@inheritDoc} */
    @Override
    protected int parseColumns() throws MZTabException {
        String header;
        Integer physicalPosition;

        ISmallMoleculeColumn column;
        SortedMap<String, IMZTabColumn> columnMapping = factory.getColumnMapping();
        SortedMap<String, IMZTabColumn> optionalMapping = factory.getOptionalColumnMapping();
        SortedMap<String, IMZTabColumn> stableMapping = factory.getStableColumnMapping();

        physPositionToOrder = generateHeaderPhysPositionToOrderMap(items);

        //Iterates through the tokens in the small molecule evidence header
        //It will identify the type of column and the position accordingly
        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {

            column = null;
            header = items[physicalPosition];
            if (header.startsWith(SmallMoleculeEvidence.Properties.idConfidenceMeasure.getPropertyName())) {
                checkIdConfidenceMeasure(header);
            } else if (header.startsWith(MZTabConstants.OPT_PREFIX)) {
                checkOptColumnName(header);
            } else {
                try {
                    column = SmallMoleculeEvidenceColumn.Stable.columnFor(header);
                } catch(IllegalArgumentException ex) {
                    throw new MZTabException(new MZTabError(LogicalErrorType.ColumnNotValid,lineNumber,header,section.getName()));    
                }

            }

            if (column != null) {
                if (!column.getOrder().equals(physPositionToOrder.get(physicalPosition))) {
                    column.setOrder(physPositionToOrder.get(physicalPosition));
                    logger.debug(column.toString());
                }
                if(column.isOptional()){
                    optionalMapping.put(column.getLogicPosition(), column);
                } else {
                    stableMapping.put(column.getLogicPosition(), column);
                }
                columnMapping.put(column.getLogicPosition(), column);
            }
        }
        return physicalPosition;
    }

    private void checkIdConfidenceMeasure(String header) throws MZTabException {
        String valueLabel = header;
        
        Pattern pattern = Pattern.compile(SmallMoleculeEvidence.Properties.idConfidenceMeasure.getPropertyName()+MZTabConstants.REGEX_INDEXED_VALUE);
        Matcher matcher = pattern.matcher(valueLabel);
        if (!matcher.find()) {
            MZTabError error = new MZTabError(FormatErrorType.StableColumn, lineNumber, header);
            throw new MZTabException(error);
        }
        
        int id = parseIndex(header, matcher.group(1));
        Parameter p = metadata.getIdConfidenceMeasure().get(id-1);
        factory.addIdConfidenceMeasureColumn(p, id, Double.class);
    }

    private Map<Integer, String> generateHeaderPhysPositionToOrderMap(String[] items) {
        Integer physicalPosition;
        Map<Integer, String> physicalPositionToOrder = new LinkedHashMap<>();
        int order = 0;
        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            physicalPositionToOrder.put(physicalPosition, fromIndexToOrder(++order));
        }
        return physicalPositionToOrder;
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
            mandatoryColumnHeaders.add(SmallMoleculeEvidence.Properties.idConfidenceMeasure.getPropertyName()+"["+(i+1)+"]");
        });

        for (String columnHeader : mandatoryColumnHeaders) {
            if (factory.findColumnByHeader(columnHeader) == null) {
                throw new MZTabException(new MZTabError(FormatErrorType.StableColumn, lineNumber, columnHeader));
            }
        }
    }
}
