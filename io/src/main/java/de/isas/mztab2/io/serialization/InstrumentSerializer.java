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
package de.isas.mztab2.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.mztab2.io.serialization.Serializers.addLineWithProperty;
import static de.isas.mztab2.io.serialization.Serializers.addLineWithPropertyParameters;
import static de.isas.mztab2.io.serialization.Serializers.addSubElementStrings;
import de.isas.mztab2.model.Instrument;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab2.model.Section;

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

    @Override
    public void serializeWithType(Instrument value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
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

            addLineWithPropertyParameters(jg, Section.Metadata.getPrefix(),
                Instrument.Properties.source.getPropertyName(),
                instrument, Arrays.asList(instrument.getSource()));

            if (instrument.getAnalyzer() != null) {
                addSubElementStrings(jg, Section.Metadata.getPrefix(),
                    instrument,
                    Instrument.Properties.analyzer.getPropertyName(),
                    instrument.getAnalyzer(), false);
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
