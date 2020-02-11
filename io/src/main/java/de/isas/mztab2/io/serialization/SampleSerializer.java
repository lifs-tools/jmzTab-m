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
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.mztab2.io.serialization.Serializers.addLineWithProperty;
import static de.isas.mztab2.io.serialization.Serializers.addSubElementParameters;
import de.isas.mztab2.model.IndexedElementAdapter;
import de.isas.mztab2.model.Sample;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>SampleSerializer implementation for {@link de.isas.mztab2.model.Sample}.</p>
 *
 * @author nilshoffmann
 * 
 */
@Slf4j
public class SampleSerializer extends StdSerializer<Sample> {

    /**
     * <p>Constructor for SampleSerializer.</p>
     */
    public SampleSerializer() {
        this(null);
    }

    /**
     * <p>Constructor for SampleSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public SampleSerializer(Class<Sample> t) {
        super(t);
    }
    
    @Override
    public void serializeWithType(Sample value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(Sample sample, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (sample != null) {
            Serializers.checkIndexedElement(new IndexedElementAdapter<Sample>(sample));
            addLineWithProperty(jg, Section.Metadata.getPrefix(), null, sample,
                sample.
                    getName());
            addLineWithProperty(jg, Section.Metadata.getPrefix(), Sample.Properties.description.getPropertyName(),
                sample,
                sample.getDescription());
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                Sample.Properties.species.getPropertyName(),
                sample.getSpecies(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                Sample.Properties.cellType.getPropertyName(),
                sample.getCellType(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                Sample.Properties.disease.getPropertyName(),
                sample.getDisease(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                Sample.Properties.tissue.getPropertyName(),
                sample.getTissue(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                Sample.Properties.custom.getPropertyName(),
                sample.getCustom(), false);
        } else {
            log.debug(Sample.class.getSimpleName()+" is null!");
        }
    }
}
