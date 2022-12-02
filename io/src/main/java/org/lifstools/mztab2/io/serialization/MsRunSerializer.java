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
package org.lifstools.mztab2.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static org.lifstools.mztab2.io.serialization.Serializers.addLineWithProperty;
import static org.lifstools.mztab2.io.serialization.Serializers.addSubElementParameter;
import static org.lifstools.mztab2.io.serialization.Serializers.addSubElementParameters;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MsRun;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>MsRunSerializer implementation for {@link org.lifstools.mztab2.model.MsRun}.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
public class MsRunSerializer extends StdSerializer<MsRun> {

    /**
     * <p>
     * Constructor for MsRunSerializer.</p>
     */
    public MsRunSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for MsRunSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public MsRunSerializer(Class<MsRun> t) {
        super(t);
    }

    @Override
    public void serializeWithType(MsRun value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(MsRun msRun, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (msRun != null) {
            Serializers.checkIndexedElement(msRun);
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                MsRun.Properties.name.getPropertyName(), msRun,
                msRun.getName());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                MsRun.Properties.location.getPropertyName(),
                msRun, msRun.getLocation());
            if (msRun.getInstrumentRef() != null) {
                addLineWithProperty(jg, Section.Metadata.getPrefix(),
                    MsRun.Properties.instrumentRef.getPropertyName(), msRun,
                    new StringBuilder().append(Metadata.Properties.instrument.
                        getPropertyName() + "[" + msRun.getInstrumentRef().
                            getId() + "]").
                        toString());
            }
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                MsRun.Properties.hash.getPropertyName(),
                msRun, msRun.getHash());
            addSubElementParameter(jg, Section.Metadata.getPrefix(), msRun,
                MsRun.Properties.hashMethod.getPropertyName(),
                msRun.getHashMethod());
            addSubElementParameter(jg, Section.Metadata.getPrefix(), msRun,
                MsRun.Properties.format.getPropertyName(), msRun.getFormat());
            addSubElementParameters(jg, Section.Metadata.getPrefix(), msRun,
                MsRun.Properties.fragmentationMethod.getPropertyName(),
                msRun.getFragmentationMethod(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), msRun,
                MsRun.Properties.scanPolarity.getPropertyName(),
                msRun.getScanPolarity(), false);
            addSubElementParameter(jg, Section.Metadata.getPrefix(), msRun,
                MsRun.Properties.idFormat.getPropertyName(),
                msRun.getIdFormat());
        } else {
            log.debug(MsRun.class.getSimpleName() + " is null!");
        }
    }
}
