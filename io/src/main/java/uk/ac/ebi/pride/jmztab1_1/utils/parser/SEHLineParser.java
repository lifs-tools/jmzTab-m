package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import uk.ac.ebi.pride.jmztab1_1.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab1_1.model.ISmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab1_1.model.Section;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.LogicalErrorType;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;

/**
 * Parse and validate Small Molecule Evidence header line into a {@link MZTabColumnFactory}.
 *
 * @author nils.hoffmann
 * @since 11/09/17
 */
public class SEHLineParser extends MZTabHeaderLineParser {

    private static Logger logger = LoggerFactory.getLogger(SEHLineParser.class);
    private Map<Integer, String> physPositionToOrder;


    public SEHLineParser(MZTabParserContext context, Metadata metadata) {
        super(context, MZTabColumnFactory.getInstance(Section.Small_Molecule_Evidence_Header), metadata);
    }

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
            if (header.startsWith("id_confidence_measure")) {
                checkIdConfidenceMeasure(header);
            } else if (header.startsWith("opt_")) {
                checkOptColumnName(header);
            } else {
                try {
                    column = SmallMoleculeEvidenceColumn.Stable.forName(header);
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
        
        Pattern pattern = Pattern.compile("id_confidence_measure\\[(\\d+)\\]");
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
        Map<Integer, String> physicalPositionToOrder = new LinkedHashMap<Integer, String>();
        int order = 0;
        boolean firstBSES = true; //BEST_SEARCH_ENGINE_SCORE
        boolean firstSES = true;  //SEARCH_ENGINE_SCORE
        String columnHeader;

        for (physicalPosition = 1; physicalPosition < items.length; physicalPosition++) {

            columnHeader = items[physicalPosition];

//            if (columnHeader.startsWith(BEST_SEARCH_ENGINE_SCORE.getName())) {
//                //We assume that columns which start with the same name they are contiguous in the table
//                if (firstBSES) {
//                    physicalPositionToOrder.put(physicalPosition, fromIndexToOrder(++order));
//                    firstBSES = false;
//                } else {
//                    physicalPositionToOrder.put(physicalPosition, fromIndexToOrder(order));
//                }
//            } else if (columnHeader.startsWith(SEARCH_ENGINE_SCORE.getName())) {
//                if (firstSES) {
//                    physicalPositionToOrder.put(physicalPosition, fromIndexToOrder(++order));
//                    firstSES = false;
//                } else {
//                    physicalPositionToOrder.put(physicalPosition, fromIndexToOrder(order));
//                }
//            } else {
                physicalPositionToOrder.put(physicalPosition, fromIndexToOrder(++order));
//            }
        }
        return physicalPositionToOrder;
    }

//    private void addBestSearchEngineScoreColumn(String header, Integer physicalPosition) throws MZTabException {
//        Pattern pattern = Pattern.compile("best_search_engine_score\\[(\\d+)\\]");
//        Matcher matcher = pattern.matcher(header);
//        SmallMoleculeColumn column;
//        int id;
//
//        if (matcher.find()) {
//            id = parseIndex(header, matcher.group(1));
//            if (!metadata.getSmallMoleculeSearchEngineScoreMap().containsKey(id)) {
//                throw new MZTabException(new MZTabError(LogicalErrorType.SmallMoleculeSearchEngineScoreNotDefined, lineNumber, header));
//            } else {
//                column = BEST_SEARCH_ENGINE_SCORE;
//                column.setOrder(physPositionToOrder.get(physicalPosition));
//                factory.addBestSearchEngineScoreOptionalColumn(column, id);
//            }
//        }
//    }

//    private void addSearchEngineScoreColumn(String header, Integer physicalPosition) throws MZTabException {
//        Pattern pattern = Pattern.compile("search_engine_score\\[(\\d+)\\]_ms_run\\[(\\d+)\\]");
//        Matcher matcher = pattern.matcher(header);
//        SmallMoleculeColumn column;
//
//        if (matcher.find()) {
//            Integer score_id = parseIndex(header, matcher.group(1));
//            Integer ms_run_id = parseIndex(header, matcher.group(2));
//            MsRun msRun = metadata.getMsRunMap().get(ms_run_id);
//
//            if (msRun == null) {
//                throw new MZTabException(new MZTabError(LogicalErrorType.MsRunNotDefined, lineNumber, header));
//            } else if (!metadata.getSmallMoleculeSearchEngineScoreMap().containsKey(score_id)) {
//                throw new MZTabException(new MZTabError(LogicalErrorType.SmallMoleculeSearchEngineScoreNotDefined, lineNumber, header + "_ms_run[" + msRun.getId() + "]"));
//            } else {
//                column = SEARCH_ENGINE_SCORE;
//                column.setOrder(physPositionToOrder.get(physicalPosition));
//                factory.addSearchEngineScoreOptionalColumn(column, score_id, msRun);
//            }
//        }
//    }

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
//        MZTabDescription.Mode mode = metadata.getMZTabMode();
//        MZTabDescription.Type type = metadata.getMZTabType();

        //mandatory columns
        List<String> mandatoryColumnHeaders = new ArrayList<String>();
        for(ISmallMoleculeColumn col:SmallMoleculeEvidenceColumn.Stable.values()) {
            mandatoryColumnHeaders.add(col.getName());
        }

        IntStream.range(0, metadata.getIdConfidenceMeasure().size()).
        forEachOrdered(i ->
        {
            mandatoryColumnHeaders.add("id_confidence_measure["+(i+1)+"]");
        });

        for (String columnHeader : mandatoryColumnHeaders) {
            if (factory.findColumnByHeader(columnHeader) == null) {
                throw new MZTabException(new MZTabError(FormatErrorType.StableColumn, lineNumber, columnHeader));
            }
        }

        //smallmolecule_search_engine_score
//        if (metadata.getSmallMoleculeSearchEngineScoreMap().size() == 0) {
//            throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "smallmolecule_search_engine_score[1-n]", mode.toString(), type.toString()));
//        }

        //Mandatory in all modes
//        for (SearchEngineScore searchEngineScore : metadata.getSmallMoleculeSearchEngineScoreMap().values()) {
//            String searchEngineScoreLabel = "[" + searchEngineScore.getId() + "]";
//            refineOptionalColumn(mode, type, "best_search_engine_score" + searchEngineScoreLabel);
//        }
    }
}
