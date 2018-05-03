package de.isas.lipidomics.jmztabm.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import de.isas.lipidomics.jmztabm.io.serialization.ParameterSerializer;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.ColumnParameterMapping;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Database;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.PublicationItem;
import de.isas.mztab1_1.model.Sample;
import de.isas.mztab1_1.model.SampleProcessing;
import de.isas.mztab1_1.model.SmallMoleculeEvidence;
import de.isas.mztab1_1.model.SmallMoleculeFeature;
import de.isas.mztab1_1.model.SmallMoleculeSummary;
import de.isas.mztab1_1.model.Software;
import de.isas.mztab1_1.model.StudyVariable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import de.isas.mztab.jmztabm.test.utils.LogMethodName;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab1_1.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;

/**
 * Test class for MzTabWriter.
 *
 * @author nilshoffmann
 */
public class MzTabWriterTest {

    @Rule
    public LogMethodName methodNameLogger = new LogMethodName();

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
//TODO renable when external study has been approved
//        ExternalStudy es = new ExternalStudy().id("MTBLS400").
//            idFormat(new Parameter().name("METABOLIGHTS")).
//            title("Some external metabolights study.").
//            version("1.0").
//            url("https://www.ebi.ac.uk/metabolights/MTBLS400").
//            format(new Parameter().name("ISATAB"));
//        mtd.setStudy(es);
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
            columnName(SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS.
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
    public void testWriteMetadataToTsvWithJackson() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        try (StringWriter sw = new StringWriter()) {
            new MzTabWriter().writeMetadataWithJackson(mzTabFile, sw);
            System.out.println("Serialized Metadata: ");
            sw.flush();
            String metadataString = sw.toString();
            System.out.println(metadataString);
            Assert.assertFalse(metadataString.isEmpty());
        }
    }

