package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Sample;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import de.isas.mztab.jmztabm.test.utils.LogMethodName;
import org.junit.Assert;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabUtils;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;

/**
 * @author qingwei
 * @author Nils Hoffmann
 * @since 11/02/13
 */
public class MTDLineParserTest {
    
    @Rule
    public LogMethodName methodNameLogger = new LogMethodName();
    
    private MTDLineParser parser;
    private Metadata metadata;
    private MZTabErrorList errorList;
    private MZTabParserContext context;
    
    @Before
    public void setUp() throws MZTabException {
        context = new MZTabParserContext();
        parser = new MTDLineParser(context);
        metadata = parser.getMetadata();
        errorList = new MZTabErrorList();
    }
    
    @Test
    public void testQuantMethod() throws MZTabException {
        //        parser.parse(1, "MTD\tfixed_mod[11]\t[UNIMOD, UNIMOD:4, Carbamidomethyl, ]", errorList);
//        parser.parse(1, "MTD\tfixed_mod[11]-site\tM", errorList);
//        parser.parse(1, "MTD\tfixed_mod[2]\t[UNIMOD, UNIMOD:35, Oxidation, ]", errorList);
//        parser.parse(1, "MTD\tfixed_mod[2]-site\tN-term", errorList);
//        parser.parse(1, "MTD\tfixed_mod[3]\t[UNIMOD, UNIMOD:1, Acetyl, ]", errorList);
//        parser.parse(1, "MTD\tfixed_mod[3]-position\tProtein C-term", errorList);
//        assertTrue(metadata.getFixedModMap().size() == 3);
//        assertTrue(metadata.getFixedModMap().get(11).getSite().equals("M"));
//        assertTrue(metadata.getVariableModMap().size() == 0);
        parser.parse(1, "MTD\tquantification_method\t[MS, MS:1001837, iTraq, ]",
            errorList);
        assertTrue(metadata.getQuantificationMethod() != null);
    }
    
    @Test
    public void testUri() throws MZTabException {
        parser.
            parse(1, "MTD\turi[1]\thttp://www.ebi.ac.uk/pride/url/to/experiment",
                errorList);
        parser.parse(1,
            "MTD\turi[2]\thttp://proteomecentral.proteomexchange.org/cgi/GetDataset",
            errorList);
        assertTrue(metadata.getUri().
            size() == 2);
    }
    
    @Test
    public void testContact() throws MZTabException {
        parser.parse(1, "MTD\tcontact[11]-name\tJames D. Watson", errorList);
        parser.
            parse(1, "MTD\tcontact[11]-affiliation\tCambridge University, UK",
                errorList);
        parser.parse(1, "MTD\tcontact[11]-email\twatson@cam.ac.uk", errorList);
        parser.parse(1, "MTD\tcontact[2]-affiliation\tCambridge University, UK",
            errorList);
        parser.parse(1, "MTD\tcontact[2]-email\tcrick@cam.ac.uk", errorList);
        assertTrue(metadata.getContacts().
            size() == 2);
    }
    
    @Test
    public void testPublication() throws MZTabException {
        //        parser.parse(1, "MTD\tprotein_search_engine_score[1]\t[MS, MS:1001171, Mascot:score,]", errorList);
//        parser.parse(1, "MTD\tpsm_search_engine_score[2]\t[MS, MS:1001330, X!Tandem:expect,]", errorList);
//        parser.parse(1, "MTD\tpsm_search_engine_score[3]\t[MS, MS:1001331, X!Tandem:hyperscore,]", errorList);
//        assertTrue(metadata.getProteinSearchEngineScoreMap().size() == 1);
//        param = metadata.getProteinSearchEngineScoreMap().get(1).getParam();
//        assertTrue(param.toString().contains("score"));
//        parser.parse(1, "MTD\tfalse_discovery_rate\t[MS, MS:1234, pep-fdr, 0.5]|[MS, MS:1001364, pep:global FDR, 0.01]|[MS, MS:1001214, pep:global FDR, 0.08]", errorList);
//        assertTrue(metadata.getFalseDiscoveryRate().size() == 3);
        parser.parse(1,
            "MTD\tpublication[1]\tpubmed:21063943|doi:10.1007/978-1-60761-987-1_6",
            errorList);
        parser.parse(1,
            "MTD\tpublication[12]\tpubmed:20615486|doi:10.1016/j.jprot.2010.06.008",
            errorList);
        assertTrue(metadata.getPublications().
            size() == 2);
    }
    
