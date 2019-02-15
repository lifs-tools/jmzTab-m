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
import de.isas.mztab2.model.Contact;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>ContactSerializer implementation for {@link de.isas.mztab2.model.Contact}.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
public class ContactSerializer extends StdSerializer<Contact> {

    /**
     * <p>
     * Constructor for ContactSerializer.</p>
     */
    public ContactSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for ContactSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public ContactSerializer(Class<Contact> t) {
        super(t);
    }

    @Override
    public void serializeWithType(Contact value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Contact contact, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (contact != null) {
            Serializers.checkIndexedElement(contact);
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                Contact.Properties.name.getPropertyName(), contact,
                contact.
                    getName());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                Contact.Properties.email.getPropertyName(), contact,
                contact.
                    getEmail());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                Contact.Properties.affiliation.getPropertyName(), contact,
                contact.getAffiliation());

        } else {
            log.debug(Contact.class.getSimpleName() + " is null!");
        }
    }
}
