/*
 * Copyright 2017 Leibniz Institut für Analytische Wissenschaften - ISAS e.V..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLine;
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
            if(instrument.getInstrumentAnalyzer()!=null) {
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
            }
            addLine(jg, Section.Metadata.getPrefix(),
                "instrument[" + instrument.getId() + "]-detector",
                ParameterSerializer.toString(instrument.
                    getInstrumentDetector()));

        } else {
            System.err.println("Instrument is null!");
        }
    }
}
