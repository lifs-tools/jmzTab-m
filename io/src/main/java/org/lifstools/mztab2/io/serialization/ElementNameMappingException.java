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
package org.lifstools.mztab2.io.serialization;

/**
 * <p>
 * ElementNameMappingException should be thrown to indicate that the state of
 * the passed in element did not allow mapping to a proper mzTab name.</p>
 *
 * @author nilshoffmann
 *
 */
public final class ElementNameMappingException extends RuntimeException {

    /**
     * <p>
     * Constructor for ElementNameMappingException.</p>
     *
     * @param property the property name for the given element.
     * @param element a {@link java.lang.Object} object.
     *
     */
    public ElementNameMappingException(String property, Object element) {
        super(
            "No mzTab element name mapping available for property '" + property + "' on element '" + element.
                getClass().
                getName() + "' with value '" + element + "'");
    }

}
