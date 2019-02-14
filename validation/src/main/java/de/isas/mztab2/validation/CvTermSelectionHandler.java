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
package de.isas.mztab2.validation;

import de.isas.mztab2.model.Parameter;
import java.util.List;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Interface for JxPath subtree selection handling. The argument is the valid
 * JxPath selection consisting of pairs of pointers into the object hierarchy
 * and the corresponding Parameter. Implementations of this interface can act as
 * filters, removing pairs from the collection before returning it.
 *
 * @author nilshoffmann
 */
public interface CvTermSelectionHandler {

    /**
     * Handle the provided selection, e.g. by filtering it or by modifying
     * elements therein.
     *
     * @param selection the selection to handle.
     * @return the selection or any processed variant of it. Should never be
     * null.
     */
    List<Pair<Pointer, Parameter>> handleSelection(
            List<Pair<Pointer, Parameter>> selection);
}
