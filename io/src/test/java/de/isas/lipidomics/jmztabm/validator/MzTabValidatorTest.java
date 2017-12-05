package de.isas.lipidomics.jmztabm.validator;

import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.MzTabFileDescription;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.PublicationItem;
import java.util.Arrays;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for MzTabWriter.
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabValidatorTest {

    static MzTab createTestFile() {

        final MzTab mztabfile = new MzTab().metadata(
                new de.isas.mztab1_1.model.Metadata().
                        fileDescription(new MzTabFileDescription().mzTabVersion(
                                "1.1.0").
                                mzTabID("ISAS_2017_M_11451").
                                title("A minimal test file").
                                description("A description of an mzTab file.")).
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

        mztabfile.getMetadata().
                addPublicationsItem(publication1).
                addPublicationsItem(publication2);
        mztabfile.getMetadata().
                addCvItem(new CV().label("MS").
                        fullName("PSI-MS").
                        version("4.0.18").
                        url("https://github.com/HUPO-PSI/psi-ms-CV/blob/master/psi-ms.obo"));
        return mztabfile;
    }

    @Test
    public void testMetadataBeanValidation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<MzTab>> violations = validator.validate(createTestFile());
        for(ConstraintViolation<MzTab> violation:violations) {
            System.err.println("Validation error at "+violation.getPropertyPath().toString()+": "+violation.getMessage());
        }
    }
}
