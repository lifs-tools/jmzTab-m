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
package uk.ac.ebi.pride.jmztab2.utils.parser;

import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.ColumnParameterMapping;
import org.lifstools.mztab2.model.Contact;
import org.lifstools.mztab2.model.Database;
import org.lifstools.mztab2.model.Instrument;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.Publication;
import org.lifstools.mztab2.model.PublicationItem;
import org.lifstools.mztab2.model.Sample;
import org.lifstools.mztab2.model.SampleProcessing;
import org.lifstools.mztab2.model.Software;
import org.lifstools.mztab2.model.StudyVariable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.Data;
import static uk.ac.ebi.pride.jmztab2.model.MZTabStringUtils.isEmpty;

/**
 * <p>MZTabParserContext is used to keep track of indexed elements and interrelations during parsing.</p>
 *
 * @author nilshoffmann
 * @since 11/09/17
 * 
 */
@Data
public class MZTabParserContext {
    private SortedMap<Integer, SampleProcessing> sampleProcessingMap = new TreeMap<>(); 
    private SortedMap<Integer, Instrument> instrumentMap = new TreeMap<>(); 
    private SortedMap<Integer, Software> softwareMap = new TreeMap<>(); 

    private SortedMap<Integer, Publication> publicationMap = new TreeMap<>(); 
    private SortedMap<Integer, Contact> contactMap = new TreeMap<>();
    private Parameter quantificationMethod; 
    
    private SortedMap<Integer, Assay> assayMap = new TreeMap<>(); 
    private Parameter smallMoleculeQuantificationUnit;
    private SortedMap<Integer, MsRun> msRunMap = new TreeMap<>();
    private SortedMap<Integer, Parameter> customItemMap = new TreeMap<>(); 
    private SortedMap<Integer, Parameter> derivatizationItemMap = new TreeMap<>();
    private SortedMap<Integer, Parameter> idConfidenceMeasureMap = new TreeMap<>(); 
    
    private SortedMap<Integer, Sample> sampleMap = new TreeMap<>();
    private SortedMap<Integer, StudyVariable> studyVariableMap = new TreeMap<>();
    private SortedMap<Integer, CV> cvMap = new TreeMap<>();
    private SortedMap<Integer, Database> databaseMap = new TreeMap<>();
    private List<ColumnParameterMapping> smallMoleculeColUnitList = new ArrayList<>();
    private List<ColumnParameterMapping> smallMoleculeFeatureColUnitList = new ArrayList<>();
    private List<ColumnParameterMapping> smallMoleculeEvidenceColUnitList = new ArrayList<>();
    private Map<String, String> colUnitMap = new HashMap<>();
    
    /**
     * Add a sample to metadata. Samples are NOT MANDATORY in mzTab, since many software packages cannot determine what
     * type of sample was analysed (e.g. whether biological or technical replication was performed).
     *
     * @param sample SHOULD NOT be null.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Sample} object.
     */
    public Sample addSample(Metadata metadata, Sample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Sample should not be null");
        }

        this.sampleMap.put(sample.getId(), sample);
        metadata.addSampleItem(sample);
        return sample;
    }

    /**
     * Add a sample[id]-species into sample.
     *
     * @param id SHOULD be positive integer.
     * @param species if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Sample} object.
     */
    public Sample addSampleSpecies(Metadata metadata, Integer id, Parameter species) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be greater than 0!");
        }
        Sample sample = sampleMap.get(id);
        if (species == null) {
            return sample;
        }

        if (sample == null) {
            sample = new Sample();
            sample.id(id);
            sample.addSpeciesItem(species);
            sampleMap.put(id, sample);
            metadata.addSampleItem(sample);
        } else {
            sample.addSpeciesItem(species);
        }
        return sample;
    }

    /**
     * Add a sample[id]-tissue into sample.
     *
     * @param id SHOULD be positive integer.
     * @param tissue if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Sample} object.
     */
    public Sample addSampleTissue(Metadata metadata, Integer id, Parameter tissue) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be greater than 0!");
        }
        Sample sample = sampleMap.get(id);
        if (tissue == null) {
            return sample;
        }

        if (sample == null) {
            sample = new Sample();
            sample.id(id);
            sample.addTissueItem(tissue);
            sampleMap.put(id, sample);
            metadata.addSampleItem(sample);
        } else {
            sample.addTissueItem(tissue);
        }
        return sample;
    }

    /**
     * Add a sample[id]-cell_type into sample.
     *
     * @param id SHOULD be positive integer.
     * @param cellType if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Sample} object.
     */
    public Sample addSampleCellType(Metadata metadata, Integer id, Parameter cellType) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be greater than 0!");
        }
        Sample sample = sampleMap.get(id);
        if (cellType == null) {
            return sample;
        }

        if (sample == null) {
            sample = new Sample();
            sample.id(id);
            sample.addCellTypeItem(cellType);
            sampleMap.put(id, sample);
            metadata.addSampleItem(sample);
        } else {
            sample.addCellTypeItem(cellType);
        }
        return sample;
    }

    /**
     * Add a sample[id]-disease into sample.
     *
     * @param id SHOULD be positive integer.
     * @param disease if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Sample} object.
     */
    public Sample addSampleDisease(Metadata metadata, Integer id, Parameter disease) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be greater than 0!");
        }
        Sample sample = sampleMap.get(id);
        if (disease == null) {
            return sample;
        }

        if (sample == null) {
            sample = new Sample();
            sample.id(id);
            sample.addDiseaseItem(disease);
            sampleMap.put(id, sample);
            metadata.addSampleItem(sample);
        } else {
            sample.addDiseaseItem(disease);
        }
        return sample;
    }

    /**
     * Add a sample[id]-description into sample.
     *
     * @param id SHOULD be positive integer.
     * @param description if empty ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Sample} object.
     */
    public Sample addSampleDescription(Metadata metadata, Integer id, String description) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be greater than 0!");
        }

        Sample sample = sampleMap.get(id);
        if (isEmpty(description)) {
            return sample;
        }

        if (sample == null) {
            sample = new Sample();
            sample.id(id);
            sample.setDescription(description);
            sampleMap.put(id, sample);
            metadata.addSampleItem(sample);
        } else {
            sample.setDescription(description);
        }
        return sample;
    }

    /**
     * Add a sample[id]-custom into sample. Add a custom parameter for sample.
     *
     * @param id SHOULD be positive integer.
     * @param custom if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Sample} object.
     */
    public Sample addSampleCustom(Metadata metadata, Integer id, Parameter custom) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be greater than 0!");
        }
        Sample sample = sampleMap.get(id);
        if (custom == null) {
            return sample;
        }

        if (sample == null) {
            sample = new Sample();
            sample.id(id);
            sample.addCustomItem(custom);
            sampleMap.put(id, sample);
            metadata.addSampleItem(sample);
        } else {
            sample.addCustomItem(custom);
        }
        return sample;
    }

    /**
     * Add a sample_processing[id]. A list of parameters describing a sample processing step.
     * The order of the data_processing items should reflect the order these processing steps
     * were performed in. If multiple parameters are given for a step these MUST be separated by a "|".
     *
     * @param id SHOULD be positive integer.
     * @param sampleProcessing if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.SampleProcessing} object.
     */
    public SampleProcessing addSampleProcessing(Metadata metadata, Integer id, List<Parameter> sampleProcessing) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be greater than 0!");
        }
        if (sampleProcessing == null) {
            return null;
        }

