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
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import de.isas.mztab1_1.model.CV;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 * <p>CvSerializer class.</p>
 *
 * @author nilshoffmann
 * 
 */
public class CvSerializer extends StdSerializer<CV> {

    /**
     * <p>Constructor for CvSerializer.</p>
     */
    public CvSerializer() {
        this(null);
    }

    /**
     * <p>Constructor for CvSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public CvSerializer(Class<CV> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(CV cv, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (cv != null) {
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "label", cv,
                cv.
                    getLabel());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "url", cv,
                cv.
                    getUrl());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "version", cv,
                cv.getVersion());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "full_name", cv,
                cv.getFullName());

        } else {
            Logger.getLogger(CvSerializer.class.getName()).
                log(Level.FINE, "CV is null!");
        }
    }
}
