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
package uk.ac.ebi.pride.jmztab2.utils.parser;

import uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;

/**
 * Create and maintain a couple of mappings between physical position and logical position.
 * Physical position: Integer, the position of mzTab file.
 * Logical position: String, the internal order of specification.
 *
 * @author qingwei
 * @since 16/10/13
 * 
 */
public final class PositionMapping {
    // physicalPosition <--> logicalPosition
    private final SortedMap<Integer, String> mappings = new TreeMap<>();

    /**
     * <p>Constructor for PositionMapping.</p>
     *
     * @param factory a {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory} object.
     * @param headerLine a {@link java.lang.String} object.
     */
    public PositionMapping(MZTabColumnFactory factory, String headerLine) {
        this(factory, headerLine.split("\t"));
    }

    /**
     * <p>Constructor for PositionMapping.</p>
     *
     * @param factory a {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory} object.
     * @param headerList an array of {@link java.lang.String} objects.
     */
    public PositionMapping(MZTabColumnFactory factory, String[] headerList) {
        String header;
        for (int physicalPosition = 0; physicalPosition < headerList.length; physicalPosition++) {
            header = headerList[physicalPosition];
            IMZTabColumn column = factory.findColumnByHeader(header);
            if (column != null) {
                put(physicalPosition, column.getLogicPosition());
            }
        }
    }

    /**
     * <p>put.</p>
     *
     * @param physicalPosition a {@link java.lang.Integer} object.
     * @param logicalPosition a {@link java.lang.String} object.
     */
    public void put(Integer physicalPosition, String logicalPosition) {
        this.mappings.put(physicalPosition, logicalPosition);
    }

    /**
     * <p>isEmpty.</p>
     *
     * @return a boolean.
     */
    public boolean isEmpty() {
        return mappings.isEmpty();
    }

    /**
     * <p>size.</p>
     *
     * @return a int.
     */
    public int size() {
        return mappings.size();
    }

    /**
     * <p>containsKey.</p>
     *
     * @param key a {@link java.lang.Integer} object.
     * @return a boolean.
     */
    public boolean containsKey(Integer key) {
        return mappings.containsKey(key);
    }

    /**
     * <p>keySet.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Integer> keySet() {
        return mappings.keySet();
    }

    /**
     * <p>values.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> values() {
        return mappings.values();
    }

    /**
     * <p>get.</p>
     *
     * @param key a {@link java.lang.Integer} object.
     * @return a {@link java.lang.String} object.
     */
    public String get(Integer key) {
        return mappings.get(key);
    }

    /**
     * Exchange key and value to "LogicalPosition, PhysicalPosition". This method used to simply the locate
     * operation by logical position to physical position.
     *
     * @return a {@link java.util.SortedMap} object.
     */
    public SortedMap<String, Integer> reverse() {
        SortedMap<String, Integer> reverseMappings = new TreeMap<>();

        String logicalPosition;
        for (Integer physicalPosition : mappings.keySet()) {
            logicalPosition = mappings.get(physicalPosition);
            reverseMappings.put(logicalPosition, physicalPosition);
        }

        return reverseMappings;
    }
}
