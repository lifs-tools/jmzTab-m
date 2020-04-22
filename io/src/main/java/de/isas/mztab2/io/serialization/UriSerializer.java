/*
 * Copyright 2020 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
import de.isas.mztab2.model.CV;
import de.isas.mztab2.model.Uri;
import java.io.IOException;
import static java.lang.Math.log;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>
 * UriSerializer implementation for {@link de.isas.mztab2.model.Uri}.</p>
 *
 * @author nilshoffmann
 */
@Slf4j
public class UriSerializer extends StdSerializer<Uri> {

    /**
     * <p>
     * Constructor for UriSerializer.</p>
     */
    public UriSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for UriSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public UriSerializer(Class<Uri> t) {
        super(t);
    }

    @Override
    public void serializeWithType(Uri value, JsonGenerator gen,
            SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Uri uri, JsonGenerator jg,
            SerializerProvider sp) throws IOException {
        if (uri != null) {
            Serializers.checkIndexedElement(uri);
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                    Uri.Properties.value.getPropertyName(), uri,
                    uri.
                            getValue());

        } else {
            log.debug(Uri.class.getSimpleName() + " is null!");
        }
    }
}
