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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import de.isas.mztab1_1.model.IndexedElement;
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.Parameter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataElement;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 * @since 11/30/17
 */
public class Serializers {

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

    public static String printOptColumnMapping(OptColumnMapping ocm) {
        StringBuilder sb = new StringBuilder();
        sb.append("opt_").
            append(ocm.getIdentifier());
        if (ocm.getParam() != null) {
            sb.append("_cv_").
                append(ocm.getParam().
                    getCvAccession()).
                append(ocm.getParam().
                    getName().
                    replaceAll(" ", "_"));
        }
        //valid characters: ‘A’-‘Z’, ‘a’-‘z’, ‘0’-‘9’, ‘’, ‘-’, ‘[’, ‘]’, and ‘:’.
        //[A-Za-z0-9\[\]-:]+
        return sb.toString();
    }

    public static void addIndexedLine(JsonGenerator jg, String prefix,
        Object element, Parameter parameter) {
        addIndexedLine(jg, prefix, element, Arrays.asList(parameter));
    }

    public static void addIndexedLine(JsonGenerator jg, String prefix,
        Object element,
        List<Parameter> parameterList) {
        if (parameterList == null || parameterList.isEmpty()) {
            Logger.getLogger(MetadataSerializer.class.getName()).
                info(
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

    public static void addLineWithParameters(JsonGenerator jg, String prefix,
        Object element,
        List<Parameter> parameterList) {
        if (parameterList == null || parameterList.isEmpty()) {
            Logger.getLogger(MetadataSerializer.class.getName()).
                info(
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

    public static void addLineWithPropertyParameters(JsonGenerator jg,
        String prefix,
        String propertyName, Object element,
        List<Parameter> value) {
        if (value == null || value.isEmpty()) {
            Logger.getLogger(MetadataSerializer.class.getName()).
                info("Skipping null or empty values for " + getElementName(
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

    public static void addLineWithProperty(JsonGenerator jg, String prefix,
        String propertyName, Object element,
        String... value) {
        if (value == null || value.length == 0) {
            Logger.getLogger(MetadataSerializer.class.getName()).
                info("Skipping null or empty values for " + getElementName(
                    element));
            return;
        }
        if (value.length == 1 && (value[0] == null || value[0].isEmpty())) {
            Logger.getLogger(MetadataSerializer.class.getName()).
                info("Skipping empty value for " + getElementName(
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
            Logger.getLogger(MetadataSerializer.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    public static void addLine(JsonGenerator jg, String prefix, Object element,
        String... value) {
        addLineWithProperty(jg, prefix, null, element, value);
    }

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

    public static Map<String, Object> asMap(Object element) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(element, Map.class);
    }

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

    public static void addSubElementParameter(JsonGenerator jg, String prefix,
        Object element, String subElementName, Parameter subElement) {
        if (subElement == null) {
            String elementName = Serializers.getElementName(element).
                get();
            System.err.println(
                "'" + elementName + "-" + subElementName + "' is null or empty!");
            return;
        }
        addSubElementStrings(jg, prefix, element, subElementName, Arrays.asList(
            ParameterSerializer.toString(subElement)), true);
    }

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
                        System.err.println(
                            "parameter is null for " + subElementName);
                        return "null";
                    }
                }).
                collect(Collectors.toList()), oneLine);
    }

    public static boolean checkForNull(Object element, List<?> subElements,
        String subElementName) {
        String elementName = Serializers.getElementName(element).
            get();
        if (subElements == null || subElements.isEmpty()) {
            System.err.println(
                "'" + elementName + "-" + subElementName + "' is null or empty!");
            return true;
        }
        return false;
    }

    public static void writeAsNumberArray(JsonGenerator jg,
        List<? extends Number> elements) {
        try {
            jg.writeStartArray();
            elements.forEach((element) ->
            {
                try {
                    Serializers.writeNumber(jg, element.doubleValue());
                } catch (IOException ex) {
                    Logger.getLogger(SmallMoleculeSummarySerializer.class.
                        getName()).
                        log(Level.SEVERE, null, ex);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(SmallMoleculeSummarySerializer.class.getName()).
                log(Level.SEVERE, null, ex);
        } finally {
            try {
                jg.writeEndArray();
            } catch (IOException ex) {
                Logger.getLogger(SmallMoleculeSummarySerializer.class.getName()).
                    log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void writeAsStringArray(JsonGenerator jg,
        List<String> elements) {
        try {
            jg.writeStartArray();
            elements.forEach((element) ->
            {
                try {
                    jg.writeString(element);
                } catch (IOException ex) {
                    Logger.getLogger(SmallMoleculeSummarySerializer.class.
                        getName()).
                        log(Level.SEVERE, null, ex);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(SmallMoleculeSummarySerializer.class.getName()).
                log(Level.SEVERE, null, ex);
        } finally {
            try {
                jg.writeEndArray();
            } catch (IOException ex) {
                Logger.getLogger(SmallMoleculeSummarySerializer.class.getName()).
                    log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void writeNumber(JsonGenerator jg, Integer value) throws IOException {
        if(value==null) {
            jg.writeNull();
        } else {
            jg.writeNumber(value);
        }
    }
    
    public static void writeNumber(JsonGenerator jg, Double value) throws IOException {
        if(value==null) {
            jg.writeNull();
        } else {
            jg.writeNumber(value);
        }
    }

}
