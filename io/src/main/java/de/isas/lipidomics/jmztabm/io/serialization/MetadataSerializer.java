/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLine;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.Publication;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabUtils;

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
            addLine(jg, prefix, "mzTab-version", t.
                getMzTabVersion());
            addLine(jg, prefix, "mzTab-ID", t.
                getMzTabID());
            addLine(jg, prefix, "title", t.
                getTitle());
            addLine(jg, prefix, "description", t.
                getDescription());
            
            //sample processing
            final AtomicInteger cnt = new AtomicInteger(1);
            t.getSampleProcessing().stream().forEach((sampleProcessing) ->
                {
                    Serializers.addIndexedLine(jg, prefix, sampleProcessing, cnt.get(), sampleProcessing.getSampleProcessing());
                    cnt.incrementAndGet();
                });
            
            //contacts
            t.getContacts().
                stream().
                sorted((contact1,
                    contact2) ->
                {
                    return Integer.compare(contact1.getId(), contact2.getId());
                }).
                forEach((contact) ->
                {
                    addLine(jg, prefix, "contact[" + contact.getId() + "]-name",
                        contact.
                            getName());
                    addLine(jg, prefix, "contact[" + contact.getId() + "]-email",
                        contact.
                            getEmail());
                    addLine(jg, prefix,
                        "contact[" + contact.getId() + "]-affiliation",
                        contact.getAffiliation());
                });
            //publications
            t.getPublications().
                stream().
                sorted((publication1,
                    publication2) ->
                {
                    return Integer.compare(publication1.getId(), publication2.
                        getId());
                }).
                forEach((p) ->
                {
                    addLine(jg, prefix, "publication[" + p.getId() + "]", p.
                        getPublicationItems().
                        stream().
                        map(pitem ->
                            pitem.getType().
                                name() + ":" + pitem.getAccession()).
                        collect(Collectors.joining(
                            "|", "", "")));
                });
        }
    }

}
