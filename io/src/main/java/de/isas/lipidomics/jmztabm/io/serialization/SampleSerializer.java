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
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementParameters;
import de.isas.mztab1_1.model.Sample;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann &lt;nils.hoffmann@isas.de&gt;
 */
public class SampleSerializer extends StdSerializer<Sample> {

    public SampleSerializer() {
        this(null);
    }

    public SampleSerializer(Class<Sample> t) {
        super(t);
    }

    @Override
    public void serialize(Sample sample, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (sample != null) {
            addLineWithProperty(jg, Section.Metadata.getPrefix(), null, sample,
                sample.
                    getName());
            addLineWithProperty(jg, Section.Metadata.getPrefix(), "description",
                sample,
                sample.getDescription());
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "species",
                sample.getSpecies(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "cell_type",
                sample.getCellType(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "disease",
                sample.getDisease(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "tissue",
                sample.getTissue(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "id_confidence_measure", sample.
                    getIdConfidenceMeasure(), false);
            addSubElementParameters(jg, Section.Metadata.getPrefix(), sample,
                "custom",
                sample.getCustom(), false);
        } else {
            Logger.getLogger(SampleSerializer.class.getName()).
                log(Level.FINE, "SampleSerializer is null!");
        }
    }
}
