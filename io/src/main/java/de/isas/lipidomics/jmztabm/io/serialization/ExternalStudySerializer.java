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
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLine;
import de.isas.mztab1_1.model.ExternalStudy;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class ExternalStudySerializer extends StdSerializer<ExternalStudy> {

    public ExternalStudySerializer() {
        this(null);
    }

    public ExternalStudySerializer(Class<ExternalStudy> t) {
        super(t);
    }

    @Override
    public void serialize(ExternalStudy study, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (study != null) {
            addLine(jg, Section.Metadata.getPrefix(),
                "study-url", study.getUrl());
            addLine(jg, Section.Metadata.getPrefix(),
                "study-id", study.getId());
            addLine(jg, Section.Metadata.getPrefix(),
                "study-id-format", ParameterSerializer.toString(study.
                    getIdFormat()));
            addLine(jg, Section.Metadata.getPrefix(),
                "study-title", study.getTitle());
            addLine(jg, Section.Metadata.getPrefix(),
                "study-version", study.getVersion());
            addLine(jg, Section.Metadata.getPrefix(),
                "study-format", ParameterSerializer.toString(study.
                    getFormat()));

        } else {
            Logger.getLogger(ExternalStudySerializer.class.getName()).
                log(Level.FINE, "ExternalStudy is null!");
        }
    }
}
