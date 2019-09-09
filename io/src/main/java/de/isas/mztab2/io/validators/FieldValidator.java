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
package de.isas.mztab2.io.validators;

import java.util.Collections;
import java.util.List;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Implementations of the field validator are applied to the type object (usually a parsed string element)
 * during the parsing process. The validator can access the current parsing context for state-dependent 
 * validation and receives the current column specification for type specific checking.
 *
 * @author nilshoffmann
 * @param <T> the type of the parsed object to validate.
 */
public interface FieldValidator<T> {
    /**
     * Validate the given object of type T, using the provided parser context and column to
     * check the state of not yet completely parsed and constructed mzTab hierarchy.
     *
     * @param lineNumber the current line number being parsed. Maybe -1 to indicate no active lineNumber, e.g. for post-hoc refine validation.
     * @param parserContext the parser context
     * @param column the IMzTabColumn to use for type validation constraints
     * @param field the string representation of the field
     * @param t the object to validate
     * @return a list of {@link MZTabError}, maybe empty.
     */
    public default List<MZTabError> validateLine(int lineNumber, MZTabParserContext parserContext, IMZTabColumn column, String field, T t) {
        return Collections.emptyList();
    }
}