    @Test
    public void testSoftware() throws MZTabException {
        Parameter param;
        parser.parse(1, "MTD\tsoftware[11]\t[MS, MS:1001207, Mascot, 2.3]",
            errorList);
        parser.parse(1,
            "MTD\tsoftware[2]-setting[1]\tFragment tolerance = 0.1Da", errorList);
        parser.parse(1, "MTD\tsoftware[2]-setting[2]\tParent tolerance = 0.5Da",
            errorList);
        param = context.getSoftwareMap().
            get(11).
            getParameter();
        assertTrue(param.toString().
            contains("Mascot"));
        List<String> settingList = context.getSoftwareMap().
            get(2).
            getSetting();
        assertTrue(settingList.size() == 2);
    }
    
    @Test
    public void testInstrument() throws MZTabException {
        Parameter param;
        parser.parse(1,
            "MTD\tinstrument[1]-name\t[MS, MS:100049, LTQ Orbitrap, ]",
            errorList);
        parser.parse(1,
            "MTD\tinstrument[1]-analyzer[1]\t[MS, MS:1000291, linear ion trap, ]",
            errorList);
        parser.parse(1, "MTD\tinstrument[2]-source\t[MS, MS:1000598, ETD, ]",
            errorList);
        parser.parse(1,
            "MTD\tinstrument[13]-detector\t[MS, MS:1000253, electron multiplier, ]",
            errorList);
        param = context.getInstrumentMap().
            get(1).
            getInstrumentName();
        assertTrue(param.toString().
            contains("LTQ Orbitrap"));
        List<Parameter> analyzerList = context.getInstrumentMap().
            get(1).
            getInstrumentAnalyzer();
        assertTrue(analyzerList.size() == 1);
        param = context.getInstrumentMap().
            get(2).
            getInstrumentSource();
        assertTrue(param.toString().
            contains("ETD"));
        param = context.getInstrumentMap().
            get(13).
            getInstrumentDetector();
        assertTrue(param.toString().
            contains("electron multiplier"));
    }
    
    @Test
    public void testSampleProcessing() throws MZTabException {
        parser.parse(1,
            "MTD\tsample_processing[1]\t[SEP, SEP:00173, SDS PAGE, ]", errorList);
        Parameter param = context.getSampleProcessingMap().
            get(1).
            getSampleProcessing().
            get(0);
        assertTrue(param instanceof Parameter);
        Parameter cvParam = param;
        assertTrue(cvParam.getName().
            contains("SDS PAGE"));
        assertTrue(MZTabUtils.isEmpty(cvParam.getValue()));
        
        parser.parse(1,
            "MTD\tsample_processing[12]\t[SEP, SEP:00142, enzyme digestion, ]|[MS, MS:1001251, Trypsin, ]",
            errorList);
        assertTrue(context.getSampleProcessingMap().
            size() == 2);
        param = context.getSampleProcessingMap().
            get(12).
            getSampleProcessing().
            get(0);
        assertTrue(param instanceof Parameter);
        cvParam = (Parameter) param;
        assertTrue(cvParam.getName().
            contains("enzyme digestion"));
        assertTrue(MZTabUtils.isEmpty(cvParam.getValue()));
        param = context.getSampleProcessingMap().
            get(12).
            getSampleProcessing().
            get(1);
        assertTrue(param instanceof Parameter);
        cvParam = (Parameter) param;
        assertTrue(cvParam.getName().
            contains("Trypsin"));
        assertTrue(MZTabUtils.isEmpty(cvParam.getValue()));
    }
    
