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
package de.isas.lipidomics.jmztabm.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import static de.isas.lipidomics.jmztabm.io.MzTabTestData.create1_1TestFile;
import de.isas.lipidomics.jmztabm.io.serialization.ParameterConverter;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Database;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.Parameter;
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
        String s = new ParameterConverter().convert(p);
        System.out.println(s);
        String expected = "[MS, MS:100179, made up for testing, ]";
        Assert.assertEquals(expected, s);
        p.value("some value");
        s = new ParameterConverter().convert(p);
        System.out.println(s);
        expected = "[MS, MS:100179, made up for testing, some value]";
        Assert.assertEquals(expected, s);
    }

    @Test
    public void testUserParameterToString() {
        Parameter p = new Parameter().name("made up for testing").
            value("some arbitrary value");
        String s = new ParameterConverter().convert(p);
        System.out.println(s);
        String expected = "[, , made up for testing, some arbitrary value]";
        Assert.assertEquals(expected, s);
    }

    @Test
    public void testWriteMetadataToTsvWithJackson() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        try (StringWriter sw = new StringWriter()) {
            new MzTabNonValidatingWriter().writeMetadataWithJackson(mzTabFile, sw);
            System.out.println("Serialized Metadata: ");
            sw.flush();
            String metadataString = sw.toString();
            System.out.println(metadataString);
            Assert.assertFalse(metadataString.isEmpty());
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
            addAdductIonsItem("[M+H]1+").
            reliability("1").
            bestIdConfidenceMeasure(new Parameter().name(
                "qualifier ions exact mass")).
            bestIdConfidenceValue(0.958).
            addAbundanceAssayItem(4.448784E-05).
            addAbundanceAssayItem(null).
            addAbundanceStudyVariableItem(4.448784E-05).
            addAbundanceStudyVariableItem(null).
            addAbundanceVariationStudyVariableItem(0.0d).
            addAbundanceVariationStudyVariableItem(0.00001d);
        mzTabFile.addSmallMoleculeSummaryItem(smsi);
        try (StringWriter sw = new StringWriter()) {
            new MzTabNonValidatingWriter().
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
            new MzTabNonValidatingWriter().
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
            new MzTabNonValidatingWriter().
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
        MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
        try (OutputStreamWriter osw = new OutputStreamWriter(System.out,
            Charset.forName(
                "UTF-8"))) {
            writer.write(osw, mzTabFile);
        }
    }

    @Test
    public void testWriteMzTabToTsvWithJacksonForPath() throws IOException {
        MzTab mzTabFile = create1_1TestFile();
        MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
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
        MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
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
        MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
        writer.write(tempFile.toPath(), mzTabFile);
        MZTabFileParser parser = new MZTabFileParser(tempFile);
        MZTabErrorList errors = parser.parse(System.out,
            MZTabErrorType.Level.Info, 500);
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
        MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
        writer.write(tempFile.toPath(), mzTabFile);
        MZTabFileParser parser = new MZTabFileParser(tempFile);
        MZTabErrorList errors = parser.parse(System.out,
            MZTabErrorType.Level.Info, 500);
        System.err.println(errors);
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
        MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
        writer.write(tempFile.toPath(), mzTabFile);
        long lines = Files.lines(tempFile.toPath()).
            count();
        Assert.assertEquals(
            3
            + // additional empty lines
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
        mtd.mzTabVersion("2.0.0-M");
        mtd.mzTabID("1");
        for (int i = 1; i <= 10; i++) {
            Contact c = new Contact().id(i).
                name("C" + i).
                email("c" + i + "@email.com").
                affiliation("A" + i);
            mtd.addContactItem(c);
        }
//        mtd.addQuantificationMethodItem(new Parameter().id(1).
//            cvLabel("MS").
//            cvAccession("MS:1002038").
//            name("unlabeled sample"));
        mtd.addSoftwareItem(new Software().id(1).
            parameter(new Parameter().id(1).
                name("LipidDataAnalyzer").
                value("2.6.3_nightly")));
        MsRun msrun1 = new MsRun().id(1).
            location(
                "file://D:/Experiment1/Orbitrap_CID/negative/50/014_Ex1_Orbitrap_CID_neg_50.chrom");
        MsRun msrun2 = new MsRun().id(2).
            location(
                "file://D:/Experiment1/Orbitrap_CID/negative/50/015_Ex1_Orbitrap_CID_neg_50.chrom");
        MsRun msrun3 = new MsRun().id(3).
            location(
                "file://D:/Experiment1/Orbitrap_CID/negative/50/016_Ex1_Orbitrap_CID_neg_50.chrom");
        MsRun msrun4 = new MsRun().id(4).
            location(
                "file://D:/Experiment1/Orbitrap_CID/negative/50/017_Ex1_Orbitrap_CID_neg_50.chrom");
        MsRun msrun5 = new MsRun().id(5).
            location(
                "file://D:/Experiment1/Orbitrap_CID/negative/50/018_Ex1_Orbitrap_CID_neg_50.chrom");
        mtd.addMsRunItem(msrun1).
            addMsRunItem(msrun2).
            addMsRunItem(msrun3).
            addMsRunItem(msrun4).
            addMsRunItem(msrun5);
        Assay a1 = new Assay().id(1).
            addMsRunRefItem(msrun1);
        Assay a2 = new Assay().id(2).
            addMsRunRefItem(msrun2);
        Assay a3 = new Assay().id(3).
            addMsRunRefItem(msrun3);
        Assay a4 = new Assay().id(4).
            addMsRunRefItem(msrun4);
        Assay a5 = new Assay().id(5).
            addMsRunRefItem(msrun5);
        mtd.addAssayItem(a1).
            addAssayItem(a2).
            addAssayItem(a3).
            addAssayItem(a4).
            addAssayItem(a5);
        StudyVariable sv1 = new StudyVariable().id(1).
            name("Group 1").
            description("Group 1");
        sv1.addAssayRefsItem(a1).
            addAssayRefsItem(a2).
            addAssayRefsItem(a3);
        StudyVariable sv2 = new StudyVariable().id(2).
            name("Group 2").
            description("Group 2");
        sv2.addAssayRefsItem(a4).
            addAssayRefsItem(a5);
        mtd.addStudyVariableItem(sv1).
            addStudyVariableItem(sv2);
        mtd.addCvItem(new CV().id(1).
            label("MS").
            url("https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo").
            version("4.0.9").
            fullName("PSI-MS controlled vocabulary"));
        mtd.addDatabaseItem(new Database().id(1).
            prefix("nd").
            url("none").
            version("none").
            param(new Parameter().name("no database").
                value(null)));
        mztab.metadata(mtd);

        SmallMoleculeSummary summary = new SmallMoleculeSummary();
        summary.setSmlId(null);
        summary.setSmfIdRefs(new ArrayList<>());
        summary.setDatabaseIdentifier(new ArrayList<>());
        summary.setSmiles(new ArrayList<>());
        summary.setInchi(new ArrayList<>());
        summary.setChemicalName(new ArrayList<>());
        summary.setUri(new ArrayList<>());
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
            abundanceAssay.add(assay.getId().
                doubleValue() + 0.1d);
        }
        summary.setAbundanceAssay(abundanceAssay);
        List<Double> abundanceStudyVariable = new ArrayList<Double>();
        List<Double> abundanceCoeffvarStudyVariable = new ArrayList<Double>();
        for (StudyVariable sv : mtd.getStudyVariable()) {
            abundanceStudyVariable.add(sv.getId().
                doubleValue() + 0.2d);
            abundanceCoeffvarStudyVariable.add(sv.getId().
                doubleValue() + 0.1d);
        }
        summary.setAbundanceStudyVariable(abundanceStudyVariable);
        summary.
            setAbundanceVariationStudyVariable(abundanceCoeffvarStudyVariable);
        mztab.addSmallMoleculeSummaryItem(summary);

        File tempFile = File.createTempFile(
            "testSmlNullEmptyHandling",
            ".mztab");
        MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
        writer.write(tempFile.toPath(), mztab);
        Files.lines(tempFile.toPath()).forEach((line) ->
            {
                System.out.println(line);
            });
        long lines = Files.lines(tempFile.toPath()).
            count();
        Assert.assertEquals(
            3
            + //additional empty lines 
            57
            +//metadata lines
            2 //Small molecule summary header + content
            + 1 // Small molecule feature header
            + 1 // Small molecule evidence header
            ,
             lines);
    }
}
