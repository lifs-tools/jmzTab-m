/*
 * Copyright 2026 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package org.lifstools.mztab2.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.lifstools.mztab2.io.serialization.Serializers;
import org.lifstools.mztab2.model.IndexedElement;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.ValidationLocation;
import org.lifstools.mztab2.model.ValidationLocation.SectionEnum;
import uk.ac.ebi.pride.jmztab2.model.MetadataElement;
import uk.ac.ebi.pride.jmztab2.model.MetadataProperty;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;

/**
 * Maps paths in the mzTab object model, as used by the CV mapping rules, to
 * locations in an mzTab file: metadata keys and groups, or table cells.
 *
 * A location field is only set when the mapping is certain; a path that cannot
 * be mapped keeps only what is known, e.g. the metadata group and the element
 * path.
 *
 * @author nilshoffmann
 */
public final class ModelLocations {

    private static final String METADATA = "metadata";
    /**
     * The group of metadata keys without a number, as in
     * {@link Locations#groupOf(String)}.
     */
    private static final String GENERAL_GROUP = "general";
    private static final Map<String, SectionEnum> TABLES = Map.of(
        "smallMoleculeSummary", SectionEnum.SML,
        "smallMoleculeFeature", SectionEnum.SMF,
        "smallMoleculeEvidence", SectionEnum.SME);
    private static final Map<SectionEnum, String> TABLE_NAMES = Map.of(
        SectionEnum.SML, "small molecule summary",
        SectionEnum.SMF, "small molecule feature",
        SectionEnum.SME, "small molecule evidence");
    /**
     * Parameter properties whose values are written under the element's own
     * key, e.g. software[1] for software.parameter.
     */
    private static final Set<String> SELF_NAMED = Set.of(
        "software/parameter", "database/param",
        "sampleProcessing/sampleProcessing");
    private static final Pattern SEGMENT = Pattern.compile(
        "@?([A-Za-z_][A-Za-z0-9_]*)(?:\\[(\\d+)\\])?");

    private ModelLocations() {
    }

    /**
     * The location of the exact element a pointer selects.
     *
     * @param pointer a pointer into an {@link org.lifstools.mztab2.model.MzTab}
     * object model
     * @return the location, with the pointer's path as element path
     */
    public static ValidationLocation ofPointer(Pointer pointer) {
        String path = pointer.asPath();
        List<Segment> segments = parse(path);
        ValidationLocation location = new ValidationLocation();
        if (segments.isEmpty()) {
            return location.elementPath(path);
        }
        Segment first = segments.get(0);
        SectionEnum table = TABLES.get(first.name);
        if (table != null) {
            location.section(table);
            if (first.index != null) {
                location.rowIndex(first.index - 1);
            }
            if (segments.size() == 2 && segments.get(1).index == null) {
                location.column(snakeCase(segments.get(1).name));
            }
        } else if (METADATA.equals(first.name) && segments.size() > 1) {
            location = ofMetadataPointer(pointer, segments);
        }
        return location.elementPath(path);
    }

    /**
     * The location closest to what a CV mapping rule's element path addresses:
     * the metadata group, or the table section and column (no row).
     *
     * No element is matched, so no element path is set.
     *
     * @param cvElementPath the rule's element path, e.g.
     * /metadata/sample/@species
     * @return the location
     */
    public static ValidationLocation ofRulePath(String cvElementPath) {
        List<Segment> segments = parse(cvElementPath);
        ValidationLocation location = new ValidationLocation();
        if (!segments.isEmpty()) {
            Segment first = segments.get(0);
            SectionEnum table = TABLES.get(first.name);
            if (table != null) {
                location.section(table);
                if (segments.size() == 2) {
                    location.column(snakeCase(segments.get(1).name));
                }
            } else if (METADATA.equals(first.name) && segments.size() > 1) {
                location.section(SectionEnum.MTD);
                String elementName = segments.get(1).name;
                if (isMetadataList(elementName)) {
                    metadataElement(elementName).ifPresent(
                        element -> location.metadataGroup(groupOf(element)));
                } else {
                    location.metadataGroup(GENERAL_GROUP);
                }
            }
        }
        return location;
    }

    /**
     * Describes what a CV mapping rule's element path addresses in words for a
     * suggested fix: the metadata key pattern (e.g. sample[n]-species or
     * quantification_method), else the group or table column.
     *
     * @param cvElementPath the rule's element path, e.g.
     * /metadata/sample/@species
     * @return the description, or null if nothing is known
     */
    public static String describeRulePath(String cvElementPath) {
        String pattern = keyPattern(parse(cvElementPath));
        return pattern != null ? pattern : describe(ofRulePath(cvElementPath));
    }

    /**
     * Describes a location in words for a suggested fix: the metadata key, the
     * table cell, or the key pattern of the element path (e.g.
     * sample[n]-custom when the element has no id).
     *
     * @param location the location, may be null
     * @return the description, or null if nothing is known
     */
    public static String describe(ValidationLocation location) {
        if (location == null) {
            return null;
        }
        if (location.getMetadataKey() != null) {
            return location.getMetadataKey();
        }
        String tableName = TABLE_NAMES.get(location.getSection());
        if (tableName != null) {
            StringBuilder sb = new StringBuilder();
            if (location.getColumn() != null) {
                sb.append(location.getColumn()).append(" in ");
            }
            sb.append(tableName);
            if (location.getRowIndex() != null) {
                sb.append(" row ").append(location.getRowIndex() + 1);
            }
            return sb.toString();
        }
        if (location.getElementPath() != null) {
            String pattern = keyPattern(parse(location.getElementPath()));
            if (pattern != null) {
                return pattern;
            }
        }
        return location.getMetadataGroup();
    }