    @Test
    public void testMzTabFileInfo() throws MZTabException {
        parser.parse(1, "MTD\tmzTab-version\t2.0.0-M", errorList);
        assertTrue(metadata.getMzTabVersion().
            equals("2.0.0-M"));
        try {
            parser.parse(1, "MTD\tmzTab-version\t1.0.0", errorList);
            Assert.fail("You shall not pass!");
        } catch (MZTabException ex) {
            
        }
        
        try {
            parser.parse(1, "MTD\tmzTab-version\t3.0.0", errorList);
            Assert.fail("You shall not pass!");
        } catch (MZTabException ex) {
            
        }

//        parser.parse(1, "MTD\tmzTab-mode\tComplete", errorList);
//        assertTrue(metadata.getTabDescription().getMode() == MZTabDescription.Mode.Complete);
//
//        parser.parse(1, "MTD\tmzTab-mode\tSummary", errorList);
//        assertTrue(metadata.getTabDescription().getMode() == MZTabDescription.Mode.Summary);
//
//        parser.parse(1, "MTD\tmzTab-type\tQuantification", errorList);
//        assertTrue(metadata.getTabDescription().getType() == MZTabDescription.Type.Quantification);
//
//        parser.parse(1, "MTD\tmzTab-type\tIdentification", errorList);
//        assertTrue(metadata.getTabDescription().getType() == MZTabDescription.Type.Identification);
        parser.parse(1, "MTD\tmzTab-ID\tPRIDE_1234", errorList);
        assertTrue(metadata.getMzTabID().
            equals("PRIDE_1234"));
        
        parser.parse(1, "MTD\ttitle\tmzTab iTRAQ test", errorList);
        assertTrue(metadata.getTitle().
            contains("mzTab iTRAQ test"));
        
        parser.parse(1,
            "MTD\tdescription\tAn experiment investigating the effects of Il-6.",
            errorList);
        assertTrue(metadata.getDescription().
            contains("An experiment investigating the effects of Il-6."));
    }
    
    @Test
    public void testDatabase() throws MZTabException {
        parser.parse(1, "MTD\tdatabase[1]\t[MIRIAM,MIR:00100079 , “HMDB”, ]",
            errorList);
        parser.parse(1, "MTD\tdatabase[1]-prefix\thmdb", errorList);
        parser.parse(1, "MTD\tdatabase[1]-version\t3.6", errorList);
        parser.parse(1, "MTD\tdatabase[1]-url\thttp://www.hmdb.ca/", errorList);
        assertFalse(context.getDatabaseMap().
            isEmpty());
        assertNotNull(context.getDatabaseMap().
            get(1));
        assertEquals("hmdb", context.getDatabaseMap().
            get(1).
            getPrefix());
        assertEquals("3.6", context.getDatabaseMap().
            get(1).
            getVersion());
        assertEquals("http://www.hmdb.ca/", context.getDatabaseMap().
            get(1).
            getUrl());
        assertEquals(new Parameter().cvLabel("MIRIAM").
            cvAccession("MIR:00100079").
            name("“HMDB”").
            value(null), context.getDatabaseMap().
            get(1).
            getParam());
    }
    
    @Test
    public void testCustom() throws MZTabException {
        parser.parse(1, "MTD\tcustom[1]\t[, , MS operator, Florian]", errorList);
        assertTrue(metadata.getCustom().
            size() == 1);
    }
    
    @Test
    public void testCv() throws MZTabException {
        parser.parse(1, "MTD\tcv[1]-label\tMS", errorList);
        parser.parse(1, "MTD\tcv[12]-full_name\tMS", errorList);
        parser.parse(1, "MTD\tcv[1]-version\t3.54.0", errorList);
        parser.parse(1,
            "MTD\tcv[12]-url\thttp://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo",
            errorList);
        assertTrue(context.getCvMap().
            size() == 2);
        assertTrue(context.getCvMap().
            get(1).
            getVersion().
            equals("3.54.0"));
        assertTrue(context.getCvMap().
            get(12).
            getUrl().
            equals(
                "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo"));
    }
    
    @Test
    public void testIdConfidenceMeasure() throws MZTabException {
        //MTD id_confidence_measure[1] [MS, MS:1001419, SpectraST:discriminant score F,]
        parser.parse(1,
            "MTD\tid_confidence_measure[1]\t[MS, MS:1001419, “SpectraST:discriminant score F”,]",
            errorList);
        assertFalse(context.getIdConfidenceMeasureMap().
            isEmpty());
        assertNotNull(context.getIdConfidenceMeasureMap().
            get(1));
        assertEquals(new Parameter().id(1).
            cvLabel("MS").
            cvAccession("MS:1001419").
            name("“SpectraST:discriminant score F”").
            value(null), context.getIdConfidenceMeasureMap().
            get(1));
    }
    
