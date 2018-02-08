package de.isas.lipidomics.jmztabm.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.isas.lipidomics.jmztabm.io.serialization.ParameterSerializer;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.ColumnParameterMapping;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.ExternalStudy;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.PublicationItem;
import de.isas.mztab1_1.model.Sample;
import de.isas.mztab1_1.model.SampleProcessing;
import de.isas.mztab1_1.model.Software;
import de.isas.mztab1_1.model.StudyVariable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeFeatureColumn;

/**
 * Test class for MzTabWriter.
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabWriterTest {

    static MzTab createTestFile() {

        final MzTab mztabfile = new MzTab().metadata(
            new de.isas.mztab1_1.model.Metadata().mzTabVersion("1.1.0").
                mzTabID("ISAS_2017_M_11451").
                title("A minimal test file").
                description("A description of an mzTab file.").
                addContactsItem(
                    new Contact().id(1).
                        name("Nils Hoffmann").
                        email("nils.hoffmann_at_isas.de").
                        affiliation(
                            "ISAS e.V. Dortmund, Germany")
                ).
                addMsrunItem(
                    new MsRun().id(1).
                        location("file:///path/to/file1.mzML").
                        format(
                            new Parameter().
                                cvLabel("MS").
                                cvAccession("MS:1000584").
                                name("mzML file")
                        ).
                        idFormat(
                            new Parameter().
                                cvLabel("MS").
                                cvAccession("MS:1001530").
                                name("mzML unique identifier")
                        )
                )
        );
        PublicationItem item1_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("21063943");
        PublicationItem item1_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1007/978-1-60761-987-1_6");
        Publication publication1 = new Publication().id(1);
        publication1.setPublicationItems(Arrays.asList(item1_1, item1_2));

        PublicationItem item2_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("20615486");
        PublicationItem item2_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1016/j.jprot.2010.06.008");
        Publication publication2 = new Publication().id(2);
        publication2.setPublicationItems(Arrays.asList(item2_1, item2_2));

        mztabfile.getMetadata().
            addPublicationsItem(publication1).
            addPublicationsItem(publication2);
        return mztabfile;
    }

    static MzTab create1_1TestFile() {
        de.isas.mztab1_1.model.Metadata mtd = new de.isas.mztab1_1.model.Metadata();
        mtd.mzTabID("PRIDE_1234").
            mzTabVersion("1.1").
            title("My first test experiment").
            description("An experiment investigating the effects of Il-6.");
        SampleProcessing sp = new SampleProcessing().id(1).
            addSampleProcessingItem(new Parameter().cvLabel("SEP").
                cvAccession("SEP:00142").
                name("enzyme digestion")).
            addSampleProcessingItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1001251").
                name("Trypsin")).
            addSampleProcessingItem(new Parameter().cvLabel("SEP").
                cvAccession("SEP:00173").
                name("SDS PAGE"));
        mtd.sampleProcessing(Arrays.asList(sp));

        Instrument instrument1 = new Instrument().id(1).
            instrumentName(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:100049").
                    name("LTQ Orbitrap")).
            instrumentSource(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000073").
                    name("ESI")).
            instrumentAnalyzer(Arrays.asList(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000291").
                    name("linear ion trap"))
            ).
            instrumentDetector(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000253").
                    name("electron multiplier")
            );
        mtd.addInstrumentsItem(instrument1);
        Instrument instrument2 = new Instrument().id(2).
            instrumentName(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000031").
                    name("instrument model").
                    value("name of the instrument not included in the CV")).
            instrumentSource(new Parameter().cvLabel("MS").
                cvAccession("MS:1000598").
                name("ETD")).
            addInstrumentAnalyzerItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1000484").
                name("orbitrap")).
            instrumentDetector(new Parameter().cvLabel("MS").
                cvAccession("MS:1000348").
                name("focal plane collector"));
        mtd.addInstrumentsItem(instrument2);
        Software software1 = new Software().id(1).
            parameter(new Parameter().cvLabel(
                "MS").
                cvAccession("MS:1001207").
                name("Mascot").
                value("2.3")).
            setting(Arrays.asList("Fragment tolerance = 0.1Da",
                "Parent tolerance = 0.5Da"));
        mtd.addSoftwareItem(software1);

        PublicationItem item1_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("21063943");
        PublicationItem item1_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1007/978-1-60761-987-1_6");
        Publication publication1 = new Publication().id(1);
        publication1.setPublicationItems(Arrays.asList(item1_1, item1_2));

        PublicationItem item2_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("20615486");
        PublicationItem item2_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1016/j.jprot.2010.06.008");
        Publication publication2 = new Publication().id(2);
        publication2.setPublicationItems(Arrays.asList(item2_1, item2_2));

        mtd.addPublicationsItem(publication1).
            addPublicationsItem(publication2);

        mtd.addContactsItem(new Contact().id(1).
            name("James D. Watson").
            affiliation("Cambridge University, UK").
            email("watson@cam.ac.uk"));
        mtd.addContactsItem(new Contact().id(2).
            name("Francis Crick").
            affiliation("Cambridge University, UK").
            email("crick@cam.ac.uk"));

        ExternalStudy es = new ExternalStudy().id("MTBLS400").
            idFormat(new Parameter().name("METABOLIGHTS")).
            title("Some external metabolights study.").
            version("1.0").
            url("https://www.ebi.ac.uk/metabolights/MTBLS400").
            format(new Parameter().name("ISATAB"));
        mtd.setStudy(es);
        try {
            mtd.addUriItem(new URI(
                "http://www.ebi.ac.uk/pride/url/to/experiment").toASCIIString());
            mtd.addUriItem(new URI(
                "http://proteomecentral.proteomexchange.org/cgi/GetDataset").
                toASCIIString());
        } catch (URISyntaxException ex) {
            Logger.getLogger(MzTabWriterTest.class.getName()).
                log(Level.SEVERE, null, ex);
        }
        MsRun msRun1 = new MsRun().id(1).
            location("file://ftp.ebi.ac.uk/path/to/file").
            idFormat(new Parameter().cvLabel("MS").
                cvAccession("MS:1001530").
                name(
                    "mzML unique identifier")).
            format(new Parameter().cvLabel("MS").
                cvAccession("MS:1000584").
                name("mzML file")).
            addFragmentationMethodItem(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000133").
                    name("CID"));
        mtd.addMsrunItem(msRun1);
        MsRun msRun2 = new MsRun().id(2).
            location("ftp://ftp.ebi.ac.uk/path/to/file").
            format(new Parameter().cvLabel("MS").
                cvAccession("MS:1001062").
                name("Mascot MGF file")).
            hash("de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3").
            hashMethod(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000569").
                    name("SHA-1")).
            addFragmentationMethodItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1000422").
                name("HCD"));
        mtd.addMsrunItem(msRun2);
        mtd.addCustomItem(new Parameter().id(1).
            name("MS operator").
            value("Florian"));

        Sample sample1 = new Sample().id(1).
            description("Hepatocellular carcinoma samples.").
            addSpeciesItem(new Parameter().cvLabel("NEWT").
                cvAccession("9606").
                name(
                    "Homo sapiens (Human)")).
            addSpeciesItem(new Parameter().cvLabel("NEWT").
                cvAccession("573824").
                name("Human rhinovirus 1")).
            addTissueItem(new Parameter().cvLabel("BTO").
                cvAccession("BTO:0000759").
                name("liver")).
            addCellTypeItem(new Parameter().cvLabel("CL").
                cvAccession("CL:0000182").
                name("hepatocyte")).
            addDiseaseItem(new Parameter().cvLabel("DOID").
                cvAccession("DOID:684").
                name("hepatocellular carcinoma")).
            addDiseaseItem(new Parameter().cvLabel("DOID").
                cvAccession("DOID:9451").
                name("alcoholic fatty liver")).
            addCustomItem(new Parameter().name("Extraction date").
                value("2011-12-21")).
            addCustomItem(new Parameter().name("Extraction reason").
                value("liver biopsy")).
            addIdConfidenceMeasureItem(new Parameter().name(
                "coelution with authentic standard"));
        mtd.addSampleItem(sample1);
        Sample sample2 = new Sample().id(2).
            name("Sample 2").
            description("Healthy control samples.").
            addSpeciesItem(new Parameter().cvLabel("NEWT").
                cvAccession("9606").
                name(
                    "Homo sapiens (Human)")).
            addSpeciesItem(new Parameter().cvLabel("NEWT").
                cvAccession("12130").
                name("Human rhinovirus 2")).
            addTissueItem(new Parameter().cvLabel("BTO").
                cvAccession("BTO:0000759").
                name("liver")).
            addCellTypeItem(new Parameter().cvLabel("CL").
                cvAccession("CL:0000182").
                name("hepatocyte")).
            addCustomItem(new Parameter().name("Extraction date").
                value("2011-12-19")).
            addCustomItem(new Parameter().name("Extraction reason").
                value("liver biopsy")).
            addIdConfidenceMeasureItem(new Parameter().name(
                "coelution with authentic standard"));
        mtd.addSampleItem(sample2);

        Assay assay1 = new Assay().id(1).
            name("Assay 1").
            msRunRef(msRun1).
            sampleRef(sample1);
        mtd.addAssayItem(assay1);
        Assay assay2 = new Assay().id(2).
            name("Assay 2").
            msRunRef(msRun2).
            sampleRef(sample2);
        mtd.addAssayItem(assay2);

        StudyVariable studyVariable1 = new StudyVariable().
            id(1).
            description(
                "Group A").
            addAssayRefsItem(
                assay1).
            addAssayRefsItem(assay2).
            addSampleRefsItem(sample1).
            addFactorsItem(new Parameter().name("spike-in").
                value("0.74 fmol/uL"));
        mtd.addStudyVariableItem(studyVariable1);
        StudyVariable studyVariable2 = new StudyVariable().
            id(2).
            description("Group B").
            addAssayRefsItem(assay1).
            addAssayRefsItem(assay2).
            addSampleRefsItem(sample2).
            addFactorsItem(new Parameter().name("spike-in").
                value("0.74 fmol/uL"));
        mtd.addStudyVariableItem(studyVariable2);
        mtd.addCvItem(new CV().id(1).
            label("MS").
            fullName("PSI-MS ontology").
            version("3.54.0").
            url("https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo"));
//
        mtd.addQuantificationMethodItem(new Parameter().cvLabel("MS").
            cvAccession("MS:1001837").
            name("iTRAQ quantitation analysis"));
        mtd.addQuantificationMethodItem(new Parameter().cvLabel("MS").
            cvAccession("MS:1001838").
            name("SRM quantitation analysis"));

        mtd.addIdConfidenceMeasureItem(new Parameter().id(1).
            name("some confidence measure term"));

        //column names can be defined as strings
        mtd.addColunitSmallMoleculeItem(new ColumnParameterMapping().columnName(
            "retention_time").
            param(new Parameter().id(1).
                cvLabel("UO").
                cvAccession("UO:0000031").
                name("minute")));
        //or via the respective enum member's getName() method, for fixed columns
        mtd.addColunitSmallMoleculeFeatureItem(new ColumnParameterMapping().
            columnName(SmallMoleculeFeatureColumn.Stable.RETENTION_TIME.
                getName()).
            param(new Parameter().id(1).
                cvLabel("UO").
                cvAccession("UO:0000031").
                name("minute")));

        mtd.addColunitSmallMoleculeEvidenceItem(new ColumnParameterMapping().
            columnName(SmallMoleculeEvidenceColumn.Stable.RANK.
                getName()).
            param(new Parameter().id(1).
                name("semi-stable sorted ascending order")));
//        mtd.addProteinColUnit(ProteinColumn.RELIABILITY, new Parameter("MS",
//                "MS:00001231", "PeptideProphet:Score", null));
//
//        MZTabColumnFactory peptideFactory = MZTabColumnFactory.getInstance(
//                Section.Peptide);
//        peptideFactory.addDefaultStableColumns();
//
//        PeptideColumn peptideColumn = (PeptideColumn) peptideFactory.
//                findColumnByHeader("retention_time");
//        mtd.addPeptideColUnit(peptideColumn, new Parameter("UO", "UO:0000031",
//                "minute", null));
//
//        mtd.addPSMColUnit(PSMColumn.RETENTION_TIME, new Parameter("UO",
//                "UO:0000031", "minute", null));
//
//        System.out.println(mtd);
        MzTab mzTab = new MzTab();
        mzTab.metadata(mtd);
        return mzTab;
    }

    @Test
    public void testWriteDefaultToString() {
        try (BufferedWriter bw = Files.newBufferedWriter(File.createTempFile(
            "testWriteDefaultToString", ".txt").
            toPath(), Charset.forName("UTF-8"), StandardOpenOption.WRITE)) {
            bw.write(create1_1TestFile().
                toString());
        } catch (IOException ex) {
            Logger.getLogger(MzTabWriterTest.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testWriteJsonMapper() {
        try (BufferedWriter bw = Files.newBufferedWriter(File.createTempFile(
            "testWriteJson", ".json").
            toPath(), Charset.forName("UTF-8"), StandardOpenOption.WRITE)) {
            ObjectMapper mapper = new ObjectMapper();
            bw.write(mapper.writeValueAsString(create1_1TestFile()));
        } catch (IOException ex) {
            Logger.getLogger(MzTabWriterTest.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testCvParameterToString() {
        Parameter p = new Parameter().cvLabel("MS").
            cvAccession("MS:100179").
            name("made up for testing").
            value(null);
        String s = ParameterSerializer.toString(p);
        System.out.println(s);
        String expected = "[MS, MS:100179, made up for testing, ]";
        Assert.assertEquals(expected, s);
        p.value("some value");
        s = ParameterSerializer.toString(p);
        System.out.println(s);
        expected = "[MS, MS:100179, made up for testing, some value]";
        Assert.assertEquals(expected, s);
    }

    @Test
    public void testUserParameterToString() {
        Parameter p = new Parameter().name("made up for testing").
            value("some arbitrary value");
        String s = ParameterSerializer.toString(p);
        System.out.println(s);
        String expected = "[, , made up for testing, some arbitrary value]";
        Assert.assertEquals(expected, s);
    }

    @Test
    public void testWriteMetadataToTsvWithJackson() {
        MzTab mzTabFile = create1_1TestFile();
        String metadata = new MzTabWriter().writeMetadataWithJackson(mzTabFile);
        System.out.println("Serialized Metadata: ");
        System.out.println(metadata);
    }
    
    @Test
    public void testWriteSmallMoleculeSummaryToTsvWithJackson() {
        MzTab mzTabFile = create1_1TestFile();
        String smallMoleculeSummary = new MzTabWriter().
            writeSmallMoleculeSummaryWithJackson(mzTabFile);
        System.out.println("Serialized SmallMoleculeSummary: ");
        System.out.println(smallMoleculeSummary);
    }
    
    @Test
    public void testWriteSmallMoleculeFeaturesToTsvWithJackson() {
        MzTab mzTabFile = create1_1TestFile();
        String smallMoleculeFeatures = new MzTabWriter().
            writeSmallMoleculeFeaturesWithJackson(mzTabFile);
        System.out.println("Serialized SmallMoleculeFeatures: ");
        System.out.println(smallMoleculeFeatures);
    }
    
    @Test
    public void testWriteSmallMoleculeEvidenceToTsvWithJackson() {
        MzTab mzTabFile = create1_1TestFile();
        String smallMoleculeEvidences = new MzTabWriter().
            writeSmallMoleculeEvidenceWithJackson(mzTabFile);
        System.out.println("Serialized SmallMoleculeEvidence: ");
        System.out.println(smallMoleculeEvidences);
    }
    
    @Test
    public void testWriteMzTabToTsvWithJackson() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        MzTabWriter writer = new MzTabWriter();
        
        writer.write(new OutputStreamWriter(System.out, Charset.forName(
            "UTF-8")), mzTabFile);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWriteMzTabToTsvWithJacksonInvalidEncoding() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        MzTabWriter writer = new MzTabWriter();
        writer.write(new OutputStreamWriter(System.out, Charset.forName("ISO-8859-15")), mzTabFile);
    }

//    @Test
//    public void testReadWriteRoundtripWithJackson() throws IOException, URISyntaxException {
//        MzTab mzTabFile = MzTabRawParserTest.parseResource(
//            "metabolomics/lipidomics-example.mzTab");
//        String metadata = MzTabWriter.writeMetadataWithJackson(mzTabFile);
//        System.out.println("Serialized Metadata: ");
//        System.out.println(metadata);
//
//        String smallMoleculeSummary = MzTabWriter.
//            writeSmallMoleculeSummaryWithJackson(mzTabFile);
//        System.out.println("Serialized SmallMoleculeSummary: ");
//        System.out.println(smallMoleculeSummary);
//
//        String smallMoleculeFeatures = MzTabWriter.
//            writeSmallMoleculeFeaturesWithJackson(mzTabFile);
//        System.out.println("Serialized SmallMoleculeFeatures: ");
//        System.out.println(smallMoleculeFeatures);
//
//        String smallMoleculeEvidences = MzTabWriter.
//            writeSmallMoleculeEvidenceWithJackson(mzTabFile);
//        System.out.println("Serialized SmallMoleculeEvidence: ");
//        System.out.println(smallMoleculeEvidences);
//    }
}
