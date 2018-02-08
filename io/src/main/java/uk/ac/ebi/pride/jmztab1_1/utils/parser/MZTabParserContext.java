/*
 * 
 */
package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.ColumnParameterMapping;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Database;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.PublicationItem;
import de.isas.mztab1_1.model.Sample;
import de.isas.mztab1_1.model.SampleProcessing;
import de.isas.mztab1_1.model.Software;
import de.isas.mztab1_1.model.StudyVariable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.Data;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabColumn;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabUtils.isEmpty;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@Data
public class MZTabParserContext {
    private SortedMap<Integer, SampleProcessing> sampleProcessingMap = new TreeMap<Integer, SampleProcessing>(); //1.1
    private SortedMap<Integer, Instrument> instrumentMap = new TreeMap<Integer, Instrument>(); //1.1
    private SortedMap<Integer, Software> softwareMap = new TreeMap<Integer, Software>(); //1.1
//    private SortedMap<Integer, ProteinSearchEngineScore> proteinSearchEngineScoreMap = new TreeMap<Integer, ProteinSearchEngineScore>(); 
//    private SortedMap<Integer, PeptideSearchEngineScore> peptideSearchEngineScoreMap = new TreeMap<Integer, PeptideSearchEngineScore>();
//    private SortedMap<Integer, PSMSearchEngineScore> psmSearchEngineScoreMap = new TreeMap<Integer, PSMSearchEngineScore>();

    private SortedMap<Integer, Publication> publicationMap = new TreeMap<Integer, Publication>(); //1.1
//    private SplitList<Parameter> falseDiscoveryRate = new SplitList<Parameter>(BAR);
    private SortedMap<Integer, Contact> contactMap = new TreeMap<Integer, Contact>(); //1.1
    private List<URI> uriList = new ArrayList<URI>(); //1.1
//    private SortedMap<Integer, FixedMod> fixedModMap = new TreeMap<Integer, FixedMod>();
//    private SortedMap<Integer, VariableMod> variableModMap = new TreeMap<Integer, VariableMod>();
    private Parameter quantificationMethod; //1.1
//    private Parameter proteinQuantificationUnit;
//    private Parameter peptideQuantificationUnit;
    private SortedMap<Integer, Assay> assayMap = new TreeMap<Integer, Assay>(); //1.1
    private Parameter smallMoleculeQuantificationUnit;
    private SortedMap<Integer, MsRun> msRunMap = new TreeMap<Integer, MsRun>();
    private SortedMap<Integer, Parameter> customItemMap = new TreeMap<Integer, Parameter>(); //1.1
    private SortedMap<Integer, Parameter> idConfidenceMeasureMap = new TreeMap<Integer, Parameter>(); //1.1
    
    private SortedMap<Integer, Sample> sampleMap = new TreeMap<Integer, Sample>();
    private SortedMap<Integer, StudyVariable> studyVariableMap = new TreeMap<Integer, StudyVariable>();
    private SortedMap<Integer, CV> cvMap = new TreeMap<Integer, CV>();
    private SortedMap<Integer, Database> databaseMap = new TreeMap<Integer, Database>();
//    private List<ColUnit> proteinColUnitList = new ArrayList<ColUnit>();
//    private List<ColUnit> peptideColUnitList = new ArrayList<ColUnit>();
//    private List<ColUnit> psmColUnitList = new ArrayList<ColUnit>();
    private List<ColumnParameterMapping> smallMoleculeColUnitList = new ArrayList<ColumnParameterMapping>();
    private List<ColumnParameterMapping> smallMoleculeFeatureColUnitList = new ArrayList<ColumnParameterMapping>();
    private List<ColumnParameterMapping> smallMoleculeEvidenceColUnitList = new ArrayList<ColumnParameterMapping>();
    private Map<String, String> colUnitMap = new HashMap<String, String>();
    