    @Test
    public void testMsRun() throws MZTabException {
        parser.parse(1, "MTD\tms_run[1]-format\t[MS, MS:1000584, mzML file, ]",
            errorList);
        parser.parse(1, "MTD\tms_run[2]-location\tfile://C:/path/to/my/file",
            errorList);
        parser.parse(1,
            "MTD\tms_run[2]-id_format\t[MS, MS:1000774, multiple peak list, nativeID format]",
            errorList);
        parser.parse(1,
            "MTD\tms_run[2]-fragmentation_method\t[MS, MS:1000133, CID, ]",
            errorList);
        parser.parse(1,
            "MTD\tms_run[3]-location\tftp://ftp.ebi.ac.uk/path/to/file",
            errorList);
        assertTrue(context.getMsRunMap().
            size() == 3);
        MsRun msRun2 = context.getMsRunMap().
            get(2);
        assertTrue(msRun2.getLocation().
            toString().
            equals("file://C:/path/to/my/file"));
        assertTrue(msRun2.getFragmentationMethod().
            get(0).
            getCvAccession().
            equals("MS:1000133"));
        
        parser.parse(1,
            "MTD\tms_run[2]-hash\tde9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3",
            errorList);
        parser.parse(1,
            "MTD\tms_run[2]-hash_method\t[MS, MS: MS:1000569, SHA-1, ]",
            errorList);
        assertTrue(msRun2.getHash().
            equals("de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3"));
        assertTrue(msRun2.getHashMethod().
            getName().
            equals("SHA-1"));
    }
    
    @Test
    public void testMsRunLocationNull() throws MZTabException {
        parser.parse(1, "MTD\tms_run[1]-location\tnull\n", errorList);
        parser.parse(1, "MTD\tms_run[2]-location\tfile://C:/path/to/my/file\n",
            errorList);
        parser.parse(1,
            "MTD\tms_run[2]-id_format\t[MS, MS:1000774, multiple peak list, nativeID format]\n",
            errorList);
        parser.parse(1,
            "MTD\tms_run[2]-fragmentation_method\t[MS, MS:1000133, CID, ]\n",
            errorList);
        parser.parse(1,
            "MTD\tms_run[3]-location\tftp://ftp.ebi.ac.uk/path/to/file\n",
            errorList);
        assertTrue(context.getMsRunMap().
            size() == 3);
        MsRun msRun1 = context.getMsRunMap().
            get(1);
        assertNull(msRun1.getLocation());
        
        MsRun msRun2 = context.getMsRunMap().
            get(2);
        assertTrue(msRun2.getLocation().
            toString().
            equals("file://C:/path/to/my/file"));
        assertTrue(msRun2.getFragmentationMethod().
            get(0).
            getCvAccession().
            equals("MS:1000133"));
        
        parser.parse(1,
            "MTD\tms_run[2]-hash\tde9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3\n",
            errorList);
        parser.parse(1,
            "MTD\tms_run[2]-hash_method\t[MS, MS: MS:1000569, SHA-1, ]\n",
            errorList);
        assertTrue(msRun2.getHash().
            equals("de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3"));
        assertTrue(msRun2.getHashMethod().
            getName().
            equals("SHA-1"));
    }
    
    @Test
    public void testSample() throws MZTabException {
        parser.parse(1,
            " MTD\tsample[1]-species[1]\t[NEWT, 9606, Homo sapien (Human), ]",
            errorList);
        parser.parse(1,
            " MTD\tsample[1]-species[2]\t[NEWT, 573824, Human rhinovirus 1, ]",
            errorList);
        Sample sample1 = context.getSampleMap().
            get(1);
        assertTrue(sample1.getSpecies().
            size() == 2);
        
        parser.parse(1, "MTD\tsample[1]-tissue[1]\t[BTO, BTO:0000759, liver, ]",
            errorList);
        assertTrue(sample1.getTissue().
            size() == 1);
        
        parser.parse(1,
            " MTD\tsample[1]-cell_type[1]\t[CL, CL:0000182, hepatocyte, ]",
            errorList);
        assertTrue(sample1.getCellType().
            size() == 1);
        
        parser.parse(1,
            " MTD\tsample[1]-disease[1]\t[DOID, DOID:684, hepatocellular carcinoma, ]",
            errorList);
        parser.parse(1,
            " MTD\tsample[1]-disease[2]\t[DOID, DOID:9451, alcoholic fatty liver, ]",
            errorList);
        assertTrue(sample1.getDisease().
            size() == 2);
        
        parser.parse(1,
            " MTD \t sample[1]-description \t  Hepatocellular carcinoma samples.",
            errorList);
        parser.parse(1,
            " MTD \t sample[2]-description \t  Healthy control samples.",
            errorList);
        assertTrue(sample1.getDescription().
            contains("Hepatocellular carcinoma samples."));
        Sample sample2 = context.getSampleMap().
            get(2);
        assertTrue(sample2.getDescription().
            contains("Healthy control samples."));
        
        parser.parse(1,
            "MTD\tsample[1]-custom[1]\t[,,Extraction date, 2011-12-21]",
            errorList);
        parser.parse(1,
            "MTD\tsample[1]-custom[2]\t[,,Extraction reason, liver biopsy]",
            errorList);
        assertTrue(sample1.getCustom().
            size() == 2);
    }
    
