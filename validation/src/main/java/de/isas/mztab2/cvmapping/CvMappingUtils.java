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

import info.psidev.cvmapping.CvMappingRule;
import info.psidev.cvmapping.CvTerm;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author nilshoffmann
 */
public class CvMappingUtils {

    public static String niceToString(CvMappingRule rule) {
        StringBuilder sb = new StringBuilder();
        switch (rule.getRequirementLevel()) {
            case MAY:
                sb.append("OPTIONAL ");
                break;
            case SHOULD:
                sb.append("RECOMMENDED ");
                break;
            case MUST:
                sb.append("REQUIRED ");
                break;
        }

        sb.append("rule '").
            append(rule.getId()).
            append("' for path '").
            append(rule.getCvElementPath()).
            append("' and with scope '").
            append(rule.getScopePath()).
            append("' ");

        sb.append(" matching ");
        switch (rule.getCvTermsCombinationLogic()) {
            case AND:
                sb.append(" ALL of");
                break;
            case OR:
                sb.append(" ANY of");
                break;
            case XOR:
                sb.append(" EXACTLY ONE of");
                break;
        }
        sb.append(" the terms ");
        sb.append(rule.getCvTerm().
            stream().
            map((term) ->
            {
                StringBuilder termString = new StringBuilder();
                termString.append(" '").
                    append(term.getTermAccession()).
                    append("' with name '").
                    append(term.getTermName()).
                    append("'");

                if (term.isAllowChildren()) {
                    if (term.isUseTerm()) {
                        termString.append(
                            " including itself or any of its children.");
                    } else {
                        termString.append(" excluding itself but including any children.");
                    }
                } else {
                    if (term.isUseTerm()) {
                        termString.append(" exactly.");
                    }
                }

                return termString.append("'").
                    toString();
            }).
            collect(Collectors.joining("|", "[", "]")));
        return sb.toString();
    }

    public static String toString(CvMappingRule rule) {
        StringBuilder sb = new StringBuilder();
        return sb.append("Rule{").
            append("id='").
            append(rule.getId()).
            append("', ").
            append("name='").
            append(rule.getName()).
            append("', ").
            append("cvElementPath='").
            append(rule.getCvElementPath()).
            append("', ").
            append("scopePath='").
            append(rule.getScopePath()).
            append("', ").
            append("requirementLevel='").
            append(rule.getRequirementLevel()).
            append("', ").
            append("combinationLogic='").
            append(rule.getCvTermsCombinationLogic()).
            append("', ").
            append("terms='").
            append(
                toString(rule.getCvTerm())
            ).
            append("'}").
            toString();
    }

    public static String toString(List<CvTerm> terms) {
        StringBuilder sb = new StringBuilder();
        terms.forEach((term) ->
        {
            sb.append("Term{").
                append("cv='").
                append(term.getCvIdentifierRef().
                    getCvIdentifier()).
                append("', ").
                append("accession='").
                append(term.getTermAccession()).
                append("', ").
                append("allowChildren='").
                append(term.isAllowChildren()).
                append("', ").
                append("repeatable='").
                append(term.isIsRepeatable()).
                append("', ").
                append("useTerm='").
                append(term.isUseTerm()).
                append("', ").
                append("useTermName='").
                append(term.isUseTermName()).
                append("'}");
        });
        return sb.toString();
    }
}
