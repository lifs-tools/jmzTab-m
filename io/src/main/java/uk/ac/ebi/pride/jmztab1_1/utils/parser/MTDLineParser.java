package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import uk.ac.ebi.pride.jmztab1_1.model.MetadataProperty;
import uk.ac.ebi.pride.jmztab1_1.model.SplitList;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataSubElement;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataElement;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorOverflowException;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.IndexedElement;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.PublicationItem;
import de.isas.mztab1_1.model.Sample;
import de.isas.mztab1_1.model.SampleProcessing;
import de.isas.mztab1_1.model.Software;
import de.isas.mztab1_1.model.StudyVariable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.ac.ebi.pride.jmztab1_1.model.MZTabUtils.*;

/**
 * Parse a metadata line  into a element.
 * Metadata Element start with MTD, its structure like:
 * MTD  {@link MetadataElement}([id])(-{@link MetadataSubElement}[pid])(-{@link MetadataProperty})    {Element Value}
 *
 * @see MetadataElement
 * @see MetadataSubElement
 * @see MetadataProperty
 *
 * @author qingwei
 * @since 08/02/13
 */
public class MTDLineParser extends MZTabLineParser {
    private static final String Error_Header = "MTD\t";

    private Metadata metadata = new Metadata();

    public MTDLineParser(MZTabParserContext context) {
        super(context);
    }

    /**
     * Most of time, we use {@link #parseNormalMetadata(String, String)} to parse defineLabel into
     * Metadata Element.
     */
    public void parse(int lineNumber, String mtdLine, MZTabErrorList errorList) throws MZTabException {
        super.parse(lineNumber, mtdLine, errorList);

        if (items.length != 3) {
            MZTabError error = new MZTabError(FormatErrorType.MTDLine, lineNumber, mtdLine);
            throw new MZTabException(error);
        }

        String defineLabel = items[1].trim().toLowerCase();
        String valueLabel = items[2].trim();

        parseNormalMetadata(defineLabel, valueLabel);
    }

    /**
     * Parse valueLabel based on email format. If exists parse error, add it into {@link MZTabErrorList}.
     */
    private String checkEmail(String defineLabel, String valueLabel) {
        String email = parseEmail(valueLabel);

        if (email == null) {
            errorList.add(new MZTabError(FormatErrorType.Email, lineNumber, Error_Header + defineLabel, valueLabel));
        }

        return email;
    }

    /**
     * Parse {@link MetadataProperty} which depend on the {@link MetadataElement}.
     * If exists parse error, stop validate and throw {@link MZTabException} directly.
     */
    private MetadataProperty checkProperty(MetadataElement element, String propertyName) throws MZTabException {
        if (isEmpty(propertyName)) {
            return null;
        }

        MetadataProperty property = MetadataProperty.findProperty(element, propertyName);
        if (property == null) {
            MZTabError error = new MZTabError(FormatErrorType.MTDDefineLabel, lineNumber, element.getName() + "-" + propertyName);
            throw new MZTabException(error);
        }

        return property;
    }

    /**
     * Parse {@link MetadataProperty} which depend on the {@link MetadataSubElement}
     * If exists parse error, stop validate and throw {@link MZTabException} directly.
     */
    private MetadataProperty checkProperty(MetadataSubElement subElement, String propertyName) throws MZTabException {
        if (isEmpty(propertyName)) {
            return null;
        }

        MetadataProperty property = MetadataProperty.findProperty(subElement, propertyName);
        if (property == null) {
            MZTabError error = new MZTabError(FormatErrorType.MTDDefineLabel, lineNumber, subElement.getName() + "-" + propertyName);
            throw new MZTabException(error);
        }

        return property;
    }

    /**
     * Parse valueLabel to {@link MZTabDescription.Mode}
     * If exists parse error, stop validate and throw {@link MZTabException} directly.
     */
//    private MZTabDescription.Mode checkMZTabMode(String defineLabel, String valueLabel) throws MZTabException {
//        try {
//            return MZTabDescription.Mode.valueOf(valueLabel);
//        } catch (IllegalArgumentException e) {
//            MZTabError error = new MZTabError(FormatErrorType.MZTabMode, lineNumber, Error_Header + defineLabel, valueLabel);
//            throw new MZTabException(error);
//        }
//    }

