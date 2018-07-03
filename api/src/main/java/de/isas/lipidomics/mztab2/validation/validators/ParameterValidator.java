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
package de.isas.lipidomics.mztab2.validation.validators;

import de.isas.lipidomics.mztab2.validation.constraints.CheckParameter;
import de.isas.mztab2.model.Parameter;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Base interface for parameter validation with default validation method.
 *
 * @author nilshoffmann
 */
public interface ParameterValidator extends ConstraintValidator<CheckParameter, Parameter> {

    @Override
    default boolean isValid(Parameter parameter,
        ConstraintValidatorContext context) {
        if (parameter==null || (parameter.getCvLabel() != null && parameter.getCvAccession() != null && parameter.
            getName() != null) || (parameter.getName() != null && parameter.
            getValue() != null)) {
            return true;
        }
        return false;
    }
}
