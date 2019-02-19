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
package uk.ac.ebi.pride.jmztab2.model;

import de.isas.mztab2.model.OptColumnMapping;
import de.isas.mztab2.model.OptColumnMapping;

/**
 * Interface for optional column mapping builders.
 *
 * @author nilshoffmann
 * @see OptColumnMappingBuilder
 */
public interface IOptColumnMappingBuilder {

    /**
     * Use the current builder state to create an OptColumnMapping with the
     * provided value for e.g. a particular feature (row).
     *
     * @param value the value for the mapping.
     * @return the optional column mapping built from this builder instance.
     */
    public default OptColumnMapping build(String value) {
        throw new RuntimeException("Build method not implemented in " + getClass().getName());
    }
}
