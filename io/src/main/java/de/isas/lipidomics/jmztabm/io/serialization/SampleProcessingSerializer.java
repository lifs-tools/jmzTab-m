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
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.isas.mztab1_1.model.SampleProcessing;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 * <p>SampleProcessingSerializer class.</p>
 *
 * @author nilshoffmann
 * 
 */
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

    /** {@inheritDoc} */
    @Override
    public void serialize(SampleProcessing sampleProcessing, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (sampleProcessing != null) {
            Serializers.addIndexedLine(jg, sp, Section.Metadata.getPrefix(),
                sampleProcessing,
                sampleProcessing.getSampleProcessing());
        } else {
            Logger.getLogger(SampleProcessingSerializer.class.getName()).
                log(Level.FINE, "SampleProcessing is null!");
        }
    }
}