    private static ValidationLocation ofMetadataPointer(Pointer pointer,
        List<Segment> segments) {
        Segment elementSegment = segments.get(1);
        Optional<MetadataElement> element = metadataElement(
            elementSegment.name);
        if (segments.size() == 2) {
            // a parameter directly on the metadata: a scalar or a parameter list
            if (elementSegment.index == null) {
                if (element.isEmpty() || isMetadataList(elementSegment.name)) {
                    return Locations.metadataGroup(GENERAL_GROUP);
                }
                return Locations.metadataKey(element.get().getName());
            }
            if (element.isEmpty()) {
                return Locations.section(SectionEnum.MTD);
            }
            // numbered as MetadataSerializer.serializeParameterListWithMetadataElement does
            Integer id = idOf(pointer.getValue());
            int n = id != null && id > 0 ? id : elementSegment.index;
            return Locations.metadataKey(element.get().getName() + "[" + n + "]");
        }
        if (element.isEmpty() || elementSegment.index == null) {
            return Locations.section(SectionEnum.MTD);
        }
        if (segments.size() != 3) {
            // deeper paths lead through references to other elements
            return Locations.section(SectionEnum.MTD);
        }
        ValidationLocation group = Locations.metadataGroup(groupOf(element.
            get()));
        if (element.get().getName().contains("-")) {
            return group;
        }
        Integer id = idOf(bean(pointer, "/" + METADATA + "/" + elementSegment.name
            + "[" + elementSegment.index + "]"));
        Segment propertySegment = segments.get(2);
        String suffix;
        if (SELF_NAMED.contains(elementSegment.name + "/" + propertySegment.name)) {
            suffix = "";
        } else {
            String property = snakeCase(propertySegment.name);
            if (MetadataProperty.findProperty(element.get(), property).isEmpty()) {
                return group;
            }
            suffix = "-" + property + (propertySegment.index == null ? ""
                : "[" + propertySegment.index + "]");
        }
        if (id == null) {
            return group;
        }
        return Locations.metadataKey(element.get().getName() + "[" + id + "]"
            + suffix);
    }

    /**
     * The key pattern of a metadata path, with [n] for every element number,
     * e.g. sample[n]-species; null if the path does not name a known key.
     */
    private static String keyPattern(List<Segment> segments) {
        if (segments.size() < 2 || segments.size() > 3 || !METADATA.equals(
            segments.get(0).name)) {
            return null;
        }
        String elementName = segments.get(1).name;
        Optional<MetadataElement> element = metadataElement(elementName);
        if (element.isEmpty()) {
            return null;
        }
        if (!isMetadataList(elementName)) {
            return segments.size() == 2 ? element.get().getName() : null;
        }
        String pattern = element.get().getName() + "[n]";
        if (segments.size() == 2) {
            return pattern;
        }
        String propertyName = segments.get(2).name;
        if (SELF_NAMED.contains(elementName + "/" + propertyName)) {
            return pattern;
        }
        String property = snakeCase(propertyName);
        return MetadataProperty.findProperty(element.get(), property).
            map(p -> pattern + "-" + property).
            orElse(null);
    }

    /**
     * The metadata element of a property of {@link Metadata}, matched by its
     * name in an mzTab file, e.g. msRun to ms_run or
     * smallMoleculeQuantificationUnit to small_molecule-quantification_unit.
     */
    private static Optional<MetadataElement> metadataElement(
        String propertyName) {
        String snake = snakeCase(propertyName);
        return Arrays.stream(MetadataElement.values()).
            filter(e -> e.getName().replace('-', '_').equals(snake)).
            findFirst();
    }

    /**
     * The group of a numbered metadata element; colunit-... elements are
     * written without a number and therefore belong to the general group.
     */
    private static String groupOf(MetadataElement element) {
        return element.getName().contains("-") ? GENERAL_GROUP : element.
            getName();
    }

    private static boolean isMetadataList(String propertyName) {
        String getter = "get" + Character.toUpperCase(propertyName.charAt(0))
            + propertyName.substring(1);
        try {
            return List.class.isAssignableFrom(Metadata.class.getMethod(getter).
                getReturnType());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static Object bean(Pointer pointer, String path) {
        if (!(pointer instanceof NodePointer)) {
            return null;
        }
        try {
            return JXPathContext.newContext(((NodePointer) pointer).getRootNode()).
                getValue(path);
        } catch (JXPathException e) {
            return null;
        }
    }

    private static Integer idOf(Object bean) {
        if (bean == null) {
            return null;
        }
        return IndexedElement.of(bean).map(IndexedElement::getId).orElse(null);
    }

    private static String snakeCase(String camelCase) {
        return Serializers.camelCaseToUnderscoreLowerCase(camelCase);
    }

    /**
     * Splits a path into its steps; '@' is dropped and predicates other than a
     * position are ignored.
     */
    private static List<Segment> parse(String path) {
        List<Segment> segments = new ArrayList<>();
        for (String step : path.split("/")) {
            if (step.isEmpty()) {
                continue;
            }
            Matcher m = SEGMENT.matcher(step);
            if (!m.lookingAt()) {
                return new ArrayList<>();
            }
            segments.add(new Segment(m.group(1), m.group(2) == null ? null
                : Integer.valueOf(m.group(2))));
        }
        return segments;
    }

    private record Segment(String name, Integer index) {

    }
}
