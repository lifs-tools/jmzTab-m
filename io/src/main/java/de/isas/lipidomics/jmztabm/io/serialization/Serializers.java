/*
 * Copyright 2017 Nils Hoffmann &lt;nils.hoffmann@isas.de&gt;.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.IndexedElement;
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.StudyVariable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.NULL;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataElement;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataProperty;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeColumn;

/**
 * <p>Serializers class.</p>
 *
 * @author nilshoffmann
 * @since 11/30/17
 * 
 */
public class Serializers {

    /**
     * <p>getReference.</p>
     *
     * @param element a {@link java.lang.Object} object.
     * @param idx a {@link java.lang.Integer} object.
     * @return a {@link java.lang.String} object.
     */
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

    /**
     * <p>printAbundanceAssay.</p>
     *
     * @param a a {@link de.isas.mztab1_1.model.Assay} object.
     * @return a {@link java.lang.String} object.
     */
    public static String printAbundanceAssay(Assay a) {
        StringBuilder sb = new StringBuilder();
        return sb.append("abundance_assay[").
            append(a.getId()).
            append("]").
            toString();
    }

    /**
     * <p>printAbundanceStudyVar.</p>
     *
     * @param sv a {@link de.isas.mztab1_1.model.StudyVariable} object.
     * @return a {@link java.lang.String} object.
     */
    public static String printAbundanceStudyVar(StudyVariable sv) {
        StringBuilder sb = new StringBuilder();
        return sb.append("abundance_study_variable[").
            append(sv.getId()).
            append("]").
            toString();
    }

    /**
     * <p>printAbundanceCoeffVarStudyVar.</p>
     *
     * @param sv a {@link de.isas.mztab1_1.model.StudyVariable} object.
     * @return a {@link java.lang.String} object.
     */
    public static String printAbundanceCoeffVarStudyVar(StudyVariable sv) {
        StringBuilder sb = new StringBuilder();
        return sb.append("abundance_coeffvar_study_variable[").
            append(sv.getId()).
            append("]").
            toString();
    }

    /**
     * <p>printOptColumnMapping.</p>
     *
     * @param ocm a {@link de.isas.mztab1_1.model.OptColumnMapping} object.
     * @return a {@link java.lang.String} object.
     */
    public static String printOptColumnMapping(OptColumnMapping ocm) {
        StringBuilder sb = new StringBuilder();
        sb.append("opt_").
            append(ocm.getIdentifier());
        if (ocm.getParam() != null) {
            sb.append("_cv_").
                append(ocm.getParam().
                    getCvAccession()).
                append("_").
                append(ocm.getParam().
                    getName().
                    replaceAll(" ", "_"));
        }
        //TODO: check for valid characters in definition
        //valid characters: ‘A’-‘Z’, ‘a’-‘z’, ‘0’-‘9’, ‘’, ‘-’, ‘[’, ‘]’, and ‘:’.
        //[A-Za-z0-9\[\]-:]+
        return sb.toString();
    }

    /**
     * <p>addIndexedLine.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param parameter a {@link de.isas.mztab1_1.model.Parameter} object.
     */
    public static void addIndexedLine(JsonGenerator jg, String prefix,
        Object element, Parameter parameter) {
        addIndexedLine(jg, prefix, element, Arrays.asList(parameter));
    }

