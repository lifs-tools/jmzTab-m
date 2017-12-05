/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.Publication;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MetadataSerializer extends StdSerializer<Metadata> {

    public MetadataSerializer() {
        this(null);
    }

    public MetadataSerializer(Class<Metadata> t) {
        super(t);
    }

    @Override
    public void serialize(Metadata t, JsonGenerator jg, SerializerProvider sp) throws IOException {
        if (t != null) {
            String prefix = t.getPrefix().
                    name();
            addLine(jg, prefix, "mzTab-ID", t.getFileDescription().
                    getMzTabID());
            addLine(jg, prefix, "mzTab-version", t.getFileDescription().
                    getMzTabVersion());
            addLine(jg, prefix, "title", t.getFileDescription().
                    getTitle());
            addLine(jg, prefix, "description", t.getFileDescription().
                    getDescription());
            for (int i = 0; i < t.getContacts().
                    size(); i++) {
                Contact contact = t.getContacts().
                        get(i);
                addLine(jg, prefix, "contact[" + (i + 1) + "]-name", contact.
                        getName());
                addLine(jg, prefix, "contact[" + (i + 1) + "]-email", contact.
                        getEmail());
                addLine(jg, prefix, "contact[" + (i + 1) + "]-affiliation",
                        contact.getAffiliation());
            }
            for (int i = 0; i < t.getPublications().
                    size(); i++) {
                Publication p = t.getPublications().
                        get(i);
                addLine(jg, prefix, "publication[" + (i + 1)+"]", p.stream().
                        map(pitem ->
                                pitem.getType().
                                        name() + ":" + pitem.getAccession()).
                        collect(Collectors.joining(
                                "|", "", "")));
            }
        }
    }

    public void addLine(JsonGenerator jg, String prefix, String key,
            String value) throws IOException {
        jg.writeStartArray();
        //prefix
        jg.writeString(prefix);
        //key
        jg.writeString(key);
        //value
        jg.writeString(value);
        jg.writeEndArray();
    }

}
