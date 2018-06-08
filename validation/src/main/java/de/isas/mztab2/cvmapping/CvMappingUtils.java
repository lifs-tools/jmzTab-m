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

/**
 *
 * @author Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V.
 */
public class CvMappingUtils {

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
            append("scopePath=").
            append(rule.getScopePath()).
            append("', ").
            append("requirementLevel='").
            append(rule.getRequirementLevel()).
            append("', ").
            append(rule.getName()).
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
                append("cv=").
                append(term.getCvIdentifierRef().
                    getCvIdentifier()).
                append("; ").
                append("accession=").
                append(term.getTermAccession()).
                append("allowChildren=").
                append(term.isAllowChildren()).
                append("; ").
                append("repeatable=").
                append(term.isIsRepeatable()).
                append(" ;").
                append("useTerm=").
                append(term.isUseTerm()).
                append("; ").
                append("useTermName=").
                append(term.isUseTermName()).
                append("}");
        });
        return sb.toString();
    }
}