        /**
     * Add a sample to metadata. Samples are NOT MANDATORY in mzTab, since many software packages cannot determine what
     * type of sample was analysed (e.g. whether biological or technical replication was performed).
     *
     * @param sample SHOULD NOT set null.
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
     */
    public Sample addSampleSpecies(Metadata metadata, Integer id, Parameter species) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be great than 0!");
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
     */
    public Sample addSampleTissue(Metadata metadata, Integer id, Parameter tissue) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be great than 0!");
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
     */
    public Sample addSampleCellType(Metadata metadata, Integer id, Parameter cellType) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be great than 0!");
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
     */
    public Sample addSampleDisease(Metadata metadata, Integer id, Parameter disease) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be great than 0!");
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
     */
    public Sample addSampleDescription(Metadata metadata, Integer id, String description) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be great than 0!");
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
     */
    public Sample addSampleCustom(Metadata metadata, Integer id, Parameter custom) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be great than 0!");
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
     */
    public SampleProcessing addSampleProcessing(Metadata metadata, Integer id, List<Parameter> sampleProcessing) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample id should be great than 0!");
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
     */
    public SampleProcessing addSampleProcessingParameter(Metadata metadata, Integer id, Parameter param) {
        if (id <= 0) {
            throw new IllegalArgumentException("Sample processing id should be great than 0!");
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
     * @param instrument SHOULD NOT set null.
     */
    public Instrument addInstrument(Metadata metadata, Instrument instrument) {
        if (instrument == null) {
            throw new IllegalArgumentException("Instrument should not be null");
        }
        instrumentMap.put(instrument.getId(), instrument);
        metadata.addInstrumentsItem(instrument);
        return instrument;
    }

    /**
     * Add a parameter for instrument[id]-name
     *
     * @param id SHOULD be positive integer.
     * @param name if null ignore operation.
     */
    public Instrument addInstrumentName(Metadata metadata, Integer id, Parameter name) {
        if (id <= 0) {
            throw new IllegalArgumentException("Instrument id should be great than 0!");
        }
        Instrument instrument = instrumentMap.get(id);
        if (name == null) {
            return instrument;
        }

        if (instrument == null) {
            instrument = new Instrument();
            instrument.id(id);
            instrument.instrumentName(name);
            instrumentMap.put(id, instrument);
            metadata.addInstrumentsItem(instrument);
        } else {
            instrument.instrumentName(name);
        }
        return instrument;
    }

    /**
     * Add a parameter for instrument[id]-source
     *
     * @param id SHOULD be positive integer.
     * @param source if null ignore operation.
     */
    public Instrument addInstrumentSource(Metadata metadata, Integer id, Parameter source) {
        if (id <= 0) {
            throw new IllegalArgumentException("Instrument id should be great than 0!");
        }
        Instrument instrument = instrumentMap.get(id);
        if (source == null) {
            return instrument;
        }

        if (instrument == null) {
            instrument = new Instrument();
            instrument.id(id);
            instrument.setInstrumentSource(source);
            instrumentMap.put(id, instrument);
            metadata.addInstrumentsItem(instrument);
        } else {
            instrument.setInstrumentSource(source);
        }
        return instrument;
    }

    /**
     * Add a parameter for instrument[id]-analyzer[i]
     *
     * @param id SHOULD be positive integer.
     * @param analyzer if null ignore operation.
     */
    public Instrument addInstrumentAnalyzer(Metadata metadata, Integer id, Parameter analyzer) {
        if (id <= 0) {
            throw new IllegalArgumentException("Instrument id should be great than 0!");
        }
        Instrument instrument = instrumentMap.get(id);
        if (analyzer == null) {
            return instrument;
        }

        if (instrument == null) {
            instrument = new Instrument();
            instrument.id(id);
            instrument.addInstrumentAnalyzerItem(analyzer);
            instrumentMap.put(id, instrument);
            metadata.addInstrumentsItem(instrument);
        } else {
            instrument.addInstrumentAnalyzerItem(analyzer);
        }
        return instrument;
    }

    /**
     * Add a parameter for instrument[id]-detector
     *
     * @param id SHOULD be positive integer.
     * @param detector if null ignore operation.
     */
    public Instrument addInstrumentDetector(Metadata metadata, Integer id, Parameter detector) {
        if (id <= 0) {
            throw new IllegalArgumentException("Instrument id should be great than 0!");
        }

        Instrument instrument = instrumentMap.get(id);
        if (detector == null) {
            return instrument;
        }

        if (instrument == null) {
            instrument = new Instrument();
            instrument.id(id);
            instrument.setInstrumentDetector(detector);
            instrumentMap.put(id, instrument);
            metadata.addInstrumentsItem(instrument);
        } else {
            instrument.setInstrumentDetector(detector);
        }
        return instrument;
    }

    /**
     * Add a software to metadata, which used to analyze the data and obtain the reported results.
     *
     * @param software SHOULD NOT set null
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
     */
    public Software addSoftwareParameter(Metadata metadata, Integer id, Parameter param) {
        if (id <= 0) {
            throw new IllegalArgumentException("Software id should be great than 0!");
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
     */
    public Software addSoftwareSetting(Metadata metadata, Integer id, String setting) {
        if (id <= 0) {
            throw new IllegalArgumentException("Software id should be great than 0!");
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
     * Add a protein_search_engine_score[id] parameter. The parameter's value SHOULD contain the engine score cv param.
     * The order (numbering) SHOULD reflect their importance for the identification and be used to determine
     * the identification's rank.
     *
     * @param id SHOULD be positive integer.
     * @param param if null ignore operation.
     */
//    public void addProteinSearchEngineScoreParameter(Integer id, Parameter param) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("Protein search engine score id should be great than 0!");
//        }
//        if (param == null) {
//            return;
//        }
//
//        ProteinSearchEngineScore searchEngineScore = proteinSearchEngineScoreMap.get(id);
//        if (searchEngineScore == null) {
//            searchEngineScore = new ProteinSearchEngineScore(id);
//            searchEngineScore.setParameter(param);
//            proteinSearchEngineScoreMap.put(id, searchEngineScore);
//        } else {
//            searchEngineScore.setParameter(param);
//        }
//    }

    /**
     * Add a peptide_search_engine_score[id] parameter. The parameter's value SHOULD contain the engine score cv param.
     * The order (numbering) SHOULD reflect their importance for the identification and be used to determine
     * the identification's rank.
     *
     * @param id SHOULD be positive integer.
     * @param param if null ignore operation.
     */
//    public void addPeptideSearchEngineScoreParameter(Integer id, Parameter param) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("Peptide search engine score id should be great than 0!");
//        }
//        if (param == null) {
//            return;
//        }
//
//        PeptideSearchEngineScore searchEngineScore = peptideSearchEngineScoreMap.get(id);
//        if (searchEngineScore == null) {
//            searchEngineScore = new PeptideSearchEngineScore(id);
//            searchEngineScore.setParameter(param);
//            peptideSearchEngineScoreMap.put(id, searchEngineScore);
//        } else {
//            searchEngineScore.setParameter(param);
//        }
//    }

    /**
     * Add a psm_search_engine_score[id] parameter. The parameter's value SHOULD contain the engine score cv param.
     * The order (numbering) SHOULD reflect their importance for the identification and be used to determine
     * the identification's rank.
     *
     * @param id SHOULD be positive integer.
     * @param param if null ignore operation.
     */
//    public void addPsmSearchEngineScoreParameter(Integer id, Parameter param) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("PSM search engine score id should be great than 0!");
//        }
//        if (param == null) {
//            return;
//        }
//
//        PSMSearchEngineScore searchEngineScore = psmSearchEngineScoreMap.get(id);
//        if (searchEngineScore == null) {
//            searchEngineScore = new PSMSearchEngineScore(id);
//            searchEngineScore.setParameter(param);
//            psmSearchEngineScoreMap.put(id, searchEngineScore);
//        } else {
//            searchEngineScore.setParameter(param);
//        }
//    }

    /**
     * Add a smallmolecule_search_engine_score[id] parameter. The parameter's value SHOULD contain the engine score cv param.
     * The order (numbering) SHOULD reflect their importance for the identification and be used to determine
     * the identification's rank.
     *
     * @param id SHOULD be positive integer.
     * @param param if null ignore operation.
     */
//    public void addSmallMoleculeSearchEngineScoreParameter(Integer id, Parameter param) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("PSM search engine score id should be great than 0!");
//        }
//        if (param == null) {
//            return;
//        }
//
//        SmallMoleculeSearchEngineScore searchEngineScore = smallMoleculeSearchEngineScoreMap.get(id);
//        if (searchEngineScore == null) {
//            searchEngineScore = new SmallMoleculeSearchEngineScore(id);
//            searchEngineScore.setParameter(param);
//            smallMoleculeSearchEngineScoreMap.put(id, searchEngineScore);
//        } else {
//            searchEngineScore.setParameter(param);
//        }
//    }

    /**
     * Add a false_discovery_rate parameter to metadata. The file's false discovery rate(s) reported at the PSM,
     * peptide, and/or protein level. False Localization Rate (FLD) for the reporting of modifications can also be
     * reported here. Multiple parameters MUST be separated by "|".
     *
     * @param param SHOULD NOT set null.
     */
//    public void addFalseDiscoveryRateParameter(Parameter param) {
//        if (param == null) {
//            throw new NullPointerException("False discovery rate parameter should not set null");
//        }
//
//        this.falseDiscoveryRate.add(param);
//    }

    /**
     * Add a publiction to metadata. A publication associated with this file. Several publications can be given by
     * indicating the number in the square brackets after "publication". PubMed ids must be prefixed by "pubmed:",
     * DOIs by "doi:". Multiple identifiers MUST be separated by "|".
     *
     * @param publication SHOULD NOT set null.
     */
    public Publication addPublication(Metadata metadata, Publication publication) {
        if (publication == null) {
            throw new IllegalArgumentException("Publication should not be null");
        }
        publicationMap.put(publication.getId(), publication);
        metadata.addPublicationsItem(publication);
        return publication;
    }

    /**
     * Add a publication item to metadata. PubMed ids must be prefixed by "pubmed:", DOIs by "doi:".
     * Multiple identifiers MUST be separated by "|".
     *
     * @param id SHOULD be positive integer.
     * @param type SHOULD NOT set null.
     * @param accession SHOULD NOT set empty.
     */
    public Publication addPublicationItem(Metadata metadata, Integer id, PublicationItem.TypeEnum type, String accession) {
        if (id <= 0) {
            throw new IllegalArgumentException("Publication id should be great than 0!");
        }
        if (type == null) {
            throw new NullPointerException("Publication type should not set null");
        }
        if (isEmpty(accession)) {
            throw new IllegalArgumentException("Publication accession should not set empty.");
        }

        Publication publication = publicationMap.get(id);
        if (publication == null) {
            publication = new Publication();
            publication.id(id);
            publication.addPublicationItemsItem(new PublicationItem().type(type).accession(accession));
            publicationMap.put(id, publication);
            metadata.addPublicationsItem(publication);
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
     * @param items SHOULD NOT set null.
     */
    public Publication addPublicationItems(Metadata metadata, Integer id, Collection<PublicationItem> items) {
        if (id <= 0) {
            throw new IllegalArgumentException("Publication id should be great than 0!");
        }
        if (items == null) {
            throw new NullPointerException("Publication items should not set null");
        }

        Publication publication = publicationMap.get(id);
        if (publication == null) {
            publication = new Publication();
            publication.id(id);
            publication.setPublicationItems(new ArrayList<>(items));
            publicationMap.put(id, publication);
            metadata.addPublicationsItem(publication);
        } else {
            publication.setPublicationItems(new ArrayList<>(items));
        }
        return publication;
    }

    /**
     * Add a contact into metadata.
     *
     * @param contact SHOULD NOT set null.
     */
    public Contact addContact(Metadata metadata, Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact should not be null");
        }

        contactMap.put(contact.getId(), contact);
        metadata.addContactsItem(contact);
        return contact;
    }

    /**
     * Add contact[id]-name. Several contacts can be given by indicating the number in the square brackets
     * after "contact". A contact has to be supplied in the format [first name] [initials] [last name] (see example).
     *
     * @param id SHOULD be positive integer.
     * @param name SHOULD NOT set empty.
     */
    public Contact addContactName(Metadata metadata, Integer id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException("Contact id should be great than 0!");
        }
        if (isEmpty(name)) {
            throw new IllegalArgumentException("Contact name should not set empty.");
        }

        Contact contact = contactMap.get(id);
        if (contact == null) {
            contact = new Contact();
            contact.id(id);
            contact.setName(name);
            contactMap.put(id, contact);
            metadata.addContactsItem(contact);
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
     */
    public Contact addContactAffiliation(Metadata metadata, Integer id, String affiliation) {
        if (id <= 0) {
            throw new IllegalArgumentException("Contact id should be great than 0!");
        }
        if (isEmpty(affiliation)) {
            throw new IllegalArgumentException("Contact affiliation should not set empty.");
        }

        Contact contact = contactMap.get(id);
        if (contact == null) {
            contact = new Contact();
            contact.id(id);
            contact.setAffiliation(affiliation);
            contactMap.put(id, contact);
            metadata.addContactsItem(contact);
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
     */
    public Contact addContactEmail(Metadata metadata, Integer id, String email) {
        if (id <= 0) {
            throw new IllegalArgumentException("Contact id should be great than 0!");
        }
        if (isEmpty(email)) {
            throw new IllegalArgumentException("Contact email should not set empty.");
        }

        Contact contact = contactMap.get(id);
        if (contact == null) {
            contact = new Contact();
            contact.id(id);
            contact.setEmail(email);
            contactMap.put(id, contact);
            metadata.addContactsItem(contact);
        } else {
            contact.setEmail(email);
        }
        return contact;
    }

    /**
     * Add uri into metadata. The URI pointing to the file's source data (e.g., a PRIDE experiment or a PeptideAtlas build).
     * @param uri if null ignore operation.
     */
    public URI addUri(Metadata metadata, URI uri) {
        if (uri == null) {
            return null;
        }

        this.uriList.add(uri);
        metadata.addUriItem(uri.toASCIIString());
        return uri;
    }

    /**
     * Add fixed_mod[id] into metadata. A parameter describing a fixed modifications searched for. Multiple
     * fixed modifications are numbered 1..n.
     *
     * @param mod if null ignore operation.
     */
//    public void addFixedMod(FixedMod mod) {
//        if (mod == null) {
//            return;
//        }
//
//        this.fixedModMap.put(mod.getId(), mod);
//    }

    /**
     * Add fixed_mod[id] parameter into metadata. A parameter describing a fixed modifications searched for.
     *
     * @param id SHOULD be positive integer.
     * @param param if null ignore operation.
     */
//    public void addFixedModParameter(Integer id, Parameter param) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("fixed_mod id should be great than 0!");
//        }
//        if (param == null) {
//            return;
//        }
//
//        FixedMod mod = fixedModMap.get(id);
//        if (mod == null) {
//            mod = new FixedMod(id);
//            mod.setParameter(param);
//            fixedModMap.put(id, mod);
//        } else {
//            mod.setParameter(param);
//        }
//    }

    /**
     * Add fixed_mod[id]-site into metadata. A string describing a fixed modifications site. Following the unimod
     * convention, modification site is a residue (e.g. "M"), terminus ("N-term" or "C-term") or both (e.g.
     * "N-term Q" or "C-term K").
     *
     * @param id SHOULD be positive integer.
     * @param site SHOULD NOT set empty.
     */
//    public void addFixedModSite(Integer id, String site) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("fixed_mod id should be great than 0!");
//        }
//        if (isEmpty(site)) {
//            throw new IllegalArgumentException("fixed_mod site should not set empty.");
//        }
//
//        FixedMod mod = fixedModMap.get(id);
//        if (mod == null) {
//            mod = new FixedMod(id);
//            mod.setSite(site);
//            fixedModMap.put(id, mod);
//        } else  {
//            mod.setSite(site);
//        }
//    }

    /**
     * Add fixed_mod[id]-position into metadata. A string describing the term specifity of a fixed modification.
     * Following the unimod convention, term specifity is denoted by the strings "Anywhere", "Any N-term",
     * "Any C-term", "Protein N-term", "Protein C-term".
     *
     * @param id SHOULD be positive integer.
     * @param position SHOULD NOT set empty.
     */
//    public void addFixedModPosition(Integer id, String position) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("fixed_mod id should be great than 0!");
//        }
//        if (isEmpty(position)) {
//            throw new IllegalArgumentException("fixed_mod position should not set empty.");
//        }
//
//        FixedMod mod = fixedModMap.get(id);
//        if (mod == null) {
//            mod = new FixedMod(id);
//            mod.setPosition(position);
//            fixedModMap.put(id, mod);
//        } else  {
//            mod.setPosition(position);
//        }
//    }

    /**
     * Add variable_mod[id] into metadata.
     *
     * @param mod if null ignore operation.
     */
//    public void addVariableMod(VariableMod mod) {
//        if (mod == null) {
//            return;
//        }
//
//        this.variableModMap.put(mod.getId(), mod);
//    }

    /**
     * Add variable_mod[id] parameter into metadata. A parameter describing a variable modifications searched for.
     * Multiple variable modifications are numbered 1.. n.
     *
     * @param id SHOULD be positive integer.
     * @param param if null ignore operation.
     */
//    public void addVariableModParameter(Integer id, Parameter param) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("variable_mod id should be great than 0!");
//        }
//        if (param == null) {
//            return;
//        }
//
//        VariableMod mod = variableModMap.get(id);
//        if (mod == null) {
//            mod = new VariableMod(id);
//            mod.setParameter(param);
//            variableModMap.put(id, mod);
//        } else {
//            mod.setParameter(param);
//        }
//    }

    /**
     * Add variable_mod[id]-site into metadata. A string describing a variable modifications site.
     * Following the unimod convention, modification site is a residue (e.g. "M"), terminus ("N-term"
     * or "C-term") or both (e.g. "N-term Q" or "C-term K").
     *
     * @param id SHOULD be positive integer.
     * @param site SHOULD NOT set empty.
     */
//    public void addVariableModSite(Integer id, String site) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("variable_mod id should be great than 0!");
//        }
//        if (isEmpty(site)) {
//            throw new IllegalArgumentException("variable_mod site should not set empty.");
//        }
//
//        VariableMod mod = variableModMap.get(id);
//        if (mod == null) {
//            mod = new VariableMod(id);
//            mod.setSite(site);
//            variableModMap.put(id, mod);
//        } else  {
//            mod.setSite(site);
//        }
//    }

    /**
     * Add variable_mod[id]-position into metadata. A string describing the term specifity of a variable modification.
     * Following the unimod convention, term specifity is denoted by the strings "Anywhere", "Any N-term",
     * "Any C-term", "Protein N-term", "Protein C-term".
     *
     * @param id SHOULD be positive integer.
     * @param position SHOULD NOT set empty.
     */
//    public void addVariableModPosition(Integer id, String position) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("variable_mod id should be great than 0!");
//        }
//        if (isEmpty(position)) {
//            throw new IllegalArgumentException("variable_mod position should not set empty.");
//        }
//
//        VariableMod mod = variableModMap.get(id);
//        if (mod == null) {
//            mod = new VariableMod(id);
//            mod.setPosition(position);
//            variableModMap.put(id, mod);
//        } else  {
//            mod.setPosition(position);
//        }
//    }

    /**
     * Add a ms_run[id] into metadata. An MS run is effectively one run (or set of runs on pre-fractionated samples)
     * on an MS instrument, and is referenced from assay in different contexts.
     *
     * @param msRun SHOULD NOT set null.
     */
    public MsRun addMsRun(Metadata metadata, MsRun msRun) {
        if (msRun == null) {
            throw new IllegalArgumentException("MsRun should not be null");
        }

        msRunMap.put(msRun.getId(), msRun);
        metadata.addMsrunItem(msRun);
        return msRun;
    }

    /**
     * Add ms_run[id]-format into metadata. A parameter specifying the data format of the external MS data file.
     *
     * @param id SHOULD be positive integer.
     * @param format if null ignore operation.
     */
    public MsRun addMsRunFormat(Metadata metadata, Integer id, Parameter format) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be great than 0!");
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
            metadata.addMsrunItem(msRun);
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
     */
    public MsRun addMsRunLocation(Metadata metadata, Integer id, URL location) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be great than 0!");
        }
//        The ms_run[id]-location needs to be created even if it is null because it is mandatory.
//        if (location == null) {
//            return;
//        }

        MsRun msRun = msRunMap.get(id);
        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.setLocation(location==null?null:location.toString());
            msRunMap.put(id, msRun);
            metadata.addMsrunItem(msRun);
        } else {
            msRun.setLocation(location.toString());
        }
        return msRun;
    }

    /**
     * Add ms_run[id]-id_format into metadata. Parameter specifying the id format used in the external data file.
     *
     * @param id SHOULD be positive integer.
     * @param idFormat if null ignore operation.
     */
    public MsRun addMsRunIdFormat(Metadata metadata, Integer id, Parameter idFormat) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be great than 0!");
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
            metadata.addMsrunItem(msRun);
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
     */
    public MsRun addMsRunFragmentationMethod(Metadata metadata, Integer id, Parameter fragmentationMethod) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be great than 0!");
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
            metadata.addMsrunItem(msRun);
        } else {
            msRun.addFragmentationMethodItem(fragmentationMethod);
        }
        return msRun;
    }

    public MsRun addMsRunHash(Metadata metadata, Integer id, String hash) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be great than 0!");
        }
        if (isEmpty(hash)) {
            throw new IllegalArgumentException("ms_run hash should not set empty.");
        }

        MsRun msRun = msRunMap.get(id);
        if (msRun == null) {
            msRun = new MsRun();
            msRun.id(id);
            msRun.setHash(hash);
            msRunMap.put(id, msRun);
            metadata.addMsrunItem(msRun);
        } else {
            msRun.setHash(hash);
        }
        return msRun;
    }

    public MsRun addMsRunHashMethod(Metadata metadata, Integer id, Parameter hashMethod) {
        if (id <= 0) {
            throw new IllegalArgumentException("ms_run id should be great than 0!");
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
            metadata.addMsrunItem(msRun);
        } else {
            msRun.setHashMethod(hashMethod);
        }
        return msRun;
    }

//    /**
//     * Add a custom parameter into metadata. Any additional parameters describing the analysis reported.
//     *
//     * @param custom if null ignore operation.
//     */
//    public Parameter addCustom(Metadata metadata, Parameter custom) {
//        if (custom == null) {
//            return null;
//        }
//
//        this.customList.add(custom);
//        metadata.addCustomItem(custom);
//        return custom;
//    }

    /**
     * Add a assay into metadata. The application of a measurement about the sample (in this case through MS) -
     * producing values about small molecules, peptides or proteins. One assay is typically mapped to one MS run
     * in the case of label-free MS analysis or multiple assays are mapped to one MS run for multiplexed techniques,
     * along with a description of the label or tag applied.
     *
     * @param assay SHOULD NOT set null.
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
     * Add assay[id]-quantification_reagent into metadata. The reagent used to label the sample in the assay.
     * For label-free analyses the "unlabeled sample" CV term SHOULD be used. For the "light" channel in
     * label-based experiments the appropriate CV term specifying the labelling channel should be used.
     *
     * @param id SHOULD be positive integer.
     * @param quantificationReagent if null ignore operation.
     */
//    public void addAssayQuantificationReagent(Integer id, Parameter quantificationReagent) {
//        if (id <= 0) {
//            throw new IllegalArgumentException("assay id should be great than 0!");
//        }
//        if (quantificationReagent == null) {
//            return;
//        }
//
//        Assay assay = assayMap.get(id);
//        if (assay == null) {
//            assay = new Assay();
//            assay.id(id);
//            assay.setQuantificationReagent(quantificationReagent);
//            assayMap.put(id, assay);
//        } else {
//            assay.setQuantificationReagent(quantificationReagent);
//        }
//    }

    /**
     * Add assay[id]-sample_ref into metadata. An association from a given assay to the sample analysed.
     *
     * @param id SHOULD be positive integer.
     * @param sample SHOULD NOT set null, and SHOULD be defined in metadata first.
     */
    public Assay addAssaySample(Metadata metadata, Integer id, Sample sample) {
        if (id <= 0) {
            throw new IllegalArgumentException("assay id should be great than 0!");
        }
        if (sample == null) {
            throw new NullPointerException("assay sample_ref should not set null.");
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
     * @param msRun SHOULD NOT set null, and SHOULD be defined in metadata first.
     */
    public Assay addAssayMsRun(Metadata metadata, Integer id, MsRun msRun) {
        if (id <= 0) {
            throw new IllegalArgumentException("assay id should be great than 0!");
        }
        if (msRun == null) {
            throw new NullPointerException("assay ms_run_ref should not set null.");
        }
        if (! msRunMap.containsValue(msRun)) {
            throw new IllegalArgumentException("ms_run should be defined in metadata first.");
        }

        Assay assay = assayMap.get(id);
        if (assay == null) {
            assay = new Assay();
            assay.id(id);
            assay.setMsRunRef(msRun);
            assayMap.put(id, assay);
            metadata.addAssayItem(assay);
        } else {
            assay.setMsRunRef(msRun);
        }
        return assay;
    }

    /**
     * Add assay[assayId]-quantification_mod[1-n] into metadata. A parameter describing a modification
     * associated with a quantification_reagent. Multiple modifications are numbered 1..n.
     *
     * @param assayId SHOULD be positive integer.
     * @param mod if null ignore operation.
     */
//    public void addAssayQuantificationMod(Integer assayId, AssayQuantificationMod mod) {
//        if (assayId <= 0) {
//            throw new IllegalArgumentException("assay id should be great than 0!");
//        }
//        if (mod == null) {
//            return;
//        }
//
//        Assay assay = assayMap.get(assayId);
//        if (assay == null) {
//            assay = new Assay();
//            assay.id(assayId);
//            assay.addQuantificationMod(mod);
//            assayMap.put(assayId, assay);
//        } else {
//            assay.addQuantificationMod(mod);
//        }
//    }

    /**
     * Add assay[assayId]-quantification_mod[quanModId] into metadata. A parameter describing a modification
     * associated with a quantification_reagent.
     *
     * @param assayId SHOULD be positive integer.
     * @param quanModId SHOULD be positive integer.
     * @param param if null ignore operation.
     */
//    public void addAssayQuantificationModParameter(Integer assayId, Integer quanModId, Parameter param) {
//        if (assayId <= 0) {
//            throw new IllegalArgumentException("assay id should be great than 0!");
//        }
//        if (quanModId <= 0) {
//            throw new IllegalArgumentException("quantification_mod id should be great than 0!");
//        }
//        if (param == null) {
//            return;
//        }
//
//        Assay assay = assayMap.get(assayId);
//        if (assay == null) {
//            assay = new Assay();
//            assay.id(assayId);
//            assay.addQuantificationModParameter(quanModId, param);
//            assayMap.put(assayId, assay);
//        } else {
//            assay.addQuantificationModParameter(quanModId, param);
//        }
//    }

    /**
     * Add assay[assayId]-quantification_mod[quanModId]-site into metadata. A string describing the modifications
     * site. Following the unimod convention, modification site is a residue (e.g. "M"), terminus ("N-term"
     * or "C-term") or both (e.g. "N-term Q" or "C-term K").
     *
     * @param assayId SHOULD be positive integer.
     * @param quanModId SHOULD be positive integer.
     * @param site SHOULD NOT empty.
     */
//    public void addAssayQuantificationModSite(Integer assayId, Integer quanModId, String site) {
//        if (assayId <= 0) {
//            throw new IllegalArgumentException("assay id should be great than 0!");
//        }
//        if (quanModId <= 0) {
//            throw new IllegalArgumentException("quantification_mod id should be great than 0!");
//        }
//        if (isEmpty(site)) {
//            throw new IllegalArgumentException("quantification_mod-site should not empty!");
//        }
//
//        Assay assay = assayMap.get(assayId);
//        if (assay == null) {
//            assay = new Assay(assayId);
//            assay.addQuantificationModSite(quanModId, site);
//            assayMap.put(assayId, assay);
//        } else {
//            assay.addQuantificationModSite(quanModId, site);
//        }
//    }

    /**
     * Add assay[assayId]-quantification_mod[quanModId]-site into metadata. A string describing the term specifity
     * of the modification. Following the unimod convention, term specifity is denoted by the strings "Anywhere",
     * "Any N-term", "Any C-term", "Protein N-term", "Protein C-term".
     *
     * @param assayId SHOULD be positive integer.
     * @param quanModId SHOULD be positive integer.
     * @param position SHOULD NOT empty.
     */
//    public void addAssayQuantificationModPosition(Integer assayId, Integer quanModId, String position) {
//        if (assayId <= 0) {
//            throw new IllegalArgumentException("assay id should be great than 0!");
//        }
//        if (quanModId <= 0) {
//            throw new IllegalArgumentException("quantification_mod id should be great than 0!");
//        }
//        if (isEmpty(position)) {
//            throw new IllegalArgumentException("quantification_mod position should not empty!");
//        }
//
//        Assay assay = assayMap.get(assayId);
//        if (assay == null) {
//            assay = new Assay(assayId);
//            assay.addQuantificationModPosition(quanModId, position);
//            assayMap.put(assayId, assay);
//        } else {
//            assay.addQuantificationModPosition(quanModId, position);
//        }
//    }

    /**
     * Add a study variable into metadata. The variables about which the final results of a study are reported, which
     * may have been derived following averaging across a group of replicate measurements (assays). In files where assays
     * are reported, study variables have references to assays. The same concept has been defined by others as "experimental factor".
     *
     * @param studyVariable SHOULD NOT set null.
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
     * @param assay SHOULD NOT set null, and should be defined in metadata first.
     */
    public StudyVariable addStudyVariableAssay(Metadata metadata, Integer id, Assay assay) {
        if (id <= 0) {
            throw new IllegalArgumentException("study variable id should be greater than 0!");
        }
        if (assay == null) {
            throw new NullPointerException("study_variable[n]-assay_ref should not set null.");
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
     * Add a study_variable[id]-sample_refs. Comma-separated references to the samples that were analysed in the study variable.
     *
     * @param id SHOULD be positive integer.
     * @param sample SHOULD NOT set null, and should be defined in metadata first.
     */
    public StudyVariable addStudyVariableSample(Metadata metadata, Integer id, Sample sample) {
        if (id <= 0) {
            throw new IllegalArgumentException("study variable id should be greater than 0!");
        }
        if (sample == null) {
            throw new NullPointerException("study_variable[n]-sample_ref should not set null.");
        }
        if (! sampleMap.containsValue(sample)) {
            throw new IllegalArgumentException("sample should be defined in metadata first");
        }

        StudyVariable studyVariable = studyVariableMap.get(id);
        if (studyVariable == null) {
            studyVariable = new StudyVariable();
            studyVariable.id(id);
            studyVariable.addSampleRefsItem(sample);
            studyVariableMap.put(id, studyVariable);
            metadata.addStudyVariableItem(studyVariable);
        } else {
            studyVariable.addSampleRefsItem(sample);
        }
        return studyVariable;
    }

    /**
     * Add a study_variable[id]-description. A textual description of the study variable.
     *
     * @param id SHOULD be positive integer.
     * @param description if empty ignore operation.
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
     * Add a study_variable[id]-quantification_value_function. This is a Parameter detailing how the 
     * reported study variable abundances have been calculated.
     * 
     * @param metadata
     * @param id SHOULD be positive integer.
     * @param checkParameter the parameter.
     */
    public StudyVariable addStudyVariableQuantificationFunction(Metadata metadata, Integer id,
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
            studyVariable.addQuantificationValueFunctionItem(checkParameter);
            studyVariableMap.put(id, studyVariable);
            metadata.addStudyVariableItem(studyVariable);
        } else {
            studyVariable.addQuantificationValueFunctionItem(checkParameter);
        }
        return studyVariable;
    }

    /**
     * Add a controlled vocabularies/ontologies into metadata. Define the controlled vocabularies/ontologies
     * used in the mzTab file.
     *
     * @param cv SHOULD NOT set null.
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
     */
    public CV addCVLabel(Metadata metadata, Integer id, String label) {
        if (id <= 0) {
            throw new IllegalArgumentException("controlled vocabularies id should be great than 0!");
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
     */
    public CV addCVFullName(Metadata metadata, Integer id, String fullName) {
        if (id <= 0) {
            throw new IllegalArgumentException("controlled vocabularies id should be great than 0!");
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
     */
    public CV addCVVersion(Metadata metadata, Integer id, String version) {
        if (id <= 0) {
            throw new IllegalArgumentException("controlled vocabularies id should be great than 0!");
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
     * Add a cv[id]-url. A string containing the URLs of the controlled vocabularies/ontologies used in the
     * mzTab file
     *
     * @param id SHOULD be positive integer.
     */
    public CV addCVURL(Metadata metadata, Integer id, String url) {
        if (id <= 0) {
            throw new IllegalArgumentException("controlled vocabularies id should be great than 0!");
        }

        CV cv = cvMap.get(id);
        if (cv == null) {
            cv = new CV();
            cv.id(id);
            metadata.addCvItem(cv);
        }

        cv.setUrl(url);
        return cvMap.put(id, cv);
    }

    /**
     * Defines the unit for the data reported in a column of the protein section. Defines the unit for the data reported
     * in a column of the protein section. The format of the value has to be {column name}={Parameter defining the unit}
     * This field MUST NOT be used to define a unit for quantification columns. The unit used for protein quantification
     * values MUST be set in protein-quantification_unit.
     *
     * @param column SHOULD NOT set null
     * @param param SHOULD NOT set null
     */
//    public void addProteinColUnit(MZTabColumn column, Parameter param) {
//        this.proteinColUnitList.add(new ColUnit(column, param));
//    }

    /**
     * Defines the unit for the data reported in a column of the peptide section. Defines the used unit for a column in the
     * peptide section. The format of the value has to be {column name}={Parameter defining the unit}. This field MUST NOT
     * be used to define a unit for quantification columns. The unit used for peptide quantification values MUST be set in
     * peptide-quantification_unit.

     *
     * @param column SHOULD NOT set null
     * @param param SHOULD NOT set null
     */
//    public void addPeptideColUnit(MZTabColumn column, Parameter param) {
//        this.peptideColUnitList.add(new ColUnit(column, param));
//    }

    /**
     * Defines the unit for the data reported in a column of the PSM section. Defines the used unit for a column in the PSM
     * section. The format of the value has to be {column name}={Parameter defining the unit} This field MUST NOT be used to
     * define a unit for quantification columns. The unit used for peptide quantification values MUST be set in
     * peptide-quantification_unit.

     *
     * @param column SHOULD NOT set null
     * @param param SHOULD NOT set null
     */
//    public void addPSMColUnit(MZTabColumn column, Parameter param) {
//        this.psmColUnitList.add(new ColUnit(column, param));
//    }

    /**
     * Defines the unit for the data reported in a column of the small molecule section. Defines the used unit for a column
     * in the small molecule section. The format of the value has to be {column name}={Parameter defining the unit}
     * This field MUST NOT be used to define a unit for quantification columns. The unit used for small molecule quantification
     * values MUST be set in small_molecule-quantification_unit.
     *
     * @param column SHOULD NOT set null
     * @param param SHOULD NOT set null
     */
    public void addSmallMoleculeColUnit(Metadata metadata, MZTabColumn column, Parameter param) {
        ColumnParameterMapping cpm = new ColumnParameterMapping();
        cpm.columnName(column.getName()).param(param);
        this.smallMoleculeColUnitList.add(cpm);
        metadata.addColunitSmallMoleculeItem(cpm);
    }
    
    /**
     * Defines the unit for the data reported in a column of the small molecule section. Defines the used unit for a column
     * in the small molecule section. The format of the value has to be {column name}={Parameter defining the unit}
     * This field MUST NOT be used to define a unit for quantification columns. The unit used for small molecule quantification
     * values MUST be set in small_molecule-quantification_unit.
     *
     * @param column SHOULD NOT set null
     * @param param SHOULD NOT set null
     */
    public void addSmallMoleculeFeatureColUnit(Metadata metadata, MZTabColumn column, Parameter param) {
        ColumnParameterMapping cpm = new ColumnParameterMapping();
        cpm.columnName(column.getName()).param(param);
        this.smallMoleculeFeatureColUnitList.add(cpm);
        metadata.addColunitSmallMoleculeFeatureItem(cpm);
    }
    
    /**
     * Defines the unit for the data reported in a column of the small molecule section. Defines the used unit for a column
     * in the small molecule section. The format of the value has to be {column name}={Parameter defining the unit}
     * This field MUST NOT be used to define a unit for quantification columns. The unit used for small molecule quantification
     * values MUST be set in small_molecule-quantification_unit.
     *
     * @param column SHOULD NOT set null
     * @param param SHOULD NOT set null
     */
    public void addSmallMoleculeEvidenceColUnit(Metadata metadata, MZTabColumn column, Parameter param) {
        ColumnParameterMapping cpm = new ColumnParameterMapping();
        cpm.columnName(column.getName()).param(param);
        this.smallMoleculeEvidenceColUnitList.add(cpm);
        metadata.addColunitSmallMoleculeEvidenceItem(cpm);
    }

    /**
     *  Defines a method to access the colUnit to help in the transformation from columnName String -> to columnName MZTabColumn
     */
    public Map<String, String> getColUnitMap() {
        return colUnitMap;
    }

    /**
     * Add a confidence measure id parameter.
     * @param id SHOULD NOT set null
     * @param parameter SHOULD NOT set null
     */
    void addIdConfidenceMeasure(Metadata metadata, Integer id, Parameter parameter) {
        if(parameter.getId()==null) {
            parameter.setId(id);
        }
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
        if(custom.getId()==null) {
            custom.setId(id);
        }
        this.customItemMap.put(id, custom);
        metadata.addCustomItem(custom);
        return custom;
    }

}
