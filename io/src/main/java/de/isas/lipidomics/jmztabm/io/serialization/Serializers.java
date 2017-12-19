/*
 * Copyright 2017 Nils Hoffmann <nils.hoffmann@isas.de>.
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
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.IndexedElement;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.StudyVariable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static uk.ac.ebi.pride.jmztab.model.MZTabConstants.COMMA;
import uk.ac.ebi.pride.jmztab.model.MZTabUtils;
import uk.ac.ebi.pride.jmztab.model.MetadataProperty;
import uk.ac.ebi.pride.jmztab.model.Section;
import static uk.ac.ebi.pride.jmztab.model.MZTabConstants.MINUS;
import static uk.ac.ebi.pride.jmztab.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab.model.MZTabConstants.NULL;
import static uk.ac.ebi.pride.jmztab.model.MZTabConstants.TAB;
import static uk.ac.ebi.pride.jmztab.model.MetadataElement.MZTAB;
import static uk.ac.ebi.pride.jmztab.model.MetadataProperty.MZTAB_ID;
import static uk.ac.ebi.pride.jmztab.model.MetadataProperty.MZTAB_VERSION;
import static uk.ac.ebi.pride.jmztab.model.MetadataProperty.STUDY_VARIABLE_DESCRIPTION;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 * @since 11/30/17
 */
public class Serializers {