    /**
     * Parse valueLabel to {@link MZTabDescription.Mode}
     * If exists parse error, stop validate and throw {@link MZTabException} directly.
     */
//    private MZTabDescription.Type checkMZTabType(String defineLabel, String valueLabel) throws MZTabException {
//        try {
//            return MZTabDescription.Type.valueOf(valueLabel);
//        } catch (IllegalArgumentException e) {
//            MZTabError error = new MZTabError(FormatErrorType.MZTabType, lineNumber, Error_Header + defineLabel, valueLabel);
//            throw new MZTabException(error);
//        }
//    }

    /**
     * Parse valueLabel to {@link Parameter}
     * If exists parse error, add it into {@link MZTabErrorList}
     */
    private Parameter checkParameter(String defineLabel, String valueLabel) {
        Parameter param = parseParam(valueLabel);
        if (param == null) {
            errorList.add(new MZTabError(FormatErrorType.Param, lineNumber, Error_Header + defineLabel, valueLabel));
        }
        return param;
    }

    /**
     * Parse valueLabel to a list of '|' separated parameters.
     * If exists parse error, add it into {@link MZTabErrorList}
     */
    private List<Parameter> checkParameterList(String defineLabel, String valueLabel) {
        List<Parameter> paramList = parseParamList(valueLabel);

        if (paramList.size() == 0) {
            errorList.add(new MZTabError(FormatErrorType.ParamList, lineNumber, Error_Header + defineLabel, valueLabel));
        }

        return paramList;
    }

    /**
     * Parse valueLabel to a list of '|' separated parameters.
     * If exists parse error, add it into {@link MZTabErrorList}
     */
    private Publication checkPublication(Integer id, String defineLabel, String valueLabel) {
        if(!context.getPublicationMap().containsKey(id)) {
            context.addPublication(metadata, new Publication().id(id));
        }
        Publication publications = parsePublicationItems(context.getPublicationMap().get(id), valueLabel);
        
        if (publications.getPublicationItems() == null || publications.getPublicationItems().size() == 0) {
            errorList.add(new MZTabError(FormatErrorType.Publication, lineNumber, Error_Header + defineLabel, valueLabel));
        }

        return publications;
    }

    /**
     * Parse valueLabel to a {@link java.net.URI}
     * If exists parse error, add it into {@link MZTabErrorList}
     */
    private java.net.URI checkURI(String defineLabel, String valueLabel) {
        java.net.URI uri = parseURI(valueLabel);
        if (uri == null) {
            errorList.add(new MZTabError(FormatErrorType.URI, lineNumber, Error_Header + defineLabel, valueLabel));
        }

        return uri;
    }

    /**
     * Parse valueLabel to {@link java.net.URL}
     * If exists parse error, add it into {@link MZTabErrorList}
     */
    private java.net.URL checkURL(String defineLabel, String valueLabel) {

        if(null == parseString(valueLabel)){
            // "null" value is supported when the ms_run[1-n]-location is unknown
            errorList.add(new MZTabError(LogicalErrorType.NotNULL, lineNumber, Error_Header + defineLabel, valueLabel));
            return null;
        }

        java.net.URL url = parseURL(valueLabel);
        if (url == null) {  //Malformed exception
            errorList.add(new MZTabError(FormatErrorType.URL, lineNumber, Error_Header + defineLabel, valueLabel));
            return null;
        }

        return url;
    }

    /**
     * Parse defineLabel to a index id number.
     * If exists parse error, stop validate and throw {@link MZTabException} directly.
     */
    private int checkIndex(String defineLabel, String id) throws MZTabException {
        try {
            Integer index = Integer.parseInt(id);
            if (index < 1) {
                throw new NumberFormatException();
            }

            return index;
        } catch (NumberFormatException e) {
            MZTabError error = new MZTabError(LogicalErrorType.IdNumber, lineNumber, Error_Header + defineLabel, id);
            throw new MZTabException(error);
        }
    }

    /**
     * Parse valueLabel to a {@link IndexedElement}
     * If exists parse error, stop validate and throw {@link MZTabException} directly.
     */
    private IndexedElement checkIndexedElement(String defineLabel, String valueLabel, MetadataElement element) throws MZTabException {
        IndexedElement indexedElement = parseParameter(valueLabel, element);
        if (indexedElement == null) {
            MZTabError error = new MZTabError(FormatErrorType.IndexedElement, lineNumber, Error_Header + defineLabel, valueLabel);
            throw new MZTabException(error);
        }

        return indexedElement;
    }

