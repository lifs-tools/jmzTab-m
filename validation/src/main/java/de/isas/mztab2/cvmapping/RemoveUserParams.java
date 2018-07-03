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
import de.isas.mztab2.validation.CvTermSelectionHandler;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Removes user parameters from the provided selection.
 *
 * @author nilshoffmann
 */
@Slf4j
public class RemoveUserParams implements CvTermSelectionHandler {

    @Override
    public List<Pair<Pointer, Parameter>> handleSelection(
        List<Pair<Pointer, Parameter>> selection) {
        List<Pair<Pointer, Parameter>> filteredSelection = selection.
            stream().
            filter((pair) ->
            {
                if (pair.getValue().
                    getCvAccession() == null || pair.getValue().
                        getCvAccession().
                        isEmpty()) {
                    //user parameter
                    log.debug("Removing user parameter for path " + pair.
                        getKey().
                        asPath() + " with value " + pair.getValue());
                    return false;
                }
                return true;
            }).
            collect(Collectors.toList());
        filteredSelection.forEach((pair) ->
        {
            Parameter p = pair.getValue();
            if (!p.getCvAccession().
                contains(":")) {
                throw new RuntimeException(
                    "Malformed cv accession '" + p.getCvAccession() + "' for " + pair.
                    getKey().
                    asPath() + ". Must be: <CV>:<TERMNUMBER>");
            }
        });
        return filteredSelection;
    }
}