    public static String serialize(Section section, Metadata metadata) {
        StringBuilder sb = new StringBuilder();
        printPrefix(sb, Section.Metadata).
            append(MZTAB).
            append(MINUS).
            append(MZTAB_VERSION).
            append(TAB).
            append(metadata.getMzTabVersion()).
            append(NEW_LINE);

        if (!MZTabUtils.isEmpty(metadata.getMzTabID())) {
            printPrefix(sb, Section.Metadata).
                append(MZTAB).
                append(MINUS).
                append(MZTAB_ID).
                append(TAB).
                append(metadata.getMzTabID()).
                append(NEW_LINE);
        }
        return sb.toString();
    }

//    public static String serialize(Section section, Metadata metadata) {
//        StringBuilder sb = new StringBuilder();
//
//        sb.append(metadata.getTabDescription().
//            toString());
//
//        if (metadata.getTitle() != null) {
//            printPrefix(sb, section).
//                append(TITLE).
//                append(TAB).
//                append(metadata.getTitle()).
//                append(NEW_LINE);
//        }
//
//        if (metadata.getDescription() != null) {
//            printPrefix(sb, section).
//                append(DESCRIPTION).
//                append(TAB).
//                append(metadata.getDescription()).
//                append(NEW_LINE);
//        }
//
//        sb = printMap(metadata.getSampleProcessingMap(), SAMPLE_PROCESSING.
//            toString(), sb, section);
//        sb = printMap(metadata.getInstrumentMap(), INSTRUMENT.toString(), sb,
//            section);
//        sb = printMap(metadata.getSoftwareMap(), SOFTWARE.toString(), sb,
//            section);
//        sb = printMap(metadata.getProteinSearchEngineScoreMap(),
//            PROTEIN_SEARCH_ENGINE_SCORE.toString(), sb, section);
//        sb = printMap(metadata.getPeptideSearchEngineScoreMap(),
//            PEPTIDE_SEARCH_ENGINE_SCORE.toString(), sb, section);
//        sb = printMap(metadata.getPsmSearchEngineScoreMap(),
//            PSM_SEARCH_ENGINE_SCORE.toString(), sb, section);
//        sb = printMap(metadata.getSmallMoleculeSearchEngineScoreMap(),
//            SMALLMOLECULE_SEARCH_ENGINE_SCORE.toString(), sb, section);
//
//        if (!metadata.getFalseDiscoveryRate().
//            isEmpty()) {
//            printPrefix(sb, section).
//                append(FALSE_DISCOVERY_RATE).
//                append(TAB).
//                append(metadata.getFalseDiscoveryRate()).
//                append(NEW_LINE);
//        }
//
//        sb = printMap(metadata.getPublicationMap(), PUBLICATION.toString(), sb,
//            section);
//        sb = printMap(metadata.getContactMap(), CONTACT.toString(), sb, section);
//
//        printList(metadata.getUriList(), URI.toString(), sb, section);
//
//        sb = printMap(metadata.getFixedModMap(), FIXED_MOD.toString(), sb,
//            section);
//        sb = printMap(metadata.getVariableModMap(), VARIABLE_MOD.toString(), sb,
//            section);
//
//        if (metadata.getQuantificationMethod() != null) {
//            printPrefix(sb, section).
//                append(QUANTIFICATION_METHOD).
//                append(TAB).
//                append(metadata.getQuantificationMethod()).
//                append(NEW_LINE);
//        }
//        if (metadata.getProteinQuantificationUnit() != null) {
//            printPrefix(sb, section).
//                append(PROTEIN).
//                append(MINUS).
//                append(PROTEIN_QUANTIFICATION_UNIT).
//                append(TAB).
//                append(metadata.getProteinQuantificationUnit()).
//                append(NEW_LINE);
//        }
//        if (metadata.getPeptideQuantificationUnit() != null) {
//            printPrefix(sb, section).
//                append(PEPTIDE).
//                append(MINUS).
//                append(PEPTIDE_QUANTIFICATION_UNIT).
//                append(TAB).
//                append(metadata.getPeptideQuantificationUnit()).
//                append(NEW_LINE);
//        }
//        if (metadata.getSmallMoleculeQuantificationUnit() != null) {
//            printPrefix(sb, section).
//                append(SMALL_MOLECULE).
//                append(MINUS).
//                append(SMALL_MOLECULE_QUANTIFICATION_UNIT).
//                append(TAB).
//                append(metadata.getSmallMoleculeQuantificationUnit()).
//                append(NEW_LINE);
//        }
//
//        sb = printMap(metadata.getMsRunMap(), MS_RUN.toString(), sb, section);
//        sb = printMap(metadata.getSampleMap(), SAMPLE.toString(), sb, section);
//        sb = printMap(metadata.getAssayMap(), ASSAY.toString(), sb, section);
//        sb = printMap(metadata.getStudyVariableMap(), STUDY_VARIABLE.toString(),
//            sb, section);
//        sb = printMap(metadata.getCvMap(), CV.toString(), sb, section);
//
//        for (ColUnit colUnit : metadata.getProteinColUnitList()) {
//            printPrefix(sb, section).
//                append(COLUNIT).
//                append(MINUS).
//                append(COLUNIT_PROTEIN);
//            sb.append(TAB).
//                append(colUnit).
//                append(NEW_LINE);
//        }
//        for (ColUnit colUnit : metadata.getPeptideColUnitList()) {
//            printPrefix(sb, section).
//                append(COLUNIT).
//                append(MINUS).
//                append(COLUNIT_PEPTIDE);
//            sb.append(TAB).
//                append(colUnit).
//                append(NEW_LINE);
//        }
//        for (ColUnit colUnit : metadata.getPsmColUnitList()) {
//            printPrefix(sb, section).
//                append(COLUNIT).
//                append(MINUS).
//                append(COLUNIT_PSM);
//            sb.append(TAB).
//                append(colUnit).
//                append(NEW_LINE);
//        }
//        for (ColUnit colUnit : metadata.getSmallMoleculeColUnitList()) {
//            printPrefix(sb, section).
//                append(COLUNIT).
//                append(MINUS).
//                append(COLUNIT_SMALL_MOLECULE);
//            sb.append(TAB).
//                append(colUnit).
//                append(NEW_LINE);
//        }
//
//        printList(metadata.getCustomList(), CUSTOM.toString(), sb, section);
//
//        return sb.toString();
//    }
    public static String serialize(Section section, Assay assay) {
        StringBuilder sb = new StringBuilder();

//        if (quantificationReagent != null) {
//            printPrefix(sb).append(getReference()).append(MINUS).append(MetadataProperty.ASSAY_QUANTIFICATION_REAGENT);
//            sb.append(TAB).append(quantificationReagent).append(NEW_LINE);
//        }
        if (assay.getSampleRef() != null) {
            printPrefix(sb, section).
                append(getReference(assay, assay.getId())).
                    append(MINUS).
                    append(MetadataProperty.ASSAY_SAMPLE_REF);
            sb.append(TAB).
                append(getReference(assay.getSampleRef(), assay.getSampleRef().getId())).
                append(NEW_LINE);
        }

        if (assay.getMsRunRef() != null) {
            printPrefix(sb, section).
                append(assay.getName()).
                append(MINUS).
                append(MetadataProperty.ASSAY_MS_RUN_REF);
            sb.append(TAB).
                append(getReference(assay.getMsRunRef(), assay.getMsRunRef().getId())).
                append(NEW_LINE);
        }

//        for (AssayQuantificationMod mod : quantificationModMap.values()) {
//            sb.append(mod);
//        }
        if (assay.getCustom() != null) {
            printList(assay, assay.getCustom(), MetadataProperty.ASSAY_CUSTOM,
                sb);
        }

        if (assay.getExternalUri() != null) {
            printProperty(assay, assay.getId(), MetadataProperty.ASSAY_EXTERNAL_URI, assay.
                getExternalUri());
        }

        return sb.toString();
    }

