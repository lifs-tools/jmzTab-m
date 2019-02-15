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
import static de.isas.mztab2.io.serialization.Serializers.addSubElementStrings;
import de.isas.mztab2.model.Software;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
* <p>SoftwareSerializer implementation for {@link de.isas.mztab2.model.Software}.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
public class SoftwareSerializer extends StdSerializer<Software> {

    /**
     * <p>
     * Constructor for SoftwareSerializer.</p>
     */
    public SoftwareSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for SoftwareSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public SoftwareSerializer(Class<Software> t) {
        super(t);
    }

    @Override
    public void serializeWithType(Software value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Software software, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (software != null) {
            Serializers.checkIndexedElement(software);
            Serializers.addIndexedLine(jg, sp, Section.Metadata.getPrefix(),
                software,
                software.getParameter());
            addSubElementStrings(jg, Section.Metadata.getPrefix(), software,
                Software.Properties.setting.getPropertyName(),
                software.getSetting(), false);
        } else {
            log.debug(Software.class.getSimpleName() + " is null!");
        }
    }
}
