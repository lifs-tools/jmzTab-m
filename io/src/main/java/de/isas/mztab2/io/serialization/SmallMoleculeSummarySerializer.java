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
package de.isas.mztab2.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.mztab2.io.serialization.Serializers.writeAsNumberArray;
import static de.isas.mztab2.io.serialization.Serializers.writeAsStringArray;
import static de.isas.mztab2.io.serialization.Serializers.writeNumber;
import static de.isas.mztab2.io.serialization.Serializers.writeOptColumnMappings;
import static de.isas.mztab2.io.serialization.Serializers.writeString;
import de.isas.mztab2.model.SmallMoleculeSummary;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab2.model.AbundanceColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn;
import static de.isas.mztab2.io.serialization.Serializers.writeIndexedDoubles;
import static de.isas.mztab2.io.serialization.Serializers.writeObject;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import static uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn.Stable.columnFor;

/**
 * <p>
 * SmallMoleculeSummarySerializer class.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
public class SmallMoleculeSummarySerializer extends StdSerializer<SmallMoleculeSummary> {

    /**
     * <p>
     * Constructor for SmallMoleculeSummarySerializer.</p>
     */
    public SmallMoleculeSummarySerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for SmallMoleculeSummarySerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public SmallMoleculeSummarySerializer(Class<SmallMoleculeSummary> t) {
        super(t);
    }

    @Override
    public void serializeWithType(SmallMoleculeSummary value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(SmallMoleculeSummary smallMoleculeSummary,
        JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (smallMoleculeSummary != null) {
            jg.writeStartObject();
            writeString(SmallMoleculeSummary.HeaderPrefixEnum.SMH.getValue(), jg,
                SmallMoleculeSummary.PrefixEnum.SML.
                    getValue());
            writeNumber(columnFor(SmallMoleculeColumn.Stable.SML_ID), jg,
                smallMoleculeSummary.getSmlId());
            writeAsNumberArray(columnFor(SmallMoleculeColumn.Stable.SMF_ID_REFS),
                jg,
                Optional.ofNullable(smallMoleculeSummary.
                    getSmfIdRefs()).
                    orElse(Collections.emptyList()));
            writeAsStringArray(columnFor(
                SmallMoleculeColumn.Stable.DATABASE_IDENTIFIER),
                jg, smallMoleculeSummary.getDatabaseIdentifier());
            writeAsStringArray(columnFor(
                SmallMoleculeColumn.Stable.CHEMICAL_FORMULA), jg,
                smallMoleculeSummary.getChemicalFormula());
            writeAsStringArray(columnFor(SmallMoleculeColumn.Stable.SMILES), jg,
                smallMoleculeSummary.getSmiles());
            writeAsStringArray(columnFor(SmallMoleculeColumn.Stable.INCHI), jg,
                smallMoleculeSummary.getInchi());
            writeAsStringArray(columnFor(
                SmallMoleculeColumn.Stable.CHEMICAL_NAME), jg,
                smallMoleculeSummary.getChemicalName());
            writeAsStringArray(columnFor(SmallMoleculeColumn.Stable.URI), jg,
                smallMoleculeSummary.getUri());
            writeAsNumberArray(columnFor(
                SmallMoleculeColumn.Stable.THEOR_NEUTRAL_MASS), jg,
                smallMoleculeSummary.
                    getTheoreticalNeutralMass());
            writeAsStringArray(columnFor(SmallMoleculeColumn.Stable.ADDUCT_IONS),
                jg,
                smallMoleculeSummary.getAdductIons());
            writeString(columnFor(SmallMoleculeColumn.Stable.RELIABILITY), jg,
                smallMoleculeSummary.getReliability());
            writeObject(
                columnFor(SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_MEASURE),
                jg, sp,
                smallMoleculeSummary.
                    getBestIdConfidenceMeasure());
            writeNumber(
                columnFor(SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_VALUE),
                jg,
                smallMoleculeSummary.
                    getBestIdConfidenceValue());
            writeIndexedDoubles(AbundanceColumn.Field.ABUNDANCE_ASSAY.toString(),
                jg, Optional.ofNullable(smallMoleculeSummary.
                    getAbundanceAssay()).
                    orElse(Collections.emptyList()));
            writeIndexedDoubles(AbundanceColumn.Field.ABUNDANCE_STUDY_VARIABLE.
                toString(), jg, Optional.ofNullable(smallMoleculeSummary.
                    getAbundanceStudyVariable()).
                    orElse(Collections.emptyList()));
            writeIndexedDoubles(
                AbundanceColumn.Field.ABUNDANCE_VARIATION_STUDY_VARIABLE.
                    toString(), jg,
                Optional.ofNullable(smallMoleculeSummary.
                    getAbundanceVariationStudyVariable()).
                    orElse(Collections.emptyList()));

            writeOptColumnMappings(smallMoleculeSummary.getOpt(), jg, sp);
            jg.writeEndObject();
        } else {
            log.debug(SmallMoleculeSummary.class.getSimpleName() + " is null!");
        }
    }

}
