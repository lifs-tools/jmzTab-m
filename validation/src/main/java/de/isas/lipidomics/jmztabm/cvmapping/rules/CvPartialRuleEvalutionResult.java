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

import de.isas.lipidomics.jmztabm.cvmapping.ParameterComparisonResult;
import de.isas.mztab1_1.model.Parameter;
import info.psidev.cvmapping.CvMappingRule;
import info.psidev.cvmapping.CvTerm;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Lowest level evaluation result of one rule and one root term against a
 * specific parameter selection from the domain model.
 *
 * @author nilshoffmann
 */
public class CvPartialRuleEvalutionResult {

    private final CvMappingRule rule;
    private final CvTerm ruleCvTerm;
    private final Pair<Pointer, ? extends Parameter> selection;
    private final ParameterComparisonResult result;

    public CvPartialRuleEvalutionResult(CvMappingRule rule, CvTerm ruleCvTerm,
        Pair<Pointer, ? extends Parameter> selection,
        ParameterComparisonResult result) {
        this.rule = rule;
        this.ruleCvTerm = ruleCvTerm;
        this.selection = selection;
        this.result = result;
    }

    public CvMappingRule getRule() {
        return rule;
    }

    public CvTerm getRuleCvTerm() {
        return ruleCvTerm;
    }

    public Pair<Pointer, ? extends Parameter> getSelection() {
        return selection;
    }

    public ParameterComparisonResult getResult() {
        return result;
    }

}
