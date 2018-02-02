/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementParameters;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementStrings;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.Sample;
import de.isas.mztab1_1.model.StudyVariable;
import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class StudyVariableSerializer extends StdSerializer<StudyVariable> {

    public StudyVariableSerializer() {
        this(null);
    }

    public StudyVariableSerializer(Class<StudyVariable> t) {
        super(t);
    }

    @Override
    public void serialize(StudyVariable studyVariable, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (studyVariable != null) {
            addLineWithProperty(jg, Section.Metadata.getPrefix(), "name",
                studyVariable,
                studyVariable.getName());
            addLineWithProperty(jg, Section.Metadata.getPrefix(), "description",
                studyVariable, studyVariable.getDescription());
            addSubElementParameters(jg, Section.Metadata.getPrefix(),
                studyVariable,
                "factors", studyVariable.getFactors(), true);
            addSubElementParameters(jg, Section.Metadata.getPrefix(),
                studyVariable,
                "quantification_value_function", studyVariable.
                    getQuantificationValueFunction(), true);
            addSubElementStrings(jg, Section.Metadata.getPrefix(), studyVariable,
                "assay_refs", studyVariable.getAssayRefs().
                    stream().
                    sorted(Comparator.comparing(Assay::getId,
                        Comparator.nullsFirst(Comparator.
                            naturalOrder())
                    )).
                    map((assayRef) ->
                    {
                        return new StringBuilder().append("assay").
                            append("[").
                            append(assayRef.getId()).
                            append("]").
                            toString();
                    }).
                    collect(Collectors.toList()), true);
            addSubElementStrings(jg, Section.Metadata.getPrefix(), studyVariable,
                "sample_refs", studyVariable.getSampleRefs().
                    stream().
                    sorted(Comparator.comparing(Sample::getId,
                        Comparator.nullsFirst(Comparator.
                            naturalOrder())
                    )).
                    map((assayRef) ->
                    {
                        return new StringBuilder().append("sample").
                            append("[").
                            append(assayRef.getId()).
                            append("]").
                            toString();
                    }).
                    collect(Collectors.toList()), true);
        } else {
            System.err.println("StudyVariable is null!");
        }
    }
}
