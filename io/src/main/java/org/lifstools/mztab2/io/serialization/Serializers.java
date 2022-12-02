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
package org.lifstools.mztab2.io.serialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.IndexedElement;
import org.lifstools.mztab2.model.OptColumnMapping;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.Uri;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NULL;
import uk.ac.ebi.pride.jmztab2.model.MetadataElement;
import uk.ac.ebi.pride.jmztab2.model.MetadataProperty;

/**
 * <p>
 * Utility class providing helper methods for other serializers.</p>
 *
 * @author nilshoffmann
 * @since 11/30/17
 *
 */
@Slf4j
public class Serializers {

    /**
     * <p>
     * getReference.</p>
     *
     * @param element a {@link java.lang.Object} object.
     * @param idx a {@link java.lang.Integer} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getReference(Object element, Integer idx) {
        StringBuilder sb = new StringBuilder();
        sb.append(getElementName(element).
                orElseThrow(()
                        -> {
                    return new IllegalArgumentException(
                            "No mzTab element name mapping available for " + element.
                                    getClass().
                                    getName());
                }));

        return sb.toString();
    }

    /**
     * <p>
     * printAbundanceAssay.</p>
     *
     * @param a a {@link org.lifstools.mztab2.model.Assay} object.
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
     * <p>
     * printAbundanceStudyVar.</p>
     *
     * @param sv a {@link org.lifstools.mztab2.model.StudyVariable} object.
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
     * <p>
     * printAbundanceCoeffVarStudyVar.</p>
     *
     * @param sv a {@link org.lifstools.mztab2.model.StudyVariable} object.
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
     * <p>
     * printOptColumnMapping.</p>
     *
     * @param ocm a {@link org.lifstools.mztab2.model.OptColumnMapping} object.
     * @return a {@link java.lang.String} object.
     */
    public static String printOptColumnMapping(OptColumnMapping ocm) {
        StringBuilder sb = new StringBuilder();
        log.debug("Identifier={}; OptColumnMapping: {}", ocm.getIdentifier(), ocm);
        if ("global".equals(ocm.getIdentifier()) || ocm.getIdentifier().startsWith("global")) {
            sb.append("opt_");
            sb.append(ocm.getIdentifier());
            if (ocm.getParam() != null) {
                sb.append("_cv_").
                        append(ocm.getParam().
                                getCvAccession()).
                        append("_").
                        append(ocm.getParam().
                                getName().
                                replaceAll(" ", "_"));
            }
        } else {
            log.debug("OptColumnMapping: {}", ocm);
            // object reference case, value is now the actual value
            sb.append(ocm.getIdentifier());
        }
        log.debug("asString: {}", sb.toString());
//        //TODO: check for valid characters in definition
//        //valid characters: ‘A’-‘Z’, ‘a’-‘z’, ‘0’-‘9’, ‘’, ‘-’, ‘[’, ‘]’, and ‘:’.
//        //[A-Za-z0-9\[\]-:]+
        return sb.toString();
    }

    /**
     * <p>
     * addIndexedLine for elements like assay[1] that have an id and one
     * additional property element</p>
     *
     * @param <T> the type of {@link IndexedElement}.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider}
     * object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param indexedElement a {@link org.lifstools.mztab2.model.Parameter} object.
     */
    public static <T extends Object> void addIndexedLine(
            JsonGenerator jg, SerializerProvider sp, String prefix,
            Object element, T indexedElement) {
        addIndexedLine(jg, sp, prefix, element, Arrays.asList(indexedElement));
    }

