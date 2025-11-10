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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static org.lifstools.mztab2.io.serialization.Serializers.addIndexedLine;
import static org.lifstools.mztab2.io.serialization.Serializers.addLine;
import static org.lifstools.mztab2.io.serialization.Serializers.addLineWithMetadataProperty;
import static org.lifstools.mztab2.io.serialization.Serializers.addLineWithParameters;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Contact;
import org.lifstools.mztab2.model.Database;
import org.lifstools.mztab2.model.IndexedElement;
import org.lifstools.mztab2.model.Instrument;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.Publication;
import org.lifstools.mztab2.model.Sample;
import org.lifstools.mztab2.model.SampleProcessing;
import org.lifstools.mztab2.model.Software;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.Uri;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.model.MetadataElement;
import uk.ac.ebi.pride.jmztab2.model.MetadataProperty;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>
 * MetadataSerializer class. Implements a custom, partially delegating serializer for {@link org.lifstools.mztab2.model.Metadata} objects 
 * based on Jackson CSV.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
public class MetadataSerializer extends StdSerializer<Metadata> {

    /**
     * <p>
     * Constructor for MetadataSerializer.</p>
     */
    public MetadataSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for MetadataSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public MetadataSerializer(Class<Metadata> t) {
        super(t);
    }

    /**
     * <p>
     * Serialize a list of Parameters for the provided metadata element.</p>
     *
     * @param list a {@link java.util.List} object.
     * @param mtdElement a
     * {@link uk.ac.ebi.pride.jmztab2.model.MetadataElement} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider}
     * object.
     * @param comparator an optional {@link java.util.Comparator} object.
     * @param <T> a T object.
     */
    public static <T extends Object> void serializeListWithMetadataElement(
        List<T> list, MetadataElement mtdElement, JsonGenerator jg,
        SerializerProvider sp, Optional<Comparator<? super T>> comparator) {
        var stream = list.stream();
        if (comparator.isPresent()) {
            stream = stream.sorted(comparator.get());
        }
        stream.
            forEach((object) ->
            {
                if (object != null) {
                    addIndexedLine(jg, sp, Section.Metadata.getPrefix(),
                        mtdElement.getName() + "[" + IndexedElement.of(object).getId() + "]",
                        object);
                } else {
                    throw new NullPointerException(
                        "Object in list for metadata element " + mtdElement.
                            getName() + " must not be null!");
                }
            });
    }

