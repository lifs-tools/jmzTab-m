/*
 * Copyright 2019 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static org.lifstools.mztab2.io.serialization.Serializers.addLineWithProperty;
import static org.lifstools.mztab2.io.serialization.Serializers.addSubElementStrings;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.StudyVariableGroup;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>StudyVariableGroupSerializer implementation for
 * {@link org.lifstools.mztab2.model.StudyVariableGroup}.</p>
 *
 * @author nilshoffmann
 */
@Slf4j
public class StudyVariableGroupSerializer extends StdSerializer<StudyVariableGroup> {

    /**
     * <p>Constructor for StudyVariableGroupSerializer.</p>
     */
    public StudyVariableGroupSerializer() {
        this(null);
    }

    /**
     * <p>Constructor for StudyVariableGroupSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public StudyVariableGroupSerializer(Class<StudyVariableGroup> t) {
        super(t);
    }

    @Override
    public void serializeWithType(StudyVariableGroup value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen,
            typeSer.typeId(value, JsonToken.START_OBJECT));
        typeSer.writeTypePrefix(gen, typeIdDef);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

    /**
     * {@inheritDoc}
     *
     * Serializes a StudyVariableGroup to mzTab metadata lines, e.g.:
     * <pre>
     * MTD study_variable_group[1]  [PATO, PATO:0000383, sex, ]
     * MTD study_variable_group[1]-description  Sex of the individual
     * MTD study_variable_group[1]-type  [STATO, STATO:0000252, categorical variable, ]
     * MTD study_variable_group[1]-datatype  xsd:string
     * </pre>
     */
    @Override
    public void serialize(StudyVariableGroup studyVariableGroup, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (studyVariableGroup != null) {
            Serializers.checkIndexedElement(studyVariableGroup);
            // Main line: MTD study_variable_group[id]  [cv, accession, name, value]
            addLineWithProperty(jg, Section.Metadata.getPrefix(), null,
                studyVariableGroup, studyVariableGroup.getParameter());
            // Mandatory description
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                StudyVariableGroup.JSON_PROPERTY_DESCRIPTION,
                studyVariableGroup, studyVariableGroup.getDescription());
            // Optional STATO type parameter
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                StudyVariableGroup.JSON_PROPERTY_TYPE,
                studyVariableGroup, studyVariableGroup.getType());
            // Optional xsd datatype string
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                StudyVariableGroup.JSON_PROPERTY_DATATYPE,
                studyVariableGroup, studyVariableGroup.getDatatype());
            // Optional unit parameter
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                StudyVariableGroup.JSON_PROPERTY_UNIT,
                studyVariableGroup, studyVariableGroup.getUnit());
            // study_variable_refs as a bar-separated list of study_variable[id]
            addSubElementStrings(jg, Section.Metadata.getPrefix(),
                studyVariableGroup,
                StudyVariableGroup.JSON_PROPERTY_STUDY_VARIABLE_REFS,
                Optional.ofNullable(studyVariableGroup.getStudyVariableRefs()).
                    orElse(Collections.emptyList()).
                    stream().
                    sorted(Comparator.comparing(StudyVariable::getId,
                        Comparator.nullsFirst(Comparator.naturalOrder()))).
                    map((svRef) ->
                    {
                        return new StringBuilder().
                            append(Metadata.JSON_PROPERTY_STUDY_VARIABLE).
                            append("[").
                            append(svRef.getId()).
                            append("]").
                            toString();
                    }).
                    collect(Collectors.toList()), true);
        } else {
            log.debug(StudyVariableGroup.class.getSimpleName() + " is null!");
        }
    }
}
