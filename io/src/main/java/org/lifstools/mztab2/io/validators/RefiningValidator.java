/*
 * Copyright 2019 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package org.lifstools.mztab2.io.validators;

import java.util.Collections;
import java.util.List;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Implementations of the refining validator are applied to the type object
 * after the actual line parsing process, for post-hoc validation. This may 
 * happen before the actual mzTab object has been fully constructed. Implementations will therefor
 * receive the current parser context, too.
 *
 * @author nilshoffmann
 * @param <T> the type of the parsed object to validate.
 */
public interface RefiningValidator<T> {

    /**
     * Validate the given object of type T, using the provided parser context to
     * check state of not yet completely parsed and constructed mzTab hierarchy.
     *
     * @param t the object to validate
     * @param parserContext the parser context
     * @return a list of {@link MZTabError}, maybe empty.
     */
    public default List<MZTabError> validateRefine(T t, MZTabParserContext parserContext) {
        return Collections.emptyList();
    }

}
