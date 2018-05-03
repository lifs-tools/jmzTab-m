/*
 * Copyright 2018 nilshoffmann.
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
package de.isas.lipidomics.jmztabm.cvmapping;

import de.isas.mztab1_1.model.Parameter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 *
 * @author nilshoffmann
 */
public class CheckCvMappingValidator implements ConstraintValidator<CheckCvMapping, Parameter> {

    @Override
    public void initialize(CheckCvMapping constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Parameter value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        Logger.getLogger(CheckCvMappingValidator.class.getName()).log(Level.INFO, "Validating {0}", value);

        //the parameter is a user parameter, when both label and accession are null or empty
        if (isNullOrEmpty(value.getCvLabel()) && isNullOrEmpty(value.
            getCvAccession())) {
            return true;
        }

        //validate parameter against mapping path -> pull path from mapping file and compare allowed cv parameters
        boolean isValid = true;
//        System.out.println(valueContext.getPropertyPath());
//        hibernateContext.
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "{de.isas.mztab.jmztabm.cvmapping.CheckCvMapping."
                + "constraintvalidatorcontext.CheckCvMapping.message}"
            ).
                addConstraintViolation();
        }
        return isValid;
    }

    private boolean isNullOrEmpty(String value) {
        return (value == null || value.isEmpty());
    }
}
