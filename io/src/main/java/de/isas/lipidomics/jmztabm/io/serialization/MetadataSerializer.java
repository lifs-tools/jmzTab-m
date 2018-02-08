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
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addIndexedLine;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLine;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithParameters;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Database;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.Sample;
import de.isas.mztab1_1.model.SampleProcessing;
import de.isas.mztab1_1.model.Software;
import de.isas.mztab1_1.model.StudyVariable;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataElement;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MetadataSerializer extends StdSerializer<Metadata> {

    public MetadataSerializer() {
        this(null);
    }

    public MetadataSerializer(Class<Metadata> t) {
        super(t);
    }

    public static <T extends Parameter> void serializeListWithMetadataElement(
        List<T> list, MetadataElement mtdElement, JsonGenerator jg,
        SerializerProvider sp, Comparator<? super T> comparator) {
        list.stream().
            sorted(comparator).
            forEach((object) ->
            {
                addIndexedLine(jg, Section.Metadata.getPrefix(),
                    mtdElement.getName() + "[" + object.getId() + "]",
                    object);
            });
    }

    public static <T> void serializeList(List<T> list, JsonGenerator jg,
        SerializerProvider sp, Comparator<? super T> comparator) {
        list.stream().
            sorted(comparator).
            forEach((object) ->
            {
                serializeObject(object, jg, sp);
            });
    }

    public static void serializeObject(Object object, JsonGenerator jg,
        SerializerProvider sp) {
        try {
            System.err.println(
                "Serializing element " + Serializers.
                    getElementName(object).
                    get());
            sp.findValueSerializer(object.getClass()).
                serialize(object, jg, sp);
        } catch (IOException ex) {
            Logger.getLogger(MetadataSerializer.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void serialize(Metadata t, JsonGenerator jg, SerializerProvider sp) throws IOException {
        if (t != null) {
            String prefix = t.getPrefix().
                name();
            addLine(jg, prefix, "mzTab-version", t.
                getMzTabVersion());
            addLine(jg, prefix, "mzTab-ID", t.
                getMzTabID());
            addLine(jg, prefix, "title", t.
                getTitle());
            addLine(jg, prefix, "description", t.
                getDescription());
            //contacts
            if (t.getContacts() != null) {
                serializeList(t.getContacts(), jg, sp, Comparator.comparing(
                    Contact::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("Contacts are null!");
            }
            //publications
            if (t.getPublications() != null) {
                serializeList(t.getPublications(), jg, sp, Comparator.comparing(
                    Publication::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("Publications are null!");
            }
            //external study
            if (t.getStudy() != null) {
                serializeObject(t.getStudy(), jg, sp);
            } else {
                System.err.println("External Study is null!");
            }
            //instruments
            if (t.getInstruments() != null) {
                serializeList(
                    t.getInstruments(), jg, sp, Comparator.comparing(
                    Instrument::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("Instruments is null!");
            }
            //quantification method
            if (t.getQuantificationMethod() != null) {
                addLineWithParameters(jg, prefix, "quantification_method",
                    t.getQuantificationMethod());
            } else {
                System.err.println("Quantification method is null!");
            }
            //sample
            if (t.getSample() != null) {
                serializeList(
                    t.getSample(), jg, sp, Comparator.comparing(Sample::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("Samples are null!");
            }
            //sample processing
            if (t.getSampleProcessing() != null) {
                serializeList(
                    t.getSampleProcessing(), jg, sp, Comparator.comparing(
                    SampleProcessing::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("Sample processing is null!");
            }
            //software
            if (t.getSoftware() != null) {
                serializeList(
                    t.getSoftware(), jg, sp, Comparator.comparing(
                    Software::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("Software is null!");
            }
            //study variable
            if (t.getStudyVariable() != null) {
                serializeList(
                    t.getStudyVariable(), jg, sp, Comparator.comparing(
                    StudyVariable::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("Study Variable is null!");
            }
            //chemical modification
            if (t.getChemicalModification() != null) {
                throw new RuntimeException(
                    "Handling of chemical_modification has not yet been implemented!");
            } else {
                System.err.println("Chemical modification is null!");
            }
            //ms run
            if (t.getMsrun() != null) {
                serializeList(t.getMsrun(), jg, sp, Comparator.comparing(
                    MsRun::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("MS runs are null!");
            }
            //assay
            if (t.getAssay() != null) {
                serializeList(t.getAssay(), jg, sp, Comparator.comparing(
                    Assay::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("Assays are null!");
            }
            //custom
            if (t.getCustom() != null) {
                serializeListWithMetadataElement(t.getCustom(),
                    MetadataElement.CUSTOM, jg, sp, Comparator.comparing(
                        Parameter::getId, Comparator.nullsFirst(Comparator.
                            naturalOrder())));
            } else {
                System.err.println("Customs are null!");
            }
            if (t.getCv() != null) {
                serializeList(t.getCv(), jg, sp, Comparator.comparing(
                    CV::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("CVs are null!");
            }
            if (t.getDatabase() != null) {
                serializeList(t.getDatabase(), jg, sp, Comparator.comparing(
                    Database::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                System.err.println("Databases are null!");
            }
            if (t.getIdConfidenceMeasure() != null) {
                serializeListWithMetadataElement(t.getIdConfidenceMeasure(),
                    MetadataElement.ID_CONFIDENCE_MEASURE, jg, sp, Comparator.
                        comparing(
                            Parameter::getId, Comparator.nullsFirst(Comparator.
                                naturalOrder())));
            } else {
                System.err.println("Id confidence measure is null!");
            }
            if (t.getColunitSmallMolecule() != null) {
                t.getColunitSmallMolecule().
                    forEach((colUnit) ->
                    {
                        addLine(jg, prefix,
                            MetadataElement.COLUNIT_SMALL_MOLECULE, colUnit.
                                getColumnName() + "=" + ParameterSerializer.
                                toString(colUnit.getParam()));
                    });
            } else {
                System.err.println("Colunit small molecule is null!");
            }
            if (t.getColunitSmallMoleculeFeature() != null) {
                t.getColunitSmallMoleculeFeature().
                    forEach((colUnit) ->
                    {
                        addLine(jg, prefix,
                            MetadataElement.COLUNIT_SMALL_MOLECULE_FEATURE,
                            colUnit.
                                getColumnName() + "=" + ParameterSerializer.
                                toString(colUnit.getParam()));
                    });
            } else {
                System.err.println("Colunit small molecule feature is null!");
            }
            if (t.getColunitSmallMoleculeEvidence() != null) {
                t.getColunitSmallMoleculeEvidence().
                    forEach((colUnit) ->
                    {
                        addLine(jg, prefix,
                            MetadataElement.COLUNIT_SMALL_MOLECULE_EVIDENCE,
                            colUnit.
                                getColumnName() + "=" + ParameterSerializer.
                                toString(colUnit.getParam()));
                    });
            } else {
                System.err.println("Colunit small molecule evidence is null!");
            }
        }
    }

}
