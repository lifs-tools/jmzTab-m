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
package org.lifstools.mztab2.validation;

import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Contact;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.Publication;
import org.lifstools.mztab2.model.PublicationItem;
import org.lifstools.mztab2.model.Sample;
import org.lifstools.mztab2.model.ValidationMessage;
import org.lifstools.mztab2.test.utils.LogMethodName;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.bind.JAXBException;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test class for MzTabWriter.
 *
 * @author nilshoffmann
 */
public class MzTabValidatorTest {

    @Rule
    public LogMethodName methodNameLogger = new LogMethodName();

    public static MzTab createTestFile() {
        Contact contact1 = new Contact().
            name("Nils Hoffmann").
            email("nils.hoffmann@isas.de").
            affiliation(
                "ISAS e.V. Dortmund, Germany");
        contact1.id(1);
        MsRun msRun1 = new MsRun().id(1).
            location("file:///path/to/file1.mzML").
            format(
                new Parameter().id(1).
                    cvLabel("MS").
                    cvAccession("MS:1000584").
                    name("mzML file").
                    value("")
            ).
            idFormat(
                new Parameter().id(1).
                    cvLabel("MS").
                    cvAccession("MS:1001530").
                    name("mzML unique identifier").
                    value("")
            ).
            addScanPolarityItem(
                new Parameter().id(1).
                    cvLabel("MS").
                    cvAccession("MS:1000129").
                    name("negative scan")
            ).addScanPolarityItem(
                new Parameter().id(1).
                    cvLabel("MS").
                    cvAccession("MS:1000130").
                    name("positive scan")
            );
        final MzTab mztabfile = new MzTab().metadata(
            new org.lifstools.mztab2.model.Metadata().mzTabVersion(
                "2.0.0-M").
                mzTabID("ISAS_2017_M_11451").
                title("A minimal test file").
                description("A description of an mzTab file.").
                addContactItem(
                    contact1
                ).
                addMsRunItem(
                    msRun1
                )
        );
        Sample sample1 = new Sample().id(1).
            description("Hepatocellular carcinoma samples.").
            addSpeciesItem(new Parameter().cvLabel("NCBITaxon").
                cvAccession("NCBITaxon:9606").
                name(
                    "Homo sapiens (Human)")).
            addSpeciesItem(new Parameter().cvLabel("NCBITaxon").
                cvAccession("NCBITaxon:12134").
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
                cvAccession("DOID:9452").
                name("alcoholic fatty liver")).
            addCustomItem(new Parameter().name("Extraction date").
                value("2011-12-21")).
            addCustomItem(new Parameter().name("Extraction reason").
                value("liver biopsy"));
        mztabfile.getMetadata().
            addSampleItem(sample1);
        PublicationItem item1_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("21063943");
        PublicationItem item1_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1007/978-1-60761-987-1_6");
        Publication publication1 = new Publication().id(1);
        publication1.publicationItems(Arrays.asList(item1_1, item1_2));

        PublicationItem item2_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("20615486");
        PublicationItem item2_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1016/j.jprot.2010.06.008");
        Publication publication2 = new Publication().id(2);
        publication2.publicationItems(Arrays.asList(item2_1, item2_2));

        mztabfile.getMetadata().
            addPublicationItem(publication1).
            addPublicationItem(publication2);
        CV cv1 = new CV().label("MS").
            fullName("PSI-MS").
            version("4.0.18").
            uri("https://github.com/HUPO-PSI/psi-ms-CV/blob/master/psi-ms.obo");
        cv1.id(1);
        mztabfile.getMetadata().
            addCvItem(cv1);
        return mztabfile;
    }

    @Test
    public void testDefaultMetadataBeanValidation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<MzTab>> violations = validator.validate(
            createTestFile());
        for (ConstraintViolation<MzTab> violation : violations) {
            System.err.println("Validation error at " + violation.
                getPropertyPath().
                toString() + ": " + violation.getMessage());
        }
    }

    @Test
    public void testCustomBeanValidation() {
        MzTabBeanValidator validator = new MzTabBeanValidator();
        List<ValidationMessage> violations = validator.validate(
            createTestFile());
        for (ValidationMessage violation : violations) {
            System.err.println(violation);
        }
    }

    @Test
    public void testDelegatingValidator() {
        List<ValidationMessage> messages = MzTabValidator.validate(
            createTestFile(), ValidationMessage.MessageTypeEnum.INFO,
            new MzTabBeanValidator(false));
        for (ValidationMessage violation : messages) {
            System.err.println(violation);
        }
    }

    @Test
    public void testJaxbCvMapping() throws JAXBException {
        MzTab mzTab = createTestFile();
        CvMappingValidator validator = CvMappingValidator.of(
            MzTabValidatorTest.class.
                getResource("/mappings/mzTab-M-mapping.xml"), true);
        List<ValidationMessage> validationMessages = validator.validate(mzTab);
        for (ValidationMessage violation : validationMessages) {
            System.err.println(violation);
        }
    }

}
