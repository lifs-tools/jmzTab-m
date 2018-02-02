/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import de.isas.mztab1_1.model.CV;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class CvSerializer extends StdSerializer<CV> {

    public CvSerializer() {
        this(null);
    }

    public CvSerializer(Class<CV> t) {
        super(t);
    }

    @Override
    public void serialize(CV cv, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (cv != null) {
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "label", cv,
                cv.
                    getLabel());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "url", cv,
                cv.
                    getUrl());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "version", cv,
                cv.getVersion());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "full_name", cv,
                cv.getFullName());

        } else {
            System.err.println("CV is null!");
        }
    }
}
