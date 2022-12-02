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
import static org.lifstools.mztab2.io.serialization.Serializers.writeAsNumberArray;
import static org.lifstools.mztab2.io.serialization.Serializers.writeNumber;
import static org.lifstools.mztab2.io.serialization.Serializers.writeObject;
import static org.lifstools.mztab2.io.serialization.Serializers.writeString;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.AbundanceColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn;
import static uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn.Stable.columnFor;

/**
 * <p>SmallMoleculeFeatureSerializer implementation for {@link org.lifstools.mztab2.model.SmallMoleculeFeature}.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
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

    @Override
    public void serializeWithType(SmallMoleculeFeature value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
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
            writeString(SmallMoleculeFeature.HeaderPrefixEnum.SFH.getValue(), jg,
                SmallMoleculeFeature.PrefixEnum.SMF.
                    getValue());
            writeNumber(columnFor(SmallMoleculeFeatureColumn.Stable.SMF_ID), jg,
                smallMoleculeFeature.getSmfId());
            writeAsNumberArray(columnFor(
                SmallMoleculeFeatureColumn.Stable.SME_ID_REFS), jg,
                Optional.ofNullable(smallMoleculeFeature.getSmeIdRefs()).
                    orElse(Collections.emptyList()));
            writeNumber(
                columnFor(
                    SmallMoleculeFeatureColumn.Stable.SME_ID_REF_AMBIGUITY_CODE),
                jg,
                smallMoleculeFeature.
                    getSmeIdRefAmbiguityCode());
            writeString(columnFor(SmallMoleculeFeatureColumn.Stable.ADDUCT_ION),
                jg,
                smallMoleculeFeature.getAdductIon());
            writeObject(columnFor(SmallMoleculeFeatureColumn.Stable.ISOTOPOMER),
                jg, sp,
                smallMoleculeFeature.
                    getIsotopomer());
            writeNumber(columnFor(
                SmallMoleculeFeatureColumn.Stable.EXP_MASS_TO_CHARGE), jg,
                smallMoleculeFeature.
                    getExpMassToCharge());
            writeNumber(columnFor(SmallMoleculeFeatureColumn.Stable.CHARGE), jg,
                smallMoleculeFeature.getCharge());
            writeNumber(
                columnFor(
                    SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS),
                jg,
                smallMoleculeFeature.
                    getRetentionTimeInSeconds());
            writeNumber(
                columnFor(
                    SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS_START),
                jg, smallMoleculeFeature.
                    getRetentionTimeInSecondsStart());
            writeNumber(
                columnFor(
                    SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS_END),
                jg, smallMoleculeFeature.
                    getRetentionTimeInSecondsEnd());
            Serializers.writeIndexedDoubles(
                AbundanceColumn.Field.ABUNDANCE_ASSAY.toString(), jg,
                Optional.ofNullable(smallMoleculeFeature.getAbundanceAssay()).
                    orElse(Collections.emptyList()));
            Serializers.
                writeOptColumnMappings(smallMoleculeFeature.getOpt(), jg, sp);
            jg.writeEndObject();
        } else {
            log.debug(SmallMoleculeFeature.class.getSimpleName(), " is null!");
        }
    }
}
