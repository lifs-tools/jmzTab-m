/*
 * Copyright 2018 nilshoffmann.
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
package de.isas.lipidomics.jmztabm.validator;

import de.isas.mztab1_1.model.Parameter;
import info.psidev.cvmapping.CvMappingRule;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

/**
 *
 * @author nilshoffmann
 */
public class RuleEvalutionResult {
    
    public static enum Status{MATCH,NO_MATCH};
    
    private final CvMappingRule rule;
    private final Pair<Pointer, ? extends Parameter> selection;
    private final RuleEvalutionResult.Status status;

    public RuleEvalutionResult(CvMappingRule rule, Term term, 
        Pair<Pointer, ? extends Parameter> selection, RuleEvalutionResult.Status status) {
        this.rule = rule;
        this.selection = selection;
        this.status = status;
    }

    public CvMappingRule getRule() {
        return rule;
    }

    public Pair<Pointer, ? extends Parameter> getSelection() {
        return selection;
    }

    public Status getStatus() {
        return status;
    }
    
}
