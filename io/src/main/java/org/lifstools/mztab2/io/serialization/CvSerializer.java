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
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static org.lifstools.mztab2.io.serialization.Serializers.addLineWithProperty;
import org.lifstools.mztab2.model.CV;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>CvSerializer implementation for {@link org.lifstools.mztab2.model.CV}.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
public class CvSerializer extends StdSerializer<CV> {

    /**
     * <p>
     * Constructor for CvSerializer.</p>
     */
    public CvSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for CvSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public CvSerializer(Class<CV> t) {
        super(t);
    }

    @Override
    public void serializeWithType(CV value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen,
        typeSer.typeId(value, JsonToken.START_OBJECT));
        typeSer.writeTypePrefix(gen, typeIdDef);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(CV cv, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (cv != null) {
            Serializers.checkIndexedElement(cv);
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                CV.Properties.label.getPropertyName(), cv,
                cv.
                    getLabel());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                CV.Properties.uri.getPropertyName(), cv,
                cv.
                    getUri());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                CV.Properties.version.getPropertyName(), cv,
                cv.getVersion());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                CV.Properties.fullName.getPropertyName(), cv,
                cv.getFullName());

        } else {
           log.debug(CV.class.getSimpleName() + " is null!");
        }
    }
}
