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
import static de.isas.mztab2.io.serialization.Serializers.addLine;
import de.isas.mztab2.model.Publication;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>
 * PublicationSerializer class.</p>
 *
 * @author nilshoffmann
 *
 */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Publication publication, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (publication != null) {
            addLine(jg, Section.Metadata.getPrefix(),
                "publication[" + publication.getId() + "]", Optional.ofNullable(
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
            Logger.getLogger(PublicationSerializer.class.getName()).
                log(Level.FINE, "Publication is null!");
        }
    }
}