    @Test
    public void testWriteSmallMoleculeSummaryToTsvWithJackson() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        SmallMoleculeSummary smsi = new SmallMoleculeSummary();
        smsi.smlId("" + 1).
            smfIdRefs(Arrays.asList("" + 1, "" + 2, "" + 3, "" + 4, "" + 5)).
            chemicalName(Arrays.asList("Cer(d18:1/24:0)",
                "N-(tetracosanoyl)-sphing-4-enine", "C24 Cer")).
            addOptItem(new OptColumnMapping().identifier("global").
                value("lipid_category").
                param(new Parameter().cvLabel("LM").
                    cvAccession("LM:SP").
                    name("Category").
                    value("Sphingolipids"))).
            addOptItem(new OptColumnMapping().identifier("global").
                value("lipid_species").
                param(new Parameter().cvLabel("LH").
                    cvAccession("LH:XXXXX").
                    name("Species").
                    value("Cer 42:1"))).
            addOptItem(new OptColumnMapping().identifier("global").
                value("lipid_best_id_level").
                param(new Parameter().cvLabel("LH").
                    cvAccession("LH:XXXXX").
                    name("Sub Species").
                    value("Cer d18:1/24:0"))).
            addDatabaseIdentifierItem("LM:LMSP02010012").
            addChemicalFormulaItem("C42H83NO3").
            addSmilesItem(
                "CCCCCCCCCCCCCCCCCCCCCCCC(=O)N[C@@H](CO)[C@H](O)/C=C/CCCCCCCCCCCCC").
            addInchiItem(
                "InChI=1S/C42H83NO3/c1-3-5-7-9-11-13-15-17-18-19-20-21-22-23-24-26-28-30-32-34-36-38-42(46)43-40(39-44)41(45)37-35-33-31-29-27-25-16-14-12-10-8-6-4-2/h35,37,40-41,44-45H,3-34,36,38-39H2,1-2H3,(H,43,46)/b37-35+/t40-,41+/m0/s1").
            addUriItem(
                "http://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012").
            addTheoreticalNeutralMassItem(649.6373).
            expMassToCharge(650.6432).
            retentionTimeInSeconds(821.2341).
            addAdductIonsItem("[M+H]1+").
            reliability("1").
            bestIdConfidenceMeasure(new Parameter().name(
                "qualifier ions exact mass")).
            bestIdConfidenceValue(0.958).
            addAbundanceAssayItem(4.448784E-05).
            addAbundanceAssayItem(5.448784E-05).
            addAbundanceStudyVariableItem(4.448784E-05).
            addAbundanceStudyVariableItem(5.448784E-05).
            addAbundanceCoeffvarStudyVariableItem(0.0d).
            addAbundanceCoeffvarStudyVariableItem(0.00001d);
        mzTabFile.addSmallMoleculeSummaryItem(smsi);
        try (StringWriter sw = new StringWriter()) {
            new MzTabWriter().
                writeSmallMoleculeSummaryWithJackson(mzTabFile, sw);
            System.out.println("Serialized SmallMoleculeSummary: ");
            sw.flush();
            String smallMoleculeSummary = sw.toString();
            System.out.println(smallMoleculeSummary);
            Assert.assertFalse(smallMoleculeSummary.isEmpty());
            //check for exactly one header line and one content line
            Assert.assertEquals(2,
                smallMoleculeSummary.split("\r\n|\r|\n").length);
        }
    }

    @Test
    public void testWriteSmallMoleculeSummaryWithNullToTsvWithJackson() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        SmallMoleculeSummary smsi = new SmallMoleculeSummary();
        smsi.smlId("" + 1).
            smfIdRefs(Arrays.asList("" + 1, "" + 2, "" + 3, "" + 4, "" + 5)).
            chemicalName(Arrays.asList("Cer(d18:1/24:0)",
                "N-(tetracosanoyl)-sphing-4-enine", "C24 Cer")).
            addOptItem(new OptColumnMapping().identifier("global").
                value("lipid_category").
                param(new Parameter().cvLabel("LM").
                    cvAccession("LM:SP").
                    name("Category").
                    value("Sphingolipids"))).
            addOptItem(new OptColumnMapping().identifier("global").
                value("lipid_species").
                param(new Parameter().cvLabel("LH").
                    cvAccession("LH:XXXXX").
                    name("Species").
                    value("Cer 42:1"))).
            addOptItem(new OptColumnMapping().identifier("global").
                value("lipid_best_id_level").
                param(new Parameter().cvLabel("LH").
                    cvAccession("LH:XXXXX").
                    name("Sub Species").
                    value("Cer d18:1/24:0"))).
            addDatabaseIdentifierItem("LM:LMSP02010012").
            //addChemicalFormulaItem("C42H83NO3").
            //addSmilesItem(
            //    "CCCCCCCCCCCCCCCCCCCCCCCC(=O)N[C@@H](CO)[C@H](O)/C=C/CCCCCCCCCCCCC").
            addInchiItem(
                "InChI=1S/C42H83NO3/c1-3-5-7-9-11-13-15-17-18-19-20-21-22-23-24-26-28-30-32-34-36-38-42(46)43-40(39-44)41(45)37-35-33-31-29-27-25-16-14-12-10-8-6-4-2/h35,37,40-41,44-45H,3-34,36,38-39H2,1-2H3,(H,43,46)/b37-35+/t40-,41+/m0/s1").
            addUriItem(
                "http://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012").
            addTheoreticalNeutralMassItem(649.6373).
            //            expMassToCharge(650.6432).
            retentionTimeInSeconds(821.2341).
            addAdductIonsItem("[M+H]1+").
            reliability("1").
            bestIdConfidenceMeasure(new Parameter().name(
                "qualifier ions exact mass")).
            bestIdConfidenceValue(0.958).
            addAbundanceAssayItem(4.448784E-05).
            addAbundanceAssayItem(null).
            addAbundanceStudyVariableItem(4.448784E-05).
            addAbundanceStudyVariableItem(null).
            addAbundanceCoeffvarStudyVariableItem(0.0d).
            addAbundanceCoeffvarStudyVariableItem(0.00001d);
        mzTabFile.addSmallMoleculeSummaryItem(smsi);
        try (StringWriter sw = new StringWriter()) {
            new MzTabWriter().
                writeSmallMoleculeSummaryWithJackson(mzTabFile, sw);
            sw.flush();
            String smallMoleculeSummary = sw.toString();
            System.out.println("Serialized SmallMoleculeSummary: ");
            System.out.println(smallMoleculeSummary);
            Assert.assertFalse(smallMoleculeSummary.isEmpty());
            //check for exactly one header and one content line
            Assert.assertEquals(2,
                smallMoleculeSummary.split("\r\n|\r|\n").length);
        }

    }

    @Test
    public void testWriteSmallMoleculeFeaturesToTsvWithJackson() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        try (StringWriter sw = new StringWriter()) {
            new MzTabWriter().
                writeSmallMoleculeFeaturesWithJackson(mzTabFile, sw);
            sw.flush();
            String smallMoleculeFeatures = sw.toString();
            System.out.println("Serialized SmallMoleculeFeatures: ");
            System.out.println(smallMoleculeFeatures);
            Assert.assertFalse(smallMoleculeFeatures.isEmpty());
            //check for exactly one header line
            Assert.assertEquals(1,
                smallMoleculeFeatures.split("\r\n|\r|\n").length);
        }
    }

    @Test
    public void testWriteSmallMoleculeEvidenceToTsvWithJackson() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        try (StringWriter sw = new StringWriter()) {
            new MzTabWriter().
                writeSmallMoleculeEvidenceWithJackson(mzTabFile, sw);
            sw.flush();
            String smallMoleculeEvidences = sw.toString();
            System.out.println("Serialized SmallMoleculeEvidence: ");
            System.out.println(smallMoleculeEvidences);
            Assert.assertFalse(smallMoleculeEvidences.isEmpty());
            //check for exactly one header line
            Assert.assertEquals(1,
                smallMoleculeEvidences.split("\r\n|\r|\n").length);
        }
    }

    @Test
    public void testWriteMzTabToTsvWithJackson() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        MzTabWriter writer = new MzTabWriter();
        try (OutputStreamWriter osw = new OutputStreamWriter(System.out,
            Charset.forName(
                "UTF-8"))) {
            writer.write(osw, mzTabFile);
        }
    }

    @Test
    public void testWriteMzTabToTsvWithJacksonForPath() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        MzTabWriter writer = new MzTabWriter();
        File tempFile = File.createTempFile("mzTabWriterTest", ".mztab");
        writer.write(tempFile.toPath(), mzTabFile);
        MZTabFileParser parser = new MZTabFileParser(tempFile);
        MZTabErrorList errors = parser.parse(System.out,
            MZTabErrorType.Level.Info, 500);
        //we expect errors here, since our test file has neither summary, feature nor evidence sections.
        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(1, errors.size());
