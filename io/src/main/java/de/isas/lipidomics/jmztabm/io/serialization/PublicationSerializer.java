/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLine;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import de.isas.mztab1_1.model.Publication;
import java.io.IOException;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class PublicationSerializer extends StdSerializer<Publication> {

    public PublicationSerializer() {
        this(null);
    }

    public PublicationSerializer(Class<Publication> t) {
        super(t);
    }

    @Override
    public void serialize(Publication publication, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (publication != null) {
            addLine(jg, Section.Metadata.getPrefix(),
                "publication[" + publication.getId() + "]", publication.
                getPublicationItems().
                stream().
                map(pitem ->
                    pitem.getType().
                        name() + ":" + pitem.getAccession()).
                collect(Collectors.joining(
                    "|", "", "")));
        } else {
            System.err.println("Publication is null!");
        }
    }
}
