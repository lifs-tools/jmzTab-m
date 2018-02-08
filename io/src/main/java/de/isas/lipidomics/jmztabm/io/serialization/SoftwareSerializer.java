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
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementStrings;
import de.isas.mztab1_1.model.Software;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class SoftwareSerializer extends StdSerializer<Software> {

    public SoftwareSerializer() {
        this(null);
    }

    public SoftwareSerializer(Class<Software> t) {
        super(t);
    }

    @Override
    public void serialize(Software software, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (software != null) {
            Serializers.addIndexedLine(jg, Section.Metadata.getPrefix(),
                software,
                software.getParameter());
            addSubElementStrings(jg, Section.Metadata.getPrefix(), software,
                "setting",
                software.getSetting(), false);
        } else {
            System.err.println("Software is null!");
        }
    }
}