//        MzTab mzTabReRead = parser.getMZTabFile();
//        Assert.assertEquals(mzTabFile, mzTabReRead);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMzTabToTsvWithJacksonInvalidEncoding() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        MzTabWriter writer = new MzTabWriter();
        writer.write(new OutputStreamWriter(System.out, Charset.forName(
            "ISO-8859-15")), mzTabFile);
    }

    @Test
    public void testReadWriteRoundtripWithJackson() throws IOException, URISyntaxException, MZTabException {
        MzTab mzTabFile = MzTabRawParserTest.parseResource(
            "metabolomics/lipidomics-example.mzTab", MZTabErrorType.Level.Info,
            0);
        File tempFile = File.createTempFile("testReadWriteRoundtripWithJackson",
            ".mztab");
        MzTabWriter writer = new MzTabWriter();
        writer.write(tempFile.toPath(), mzTabFile);
        MZTabFileParser parser = new MZTabFileParser(tempFile);
        MZTabErrorList errors = parser.parse(System.out,
            MZTabErrorType.Level.Info, 500);
        Assert.assertNull(parser.getMZTabFile().
            getMetadata().
            getColunitSmallMolecule());
        Assert.assertNotNull(parser.getMZTabFile().
            getMetadata().
            getColunitSmallMoleculeEvidence());
        Assert.assertEquals(1, parser.getMZTabFile().
            getMetadata().
            getColunitSmallMoleculeEvidence().
            size());
        Assert.assertTrue(errors.isEmpty());
        //TODO we can not use equals, since comments are not preserved during writing
        //Assert.assertEquals(mzTabFile, parser.getMZTabFile());
        compareMzTabModels(mzTabFile, parser.getMZTabFile());
    }

    @Test
    public void testReadWriteRoundtripWithJacksonMTBLS263() throws IOException, URISyntaxException, MZTabException {
        MzTab mzTabFile = MzTabRawParserTest.parseResource(
            "metabolomics/MTBLS263.mztab", MZTabErrorType.Level.Info,
            0);
        File tempFile = File.createTempFile(
            "testReadWriteRoundtripWithJacksonMTBLS263",
            ".mztab");
        MzTabWriter writer = new MzTabWriter();
        writer.write(tempFile.toPath(), mzTabFile);
        MZTabFileParser parser = new MZTabFileParser(tempFile);
        MZTabErrorList errors = parser.parse(System.out,
            MZTabErrorType.Level.Info, 500);
        Assert.assertTrue(errors.isEmpty());
        compareMzTabModels(mzTabFile, parser.getMZTabFile());
    }

    void compareMzTabModels(MzTab model1, MzTab model2
    ) {
        Assert.assertEquals(model1.getMetadata(), model2.getMetadata());
        Assert.assertEquals(model1.getSmallMoleculeSummary(), model2.
            getSmallMoleculeSummary());
        Assert.assertEquals(model1.getSmallMoleculeFeature(), model2.
            getSmallMoleculeFeature());
        Assert.assertEquals(model1.getSmallMoleculeEvidence(), model2.
            getSmallMoleculeEvidence());
    }

    @Test
    public void testLargeNumberOfFeaturesFromMTBLS263() throws URISyntaxException, IOException, MZTabException {
        MzTab mzTabFile = MzTabRawParserTest.parseResource(
            "metabolomics/MTBLS263.mztab", MZTabErrorType.Level.Info,
            0);
        final int expFactor = 100;
        List<SmallMoleculeSummary> sms = mzTabFile.getSmallMoleculeSummary();
        final AtomicInteger smsIds = new AtomicInteger(sms.size());
        List<SmallMoleculeSummary> newSms = new ArrayList<>(sms.size() * 5);
        newSms.addAll(sms);
        sms.forEach((smallMolecule) ->
        {
            for (int i = 0; i < expFactor; i++) {
                try {
                    SmallMoleculeSummary clone = clone(
                        SmallMoleculeSummary.class, smallMolecule);
                    clone.setSmlId((smsIds.get() + i) + "");
                    newSms.add(clone);
                } catch (IOException ex) {
                    Logger.getLogger(MzTabWriterTest.class.getName()).
                        log(Level.SEVERE, null, ex);
                }
            }
            smsIds.addAndGet(expFactor);
        });
        mzTabFile.setSmallMoleculeSummary(newSms);

        List<SmallMoleculeFeature> smf = mzTabFile.getSmallMoleculeFeature();
        final AtomicInteger smfIds = new AtomicInteger(smf.size());
        List<SmallMoleculeFeature> newSmf = new ArrayList<>(
            smf.size() * expFactor);
        newSmf.addAll(smf);
        smf.forEach((smallMoleculeFeature) ->
        {
            for (int i = 0; i < expFactor; i++) {
                try {
                    SmallMoleculeFeature clone = clone(
                        SmallMoleculeFeature.class, smallMoleculeFeature);
                    clone.setSmfId((smfIds.get() + i) + "");
                    newSmf.add(clone);
                } catch (IOException ex) {
                    Logger.getLogger(MzTabWriterTest.class.getName()).
                        log(Level.SEVERE, null, ex);
                }
            }
            smfIds.addAndGet(expFactor);
        });
        mzTabFile.setSmallMoleculeFeature(newSmf);

        List<SmallMoleculeEvidence> sme = mzTabFile.getSmallMoleculeEvidence();
        final AtomicInteger smeIds = new AtomicInteger(sme.size());
        List<SmallMoleculeEvidence> newSme = new ArrayList<>(
            sme.size() * expFactor);
        newSme.addAll(sme);
        sme.forEach((smallMoleculeEvidence) ->
        {
            for (int i = 0; i < expFactor; i++) {
                try {
                    SmallMoleculeEvidence clone = clone(
                        SmallMoleculeEvidence.class, smallMoleculeEvidence);
                    clone.setSmeId((smeIds.get() + i) + "");
                    newSme.add(clone);
                } catch (IOException ex) {
                    Logger.getLogger(MzTabWriterTest.class.getName()).
                        log(Level.SEVERE, null, ex);
                }
            }
            smeIds.addAndGet(expFactor);
        });
        mzTabFile.setSmallMoleculeEvidence(newSme);

        File tempFile = File.createTempFile(
            "testLargeNumberOfFeaturesFromMTBLS263",
            ".mztab");
        MzTabWriter writer = new MzTabWriter();
        writer.write(tempFile.toPath(), mzTabFile);
        long lines = Files.lines(tempFile.toPath()).
            count();
        Assert.assertEquals(
            3 + // additional empty lines
            53
            +//metadata lines
            1 + 17
            +//summary header and original data
            (17 * expFactor)
            +//cloned summary data 
            1 + 19
            + //feature header and original data
            (19 * expFactor)
            +//cloned feature data 
            1 + 19
            + //evidence header and original data
            (19 * expFactor) //cloned evidence data
            ,
             lines);

    }

    <T> T clone(Class<? extends T> clazz, T t) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        String jsonSource = mapper.writeValueAsString(t);
        return mapper.readValue(jsonSource, clazz);
    }

    @Test
    public void testSmlNullEmptyHandling() throws IOException {
        MzTab mztab = new MzTab();
        Metadata mtd = new Metadata();
        mtd.mzTabVersion("1.1");
        mtd.mzTabID("1");
        for(int i = 1; i<=10; i++) {
            Contact c = new Contact().id(i).name("C"+i).email("c"+i+"@email.com").affiliation("A"+i);
            mtd.addContactsItem(c);
        }
        mtd.addQuantificationMethodItem(new Parameter().id(1).cvLabel("MS").cvAccession("MS:1002038").name("unlabeled sample"));
        mtd.addSoftwareItem(new Software().id(1).parameter(new Parameter().id(1).name("LipidDataAnalyzer").value("2.6.3_nightly")));
        MsRun msrun1 = new MsRun().id(1).location("file://D:/Experiment1/Orbitrap_CID/negative/50/014_Ex1_Orbitrap_CID_neg_50.chrom");
        MsRun msrun2 = new MsRun().id(2).location("file://D:/Experiment1/Orbitrap_CID/negative/50/015_Ex1_Orbitrap_CID_neg_50.chrom");
        MsRun msrun3 = new MsRun().id(3).location("file://D:/Experiment1/Orbitrap_CID/negative/50/016_Ex1_Orbitrap_CID_neg_50.chrom");
        MsRun msrun4 = new MsRun().id(4).location("file://D:/Experiment1/Orbitrap_CID/negative/50/017_Ex1_Orbitrap_CID_neg_50.chrom");
        MsRun msrun5 = new MsRun().id(5).location("file://D:/Experiment1/Orbitrap_CID/negative/50/018_Ex1_Orbitrap_CID_neg_50.chrom");
        mtd.addMsrunItem(msrun1).addMsrunItem(msrun2).addMsrunItem(msrun3).addMsrunItem(msrun4).addMsrunItem(msrun5);
        Assay a1 = new Assay().id(1).msRunRef(msrun1);
        Assay a2 = new Assay().id(2).msRunRef(msrun2);
        Assay a3 = new Assay().id(3).msRunRef(msrun3);
        Assay a4 = new Assay().id(4).msRunRef(msrun4);
        Assay a5 = new Assay().id(5).msRunRef(msrun5);
        mtd.addAssayItem(a1).addAssayItem(a2).addAssayItem(a3).addAssayItem(a4).addAssayItem(a5);
        StudyVariable sv1 = new StudyVariable().id(1).name("Group 1").description("Group 1");
        sv1.addAssayRefsItem(a1).addAssayRefsItem(a2).addAssayRefsItem(a3);
        StudyVariable sv2 = new StudyVariable().id(2).name("Group 2").description("Group 2");
        sv2.addAssayRefsItem(a4).addAssayRefsItem(a5);
        mtd.addStudyVariableItem(sv1).addStudyVariableItem(sv2);
        mtd.addCvItem(new CV().id(1).label("MS").url("https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo").version("4.0.9").fullName("PSI-MS controlled vocabulary"));
        mtd.addDatabaseItem(new Database().id(1).prefix("nd").url("none").version("none").param(new Parameter().name("no database").value(null)));
        mtd.addColunitSmallMoleculeItem(new ColumnParameterMapping().columnName(
            "retention_time").param(new Parameter().cvLabel("UO").cvAccession("UO:0000031").name("minute")));
        mztab.metadata(mtd);
        
        SmallMoleculeSummary summary = new SmallMoleculeSummary();
        summary.setSmlId(null);
        summary.setSmfIdRefs(new ArrayList<String>());
        summary.setDatabaseIdentifier(new ArrayList<String>());
        summary.setSmiles(new ArrayList<String>());
        summary.setInchi(new ArrayList<String>());
        summary.setChemicalName(new ArrayList<String>());
        summary.setUri(new ArrayList<String>());
        summary.setExpMassToCharge(null);
        summary.setRetentionTimeInSeconds(123.414d);
        List<String> adducts = new ArrayList<String>();
        adducts.add("[M+H]1+");
        summary.setAdductIons(adducts);
        summary.setReliability("2");
        summary.setBestIdConfidenceMeasure(new Parameter().cvLabel("PRIDE").
            cvAccession("PRIDE:0000330").
            name("Arbitrary quantification unit"));
        summary.setBestIdConfidenceValue(0.02d);
        List<Double> abundanceAssay = new ArrayList<Double>();
        for (Assay assay : mtd.getAssay()) {
            abundanceAssay.add(assay.getId().doubleValue()+0.1d);
        }
        summary.setAbundanceAssay(abundanceAssay);
        List<Double> abundanceStudyVariable = new ArrayList<Double>();
        List<Double> abundanceCoeffvarStudyVariable = new ArrayList<Double>();
        for (StudyVariable sv : mtd.getStudyVariable()) {
            abundanceStudyVariable.add(sv.getId().doubleValue()+0.2d);
            abundanceCoeffvarStudyVariable.add(sv.getId().doubleValue()+0.1d);
        }
        summary.setAbundanceStudyVariable(abundanceStudyVariable);
        summary.
            setAbundanceCoeffvarStudyVariable(abundanceCoeffvarStudyVariable);
        mztab.addSmallMoleculeSummaryItem(summary);
        
        
        File tempFile = File.createTempFile(
            "testSmlNullEmptyHandling",
            ".mztab");
        MzTabWriter writer = new MzTabWriter();
        writer.write(tempFile.toPath(), mztab);
        long lines = Files.lines(tempFile.toPath()).
            count();
        Assert.assertEquals(
            3 + //additional empty lines 
            59
            +//metadata lines
            2 //Small molecule summary header + content
            + 1 // Small molecule feature header
            + 1 // Small molecule evidence header
            ,
             lines);
    }
}
