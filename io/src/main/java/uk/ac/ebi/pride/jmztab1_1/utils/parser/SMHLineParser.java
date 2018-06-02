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
package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab1_1.model.ISmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab1_1.model.Section;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.LogicalErrorType;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.StudyVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeColumn.Stable;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;

/**
 * Parse and validate Small Molecule header line into a {@link uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory}.
 *
 * @author qingwei
 * @author ntoro
 * @since 10/02/13
 * 
 */
public class SMHLineParser extends MZTabHeaderLineParser {

    private static final Logger logger = LoggerFactory.getLogger(SMHLineParser.class);
    private Map<Integer, String> physPositionToOrder;


    /**
     * <p>Constructor for SMHLineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab1_1.utils.parser.MZTabParserContext} object.
     * @param metadata a {@link de.isas.mztab1_1.model.Metadata} object.
     */
    public SMHLineParser(MZTabParserContext context, Metadata metadata) {
        super(context, MZTabColumnFactory.getInstance(Section.Small_Molecule_Header), metadata);
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

        //Iterates through the tokens in the protein header
        //It will identify the type of column and the position accordingly
        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {

            column = null;
            header = items[physicalPosition];
            if (header.startsWith("abundance_")) {
                checkAbundanceColumns(physicalPosition, physPositionToOrder.get(physicalPosition));
            } else if (header.startsWith(MZTabConstants.OPT_PREFIX)) {
                checkOptColumnName(header);
            } else {
                try {
                    column = SmallMoleculeColumn.Stable.forName(header);
                } catch(IllegalArgumentException iae) {
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
     * 1. abundance_assay[1-n]
     * 2. abundance_study_variable[1-m]
     * 3. abundance_coeffvar_study_variable[1-m]
     * 
     * NOTICE: this method will be called at end of parse() function.
     * @see MZTabHeaderLineParser#parse(int, String, MZTabErrorList)
     * @see MZTabHeaderLineParser#parse(int, String, MZTabErrorList)
     * @see #refineOptionalColumn(java.lang.String)
     */
    @Override
    protected void refine() throws MZTabException {

        for (Stable columnHeader : SmallMoleculeColumn.Stable.values()) {
            if (factory.findColumnByHeader(columnHeader.getHeader()) == null) {
                throw new MZTabException(new MZTabError(FormatErrorType.StableColumn, lineNumber, columnHeader.getHeader()));
            }
        }

        if (metadata.getSmallMoleculeQuantificationUnit() == null) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NoSmallMoleculeQuantificationUnit, lineNumber));
        }

        if (metadata.getSmallMoleculeIdentificationReliability() == null) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NoSmallMoleculeIdentificationReliability, lineNumber));
        }
        for (StudyVariable studyVariable : metadata.getStudyVariable()) {
            String svLabel = "_study_variable[" + studyVariable.getId() + "]";
            refineOptionalColumn(Section.Small_Molecule_Header, "abundance" + svLabel);
            refineOptionalColumn(Section.Small_Molecule_Header, "abundance_variation" + svLabel);
        }
        for (Assay assay : metadata.getAssay()) {
            String assayLabel = "_assay[" + assay.getId() + "]";
            refineOptionalColumn(Section.Small_Molecule_Header, "abundance" + assayLabel);
        }
    }
}
