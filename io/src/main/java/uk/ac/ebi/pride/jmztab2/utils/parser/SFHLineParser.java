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

import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.Parameter;
import java.util.*;
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

    /**
     * <p>Constructor for SFHLineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     */
    public SFHLineParser(MZTabParserContext context, Metadata metadata) {
        super(context, MZTabColumnFactory.getInstance(Section.Small_Molecule_Feature_Header), metadata);
    }

    /** {@inheritDoc} */
    @Override
    protected int parseColumns() throws MZTabException {
        String header;
        int physicalPosition;
        ISmallMoleculeFeatureColumn column;

        //Iterates through the tokens in the small molecule feature header.
        //The 1-based physical position of each column is its order.
        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            column = null;
            header = items[physicalPosition];
            if (header.startsWith(MZTabConstants.ABUNDANCE_PREFIX)) {
                checkAbundanceColumns(physicalPosition, physicalPosition);
            } else if (header.startsWith(MZTabConstants.OPT_PREFIX)) {
                checkOptColumnName(header, physicalPosition);
            } else {
                try {
                    column = SmallMoleculeFeatureColumn.Stable.columnFor(header);
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
            String assayLabel = "_"+Metadata.JSON_PROPERTY_ASSAY+"[" + assay.getId() + "]";
            refineOptionalColumn(Section.Small_Molecule_Feature_Header, "abundance" + assayLabel);
        }
    }
}
