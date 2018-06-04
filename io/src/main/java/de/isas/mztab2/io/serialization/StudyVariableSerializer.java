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
package de.isas.mztab2.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.mztab2.io.serialization.Serializers.addLineWithProperty;
import static de.isas.mztab2.io.serialization.Serializers.addSubElementParameters;
import static de.isas.mztab2.io.serialization.Serializers.addSubElementStrings;
import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.Sample;
import de.isas.mztab2.model.StudyVariable;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>
 * StudyVariableSerializer class.</p>
 *
 * @author nilshoffmann
 *
 */
public class StudyVariableSerializer extends StdSerializer<StudyVariable> {

    /**
     * <p>
     * Constructor for StudyVariableSerializer.</p>
     */
    public StudyVariableSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for StudyVariableSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public StudyVariableSerializer(Class<StudyVariable> t) {
        super(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(StudyVariable studyVariable, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (studyVariable != null) {
            addLineWithProperty(jg, Section.Metadata.getPrefix(), null,
                studyVariable,
                studyVariable.getName());
            addLineWithProperty(jg, Section.Metadata.getPrefix(), "description",
                studyVariable, studyVariable.getDescription());
            addSubElementParameters(jg, Section.Metadata.getPrefix(),
                studyVariable,
                "factors", studyVariable.getFactors(), true);
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "average_function", studyVariable, studyVariable.
                    getAverageFunction());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "variation_function", studyVariable, studyVariable.
                    getVariationFunction());
            addSubElementStrings(jg, Section.Metadata.getPrefix(), studyVariable,
                "assay_refs", Optional.ofNullable(studyVariable.getAssayRefs()).
                    orElse(Collections.emptyList()).
                    stream().
                    sorted(Comparator.comparing(Assay::getId,
                        Comparator.nullsFirst(Comparator.
                            naturalOrder())
                    )).
                    map((assayRef) ->
                    {
                        return new StringBuilder().append("assay").
                            append("[").
                            append(assayRef.getId()).
                            append("]").
                            toString();
                    }).
                    collect(Collectors.toList()), true);
        } else {
            Logger.getLogger(StudyVariableSerializer.class.getName()).
                log(Level.FINE, "StudyVariable is null!");
        }
    }
}
