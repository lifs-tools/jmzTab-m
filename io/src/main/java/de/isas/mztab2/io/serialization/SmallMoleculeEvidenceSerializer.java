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
import static de.isas.mztab2.io.serialization.Serializers.writeAsStringArray;
import static de.isas.mztab2.io.serialization.Serializers.writeNumber;
import static de.isas.mztab2.io.serialization.Serializers.writeString;
import static de.isas.mztab2.io.serialization.Serializers.writeObject;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.SmallMoleculeEvidence;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn;

/**
 * <p>
 * SmallMoleculeEvidenceSerializer class.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
public class SmallMoleculeEvidenceSerializer extends StdSerializer<SmallMoleculeEvidence> {

    /**
     * <p>
     * Constructor for SmallMoleculeEvidenceSerializer.</p>
     */
    public SmallMoleculeEvidenceSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for SmallMoleculeEvidenceSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public SmallMoleculeEvidenceSerializer(Class<SmallMoleculeEvidence> t) {
        super(t);
    }

    @Override
    public void serializeWithType(SmallMoleculeEvidence value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(SmallMoleculeEvidence smallMoleculeEvidence,
        JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (smallMoleculeEvidence != null) {
            jg.writeStartObject();
            writeString(SmallMoleculeEvidence.HeaderPrefixEnum.SEH.getValue(),
                jg, SmallMoleculeEvidence.PrefixEnum.SME.
                    getValue());
            writeNumber(SmallMoleculeEvidenceColumn.Stable.SME_ID, jg,
                smallMoleculeEvidence.getSmeId());
            writeString(SmallMoleculeEvidenceColumn.Stable.EVIDENCE_INPUT_ID,
                jg, smallMoleculeEvidence.getEvidenceInputId());
            writeString(SmallMoleculeEvidenceColumn.Stable.DATABASE_IDENTIFIER,
                jg, smallMoleculeEvidence.getDatabaseIdentifier());
            writeString(SmallMoleculeEvidenceColumn.Stable.CHEMICAL_FORMULA, jg,
                smallMoleculeEvidence.getChemicalFormula());
            writeString(SmallMoleculeEvidenceColumn.Stable.SMILES, jg,
                smallMoleculeEvidence.getSmiles());
            writeString(SmallMoleculeEvidenceColumn.Stable.INCHI, jg,
                smallMoleculeEvidence.getInchi());
            writeString(SmallMoleculeEvidenceColumn.Stable.CHEMICAL_NAME, jg,
                smallMoleculeEvidence.getChemicalName());
            writeString(SmallMoleculeEvidenceColumn.Stable.URI, jg,
                smallMoleculeEvidence.getUri());
            writeObject(SmallMoleculeEvidenceColumn.Stable.DERIVATIZED_FORM, jg,
                sp,
                smallMoleculeEvidence.getDerivatizedForm());
            writeString(SmallMoleculeEvidenceColumn.Stable.ADDUCT_ION, jg,
                smallMoleculeEvidence.getAdductIon());
            writeNumber(SmallMoleculeEvidenceColumn.Stable.EXP_MASS_TO_CHARGE,
                jg, smallMoleculeEvidence.
                    getExpMassToCharge());
            writeNumber(SmallMoleculeEvidenceColumn.Stable.CHARGE, jg,
                smallMoleculeEvidence.getCharge());
            writeNumber(
                SmallMoleculeEvidenceColumn.Stable.THEORETICAL_MASS_TO_CHARGE,
                jg, smallMoleculeEvidence.
                    getTheoreticalMassToCharge());
            writeAsStringArray(SmallMoleculeEvidenceColumn.Stable.SPECTRA_REF,
                jg, Optional.ofNullable(smallMoleculeEvidence.
                    getSpectraRef()).
                    orElse(Collections.emptyList()).
                    stream().
                    map((spectraRef) ->
                    {
                        return Metadata.Properties.msRun.getPropertyName() + "[" + spectraRef.
                            getMsRun().
                            getId() + "]:" + spectraRef.getReference();
                    }).
                    collect(Collectors.toList()));
            writeObject(SmallMoleculeEvidenceColumn.Stable.IDENTIFICATION_METHOD,
                jg, sp, smallMoleculeEvidence.getIdentificationMethod());
            writeObject(SmallMoleculeEvidenceColumn.Stable.MS_LEVEL, jg, sp,
                smallMoleculeEvidence.
                    getMsLevel());
            Serializers.writeIndexedDoubles(
                SmallMoleculeEvidence.Properties.idConfidenceMeasure.
                    getPropertyName(), jg,
                Optional.ofNullable(smallMoleculeEvidence.
                    getIdConfidenceMeasure()).
                    orElse(Collections.emptyList()));
            writeNumber(SmallMoleculeEvidenceColumn.Stable.RANK, jg,
                smallMoleculeEvidence.getRank());
            Serializers.writeOptColumnMappings(smallMoleculeEvidence.getOpt(),
                jg, sp);
            jg.writeEndObject();
        } else {
            log.debug(SmallMoleculeEvidence.class.getSimpleName() + " is null!");
        }
    }
}
