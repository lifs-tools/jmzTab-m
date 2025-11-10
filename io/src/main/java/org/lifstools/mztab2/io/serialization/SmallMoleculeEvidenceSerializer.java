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
import static org.lifstools.mztab2.io.serialization.Serializers.writeAsStringArray;
import static org.lifstools.mztab2.io.serialization.Serializers.writeNumber;
import static org.lifstools.mztab2.io.serialization.Serializers.writeObject;
import static org.lifstools.mztab2.io.serialization.Serializers.writeString;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NULL;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn;
import static uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn.Stable.columnFor;

/**
* <p>SmallMoleculeEvidenceSerializer implementation for {@link org.lifstools.mztab2.model.SmallMoleculeEvidence}.</p>
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
    public void serialize(SmallMoleculeEvidence smallMoleculeEvidence,
        JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (smallMoleculeEvidence != null) {
            jg.writeStartObject();
            writeString(SmallMoleculeEvidence.HeaderPrefixEnum.SEH.getValue(),
                jg, SmallMoleculeEvidence.PrefixEnum.SME.
                    getValue());
            writeNumber(columnFor(
                SmallMoleculeEvidenceColumn.Stable.SME_ID), jg,
                smallMoleculeEvidence.getSmeId());
            writeString(columnFor(
                SmallMoleculeEvidenceColumn.Stable.EVIDENCE_INPUT_ID),
                jg, smallMoleculeEvidence.getEvidenceInputId());
            writeString(columnFor(
                SmallMoleculeEvidenceColumn.Stable.DATABASE_IDENTIFIER),
                jg, smallMoleculeEvidence.getDatabaseIdentifier());
            writeString(columnFor(
                SmallMoleculeEvidenceColumn.Stable.CHEMICAL_FORMULA), jg,
                smallMoleculeEvidence.getChemicalFormula());
            writeString(columnFor(SmallMoleculeEvidenceColumn.Stable.SMILES), jg,
                smallMoleculeEvidence.getSmiles());
            writeString(columnFor(SmallMoleculeEvidenceColumn.Stable.INCHI), jg,
                smallMoleculeEvidence.getInchi());
            writeString(columnFor(
                SmallMoleculeEvidenceColumn.Stable.CHEMICAL_NAME), jg,
                smallMoleculeEvidence.getChemicalName());
            if (smallMoleculeEvidence.getUri() == null) {
                writeString(columnFor(SmallMoleculeEvidenceColumn.Stable.URI), jg,
                    NULL);
            } else {
                writeString(columnFor(SmallMoleculeEvidenceColumn.Stable.URI), jg,
                    smallMoleculeEvidence.getUri().toASCIIString());
            }
            writeObject(columnFor(
                SmallMoleculeEvidenceColumn.Stable.DERIVATIZED_FORM), jg,
                sp,
                smallMoleculeEvidence.getDerivatizedForm());
            writeString(columnFor(SmallMoleculeEvidenceColumn.Stable.ADDUCT_ION),
                jg,
                smallMoleculeEvidence.getAdductIon());
            writeNumber(columnFor(
                SmallMoleculeEvidenceColumn.Stable.EXP_MASS_TO_CHARGE),
                jg, smallMoleculeEvidence.
                    getExpMassToCharge());
            writeNumber(columnFor(SmallMoleculeEvidenceColumn.Stable.CHARGE), jg,
                smallMoleculeEvidence.getCharge());
            writeNumber(
                columnFor(
                    SmallMoleculeEvidenceColumn.Stable.THEORETICAL_MASS_TO_CHARGE),
                jg, smallMoleculeEvidence.
                    getTheoreticalMassToCharge());
            writeAsStringArray(columnFor(
                SmallMoleculeEvidenceColumn.Stable.SPECTRA_REF),
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
            writeObject(columnFor(
                SmallMoleculeEvidenceColumn.Stable.IDENTIFICATION_METHOD),
                jg, sp, smallMoleculeEvidence.getIdentificationMethod());
            writeObject(columnFor(SmallMoleculeEvidenceColumn.Stable.MS_LEVEL),
                jg, sp,
                smallMoleculeEvidence.
                    getMsLevel());
            Serializers.writeIndexedDoubles(
                SmallMoleculeEvidence.Properties.idConfidenceMeasure.
                    getPropertyName(), jg,
                Optional.ofNullable(smallMoleculeEvidence.
                    getIdConfidenceMeasure()).
                    orElse(Collections.emptyList()));
            writeNumber(columnFor(SmallMoleculeEvidenceColumn.Stable.RANK), jg,
                smallMoleculeEvidence.getRank());
            Serializers.writeOptColumnMappings(smallMoleculeEvidence.getOpt(),
                jg, sp);
            jg.writeEndObject();
        } else {
            log.debug(SmallMoleculeEvidence.class.getSimpleName() + " is null!");
        }
    }
}
