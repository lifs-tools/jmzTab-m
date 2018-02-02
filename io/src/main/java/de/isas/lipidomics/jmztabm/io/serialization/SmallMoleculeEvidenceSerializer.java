/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import de.isas.mztab1_1.model.SmallMoleculeEvidence;
import java.io.IOException;
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
            jg.writeString("SME");
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
            jg.writeNumber(smallMoleculeEvidence.getExpMassToCharge());
            jg.writeNumber(smallMoleculeEvidence.getCharge());
            jg.writeNumber(smallMoleculeEvidence.getTheoreticalMassToCharge());
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
            jg.writeNumber(smallMoleculeEvidence.getRank());
            //TODO opt columns
        } else {
            System.err.println("StudyVariable is null!");
        }
    }
}
