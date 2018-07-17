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
import static de.isas.mztab2.cvmapping.CvMappingUtils.toErrorLevel;
import de.isas.mztab2.cvmapping.RuleEvaluationResult;
import de.isas.mztab2.io.serialization.ParameterConverter;
import de.isas.mztab2.model.ValidationMessage;
import de.isas.mztab2.validation.CvTermValidationHandler;
import info.psidev.cvmapping.CvMappingRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 *
 * @author nilshoffmann
 */
public class XorValidationHandler implements CvTermValidationHandler {

    @Override
    public List<ValidationMessage> handleParameters(RuleEvaluationResult result,
        boolean errorOnTermNotInRule) {
        // xor logic means, if one of the defined terms or its children is set, none of the others is allowed
        final List<ValidationMessage> messages = new ArrayList<>();
        // all defined terms or children thereof need to appear
        Set<String> matchedParameters = new HashSet<String>();
        matchedParameters.addAll(result.getAllowedParameters().
            keySet());
        matchedParameters.retainAll(result.getFoundParameters().
            keySet());
        if (matchedParameters.isEmpty()) {
            for (String s : result.getAllowedParameters().
                keySet()) {
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
                    default:
                        throw new IllegalArgumentException(
                            "Unknown requirement level value: " + result.
                                getRule().
                                getRequirementLevel() + "! Supported are: " + Arrays.
                                toString(CvMappingRule.RequirementLevel.
                                    values()));
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
        } else if (matchedParameters.size() > 1) {
            //Only one of the provided cv terms for "{1}" {0} be reported. You defined terms "{2}". Allowed terms are defined in rule "{3}".
            String definedParameters = matchedParameters.stream().
                collect(Collectors.joining(", "));
            MZTabErrorType xorErrorType = MZTabErrorType.forLevel(
                MZTabErrorType.Category.CrossCheck,
                toErrorLevel(result.getRule().
                    getRequirementLevel()), "CvTermXor");
            MZTabError error = new MZTabError(xorErrorType, -1,
                result.getRule().
                    getRequirementLevel().
                    value(), result.getRule().
                    getCvElementPath(), definedParameters,
                CvMappingUtils.niceToString(result.getRule()));
            messages.add(error.toValidationMessage());
        }
        return messages;
    }
}
