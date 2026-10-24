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
package org.lifstools.mztab2.validation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.lifstools.mztab2.cvmapping.CvParameterLookupService;
import org.lifstools.mztab2.cvmapping.ParameterComparisonResult;
import org.lifstools.mztab2.model.Parameter;

/**
 * Lookup service with a fixed term hierarchy, so that tests need no network.
 *
 * @author nilshoffmann
 */
public class StubLookupService extends CvParameterLookupService {

    private final Map<String, Set<String>> childrenByParent;

    /**
     * @param childrenByParent the child accessions of each parent accession
     */
    public StubLookupService(Map<String, Set<String>> childrenByParent) {
        this.childrenByParent = childrenByParent;
    }

    @Override
    public ParameterComparisonResult isChildOfOrSame(Parameter parent,
        Parameter child) {
        if (parent.getCvAccession().equalsIgnoreCase(child.getCvAccession())) {
            return ParameterComparisonResult.IDENTICAL;
        }
        if (childrenByParent.getOrDefault(parent.getCvAccession(),
            Collections.emptySet()).contains(child.getCvAccession())) {
            return ParameterComparisonResult.CHILD_OF;
        }
        return ParameterComparisonResult.NOT_RELATED;
    }
}
