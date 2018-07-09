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
package uk.ac.ebi.pride.jmztab2.model;

import de.isas.mztab2.model.IndexedElement;
import de.isas.mztab2.model.OptColumnMapping;
import de.isas.mztab2.model.Parameter;

/**
 * Builder for reusable {@link OptColumnMapping} creation for multiple rows.
 *
 * @author nilshoffmann
 */
public class OptColumnMappingBuilder {

    private Parameter param;
    private String name;
    private IndexedElement indexedElement;
    private boolean global;

    /**
     * Configure the builder for a global type column. Mutually exclusive with {@link #withParameter(de.isas.mztab2.model.Parameter)
     * }.
     *
     * @return the builder instance.
     */
    public OptColumnMappingBuilder forGlobal() {
        if (this.param != null) {
            throw new IllegalStateException(
                "Can not set name for opt column, parameter already has been set!");
        }
        this.global = true;
        return this;
    }

    /**
     * Configure this builder to create an optional column mapping with the
     * provided name. Mutually exclusive with {@link #withParameter(de.isas.mztab2.model.Parameter)
     * }.
     *
     * @param name the name of this optional column.
     * @return the builder instance.
     */
    public OptColumnMappingBuilder withName(String name) {
        if (this.param != null) {
            throw new IllegalStateException(
                "Can not set name for opt column, parameter already has been set!");
        }
        this.name = name;
        return this;
    }

    /**
     * Configure this builder to create optional column mapping for an indexed
     * element, such as
     * {@link de.isas.mztab2.model.Assay}, {@link de.isas.mztab2.model.StudyVariable},
     * or {@link de.isas.mztab2.model.MsRun}. Mutually exclusive with
     * {@link #withName(java.lang.String)}.
     *
     * @param <T> The generic type of the indexed element.
     * @param element the indexed element to reference in this optional column.
     * @return the builder instance.
     */
    public <T extends IndexedElement> OptColumnMappingBuilder forIndexedElement(
        T element) {
        if (global) {
            throw new IllegalStateException(
                "Can not set indexed element for global opt column!");
        }
        this.indexedElement = element;
        return this;
    }

    /**
     * Configure this builder to create cv parameter optional columns.
     *
     * @param parameter the cv parameter for this mapping.
     * @return the builder instance.
     */
    public OptColumnMappingBuilder withParameter(Parameter parameter) {
        if (this.name != null) {
            throw new IllegalStateException(
                "Can not set parameter for opt column, name already has been set!");
        }
        if (parameter.getCvAccession() == null || parameter.getCvAccession().
            isEmpty()) {
            throw new IllegalArgumentException(
                "Parameter must have cvAccession defined!");
        }
        if (parameter.getCvLabel() == null || parameter.getCvAccession().
            isEmpty()) {
            throw new IllegalArgumentException(
                "Parameter must have cvLabel defined!");
        }
        if (parameter.getName() == null || parameter.getCvAccession().
            isEmpty()) {
            throw new IllegalArgumentException(
                "Parameter must have name defined!");
        }
        this.param = parameter;
        return this;
    }

    /**
     * Use the current builder state to create an OptColumnMapping with the
     * provided value for e.g. a particular feature (row).
     *
     * @param value the value for the mapping.
     * @return the optional column mapping built from this builder instance.
     */
    public OptColumnMapping build(String value) {

        if (this.param != null) {
            ParameterOptionColumn poc = new ParameterOptionColumn(indexedElement,
                param,
                String.class, 0);
            return new OptColumnMapping().param(param).
                value(value).
                identifier(poc.getHeader());
        } else {
            OptionColumn oc = new OptionColumn(indexedElement, name,
                String.class,
                0);
            return new OptColumnMapping().value(value).
                identifier(oc.getHeader());
        }

    }
}
