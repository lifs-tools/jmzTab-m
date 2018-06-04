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

import de.isas.mztab2.io.serialization.Serializers;
import de.isas.mztab2.model.IndexedElement;

/**
 * Additional columns can be added to the end of the protein table. These column headers MUST start with the prefix "opt_".
 * Column names MUST only contain the following characters: 'A'-'Z', 'a'-'z', '0'-'9', '_', '-', '[', ']', and ':'.
 *
 * @author qingwei
 * @since 28/05/13
 * 
 */
public class OptionColumn extends MZTabColumn {
    /** Constant <code>OPT="opt"</code> */
    public static final String OPT = "opt";
    /** Constant <code>GLOBAL="global"</code> */
    public static final String GLOBAL = "global";

    /**
     * Get the optional column header, which start with the prefix "opt_".
     * the format: opt_{IndexedElement[id]}_{value}. Spaces within the parameter's name MUST be replaced by '_'.
     *
     * @param element if the value relates to all replicates, we use "global" in header. Here, if user set element is null for
     *                define for all replicates.
     * @param value SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    public static String getHeader(IndexedElement element, String value) {
        if (MZTabStringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Optional column's value should not be empty.");
        }

        return OPT + "_" + (element == null ? GLOBAL : Serializers.getReference(element, element.getId())) +
                "_" + value.replaceAll(" ", "_");
    }

    /**
     * Create a optional column. Which header start with the prefix "opt_", logical position always stay the end of table.
     *
     * @see #getHeader() generate optional column header.
     * @param element if the value relates to all replicates, we use "global" in header. Here, if user set element is null for
     *                define for all replicates.
     * @param value SHOULD NOT be empty.
     * @param columnType SHOULD NOT be empty.
     * @param offset SHOULD be positive integer.
     */
    public OptionColumn(IndexedElement element, String value, Class columnType, int offset) {
        super(getHeader(element, value), columnType, true, offset + 1 + "");
    }
}
