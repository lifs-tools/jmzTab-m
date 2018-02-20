/*
 * Copyright 2017 Leibniz Institut für Analytische Wissenschaften - ISAS e.V..
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
 * @author Nils Hoffmann &lt;nils.hoffmann@isas.de&gt;
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
