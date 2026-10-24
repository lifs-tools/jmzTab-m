/*
 * Copyright 2018 LIFS Tools Consortium.
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
import org.lifstools.mztab2.validation.CvTermSelectionHandler;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Removes user parameters, and cv parameters whose term cannot be looked up
 * (see {@link #isMalformed(Parameter)}), from the provided selection.
 *
 * @author nilshoffmann
 */
@Slf4j
public class RemoveUserParams implements CvTermSelectionHandler {

    /**
     * Whether a cv parameter (one with an accession) has no cv label or an
     * accession that is not of the form &lt;CV label&gt;:&lt;term number&gt;,
     * e.g. [nan, nan, nan, ]. Its term cannot be looked up; the
     * {@link org.lifstools.mztab2.validation.CvMappingValidator} reports it.
     *
     * @param p the parameter
     * @return true if the parameter has an accession but is malformed
     */
    public static boolean isMalformed(Parameter p) {
        if (p == null || p.getCvAccession() == null || p.getCvAccession().
            isEmpty()) {
            return false;
        }
        return p.getCvLabel() == null || p.getCvLabel().
            isBlank() || !p.getCvAccession().
                contains(":");
    }

    @Override
    public List<Pair<Pointer, Parameter>> handleSelection(
        List<Pair<Pointer, Parameter>> selection) {
        List<Pair<Pointer, Parameter>> filteredSelection = selection.
            stream().
            filter((pair) ->
            {
                if (pair.getValue() == null || pair.getValue().
                    getCvAccession() == null || pair.getValue().
                        getCvAccession().
                        isEmpty()) {
                    //user parameter
                    log.debug("Removing user parameter for path " + pair.
                        getKey().
                        asPath() + " with value " + pair.getValue());
                    return false;
                }
                if (isMalformed(pair.getValue())) {
                    log.debug("Removing malformed cv parameter for path " + pair.
                        getKey().
                        asPath() + " with value " + pair.getValue());
                    return false;
                }
                return true;
            }).
            collect(Collectors.toList());
        return filteredSelection;
    }
}
