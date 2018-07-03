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
package de.isas.mztab2.cvmapping;

import de.isas.mztab2.model.Parameter;
import info.psidev.cvmapping.CvMappingRule;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Captures results of applying a rule to a parameter selection.
 *
 * @author nilshoffmann
 */
public class RuleEvaluationResult {

    private final CvMappingRule rule;
    private final List<Pair<Pointer, Parameter>> filteredSelection;
    private final Map<String, Parameter> allowedParameters;
    private final Map<String, Pair<Pointer, Parameter>> foundParameters;

    public RuleEvaluationResult(CvMappingRule rule,
        List<Pair<Pointer, Parameter>> filteredSelection,
        Map<String, Parameter> allowedParameters,
        Map<String, Pair<Pointer, Parameter>> foundParameters) {
        this.rule = rule;
        this.filteredSelection = filteredSelection;
        this.allowedParameters = allowedParameters;
        this.foundParameters = foundParameters;
    }

    public CvMappingRule getRule() {
        return rule;
    }

    public List<Pair<Pointer, Parameter>> getFilteredSelection() {
        return filteredSelection;
    }

    public Map<String, Parameter> getAllowedParameters() {
        return allowedParameters;
    }

    public Map<String, Pair<Pointer, Parameter>> getFoundParameters() {
        return foundParameters;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + Objects.hashCode(this.rule);
        hash = 13 * hash + Objects.hashCode(this.filteredSelection);
        hash = 13 * hash + Objects.hashCode(this.allowedParameters);
        hash = 13 * hash + Objects.hashCode(this.foundParameters);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RuleEvaluationResult other = (RuleEvaluationResult) obj;
        if (!Objects.equals(this.rule, other.rule)) {
            return false;
        }
        if (!Objects.equals(this.filteredSelection, other.filteredSelection)) {
            return false;
        }
        if (!Objects.equals(this.allowedParameters, other.allowedParameters)) {
            return false;
        }
        if (!Objects.equals(this.foundParameters, other.foundParameters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RuleEvaluationResult{" + "rule=" + rule + ", filteredSelection=" + filteredSelection + ", allowedParameters=" + allowedParameters + ", foundParameters=" + foundParameters + '}';
    }

}
