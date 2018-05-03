package de.isas.lipidomics.jmztabm.validator;

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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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
}
