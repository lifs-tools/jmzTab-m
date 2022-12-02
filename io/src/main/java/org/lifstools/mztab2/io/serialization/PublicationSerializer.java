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
import static org.lifstools.mztab2.io.serialization.Serializers.addLine;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.Publication;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>PublicationSerializer implementation for {@link org.lifstools.mztab2.model.Publication}.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
public class PublicationSerializer extends StdSerializer<Publication> {

    /**
     * <p>
     * Constructor for PublicationSerializer.</p>
     */
    public PublicationSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for PublicationSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public PublicationSerializer(Class<Publication> t) {
        super(t);
    }
    
    @Override
    public void serializeWithType(Publication value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Publication publication, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (publication != null) {
            Serializers.checkIndexedElement(publication);
            addLine(jg, Section.Metadata.getPrefix(),
                Metadata.Properties.publication+"[" + publication.getId() + "]", Optional.ofNullable(
                publication.
                    getPublicationItems()).
                orElse(Collections.emptyList()).
                stream().
                map(pitem ->
                    pitem.getType().
                        getValue() + ":" + pitem.getAccession()).
                collect(Collectors.joining(
                    "|", "", "")));
        } else {
            log.debug(Publication.class.getSimpleName()+" is null!");
        }
    }
}