    /**
     * <p>
     * addIndexedLine for elements like assay[1] that have an id and a list of
     * additional property elements</p>
     *
     * @param <T> the type of {@link IndexedElement}.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider}
     * object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param indexedElementList a {@link java.util.List} object.
     */
    public static <T extends Object> void addIndexedLine(
            JsonGenerator jg, SerializerProvider sp, String prefix,
            Object element,
            List<T> indexedElementList) {
        Optional<List<T>> iel = Optional.ofNullable(indexedElementList);
        if (!iel.isPresent() || indexedElementList.isEmpty()) {

            log.debug(
                    "Skipping null or empty indexed element list values for {}",
                    getElementName(
                            element));
            return;
        }
        try {
            jg.writeStartArray();
            //prefix
            jg.writeString(prefix);
            //key
            jg.writeString(new StringBuilder().append(getElementName(element).
                    orElseThrow(()
                            -> {
                        return new ElementNameMappingException("unknown", element);
                    })).
                    toString());
            //value
            jg.writeString(indexedElementList.stream().
                    map((indexedElement)
                            -> {
                        if (indexedElement instanceof Parameter) {
                            return new ParameterConverter().convert(
                                    (Parameter) indexedElement);
                        } else if (indexedElement instanceof Uri) {
                            return new UriConverter().convert((Uri) indexedElement);
                        } else {
                            throw new IllegalArgumentException(
                                    "Serialization of type " + indexedElement.getClass() + " currently not supported!");
                        }
                    }).
                    collect(Collectors.joining("|")));
            jg.writeEndArray();
        } catch (IOException ex) {

            log.error("Caught IO Exception while trying to write indexed line:",
                    ex);
        }
    }

    /**
     * <p>
     * addLineWithParameters.</p>
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

            log.debug(
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
                    orElseThrow(()
                            -> {
                        return new ElementNameMappingException("unknown", element);
                    })).
                    toString());
            //value
            jg.writeString(parameterList.stream().
                    map((parameter)
                            -> {
                        return new ParameterConverter().convert(parameter);
                    }).
                    collect(Collectors.joining("|")));
            jg.writeEndArray();
        } catch (IOException ex) {
            log.error(
                    "Caught IO Exception while trying to write line with parameters:",
                    ex);
        }
    }

    /**
     * <p>
     * addLineWithPropertyParameters.</p>
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

            log.debug("Skipping null or empty values for {}",
                    getElementName(
                            element));
            return;
        }
        addLineWithProperty(jg, prefix, propertyName, element, value.stream().
                map((parameter)
                        -> {
                    return new ParameterConverter().convert(parameter);
                }).
                collect(Collectors.joining("|")));
    }

    /**
     * <p>
     * addLineWithMetadataProperty.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param property a {@link uk.ac.ebi.pride.jmztab2.model.MetadataProperty}
     * object.
     * @param element a {@link java.lang.Object} object.
     * @param value a {@link java.lang.Object} object.
     */
    public static void addLineWithMetadataProperty(JsonGenerator jg,
            String prefix, MetadataProperty property, Object element,
            Object... value) {
        addLineWithProperty(jg, prefix, property.getName(), element, value);
    }

    /**
     * <p>
     * addLineWithNullProperty.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     */
    public static void addLineWithNullProperty(JsonGenerator jg, String prefix,
            String propertyName, Object element) {
        try {
            jg.writeStartArray();
            //prefix
            jg.writeString(prefix);
            //key
            String key = getElementName(element).
                    orElseThrow(()
                            -> {
                        return new ElementNameMappingException(propertyName, element);
                    });
            if (propertyName == null) {
                jg.writeString(key);
            } else {
                jg.writeString(key + "-" + propertyName);
            }
            //value
            jg.writeString(NULL);
            jg.writeEndArray();
        } catch (IOException ex) {

            log.error(
                    "Caught exception while trying to write line with null property:",
                    ex);
        }
    }

    /**
     * <p>
     * addLineWithProperty.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param value a {@link java.lang.Object} object.
     */
    public static void addLineWithProperty(JsonGenerator jg, String prefix,
            String propertyName, Object element,
            Object... value) {
        if (value == null || value.length == 0) {

            log.debug("Skipping null or empty values for {}",
                    getElementName(
                            element));
            return;
        }
        if (value.length == 1 && (value[0] == null)) {

            log.debug("Skipping empty value for {}", getElementName(
                    element));
            return;
        }
        try {
            jg.writeStartArray();
            //prefix
            jg.writeString(prefix);
            //key
            String key = getElementName(element).
                    orElseThrow(()
                            -> {
                        return new ElementNameMappingException(propertyName, element);
                    });
            if (propertyName == null) {
                jg.writeString(key);
            } else {
                jg.writeString(key + "-" + propertyName);
            }
            //value
            for (Object o : value) {
                jg.writeObject(o);
            }
            jg.writeEndArray();
        } catch (IOException ex) {

            log.error(
                    "Caught IO exception while trying to write line with property:",
                    ex);
        }
    }

    /**
     * <p>
     * addLine.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param value a {@link java.lang.Object} object.
     */
    public static void addLine(JsonGenerator jg, String prefix, Object element,
            Object... value) {
        addLineWithProperty(jg, prefix, null, element, value);
    }