    @Test
    public void testAssay() throws MZTabException {
//        parser.parse(1, "MTD\tassay[1]-quantification_reagent\t[PRIDE,PRIDE:0000114,iTRAQ reagent,114]", errorList);
//        parser.parse(1, "MTD\tassay[2]-quantification_reagent\t[PRIDE,PRIDE:0000115,iTRAQ reagent,115]", errorList);
//        assertTrue(context.getAssayMap().size() == 2);
//        assertTrue(context.getAssayMap().get(1).().getAccession().equals("PRIDE:0000114"));

        Sample sample1 = new Sample();
        sample1.id(1);
        Sample sample2 = new Sample();
        sample2.id(2);
        context.addSample(metadata, sample1);
        context.addSample(metadata, sample2);
        parser.parse(1, "MTD\tassay[1]-sample_ref\tsample[1]", errorList);
        parser.parse(1, "MTD\tassay[2]-sample_ref\tsample[2]", errorList);
        assertTrue(context.getAssayMap().
            get(1).
            getSampleRef().
            equals(sample1));
        assertTrue(context.getAssayMap().
            get(2).
            getSampleRef().
            equals(sample2));
        
        MsRun msRun1 = new MsRun();
        msRun1.id(1);
        MsRun msRun2 = new MsRun();
        msRun2.id(2);
        context.addMsRun(metadata, msRun1);
        context.addMsRun(metadata, msRun2);
        parser.parse(1, "MTD\tassay[1]-ms_run_ref\tms_run[1]", errorList);
        parser.parse(1, "MTD\tassay[2]-ms_run_ref\tms_run[2]", errorList);
        assertTrue(context.getAssayMap().
            get(1).
            getMsRunRef().
            equals(msRun1));
        assertTrue(context.getAssayMap().
            get(2).
            getMsRunRef().
            equals(msRun2));

//        parser.parse(1, "MTD\tassay[2]-quantification_mod[1]\t[UNIMOD, UNIMOD:188, Label:13C(6), ]", errorList);
//        parser.parse(1, "MTD\tassay[2]-quantification_mod[1]-site\tR", errorList);
//        parser.parse(1, "MTD\tassay[2]-quantification_mod[1]-position\tAnywhere", errorList);
//        parser.parse(1, "MTD\tassay[2]-quantification_mod[2]\t[UNIMOD, UNIMOD:188, Label:13C(6), ]", errorList);
//        parser.parse(1, "MTD\tassay[2]-quantification_mod[2]-site\tK", errorList);
//        parser.parse(1, "MTD\tassay[2]-quantification_mod[2]-position\tAnywhere", errorList);
//        assertTrue(context.getAssayMap().get(2).getQuantificationModMap().size() == 2);
//        AssayQuantificationMod quantificationMod = metadata.getAssayMap().get(2).getQuantificationModMap().get(1);
//        assertTrue(quantificationMod.getSite().equals("R"));
//        quantificationMod = metadata.getAssayMap().get(2).getQuantificationModMap().get(2);
//        assertTrue(quantificationMod.getPosition().equals("Anywhere"));
    }
    
