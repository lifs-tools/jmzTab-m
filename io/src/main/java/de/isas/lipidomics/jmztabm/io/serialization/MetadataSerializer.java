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
import de.isas.lipidomics.jmztabm.io.MzTabWriter;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addIndexedLine;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLine;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithMetadataProperty;
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
import uk.ac.ebi.pride.jmztab1_1.model.MetadataProperty;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 * <p>MetadataSerializer class.</p>
 *
 * @author nilshoffmann
 * 
 */
public class MetadataSerializer extends StdSerializer<Metadata> {

    /**
     * <p>Constructor for MetadataSerializer.</p>
     */
    public MetadataSerializer() {
        this(null);
    }

    /**
     * <p>Constructor for MetadataSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public MetadataSerializer(Class<Metadata> t) {
        super(t);
    }

    /**
     * <p>serializeListWithMetadataElement.</p>
     *
     * @param list a {@link java.util.List} object.
     * @param mtdElement a {@link uk.ac.ebi.pride.jmztab1_1.model.MetadataElement} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider} object.
     * @param comparator a {@link java.util.Comparator} object.
     * @param <T> a T object.
     */
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

    /**
     * <p>serializeList.</p>
     *
     * @param list a {@link java.util.List} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider} object.
     * @param comparator a {@link java.util.Comparator} object.
     * @param <T> a T object.
     */
    public static <T> void serializeList(List<T> list, JsonGenerator jg,
        SerializerProvider sp, Comparator<? super T> comparator) {
        list.stream().
            sorted(comparator).
            forEach((object) ->
            {
                serializeObject(object, jg, sp);
            });
    }

    /**
     * <p>serializeObject.</p>
     *
     * @param object a {@link java.lang.Object} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider} object.
     */
    public static void serializeObject(Object object, JsonGenerator jg,
        SerializerProvider sp) {
        try {
            Logger.getLogger(MetadataSerializer.class.getName()).
                log(Level.FINE,
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

    /** {@inheritDoc} */
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
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Contacts are null!");
            }
            //publications
            if (t.getPublications() != null) {
                serializeList(t.getPublications(), jg, sp, Comparator.comparing(
                    Publication::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Publications are null!");
            }
            //external study
            if (t.getStudy() != null) {
                serializeObject(t.getStudy(), jg, sp);
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "External Study is null!");
            }
            //instruments
            if (t.getInstruments() != null) {
                serializeList(
                    t.getInstruments(), jg, sp, Comparator.comparing(
                    Instrument::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Instruments are null!");
            }
            //quantification method
            if (t.getQuantificationMethod() != null) {
                addLineWithParameters(jg, prefix, "quantification_method",
                    t.getQuantificationMethod());
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Quantification method is null!");
            }
            //sample
            if (t.getSample() != null) {
                serializeList(
                    t.getSample(), jg, sp, Comparator.comparing(Sample::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Samples are null!");
            }
            //sample processing
            if (t.getSampleProcessing() != null) {
                serializeList(
                    t.getSampleProcessing(), jg, sp, Comparator.comparing(
                    SampleProcessing::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Sample processing is null!");
            }
            //software
            if (t.getSoftware() != null) {
                serializeList(
                    t.getSoftware(), jg, sp, Comparator.comparing(
                    Software::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Software is null!");
            }
            //chemical modification
            if (t.getChemicalModification() != null) {
                throw new RuntimeException(
                    "Handling of chemical_modification has not yet been implemented!");
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Chemical modification is null!");
            }
            //ms run
            if (t.getMsrun() != null) {
                serializeList(t.getMsrun(), jg, sp, Comparator.comparing(
                    MsRun::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "MS runs are null!");
            }
            //assay
            if (t.getAssay() != null) {
                serializeList(t.getAssay(), jg, sp, Comparator.comparing(
                    Assay::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Assays are null!");
            }
            //study variable
            if (t.getStudyVariable() != null) {
                serializeList(
                    t.getStudyVariable(), jg, sp, Comparator.comparing(
                    StudyVariable::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Study Variable is null!");
            }
            //custom
            if (t.getCustom() != null) {
                serializeListWithMetadataElement(t.getCustom(),
                    MetadataElement.CUSTOM, jg, sp, Comparator.comparing(
                        Parameter::getId, Comparator.nullsFirst(Comparator.
                            naturalOrder())));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Custom is null!");
            }
            //cv
            if (t.getCv() != null) {
                serializeList(t.getCv(), jg, sp, Comparator.comparing(
                    CV::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "CVs are null!");
            }
            //small molecule quantification unit
            if (t.getSmallMoleculeQuantificationUnit() != null) {
                addLineWithMetadataProperty(jg, prefix,
                    MetadataProperty.SMALL_MOLECULE_QUANTIFICATION_UNIT,
                    MetadataElement.SMALL_MOLECULE, ParameterSerializer.
                        toString(t.getSmallMoleculeQuantificationUnit()));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE,
                        "Small molecule quantification unit is null!");
            }
            //small molecule feature quantification unit
            if (t.getSmallMoleculeFeatureQuantificationUnit() != null) {
                addLineWithMetadataProperty(jg, prefix,
                    MetadataProperty.SMALL_MOLECULE_FEATURE_QUANTIFICATION_UNIT,
                    MetadataElement.SMALL_MOLECULE_FEATURE, ParameterSerializer.
                        toString(t.getSmallMoleculeFeatureQuantificationUnit()));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE,
                        "Small molecule feature quantification unit is null!");
            }
            //small molecule identification reliability
            if (t.getSmallMoleculeIdentificationReliability() != null) {
                addLineWithMetadataProperty(jg, prefix,
                    MetadataProperty.SMALL_MOLECULE_IDENTIFICATION_RELIABILITY,
                    MetadataElement.SMALL_MOLECULE, ParameterSerializer.
                        toString(t.getSmallMoleculeIdentificationReliability()));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE,
                        "Small molecule identification reliability is null!");
            }
            //database
            if (t.getDatabase() != null) {
                serializeList(t.getDatabase(), jg, sp, Comparator.comparing(
                    Database::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Databases are null!");
            }
            if (t.getIdConfidenceMeasure() != null) {
                serializeListWithMetadataElement(t.getIdConfidenceMeasure(),
                    MetadataElement.ID_CONFIDENCE_MEASURE, jg, sp, Comparator.
                        comparing(
                            Parameter::getId, Comparator.nullsFirst(Comparator.
                                naturalOrder())));
            } else {
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Id confidence measure is null!");
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
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Colunit small molecule is null!");
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
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Colunit small molecule feature is null!");
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
                Logger.getLogger(MetadataSerializer.class.getName()).
                    log(Level.FINE, "Colunit small molecule evidence is null!");
            }
        }
    }

}
