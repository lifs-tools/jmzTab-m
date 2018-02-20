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
import de.isas.lipidomics.jmztabm.io.MzTabWriter;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addIndexedLine;
import de.isas.mztab1_1.model.Parameter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataElement;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 * <p>CustomSerializer class.</p>
 *
 * @author nilshoffmann
 * 
 */
public class CustomSerializer extends StdSerializer<Parameter> {

    /**
     * <p>Constructor for CustomSerializer.</p>
     */
    public CustomSerializer() {
        this(null);
    }

    /**
     * <p>Constructor for CustomSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public CustomSerializer(Class<Parameter> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(Parameter customParameter, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (customParameter != null) {
            addIndexedLine(jg, Section.Metadata.getPrefix(),
                MetadataElement.CUSTOM, customParameter);

        } else {
            Logger.getLogger(CustomSerializer.class.getName()).
                log(Level.FINE, "Custom is null!");
        }
    }
}
