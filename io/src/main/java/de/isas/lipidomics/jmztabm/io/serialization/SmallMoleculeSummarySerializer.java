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
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.writeAsNumberArray;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.writeAsStringArray;
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.SmallMoleculeSummary;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class SmallMoleculeSummarySerializer extends StdSerializer<SmallMoleculeSummary> {
    
    public SmallMoleculeSummarySerializer() {
        this(null);
    }
    
    public SmallMoleculeSummarySerializer(Class<SmallMoleculeSummary> t) {
        super(t);
    }
    
    @Override
    public void serialize(SmallMoleculeSummary smallMoleculeSummary,
        JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (smallMoleculeSummary != null) {
            
            jg.writeString(SmallMoleculeSummary.PrefixEnum.SML.getValue());
            jg.writeString(smallMoleculeSummary.getSmlId());
            jg.writeStartArray();
            writeAsStringArray(jg, smallMoleculeSummary.getSmfIdRefs());
            writeAsStringArray(jg, smallMoleculeSummary.getDatabaseIdentifier());
            writeAsStringArray(jg, smallMoleculeSummary.getChemicalFormula());
            writeAsStringArray(jg, smallMoleculeSummary.getSmiles());
            writeAsStringArray(jg, smallMoleculeSummary.getInchi());
            writeAsStringArray(jg, smallMoleculeSummary.getChemicalName());
            writeAsStringArray(jg, smallMoleculeSummary.getUri());
            writeAsNumberArray(jg, smallMoleculeSummary.
                getTheoreticalNeutralMass());
            Serializers.writeNumber(jg, smallMoleculeSummary.
                getExpMassToCharge());
            Serializers.writeNumber(jg, smallMoleculeSummary.getRetentionTime());
            writeAsStringArray(jg, smallMoleculeSummary.getAdductIons());
            jg.writeString(smallMoleculeSummary.getReliability());
            jg.writeString(ParameterSerializer.toString(smallMoleculeSummary.
                getBestIdConfidenceMeasure()));
            Serializers.writeNumber(jg, smallMoleculeSummary.
                getBestIdConfidenceValue());
            smallMoleculeSummary.getAbundanceAssay().
                forEach((abundance_assay) ->
                {
                    try {
                        Serializers.writeNumber(jg, abundance_assay);
                    } catch (IOException ex) {
                        Logger.getLogger(SmallMoleculeSummarySerializer.class.
                            getName()).
                            log(Level.SEVERE, null, ex);
                    }
                });
            
            smallMoleculeSummary.getAbundanceStudyVariable().
                forEach((abundance_sv) ->
                {
                    try {
                        Serializers.writeNumber(jg, abundance_sv);
                    } catch (IOException ex) {
                        Logger.getLogger(SmallMoleculeSummarySerializer.class.
                            getName()).
                            log(Level.SEVERE, null, ex);
                    }
                });
            
            smallMoleculeSummary.getAbundanceCoeffvarStudyVariable().
                forEach((abundance_coeffvar_sv) ->
                {
                    try {
                        Serializers.writeNumber(jg, abundance_coeffvar_sv);
                    } catch (IOException ex) {
                        Logger.getLogger(SmallMoleculeSummarySerializer.class.
                            getName()).
                            log(Level.SEVERE, null, ex);
                    }
                });
            //TODO opt columns
            for (OptColumnMapping ocm : Optional.ofNullable(
                smallMoleculeSummary.getOpt()).
                orElse(Collections.emptyList())) {
                jg.writeString(ocm.getValue());
            }
        } else {
            System.err.println("SmallMoleculeSummary is null!");
        }
    }
}
