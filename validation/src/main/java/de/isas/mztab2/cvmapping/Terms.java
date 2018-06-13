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
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

/**
 *
 * @author nilshoffmann
 */
public class Terms {

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
}
