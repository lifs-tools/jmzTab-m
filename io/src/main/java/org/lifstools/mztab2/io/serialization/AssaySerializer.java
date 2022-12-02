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
import static org.lifstools.mztab2.io.serialization.Serializers.addSubElementStrings;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.Sample;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>AssaySerializer implementation for {@link org.lifstools.mztab2.model.Assay}.</p>
 * 
 * @author nilshoffmann
 * 
 */
@Slf4j
public class AssaySerializer extends StdSerializer<Assay> {

    /**
     * <p>Constructor for AssaySerializer.</p>
     */
    public AssaySerializer() {
        this(null);
    }

    /**
     * <p>Constructor for AssaySerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public AssaySerializer(Class<Assay> t) {
        super(t);
    }
    
    @Override
    public void serializeWithType(Assay value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(Assay assay, JsonGenerator jg, SerializerProvider sp) throws IOException {
        if (assay != null) {
            Serializers.checkIndexedElement(assay);
            addLineWithProperty(jg, Section.Metadata.getPrefix(), null, assay,
                assay.
                    getName());
            
            addLineWithProperty(jg, Section.Metadata.getPrefix(), Assay.Properties.externalUri.getPropertyName(),
                assay, assay.getExternalUri());

            addSubElementStrings(jg, Section.Metadata.getPrefix(), assay, Assay.Properties.custom.getPropertyName(), assay.getCustom(), false);

            List<MsRun> msRunRef = assay.getMsRunRef();
            if (msRunRef != null) {
                addSubElementStrings(jg, Section.Metadata.getPrefix(), assay,
                    Assay.Properties.msRunRef.getPropertyName(), msRunRef.
                        stream().
                        sorted(Comparator.comparing(MsRun::getId,
                            Comparator.nullsFirst(Comparator.
                                naturalOrder())
                        )).
                        map((mref) ->
                        {
                            return new StringBuilder().append(org.lifstools.mztab2.model.Metadata.Properties.msRun.getPropertyName()).
                                append("[").
                                append(mref.getId()).
                                append("]").
                                toString();
                        }).
                        collect(Collectors.toList()), true);
            }
            Sample sampleRef = assay.getSampleRef();
            if (sampleRef != null) {
                addSubElementStrings(jg, Section.Metadata.getPrefix(), assay,
                    Assay.Properties.sampleRef.getPropertyName(), Arrays.asList(sampleRef).
                        stream().
                        sorted(Comparator.comparing(Sample::getId,
                            Comparator.nullsFirst(Comparator.
                                naturalOrder())
                        )).
                        map((sref) ->
                        {
                            return new StringBuilder().append(org.lifstools.mztab2.model.Metadata.Properties.sample.getPropertyName()).
                                append("[").
                                append(sref.getId()).
                                append("]").
                                toString();
                        }).
                        collect(Collectors.toList()), true);
            }

        } else {
            log.debug(Assay.class.getSimpleName()+" is null!");
        }
    }
}
