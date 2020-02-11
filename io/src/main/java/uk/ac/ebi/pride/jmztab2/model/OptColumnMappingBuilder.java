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

import de.isas.mztab2.model.IndexedElementAdapter;
import de.isas.mztab2.model.OptColumnMapping;
import de.isas.mztab2.model.Parameter;

/**
 * Builder for reusable {@link OptColumnMapping} creation for multiple rows.
 *
 * @author nilshoffmann
 */
public class OptColumnMappingBuilder implements IOptColumnMappingBuilder {

    private OptColumnMappingBuilder() {

    }

    private static class NameParamOptColumnMappingBuilder implements IOptColumnMappingBuilder {

        protected Parameter param;
        protected String name;

        /**
         * Configure this builder to create an optional column mapping with the
         * provided name. Mutually exclusive with {@link #withParameter(de.isas.mztab2.model.Parameter)
         * }.
         *
         * @param name the name of this optional column.
         * @return the builder instance.
         */
        IOptColumnMappingBuilder withName(String name) {
            if (this.param != null) {
                throw new IllegalStateException(
                        "Can not set name for opt column, parameter already has been set!");
            }
            this.name = name;
            return this;
        }

        /**
         * Configure this builder to create cv parameter optional columns.
         *
         * @param parameter the cv parameter for this mapping.
         * @return the builder instance.
         */
        IOptColumnMappingBuilder withParameter(Parameter parameter) {
            if (this.name != null) {
                throw new IllegalStateException(
                        "Can not set parameter for opt column, name has been set already!");
            }
            if (parameter.getCvAccession() == null || parameter.getCvAccession().
                    isEmpty()) {
                throw new IllegalArgumentException(
                        "Parameter must have cvAccession defined!");
            }
            if (parameter.getCvLabel() == null || parameter.getCvLabel().
                    isEmpty()) {
                throw new IllegalArgumentException(
                        "Parameter must have cvLabel defined!");
            }
            if (parameter.getName() == null || parameter.getName().
                    isEmpty()) {
                throw new IllegalArgumentException(
                        "Parameter must have name defined!");
            }
            this.param = parameter;
            return this;
        }
    }

    /**
     * Create a new {@link OptColumnMappingBuilder} for global optional columns.
     * Use this to create a global optional column, either with a dedicated
     * name, or with a parameter. This applies to all replicates.
     *
     * {@code
     *  opt_global_someProperty ... opt_global_cv_MS_MS:113123_parameter_name
     *  <somePropertyValue> ... <parameter_name_value>
     * }
     */
    public static class GlobalOptColumnMappingBuilder extends NameParamOptColumnMappingBuilder {

        @Override
        public GlobalOptColumnMappingBuilder withName(String name) {
            super.withName(name);
            return this;
        }

        @Override
        public GlobalOptColumnMappingBuilder withParameter(Parameter parameter) {
            super.withParameter(parameter);
            return this;
        }

        @Override
        public OptColumnMapping build(String value) {

            if (this.param != null) {
                return new OptColumnMapping().param(param).
                        value(value).
                        identifier(ParameterOptionColumn.getHeader(null, param));
            } else {
                if(name == null) {
                    throw new IllegalArgumentException("Name must be defined if parameter is not set!");
                }
                return new OptColumnMapping().value(value).
                        identifier(OptionColumn.getHeader(null, name));
            }
        }
    }

    /**
     * Create a new {@link OptColumnMappingBuilder} for {@link IndexedElement}.
     * Either referencing an indexed element property with a dedicated name, or
     * with a parameter. This applies only to the referenced objects.
     *
     * {@code
     *  opt_assay[1]_someProperty ... opt_assay[1]_cv_MS_MS:113123_parameter_name
     *  <somePropertyValue> ... <parameter_name_value>
     * }
     */
    public static class IndexedElementOptColumnMappingBuilder extends NameParamOptColumnMappingBuilder {

        private final IndexedElementAdapter indexedElement;

        @Override
        public IndexedElementOptColumnMappingBuilder withName(String name) {
            super.withName(name);
            return this;
        }

        @Override
        public IndexedElementOptColumnMappingBuilder withParameter(Parameter parameter) {
            super.withParameter(parameter);
            return this;
        }

        /**
         * Configure this builder to create optional column mapping for an
         * indexed element, such as
         * {@link de.isas.mztab2.model.Assay}, {@link de.isas.mztab2.model.StudyVariable},
         * or {@link de.isas.mztab2.model.MsRun}.
         *
         * @param indexedElement the indexed element to reference in this
         * optional column.
         */
        public IndexedElementOptColumnMappingBuilder(IndexedElementAdapter indexedElement) {
            this.indexedElement = indexedElement;
        }

        @Override
        public OptColumnMapping build(String value) {
            if (this.indexedElement == null) {
                throw new IllegalArgumentException("Indexed element must not be null!");
            }
            if (this.param != null) {
                return new OptColumnMapping().param(param).
                        value(value).
                        identifier(ParameterOptionColumn.getHeader(indexedElement, param));
            } else {
                if(name == null) {
                    throw new IllegalArgumentException("Name must be defined if parameter is not set!");
                }
                return new OptColumnMapping().value(value).
                        identifier(OptionColumn.getHeader(indexedElement, name));
            }
        }
    }

    /**
     * Configure the builder for a global type column.
     *
     * @return the builder instance for a global opt column.
     */
    public static GlobalOptColumnMappingBuilder forGlobal() {
        return new GlobalOptColumnMappingBuilder();
    }

    /**
     * Configure the builder for an object reference (indexed element) type
     * column.
     *
     * @param indexedElement the object to reference.
     * @return the builder instance for an indexed element opt column.
     */
    public static IndexedElementOptColumnMappingBuilder forIndexedElement(IndexedElementAdapter indexedElement) {
        return new IndexedElementOptColumnMappingBuilder(indexedElement);
    }
}
