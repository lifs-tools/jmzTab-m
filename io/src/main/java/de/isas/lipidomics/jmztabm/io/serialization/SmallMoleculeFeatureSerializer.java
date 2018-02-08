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
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.SmallMoleculeFeature;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class SmallMoleculeFeatureSerializer extends StdSerializer<SmallMoleculeFeature> {

    public SmallMoleculeFeatureSerializer() {
        this(null);
    }

    public SmallMoleculeFeatureSerializer(Class<SmallMoleculeFeature> t) {
        super(t);
    }

    @Override
    public void serialize(SmallMoleculeFeature smallMoleculeFeature,
        JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (smallMoleculeFeature != null) {
            jg.writeString("SMF");
            jg.writeNumber(smallMoleculeFeature.getSmfId());
            Serializers.writeAsStringArray(jg, smallMoleculeFeature.
                getSmeIdRefs());
            jg.writeNumber(smallMoleculeFeature.getSmeIdRefAmbiguityCode());
            jg.writeString(smallMoleculeFeature.getAdductIon());
            jg.writeString(ParameterSerializer.toString(smallMoleculeFeature.
                getIsotopomer()));
            jg.writeNumber(smallMoleculeFeature.getExpMassToCharge());
            jg.writeNumber(smallMoleculeFeature.getCharge());
            jg.writeNumber(smallMoleculeFeature.getRetentionTime());
            jg.writeNumber(smallMoleculeFeature.getRetentionTimeStart());
            jg.writeNumber(smallMoleculeFeature.getRetentionTimeEnd());

            smallMoleculeFeature.getAbundanceAssay().
                forEach((abundance_assay) ->
                {
                    try {
                        jg.writeNumber(abundance_assay);
                    } catch (IOException ex) {
                        Logger.getLogger(SmallMoleculeFeatureSerializer.class.
                            getName()).
                            log(Level.SEVERE, null, ex);
                    }
                });
            //TODO opt columns
            for(OptColumnMapping ocm:smallMoleculeFeature.getOpt()) {
                jg.writeString(ocm.getValue());
            }
        } else {
            System.err.println("StudyVariable is null!");
        }
    }
}
