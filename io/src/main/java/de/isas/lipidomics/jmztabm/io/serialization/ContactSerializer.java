/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import de.isas.mztab1_1.model.Contact;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class ContactSerializer extends StdSerializer<Contact> {

    public ContactSerializer() {
        this(null);
    }

    public ContactSerializer(Class<Contact> t) {
        super(t);
    }

    @Override
    public void serialize(Contact contact, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (contact != null) {
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "name", contact,
                contact.
                    getName());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "email", contact,
                contact.
                    getEmail());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "affiliation", contact,
                contact.getAffiliation());

        } else {
            System.err.println("Contact is null!");
        }
    }
}
