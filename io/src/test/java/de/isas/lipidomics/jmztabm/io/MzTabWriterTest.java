package de.isas.lipidomics.jmztabm.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
import static de.isas.lipidomics.jmztabm.io.MzTabWriter.SEP;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.ParameterList;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.PublicationItem;
import de.isas.mztab1_1.model.Sample;
import de.isas.mztab1_1.model.Software;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aopalliance.reflect.Metadata;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for MzTabWriter.
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabWriterTest {

    static MzTab createTestFile() {

        final MzTab mztabfile = new MzTab().metadata(
                new de.isas.mztab1_1.model.Metadata().
                        mzTabVersion("1.1.0").
                        mzTabID("ISAS_2017_M_11451").
                        title("A minimal test file").
                        description("A description of an mzTab file.").
                        addContactsItem(
                                new Contact().
                                        name("Nils Hoffmann").
                                        email("nils.hoffmann_at_isas.de").
                                        affiliation(
                                                "ISAS e.V. Dortmund, Germany")
                        ).
                        addMsrunItem(
                                new MsRun().
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
        return mztabfile;
    }

    static MzTab create1_0TestFile() {
        de.isas.mztab1_1.model.Metadata mtd = new de.isas.mztab1_1.model.Metadata();
        mtd.setMzTabID("PRIDE_1234");

        mtd.setTitle("My first test experiment");
        mtd.setDescription("An experiment investigating the effects of Il-6.");
        ParameterList list1 = new ParameterList();
        list1.add(new Parameter().cvLabel("SEP").
                cvAccession("SEP:00173").
                name("SDS PAGE").
                value(null));
        mtd.addSampleProcessingItem(list1);
        ParameterList list2 = new ParameterList();
        list2.add(new Parameter().cvLabel("SEP").
                cvAccession("SEP:00142").
                name("enzyme digestion").
                value(null));
        list2.add(new Parameter().cvLabel("MS").
                cvAccession("MS:1001251").
                name("Trypsin").
                value(null));

        Instrument instrument1 = new Instrument().instrumentName(
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
        Instrument instrument2 = new Instrument().instrumentName(
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
        Software software1 = new Software().parameter(new Parameter().cvLabel(
                "MS").
                cvAccession("MS:1001207").
                name("Mascot").
                value("2.3")).
                setting(Arrays.asList("Fragment tolerance = 0.1Da",
                        "Parent tolerance = 0.5Da"));
        mtd.addSoftwareItem(software1);

//        mtd.addProteinSearchEngineScoreParam(1, new CVParam("MS", "MS:1001171",
//                "Mascot:score", null));
//        mtd.addPeptideSearchEngineScoreParam(1, new CVParam("MS", "MS:1001153",
//                "search engine specific score", null));
//        mtd.addSmallMoleculeSearchEngineScoreParam(1, new CVParam("MS",
//                "MS:1001420", "SpectraST:delta", null));
//
//        mtd.addPsmSearchEngineScoreParam(1, new CVParam("MS", "MS:1001330",
//                "X!Tandem:expect", null));
//        mtd.addPsmSearchEngineScoreParam(2, new CVParam("MS", "MS:1001331",
//                "X!Tandem:hyperscore", null));
//
//        mtd.addFalseDiscoveryRateParam(new CVParam("MS", "MS:1001364",
//                "pep:global FDR", "0.01"));
//        mtd.addFalseDiscoveryRateParam(new CVParam("MS", "MS:1001214",
//                "pep:global FDR", "0.08"));
        PublicationItem item1_1 = new PublicationItem().type(
                PublicationItem.TypeEnum.PUBMED).
                accession("21063943");
        PublicationItem item1_2 = new PublicationItem().type(
                PublicationItem.TypeEnum.DOI).
                accession("10.1007/978-1-60761-987-1_6");
        Publication publication1 = new Publication();
        publication1.addAll(Arrays.asList(item1_1, item1_2));

        PublicationItem item2_1 = new PublicationItem().type(
                PublicationItem.TypeEnum.PUBMED).
                accession("20615486");
        PublicationItem item2_2 = new PublicationItem().type(
                PublicationItem.TypeEnum.DOI).
                accession("10.1016/j.jprot.2010.06.008");
        Publication publication2 = new Publication();
        publication2.addAll(Arrays.asList(item2_1, item2_2));

        mtd.addPublicationsItem(publication1).
                addPublicationsItem(publication2);
        
        mtd.addContactsItem(new Contact().name("James D. Watson").affiliation("Cambridge University, UK").email("watson@cam.ac.uk"));
        mtd.addContactsItem(new Contact().name("Francis Crick").affiliation("Cambridge University, UK").email("crick@cam.ac.uk"));

//        mtd.addUriItem(new URI("http://www.ebi.ac.uk/pride/url/to/experiment"));
//        mtd.addUriItem(new URI(
//                "http://proteomecentral.proteomexchange.org/cgi/GetDataset"));
//
//        mtd.addFixedModParam(1, new CVParam("UNIMOD", "UNIMOD:4",
//                "Carbamidomethyl", null));
//        mtd.addFixedModSite(1, "M");
//        mtd.addFixedModParam(2, new CVParam("UNIMOD", "UNIMOD:35", "Oxidation",
//                null));
//        mtd.addFixedModSite(2, "N-term");
//        mtd.addFixedModParam(3,
//                new CVParam("UNIMOD", "UNIMOD:1", "Acetyl", null));
//        mtd.addFixedModPosition(3, "Protein C-term");
//
//        mtd.addVariableModParam(1, new CVParam("UNIMOD", "UNIMOD:21", "Phospho",
//                null));
//        mtd.addVariableModSite(1, "M");
//        mtd.addVariableModParam(2, new CVParam("UNIMOD", "UNIMOD:35",
//                "Oxidation", null));
//        mtd.addVariableModSite(2, "N-term");
//        mtd.addVariableModParam(3, new CVParam("UNIMOD", "UNIMOD:1", "Acetyl",
//                null));
//        mtd.addVariableModPosition(3, "Protein C-term");
//
//        mtd.setQuantificationMethod(new CVParam("MS", "MS:1001837",
//                "iTRAQ quantitation analysis", null));
//        mtd.setProteinQuantificationUnit(new CVParam("PRIDE", "PRIDE:0000395",
//                "Ratio", null));
//        mtd.setPeptideQuantificationUnit(new CVParam("PRIDE", "PRIDE:0000395",
//                "Ratio", null));
//        mtd.setSmallMoleculeQuantificationUnit(new CVParam("PRIDE",
//                "PRIDE:0000395", "Ratio", null));
//
//        mtd.
//                addMsRunFormat(1, new CVParam("MS", "MS:1000584", "mzML file",
//                        null));
//        mtd.addMsRunFormat(2, new CVParam("MS", "MS:1001062", "Mascot MGF file",
//                null));
//        mtd.addMsRunLocation(1, new URL("file://ftp.ebi.ac.uk/path/to/file"));
//        mtd.addMsRunLocation(2, new URL("ftp://ftp.ebi.ac.uk/path/to/file"));
//        mtd.addMsRunIdFormat(1, new CVParam("MS", "MS:1001530",
//                "mzML unique identifier", null));
//        mtd.addMsRunFragmentationMethod(1,
//                new CVParam("MS", "MS:1000133", "CID", null));
//        mtd.addMsRunHash(2, "de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3");
//        mtd.
//                addMsRunHashMethod(2, new CVParam("MS", "MS:1000569", "SHA-1",
//                        null));
////        mtd.addMsRunFragmentationMethod(2, new CVParam("MS", "MS:1000422", "HCD", null));
//
//        mtd.addCustom(new UserParam("MS operator", "Florian"));
//
//        mtd.addSampleSpecies(1, new CVParam("NEWT", "9606",
//                "Homo sapiens (Human)", null));
//        mtd.addSampleSpecies(1, new CVParam("NEWT", "573824",
//                "Human rhinovirus 1", null));
//        mtd.addSampleSpecies(2, new CVParam("NEWT", "9606",
//                "Homo sapiens (Human)", null));
//        mtd.addSampleSpecies(2, new CVParam("NEWT", "12130",
//                "Human rhinovirus 2", null));
//        mtd.addSampleTissue(1, new CVParam("BTO", "BTO:0000759", "liver", null));
//        mtd.addSampleCellType(1, new CVParam("CL", "CL:0000182", "hepatocyte",
//                null));
//        mtd.addSampleDisease(1, new CVParam("DOID", "DOID:684",
//                "hepatocellular carcinoma", null));
//        mtd.addSampleDisease(1, new CVParam("DOID", "DOID:9451",
//                "alcoholic fatty liver", null));
//        mtd.addSampleDescription(1, "Hepatocellular carcinoma samples.");
//        mtd.addSampleDescription(2, "Healthy control samples.");
//        mtd.addSampleCustom(1, new UserParam("Extraction date", "2011-12-21"));
//        mtd.addSampleCustom(1,
//                new UserParam("Extraction reason", "liver biopsy"));
//
//        Sample sample1 = mtd.getSampleMap().
//                get(1);
//        Sample sample2 = mtd.getSampleMap().
//                get(2);
//        mtd.addAssayQuantificationReagent(1, new CVParam("PRIDE",
//                "PRIDE:0000114", "iTRAQ reagent", "114"));
//        mtd.addAssayQuantificationReagent(2, new CVParam("PRIDE",
//                "PRIDE:0000115", "iTRAQ reagent", "115"));
//        mtd.addAssayQuantificationReagent(1, new CVParam("PRIDE", "MS:1002038",
//                "unlabeled sample", null));
//        mtd.addAssaySample(1, sample1);
//        mtd.addAssaySample(2, sample2);
//
//        mtd.addAssayQuantificationModParam(2, 1, new CVParam("UNIMOD",
//                "UNIMOD:188", "Label:13C(6)", null));
//        mtd.addAssayQuantificationModParam(2, 2, new CVParam("UNIMOD",
//                "UNIMOD:188", "Label:13C(6)", null));
//        mtd.addAssayQuantificationModSite(2, 1, "R");
//        mtd.addAssayQuantificationModSite(2, 2, "K");
//        mtd.addAssayQuantificationModPosition(2, 1, "Anywhere");
//        mtd.addAssayQuantificationModPosition(2, 2, "Anywhere");
//
//        MsRun msRun1 = mtd.getMsRunMap().
//                get(1);
//        mtd.addAssayMsRun(1, msRun1);
//
//        Assay assay1 = mtd.getAssayMap().
//                get(1);
//        Assay assay2 = mtd.getAssayMap().
//                get(2);
//
//        mtd.addStudyVariableAssay(1, assay1);
//        mtd.addStudyVariableAssay(1, assay2);
//        mtd.addStudyVariableSample(1, sample1);
//        mtd.addStudyVariableDescription(1,
//                "description Group B (spike-in 0.74 fmol/uL)");
//
//        mtd.addStudyVariableAssay(2, assay1);
//        mtd.addStudyVariableAssay(2, assay2);
//        mtd.addStudyVariableSample(2, sample1);
//        mtd.addStudyVariableDescription(2,
//                "description Group B (spike-in 0.74 fmol/uL)");
//
//        mtd.addCVLabel(1, "MS");
//        mtd.addCVFullName(1, "MS");
//        mtd.addCVVersion(1, "3.54.0");
//        mtd.addCVURL(1,
//                "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo");
//
//        mtd.addProteinColUnit(ProteinColumn.RELIABILITY, new CVParam("MS",
//                "MS:00001231", "PeptideProphet:Score", null));
//
//        MZTabColumnFactory peptideFactory = MZTabColumnFactory.getInstance(
//                Section.Peptide);
//        peptideFactory.addDefaultStableColumns();
//
//        PeptideColumn peptideColumn = (PeptideColumn) peptideFactory.
//                findColumnByHeader("retention_time");
//        mtd.addPeptideColUnit(peptideColumn, new CVParam("UO", "UO:0000031",
//                "minute", null));
//
//        mtd.addPSMColUnit(PSMColumn.RETENTION_TIME, new CVParam("UO",
//                "UO:0000031", "minute", null));
//        mtd.addSmallMoleculeColUnit(SmallMoleculeColumn.RETENTION_TIME,
//                new CVParam("UO", "UO:0000031", "minute", null));
//
//        System.out.println(mtd);
        return new MzTab().metadata(mtd);
    }

    @Test
    public void testWriteDefaultToString() {
        try (BufferedWriter bw = Files.newBufferedWriter(File.createTempFile(
                "testWriteDefaultToString", ".txt").
                toPath(), Charset.forName("UTF-8"), StandardOpenOption.WRITE)) {
            bw.write(createTestFile().
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
            bw.write(mapper.writeValueAsString(createTestFile()));
        } catch (IOException ex) {
            Logger.getLogger(MzTabWriterTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testWriteCsv() {
        MzTab mztabfile = createTestFile();
        CsvMapper mapper = new CsvMapper();
        CsvSchema metadataSchema = CsvSchema.builder().
                addColumn("PREFIX", CsvSchema.ColumnType.STRING).
                addColumn("KEY", CsvSchema.ColumnType.STRING).
                addColumn("VALUE", CsvSchema.ColumnType.STRING).
                build();
        metadataSchema.withAllowComments(true).
                withLineSeparator(SEP).
                withUseHeader(false).
                withArrayElementSeparator("|").
                withNullValue("null").
                withColumnSeparator('\t');
        CsvSchema schema = mapper.schemaFor(Metadata.class).
                withAllowComments(true).
                withLineSeparator(SEP).
                withUseHeader(false).
                withArrayElementSeparator("|").
                withNullValue("null").
                withColumnSeparator('\t');
        try {
            System.out.println(mapper.writer(metadataSchema).
                    writeValueAsString(mztabfile.getMetadata()));
        } catch (JsonProcessingException ex) {
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
        String s = MzTabWriter.parameterToString(p);
        System.out.println(s);
        String expected = "[MS, MS:100179, made up for testing, ]";
        Assert.assertEquals(expected, s);
        p.value("some value");
        s = MzTabWriter.parameterToString(p);
        System.out.println(s);
        expected = "[MS, MS:100179, made up for testing, some value]";
        Assert.assertEquals(expected, s);
    }

    @Test
    public void testUserParameterToString() {
        Parameter p = new Parameter().name("made up for testing").
                value("some arbitrary value");
        String s = MzTabWriter.parameterToString(p);
        System.out.println(s);
        String expected = "[, , made up for testing, some arbitrary value]";
        Assert.assertEquals(expected, s);
    }

    @Test
    public void testMtdParameterToMzTabLine() {
        Parameter p = new Parameter().cvLabel("MS").
                cvAccession("MS:100179").
                name("made up for testing").
                value(null);
        String s = MzTabWriter.parameterToString(p);
        String actual = MzTabWriter.mtdParameterToMzTabLine(1,
                "sample_processing", p);
        String expected = "MTD\tsample_processing[1]\t" + s + "\n\r";
        System.out.println(actual);
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMtdParameterToMzTabLineException() {
        Parameter p = new Parameter().cvLabel("MS").
                cvAccession("MS:100179").
                name("made up for testing").
                value(null);
        MzTabWriter.mtdParameterToMzTabLine(0,
                "sample_processing", p);
    }

    @Test
    public void testGetJsonPropertyFields() {
        MzTab mzTabFile = createTestFile();
        MzTabWriter.getJsonPropertyFields(mzTabFile.getClass());
        MzTabWriter.getJsonPropertyFields(mzTabFile.getMetadata().
                getClass());
        MzTabWriter.getJsonPropertyFields(mzTabFile.getSmallMoleculeSummary().
                getClass());
        MzTabWriter.getJsonPropertyFields(mzTabFile.getSmallMoleculeFeature().
                getClass());
        MzTabWriter.getJsonPropertyFields(mzTabFile.getSmallMoleculeEvidence().
                getClass());
    }
}
