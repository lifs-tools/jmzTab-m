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
package de.isas.mztab2.io;

import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.CV;
import de.isas.mztab2.model.Contact;
import de.isas.mztab2.model.Instrument;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.Publication;
import de.isas.mztab2.model.PublicationItem;
import de.isas.mztab2.model.Sample;
import de.isas.mztab2.model.SampleProcessing;
import de.isas.mztab2.model.Software;
import de.isas.mztab2.model.StudyVariable;
import de.isas.mztab2.model.Uri;
import java.util.Arrays;

/**
 *
 * @author nilshoffmann
 */
public class MzTabTestData {

    public static Metadata createSmallMetadata() {
        Metadata metadata
            = new de.isas.mztab2.model.Metadata().mzTabVersion("2.0.0-M").
                mzTabID("ISAS_2017_M_11451").
                title("A minimal test file").
                description("A description of an mzTab file.").
                addContactItem(
                    new Contact().id(1).
                        name("Nils Hoffmann").
                        email("nils.hoffmann_at_isas.de").
                        affiliation(
                            "ISAS e.V. Dortmund, Germany")
                ).
                addMsRunItem(
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

        metadata.
            addPublicationItem(publication1).
            addPublicationItem(publication2);
        return metadata;
    }

    public static MzTab createTestFile() {

        final MzTab mztabfile = new MzTab().metadata(
            new de.isas.mztab2.model.Metadata().mzTabVersion("2.0.0-M").
                mzTabID("ISAS_2017_M_11451").
                title("A minimal test file").
                description("A description of an mzTab file.").
                addContactItem(
                    new Contact().id(1).
                        name("Nils Hoffmann").
                        email("nils.hoffmann_at_isas.de").
                        affiliation(
                            "ISAS e.V. Dortmund, Germany")
                ).
                addMsRunItem(
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
            addPublicationItem(publication1).
            addPublicationItem(publication2);
        return mztabfile;
    }

    public static MzTab create2_0TestFile() {
        de.isas.mztab2.model.Metadata mtd = new de.isas.mztab2.model.Metadata();
        mtd.mzTabID("PRIDE_1234").
            mzTabVersion("2.0.0-M").
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
                "http://www.ebi.ac.uk/pride/url/to/experiment"));
        mtd.addUriItem(new Uri().id(2).
            value(
                "http://proteomecentral.proteomexchange.org/cgi/GetDataset"));
        mtd.addExternalStudyUriItem(new Uri().id(1).
            value(
                "https://www.ebi.ac.uk/metabolights/MTBLS400"));
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
        mtd.addMsRunItem(msRun1);
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
        mtd.addMsRunItem(msRun2);
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
            description(
                "Group A").
            addAssayRefsItem(
                assay1).
            addAssayRefsItem(assay2).
            addFactorsItem(new Parameter().name("spike-in").
                value("0.74 fmol/uL"));
        mtd.addStudyVariableItem(studyVariable1);
        StudyVariable studyVariable2 = new StudyVariable().
            id(2).
            description("Group B").
            addAssayRefsItem(assay1).
            addAssayRefsItem(assay2).
            addFactorsItem(new Parameter().name("spike-in").
                value("0.74 fmol/uL"));
        mtd.addStudyVariableItem(studyVariable2);
        mtd.addCvItem(new CV().id(1).
            label("MS").
            fullName("PSI-MS ontology").
            version("3.54.0").
            url("https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo"));
//
//        mtd.addQuantificationMethodItem(new Parameter().cvLabel("MS").
//            cvAccession("MS:1001837").
//            name("iTRAQ quantitation analysis"));
//        mtd.addQuantificationMethodItem(new Parameter().cvLabel("MS").
//            cvAccession("MS:1001838").
//            name("SRM quantitation analysis"));

        mtd.setSmallMoleculeQuantificationUnit(new Parameter().name(
            "Progenesis QI Normalised Abundance"));
        mtd.setSmallMoleculeFeatureQuantificationUnit(new Parameter().name(
            "Progenesis QI Normalised Abundance"));
        mtd.addIdConfidenceMeasureItem(new Parameter().id(1).
            name("some confidence measure term"));

        mtd.setSmallMoleculeIdentificationReliability(
            new Parameter().name(
                "Identification Reliability from 1 (lowest reliability) to 4 (highest reliability)"));

        MzTab mzTab = new MzTab();
        mzTab.metadata(mtd);
        return mzTab;
    }

}