    /**
     * Print study_variable[1-n] object to a string. The structure like:
     * <ul>
     * <li>MTD	study_variable[1]-description	description Group B (spike-in 0.74
     * fmol/uL)</li>
     * <li>MTD	study_variable[1]-assay_refs	assay[1], assay[2]</li>
     * <li>MTD	study_variable[1]-sample_refs	sample[1]</li>
     * </ul>
     */
    public static String serialize(Section section, StudyVariable studyVariable) {
        StringBuilder sb = new StringBuilder();

        if (studyVariable.getDescription() != null) {
            sb.append(printProperty(studyVariable, studyVariable.getId(), STUDY_VARIABLE_DESCRIPTION,
                studyVariable.getDescription())).
                append(NEW_LINE);
        }

        String assayRef = toAssayRef(studyVariable);
        if (assayRef.length() != 0) {
            printPrefix(sb, section).
                append(getReference(studyVariable, studyVariable.getId())).
                append(MINUS).
                append(MetadataProperty.STUDY_VARIABLE_ASSAY_REFS);
            sb.append(TAB).
                append(assayRef).
                append(NEW_LINE);
        }

        String sampleRef = toSampleRef(studyVariable);
        if (sampleRef.length() != 0) {
            printPrefix(sb, section).
                append(getReference(studyVariable, studyVariable.getId())).
                append(MINUS).
                append(MetadataProperty.STUDY_VARIABLE_SAMPLE_REFS);
            sb.append(TAB).
                append(sampleRef).
                append(NEW_LINE);
        }

        return sb.toString();
    }

    public static String getReference(Object element, Integer idx) {
        StringBuilder sb = new StringBuilder();

        sb.append(getElementName(element).
            orElseThrow(() ->
            {
                return new IllegalArgumentException(
                    "No mzTab element name mapping available for " + element.
                        getClass().
                        getName());
            })).
            append("[").
            append(idx).
            append("]");

        return sb.toString();
    }

    public static StringBuilder printPrefix(StringBuilder sb, Section section) {
        return sb.append(section.getPrefix()).
            append(TAB);
    }

    /**
     * Internal method used to output the list object, e.g.
     * {@link #uriList}, {@link #proteinColUnitList} and so on. The output line
     * structure like: MTD item[list.id + 1] list.value For example: MTD	uri[1]
     * http://www.ebi.ac.uk/pride/url/to/experiment MTD	uri[2]
     * http://proteomecentral.proteomexchange.org/cgi/GetDataset
     */
    public static StringBuilder printList(List<?> list, String item,
        StringBuilder sb, Section section) {
        for (int i = 0; i < list.size(); i++) {
            printPrefix(sb, section).
                append(item).
                append("[").
                append(i + 1).
                append("]").
                append(TAB).
                append(list.get(i)).
                append(NEW_LINE);
        }

        return sb;
    }

    /**
     * Internal method used to output the indexed element map, e.g.
     * {@link #sampleProcessingMap}, {@link #instrumentMap} and so on. The
     * output line structure like: MTD item[map.key] map.value For example: MTD
     * sample_processing[1]	[SEP, SEP:00173, SDS PAGE, ] MTD
     * sample_processing[2]	[SEP, SEP:00142, enzyme digestion, ]|[MS,
     * MS:1001251, Trypsin, ]
     */
    public static void serializeListOfParameterLists(JsonGenerator jg,
        String prefix, Object element, List<List<Parameter>> listOfParameterLists,
        String item,
        StringBuilder sb, Section section) {

        IntStream.range(1, listOfParameterLists.size() + 1).
            forEach((idx) ->
            {
                addIndexedLine(jg, prefix, getElementName(element).
                    orElseThrow(() ->
                    {
                        return new ElementNameMappingException(element);
                    }), idx, listOfParameterLists.get(
                    idx - 1));
            });
    }

    /**
     * Print indexed element with value to a String, structure like: MTD
     * {element}[id] {value.toString}
     */
    public static String printElement(Object element, Integer idx, Object value) {
        StringBuilder sb = new StringBuilder();

        printPrefix(sb, Section.Metadata).
            append(getElementName(element).
                orElseThrow(() ->
                {
                    return new ElementNameMappingException(element);
                })).
            append("[").
            append(idx).
            append("]");

        if (value != null) {
            sb.append(TAB).
                append(value);
        }

        return sb.toString();
    }

