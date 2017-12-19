/*
 * 
 */
package de.isas.lipidomics.jmztabm.validation;

import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.SmallMoleculeEvidence;
import de.isas.mztab1_1.model.SmallMoleculeFeature;
import de.isas.mztab1_1.model.SmallMoleculeSummary;
import de.isas.mztab1_1.model.ValidationMessage;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabValidator implements Validator {

    @Override
    public List<ValidationMessage> validate(MzTab mzTab) {
        List<ValidationMessage> list = new LinkedList<>();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        javax.validation.Validator validator = factory.getValidator();
        Set<ConstraintViolation<MzTab>> violations = validator.validate(mzTab);
        for(ConstraintViolation<MzTab> violation:violations) {
            list.add(new ValidationMessage().message(violation.getPropertyPath().toString()+": "+violation.getMessage()));
        }
        return list;
    }
}
