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
package org.lifstools.mztab2.model;

import org.lifstools.mztab2.model.IndexedElement;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Comment;
import org.lifstools.mztab2.model.Contact;
import org.lifstools.mztab2.model.Database;
import org.lifstools.mztab2.model.Instrument;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.OptColumnMapping;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.Publication;
import org.lifstools.mztab2.model.PublicationItem;
import org.lifstools.mztab2.model.Sample;
import org.lifstools.mztab2.model.SampleProcessing;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import org.lifstools.mztab2.model.Software;
import org.lifstools.mztab2.model.SpectraRef;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.Uri;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class MzTabTest {

    @Test
    public void testMzTabObjectCreation() {
        MzTab mztab = createTestMzTab();
        Assert.assertEquals("SomeId 1234", mztab.getMetadata().
            getMzTabID());
        Assert.assertEquals("2.0.0-M", mztab.getMetadata().
            getMzTabVersion());
        Assert.assertEquals("My first test experiment", mztab.getMetadata().
            getTitle());
        Assert.assertEquals("An experiment investigating interesting effects.",
            mztab.getMetadata().
                getDescription());
        Assert.assertEquals((long) 1, (long) mztab.getMetadata().
            getSampleProcessing().
            get(0).
            getId());
        Assert.assertEquals("sep:00210", mztab.getMetadata().
            getSampleProcessing().
            get(0).
            getSampleProcessing().
            get(0).
            getCvAccession());
        Assert.assertEquals(2, mztab.getMetadata().getInstrument().size());
        Assert.assertEquals(1, mztab.getMetadata().getSoftware().size());
        Assert.assertEquals(2, mztab.getMetadata().getPublication().size());
        Assert.assertEquals(2, mztab.getMetadata().getUri().size());
        Assert.assertEquals(1, mztab.getMetadata().getExternalStudyUri().size());
        Assert.assertEquals(2, mztab.getMetadata().getMsRun().size());
        Assert.assertEquals(1, mztab.getMetadata().getCustom().size());
        Assert.assertEquals(2, mztab.getMetadata().getSample().size());
        Assert.assertEquals(2, mztab.getMetadata().getAssay().size());
        Assert.assertEquals(2, mztab.getMetadata().getStudyVariable().size());
        Assert.assertEquals(2, mztab.getMetadata().getCv().size());
        
        Assert.assertEquals(1, mztab.getMetadata().getIdConfidenceMeasure().size());
        Assert.assertEquals(1, mztab.getMetadata().getDatabase().size());
        
        Assert.assertEquals(1, mztab.getSmallMoleculeSummary().size());
        Assert.assertEquals(1, mztab.getSmallMoleculeFeature().size());
        Assert.assertEquals(1, mztab.getSmallMoleculeEvidence().size());
    }

    public static MzTab createTestMzTab() {
        int paramId = 1;
        org.lifstools.mztab2.model.Metadata mtd = new org.lifstools.mztab2.model.Metadata();
        mtd.mzTabID("SomeId 1234").
            mzTabVersion("2.0.0-M").
            title("My first test experiment").
            description("An experiment investigating interesting effects.");
        SampleProcessing sp = new SampleProcessing().id(1).
            addSampleProcessingItem(new Parameter().cvLabel("SEP").
                cvAccession("sep:00210").
                name("High Perfomance Liquid Chromatography"));
        mtd.sampleProcessing(Arrays.asList(sp));

        Instrument instrument1 = new Instrument().id(1).
            name(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:100049").
                    name("LTQ Orbitrap")).
            source(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000073").
                    name("ESI")).
            analyzer(Arrays.asList(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000291").
                    name("linear ion trap"))
            ).
            detector(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000253").
                    name("electron multiplier")
            );
        mtd.addInstrumentItem(instrument1);
        Instrument instrument2 = new Instrument().id(2).
            name(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000031").
                    name("instrument model").
                    value("name of the instrument not included in the CV")).
            source(new Parameter().cvLabel("MS").
                cvAccession("MS:1000598").
                name("ETD")).
            addAnalyzerItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1000484").
                name("orbitrap")).
            detector(new Parameter().cvLabel("MS").
                cvAccession("MS:1000348").
                name("focal plane collector"));
        mtd.addInstrumentItem(instrument2);
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

        mtd.addPublicationItem(publication1).
            addPublicationItem(publication2);

        mtd.addContactItem(new Contact().id(1).
            name("James D. Watson").
            affiliation("Cambridge University, UK").
            email("watson@cam.ac.uk"));
        mtd.addContactItem(new Contact().id(2).
            name("Francis Crick").
            affiliation("Cambridge University, UK").
            email("crick@cam.ac.uk"));
        mtd.addUriItem(new Uri().id(1).
            value(
                URI.create("http://www.ebi.ac.uk/pride/url/to/experiment")));
        mtd.addUriItem(new Uri().id(2).
            value(
                URI.create("http://proteomecentral.proteomexchange.org/cgi/GetDataset")));
        mtd.addExternalStudyUriItem(new Uri().id(3).
            value(
                URI.create("https://www.ebi.ac.uk/metabolights/MTBLS400")));
        MsRun msRun1 = new MsRun().name("file1").id(1).
            location(URI.create("file://ftp.ebi.ac.uk/path/to/file")).
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
        mtd.addMsRunItem(msRun1);
        MsRun msRun2 = new MsRun().name("file2").id(2).
            location(URI.create("ftp://ftp.ebi.ac.uk/path/to/file")).
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
        mtd.addMsRunItem(msRun2);
        mtd.addCustomItem(new Parameter().
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
                value("liver biopsy"));
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
                value("liver biopsy"));
        mtd.addSampleItem(sample2);

        Assay assay1 = new Assay().id(1).
            name("Assay 1").
            addMsRunRefItem(msRun1).
            sampleRef(sample1);
        mtd.addAssayItem(assay1);
        Assay assay2 = new Assay().id(2).
            name("Assay 2").
            addMsRunRefItem(msRun2).
            sampleRef(sample2);
        mtd.addAssayItem(assay2);

        StudyVariable studyVariable1 = new StudyVariable().
            id(1).
            name("Group A name").
            description(
                "Group A description").
            addAssayRefsItem(
                assay1).
            addAssayRefsItem(assay2).
            addFactorsItem(new Parameter().name("spike-in").
                value("0.74 fmol/uL"));
        mtd.addStudyVariableItem(studyVariable1);
        StudyVariable studyVariable2 = new StudyVariable().
            id(2).
            name("Grou B name").
            description("Group B description").
            addAssayRefsItem(assay1).
            addAssayRefsItem(assay2).
            addFactorsItem(new Parameter().name("spike-in").
                value("0.74 fmol/uL"));
        mtd.addStudyVariableItem(studyVariable2);
        mtd.addCvItem(new CV().id(1).
            label("MS").
            fullName("PSI-MS ontology").
            version("3.54.0").
            uri(URI.create("https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo")));
        mtd.addCvItem(new CV().id(2).
            label("SEP").
            fullName("Sample Processing and Separation Techniques Ontology").
            version("1.070708").
            uri(URI.create("http://purl.bioontology.org/ontology/SEP")));
        
        mtd.addIdConfidenceMeasureItem(new Parameter().
            name("some confidence measure term"));

        mtd.addDatabaseItem(new Database().id(1).//prefix("null").
            param(new Parameter().name("nd")));
        mtd.setQuantificationMethod(new Parameter().name("test quantification").value("test quant value"));

        MzTab mzTab = new MzTab();
        mzTab.metadata(mtd);
        SmallMoleculeEvidence sme = new SmallMoleculeEvidence().smeId(1).
            addIdConfidenceMeasureItem(1.00).
            addSpectraRefItem(new SpectraRef().msRun(msRun2).
                reference("index=2")).
            charge(1).
            chemicalFormula("C42H83NO3").
            chemicalName("Cer(d18:1/24:0)").
            databaseIdentifier("nd").
            evidenceInputId("1").
            expMassToCharge(650.6373).
            msLevel(new Parameter().cvLabel("MS").
                cvAccession("MS:1000511").
                name("ms level").
                value("2")).
            rank(1).
            smiles(
                "CCCCCCCCCCCCCCCCCCCCCCCC(=O)N[C@@H](CO)[C@H](O)/C=C/CCCCCCCCCCCCC").
            theoreticalMassToCharge(649.6373).
            uri(URI.create("http://link.to.me/hj551a-2asdkj-12451")).
            addCommentItem(new Comment().prefix(Comment.PrefixEnum.COM).
                msg("Needs further investigation"));
        SmallMoleculeFeature smf = new SmallMoleculeFeature().smfId(1).
            addSmeIdRefsItem(sme.getSmeId()).
            adductIon("[M+H]1+").
            charge(1).
            expMassToCharge(650.6373).
            retentionTimeInSeconds(346.34).
            retentionTimeInSecondsEnd(349.87).
            retentionTimeInSecondsStart(342.98).
            smeIdRefAmbiguityCode(1).
            addOptItem(new OptColumnMapping().identifier("global").
                value("idk"));
        mzTab.addSmallMoleculeFeatureItem(smf);
        mzTab.addSmallMoleculeEvidenceItem(sme);
        SmallMoleculeSummary smsi = new SmallMoleculeSummary();
        smsi.smlId(1).
            addSmfIdRefsItem(smf.getSmfId()).
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
                URI.create("http://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012")).
            addTheoreticalNeutralMassItem(649.6373).
            addAdductIonsItem("[M+H]1+").
            reliability("1").
            bestIdConfidenceMeasure(new Parameter().name(
                "qualifier ions exact mass")).
            bestIdConfidenceValue(0.958).
            addAbundanceAssayItem(4.448784E-05).
            addAbundanceAssayItem(5.448784E-05).
            addAbundanceStudyVariableItem(4.448784E-05).
            addAbundanceStudyVariableItem(5.448784E-05).
            addAbundanceVariationStudyVariableItem(0.0d).
            addAbundanceVariationStudyVariableItem(0.00001d);
        mzTab.addSmallMoleculeSummaryItem(smsi);
        return mzTab;
    }

    @Test
    public void testWriteReadViaJsonMapper() throws IOException {
        File testFile = File.createTempFile(
            "testWriteJson", ".json");
        MzTab testMzTab = createTestMzTab();
        ObjectMapper mapper = new ObjectMapper();
        try (BufferedWriter bw = Files.newBufferedWriter(testFile.toPath(),
            StandardCharsets.UTF_8, StandardOpenOption.WRITE)) {
            bw.write(mapper.writeValueAsString(testMzTab));
        } catch (IOException ex) {
            Logger.getLogger(MzTabTest.class.getName()).
                log(Level.SEVERE, null, ex);
        }
        MzTab restoredFile = mapper.readValue(testFile, MzTab.class);
        Assert.assertEquals(testMzTab, restoredFile);
    }
    
    @Test
    public void testIndexedElementProxy() {
        MsRun mr = new MsRun().id(1).location(URI.create("file:///location"));
        Optional<IndexedElement> ie = IndexedElement.of(mr);
        Assert.assertEquals(mr.getId(), ie.get().getId());
    }
}