    /**
     * Print indexed element with property and value to a String, structure
     * like:
     *
     * MTD {element}[id]-{property} {value.toString}
     */
    public static String printProperty(Object element, Integer idx,
        MetadataProperty property, Object value) {
        StringBuilder sb = new StringBuilder();

        printPrefix(sb, Section.Metadata).
            append(getElementName(element).
                orElseThrow(() ->
                {
                    return new ElementNameMappingException(element);
                })).
            append("[").
            append(idx).
            append("]").
            append(MINUS).
            append(property);

        if (value != null) {
            sb.append(TAB).
                append(value);
        } else {
            sb.append(TAB).
                append(NULL);
        }

        return sb.toString();
    }

    /**
     * Print indexed element with property and value to a String, the property
     * has sub index, the structure like:
     *
     * MTD {element}[id]-{property}[sub-id] {value.toString}
     */
    public static String printProperty(IndexedElement element,
        MetadataProperty property, int subId, Object value) {
        StringBuilder sb = new StringBuilder();

        printPrefix(sb, Section.Metadata).
            append(getReference(element, element.getId())).
            append(MINUS).
            append(property).
            append("[").
            append(subId).
            append("]");

        if (value != null) {
            sb.append(TAB).
                append(value).
                append(NEW_LINE);
        }

        return sb.toString();
    }

    /**
     * Print a list of metadata line. The sub index start from 1. MTD
     * {element}[id]-{property}[1] {value.toString} MTD
     * {element}[id]-{property}[2] {value.toString} MTD
     * {element}[id]-{property}[3] {value.toString} ....
     */
    public static StringBuilder printList(IndexedElement element, List<?> list,
        MetadataProperty property, StringBuilder sb) {
        Object param;
        for (int i = 0; i < list.size(); i++) {
            param = list.get(i);
            sb.append(printProperty(element, property, i + 1, param));
        }

        return sb;
    }

    public static String toSampleRef(StudyVariable studyVariable) {
         return studyVariable.getSampleRefs().stream().map((sample) ->
        {
            return getReference(sample, sample.getId());
        }).
        collect(Collectors.joining(""+COMMA, "", " "));
    }

    public static String toAssayRef(StudyVariable studyVariable) {
        return studyVariable.getAssayRefs().stream().map((assay) ->
        {
            return getReference(assay, assay.getId());
        }).
        collect(Collectors.joining(""+COMMA, "", " "));
    }
    
    public static void addIndexedLine(JsonGenerator jg, String prefix,
        Object element, Integer idx, Parameter parameter) {
        addIndexedLine(jg, prefix, element, idx, Arrays.asList(parameter));
    }
    
    public static void addIndexedLine(JsonGenerator jg, String prefix,
        Object element, Integer idx,
        List<Parameter> parameterList) {
        try {
            jg.writeStartArray();
            //prefix
            jg.writeString(prefix);
            //key
            jg.writeString(new StringBuilder().append(getElementName(element).
                orElseThrow(() ->
                {
                    return new ElementNameMappingException(element);
                })).
                append("[").
                append(idx).
                append("]").
                toString());
            //value
            jg.writeString(parameterList.stream().
                map((parameter) ->
                {
                    return ParameterSerializer.toString(parameter);
                }).
                collect(Collectors.joining("|")));
            jg.writeEndArray();
        } catch (IOException ex) {
            Logger.getLogger(MetadataSerializer.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    public static void addLine(JsonGenerator jg, String prefix, Object element,
        String... value) {
        try {
            jg.writeStartArray();
            //prefix
            jg.writeString(prefix);
            //key
            jg.writeString(getElementName(element).
                orElseThrow(() ->
                {
                    return new ElementNameMappingException(element);
                }));
            //value
            for (String s : value) {
                jg.writeString(s);
            }
            jg.writeEndArray();
        } catch (IOException ex) {
            Logger.getLogger(MetadataSerializer.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

//    public static void addLine(JsonGenerator jg, String prefix, String key,
//        ParameterList value) {
//
//    }
    public static Optional<String> getElementName(Object element) {
//        @JacksonXmlRootElement(localName = "Assay")
        JacksonXmlRootElement rootElement = element.getClass().
            getAnnotation(JacksonXmlRootElement.class);
        if (rootElement != null) {
            return Optional.ofNullable(camelCaseToUnderscoreLowerCase(
                rootElement.localName()));
        }
        return Optional.empty();
    }

    public static String camelCaseToUnderscoreLowerCase(String camelCase) {
        return camelCase.replaceAll("(.)(\\p{Upper})", "$1_$2").
            toLowerCase();
    }

}
