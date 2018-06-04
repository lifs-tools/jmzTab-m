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
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.mztab2.io.serialization.Serializers.writeAsNumberArray;
import static de.isas.mztab2.io.serialization.Serializers.writeAsStringArray;
import static de.isas.mztab2.io.serialization.Serializers.writeNumber;
import static de.isas.mztab2.io.serialization.Serializers.writeOptColumnMappings;
import static de.isas.mztab2.io.serialization.Serializers.writeString;
import de.isas.mztab2.model.SmallMoleculeSummary;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab2.model.AbundanceColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn;
import static de.isas.mztab2.io.serialization.Serializers.writeIndexedDoubles;
import static de.isas.mztab2.io.serialization.Serializers.writeObject;

/**
 * <p>
 * SmallMoleculeSummarySerializer class.</p>
 *
 * @author nilshoffmann
 *
 */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(SmallMoleculeSummary smallMoleculeSummary,
        JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (smallMoleculeSummary != null) {
            jg.writeStartObject();
            writeString(SmallMoleculeSummary.HeaderPrefixEnum.SMH.getValue(), jg, SmallMoleculeSummary.PrefixEnum.SML.
                getValue());
            writeString(SmallMoleculeColumn.Stable.SML_ID, jg,
                smallMoleculeSummary.getSmlId());
            writeAsStringArray(SmallMoleculeColumn.Stable.SMF_ID_REFS, jg,
                smallMoleculeSummary.getSmfIdRefs());
            writeAsStringArray(SmallMoleculeColumn.Stable.DATABASE_IDENTIFIER,
                jg, smallMoleculeSummary.getDatabaseIdentifier());
            writeAsStringArray(SmallMoleculeColumn.Stable.CHEMICAL_FORMULA, jg,
                smallMoleculeSummary.getChemicalFormula());
            writeAsStringArray(SmallMoleculeColumn.Stable.SMILES, jg,
                smallMoleculeSummary.getSmiles());
            writeAsStringArray(SmallMoleculeColumn.Stable.INCHI, jg,
                smallMoleculeSummary.getInchi());
            writeAsStringArray(SmallMoleculeColumn.Stable.CHEMICAL_NAME, jg,
                smallMoleculeSummary.getChemicalName());
            writeAsStringArray(SmallMoleculeColumn.Stable.URI, jg,
                smallMoleculeSummary.getUri());
            writeAsNumberArray(SmallMoleculeColumn.Stable.THEOR_NEUTRAL_MASS, jg,
                smallMoleculeSummary.
                    getTheoreticalNeutralMass());
            writeAsStringArray(SmallMoleculeColumn.Stable.ADDUCT_IONS, jg,
                smallMoleculeSummary.getAdductIons());
            writeString(SmallMoleculeColumn.Stable.RELIABILITY, jg,
                smallMoleculeSummary.getReliability());
            writeObject(
                SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_MEASURE, jg, sp,
                smallMoleculeSummary.
                    getBestIdConfidenceMeasure());
            writeNumber(
                SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_VALUE, jg,
                smallMoleculeSummary.
                    getBestIdConfidenceValue());
            writeIndexedDoubles(AbundanceColumn.Field.ABUNDANCE_ASSAY.toString(),
                jg, smallMoleculeSummary.
                    getAbundanceAssay());
            writeIndexedDoubles(AbundanceColumn.Field.ABUNDANCE_STUDY_VARIABLE.
                toString(), jg, smallMoleculeSummary.
                    getAbundanceStudyVariable());
            writeIndexedDoubles(
                AbundanceColumn.Field.ABUNDANCE_VARIATION_STUDY_VARIABLE.
                    toString(), jg,
                smallMoleculeSummary.getAbundanceVariationStudyVariable());

            writeOptColumnMappings(smallMoleculeSummary.getOpt(), jg, sp);
            jg.writeEndObject();
        } else {
            Logger.getLogger(SmallMoleculeSummarySerializer.class.getName()).
                log(Level.FINE, "{0} null!", smallMoleculeSummary.getClass().getSimpleName());
        }
    }

}
