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
package org.lifstools.mztab2.validation;

import org.lifstools.mztab2.cvmapping.RuleEvaluationResult;
import org.lifstools.mztab2.model.ValidationMessage;
import java.util.List;

/**
 * Interface for handling of rule evaluation results.
 * @author nilshoffmann
 */
public interface CvTermValidationHandler {
    /**
     * Implementations of this method should take care that they always return a list,
     * even if it is empty.
     * @param result the validation messages produced by this handler.
     * @param errorOnTermNotInRule flag to signal, whether unknown terms should yield an error.
     * @return a list of validation messages.
     */
    List<ValidationMessage> handleParameters(RuleEvaluationResult result,
        boolean errorOnTermNotInRule);
}
