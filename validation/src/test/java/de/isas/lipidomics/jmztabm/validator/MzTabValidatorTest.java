package de.isas.lipidomics.jmztabm.validator;

import static de.isas.lipidomics.jmztabm.cvmapping.JxPathElement.toStream;
import de.isas.lipidomics.jmztabm.validation.MzTabValidator;
import de.isas.mztab.jmztabm.test.utils.LogMethodName;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.PublicationItem;
import de.isas.mztab1_1.model.ValidationMessage;
import info.psidev.cvmapping.CvMapping;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
            email("nils.hoffmann_at_isas.de").
            affiliation(
                "ISAS e.V. Dortmund, Germany");
        contact1.id(1);
        MsRun msRun1 = new MsRun().
            location("file:///path/to/file1.mzML").
            format(
                new Parameter().
                    cvLabel("MS").
                    cvAccession("MS:1000584").
                    name("mzML file").
                    value("")
            ).
            idFormat(
                new Parameter().
                    cvLabel("MS").
                    cvAccession("MS:1001530").
                    name("mzML unique identifier").
                    value("")
            );
        msRun1.id(1);
        final MzTab mztabfile = new MzTab().metadata(
            new de.isas.mztab1_1.model.Metadata().mzTabVersion(
                "1.1.0").
                mzTabID("ISAS_2017_M_11451").
                title("A minimal test file").
                description("A description of an mzTab file.").
                addContactsItem(
                    contact1
                ).
                addMsrunItem(
                    msRun1
                )
        );
        PublicationItem item1_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("21063943");
        PublicationItem item1_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1007/978-1-60761-987-1_6");
        Publication publication1 = new Publication();
        publication1.id(1);
        publication1.publicationItems(Arrays.asList(item1_1, item1_2));

        PublicationItem item2_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("20615486");
        PublicationItem item2_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1016/j.jprot.2010.06.008");
        Publication publication2 = new Publication();
        publication2.id(2);
        publication2.publicationItems(Arrays.asList(item2_1, item2_2));

        mztabfile.getMetadata().
            addPublicationsItem(publication1).
            addPublicationsItem(publication2);
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
        MzTabValidator validator = new MzTabValidator();
        List<ValidationMessage> violations = validator.validate(
            createTestFile());
        for (ValidationMessage violation : violations) {
            System.err.println(violation);
        }
    }

    @Test
    public void testJXpathAccess() {
        MzTab mzTab = createTestFile();
        JXPathContext context = JXPathContext.newContext(mzTab);

        List<?> msRuns = (List<?>) context.getValue("//metadata/msrun",
            List.class);
        assertFalse(msRuns.isEmpty());
        assertEquals("file:///path/to/file1.mzML", toStream(context.
            getPointer("//metadata/msrun/@location"), String.class).
            findFirst().
            map((t) ->
            {
                System.out.println("Path: " + t.getLeft() + " to object: " + t.
                    getRight());
                return t;
            }).
            get().
            getValue());
        Stream<? extends String> stream = toStream(context.iterate(
            "//metadata/msrun/@location"), String.class);
        assertEquals("file:///path/to/file1.mzML", stream.findFirst().
            get());

        //scopePath //metadata/msrun
        Stream<Pair<Pointer, ? extends Parameter>> pointerFormatParameters = toStream(
            context.getPointer(
                "//metadata/msrun/@format"), Parameter.class);
        assertEquals("MS:1000584", pointerFormatParameters.findFirst().
            get().
            getValue().
            getCvAccession());

        Stream<? extends Parameter> formatParameters = toStream(context.iterate(
            "//metadata/msrun/@format"), Parameter.class);
    }

    @Test
    public void testJaxbCvMapping() throws JAXBException {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        JAXBContext jaxbContext = JAXBContext.newInstance(CvMapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        CvMapping mapping = (CvMapping) u.unmarshal(MzTabValidatorTest.class.
            getResourceAsStream("/mzTab-M-test-mapping.xml"));
        MzTab mzTab = createTestFile();
        JXPathContext context = JXPathContext.newContext(mzTab);
        mapping.getCvMappingRuleList().
            getCvMappingRule().
            forEach((rule) ->
            {
                String path = rule.getCvElementPath(); // this selects all nodes for that rule
                Stream<Pair<Pointer, ? extends Parameter>> pointerFormatParameters = toStream(
                    context.getPointer(
                        path), Parameter.class);
                System.out.println("Applying rule: " + rule);
                pointerFormatParameters.forEach((selection) ->
                {
                    System.out.println("Applying to selection: " + selection);
                    // TODO: implement combination logic (OR, AND, XOR)
                    rule.getCvTerm().
                        forEach((term) ->
                        {
                            List<Term> comparisonList;
                            if (term.isAllowChildren()) {
                                Identifier ident = new Identifier(term.
                                    getTermAccession(),
                                    Identifier.IdentifierType.OBO);
                                comparisonList = client.getTermChildren(ident,
                                    term.getCvIdentifierRef().
                                        getCvIdentifier(), 1);
                                boolean match = false;
//                                comparisonList.stream().filter((comparisonTerm) ->
//                                    {
////                                       return comparisonTerm.get 
//                                    });
                            } else {
                                if (term.getCvIdentifierRef().
                                    getCvIdentifier().
                                    equals(selection.getRight().
                                        getCvLabel()) && term.getTermAccession().
                                        equals(selection.getRight().
                                            getCvAccession())) {
                                    //positive, we have a match
                                    System.out.println("Rule "+rule+" matched on: "+term + " for selection: "+ selection);
                                } else {
                                    System.err.println(
                                        "Mismatch of rule " + rule + " at " + selection.
                                            getLeft());
                                    throw new RuntimeException("Rule was not successfully applied!");
                                    //we do not have a match, if we are in OR mode, if we are in AND mode, this should issue a violation
                                }
                            }
                        });

                });
            });
    }

}
