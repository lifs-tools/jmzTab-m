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
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.ValidationMessage;
import info.psidev.cvmapping.CvMappingRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * Implements handling of rules that have no cv parameter match. Depending on
 * the requirement level, this may raise an Info, Warning or Error level
 * message.
 *
 * @author nilshoffmann
 */
@Slf4j
public class EmptyRuleHandler {

    public List<ValidationMessage> handleRule(CvMappingRule rule,
            List<Pair<Pointer, Parameter>> selection) {
        if (selection.isEmpty()) {
            log.debug(
                    "Evaluating rule " + rule.getId() + " on " + rule.
                    getCvElementPath() + " did not yield any selected elements!");
            switch (rule.getRequirementLevel()) {
                case MAY:
                    return Arrays.asList(new MZTabError(
                            CrossCheckErrorType.RulePointerObjectNullOptional, -1,
                            rule.getCvElementPath(), rule.getId(), "optional",
                            CvMappingUtils.niceToString(rule)).
                            toValidationMessage());
                case SHOULD:
                    return Arrays.asList(new MZTabError(
                            CrossCheckErrorType.RulePointerObjectNullRecommended, -1,
                            rule.getCvElementPath(), rule.getId(), "recommended",
                            CvMappingUtils.niceToString(rule)).
                            toValidationMessage());
                case MUST:
                    //The object "{0}" accessed by {1} is {2}, but was null or empty. Allowed terms are defined in {3}
                    return Arrays.asList(new MZTabError(
                            CrossCheckErrorType.RulePointerObjectNullRequired, -1,
                            rule.getCvElementPath(), rule.getId(), "required",
                            CvMappingUtils.niceToString(rule)).
                            toValidationMessage());
                default:
                    throw new IllegalArgumentException(
                            "Unknown requirement level value: " + rule.
                                    getRequirementLevel() + "! Supported are: " + Arrays.
                                    toString(CvMappingRule.RequirementLevel.
                                            values()));

            }
        } else {
            return new ArrayList<>();
        }

    }

}
