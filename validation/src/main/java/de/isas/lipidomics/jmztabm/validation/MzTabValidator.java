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
import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;

/**
 * <p>
 * MzTabValidator class.</p>
 *
 * @author nilshoffmann
 *
 */
public class MzTabValidator implements Validator<MzTab> {

    private final HibernateValidatorConfiguration configuration;
    private final ConstraintMapping constraintMapping;

    /**
     * Default constructor. Fail fast validation is disabled.
     */
    public MzTabValidator() {
        this(false);
    }

    /**
     * Constructor setting up the validator configuration and default constraint
     * mapping.
     *
     * @param failFast if true, first validation error will terminate any
     * further validation. If false, validation will continue and report all
     * validation errors.
     */
    public MzTabValidator(boolean failFast) {
        this.configuration = Validation
            .byProvider(HibernateValidator.class).
            configure();

        this.configuration.failFast(failFast);
        this.constraintMapping = configuration.
            createConstraintMapping();

    }

    /**
     * Allows registration of custom constraint / validator pairs on the
     * validation configuration. This requires that the objects to be validated
     * are annotated with the corresponding constraint definition.
     *
     * @param <A> The annotation marking elements that should be validated.
     * @param <T> The Object type on which the validation should be performed.
     * @param <V> The validator to use for the validation.
     * @param constraintDefinition The annotation marking elements that should
     * be validated.
     * @param validator The validator to use for the validation.
     * @param includeExistingValidators If true, existing validators for the
     * same type will be applied, too. If false, only the registered validator
     * will be retained for that type.
     */
    public <A extends Annotation, T extends Object, V extends ConstraintValidator<A, T>> void addConstraintAndValidator(
        Class<A> constraintDefinition, Class<V> validator,
        boolean includeExistingValidators) {
        constraintMapping.constraintDefinition(constraintDefinition).
            includeExistingValidators(includeExistingValidators).
            validatedBy(validator);
    }

    /**
     * Allows registration of custom constraint / validator pairs on the
     * validation configuration for a specific (unannotated) type.
     *
     * @param <A> The annotation marking elements that should be validated.
     * @param <T> The Object type on which the validation should be performed.
     * @param <V> The validator to use for the validation.
     * @param typeToValidate The class/type that should be validated.
     * @param constraintDefinition The annotation marking elements that should
     * be validated.
     * @param validator The validator to use for the validation.
     * @param includeExistingValidators If true, existing validators for the
     * same type will be applied, too. If false, only the registered validator
     * will be retained for that type.
     */
    public <A extends Annotation, T extends Object, V extends ConstraintValidator<A, T>> void addConstraintAndValidator(
        Class<T> typeToValidate,
        Class<A> constraintDefinition, Class<V> validator,
        boolean includeExistingValidators) {
        constraintMapping.type(typeToValidate).
            constraintDefinition(constraintDefinition).
            includeExistingValidators(includeExistingValidators).
            validatedBy(validator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ValidationMessage> validate(MzTab mzTab) {
        List<ValidationMessage> list = new LinkedList<>();

        javax.validation.Validator validator = configuration.addMapping(
            constraintMapping).
            buildValidatorFactory().
            getValidator();

        Set<ConstraintViolation<MzTab>> violations = validator.validate(mzTab);
        for (ConstraintViolation<MzTab> violation : violations) {
            list.add(new ValidationMessage().message(getPathLocatorString(
                violation) + ": " + violation.getMessage()).
                messageType(ValidationMessage.MessageTypeEnum.ERROR));
        }
        
        //add additional validators
        
        return list;
    }

    /**
     * <p>
     * getPathLocatorString.</p>
     *
     * @param cv a {@link javax.validation.ConstraintViolation} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getPathLocatorString(ConstraintViolation<?> cv) {
        return cv.getPropertyPath().
            toString();
    }
}