    /**
     * <p>addIndexedLine.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param parameterList a {@link java.util.List} object.
     */
    public static void addIndexedLine(JsonGenerator jg, String prefix,
        Object element,
        List<Parameter> parameterList) {
        if (parameterList == null || parameterList.isEmpty()) {
            Logger.getLogger(Serializers.class.getName()).
                fine(
                    "Skipping null or empty parameter list values for " + getElementName(
                        element));
            return;
        }
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
            Logger.getLogger(Serializers.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    /**
     * <p>addLineWithParameters.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param parameterList a {@link java.util.List} object.
     */
    public static void addLineWithParameters(JsonGenerator jg, String prefix,
        Object element,
        List<Parameter> parameterList) {
        if (parameterList == null || parameterList.isEmpty()) {
            Logger.getLogger(Serializers.class.getName()).
                fine(
                    "Skipping null or empty parameter list values for " + getElementName(
                        element));
            return;
        }
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

    /**
     * <p>addLineWithPropertyParameters.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param value a {@link java.util.List} object.
     */
    public static void addLineWithPropertyParameters(JsonGenerator jg,
        String prefix,
        String propertyName, Object element,
        List<Parameter> value) {
        if (value == null || value.isEmpty()) {
            Logger.getLogger(Serializers.class.getName()).
                fine("Skipping null or empty values for " + getElementName(
                    element));
            return;
        }
        addLineWithProperty(jg, prefix, propertyName, element, value.stream().
            map((parameter) ->
            {
                return ParameterSerializer.toString(parameter);
            }).
            collect(Collectors.joining("|")));
    }

    /**
     * <p>addLineWithMetadataProperty.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param property a {@link uk.ac.ebi.pride.jmztab1_1.model.MetadataProperty} object.
     * @param element a {@link java.lang.Object} object.
     * @param value a {@link java.lang.String} object.
     */
    public static void addLineWithMetadataProperty(JsonGenerator jg,
        String prefix, MetadataProperty property, Object element,
        String... value) {
        addLineWithProperty(jg, prefix, property.getName(), element, value);
    }

    /**
     * <p>addLineWithProperty.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param value a {@link java.lang.String} object.
     */
    public static void addLineWithProperty(JsonGenerator jg, String prefix,
        String propertyName, Object element,
        String... value) {
        if (value == null || value.length == 0) {
            Logger.getLogger(Serializers.class.getName()).
                fine("Skipping null or empty values for " + getElementName(
                    element));
            return;
        }
        if (value.length == 1 && (value[0] == null || value[0].isEmpty())) {
            Logger.getLogger(Serializers.class.getName()).
                fine("Skipping empty value for " + getElementName(
                    element));
            return;
        }
        try {
            jg.writeStartArray();
            //prefix
            jg.writeString(prefix);
            //key
            String key = getElementName(element).
                orElseThrow(() ->
                {
                    return new ElementNameMappingException(element);
                });
            if (propertyName == null) {
                jg.writeString(key);
            } else {
                jg.writeString(key + "-" + propertyName);
            }
            //value
            for (String s : value) {
                jg.writeString(s);
            }
            jg.writeEndArray();
        } catch (IOException ex) {
            Logger.getLogger(Serializers.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    /**
     * <p>addLine.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param value a {@link java.lang.String} object.
     */
    public static void addLine(JsonGenerator jg, String prefix, Object element,
        String... value) {
        addLineWithProperty(jg, prefix, null, element, value);
    }

    /**
     * <p>getElementName.</p>
     *
     * @param element a {@link java.lang.Object} object.
     * @return a {@link java.util.Optional} object.
     */
    public static Optional<String> getElementName(Object element) {
        if (element instanceof String) {
            return Optional.of((String) element);
        }
        if (element instanceof MetadataElement) {
            return Optional.ofNullable(((MetadataElement) element).getName());
        }
        JacksonXmlRootElement rootElement = element.getClass().
            getAnnotation(JacksonXmlRootElement.class);
        if (rootElement != null) {
            String underscoreName = camelCaseToUnderscoreLowerCase(
                rootElement.localName());
            if (element instanceof IndexedElement) {
                return Optional.of(
                    underscoreName + "[" + ((IndexedElement) element).getId() + "]");
            }
            return Optional.ofNullable(underscoreName);
        }
        return Optional.empty();
    }

    /**
     * <p>getPropertyNames.</p>
     *
     * @param element a {@link java.lang.Object} object.
     * @return a {@link java.util.List} object.
     */
    public static List<String> getPropertyNames(Object element) {
        return Arrays.asList(element.getClass().
            getAnnotationsByType(JsonProperty.class)).
            stream().
            map((jsonProperty) ->
            {
                return jsonProperty.value();
            }).
            collect(Collectors.toList());
    }

    /**
     * <p>asMap.</p>
     *
     * @param element a {@link java.lang.Object} object.
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, Object> asMap(Object element) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(element, Map.class);
    }

    /**
     * <p>camelCaseToUnderscoreLowerCase.</p>
     *
     * @param camelCase a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String camelCaseToUnderscoreLowerCase(String camelCase) {
        Matcher m = Pattern.compile("(?<=[a-z])[A-Z]").
            matcher(camelCase);

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "_" + m.group().
                toLowerCase());
        }
        m.appendTail(sb);
        return sb.toString().
            toLowerCase();
    }

    /**
     * <p>addSubElementStrings.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param subElementName a {@link java.lang.String} object.
     * @param subElements a {@link java.util.List} object.
     * @param oneLine a boolean.
     */
    public static void addSubElementStrings(JsonGenerator jg, String prefix,
        Object element, String subElementName, List<String> subElements,
        boolean oneLine) {
        if (checkForNull(element, subElements, subElementName)) {
            return;
        };
        String elementName = Serializers.getElementName(element).
            get();
        if (oneLine) {
            addLine(jg, prefix,
                elementName + "-" + subElementName,
                subElements.stream().
                    collect(Collectors.joining("" + MZTabConstants.BAR)));
        } else {
            IntStream.range(0, subElements.
                size()).
                forEachOrdered(i ->
                {
                    addLine(jg, prefix,
                        elementName + "-" + subElementName + "[" + (i + 1) + "]",
                        subElements.
                            get(i));
                });
        }
    }

    /**
     * <p>addSubElementParameter.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param subElementName a {@link java.lang.String} object.
     * @param subElement a {@link de.isas.mztab1_1.model.Parameter} object.
     */
    public static void addSubElementParameter(JsonGenerator jg, String prefix,
        Object element, String subElementName, Parameter subElement) {
        if (subElement == null) {
            String elementName = Serializers.getElementName(element).
                get();
            Logger.getLogger(Serializers.class.getName()).
                log(Level.FINE,
                    "'" + elementName + "-" + subElementName + "' is null or empty!");
            return;
        }
        addSubElementStrings(jg, prefix, element, subElementName, Arrays.asList(
            ParameterSerializer.toString(subElement)), true);
    }

    /**
     * <p>addSubElementParameters.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param subElementName a {@link java.lang.String} object.
     * @param subElements a {@link java.util.List} object.
     * @param oneLine a boolean.
     */
    public static void addSubElementParameters(JsonGenerator jg, String prefix,
        Object element, String subElementName, List<Parameter> subElements,
        boolean oneLine) {
        if (checkForNull(element, subElements, subElementName)) {
            return;
        };
        addSubElementStrings(jg, prefix, element, subElementName,
            subElements.stream().
                map((parameter) ->
                {
                    try {
                        return ParameterSerializer.toString(parameter);
                    } catch (IllegalArgumentException npe) {
                        Logger.getLogger(Serializers.class.getName()).
                            log(Level.FINE,
                                "parameter is null for " + subElementName);
                        System.err.println();
                        return "null";
                    }
                }).
                collect(Collectors.toList()), oneLine);
    }

    /**
     * <p>checkForNull.</p>
     *
     * @param element a {@link java.lang.Object} object.
     * @param subElements a {@link java.util.List} object.
     * @param subElementName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean checkForNull(Object element, List<?> subElements,
        String subElementName) {
        String elementName = Serializers.getElementName(element).
            get();
        if (subElements == null || subElements.isEmpty()) {
            Logger.getLogger(Serializers.class.getName()).
                log(Level.FINE,
                    "'" + elementName + "-" + subElementName + "' is null or empty!");
            return true;
        }
        return false;
    }

    /**
     * <p>writeString.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public static void writeString(String columnName, JsonGenerator jg,
        String value) throws IOException {
        jg.writeStringField(columnName, value);
    }

    /**
     * <p>writeString.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public static void writeString(IMZTabColumn column, JsonGenerator jg,
        String value) throws IOException {
        writeString(column.getHeader(), jg, value);
    }

    /**
     * <p>writeAsNumberArray.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsNumberArray(IMZTabColumn column, JsonGenerator jg,
        List<? extends Number> elements) {
        writeAsNumberArray(column.getHeader(), jg, elements);
    }

    /**
     * <p>writeAsNumberArray.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsNumberArray(String columnName, JsonGenerator jg,
        List<? extends Number> elements) {
        String arrayElements = elements.stream().
            map((number) ->
            {
                return "" + number.doubleValue();
            }).
            collect(Collectors.joining("" + MZTabConstants.BAR));
        try {
            if (arrayElements.isEmpty()) {
                jg.writeNullField(columnName);
            } else {
                jg.writeStringField(columnName, arrayElements);
            }
        } catch (IOException ex) {
            Logger.getLogger(Serializers.class.
                getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    /**
     * <p>writeAsStringArray.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsStringArray(IMZTabColumn column, JsonGenerator jg,
        List<String> elements) {
        writeAsStringArray(column.getHeader(), jg, elements);
    }

    /**
     * <p>writeAsStringArray.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsStringArray(JsonGenerator jg,
        List<String> elements) {
        String arrayElements = elements.stream().
            collect(Collectors.joining("" + MZTabConstants.BAR));
        try {
            if (arrayElements.isEmpty()) {
                jg.writeNull();
            } else {
                jg.writeString(arrayElements);
            }
        } catch (IOException ex) {
            Logger.getLogger(Serializers.class.
                getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    /**
     * <p>writeAsStringArray.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsStringArray(String columnName, JsonGenerator jg,
        List<String> elements) {
        String arrayElements = elements.stream().
            collect(Collectors.joining("" + MZTabConstants.BAR));
        try {
            if (arrayElements.isEmpty()) {
                jg.writeNullField(columnName);
            } else {
                jg.writeStringField(columnName, arrayElements);
            }
        } catch (IOException ex) {
            Logger.getLogger(Serializers.class.
                getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    /**
     * <p>writeNumber.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Integer} object.
     * @throws java.io.IOException if any.
     */
    public static void writeNumber(String columnName, JsonGenerator jg,
        Integer value) throws IOException {
        jg.writeFieldName(columnName);
        if (value == null) {
            jg.writeNull();
        } else {
            jg.writeNumber(value);
        }
    }

    /**
     * <p>writeNumber.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Integer} object.
     * @throws java.io.IOException if any.
     */
    public static void writeNumber(IMZTabColumn column, JsonGenerator jg,
        Integer value) throws IOException {
        writeNumber(column.getHeader(), jg, value);
    }

    /**
     * <p>writeNumber.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Double} object.
     * @throws java.io.IOException if any.
     */
    public static void writeNumber(String columnName, JsonGenerator jg,
        Double value) throws IOException {
        jg.writeFieldName(columnName);
        if (value == null) {
            jg.writeNull();
        } else {
            jg.writeNumber(value);
        }
    }

    /**
     * <p>writeNumber.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab1_1.model.IMZTabColumn} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Double} object.
     * @throws java.io.IOException if any.
     */
    public static void writeNumber(IMZTabColumn column, JsonGenerator jg,
        Double value) throws IOException {
        writeNumber(column.getHeader(), jg, value);
    }

    /**
     * <p>writeNumber.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Integer} object.
     * @throws java.io.IOException if any.
     */
    public static void writeNumber(JsonGenerator jg, Integer value) throws IOException {
        if (value == null) {
            jg.writeNull();
        } else {
            jg.writeNumber(value);
        }
    }

    /**
     * <p>writeNumber.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Double} object.
     * @throws java.io.IOException if any.
     */
    public static void writeNumber(JsonGenerator jg, Double value) throws IOException {
        if (value == null) {
            jg.writeNull();
        } else {
            jg.writeNumber(value);
        }
    }

    /**
     * <p>writeOptColumnMappings.</p>
     *
     * @param optColumnMappings a {@link java.util.List} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @throws java.io.IOException if any.
     */
    public static void writeOptColumnMappings(
        List<OptColumnMapping> optColumnMappings,
        JsonGenerator jg) throws IOException {
        for (OptColumnMapping ocm : Optional.ofNullable(
            optColumnMappings).
            orElse(Collections.emptyList())) {
            String value = NULL;
            if (ocm.getParam() != null) {
                value = ParameterSerializer.toString(ocm.getParam());
            } else {
                value = ocm.getValue() == null ? NULL : ocm.getValue();
            }
            writeString(Serializers.printOptColumnMapping(ocm), jg, value);
        }
    }

    /**
     * <p>writeIndexedValues.</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param values a {@link java.util.List} object.
     */
    public static void writeIndexedValues(String prefix,
        JsonGenerator jg, List<Double> values) {
        IntStream.range(0, values.
            size()).
            forEachOrdered(i ->
            {
                try {
                    Serializers.writeNumber(
                        prefix + "[" + (i + 1) + "]",
                        jg,
                        values.
                            get(i));
                } catch (IOException ex) {
                    Logger.getLogger(SmallMoleculeSummarySerializer.class.
                        getName()).
                        log(Level.SEVERE, null, ex);
                }
            });
    }

}
