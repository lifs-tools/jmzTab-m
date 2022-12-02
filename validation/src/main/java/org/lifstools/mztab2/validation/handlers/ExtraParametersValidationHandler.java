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
import java.util.Set;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * Implements handling of cv parameters (not user params) that are found at the object.
 * @author nilshoffmann
 */
public class ExtraParametersValidationHandler implements CvTermValidationHandler {

    @Override
    public List<ValidationMessage> handleParameters(RuleEvaluationResult result,
        boolean errorOnTermNotInRule) {
        final List<ValidationMessage> messages = new ArrayList<>();
        //handle (non-user) parameters that were found at the object but are not defined in the rule
        Set<String> extraObjectParams = SetOperations.complement(
            result.getFoundParameters().
                keySet(),
            result.getAllowedParameters().
                keySet());
        if (!extraObjectParams.isEmpty()) {
            for (String s : extraObjectParams) {
                Pair<Pointer, ? extends Parameter> p = result.
                    getFoundParameters().
                    get(s);
                MZTabError error;
                if (errorOnTermNotInRule) {
                    error = new MZTabError(CrossCheckErrorType.CvTermNotAllowed,
                        -1,
                        new ParameterConverter().convert(p.
                            getValue()), result.getRule().
                            getCvElementPath(),
                        CvMappingUtils.niceToString(result.getRule()));
                } else {
                    error = new MZTabError(CrossCheckErrorType.CvTermNotInRule,
                        -1,
                        new ParameterConverter().convert(p.
                            getValue()), result.getRule().
                            getCvElementPath(),
                        CvMappingUtils.niceToString(result.getRule()));
                }
                messages.add(error.toValidationMessage());
            }
        }
        return messages;
    }
}
