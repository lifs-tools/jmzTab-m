/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementParameter;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementParameters;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementStrings;
import de.isas.mztab1_1.model.MsRun;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MsRunSerializer extends StdSerializer<MsRun> {

    public MsRunSerializer() {
        this(null);
    }

    public MsRunSerializer(Class<MsRun> t) {
        super(t);
    }

    @Override
    public void serialize(MsRun msRun, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        
        if (msRun != null) {
            addLineWithProperty(jg, Section.Metadata.getPrefix(), "name", msRun,
                msRun.getName());
            addLineWithProperty(jg, Section.Metadata.getPrefix(), "location",
                msRun, msRun.getLocation());
            addLineWithProperty(jg, Section.Metadata.getPrefix(), "hash",
                msRun, msRun.getHash());
            addSubElementParameter(jg, Section.Metadata.getPrefix(), msRun, "hash_method",
                msRun.getHashMethod());
            addSubElementParameter(jg, Section.Metadata.getPrefix(), msRun,
                "format", msRun.getFormat());
            addSubElementParameters(jg, Section.Metadata.getPrefix(), msRun,
                "fragmentation_method",
                msRun.getFragmentationMethod(), true);
            addSubElementParameter(jg, Section.Metadata.getPrefix(), msRun,
                "id_format",
                msRun.getIdFormat());
        } else {
            System.err.println("MsRun is null!");
        }
    }
}
