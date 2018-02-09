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
import de.isas.mztab1_1.model.SmallMoleculeEvidence;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class SmallMoleculeEvidenceSerializer extends StdSerializer<SmallMoleculeEvidence> {

    public SmallMoleculeEvidenceSerializer() {
        this(null);
    }

    public SmallMoleculeEvidenceSerializer(Class<SmallMoleculeEvidence> t) {
        super(t);
    }

    @Override
    public void serialize(SmallMoleculeEvidence smallMoleculeEvidence,
        JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (smallMoleculeEvidence != null) {
            jg.writeString(SmallMoleculeEvidence.PrefixEnum.SME.getValue());
            jg.writeNumber(smallMoleculeEvidence.getSmeId());
            jg.writeNumber(smallMoleculeEvidence.getEvidenceUniqueId());
            jg.writeString(smallMoleculeEvidence.getDatabaseIdentifier());
            jg.writeString(smallMoleculeEvidence.getChemicalFormula());
            jg.writeString(smallMoleculeEvidence.getSmiles());
            jg.writeString(smallMoleculeEvidence.getInchi());
            jg.writeString(smallMoleculeEvidence.getChemicalName());
            jg.writeString(smallMoleculeEvidence.getUri());
            jg.writeString(ParameterSerializer.toString(smallMoleculeEvidence.
                getDerivatizedForm()));
            jg.writeString(smallMoleculeEvidence.getAdductIon());
            Serializers.writeNumber(jg, smallMoleculeEvidence.
                getExpMassToCharge());
            Serializers.writeNumber(jg, smallMoleculeEvidence.getCharge());
            Serializers.writeNumber(jg, smallMoleculeEvidence.
                getTheoreticalMassToCharge());
            Serializers.writeAsStringArray(jg, smallMoleculeEvidence.
                getSpectraRef().
                stream().
                map((spectraRef) ->
                {
                    return "ms_run[" + spectraRef.getMsRun().
                        getId() + "]" + spectraRef.getReference();
                }).
                collect(Collectors.toList()));
            jg.writeString(ParameterSerializer.toString(smallMoleculeEvidence.
                getIdentificationMethod()));
            jg.writeString(ParameterSerializer.toString(smallMoleculeEvidence.
                getMsLevel()));
            Serializers.writeAsNumberArray(jg, smallMoleculeEvidence.
                getIdConfidenceMeasure());
            Serializers.writeNumber(jg, smallMoleculeEvidence.getRank());
            for (OptColumnMapping ocm : Optional.ofNullable(
                smallMoleculeEvidence.getOpt()).
                orElse(Collections.emptyList())) {
                jg.writeString(ocm.getValue());
            }
        } else {
            System.err.println("SmallMoleculeEvidence is null!");
        }
    }
}
