/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
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
        } else {
            System.err.println("StudyVariable is null!");
        }
    }
}