    /**
     * Parse valueLabel to a {@link IndexedElement} list.
     * If exists parse error, stop validate and throw {@link MZTabException} directly.
     */
    private List<IndexedElement> checkIndexedElementList(String defineLabel, String valueLabel, MetadataElement element) throws MZTabException {
        List<IndexedElement> indexedElementList = parseRefList(valueLabel, element);
        if (indexedElementList == null || indexedElementList.size() == 0) {
            MZTabError error = new MZTabError(FormatErrorType.IndexedElement, lineNumber, Error_Header + defineLabel, valueLabel);
            throw new MZTabException(error);
        }
        
//        return indexedElementList.stream().map((t) ->
//            {
//                switch(element) {
//                    case ASSAY:
//                        context.
//                        break;
//                    case MS_RUN:
//                        break;
//                    case SAMPLE:
//                        break;
//                    case STUDY_VARIABLE:
//                        break;
//                    default:
//                        MZTabError error = new MZTabError(FormatErrorType.IndexedElement, lineNumber, Error_Header + defineLabel, valueLabel, "Unhandled element type!");
//                        throw new MZTabException(error);
//                }
//            }).collect(Collectors.toList());
//        }

        return indexedElementList;
    }

    /**
     * The metadata line including three parts:
     * MTD  {defineLabel}    {valueLabel}
     *
     * In normal, define label structure like:
     * {@link MetadataElement}([id])(-{@link MetadataSubElement}[pid])(-{@link MetadataProperty})
     *
     * @see MetadataElement     : Mandatory
     * @see MetadataSubElement  : Optional
     * @see MetadataProperty    : Optional.
     *
     * If exists parse error, add it into {@link MZTabErrorList}
     */
    private void parseNormalMetadata(String defineLabel, String valueLabel) throws MZTabException {
        String regexp = "(\\w+)(\\[(\\w+)\\])?(-(\\w+)(\\[(\\w+)\\])?)?(-(\\w+))?";

        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(defineLabel);

        if (matcher.find()) {
            // Stage 1: create Unit.
            MetadataElement element = MetadataElement.findElement(matcher.group(1));
            if (element == null) {
                throw new MZTabException(new MZTabError(FormatErrorType.MTDDefineLabel, lineNumber, defineLabel));
            }

            Integer id;
            MetadataProperty property;
            Parameter param;
            SplitList<Parameter> paramList;
            IndexedElement indexedElement;
            List<IndexedElement> indexedElementList;
            switch (element) {
                case MZTAB:
                    property = checkProperty(element, matcher.group(5));
                    switch (property != null ? property : null) {
                        case MZTAB_VERSION:
                            metadata.mzTabVersion(valueLabel);
                            if(metadata.getMzTabVersion().matches("1\\.0")) {
                                throw new MZTabException(new MZTabError(FormatErrorType.MZTabVersion, lineNumber, defineLabel));
                            }
                            break;
//                        case MZTAB_MODE:
//                            metadata.setMZTabMode(checkMZTabMode(defineLabel, valueLabel));
//                            break;
//                        case MZTAB_TYPE:
//                            metadata.setMZTabType(checkMZTabType(defineLabel, valueLabel));
//                            break;
                        case MZTAB_ID:
                            if (metadata.getMzTabID()!= null) {
                                throw new MZTabException(new MZTabError(LogicalErrorType.DuplicationDefine, lineNumber, defineLabel));
                            }
                            metadata.mzTabID(valueLabel);
                            break;
                    }

                    break;
                case TITLE:
                    if (metadata.getTitle() != null) {
                        throw new MZTabException(new MZTabError(LogicalErrorType.DuplicationDefine, lineNumber, defineLabel));
                    }
                    metadata.setTitle(valueLabel);
                    break;
                case DESCRIPTION:
                    if (metadata.getDescription() != null) {
                        throw new MZTabException(new MZTabError(LogicalErrorType.DuplicationDefine, lineNumber, defineLabel));
                    }
                    metadata.setDescription(valueLabel);
                    break;
                case SAMPLE_PROCESSING:
                    id = checkIndex(defineLabel, matcher.group(3));
                    addSampleProcessing(metadata, id, checkParameterList(defineLabel, valueLabel));
                    break;
                case INSTRUMENT:
                    id = checkIndex(defineLabel, matcher.group(3));
                    property = checkProperty(element, matcher.group(5));
                    param = checkParameter(defineLabel, valueLabel);
                    addInstrument(metadata, property, id, param);
                    break;
                case SOFTWARE:
                    id = checkIndex(defineLabel, matcher.group(3));
                    property = checkProperty(element, matcher.group(5));
                    addSoftware(metadata, property, defineLabel, valueLabel, id);
                    break;
//                case PROTEIN_SEARCH_ENGINE_SCORE:
//                    id = checkIndex(defineLabel, matcher.group(3));
//                    metadata.addProteinSearchEngineScoreParameter(id, checkParameter(defineLabel, valueLabel));
//                    break;
//                case PEPTIDE_SEARCH_ENGINE_SCORE:
//                    id = checkIndex(defineLabel, matcher.group(3));
//                    metadata.addPeptideSearchEngineScoreParameter(id, checkParameter(defineLabel, valueLabel));
//                    break;
//                case PSM_SEARCH_ENGINE_SCORE:
//                    id = checkIndex(defineLabel, matcher.group(3));
//                    metadata.addPsmSearchEngineScoreParameter(id, checkParameter(defineLabel, valueLabel));
//                    break;
//                case SMALLMOLECULE_SEARCH_ENGINE_SCORE:
//                    id = checkIndex(defineLabel, matcher.group(3));
//                    metadata.addSmallMoleculeSearchEngineScoreParameter(id, checkParameter(defineLabel, valueLabel));
//                    break;
//                case FALSE_DISCOVERY_RATE:
//                    if (metadata.getFalseDiscoveryRate().size() > 0) {
//                        throw new MZTabException(new MZTabError(LogicalErrorType.DuplicationDefine, lineNumber, defineLabel));
//                    }
//                    paramList = checkParameterList(defineLabel, valueLabel);
//                    metadata.setFalseDiscoveryRate(paramList);
//                    break;
                case PUBLICATION:
                    id = checkIndex(defineLabel, matcher.group(3));
                    checkPublication(id, defineLabel, valueLabel);
                    break;
                case CONTACT:
                    id = checkIndex(defineLabel, matcher.group(3));
                    property = checkProperty(element, matcher.group(5));

                    addContact(metadata, property, id, valueLabel, defineLabel);
                    break;
                case URI:
                    metadata.addUriItem(checkURI(defineLabel, valueLabel).toASCIIString());
                    break;
//                case FIXED_MOD:
//                    id = checkIndex(defineLabel, matcher.group(3));
//                    property = checkProperty(element, matcher.group(5));
//                    if (property == null) {
//                        param = checkParameter(defineLabel, valueLabel);
//                        if (param != null) {
//                            // fixed modification parameter should be setting.
//                            metadata.addFixedModParameter(id, param);
//                        }
//                    } else {
//                        switch (property) {
//                            case FIXED_MOD_POSITION:
//                                metadata.addFixedModPosition(id, valueLabel);
//                                break;
//                            case FIXED_MOD_SITE:
//                                metadata.addFixedModSite(id, valueLabel);
//                                break;
//                        }
//                    }
//                    break;
//                case VARIABLE_MOD:
//                    id = checkIndex(defineLabel, matcher.group(3));
//                    property = checkProperty(element, matcher.group(5));
//                    if (property == null) {
//                        param = checkParameter(defineLabel, valueLabel);
//                        if (param != null) {
//                            // variable modification parameter should be setting.
//                            metadata.addVariableModParameter(id, param);
//                        }
//                    } else {
//                        switch (property) {
//                            case VARIABLE_MOD_POSITION:
//                                metadata.addVariableModPosition(id, valueLabel);
//                                break;
//                            case VARIABLE_MOD_SITE:
//                                metadata.addVariableModSite(id, valueLabel);
//                                break;
//                        }
//                    }
//                    break;
                case QUANTIFICATION_METHOD:
                    if (metadata.getQuantificationMethod() != null) {
                        throw new MZTabException(new MZTabError(LogicalErrorType.DuplicationDefine, lineNumber, defineLabel));
                    }
                    metadata.setQuantificationMethod(checkParameterList(defineLabel, valueLabel));
                    break;
//                case PROTEIN:
//                    property = checkProperty(element, matcher.group(5));
//                    switch (property != null ? property : null) {
//                        case PROTEIN_QUANTIFICATION_UNIT:
//                            if (metadata.getProteinQuantificationUnit() != null) {
//                                throw new MZTabException(new MZTabError(LogicalErrorType.DuplicationDefine, lineNumber, defineLabel));
//                            }
//                            metadata.setProteinQuantificationUnit(checkParameter(defineLabel, valueLabel));
//                            break;
//                    }
//                    break;
//                case PEPTIDE:
//                    property = checkProperty(element, matcher.group(5));
//                    switch (property != null ? property : null) {
//                        case PEPTIDE_QUANTIFICATION_UNIT:
//                            if (metadata.getPeptideQuantificationUnit() != null) {
//                                throw new MZTabException(new MZTabError(LogicalErrorType.DuplicationDefine, lineNumber, defineLabel));
//                            }
//                            metadata.setPeptideQuantificationUnit(checkParameter(defineLabel, valueLabel));
//                            break;
//                    }
//                    break;
                case SMALL_MOLECULE:
                    property = checkProperty(element, matcher.group(5));
                    switch (property != null ? property : null) {
                        case SMALL_MOLECULE_QUANTIFICATION_UNIT:
                            if (metadata.getSmallMoleculeQuantificationUnit() != null) {
                                throw new MZTabException(new MZTabError(LogicalErrorType.DuplicationDefine, lineNumber, defineLabel));
                            }
                            metadata.setSmallMoleculeQuantificationUnit(checkParameter(defineLabel, valueLabel));
                            break;
                    }
                    break;
                case MS_RUN:
                    id = checkIndex(defineLabel, matcher.group(3));
                    property = checkProperty(element, matcher.group(5));

                    addMsRun(metadata, property, id, defineLabel, valueLabel);

                    break;
                case CUSTOM:
                    metadata.addCustomItem(checkParameter(defineLabel, valueLabel));
                    break;
                case SAMPLE:
                    id = checkIndex(defineLabel, matcher.group(3));
                    property = checkProperty(element, matcher.group(5));

                    addSample(metadata, property, id, defineLabel, valueLabel);
                    break;
                case ASSAY:
                    if (isEmpty(matcher.group(6))) {
                        // no quantification modification. For example: assay[1-n]-quantification_reagent
                        id = checkIndex(defineLabel, matcher.group(3));
                        property = checkProperty(element, matcher.group(5));
                        addAssay(metadata, property, defineLabel, valueLabel, id);
                    } else {
                        // quantification modification. For example: assay[1]-quantification_mod[1], assay[1]-quantification_mod[1]-site
//                        id = checkIndex(defineLabel, matcher.group(3));
//                        MetadataSubElement subElement = MetadataSubElement.findSubElement(element, matcher.group(5));
//                        switch (subElement) {
//                            case ASSAY_QUANTIFICATION_MOD:
//                                int modId = checkIndex(defineLabel, matcher.group(7));
//                                property = checkProperty(subElement, matcher.group(9));
//                                if (property == null) {
//                                    metadata.addAssayQuantificationModParameter(id, modId, checkParameter(defineLabel, valueLabel));
//                                } else {
//                                    switch (property) {
//                                        case ASSAY_QUANTIFICATION_MOD_SITE:
//                                            metadata.addAssayQuantificationModSite(id, modId, valueLabel);
//                                            break;
//                                        case ASSAY_QUANTIFICATION_MOD_POSITION:
//                                            metadata.addAssayQuantificationModPosition(id, modId, valueLabel);
//                                            break;
//                                    }
//                                }
//
//                                break;
//                        }
                    }

                    break;
                case STUDY_VARIABLE:
                    id = checkIndex(defineLabel, matcher.group(3));
                    property = checkProperty(element, matcher.group(5));
                    addStudyVariable(metadata, property, defineLabel, valueLabel, id);
                    break;
                case CV:
                    id = checkIndex(defineLabel, matcher.group(3));
                    property = checkProperty(element, matcher.group(5));
                    addCv(metadata, property, id, valueLabel);
                    break;
                case COLUNIT:
                    // In this stage, just store them into colUnitMap<defineLabel, valueLabel>.
                    // after the section columns is created we will add the col unit.
                    if (! defineLabel.equals("colunit-protein") &&
                            ! defineLabel.equals("colunit-peptide") &&
                            ! defineLabel.equals("colunit-psm") &&
                            ! defineLabel.equals("colunit-small_molecule")) {
                        errorList.add(new MZTabError(FormatErrorType.MTDDefineLabel, lineNumber, defineLabel));
                    } else {
                        context.getColUnitMap().put(defineLabel, valueLabel);
                    }
                    break;
            }

        } else {
            throw new MZTabException(new MZTabError(FormatErrorType.MTDLine, lineNumber, line));
        }
    }

