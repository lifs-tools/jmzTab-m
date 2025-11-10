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
package org.lifstools.mztab2.validation.constraints;

import org.lifstools.mztab2.validation.validators.ParameterValidator;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * JAVAX validation compliant annotation. Currently unused.
 * @author nilshoffmann
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ParameterValidator.class)
@Documented
public @interface CheckParameter {
    String message() default "{de.isas.mztab.jmztabm.validation.constraints.CheckParameter." +
            "message}";
    String messageCode() default "0";
    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
