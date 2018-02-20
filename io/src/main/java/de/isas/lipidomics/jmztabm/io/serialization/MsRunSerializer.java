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
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementParameter;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementParameters;
import de.isas.mztab1_1.model.MsRun;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 * <p>MsRunSerializer class.</p>
 *
 * @author nilshoffmann
 * 
 */
public class MsRunSerializer extends StdSerializer<MsRun> {

    /**
     * <p>Constructor for MsRunSerializer.</p>
     */
    public MsRunSerializer() {
        this(null);
    }

    /**
     * <p>Constructor for MsRunSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public MsRunSerializer(Class<MsRun> t) {
        super(t);
    }

    /** {@inheritDoc} */
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
            Logger.getLogger(MsRunSerializer.class.getName()).
                    log(Level.FINE, "MsRun is null!");
        }
    }
}
