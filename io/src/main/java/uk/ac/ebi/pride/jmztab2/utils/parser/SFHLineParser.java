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

import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.Parameter;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.model.Section;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Parse and validate Small Molecule Feature header line into a {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory}.
 *
 * @author nilshoffmann
 * @since 11/09/17
 * 
 */
public class SFHLineParser extends MZTabHeaderLineParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SFHLineParser.class);
    private Map<Integer, String> physPositionToOrder;


    /**
     * <p>Constructor for SFHLineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @param metadata a {@link de.isas.mztab2.model.Metadata} object.
     */
    public SFHLineParser(MZTabParserContext context, Metadata metadata) {
        super(context, MZTabColumnFactory.getInstance(Section.Small_Molecule_Feature_Header), metadata);
    }

    /** {@inheritDoc} */
    @Override
    protected int parseColumns() throws MZTabException {
        String header;
        Integer physicalPosition;

        ISmallMoleculeFeatureColumn column;
        SortedMap<String, IMZTabColumn> columnMapping = factory.getColumnMapping();
        SortedMap<String, IMZTabColumn> optionalMapping = factory.getOptionalColumnMapping();
        SortedMap<String, IMZTabColumn> stableMapping = factory.getStableColumnMapping();

        physPositionToOrder = generateHeaderPhysPositionToOrderMap(items);

        //Iterates through the tokens in the protein header
        //It will identify the type of column and the position accordingly
        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {

            column = null;
            header = items[physicalPosition];

            if (header.contains(MZTabConstants.ABUNDANCE_PREFIX)) {
                checkAbundanceColumns(physicalPosition, physPositionToOrder.get(physicalPosition));
            } else if (header.startsWith(MZTabConstants.OPT_PREFIX)) {
                checkOptColumnName(header);
            } else {
                try {
                    column = SmallMoleculeFeatureColumn.Stable.columnFor(header);
                } catch(IllegalArgumentException ex) {
                    throw new MZTabException(new MZTabError(LogicalErrorType.ColumnNotValid,lineNumber,header,section.getName()));
                }
                
            }

            if (column != null) {
                if (!column.getOrder().equals(physPositionToOrder.get(physicalPosition))) {
                    column.setOrder(physPositionToOrder.get(physicalPosition));
                    LOGGER.debug(column.toString());
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

    private Map<Integer, String> generateHeaderPhysPositionToOrderMap(String[] items) {
        Integer physicalPosition;
        Map<Integer, String> physicalPositionToOrder = new LinkedHashMap<>();
        int order = 0;

        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            if(physicalPositionToOrder.containsKey(physicalPosition)) {
                throw new IllegalArgumentException("Physical position "+physicalPosition+" for item "+items[physicalPosition-1]+" is already assigned!");
            }
            physicalPositionToOrder.put(physicalPosition, fromIndexToOrder(++order));
        }
        return physicalPositionToOrder;
    }

    /**
     * {@inheritDoc}
     *
     * The following optional columns are mandatory:
     * 1. abundance_assay[1-n]
     *
     * NOTICE: this method will be called at end of parse() function.
     * @see MZTabHeaderLineParser#parse(int, String, MZTabErrorList)
     * @see MZTabHeaderLineParser#parse(int, String, MZTabErrorList)
     */
    @Override
    protected void refine() throws MZTabException {

        //mandatory columns
        List<String> mandatoryColumnHeaders = new ArrayList<>();
        for(ISmallMoleculeFeatureColumn column: SmallMoleculeFeatureColumn.Stable.columns()) {
            mandatoryColumnHeaders.add(column.getName());
        }

        Parameter smallMoleculeFeatureQuantificationUnit = Optional.ofNullable(metadata.getSmallMoleculeFeatureQuantificationUnit()).orElseThrow(() -> 
            new MZTabException(new MZTabError(LogicalErrorType.NoSmallMoleculeFeatureQuantificationUnit, lineNumber)));

        for (String columnHeader : mandatoryColumnHeaders) {
            if (factory.findColumnByHeader(columnHeader) == null) {
                throw new MZTabException(new MZTabError(FormatErrorType.StableColumn, lineNumber, columnHeader));
            }
        }

        for (Assay assay : metadata.getAssay()) {
            String assayLabel = "_"+Metadata.Properties.assay+"[" + assay.getId() + "]";
            refineOptionalColumn(Section.Small_Molecule_Feature_Header, "abundance" + assayLabel);
        }
    }
}