//        sampleProcessing.setSplitChar(BAR);
        SampleProcessing sp = new SampleProcessing();
        sp.id(id);
        sp.sampleProcessing(sampleProcessing);
        metadata.addSampleProcessingItem(sp);
        sampleProcessingMap.put(id, sp);
        return sp;
    }

    /**
     * Add a processing parameter to sample_processing[id]
     *
     * @param id SHOULD be positive integer.
     * @param param if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.SampleProcessing} object.
     */
    public SampleProcessing addSampleProcessingParameter(Metadata metadata, Integer id, Parameter param) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample processing id should be greater than 0!");
        }
        SampleProcessing sampleProcessing = sampleProcessingMap.get(id);
        if (param == null) {
            return sampleProcessing;
        }

        if (sampleProcessing == null) {
            sampleProcessing = new SampleProcessing();
            sampleProcessing.id(id);
            sampleProcessing.addSampleProcessingItem(param);
            sampleProcessingMap.put(id, sampleProcessing);
            metadata.addSampleProcessingItem(sampleProcessing);
        } else {
            sampleProcessing.addSampleProcessingItem(param);
        }
        return sampleProcessing;
    }

    /**
     * Add a instrument[id] to metadata.
     *
     * @param instrument SHOULD NOT be null.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Instrument} object.
     */
    public Instrument addInstrument(Metadata metadata, Instrument instrument) {
        if (instrument == null) {
            throw new IllegalArgumentException("Instrument should not be null");
        }
        instrumentMap.put(instrument.getId(), instrument);
        metadata.addInstrumentItem(instrument);
        return instrument;
    }

    /**
     * Add a parameter for instrument[id]-name
     *
     * @param id SHOULD be positive integer.
     * @param name if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Instrument} object.
     */
    public Instrument addInstrumentName(Metadata metadata, Integer id, Parameter name) {
        if (id <= 0) {
            throw new IllegalArgumentException("Instrument id should be greater than 0!");
        }
        Instrument instrument = instrumentMap.get(id);
        if (name == null) {
            return instrument;
        }

        if (instrument == null) {
            instrument = new Instrument();
            instrument.id(id);
            instrument.name(name);
            instrumentMap.put(id, instrument);
            metadata.addInstrumentItem(instrument);
        } else {
            instrument.name(name);
        }
        return instrument;
    }

    /**
     * Add a parameter for instrument[id]-source
     *
     * @param id SHOULD be positive integer.
     * @param source if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Instrument} object.
     */
    public Instrument addInstrumentSource(Metadata metadata, Integer id, Parameter source) {
        if (id <= 0) {
            throw new IllegalArgumentException("Instrument id should be greater than 0!");
        }
        Instrument instrument = instrumentMap.get(id);
        if (source == null) {
            return instrument;
        }

        if (instrument == null) {
            instrument = new Instrument();
            instrument.id(id);
            instrument.setSource(source);
            instrumentMap.put(id, instrument);
            metadata.addInstrumentItem(instrument);
        } else {
            instrument.setSource(source);
        }
        return instrument;
    }

    /**
     * Add a parameter for instrument[id]-analyzer[i]
     *
     * @param id SHOULD be positive integer.
     * @param analyzer if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Instrument} object.
     */
    public Instrument addInstrumentAnalyzer(Metadata metadata, Integer id, Parameter analyzer) {
        if (id <= 0) {
            throw new IllegalArgumentException("Instrument id should be greater than 0!");
        }
        Instrument instrument = instrumentMap.get(id);
        if (analyzer == null) {
            return instrument;
        }

        if (instrument == null) {
            instrument = new Instrument();
            instrument.id(id);
            instrument.addAnalyzerItem(analyzer);
            instrumentMap.put(id, instrument);
            metadata.addInstrumentItem(instrument);
        } else {
            instrument.addAnalyzerItem(analyzer);
        }
        return instrument;
    }

    /**
     * Add a parameter for instrument[id]-detector
     *
     * @param id SHOULD be positive integer.
     * @param detector if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Instrument} object.
     */
    public Instrument addInstrumentDetector(Metadata metadata, Integer id, Parameter detector) {
        if (id <= 0) {
            throw new IllegalArgumentException("Instrument id should be greater than 0!");
        }

        Instrument instrument = instrumentMap.get(id);
        if (detector == null) {
            return instrument;
        }

        if (instrument == null) {
            instrument = new Instrument();
            instrument.id(id);
            instrument.setDetector(detector);
            instrumentMap.put(id, instrument);
            metadata.addInstrumentItem(instrument);
        } else {
            instrument.setDetector(detector);
        }
        return instrument;
    }

    /**
     * Add a software to metadata, which used to analyze the data and obtain the reported results.
     *
     * @param software SHOULD NOT be null
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Software} object.
     */
    public Software addSoftware(Metadata metadata, Software software) {
        if (software == null) {
            throw new IllegalArgumentException("Software should not be null");
        }

        softwareMap.put(software.getId(), software);
        metadata.addSoftwareItem(software);
        return software;
    }

    /**
     * Add a software[id] parameter. The parameter's value SHOULD contain the software's version.
     * The order (numbering) should reflect the order in which the tools were used.
     *
     * @param id SHOULD be positive integer.
     * @param param if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Software} object.
     */
    public Software addSoftwareParameter(Metadata metadata, Integer id, Parameter param) {
        if (id <= 0) {
            throw new IllegalArgumentException("Software id should be greater than 0!");
        }
        Software software = softwareMap.get(id);
        if (param == null) {
            return software;
        }

        if (software == null) {
            software = new Software();
            software.id(id);
            software.setParameter(param);
            softwareMap.put(id, software);
            metadata.addSoftwareItem(software);
        } else {
            software.setParameter(param);
        }
        return software;
    }

    /**
     * Add a software[id]-setting. This field MAY occur multiple times for a single software.
     * The value of this field is deliberately set as a String, since there currently do not
     * exist cvParameters for every possible setting.
     *
     * @param id SHOULD be positive integer.
     * @param setting if empty ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Software} object.
     */
    public Software addSoftwareSetting(Metadata metadata, Integer id, String setting) {
        if (id <= 0) {
            throw new IllegalArgumentException("Software id should be greater than 0!");
        }
        Software software = softwareMap.get(id);
        if (isEmpty(setting)) {
            return software;
        }

        if (software == null) {
            software = new Software();
            software.id(id);
            software.addSettingItem(setting);
            softwareMap.put(id, software);
            metadata.addSoftwareItem(software);
        } else  {
            software.addSettingItem(setting);
        }
        return software;
    }

    /**
     * Add a publiction to metadata. A publication associated with this file. Several publications can be given by
     * indicating the number in the square brackets after "publication". PubMed ids must be prefixed by "pubmed:",
     * DOIs by "doi:". Multiple identifiers MUST be separated by "|".
     *
     * @param publication SHOULD NOT be null.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Publication} object.
     */
    public Publication addPublication(Metadata metadata, Publication publication) {
        if (publication == null) {
            throw new IllegalArgumentException("Publication should not be null");
        }
        publicationMap.put(publication.getId(), publication);
        metadata.addPublicationItem(publication);
        return publication;
    }

    /**
     * Add a publication item to metadata. PubMed ids must be prefixed by "pubmed:", DOIs by "doi:".
     * Multiple identifiers MUST be separated by "|".
     *
     * @param id SHOULD be positive integer.
     * @param type SHOULD NOT be null.
     * @param accession SHOULD NOT set empty.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Publication} object.
     */
    public Publication addPublicationItem(Metadata metadata, Integer id, PublicationItem.TypeEnum type, String accession) {
        if (id <= 0) {
            throw new IllegalArgumentException("Publication id should be greater than 0!");
        }
        if (type == null) {
            throw new NullPointerException("Publication type should not be null");
        }
        if (isEmpty(accession)) {
            throw new IllegalArgumentException("Publication accession should not be empty.");
        }

        Publication publication = publicationMap.get(id);
        if (publication == null) {
            publication = new Publication();
            publication.id(id);
            publication.addPublicationItemsItem(new PublicationItem().type(type).accession(accession));
            publicationMap.put(id, publication);
            metadata.addPublicationItem(publication);
        } else {
            publication.addPublicationItemsItem(new PublicationItem().type(type).accession(accession));
        }
        return publication;
    }

    /**
     * Add a couple of publication items into publication[id]. Several publications can be given by
     * indicating the number in the square brackets after "publication". PubMed ids must be prefixed by "pubmed:",
     * DOIs by "doi:". Multiple identifiers MUST be separated by "|".
     *
     * @param id SHOULD be positive integer.
     * @param items SHOULD NOT be null.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Publication} object.
     */
    public Publication addPublicationItems(Metadata metadata, Integer id, Collection<PublicationItem> items) {
        if (id <= 0) {
            throw new IllegalArgumentException("Publication id should be greater than 0!");
        }
        if (items == null) {
            throw new NullPointerException("Publication items should not be null");
        }

        Publication publication = publicationMap.get(id);
        if (publication == null) {
            publication = new Publication();
            publication.id(id);
            publication.setPublicationItems(new ArrayList<>(items));
            publicationMap.put(id, publication);
            metadata.addPublicationItem(publication);
        } else {
            publication.setPublicationItems(new ArrayList<>(items));
        }
        return publication;
    }

    /**
     * Add a contact into metadata.
     *
     * @param contact SHOULD NOT be null.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Contact} object.
     */
    public Contact addContact(Metadata metadata, Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact should not be null");
        }

        contactMap.put(contact.getId(), contact);
        metadata.addContactItem(contact);
        return contact;
    }

    /**
     * Add contact[id]-name. Several contacts can be given by indicating the number in the square brackets
     * after "contact". A contact has to be supplied in the format [first name] [initials] [last name] (see example).
     *
     * @param id SHOULD be positive integer.
     * @param name SHOULD NOT set empty.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Contact} object.
     */
    public Contact addContactName(Metadata metadata, Integer id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException("Contact id should be greater than 0!");
        }
        if (isEmpty(name)) {
            throw new IllegalArgumentException("Contact name should not be empty.");
        }

        Contact contact = contactMap.get(id);
        if (contact == null) {
            contact = new Contact();
            contact.id(id);
            contact.setName(name);
            contactMap.put(id, contact);
            metadata.addContactItem(contact);
        } else {
            contact.setName(name);
        }
        return contact;
    }

    /**
     * Add contact[id]-affiliation.
     *
     * @param id SHOULD be positive integer.
     * @param affiliation SHOULD NOT set empty.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Contact} object.
     */
    public Contact addContactAffiliation(Metadata metadata, Integer id, String affiliation) {
        if (id <= 0) {
            throw new IllegalArgumentException("Contact id should be greater than 0!");
        }
        if (isEmpty(affiliation)) {
            throw new IllegalArgumentException("Contact affiliation should not be empty.");
        }

        Contact contact = contactMap.get(id);
        if (contact == null) {
            contact = new Contact();
            contact.id(id);
            contact.setAffiliation(affiliation);
            contactMap.put(id, contact);
            metadata.addContactItem(contact);
        } else {
            contact.setAffiliation(affiliation);
        }
        return contact;
    }

    /**
     * Add contact[id]-email
     *
     * @param id SHOULD be positive integer.
     * @param email SHOULD NOT set empty.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Contact} object.
     */
    public Contact addContactEmail(Metadata metadata, Integer id, String email) {
        if (id <= 0) {
            throw new IllegalArgumentException("Contact id should be greater than 0!");
        }
        if (isEmpty(email)) {
            throw new IllegalArgumentException("Contact email should not be empty.");
        }

        Contact contact = contactMap.get(id);
        if (contact == null) {
            contact = new Contact();
            contact.id(id);
            contact.setEmail(email);
            contactMap.put(id, contact);
            metadata.addContactItem(contact);
        } else {
            contact.setEmail(email);
        }
        return contact;
    }
    
        /**
     * Add contact[id]-email
     *
     * @param id SHOULD be positive integer.
     * @param orcid SHOULD NOT set empty.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Contact} object.
     */
    public Contact addContactOrcid(Metadata metadata, Integer id, String orcid) {
        if (id <= 0) {
            throw new IllegalArgumentException("Contact id should be greater than 0!");
        }

        Contact contact = contactMap.get(id);
        if (contact == null) {
            contact = new Contact();
            contact.id(id);
            contact.setOrcid(orcid);
            contactMap.put(id, contact);
            metadata.addContactItem(contact);
        } else {
            contact.setOrcid(orcid);
        }
        return contact;
    }

    /**
     * Add a ms_run[id] into metadata. An MS run is effectively one run (or set of runs on pre-fractionated samples)
     * on an MS instrument, and is referenced from assay in different contexts.
     *
     * @param msRun SHOULD NOT be null.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.MsRun} object.
     */
    public MsRun addMsRun(Metadata metadata, MsRun msRun) {
        if (msRun == null) {
            throw new IllegalArgumentException("MsRun should not be null");
        }

        msRunMap.put(msRun.getId(), msRun);
        metadata.addMsRunItem(msRun);
        return msRun;
    }

    /**
     * Add ms_run[id]-format into metadata. A parameter specifying the data format of the external MS data file.
     *
     * @param id SHOULD be positive integer.
     * @param format if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.MsRun} object.
     */
    public MsRun addMsRunFormat(Metadata metadata, Integer id, Parameter format) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be greater than 0!");
        }
        MsRun msRun = msRunMap.get(id);
        if (format == null) {
            return msRun;
        }

        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.setFormat(format);
            msRunMap.put(id, msRun);
            metadata.addMsRunItem(msRun);
        } else {
            msRun.setFormat(format);
        }
        return msRun;
    }

    /**
     * Add ms_run[id]-location into metadata. Location of the external data file. If the actual location
     * of the MS run is unknown, a "null" MUST be used as a place holder value.
     *
     * @param id SHOULD be positive integer.
     * @param location if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.MsRun} object.
     */
    public MsRun addMsRunLocation(Metadata metadata, Integer id, URI location) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be greater than 0!");
        }

        MsRun msRun = msRunMap.get(id);
        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.setLocation(location==null?null:location);
            msRunMap.put(id, msRun);
            metadata.addMsRunItem(msRun);
        } else {
            msRun.setLocation(location);
        }
        return msRun;
    }
    
    /**
     * Add ms_run[id]-instrument_ref into metadata. Reference to a commonly used instrument.
     * 
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id SHOULD be positive integer.
     * @param instrument if null ignore operation.
     * @return a {@link org.lifstools.mztab2.model.MsRun} object.
     */
    public MsRun addMsRunInstrumentRef(Metadata metadata, Integer id, Instrument instrument) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be greater than 0!");
        }
        
        if (instrument==null) {
            throw new IllegalArgumentException("instrument must not be null!");
        }

        MsRun msRun = msRunMap.get(id);
        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.setInstrumentRef(instrument);
            msRunMap.put(id, msRun);
            metadata.addMsRunItem(msRun);
        } else {
            msRun.setInstrumentRef(instrument);
        }
        return msRun;
    }

    /**
     * Add ms_run[id]-id_format into metadata. Parameter specifying the id format used in the external data file.
     *
     * @param id SHOULD be positive integer.
     * @param idFormat if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.MsRun} object.
     */
    public MsRun addMsRunIdFormat(Metadata metadata, Integer id, Parameter idFormat) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be greater than 0!");
        }
        MsRun msRun = msRunMap.get(id);
        if (idFormat == null) {
            return msRun;
        }

        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.setIdFormat(idFormat);
            msRunMap.put(id, msRun);
            metadata.addMsRunItem(msRun);
        } else {
            msRun.setIdFormat(idFormat);
        }
        return msRun;
    }

    /**
     * Add ms_run[id]-fragmentation_method into metadata. A list of "|" separated parameters describing
     * all the types of fragmentation used in a given ms run.
     *
     * @param id SHOULD be positive integer.
     * @param fragmentationMethod if null ignore operation.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.MsRun} object.
     */
    public MsRun addMsRunFragmentationMethod(Metadata metadata, Integer id, Parameter fragmentationMethod) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be greater than 0!");
        }
        MsRun msRun = msRunMap.get(id);
        if (fragmentationMethod == null) {
            return msRun;
        }

        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.addFragmentationMethodItem(fragmentationMethod);
            msRunMap.put(id, msRun);
            metadata.addMsRunItem(msRun);
        } else {
            msRun.addFragmentationMethodItem(fragmentationMethod);
        }
        return msRun;
    }

    /**
     * <p>addMsRunHash.</p>
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id a {@link java.lang.Integer} object.
     * @param hash a {@link java.lang.String} object.
     * @return a {@link org.lifstools.mztab2.model.MsRun} object.
     */
    public MsRun addMsRunHash(Metadata metadata, Integer id, String hash) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be greater than 0!");
        }
        if (isEmpty(hash)) {
            throw new IllegalArgumentException("ms_run hash should not be empty.");
        }

        MsRun msRun = msRunMap.get(id);
        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.setHash(hash);
            msRunMap.put(id, msRun);
            metadata.addMsRunItem(msRun);
        } else {
            msRun.setHash(hash);
        }
        return msRun;
    }

    /**
     * <p>addMsRunHashMethod.</p>
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id a {@link java.lang.Integer} object.
     * @param hashMethod a {@link org.lifstools.mztab2.model.Parameter} object.
     * @return a {@link org.lifstools.mztab2.model.MsRun} object.
     */
    public MsRun addMsRunHashMethod(Metadata metadata, Integer id, Parameter hashMethod) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be greater than 0!");
        }
        MsRun msRun = msRunMap.get(id);
        if (hashMethod == null) {
            return msRun;
        }

        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.setHashMethod(hashMethod);
            msRunMap.put(id, msRun);
            metadata.addMsRunItem(msRun);
        } else {
            msRun.setHashMethod(hashMethod);
        }
        return msRun;
    }
    
    /**
     * <p>addMsRunScanPolarity.</p>
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id a {@link java.lang.Integer} object.
     * @param scanPolarity a {@link org.lifstools.mztab2.model.Parameter} object.
     * @return a {@link org.lifstools.mztab2.model.MsRun} object.
     */
    public MsRun addMsRunScanPolarity(Metadata metadata, Integer id, Parameter scanPolarity) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be greater than 0!");
        }
        MsRun msRun = msRunMap.get(id);
        if (scanPolarity == null) {
            return msRun;
        }

        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.addScanPolarityItem(scanPolarity);
            msRunMap.put(id, msRun);
            metadata.addMsRunItem(msRun);
        } else {
            msRun.addScanPolarityItem(scanPolarity);
        }
        return msRun;
    }

    /**
     * Add a assay into metadata. The application of a measurement about the sample (in this case through MS) -
     * producing values about small molecules, peptides or proteins. One assay is typically mapped to one MS run
     * in the case of label-free MS analysis or multiple assays are mapped to one MS run for multiplexed techniques,
     * along with a description of the label or tag applied.
     *
     * @param assay SHOULD NOT be null.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Assay} object.
     */
    public Assay addAssay(Metadata metadata, Assay assay) {
        if (assay == null) {
            throw new IllegalArgumentException("Assay should not be null");
        }

        assayMap.put(assay.getId(), assay);
        metadata.addAssayItem(assay);
        return assay;
    }
    
    /**
     * Add a assay[id]-custom[i] into metadata. The application of a measurement about the sample (in this case through MS) -
     * producing values about small molecules, peptides or proteins. One assay is typically mapped to one MS run
     * in the case of label-free MS analysis or multiple assays are mapped to one MS run for multiplexed techniques,
     * along with a description of the label or tag applied.
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id SHOULD NOT be null.
     * @param param the parameter.
     * @return a {@link org.lifstools.mztab2.model.Assay} object.
     */
    public Assay addAssayCustom(Metadata metadata, Integer id, Parameter param) {
        if (id <= 0) {
            throw new IllegalArgumentException("assay id should be greater than 0!");
        }
        
        Assay assay = assayMap.get(id);
        if (assay == null) {
            assay = new Assay();
            assay.id(id);
            assay.addCustomItem(param);
            assayMap.put(id, assay);
            metadata.addAssayItem(assay);
        } else {
            assay.addCustomItem(param);
        }
        return assay;
    }
    
        /**
     * Add a assay[id]-external_uri into metadata. The application of a measurement about the sample (in this case through MS) -
     * producing values about small molecules, peptides or proteins. One assay is typically mapped to one MS run
     * in the case of label-free MS analysis or multiple assays are mapped to one MS run for multiplexed techniques,
     * along with a description of the label or tag applied.
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id the id of the assay element.
     * @param location SHOULD NOT be null.
     * @return a {@link org.lifstools.mztab2.model.Assay} object.
     */
    public Assay addAssayExternalUri(Metadata metadata, Integer id, URI location) {
        if (id <= 0) {
            throw new IllegalArgumentException("assay id should be greater than 0!");
        }

        Assay assay = assayMap.get(id);
        if (assay == null) {
            assay = new Assay();
            assay.id(id);
            assay.setExternalUri(location==null?null:location);
            assayMap.put(id, assay);
            metadata.addAssayItem(assay);
        } else {
            assay.setExternalUri(location);
        }
        return assay;
    }

    /**
     * Add assay[id]-sample_ref into metadata. An association from a given assay to the sample analysed.
     *
     * @param id SHOULD be positive integer.
     * @param sample SHOULD NOT be null, and SHOULD be defined in metadata first.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Assay} object.
     */
    public Assay addAssaySample(Metadata metadata, Integer id, Sample sample) {
        if (id <= 0) {
            throw new IllegalArgumentException("assay id should be greater than 0!");
        }
        if (sample == null) {
            throw new NullPointerException("assay sample_ref should not be null.");
        }
        if (! sampleMap.containsValue(sample)) {
            throw new IllegalArgumentException("Sample not defined in metadata.");
        }

        Assay assay = assayMap.get(id);
        if (assay == null) {
            assay = new Assay();
            assay.id(id);
            assay.setSampleRef(sample);
            assayMap.put(id, assay);
            metadata.addAssayItem(assay);
        } else {
            assay.setSampleRef(sample);
        }
        return assay;
    }

    /**
     * Add assay[id]-ms_run_ref into metadata. An association from a given assay to the source MS run.
     *
     * @param id SHOULD be positive integer.
     * @param msRun SHOULD NOT be null, and SHOULD be defined in metadata first.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.Assay} object.
     */
    public Assay addAssayMsRun(Metadata metadata, Integer id, MsRun msRun) {
        if (id <= 0) {
            throw new IllegalArgumentException("assay id should be greater than 0!");
        }
        if (msRun == null) {
            throw new NullPointerException("assay ms_run_ref should not be null.");
        }
        if (! msRunMap.containsValue(msRun)) {
            throw new IllegalArgumentException("ms_run should be defined in metadata first.");
        }

        Assay assay = assayMap.get(id);
        if (assay == null) {
            assay = new Assay();
            assay.id(id);
            assay.addMsRunRefItem(msRun);
            assayMap.put(id, assay);
            metadata.addAssayItem(assay);
        } else {
            assay.addMsRunRefItem(msRun);
        }
        return assay;
    }

    /**
     * Add a study variable into metadata. The variables about which the final results of a study are reported, which
     * may have been derived following averaging across a group of replicate measurements (assays). In files where assays
     * are reported, study variables have references to assays. The same concept has been defined by others as "experimental factor".
     *
     * @param studyVariable SHOULD NOT be null.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.StudyVariable} object.
     */
    public StudyVariable addStudyVariable(Metadata metadata, StudyVariable studyVariable) {
        if (studyVariable == null) {
            throw new IllegalArgumentException("StudyVariable should not be null");
        }
        studyVariableMap.put(studyVariable.getId(), studyVariable);
        metadata.addStudyVariableItem(studyVariable);
        return studyVariable;
    }

    /**
     * Add a study_variable[id]-assay_refs. Comma-separated references to the IDs of assays grouped in the study variable.
     *
     * @param id SHOULD be positive integer.
     * @param assay SHOULD NOT be null, and should be defined in metadata first.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.StudyVariable} object.
     */
    public StudyVariable addStudyVariableAssay(Metadata metadata, Integer id, Assay assay) {
        if (id <= 0) {
            throw new IllegalArgumentException("study variable id should be greater than 0!");
        }
        if (assay == null) {
            throw new NullPointerException("study_variable[n]-assay_ref should not be null.");
        }
        if (! assayMap.containsValue(assay)) {
            throw new IllegalArgumentException("assay should be defined in metadata first");
        }

        StudyVariable studyVariable = studyVariableMap.get(id);
        if (studyVariable == null) {
            studyVariable = new StudyVariable();
            studyVariable.id(id);
            studyVariable.addAssayRefsItem(assay);
            studyVariableMap.put(id, studyVariable);
            metadata.addStudyVariableItem(studyVariable);
        } else {
            studyVariable.addAssayRefsItem(assay);
        }
        return studyVariable;
    }

    /**
     * Add a study_variable[id]-description. A textual description of the study variable.
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id SHOULD be positive integer.
     * @param description if empty ignore operation.
     * @return a {@link org.lifstools.mztab2.model.StudyVariable} object.
     */
    public StudyVariable addStudyVariableDescription(Metadata metadata, Integer id, String description) {
        if (id <= 0) {
            throw new IllegalArgumentException("study variable id should be greater than 0!");
        }
        StudyVariable studyVariable = studyVariableMap.get(id);
        if (isEmpty(description)) {
            return studyVariable;
        }

        if (studyVariable == null) {
            studyVariable = new StudyVariable();
            studyVariable.id(id);
            metadata.addStudyVariableItem(studyVariable);
        }

        studyVariable.setDescription(description);
        studyVariableMap.put(id, studyVariable);
        return studyVariable;
    }
    
    /**
     * Add a study_variable[id]-factor. A Parameter further refining what is known about the study design.
     * 
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id id SHOULD be positive integer.
     * @param checkParameter the study variable factor Parameter to add.
     * @return a {@link org.lifstools.mztab2.model.StudyVariable} object.
     */
    public StudyVariable addStudyVariableFactors(Metadata metadata, Integer id, Parameter checkParameter) {
        if (id <= 0) {
            throw new IllegalArgumentException("study variable id should be greater than 0!");
        }
        StudyVariable studyVariable = studyVariableMap.get(id);
        if (checkParameter == null) {
            return studyVariable;
        }

        if (studyVariable == null) {
            studyVariable = new StudyVariable();
            studyVariable.id(id);
            metadata.addStudyVariableItem(studyVariable);
        }

        studyVariable.addFactorsItem(checkParameter);
        studyVariableMap.put(id, studyVariable);
        return studyVariable;
    }
    
    /**
     * Add a study_variable[id]-variation_function. This is a Parameter detailing how the
     * reported study variable abundances have been calculated.
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id SHOULD be positive integer.
     * @param checkParameter the parameter.
     * @return a {@link org.lifstools.mztab2.model.StudyVariable} object.
     */
    public StudyVariable addStudyVariableVariationFunction(Metadata metadata, Integer id,
        Parameter checkParameter) {
        if (id <= 0) {
            throw new IllegalArgumentException("study variable id should be greater than 0!");
        }
        StudyVariable studyVariable = studyVariableMap.get(id);
        if (checkParameter == null) {
            return studyVariable;
        }
        if (studyVariable == null) {
            studyVariable = new StudyVariable();
            studyVariable.id(id);
            studyVariable.setVariationFunction(checkParameter);
            studyVariableMap.put(id, studyVariable);
            metadata.addStudyVariableItem(studyVariable);
        } else {
            studyVariable.setVariationFunction(checkParameter);
        }
        return studyVariable;
    }
    
    /**
     * Add a study_variable[id]-average_function. This is a Parameter detailing how the
     * reported study variable abundances have been calculated.
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id SHOULD be positive integer.
     * @param checkParameter the parameter.
     * @return a {@link org.lifstools.mztab2.model.StudyVariable} object.
     */
    public StudyVariable addStudyVariableAverageFunction(Metadata metadata, Integer id,
        Parameter checkParameter) {
        if (id <= 0) {
            throw new IllegalArgumentException("study variable id should be greater than 0!");
        }
        StudyVariable studyVariable = studyVariableMap.get(id);
        if (checkParameter == null) {
            return studyVariable;
        }
        if (studyVariable == null) {
            studyVariable = new StudyVariable();
            studyVariable.id(id);
            studyVariable.setAverageFunction(checkParameter);
            studyVariableMap.put(id, studyVariable);
            metadata.addStudyVariableItem(studyVariable);
        } else {
            studyVariable.setAverageFunction(checkParameter);
        }
        return studyVariable;
    }

    /**
     * Add a controlled vocabularies/ontologies into metadata. Define the controlled vocabularies/ontologies
     * used in the mzTab file.
     *
     * @param cv SHOULD NOT be null.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @return a {@link org.lifstools.mztab2.model.CV} object.
     */
    public CV addCV(Metadata metadata, CV cv) {
        if (cv == null) {
            throw new NullPointerException("Controlled vocabularies/ontologies can not set null!");
        }

        cvMap.put(cv.getId(), cv);
        metadata.addCvItem(cv);
        return cv;
    }

    /**
     * Add a cv[id]-label. A string describing the labels of the controlled vocabularies/ontologies used in the mzTab file
     *
     * @param id SHOULD be positive integer.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param label a {@link java.lang.String} object.
     * @return a {@link org.lifstools.mztab2.model.CV} object.
     */
    public CV addCVLabel(Metadata metadata, Integer id, String label) {
        if (id <= 0) {
            throw new IllegalArgumentException("controlled vocabularies id should be greater than 0!");
        }

        CV cv = cvMap.get(id);
        if (cv == null) {
            cv = new CV();
            cv.id(id);
            metadata.addCvItem(cv);
        }

        cv.setLabel(label);
        return cvMap.put(id, cv);
    }

    /**
     * Add a cv[id]-full_name. A string describing the full names of the controlled vocabularies/ontologies used in
     * the mzTab file
     *
     * @param id SHOULD be positive integer.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param fullName a {@link java.lang.String} object.
     * @return a {@link org.lifstools.mztab2.model.CV} object.
     */
    public CV addCVFullName(Metadata metadata, Integer id, String fullName) {
        if (id <= 0) {
            throw new IllegalArgumentException("controlled vocabularies id should be greater than 0!");
        }

        CV cv = cvMap.get(id);
        if (cv == null) {
            cv = new CV();
            cv.id(id);
            metadata.addCvItem(cv);
        }

        cv.setFullName(fullName);
        return cvMap.put(id, cv);
    }

    /**
     * Add a cv[id]-version. A string describing the version of the controlled vocabularies/ontologies used in
     * the mzTab file
     *
     * @param id SHOULD be positive integer.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param version a {@link java.lang.String} object.
     * @return a {@link org.lifstools.mztab2.model.CV} object.
     */
    public CV addCVVersion(Metadata metadata, Integer id, String version) {
        if (id <= 0) {
            throw new IllegalArgumentException("controlled vocabularies id should be greater than 0!");
        }

        CV cv = cvMap.get(id);
        if (cv == null) {
            cv = new CV();
            cv.id(id);
            metadata.addCvItem(cv);
        }

        cv.setVersion(version);
        return cvMap.put(id, cv);
    }

    /**
     * Add a cv[id]-uri. A string containing the URIs of the controlled vocabularies/ontologies used in the
     * mzTab file
     *
     * @param id SHOULD be positive integer.
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param uri a {@link java.lang.String} object.
     * @return a {@link org.lifstools.mztab2.model.CV} object.
     */
    public CV addCVURI(Metadata metadata, Integer id, String uri) {
        if (id <= 0) {
            throw new IllegalArgumentException("controlled vocabularies id should be greater than 0!");
        }

        CV cv = cvMap.get(id);
        if (cv == null) {
            cv = new CV();
            cv.id(id);
            metadata.addCvItem(cv);
        }

        cv.setUri(URI.create(uri));
        return cvMap.put(id, cv);
    }

    /**
     * Defines the unit for the data reported in a column of the small molecule section. Defines the used unit for a column
     * in the small molecule section. The format of the value has to be {column name}={Parameter defining the unit}
     * This field MUST NOT be used to define a unit for quantification columns. The unit used for small molecule quantification
     * values MUST be set in small_molecule-quantification_unit.
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param columnName SHOULD NOT be null
     * @param param SHOULD NOT be null
     */
    public void addSmallMoleculeColUnit(Metadata metadata, String columnName, Parameter param) {
        ColumnParameterMapping cpm = new ColumnParameterMapping();
        cpm.columnName(columnName).param(param);
        this.smallMoleculeColUnitList.add(cpm);
        metadata.addColunitSmallMoleculeItem(cpm);
    }
    
    /**
     * Defines the unit for the data reported in a column of the small molecule section. Defines the used unit for a column
     * in the small molecule section. The format of the value has to be {column name}={Parameter defining the unit}
     * This field MUST NOT be used to define a unit for quantification columns. The unit used for small molecule quantification
     * values MUST be set in small_molecule-quantification_unit.
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param columnName SHOULD NOT be null
     * @param param SHOULD NOT be null
     */
    public void addSmallMoleculeFeatureColUnit(Metadata metadata, String columnName, Parameter param) {
        ColumnParameterMapping cpm = new ColumnParameterMapping();
        cpm.columnName(columnName).param(param);
        this.smallMoleculeFeatureColUnitList.add(cpm);
        metadata.addColunitSmallMoleculeFeatureItem(cpm);
    }
    
    /**
     * Defines the unit for the data reported in a column of the small molecule section. Defines the used unit for a column
     * in the small molecule section. The format of the value has to be {column name}={Parameter defining the unit}
     * This field MUST NOT be used to define a unit for quantification columns. The unit used for small molecule quantification
     * values MUST be set in small_molecule-quantification_unit.
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param columnName SHOULD NOT be null
     * @param param SHOULD NOT be null
     */
    public void addSmallMoleculeEvidenceColUnit(Metadata metadata, String columnName, Parameter param) {
        ColumnParameterMapping cpm = new ColumnParameterMapping();
        cpm.columnName(columnName).param(param);
        this.smallMoleculeEvidenceColUnitList.add(cpm);
        metadata.addColunitSmallMoleculeEvidenceItem(cpm);
    }

    /**
     *  Defines a method to access the colUnit to help in the transformation from columnName String -&gt; to columnName MZTabColumn
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, String> getColUnitMap() {
        return colUnitMap;
    }

    /**
     * Add a confidence measure id parameter.
     * @param id SHOULD NOT be null
     * @param parameter SHOULD NOT be null
     */
    void addIdConfidenceMeasure(Metadata metadata, Integer id, Parameter parameter) {
        this.idConfidenceMeasureMap.put(id, parameter);
        metadata.addIdConfidenceMeasureItem(parameter);
    }

    /**
     * Add a custom item parameter.
     * @param metadata
     * @param id
     * @param custom 
     * @return
     */
    Parameter addCustomItem(Metadata metadata, Integer id, Parameter custom) {
        if (custom == null) {
            return null;
        }
        this.customItemMap.put(id, custom);
        metadata.addCustomItem(custom);
        return custom;
    }
    
    /**
     * Add a derivatization agent parameter.
     * @param metadata
     * @param id
     * @param derivatizationAgent
     * @return 
     */
    Parameter addDerivatizationAgentItem(Metadata metadata, Integer id, Parameter derivatizationAgent) {
        if (derivatizationAgent == null) {
            return null;
        }
        this.derivatizationItemMap.put(id, derivatizationAgent);
        metadata.addDerivatizationAgentItem(derivatizationAgent);
        return derivatizationAgent;
    }

    /**
     * <p>addDatabase.</p>
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param database a {@link org.lifstools.mztab2.model.Database} object.
     * @return a {@link org.lifstools.mztab2.model.Database} object.
     */
    public Database addDatabase(Metadata metadata, Database database) {
        if (database == null) {
            throw new IllegalArgumentException("Database should not be null");
        }
        databaseMap.put(database.getId(), database);
        metadata.addDatabaseItem(database);
        return database;
    }

    /**
     * <p>addDatabasePrefix.</p>
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id a {@link java.lang.Integer} object.
     * @param valueLabel a {@link java.lang.String} object.
     * @return a {@link org.lifstools.mztab2.model.Database} object.
     */
    public Database addDatabasePrefix(Metadata metadata, Integer id, String valueLabel) {
        if (id <= 0) {
            throw new IllegalArgumentException("database id should be greater than 0!");
        }

        Database database = databaseMap.get(id);
        if (database == null) {
            database = new Database();
            database.id(id);
            database.setPrefix(valueLabel);
            databaseMap.put(id, database);
            metadata.addDatabaseItem(database);
        } else {
            database.setPrefix(valueLabel);
        }
        return database;
    }

    /**
     * <p>addDatabaseVersion.</p>
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id a {@link java.lang.Integer} object.
     * @param version a {@link java.lang.String} object.
     * @return a {@link org.lifstools.mztab2.model.Database} object.
     */
    public Database addDatabaseVersion(Metadata metadata, Integer id, String version) {
        if (id <= 0) {
            throw new IllegalArgumentException("database id should be greater than 0!");
        }

        Database database = databaseMap.get(id);
        if (database == null) {
            database = new Database();
            database.id(id);
            database.setVersion(version);
            databaseMap.put(id, database);
            metadata.addDatabaseItem(database);
        } else {
            database.setVersion(version);
        }
        return database;
    }

    /**
     * <p>addDatabaseUri.</p>
     *
     * @param metadata a {@link org.lifstools.mztab2.model.Metadata} object.
     * @param id a {@link java.lang.Integer} object.
     * @param checkURI a {@link java.net.URI} object.
     * @return a {@link org.lifstools.mztab2.model.Database} object.
     */
    public Database addDatabaseUri(Metadata metadata, Integer id, URI checkURI) {
        if (id <= 0) {
            throw new IllegalArgumentException("database id should be greater than 0!");
        }

        Database database = databaseMap.get(id);
        if (database == null) {
            database = new Database();
            database.id(id);
            database.setUri(checkURI==null?null:checkURI);
            databaseMap.put(id, database);
            metadata.addDatabaseItem(database);
        } else {
            database.setUri(checkURI==null?null:checkURI);
        }
        return database;
    }

}
