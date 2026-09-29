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

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory;

/**
 * Maps the physical position of each column in a header line to its column
 * definition in the {@link MZTabColumnFactory}.
 *
 * @author qingwei
 * @author nilshoffmann
 * @since 16/10/13
 *
 */
public final class PositionMapping {

    private final SortedMap<Integer, IMZTabColumn> mappings = new TreeMap<>();

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
        for (int physicalPosition = 0; physicalPosition < headerList.length; physicalPosition++) {
            IMZTabColumn column = factory.findColumnByHeader(headerList[physicalPosition]);
            if (column != null) {
                put(physicalPosition, column);
            }
        }
    }

    /**
     * <p>put.</p>
     *
     * @param physicalPosition the 0-based index in the split header line.
     * @param column the column at that position.
     */
    public void put(Integer physicalPosition, IMZTabColumn column) {
        this.mappings.put(physicalPosition, column);
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
     * @return the mapped columns in physical order.
     */
    public Collection<IMZTabColumn> values() {
        return mappings.values();
    }

    /**
     * <p>get.</p>
     *
     * @param key the 0-based index in the split header line.
     * @return the column at that position, or null.
     */
    public IMZTabColumn get(Integer key) {
        return mappings.get(key);
    }
}