    /**
     * <p>
     * serializeList.</p>
     *
     * @param list a {@link java.util.List} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider}
     * object.
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
     * <p>
     * serializeObject.</p>
     *
     * @param object a {@link java.lang.Object} object.
     * @param jg a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param sp a {@link com.fasterxml.jackson.databind.SerializerProvider}
     * object.
     */
    public static void serializeObject(Object object, JsonGenerator jg,
        SerializerProvider sp) {
        try {
            
                log.debug(
                    "Serializing element " + Serializers.
                        getElementName(object).
                        orElse("undefined"));
            sp.findValueSerializer(object.getClass()).
                serialize(object, jg, sp);
        } catch (IOException ex) {
                log.error("Caught IO exception while trying to serialize "+object, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Metadata t, JsonGenerator jg, SerializerProvider sp) throws IOException {
        if (t != null) {
            String prefix = t.getPrefix().
                name();
            if(t.getMzTabVersion()==null || t.getMzTabVersion().isEmpty()) {
                t.setMzTabVersion(MZTabConstants.VERSION_MZTAB_M);
            }
            addLine(jg, prefix, Metadata.Properties.mzTabVersion.getPropertyName(), t.getMzTabVersion());
            addLine(jg, prefix, Metadata.Properties.mzTabID.getPropertyName(), t.
                getMzTabID());
            addLine(jg, prefix, Metadata.Properties.title.getPropertyName(), t.
                getTitle());
            addLine(jg, prefix, Metadata.Properties.description.getPropertyName(), t.
                getDescription());
            //contacts
            if (t.getContact() != null) {
                serializeList(t.getContact(), jg, sp, Comparator.comparing(
                    Contact::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "Contacts are null!");
            }
            //publications
            if (t.getPublication() != null) {
                serializeList(t.getPublication(), jg, sp, Comparator.comparing(
                    Publication::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "Publications are null!");
            }
            //file uri
            if (t.getUri() != null) {
                serializeListWithMetadataElement(t.getUri(),
                    MetadataElement.URI, jg, sp, Optional.of(Comparator.comparing(
                        Uri::getId,
                        Comparator.nullsFirst(Comparator.naturalOrder()))));
            } else {
                
                    log.debug( "URI is null!");
            }
            //external study uri
            if (t.getExternalStudyUri() != null) {
                serializeListWithMetadataElement(t.getExternalStudyUri(),
                    MetadataElement.EXTERNAL_STUDY_URI, jg, sp, Optional.of(Comparator.
                        comparing(
                            Uri::getId,
                            Comparator.nullsFirst(Comparator.naturalOrder()))));
            } else {
                
                    log.debug( "External Study URI is null!");
            }
            //instruments
            if (t.getInstrument() != null) {
                serializeList(
                    t.getInstrument(), jg, sp, Comparator.comparing(
                    Instrument::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "Instruments are null!");
            }
            //quantification method
            if (t.getQuantificationMethod() != null) {
                addLineWithParameters(jg, prefix, "quantification_method",
                    Arrays.asList(t.getQuantificationMethod()));
            } else {
                
                    log.debug( "Quantification method is null!");
            }
            //sample
            if (t.getSample() != null) {
                serializeList(
                    t.getSample(), jg, sp, Comparator.comparing(Sample::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "Samples are null!");
            }
            //sample processing
            if (t.getSampleProcessing() != null) {
                serializeList(
                    t.getSampleProcessing(), jg, sp, Comparator.comparing(
                    SampleProcessing::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "Sample processing is null!");
            }
            //software
            if (t.getSoftware() != null) {
                serializeList(
                    t.getSoftware(), jg, sp, Comparator.comparing(
                    Software::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "Software is null!");
            }
            //derivatization agent
            if (t.getDerivatizationAgent() != null) {
                serializeListWithMetadataElement(t.getDerivatizationAgent(),
                    MetadataElement.DERIVATIZATION_AGENT, jg, sp, Optional.empty());
            } else {
                
                    log.debug( "Derivatization agent is null!");
            }
            //ms run
            if (t.getMsRun() != null) {
                serializeList(t.getMsRun(), jg, sp, Comparator.comparing(
                    MsRun::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "MS runs are null!");
            }
            //assay
            if (t.getAssay() != null) {
                serializeList(t.getAssay(), jg, sp, Comparator.comparing(
                    Assay::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "Assays are null!");
            }
            //study variable
            if (t.getStudyVariable() != null) {
                serializeList(
                    t.getStudyVariable(), jg, sp, Comparator.comparing(
                    StudyVariable::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "Study Variable is null!");
            }
            //custom
            if (t.getCustom() != null) {
                serializeListWithMetadataElement(t.getCustom(),
                    MetadataElement.CUSTOM, jg, sp, Optional.empty());
            } else {
                
                    log.debug( "Custom is null!");
            }
            //cv
            if (t.getCv() != null) {
                serializeList(t.getCv(), jg, sp, Comparator.comparing(
                    CV::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "CVs are null!");
            }
            //small molecule quantification unit
            if (t.getSmallMoleculeQuantificationUnit() != null) {
                addLineWithMetadataProperty(jg, prefix,
                    MetadataProperty.SMALL_MOLECULE_QUANTIFICATION_UNIT,
                    MetadataElement.SMALL_MOLECULE, t.
                        getSmallMoleculeQuantificationUnit());
            } else {
                
                    log.debug(
                        "Small molecule quantification unit is null!");
            }
            //small molecule feature quantification unit
            if (t.getSmallMoleculeFeatureQuantificationUnit() != null) {
                addLineWithMetadataProperty(jg, prefix,
                    MetadataProperty.SMALL_MOLECULE_FEATURE_QUANTIFICATION_UNIT,
                    MetadataElement.SMALL_MOLECULE_FEATURE, t.
                        getSmallMoleculeFeatureQuantificationUnit());
            } else {
                
                    log.debug(
                        "Small molecule feature quantification unit is null!");
            }
            //small molecule identification reliability
            if (t.getSmallMoleculeIdentificationReliability() != null) {
                addLineWithMetadataProperty(jg, prefix,
                    MetadataProperty.SMALL_MOLECULE_IDENTIFICATION_RELIABILITY,
                    MetadataElement.SMALL_MOLECULE, t.
                        getSmallMoleculeIdentificationReliability());
            } else {
                
                    log.debug(
                        "Small molecule identification reliability is null!");
            }
            //database
            if (t.getDatabase() != null) {
                serializeList(t.getDatabase(), jg, sp, Comparator.comparing(
                    Database::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ));
            } else {
                
                    log.debug( "Databases are null!");
            }
            if (t.getIdConfidenceMeasure() != null) {
                serializeListWithMetadataElement(t.getIdConfidenceMeasure(),
                    MetadataElement.ID_CONFIDENCE_MEASURE, jg, sp, Optional.empty());
            } else {
                
                    log.debug( "Id confidence measure is null!");
            }
            if (t.getColunitSmallMolecule() != null) {
                t.getColunitSmallMolecule().
                    forEach((colUnit) ->
                    {
                        addLine(jg, prefix,
                            MetadataElement.COLUNIT_SMALL_MOLECULE, colUnit.
                                getColumnName() + "=" + new ParameterConverter().
                                convert(colUnit.getParam()));
                    });
            } else {
                
                    log.debug( "Colunit small molecule is null!");
            }
            if (t.getColunitSmallMoleculeFeature() != null) {
                t.getColunitSmallMoleculeFeature().
                    forEach((colUnit) ->
                    {
                        addLine(jg, prefix,
                            MetadataElement.COLUNIT_SMALL_MOLECULE_FEATURE,
                            colUnit.
                                getColumnName() + "=" + new ParameterConverter().
                                convert(colUnit.getParam()));
                    });
            } else {
                
                    log.debug( "Colunit small molecule feature is null!");
            }
            if (t.getColunitSmallMoleculeEvidence() != null) {
                t.getColunitSmallMoleculeEvidence().
                    forEach((colUnit) ->
                    {
                        addLine(jg, prefix,
                            MetadataElement.COLUNIT_SMALL_MOLECULE_EVIDENCE,
                            colUnit.
                                getColumnName() + "=" + new ParameterConverter().
                                convert(colUnit.getParam()));
                    });
            } else {
                
                    log.debug( "Colunit small molecule evidence is null!");
            }
        }
    }

}
