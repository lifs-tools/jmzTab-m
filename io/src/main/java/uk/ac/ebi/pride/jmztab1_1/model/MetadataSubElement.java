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
package uk.ac.ebi.pride.jmztab1_1.model;

/**
 * A special element which depend on a {@link uk.ac.ebi.pride.jmztab1_1.model.MetadataElement}, the structure like:
 *
 * {element name}[id]-{subElement subName}
 *
 * @author qingwei
 * @since 14/10/13
 * 
 */
public enum MetadataSubElement {
    ASSAY_QUANTIFICATION_MOD                           (MetadataElement.ASSAY,           "quantification_mod");
    private MetadataElement element;
    private String subName;

    /**
     * A special element which depend on a {@link MetadataElement}, the structure like:
     * {element name}[id]-{subElement subName}
     *
     * @param element SHOULD NOT set null.
     * @param subName SHOULD NOT empty.
     */
    MetadataSubElement(MetadataElement element, String subName) {
        if (element == null) {
            throw new NullPointerException("Metadata element should not be null!");
        }
        if (MZTabStringUtils.isEmpty(subName)) {
            throw new IllegalArgumentException("sub element's name should not be empty.");
        }

        this.element = element;
        this.subName = subName;
    }

    /**
     * Used to get a unique name, which used to unique identifier the {@link uk.ac.ebi.pride.jmztab1_1.model.MetadataProperty}
     * Notice: we use '_' the concatenate element and sub element name.
     *
     * @see MetadataProperty#findProperty(MetadataSubElement, String)
     * @return {element name}_{subElement subName}
     */
    public String getName() {
        return element.getName() + "_" + subName;
    }

    /**
     * <p>Getter for the field <code>subName</code>.</p>
     *
     * @return sub element name.
     */
    public String getSubName() {
        return subName;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getSubName();
    }

    /**
     * <p>Getter for the field <code>element</code>.</p>
     *
     * @return dependent {@link uk.ac.ebi.pride.jmztab1_1.model.MetadataElement}.
     */
    public MetadataElement getElement() {
        return element;
    }

    /**
     * Find sub element by name with case-insensitive.
     * Notice: we use '_' the concatenate character, for example, assay_quantification_mod.
     *
     * @see #getName()
     * @param element a {@link uk.ac.ebi.pride.jmztab1_1.model.MetadataElement} object.
     * @param subElementName a {@link java.lang.String} object.
     * @return a {@link uk.ac.ebi.pride.jmztab1_1.model.MetadataSubElement} object.
     */
    public static MetadataSubElement findSubElement(MetadataElement element, String subElementName) {
        if (element == null || subElementName == null) {
            return null;
        }

        MetadataSubElement subElement;
        try {
            subElement = MetadataSubElement.valueOf((element.getName() + "_" + subElementName).trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            subElement = null;
        }

        return subElement;
    }
}
