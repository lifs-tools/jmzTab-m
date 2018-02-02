/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.isas.lipidomics.jmztabm.io.MzTabWriter;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLine;
import de.isas.mztab1_1.model.ExternalStudy;
import de.isas.mztab1_1.model.Instrument;
import java.io.IOException;
import java.util.stream.IntStream;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class InstrumentSerializer extends StdSerializer<Instrument> {

    public InstrumentSerializer() {
        this(null);
    }

    public InstrumentSerializer(Class<Instrument> t) {
        super(t);
    }

    @Override
    public void serialize(Instrument instrument, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (instrument != null) {
            addLine(jg, Section.Metadata.getPrefix(),
                "instrument[" + instrument.getId() + "]-name",
                ParameterSerializer.toString(instrument.
                    getInstrumentName()));
            addLine(jg, Section.Metadata.getPrefix(),
                "instrument[" + instrument.getId() + "]-source",
                ParameterSerializer.toString(instrument.
                    getInstrumentSource()));
            IntStream.range(0, instrument.getInstrumentAnalyzer().
                size()).
                forEachOrdered(i ->
                {
                    addLine(jg, Section.Metadata.getPrefix(),
                        "instrument[" + instrument.getId() + "]-analyzer[" + (i + 1) + "]",
                        ParameterSerializer.toString(instrument.
                            getInstrumentAnalyzer().
                            get(i)));
                });
            addLine(jg, Section.Metadata.getPrefix(),
                "instrument[" + instrument.getId() + "]-detector",
                ParameterSerializer.toString(instrument.
                    getInstrumentDetector()));

        } else {
            System.err.println("Instrument is null!");
        }
    }
}
