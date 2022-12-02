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
import org.lifstools.mztab2.model.Parameter;
import info.psidev.cvmapping.CvMappingRule;
import java.util.List;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Interface for rule logic handling implementations. 
 * @author nilshoffmann
 */
public interface CvRuleHandler {
    /**
     * Handle the provided rule according to the implementor's logic. The rule is applied to all elements 
     * within the filtered selection where it is applicable.
     * @param rule the rule to apply
     * @param filteredSelection the selection of pointer,paramter pairs to apply the rule on
     * @return the rule evaluation result
     */
    RuleEvaluationResult handleRule(CvMappingRule rule,
        List<Pair<Pointer, Parameter>> filteredSelection);
}
