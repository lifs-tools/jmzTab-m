/*
 * Copyright 2026 LIFS Tools Consortium.
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
package org.lifstools.mztab2.validation.handlers;

import info.psidev.cvmapping.CvMappingRule;
import org.apache.commons.jxpath.Pointer;
import org.lifstools.mztab2.cvmapping.CvMappingUtils;
import org.lifstools.mztab2.model.ValidationLocation;
import org.lifstools.mztab2.model.ValidationMessage;
import org.lifstools.mztab2.validation.ModelLocations;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * Turns errors raised by a CV mapping rule into validation messages with
 * location, rule id and the element and allowed terms of the suggested fix.
 *
 * @author nilshoffmann
 */
public final class RuleMessages {

    private RuleMessages() {
    }

    /**
     * For errors without a selected element: points at the metadata group or
     * table column that the rule addresses.
     */
    public static ValidationMessage atRulePath(MZTabError error, CvMappingRule rule) {
        return error.location(ModelLocations.ofRulePath(rule.
            getCvElementPath())).
            ruleId(rule.getId()).
            fixContext(ModelLocations.describeRulePath(rule.getCvElementPath()),
                CvMappingUtils.allowedTermsText(rule)).
            toValidationMessage();
    }

    /**
     * For errors about a selected element: points at that element.
     */
    public static ValidationMessage atPointer(MZTabError error, Pointer pointer,
        CvMappingRule rule) {
        ValidationLocation location = ModelLocations.ofPointer(pointer);
        return error.location(location).
            ruleId(rule.getId()).
            fixContext(ModelLocations.describe(location),
                CvMappingUtils.allowedTermsText(rule)).
            toValidationMessage();
    }

    /**
     * For errors about elements beyond those reported with a location: no
     * location, the fix names the number of further elements.
     */
    public static ValidationMessage onFurtherElements(MZTabError error, int count,
        CvMappingRule rule) {
        return error.ruleId(rule.getId()).
            fixContext(count + " further elements",
                CvMappingUtils.allowedTermsText(rule)).
            toValidationMessage();
    }
}