    /**
     * <p>
     * getElementName.</p>
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
                Integer id = Optional.ofNullable(((IndexedElement) element).getId()).orElseThrow(()
                        -> new NullPointerException(
                                "Field 'id' must not be null for element '" + underscoreName + "'!")
                );
                return Optional.of(
                        underscoreName + "[" + id + "]");
            } 
            IndexedElement ielement = IndexedElement.of(element);
            if(ielement!=null) {
                Integer id = Optional.ofNullable((ielement).getId()).orElseThrow(()
                        -> new NullPointerException(
                                "Field 'id' must not be null for element '" + underscoreName + "'!")
                );
                return Optional.of(
                        underscoreName + "[" + id + "]");
            }
                
            return Optional.ofNullable(underscoreName);
        }
        return Optional.empty();
    }

    /**
     * <p>
     * getPropertyNames.</p>
     *
     * @param element a {@link java.lang.Object} object.
     * @return a {@link java.util.List} object.
     */
    public static List<String> getPropertyNames(Object element) {
        return Arrays.asList(element.getClass().
                getAnnotationsByType(JsonProperty.class)).
                stream().
                map((jsonProperty)
                        -> {
                    return jsonProperty.value();
                }).
                collect(Collectors.toList());
    }

    /**
     * <p>
     * asMap.</p>
     *
     * @param element a {@link java.lang.Object} object.
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, Object> asMap(Object element) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(element, Map.class);
    }

    /**
     * <p>
     * camelCaseToUnderscoreLowerCase.</p>
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
     * <p>
     * addSubElementStrings.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param subElementName a {@link java.lang.String} object.
     * @param subElements a {@link java.util.List} object.
     * @param oneLine a boolean.
     */
    public static void addSubElementStrings(JsonGenerator jg, String prefix,
            Object element, String subElementName, List<?> subElements,
            boolean oneLine) {
        if (checkForNull(element, subElements, subElementName)) {
            return;
        }
        Serializers.getElementName(element).
                ifPresent((elementName)
                        -> {
                    if (oneLine) {
                        addLine(jg, prefix,
                                elementName + "-" + subElementName,
                                subElements.stream().
                                        map((t)
                                                -> {
                                            return t.toString();
                                        }).
                                        collect(Collectors.joining("" + MZTabConstants.BAR)));
                    } else {
                        IntStream.range(0, subElements.
                                size()).
                                forEachOrdered(i
                                        -> {
                                    addLine(jg, prefix,
                                            elementName + "-" + subElementName + "[" + (i + 1) + "]",
                                            subElements.
                                                    get(i));
                                });
                    }
                });

    }

    /**
     * <p>
     * addSubElementParameter.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param prefix a {@link java.lang.String} object.
     * @param element a {@link java.lang.Object} object.
     * @param subElementName a {@link java.lang.String} object.
     * @param subElement a {@link org.lifstools.mztab2.model.Parameter} object.
     */
    public static void addSubElementParameter(JsonGenerator jg, String prefix,
            Object element, String subElementName, Parameter subElement) {
        if (subElement == null) {
            String elementName = Serializers.getElementName(element).
                    orElse("undefined");

            log.debug("''{}-{}'' is null or empty!", new Object[]{
                elementName,
                subElementName});
            return;
        }
        addSubElementStrings(jg, prefix, element, subElementName, Arrays.asList(
                new ParameterConverter().convert(subElement)), true);
    }

