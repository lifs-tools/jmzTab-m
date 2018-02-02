/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addIndexedLine;
import de.isas.mztab1_1.model.Parameter;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataElement;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class CustomSerializer extends StdSerializer<Parameter> {

    public CustomSerializer() {
        this(null);
    }

    public CustomSerializer(Class<Parameter> t) {
        super(t);
    }

    @Override
    public void serialize(Parameter customParameter, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (customParameter != null) {
            addIndexedLine(jg, Section.Metadata.getPrefix(),
                MetadataElement.CUSTOM, customParameter);

        } else {
            System.err.println("Contact is null!");
        }
    }
}
