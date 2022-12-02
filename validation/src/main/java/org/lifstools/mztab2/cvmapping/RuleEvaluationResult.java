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
package org.lifstools.mztab2.cvmapping;

import org.lifstools.mztab2.model.Parameter;
import info.psidev.cvmapping.CvMappingRule;
import java.util.List;
import java.util.Map;
import lombok.Value;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Captures results of applying a rule to a parameter selection.
 *
 * @author nilshoffmann
 */
@Value
public class RuleEvaluationResult {

    private final CvMappingRule rule;
    private final List<Pair<Pointer, Parameter>> filteredSelection;
    private final Map<String, Parameter> allowedParameters;
    private final Map<String, Pair<Pointer, Parameter>> foundParameters;

}