    /**
     * <p>
     * addSubElementParameters.</p>
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
        }
        addSubElementStrings(jg, prefix, element, subElementName,
                subElements.stream().
                        map((parameter)
                                -> {
                            try {
                                return new ParameterConverter().convert(parameter);
                            } catch (IllegalArgumentException npe) {

                                log.debug("parameter is null for {}",
                                        subElementName);
                                return "null";
                            }
                        }).
                        collect(Collectors.toList()), oneLine);
    }

    /**
     * <p>
     * checkForNull.</p>
     *
     * @param element a {@link java.lang.Object} object.
     * @param subElements a {@link java.util.List} object.
     * @param subElementName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean checkForNull(Object element, List<?> subElements,
            String subElementName) {
        String elementName = Serializers.getElementName(element).
                orElse("undefined");
        if (subElements == null || subElements.isEmpty()) {

            log.debug("''{}-{}'' is null or empty!", new Object[]{
                elementName,
                subElementName});
            return true;
        }
        return false;
    }

    /**
     * <p>
     * writeString.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.String} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeString(String columnName, JsonGenerator jg,
            String value) throws IOException {
        if (value == null) {
            jg.writeNullField(columnName);
        } else {
            jg.writeStringField(columnName, value);
        }
    }

    /**
     * <p>
     * writeString.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn}
     * object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.String} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeString(IMZTabColumn column, JsonGenerator jg,
            String value) throws IOException {
        writeString(column.getHeader(), jg, value);
    }

    /**
     * <p>
     * writeObject.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider}
     * object.
     * @param value a {@link java.lang.Object} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeObject(String columnName, JsonGenerator jg,
            SerializerProvider sp,
            Object value) throws IOException {
        if (value == null) {
            jg.writeNullField(columnName);
        } else {
            if (value instanceof Parameter) {
                jg.writeStringField(columnName, new ParameterConverter().
                        convert((Parameter) value));
            } else if (value instanceof String) {
                jg.writeStringField(columnName, (String) value);
            } else {
                throw new IllegalArgumentException(
                        "Serialization of objects of type " + value.getClass()
                        + " currently not supported!");
            }

        }
    }

    /**
     * <p>
     * writeObject.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn}
     * object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider}
     * object.
     * @param value a {@link java.lang.Object} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeObject(IMZTabColumn column, JsonGenerator jg,
            SerializerProvider sp, Object value) throws IOException {
        writeObject(column.getHeader(), jg, sp, value);
    }

    /**
     * <p>
     * writeAsNumberArray.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn}
     * object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsNumberArray(IMZTabColumn column, JsonGenerator jg,
            List<? extends Number> elements) {
        writeAsNumberArray(column.getHeader(), jg, elements);
    }

    /**
     * <p>
     * writeAsNumberArray.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsNumberArray(String columnName, JsonGenerator jg,
            List<? extends Number> elements) {
        try {
            if (elements == null) {
                jg.writeNullField(columnName);
            } else {
                String arrayElements = elements.stream().
                        map((number)
                                -> {
                            if (number == null) {
                                return MZTabConstants.NULL;
                            } else if (number instanceof Short) {
                                return "" + number.shortValue();
                            } else if (number instanceof Integer) {
                                return "" + number.intValue();
                            } else if (number instanceof Long) {
                                return "" + number.longValue();
                            } else if (number instanceof Float) {
                                return "" + number.floatValue();
                            } else {
                                return "" + number.doubleValue();
                            }
                        }).
                        collect(Collectors.joining("" + MZTabConstants.BAR));
                if (arrayElements.isEmpty()) {
                    jg.writeNullField(columnName);
                } else {
                    jg.writeStringField(columnName, arrayElements);
                }
            }
        } catch (IOException ex) {
            log.error(
                    "Caught IO exception while trying to write as number array: ",
                    ex);
        }
    }

    /**
     * <p>
     * writeAsStringArray.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn}
     * object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsStringArray(IMZTabColumn column, JsonGenerator jg,
            List<String> elements) {
        writeAsStringArray(column.getHeader(), jg, elements);
    }

    /**
     * <p>
     * writeAsStringArray.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsStringArray(JsonGenerator jg,
            List<String> elements) {
        try {
            if (elements == null) {
                jg.writeNull();
            } else {
                String arrayElements = elements.stream().
                        map((t)
                                -> {
                            if (t == null) {
                                return MZTabConstants.NULL;
                            }
                            return t;
                        }).
                        collect(Collectors.joining("" + MZTabConstants.BAR));
                if (arrayElements.isEmpty()) {
                    jg.writeNull();
                } else {
                    jg.writeString(arrayElements);
                }
            }
        } catch (IOException ex) {
            log.error("Error while trying to write as string array:", ex);
        }
    }

    /**
     * <p>
     * writeAsStringArray.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param elements a {@link java.util.List} object.
     */
    public static void writeAsStringArray(String columnName, JsonGenerator jg,
            List<String> elements) {
        try {
            if (elements == null) {
                jg.writeNullField(columnName);
            } else {
                String arrayElements = elements.stream().
                        map((t)
                                -> {
                            if (t == null) {
                                return MZTabConstants.NULL;
                            }
                            return t;
                        }).
                        collect(Collectors.joining("" + MZTabConstants.BAR));
                if (arrayElements.isEmpty()) {
                    jg.writeNullField(columnName);
                } else {
                    jg.writeStringField(columnName, arrayElements);
                }
            }
        } catch (IOException ex) {
            log.error("Error while trying to write as string array: ", ex);
        }
    }

