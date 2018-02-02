/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementStrings;
import de.isas.mztab1_1.model.Software;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class SoftwareSerializer extends StdSerializer<Software> {

    public SoftwareSerializer() {
        this(null);
    }

    public SoftwareSerializer(Class<Software> t) {
        super(t);
    }

    @Override
    public void serialize(Software software, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (software != null) {
            Serializers.addIndexedLine(jg, Section.Metadata.getPrefix(),
                software,
                software.getParameter());
            addSubElementStrings(jg, Section.Metadata.getPrefix(), software,
                "setting",
                software.getSetting(), false);
        } else {
            System.err.println("Software is null!");
        }
    }
}
