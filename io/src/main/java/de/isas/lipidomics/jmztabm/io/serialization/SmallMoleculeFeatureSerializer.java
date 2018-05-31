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
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.writeAsStringArray;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.writeNumber;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.writeObject;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.writeString;
import de.isas.mztab1_1.model.SmallMoleculeFeature;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab1_1.model.AbundanceColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeFeatureColumn;

/**
 * <p>
 * SmallMoleculeFeatureSerializer class.</p>
 *
 * @author nilshoffmann
 *
 */
public class SmallMoleculeFeatureSerializer extends StdSerializer<SmallMoleculeFeature> {

    /**
     * <p>
     * Constructor for SmallMoleculeFeatureSerializer.</p>
     */
    public SmallMoleculeFeatureSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for SmallMoleculeFeatureSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public SmallMoleculeFeatureSerializer(Class<SmallMoleculeFeature> t) {
        super(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(SmallMoleculeFeature smallMoleculeFeature,
        JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (smallMoleculeFeature != null) {
            jg.writeStartObject();
            writeString(SmallMoleculeFeature.HeaderPrefixEnum.SFH.getValue(), jg, SmallMoleculeFeature.PrefixEnum.SMF.
                getValue());
            writeString(SmallMoleculeFeatureColumn.Stable.SMF_ID, jg,
                smallMoleculeFeature.getSmfId());
            writeAsStringArray(SmallMoleculeFeatureColumn.Stable.SME_ID_REFS, jg,
                smallMoleculeFeature.
                    getSmeIdRefs());
            writeNumber(
                SmallMoleculeFeatureColumn.Stable.SME_ID_REF_AMBIGUITY_CODE, jg,
                smallMoleculeFeature.
                    getSmeIdRefAmbiguityCode());
            writeString(SmallMoleculeFeatureColumn.Stable.ADDUCT_ION, jg,
                smallMoleculeFeature.getAdductIon());
            writeObject(SmallMoleculeFeatureColumn.Stable.ISOTOPOMER, jg, sp,
                smallMoleculeFeature.
                    getIsotopomer());
            writeNumber(SmallMoleculeFeatureColumn.Stable.EXP_MASS_TO_CHARGE, jg,
                smallMoleculeFeature.
                    getExpMassToCharge());
            writeNumber(SmallMoleculeFeatureColumn.Stable.CHARGE, jg,
                smallMoleculeFeature.getCharge());
            writeNumber(
                SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS, jg,
                smallMoleculeFeature.
                    getRetentionTimeInSeconds());
            writeNumber(
                SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS_START,
                jg, smallMoleculeFeature.
                    getRetentionTimeInSecondsStart());
            writeNumber(
                SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS_END,
                jg, smallMoleculeFeature.
                    getRetentionTimeInSecondsEnd());
            Serializers.writeIndexedDoubles(
                AbundanceColumn.Field.ABUNDANCE_ASSAY.toString(), jg,
                smallMoleculeFeature.getAbundanceAssay());
            Serializers.
                writeOptColumnMappings(smallMoleculeFeature.getOpt(), jg, sp);
            jg.writeEndObject();
        } else {
            Logger.getLogger(SmallMoleculeFeatureSerializer.class.getName()).
                log(Level.FINE, "{0} is null!", smallMoleculeFeature.getClass().getSimpleName());
        }
    }
}
