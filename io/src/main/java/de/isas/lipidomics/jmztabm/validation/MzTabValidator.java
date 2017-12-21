/*
 * 
 */
package de.isas.lipidomics.jmztabm.validation;

import de.isas.mztab1_1.model.MzTab;
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
        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                .configure()
                .buildValidatorFactory();
        javax.validation.Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<MzTab>> violations = validator.validate(mzTab);
        for(ConstraintViolation<MzTab> violation:violations) {
            list.add(new ValidationMessage().message(getPathLocatorString(
                violation)+": "+violation.getMessage()).messageType(ValidationMessage.MessageTypeEnum.ERROR));
        }
        return list;
    }
    
    protected String getPathLocatorString(ConstraintViolation<?> cv) {
        return cv.getPropertyPath().toString();
    }
}
