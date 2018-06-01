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
package de.isas.lipidomics.jmztabm.cvmapping.rules;

import info.psidev.cvmapping.CvMappingRule;
import java.util.List;
import java.util.Optional;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;

/**
 * Evaluates a {@link CvMappingRule} against the partial results provided for
 * comparison.
 *
 * @author nilshoffmann
 */
public class CvRuleCombinationEvaluator {

    public Optional<MZTabError> evaluate(CvMappingRule rule,
        List<CvPartialRuleEvalutionResult> results) {
        CvMappingRule.CvTermsCombinationLogic combinationLogic = rule.
            getCvTermsCombinationLogic();
        
            switch(combinationLogic) {
        //AND logic means, all term roots must have a hit againt the validated Parameter
                case AND:
                    break;
        //OR logic means, at least one of the term roots must have a hit against the validated Parameter
                case OR:
                    results.stream().
                        allMatch((t) ->
                        {
                            switch(t.getResult()) {
                                case CHILD_OF:
                                    if(t.getRuleCvTerm().isAllowChildren()) {
                                        return true;
                                    }
                                case IDENTICAL:
                                    if(t.getRuleCvTerm().isUseTerm()) {
                                        return true;
                                    }
                                case NOT_RELATED:
                                default:
                                    return false;
                            }
                        });
                    break;
        //XOR logic means, exactly one of the term roots must have a hit against the validated Parameter
                case XOR:
                    break;
                default:
//                    throw new 
            }
        return Optional.empty();
    }
}
