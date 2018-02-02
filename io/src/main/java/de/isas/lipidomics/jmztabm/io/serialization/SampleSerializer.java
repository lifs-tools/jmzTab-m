/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementParameters;
import de.isas.mztab1_1.model.Sample;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class SampleSerializer extends StdSerializer<Sample> {

    public SampleSerializer() {
        this(null);
    }

    public SampleSerializer(Class<Sample> t) {
        super(t);
    }

    @Override
    public void serialize(Sample sample, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (sample != null) {
            addLineWithProperty(jg, Section.Metadata.getPrefix(), "name", sample,
                sample.
                    getName());
            addLineWithProperty(jg, Section.Metadata.getPrefix(), "description",
                sample,
                sample.getDescription());
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "species",
                sample.getSpecies(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "cell_type",
                sample.getCellType(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "disease",
                sample.getDisease(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "tissue",
                sample.getTissue(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "id_confidence_measure", sample.
                    getIdConfidenceMeasure(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "custom",
                sample.getCustom(), false);
        } else {
            System.err.println("Sample is null!");
        }
    }
}
