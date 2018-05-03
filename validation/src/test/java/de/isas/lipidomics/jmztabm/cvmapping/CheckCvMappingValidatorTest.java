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

import de.isas.lipidomics.jmztabm.cvmapping.CheckCvMapping;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.ValidationMessage;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import org.junit.Assert;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author nilshoffmann
 */
public class CheckCvMappingValidatorTest {

    private HibernateValidatorConfiguration configuration;
    private ConstraintMapping constraintMapping;

    @Before
    public void initialize() {
        this.configuration = Validation
            .byProvider(HibernateValidator.class).
            configure();

        this.configuration.failFast(false);
        this.constraintMapping = configuration.
            createConstraintMapping();
    }

    /**
     * Test of isValid method, of class CheckCvMappingValidator.
     */
    @Test
    public void testIsValid() {
        List<ValidationMessage> list = new LinkedList<>();

        Logger.getLogger(CheckCvMappingValidatorTest.class.getName()).
            log(Level.INFO, "Setting up constraint definition!");
        constraintMapping.type(Parameter.class).
            constraint(new GenericConstraintDef<>(CheckCvMapping.class));

        javax.validation.Validator validator = configuration.addMapping(
            constraintMapping).
            buildValidatorFactory().
            getValidator();

        Parameter parameter = new Parameter().id(1).
            cvLabel("MS").
            cvAccession("879123").
            name("jklajsd").
            value("klhasd");

        Logger.getLogger(CheckCvMappingValidatorTest.class.getName()).
            log(Level.INFO, "Running validation!");
        Set<ConstraintViolation<Object>> violations = validator.validate(
            parameter);
        Assert.assertEquals(0, violations.size());

    }

}
