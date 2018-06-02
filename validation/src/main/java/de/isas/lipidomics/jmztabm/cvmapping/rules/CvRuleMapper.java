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

import de.isas.lipidomics.jmztabm.cvmapping.CvParameterLookupService;
import static de.isas.lipidomics.jmztabm.cvmapping.JxPathElement.toCollection;
import de.isas.lipidomics.jmztabm.cvmapping.Parameters;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Parameter;
import info.psidev.cvmapping.CvMappingRule;
import info.psidev.cvmapping.CvTerm;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;

/**
 *
 * @author Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V.
 * @param <R>
 */
public class CvRuleMapper<R> implements Function<Pair<MzTab, CvMappingRule>, List<MZTabError>> {

    private final CvParameterLookupService lookupService;

    public CvRuleMapper() {
        this(new CvParameterLookupService());
    }

    public CvRuleMapper(CvParameterLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @Override
    public List<MZTabError> apply(Pair<MzTab, CvMappingRule> t) {
        MzTab m = t.getKey();
        JXPathContext context = JXPathContext.newContext(m);
        CvMappingRule rule = t.getValue();
        List<CvTerm> permittedTermRoots = rule.getCvTerm();
        //create selection path
        String path = rule.getCvElementPath();
        //select matching objects (Parameter)
        Collection<Pair<Pointer, ? extends Parameter>> pointerFormatParameters = toCollection(
            context.getPointer(
                path), Parameter.class);
        if(pointerFormatParameters.isEmpty()) {
            switch(rule.getRequirementLevel()) {
                case MAY:
                    return Arrays.asList(new MZTabError(CrossCheckErrorType.CvTermOptional, -1, path, CvMappingUtils.toString(rule)));
                case SHOULD:
                    return Arrays.asList(new MZTabError(CrossCheckErrorType.CvTermRecommended, -1, path, CvMappingUtils.toString(rule)));
                case MUST:
                    return Arrays.asList(new MZTabError(CrossCheckErrorType.CvTermRequired, -1, path, CvMappingUtils.toString(rule)));
            }
        }
        
        // apply rule to each selection in turn
        return pointerFormatParameters.stream().map((selection) ->
        {
            //compare selected domain parameters to all rule-permitted terms (aka term roots)
            List<CvPartialRuleEvalutionResult> results = permittedTermRoots.
                stream().
                map((cvTerm) ->
                {
                    // resolve child terms and check, whether the current selection matches
                    // to either the term itself (short-circuit logic) or against the children
                    // that are resolved against EBI's ontology lookup service.
                    return new CvPartialRuleEvalutionResult(rule, cvTerm,
                        selection,
                        lookupService.isChildOfOrSame(Parameters.asParameter(
                            cvTerm), selection.getValue()));
                }).
                collect(Collectors.toList());
            // evaluate the results against the rule logic
            CvRuleCombinationEvaluator evaluator = new CvRuleCombinationEvaluator();
            return evaluator.evaluate(rule, results);

        }).
            filter((optionalValidationMessage) ->
            {
                return optionalValidationMessage.isPresent();
            }).
            map((optionalValidationMessagesWithContent) ->
            {
                return optionalValidationMessagesWithContent.get();
            }).
            collect(Collectors.toList());

    }

}