    /**
     * Refine the metadata, and check whether missing some important information.
     * fixed_mode, variable_mode must provide in the Complete file.
     * Detail information see specification 5.5
     */
    public void refineNormalMetadata() throws MZTabException {
//        MZTabDescription.Mode mode = metadata.getMZTabMode();
//        MZTabDescription.Type type = metadata.getMZTabType();

        SortedMap<Integer, StudyVariable> svMap = context.getStudyVariableMap();
        SortedMap<Integer, Assay> assayMap = context.getAssayMap();
        SortedMap<Integer, MsRun> runMap = context.getMsRunMap();

//        if (mode == MZTabDescription.Mode.Complete) {
//            if (metadata.getSoftwareMap().size() == 0) {
//                throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "software[1-n]", mode.toString(), type.toString()));
//            }
//
//            if (type == MZTabDescription.Type.Quantification) {
                if (metadata.getQuantificationMethod() == null) {
                    throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "quantification_method"));
                }
//                for (Integer id : assayMap.keySet()) {
//                    if (assayMap.get(id).getMsRun() == null) {
//                        throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "assay[" + id + "]-ms_run_ref", mode.toString(), type.toString()));
//                    }
//                    if (assayMap.get(id).getQuantificationReagent() == null) {
//                        throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "assay[" + id + "]-quantification_reagent", mode.toString(), type.toString()));
//                    }
//                }
//                if (svMap.size() > 0 && assayMap.size() > 0) {
//                    for (Integer id : svMap.keySet()) {
//                        if (svMap.get(id).getAssayMap().size() == 0) {
//                            throw new MZTabException(new MZTabError(LogicalErrorType.AssayRefs, lineNumber, "study_variable[" + id + "]-assay_refs"));
//                        }
//                    }
//                }
//            }
//        }

        // Complete and Summary should provide following information.
        // mzTab-version, mzTab-mode and mzTab-type have default values in create metadata. Not check here.
        if (metadata.getDescription() == null) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "description", metadata.getMzTabVersion()));
        }
        for (Integer id : runMap.keySet()) {
            if (runMap.get(id).getLocation() == null) {
                throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "ms_run[" + id + "]-location", metadata.getMzTabVersion()));
            }
        }

        //mods
        //fixed
