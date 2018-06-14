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
package de.isas.mztab2.validation.handlers;

import de.isas.mztab2.cvmapping.CvMappingUtils;
import de.isas.mztab2.cvmapping.RuleEvaluationResult;
import de.isas.mztab2.cvmapping.SetOperations;
import de.isas.mztab2.io.serialization.ParameterConverter;
import de.isas.mztab2.model.ValidationMessage;
import de.isas.mztab2.validation.CvTermValidationHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 *
 * @author nilshoffmann
 */
public class AndValidationHandler implements CvTermValidationHandler {

    @Override
    public List<ValidationMessage> handleParameters(RuleEvaluationResult result,
        boolean errorOnTermNotInRule) {
        final List<ValidationMessage> messages = new ArrayList<>();
        // all defined terms or children thereof need to appear
        Set<String> unmatchedRuleParams = SetOperations.complement(
            result.getAllowedParameters().
                keySet(), result.getFoundParameters().
                keySet());
        if (!unmatchedRuleParams.isEmpty()) {
            for (String s : unmatchedRuleParams) {
                MZTabErrorType errorType = null;
                switch (result.getRule().
                    getRequirementLevel()) {
                    case MAY:
                        errorType = CrossCheckErrorType.CvTermOptional;
                        break;
                    case SHOULD:
                        errorType = CrossCheckErrorType.CvTermRecommended;
                        break;
                    case MUST:
                        errorType = CrossCheckErrorType.CvTermRequired;
                        break;
                }
                MZTabError error = new MZTabError(errorType, -1,
                    new ParameterConverter().convert(result.
                        getAllowedParameters().
                        get(
                            s)), result.getRule().
                        getCvElementPath(), CvMappingUtils.
                        niceToString(
                            result.getRule()));
                messages.add(error.toValidationMessage());
            }
        }
        return messages;
    }
}
