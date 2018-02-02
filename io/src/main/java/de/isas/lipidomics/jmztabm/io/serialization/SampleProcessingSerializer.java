/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.isas.mztab1_1.model.SampleProcessing;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class SampleProcessingSerializer extends StdSerializer<SampleProcessing> {

    public SampleProcessingSerializer() {
        this(null);
    }

    public SampleProcessingSerializer(Class<SampleProcessing> t) {
        super(t);
    }

    @Override
    public void serialize(SampleProcessing sampleProcessing, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (sampleProcessing != null) {
             Serializers.addIndexedLine(jg, Section.Metadata.getPrefix(), sampleProcessing,
                            sampleProcessing.getSampleProcessing());
        } else {
            System.err.println("SampleProcessing is null!");
        }
    }
}
