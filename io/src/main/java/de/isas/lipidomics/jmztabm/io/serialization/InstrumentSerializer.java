/* 
 * Copyright 2018 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithPropertyParameters;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementStrings;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.Instrument;
import static de.isas.mztab1_1.model.Metadata.Properties.assay;
import de.isas.mztab1_1.model.MsRun;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 * <p>
 * InstrumentSerializer class.</p>
 *
 * @author nilshoffmann
 *
 */
public class InstrumentSerializer extends StdSerializer<Instrument> {

    /**
     * <p>
     * Constructor for InstrumentSerializer.</p>
     */
    public InstrumentSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for InstrumentSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public InstrumentSerializer(Class<Instrument> t) {
        super(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Instrument instrument, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (instrument != null) {

            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                Instrument.Properties.name.getPropertyName(),
                instrument,
                instrument.getName());
//            addLine(jg, Section.Metadata.getPrefix(),
//                "instrument[" + instrument.getId() + "]-name",
//                instrument.
//                    getInstrumentName());

//            addLineWithProperty(jg, Section.Metadata.getPrefix(),
//                Assay.Properties.externalUri.getPropertyName(),
//                assay, assay.getExternalUri());

            addLineWithPropertyParameters(jg, Section.Metadata.getPrefix(),
                Instrument.Properties.source.getPropertyName(),
                instrument, Arrays.asList(instrument.getSource()));

//            addLine(jg, Section.Metadata.getPrefix(),
//                "instrument[" + instrument.getId() + "]-source",
//                instrument.
//                    getInstrumentSource());
            if (instrument.getAnalyzer() != null) {

//                addLineWithPropertyParameters(jg, Section.Metadata.getPrefix(),
//                    Instrument.Properties.instrumentAnalyzer.getPropertyName(),
//                    instrument, instrument.getInstrumentAnalyzer());
                addSubElementStrings(jg, Section.Metadata.getPrefix(), instrument,
                    Instrument.Properties.analyzer.getPropertyName(), instrument.getAnalyzer(), false);
//                IntStream.range(0, instrument.getInstrumentAnalyzer().
//                    size()).
//                    forEachOrdered(i ->
//                    {
//                        addLine(jg, Section.Metadata.getPrefix(),
//                            "instrument[" + instrument.getId() + "]-analyzer[" + (i + 1) + "]",
//                            instrument.
//                                getInstrumentAnalyzer().
//                                get(i));
//                    });
            }
            if (instrument.getDetector() != null) {
                addLineWithProperty(jg, Section.Metadata.getPrefix(),
                    Instrument.Properties.detector.getPropertyName(),
                    instrument,
                    instrument.
                        getDetector());
            }
        } else {
            Logger.getLogger(InstrumentSerializer.class.getName()).
                log(Level.FINE, Instrument.class.getSimpleName() + " is null!");
        }
    }
}
