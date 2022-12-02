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
package org.lifstools.mztab2.validation.handlers;

import org.lifstools.mztab2.cvmapping.CvMappingUtils;
import org.lifstools.mztab2.cvmapping.RuleEvaluationResult;
import org.lifstools.mztab2.cvmapping.SetOperations;
import org.lifstools.mztab2.io.serialization.ParameterConverter;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.ValidationMessage;
import org.lifstools.mztab2.validation.CvTermValidationHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * Implements support for validating multiple parameters and the intersection
 * between terms as described by the rule and the actually found ones.
 *
 * @author nilshoffmann
 */
public class SharedParametersValidationHandler implements CvTermValidationHandler {

    @Override
    public List<ValidationMessage> handleParameters(
            RuleEvaluationResult result,
            boolean errorOnTermNotInRule) {
        final List<ValidationMessage> messages = new ArrayList<>();
        Set<String> sharedParameters = SetOperations.intersection(
                result.getAllowedParameters().
                        keySet(), result.getFoundParameters().
                        keySet());
        for (String paramKey : sharedParameters) {
            Pair<Pointer, ? extends Parameter> p = result.getFoundParameters().
                    get(paramKey);
            Parameter allowedParameter = result.getAllowedParameters().
                    get(paramKey);
            //The cv parameter field "{0}" for parameter "{1}" is "{2}" but should be "{3}", as defined in {4}.
            if (!Optional.ofNullable(p.getValue().
                    getCvLabel()).
                    orElse("").
                    equals(allowedParameter.getCvLabel())) {
                MZTabError error = new MZTabError(
                        CrossCheckErrorType.CvTermMalformed,
                        -1,
                        Parameter.Properties.cvLabel.getPropertyName(),
                        new ParameterConverter().convert(p.
                                getValue()), p.getValue().
                                getCvLabel(), allowedParameter.getCvLabel(),
                        CvMappingUtils.niceToString(result.getRule()));

                messages.add(error.toValidationMessage());
            }
            if (!Optional.ofNullable(p.getValue().
                    getCvAccession()).
                    orElse("").
                    equals(allowedParameter.getCvAccession())) {
                MZTabError error = new MZTabError(
                        CrossCheckErrorType.CvTermMalformed,
                        -1,
                        Parameter.Properties.cvAccession.getPropertyName(),
                        new ParameterConverter().convert(p.
                                getValue()), p.getValue().
                                getCvAccession(), allowedParameter.getCvAccession(),
                        CvMappingUtils.niceToString(result.getRule()));

                messages.add(error.toValidationMessage());
            }
            if (!Optional.ofNullable(p.getValue().
                    getName()).
                    orElse("").
                    equals(allowedParameter.getName())) {
                MZTabError error = new MZTabError(
                        CrossCheckErrorType.CvTermMalformed,
                        -1,
                        Parameter.Properties.name.getPropertyName(),
                        new ParameterConverter().convert(p.
                                getValue()), p.getValue().
                                getName(), allowedParameter.getName(),
                        CvMappingUtils.niceToString(result.getRule()));

                messages.add(error.toValidationMessage());
            }
        }
        return messages;
    }

}