    /**
     * <p>
     * writeNumber.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Integer} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeNumber(String columnName, JsonGenerator jg,
            Integer value) throws IOException {
        if (value == null) {
            jg.writeNullField(columnName);
        } else {
            jg.writeNumberField(columnName, value);
        }
    }

    /**
     * <p>
     * writeNumber.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn}
     * object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Integer} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeNumber(IMZTabColumn column, JsonGenerator jg,
            Integer value) throws IOException {
        writeNumber(column.getHeader(), jg, value);
    }

    /**
     * <p>
     * writeNumber.</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Double} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeNumber(String columnName, JsonGenerator jg,
            Double value) throws IOException {
        if (value == null) {
            jg.writeNullField(columnName);
        } else {
            if (value.equals(Double.NaN)) {
                jg.writeStringField(columnName, MZTabConstants.CALCULATE_ERROR);
            } else if (value.equals(Double.POSITIVE_INFINITY)) {
                jg.writeStringField(columnName, MZTabConstants.INFINITY);
            } else {
                jg.writeNumberField(columnName, value);
            }
        }
    }

    /**
     * <p>
     * writeNumber.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn}
     * object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Double} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeNumber(IMZTabColumn column, JsonGenerator jg,
            Double value) throws IOException {
        writeNumber(column.getHeader(), jg, value);
    }

    /**
     * <p>
     * writeNumber.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Integer} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeNumber(JsonGenerator jg, Integer value) throws IOException {
        if (value == null) {
            jg.writeNull();
        } else {
            jg.writeNumber(value);
        }
    }

    /**
     * <p>
     * writeNumber.</p>
     *
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param value a {@link java.lang.Double} object.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeNumber(JsonGenerator jg, Double value) throws IOException {
        if (value == null) {
            jg.writeNull();
        } else {
            jg.writeNumber(value);
        }
    }

    /**
     * <p>
     * writeOptColumnMappings.</p>
     *
     * @param optColumnMappings a {@link java.util.List} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp the serialization provider.
     * @throws java.io.IOException if an operation on the JsonGenerator object
     * fails.
     */
    public static void writeOptColumnMappings(
            List<OptColumnMapping> optColumnMappings,
            JsonGenerator jg, SerializerProvider sp) throws IOException {
        for (OptColumnMapping ocm : Optional.ofNullable(
                optColumnMappings).
                orElse(Collections.emptyList())) {
            // write global or indexed element param objects
            if (ocm.getParam() != null) {
                writeObject(Serializers.printOptColumnMapping(ocm), jg, sp, ocm.
                        getValue() == null
                                ? (ocm.getParam().getValue() == null || ocm.getParam().getValue().isEmpty()
                                ? NULL : ocm.getParam().getValue()) : ocm.getValue());
            } else { // write global opt column objects with the value
                writeObject(Serializers.printOptColumnMapping(ocm), jg, sp, ocm.
                        getValue() == null ? NULL : ocm.getValue());
            }
        }
    }

    /**
     * <p>
     * writeIndexedValues.</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param values a {@link java.util.List} object.
     */
    public static void writeIndexedDoubles(String prefix,
            JsonGenerator jg, List<Double> values) {
        IntStream.range(0, values.
                size()).
                forEachOrdered(i
                        -> {
                    try {
                        Serializers.writeNumber(
                                prefix + "[" + (i + 1) + "]",
                                jg,
                                values.
                                        get(i));
                    } catch (IOException ex) {
                        log.error(
                                "Caught IO exception while trying to write indexed doubles:",
                                ex);
                    }
                });
    }

    public static void checkIndexedElement(Object element) {
        Integer id = IndexedElement.of(element).getId();
        if (id == null) {
            throw new ValidationException(
                    "'id' field of " + element.toString() + " must not be null!");
        }
        if (id < 1) {
            throw new ValidationException(
                    "'id' field of " + element.toString() + " must have a value greater to equal to 1!");
        }
    }

}
