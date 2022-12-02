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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.lifstools.mztab2.model.SampleProcessing;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>SampleProcessingSerializer implementation for {@link org.lifstools.mztab2.model.SampleProcessing}.</p>
 *
 * @author nilshoffmann
 * 
 */
@Slf4j
public class SampleProcessingSerializer extends StdSerializer<SampleProcessing> {

    /**
     * <p>Constructor for SampleProcessingSerializer.</p>
     */
    public SampleProcessingSerializer() {
        this(null);
    }

    /**
     * <p>Constructor for SampleProcessingSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public SampleProcessingSerializer(Class<SampleProcessing> t) {
        super(t);
    }
    
    @Override
    public void serializeWithType(SampleProcessing value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(SampleProcessing sampleProcessing, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (sampleProcessing != null) {
            Serializers.checkIndexedElement(sampleProcessing);
            Serializers.addIndexedLine(jg, sp, Section.Metadata.getPrefix(),
                sampleProcessing,
                sampleProcessing.getSampleProcessing());
        } else {
            log.debug(SampleProcessing.class.getSimpleName()+" is null!");
        }
    }
}
