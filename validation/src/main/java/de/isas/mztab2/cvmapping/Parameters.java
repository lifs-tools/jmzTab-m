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
import info.psidev.cvmapping.CvTerm;

/**
 * Ignores parameter value property, comparse only cvLabel, cvAccession, and
 * name, if the latter is non-null.
 *
 * @author Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V.
 */
public class Parameters {

    public static boolean isEqualTo(Parameter one, Parameter two) {
        if (one.getCvLabel().toUpperCase().
            equals(two.getCvLabel().toUpperCase())) {
            if (one.getCvAccession().toUpperCase().
                equals(two.getCvAccession().toUpperCase())) {
                if (one.getName() != null && two.getName() != null) {
                    return one.getName().toUpperCase().
                        equals(two.getName().toUpperCase());
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
