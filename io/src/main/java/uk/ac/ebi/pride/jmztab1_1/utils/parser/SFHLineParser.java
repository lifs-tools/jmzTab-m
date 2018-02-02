package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab1_1.model.ISmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab1_1.model.ISmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab1_1.model.Section;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.LogicalErrorType;
import de.isas.mztab1_1.model.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeFeatureColumn.*;

/**
 * Parse and validate Small Molecule Feature header line into a {@link MZTabColumnFactory}.
 *
 * @author nils.hoffmann
 * @since 11/09/17
 */
public class SFHLineParser extends MZTabHeaderLineParser {

    private static Logger logger = LoggerFactory.getLogger(SFHLineParser.class);
    private Map<Integer, String> physPositionToOrder;


    public SFHLineParser(MZTabParserContext context, Metadata metadata) {
        super(context, MZTabColumnFactory.getInstance(Section.Small_Molecule_Feature_Header), metadata);
    }

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

            if (header.contains("abundance_assay")) {
                checkAbundanceColumns(physicalPosition, physPositionToOrder.get(physicalPosition));
            } else if (header.contains("abundance_study_variable")) {
                checkAbundanceColumns(physicalPosition, physPositionToOrder.get(physicalPosition));
                // stdev_study_variable and std_error_will be process inside
            } else if (header.contains("abundance_stdev") || header.contains("abundance_std_error")) {
                // ignore then, they have been process....
            } else if (header.startsWith("opt_")) {
                checkOptColumnName(header);
            } else {
                try {
                    column = SmallMoleculeFeatureColumn.Stable.forName(header);
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

    private Map<Integer, String> generateHeaderPhysPositionToOrderMap(String[] items) {
        Integer physicalPosition;
        Map<Integer, String> physicalPositionToOrder = new LinkedHashMap<Integer, String>();
        int order = 0;

        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {
            physicalPositionToOrder.put(physicalPosition, fromIndexToOrder(++order));
        }
        return physicalPositionToOrder;
    }

    /**
     * In "Quantification" file, following optional columns are mandatory:
     * 1. smallmolecule_abundance_study_variable[1-n]
     * 2. smallmolecule_abundance_stdev_study_variable[1-n]
     * 3. smallmolecule_abundance_std_error_study_variable[1-n]
     * <p/>
     * Beside above, in "Complete" and "Quantification" file, following optional columns also mandatory:
     * 1. search_engine_score_ms_run[1-n]
     * <p/>
     * NOTICE: this hock method will be called at end of parse() function.
     *
     * @see MZTabHeaderLineParser#parse(int, String, MZTabErrorList)
     * @see #refineOptionalColumn(MZTabDescription.Mode, MZTabDescription.Type, String)
     */
    @Override
    protected void refine() throws MZTabException {

        //mandatory columns
        List<String> mandatoryColumnHeaders = new ArrayList<String>();
        for(ISmallMoleculeFeatureColumn column: SmallMoleculeFeatureColumn.Stable.values()) {
            mandatoryColumnHeaders.add(column.getName());
        }

        for (String columnHeader : mandatoryColumnHeaders) {
            if (factory.findColumnByHeader(columnHeader) == null) {
                throw new MZTabException(new MZTabError(FormatErrorType.StableColumn, lineNumber, columnHeader));
            }
        }
    }
}
