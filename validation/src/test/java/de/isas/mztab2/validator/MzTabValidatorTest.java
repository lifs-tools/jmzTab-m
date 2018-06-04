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
package de.isas.mztab2.validator;

import de.isas.mztab2.cvmapping.rules.CvPartialRuleEvalutionResult;
import static de.isas.mztab2.cvmapping.JxPathElement.toStream;
import de.isas.mztab2.cvmapping.rules.CvMappingUtils;
import de.isas.mztab2.validation.MzTabBeanValidator;
import de.isas.lipidomics.jmztabm.validation.MzTabValidator;
import de.isas.mztab2.test.utils.LogMethodName;
import de.isas.mztab2.model.CV;
import de.isas.mztab2.model.Contact;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.Publication;
import de.isas.mztab2.model.PublicationItem;
import de.isas.mztab2.model.ValidationMessage;
import info.psidev.cvmapping.CvMapping;
import static info.psidev.cvmapping.CvMappingRule.RequirementLevel.MUST;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

/**
 * Test class for MzTabWriter.
 *
 * @author nilshoffmann
 */
public class MzTabValidatorTest {

    @Rule
    public LogMethodName methodNameLogger = new LogMethodName();

    static MzTab createTestFile() {
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
            );
        final MzTab mztabfile = new MzTab().metadata(
            new de.isas.mztab2.model.Metadata().mzTabVersion(
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
            url("https://github.com/HUPO-PSI/psi-ms-CV/blob/master/psi-ms.obo");
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

    

    @Ignore
    @Test
    public void testJaxbCvMapping() throws JAXBException {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        JAXBContext jaxbContext = JAXBContext.newInstance(CvMapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        CvMapping mapping = (CvMapping) u.unmarshal(MzTabValidatorTest.class.
            getResourceAsStream("/mzTab-M-test-mapping.xml"));
        MzTab mzTab = createTestFile();
        List<ValidationMessage> validationMessages = new ArrayList<>();
        JXPathContext context = JXPathContext.newContext(mzTab);
        mapping.getCvMappingRuleList().
            getCvMappingRule().
            forEach((rule) ->
            {
                String path = rule.getCvElementPath(); // this selects all nodes for that rule
                try {
                    Stream<Pair<Pointer, ? extends Parameter>> pointerFormatParameters = toStream(
                        context.getPointer(
                            path), Parameter.class);
                    System.out.println("Applying rule: " + CvMappingUtils.toString(rule));
                    pointerFormatParameters.forEach((selection) ->
                    {
                        System.out.
                            println("Applying to selection: " + selection);
                        boolean skip = false;
                        ValidationMessage message;
                        switch (rule.getRequirementLevel()) {
                            case MAY:
                                //info level
                                if (selection.getValue() == null) {
                                    validationMessages.add(
                                        new ValidationMessage().
                                            code("SV-1981").
                                            messageType(
                                                ValidationMessage.MessageTypeEnum.INFO).
                                            message(
                                                "Parameter at path '" + selection.
                                                    getKey().
                                                    asPath() + "' was null!"));
                                    System.out.
                                        println(
                                            "Skipping validation of null parameter for selection: " + selection);
                                    skip = true;
                                }
                                break;
                            case SHOULD:
                                //warning level
                                if (selection.getValue() == null) {
                                    validationMessages.add(
                                        message = new ValidationMessage().
                                            code("SV-1981").
                                            messageType(
                                                ValidationMessage.MessageTypeEnum.WARN).
                                            message(
                                                "Parameter at path '" + selection.
                                                    getKey().
                                                    asPath() + "' " + rule.
                                                    getRequirementLevel() + " not be null!"));
                                    System.out.
                                        println(
                                            "Skipping validation of null parameter for selection: " + selection);
                                    skip = true;
                                }
                                break;
                            case MUST:
                                if (selection.getValue() == null) {
                                    //error level
                                    validationMessages.add(
                                        new ValidationMessage().
                                            code("SV-1981").
                                            messageType(
                                                ValidationMessage.MessageTypeEnum.ERROR).
                                            message(
                                                "Parameter at path '" + selection.
                                                    getKey().
                                                    asPath() + "' " + rule.
                                                    getRequirementLevel() + " not be null!"));
                                    System.err.println(
                                        "Rule requirement level MUST requires defined CvTerms to be non-null for rule: " + CvMappingUtils.toString(
                                            rule) + " for context: " + selection);
                                    skip = true;
                                }
                                break;
                            default:
                                throw new IllegalStateException(
                                    "Unhandled requirement level: " + rule.
                                        getRequirementLevel());
                        }
                        if (!skip) {
                            // TODO: implement combination logic (OR, AND, XOR)
                            rule.getCvTerm().
                                forEach((term) ->
                                {
                                    List<Term> comparisonList;
                                    List<CvPartialRuleEvalutionResult> ruleEvalResults = new ArrayList<>();
                                    if (term.isAllowChildren()) {
                                        Identifier ident = new Identifier(term.
                                            getTermAccession(),
                                            Identifier.IdentifierType.OBO);
                                        comparisonList = client.getTermChildren(
                                            ident,
                                            term.getCvIdentifierRef().
                                                getCvIdentifier(), 1);
                                        boolean match = false;
                                        //                                comparisonList.stream().filter((comparisonTerm) ->
                                        //                                    {
                                        ////                                       return comparisonTerm.get 
                                        //                                    });
                                    } else {
                                        Term t = new Term();
                                        t.setOntologyPrefix(term.
                                            getCvIdentifierRef().
                                            getCvIdentifier());
                                        t.setOboId(term.getTermAccession());
                                        t.setLabel(term.getTermName());
                                        comparisonList = Arrays.asList(t);
                                        if (term.getCvIdentifierRef().
                                            getCvIdentifier().
                                            equals(selection.getRight().
                                                getCvLabel()) && term.
                                                getTermAccession().
                                                equals(selection.getRight().
                                                    getCvAccession())) {
                                            //positive, we have a match
                                            System.out.println(
                                                "Rule " + CvMappingUtils.toString(rule) + " matched on: " + term + " for selection: " + selection);
                                        } else {
                                            String errMessage = "Mismatch of rule " + CvMappingUtils.toString(
                                                rule) + " at " + selection.
                                                    getLeft();
                                            System.err.println(errMessage);
                                            throw new RuntimeException(
                                                errMessage);
                                            //we do not have a match, if we are in OR mode, if we are in AND mode, this should issue a violation
                                        }
                                    }
                                });
                        }
                    });
                } catch (org.apache.commons.jxpath.JXPathNotFoundException ex) {
                    System.err.println(ex.getLocalizedMessage());
                }
            });
    }

}
