/*
 * Copyright 2018 LIFS Tools Consortium.
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Implements handling of cv parameters (not user params) that are found at the object.
 * @author nilshoffmann
 */
public class ExtraParametersValidationHandler implements CvTermValidationHandler {

    /**
     * The maximum number of messages with an exact location per rule and term;
     * further elements with the same term are reported in one message without
     * location.
     */
    static final int MAX_LOCATED_PER_TERM = 100;

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
            // one message per element that uses such a parameter, up to
            // MAX_LOCATED_PER_TERM per term, then one for the further elements
            Map<String, Integer> elementsPerTerm = new HashMap<>();
            Map<String, Parameter> firstFurther = new LinkedHashMap<>();
            for (Pair<Pointer, Parameter> p : result.getFilteredSelection()) {
                String accession = p.getValue().
                    getCvAccession().
                    toUpperCase();
                if (!extraObjectParams.contains(accession)) {
                    continue;
                }
                int elements = elementsPerTerm.merge(accession, 1, Integer::sum);
                if (elements > MAX_LOCATED_PER_TERM) {
                    firstFurther.putIfAbsent(accession, p.getValue());
                    continue;
                }
                messages.add(RuleMessages.atPointer(error(result, p.getValue(),
                    errorOnTermNotInRule), p.getKey(), result.getRule()));
            }
            firstFurther.forEach((accession, parameter) -> messages.add(
                RuleMessages.onFurtherElements(error(result, parameter,
                    errorOnTermNotInRule),
                    elementsPerTerm.get(accession) - MAX_LOCATED_PER_TERM,
                    result.getRule())));
        }
        return messages;
    }

    /**
     * A term the rule does not allow: its level follows the rule's
     * requirement level (MUST: error, SHOULD: warning, MAY: info), like the
     * rule's other messages.
     */
    private static MZTabError error(RuleEvaluationResult result,
        Parameter parameter, boolean errorOnTermNotInRule) {
        MZTabErrorType type = MZTabErrorType.forLevel(
            MZTabErrorType.Category.CrossCheck,
            CvMappingUtils.toErrorLevel(result.getRule().getRequirementLevel()),
            errorOnTermNotInRule ? "CvTermNotAllowed" : "CvTermNotInRule");
        return new MZTabError(type,
            -1,
            new ParameterConverter().convert(parameter),
            result.getRule().
                getCvElementPath(),
            CvMappingUtils.niceToString(result.getRule()));
    }
}