    @Test
    public void testStudyVariable() throws MZTabException {
        parser.parse(1,
            "MTD\tstudy_variable[1]-description\tGroup B (spike-in 0,74 fmol/uL)",
            errorList);
        assertTrue(context.getStudyVariableMap().
            size() == 1);
        assertTrue(context.getStudyVariableMap().
            get(1).
            getDescription().
            equals("Group B (spike-in 0,74 fmol/uL)"));
        
        Sample sample1 = new Sample();
        sample1.id(1);
        Sample sample2 = new Sample();
        sample2.id(2);
        context.addSample(metadata, sample1);
        context.addSample(metadata, sample2);
        parser.parse(1,
            "MTD\tstudy_variable[1]-sample_refs\tsample[1],sample[2]", errorList);
        assertTrue(context.getStudyVariableMap().
            get(1).
            getSampleRefs().
            size() == 2);
        assertTrue(context.getStudyVariableMap().
            get(1).
            getSampleRefs().
            get((2 - 1)) == sample2);
        
        Assay assay1 = new Assay();
        assay1.id(1);
        Assay assay2 = new Assay();
        assay2.id(2);
        context.addAssay(metadata, assay1);
        context.addAssay(metadata, assay2);
        parser.parse(1, "MTD\tstudy_variable[2]-assay_refs\tassay[1], assay[2]",
            errorList);
        assertTrue(context.getStudyVariableMap().
            get(2).
            getAssayRefs().
            size() == 2);
        assertTrue(context.getStudyVariableMap().
            get(2).
            getAssayRefs().
            get(0) == assay1);
    }
    
    public Metadata parseMetadata(String mtdFile) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(mtdFile));
        MTDLineParser parser = new MTDLineParser(context);
        
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.trim().
                length() == 0) {
                continue;
            }
            
            parser.parse(lineNumber, line, errorList);
        }
        
        reader.close();
        
        parser.refineNormalMetadata();
        return parser.getMetadata();
    }
    
    @Test
    public void testCreateMetadata() throws Exception {
        String fileName = "testset/mtdFile.txt";
        
        URL uri = MTDLineParserTest.class.getClassLoader().
            getResource(fileName);
        if (uri != null) {
            parseMetadata(uri.getFile());
            assertTrue(errorList.isEmpty());
        } else {
            throw new FileNotFoundException(fileName);
        }
    }
    
    @Test
    public void testSmIdReliability() throws MZTabException {
        String toParse = "MTD\tsmall_molecule-identification_reliability\t[PRIDE, PRIDE:0000395, Ratio, ]";
        errorList = new MZTabErrorList(MZTabErrorType.Level.Warn);
        parser.parse(1, toParse, errorList);
        assertNotNull(metadata.getSmallMoleculeIdentificationReliability());
        assertEquals(new Parameter().cvLabel("PRIDE").
            cvAccession("PRIDE:0000395").
            name("Ratio").
            value(null), metadata.getSmallMoleculeIdentificationReliability());
    }
    
    @Test
    public void testSmQuantUnit() throws MZTabException {
        //        parser.parse(1, "MTD\tprotein-quantification_unit\t[PRIDE, PRIDE:0000395, Ratio, ]", errorList);
        //        parser.parse(1, "MTD\tpeptide-quantification_unit\t[PRIDE, PRIDE:0000395, Ratio, ]", errorList);
        //        parser.parse(1,
        //            "MTD\tsmall_molecule-quantification_unit\t[PRIDE, PRIDE:0000395, Ratio, ]",
        //            errorList);
        //        assertTrue(metadata.getProteinQuantificationUnit() != null);
        //        assertTrue(metadata.getPeptideQuantificationUnit() != null);
//        assertTrue(metadata.getSmallMoleculeQuantificationUnit() != null);
        String toParse = "MTD\tsmall_molecule-quantification_unit\t[PSI-MS, MS:000XXXX, Progenesis QI Normalised Abundance, ]";
        errorList = new MZTabErrorList(MZTabErrorType.Level.Warn);
        parser.parse(1, toParse, errorList);
        assertNotNull(metadata.getSmallMoleculeQuantificationUnit());
        assertEquals(new Parameter().cvLabel("PSI-MS").
            cvAccession("MS:000XXXX").
            name("Progenesis QI Normalised Abundance").
            value(null), metadata.getSmallMoleculeQuantificationUnit());
    }
    
    @Test
    public void testSmFeatureQuantUnit() throws MZTabException {
        String toParse = "MTD\tsmall_molecule_feature-quantification_unit\t[PSI-MS, MS:000XXXX, Progenesis QI Normalised Abundance, ]";
        errorList = new MZTabErrorList(MZTabErrorType.Level.Warn);
        parser.parse(1, toParse, errorList);
        assertNotNull(metadata.getSmallMoleculeFeatureQuantificationUnit());
        assertEquals(new Parameter().cvLabel("PSI-MS").
            cvAccession("MS:000XXXX").
            name("Progenesis QI Normalised Abundance").
            value(null), metadata.getSmallMoleculeFeatureQuantificationUnit());
    }
}