//        if (metadata.getFixedModMap().size() == 0) {
//            throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "fixed_mod[1-n]", mode.toString(), type.toString()));
//        }
//        //variable
//        if (metadata.getVariableModMap().size() == 0) {
//            throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "variable_mod[1-n]", mode.toString(), type.toString()));
//        }

//        if (type == MZTabDescription.Type.Quantification) {
//            for (Integer id : svMap.keySet()) {
//                if (svMap.get(id).getDescription() == null) {
//                    throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, "study_variable[" + id + "]-description", mode.toString(), type.toString()));
//                }
//            }
//        }
    }

    public Metadata getMetadata() {
        return metadata;
    }

    private void addSampleProcessing(Metadata metadata, Integer id,
        List<Parameter> checkParameterList) throws MZTabException {
        SampleProcessing sp = context.addSampleProcessing(metadata,id, checkParameterList);
        if(sp == null) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NULL, lineNumber, "sample_processing["+id+"]"));
        }
    }
    
    private void addInstrument(Metadata metadata, MetadataProperty property, Integer id,
        Parameter param) throws MZTabException {
        Instrument instrument = null;
        
        switch (property != null ? property : null) {
            case INSTRUMENT_NAME:
                instrument = context.addInstrumentName(metadata,id, param);
                break;
            case INSTRUMENT_SOURCE:
                instrument = context.addInstrumentSource(metadata,id, param);
                break;
            case INSTRUMENT_ANALYZER:
                instrument = context.addInstrumentAnalyzer(metadata,id, param);
                break;
            case INSTRUMENT_DETECTOR:
                instrument = context.addInstrumentDetector(metadata,id, param);
                break;
        }
        if(instrument == null) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NULL, lineNumber, "instrument["+id+"]"));
        }
    }

    private void addSoftware(Metadata metadata, MetadataProperty property, String defineLabel,
        String valueLabel, Integer id) throws MZTabErrorOverflowException, MZTabException {
        Parameter param;
        Software software = null;
        if (property == null) {
            param = checkParameter(defineLabel, valueLabel);
            if (param != null && (param.getValue() == null || param.getValue().trim().length() == 0)) {
                // this is a warn.
                errorList.add(new MZTabError(LogicalErrorType.SoftwareVersion, lineNumber, valueLabel));
            }
            software = context.addSoftwareParameter(metadata,id, param);
        } else {
            switch (property) {
                case SOFTWARE_SETTING:
                    software = context.addSoftwareSetting(metadata,id, valueLabel);
                    break;
            }
        }
        if(software == null) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NULL, lineNumber, "software["+id+"]"));
        }
    }

    private void addContact(Metadata metadata, MetadataProperty property, Integer id,
        String valueLabel, String defineLabel) throws MZTabException {
        Contact contact = null;
        switch (property != null ? property : null) {
            case CONTACT_NAME:
                contact = context.addContactName(metadata,id, valueLabel);
                break;
            case CONTACT_AFFILIATION:
                contact = context.addContactAffiliation(metadata,id, valueLabel);
                break;
            case CONTACT_EMAIL:
                checkEmail(defineLabel, valueLabel);
                contact = context.addContactEmail(metadata,id, valueLabel);
                break;
        }
        if(contact == null) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NULL, lineNumber, "contact["+id+"]"));
        }
    }

    private void addPublication(Metadata metadata, Publication publication) {
        context.addPublication(metadata, publication);
    }

    private void addMsRun(Metadata metadata, MetadataProperty property, Integer id,
        String defineLabel, String valueLabel) throws MZTabException{
        MsRun msRun = null;
        if(property==null) {
            msRun = context.addMsRun(metadata, new MsRun().id(id).name(valueLabel));
        } else {
            switch (property != null ? property : null) {
                case MS_RUN_FORMAT:
                    msRun = context.addMsRunFormat(metadata, id, checkParameter(defineLabel, valueLabel));
                    break;
                case MS_RUN_LOCATION:
                    msRun = context.addMsRunLocation(metadata, id, checkURL(defineLabel, valueLabel));
                    break;
                case MS_RUN_ID_FORMAT:
                    msRun = context.addMsRunIdFormat(metadata, id, checkParameter(defineLabel, valueLabel));
                    break;
                case MS_RUN_FRAGMENTATION_METHOD:
                    msRun = context.addMsRunFragmentationMethod(metadata, id, checkParameter(defineLabel, valueLabel));
                    break;
                case MS_RUN_HASH:
                    msRun = context.addMsRunHash(metadata, id, valueLabel);
                    break;
                case MS_RUN_HASH_METHOD:
                    msRun = context.addMsRunHashMethod(metadata, id, checkParameter(defineLabel, valueLabel));
                    break;
            }
        }
        if(msRun == null) {
            throw new MZTabException(new MZTabError(LogicalErrorType.NULL, lineNumber, "ms_run["+id+"]"));
        }
    }
    
    private void addSample(Metadata metadata, MetadataProperty property, Integer id,
        String defineLabel, String valueLabel) {
        switch (property != null ? property : null) {
            case SAMPLE_SPECIES:
                context.addSampleSpecies(metadata,id, checkParameter(defineLabel, valueLabel));
                break;
            case SAMPLE_TISSUE:
                context.addSampleTissue(metadata,id, checkParameter(defineLabel, valueLabel));
                break;
            case SAMPLE_CELL_TYPE:
                context.addSampleCellType(metadata,id, checkParameter(defineLabel, valueLabel));
                break;
            case SAMPLE_DISEASE:
                context.addSampleDisease(metadata,id, checkParameter(defineLabel, valueLabel));
                break;
            case SAMPLE_DESCRIPTION:
                context.addSampleDescription(metadata,id, valueLabel);
                break;
            case SAMPLE_CUSTOM:
                context.addSampleCustom(metadata,id, checkParameter(defineLabel, valueLabel));
                break;
        }
    }

    private void addAssay(Metadata metadata, MetadataProperty property, String defineLabel,
        String valueLabel, Integer id) throws MZTabException {
        IndexedElement indexedElement;
        switch (property != null ? property : null) {
//                            case ASSAY_QUANTIFICATION_REAGENT:
//                                metadata.addAssayQuantificationReagent(id, checkParameter(defineLabel, valueLabel));
//                                break;
            case ASSAY_SAMPLE_REF:
                indexedElement = checkIndexedElement(defineLabel, valueLabel, MetadataElement.SAMPLE);
                if (indexedElement != null) {
                    Sample sample = context.getSampleMap().get(indexedElement.getId());
                    if (sample == null) {
                        throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, valueLabel,
                            valueLabel));
                    }
                    context.addAssaySample(metadata, id, sample);
                }
                break;
            case ASSAY_MS_RUN_REF:
                indexedElement = checkIndexedElement(defineLabel, valueLabel, MetadataElement.MS_RUN);
                if (indexedElement != null) {
                    MsRun msRun = context.getMsRunMap().get(indexedElement.getId());
                    if (msRun == null) {
                        throw new MZTabException(new MZTabError(
                            LogicalErrorType.NotDefineInMetadata, lineNumber,
                            valueLabel));
                    }
                    context.addAssayMsRun(metadata, id, msRun);
                }
                break;
        }
    }

    private void addStudyVariable(Metadata metadata, MetadataProperty property, String defineLabel,
        String valueLabel, Integer id) throws MZTabErrorOverflowException, MZTabException {
        List<IndexedElement> indexedElementList;
        if(property!=null) {
            switch (property) {
                case STUDY_VARIABLE_ASSAY_REFS:
                    indexedElementList = checkIndexedElementList(defineLabel, valueLabel, MetadataElement.ASSAY);
                    // detect duplicates
                    indexedElementList.stream().filter(i -> Collections.frequency(indexedElementList, i) >1)
                    .collect(Collectors.toSet()).forEach((indexedElement) ->
                        {
                            errorList.add(new MZTabError(LogicalErrorType.DuplicationID, lineNumber, valueLabel));
                        });
                    // check that assays exist
                    for (IndexedElement e : indexedElementList) {
                        //assays need to be defined before
                        if (! context.getAssayMap().containsKey(e.getId())) {
                            // can not find assay[id] in metadata.
                            throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, valueLabel,
                                valueLabel));
                        }
                        context.addStudyVariableAssay(metadata, id, context.getAssayMap().get(e.getId()));
                    }
                    break;
                case STUDY_VARIABLE_SAMPLE_REFS:
                    indexedElementList = checkIndexedElementList(defineLabel, valueLabel, MetadataElement.SAMPLE);
                    // detect duplicates
                    indexedElementList.stream().filter(i -> Collections.frequency(indexedElementList, i) >1)
                    .collect(Collectors.toSet()).forEach((indexedElement) ->
                        {
                            errorList.add(new MZTabError(LogicalErrorType.DuplicationID, lineNumber, valueLabel));
                        });
                    // check that sample exist
                    for (IndexedElement e : indexedElementList) {
                        if (! context.getSampleMap().containsKey(e.getId())) {
                            // can not find assay[id] in metadata.
                            throw new MZTabException(new MZTabError(LogicalErrorType.NotDefineInMetadata, lineNumber, valueLabel,
                                valueLabel));
                        }
                        context.addStudyVariableSample(metadata, id, context.getSampleMap().get(e.getId()));
                    }
                    break;
                case STUDY_VARIABLE_DESCRIPTION:
                    context.addStudyVariableDescription(metadata, id, valueLabel);
                    break;
            }
        } else {
            context.addStudyVariable(metadata, new StudyVariable().id(id).name(valueLabel));
        }
    }

    private void addCv(Metadata metadata, MetadataProperty property, Integer id, String valueLabel) {
        switch (property != null ? property : null) {
            case CV_LABEL:
                context.addCVLabel(metadata, id, valueLabel);
                break;
            case CV_FULL_NAME:
                context.addCVFullName(metadata, id, valueLabel);
                break;
            case CV_VERSION:
                context.addCVVersion(metadata, id, valueLabel);
                break;
            case CV_URL:
                context.addCVURL(metadata, id, valueLabel);
                break;
        }
    }
}
