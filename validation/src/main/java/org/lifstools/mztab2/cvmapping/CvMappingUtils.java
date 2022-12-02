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
import info.psidev.cvmapping.CvTerm;
import java.util.List;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

/**
 * Utility methods for conversion between the mapping file domain and the mzTab
 * domain.
 *
 * @author nilshoffmann
 */
public class CvMappingUtils {

    /**
     * Maps the provided requirement level to the corresponding MZTabErrorType.Level.
     * @param requirementLevel the requirement level
     * @return the corresponding error type level
     */
    public static MZTabErrorType.Level toErrorLevel(
        CvMappingRule.RequirementLevel requirementLevel) {
        switch (requirementLevel) {
            case MAY:
                return MZTabErrorType.Level.Info;
            case SHOULD:
                return MZTabErrorType.Level.Warn;
            case MUST:
                return MZTabErrorType.Level.Error;
            default:
                throw new IllegalArgumentException(
                    "Unhandled case: " + requirementLevel);
        }
    }

    /**
     * Creates a user-friendly string of a mapping rule.
     * @param rule the cv mapping rule
     * @return the string representation
     */
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
                        termString.append(
                            " excluding itself but including any children.");
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

    /**
     * Creates a string representation of a mapping rule.
     * @param rule the cv mapping rule
     * @return the string representation
     * @see niceToString for a nicer string intended for humans
     */
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

    public static boolean isEqualTo(Term term, Parameter param) {
        if (param.getCvLabel().
            equals(term.getOntologyPrefix())) {
            if (param.getCvAccession().
                equals(term.getOboId().
                    getIdentifier())) {
                if (param.getName().
                    equals(term.getLabel())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Parameter asParameter(Term term) {
        return new Parameter().cvLabel(term.getOntologyPrefix()).
            cvAccession(term.getOboId().
                getIdentifier()).
            name(term.getLabel());
    }

    public static boolean isEqualTo(Parameter one, Parameter two) {
        if (one.getCvLabel().
            toUpperCase().
            equals(two.getCvLabel().
                toUpperCase())) {
            if (one.getCvAccession().
                toUpperCase().
                equals(two.getCvAccession().
                    toUpperCase())) {
                if (one.getName() != null && two.getName() != null) {
                    return one.getName().
                        toUpperCase().
                        equals(two.getName().
                            toUpperCase());
                } else { // equal if one or both names are null
                    return true;
                }
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    public static Parameter asParameter(CvTerm term) {
        return new Parameter().cvAccession(term.getTermAccession()).
            cvLabel(term.getCvIdentifierRef().
                getCvIdentifier()).
            name(term.getTermName());
    }
}
