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
package de.isas.mztab2.cvmapping;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * Utility methods for sets with common names.
 *
 * @author nilshoffmann
 */
public class SetOperations {

    /**
     * Creates a new typed {@link LinkedHashSet} (preserving order of insertion)
     * from the given collection.
     *
     * @param <T> the value type
     * @param c the collection
     * @return a new typed hash set
     */
    public static <T> Set<T> newSet(Collection<? extends T> c) {
        return new LinkedHashSet<>(c);
    }

    /**
     * Returns the union (a+b) of <code>a</code> and <code>b</code>.
     *
     * @param <T> the value type
     * @param a set a
     * @param b set b
     * @return the union of a and b
     */
    public static <T> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> union = new LinkedHashSet<>(a);
        union.addAll(b);
        return union;
    }

    /**
     * Returns the intersection (a \cap b, all common elements) of
     * <code>a</code> and <code>b</code>.
     *
     * @param <T> the value type
     * @param a set a
     * @param b set b
     * @return the intersection of a and b
     */
    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> inters = new LinkedHashSet<>(a);
        inters.retainAll(b);
        return inters;
    }

    /**
     * Returns the complement (a without elements also in b) of <code>a</code>
     * and <code>b</code>.
     *
     * @param <T> the value type
     * @param a set a
     * @param b set b
     * @return the complement of a and b
     */
    public static <T> Set<T> complement(Set<T> a, Set<T> b) {
        Set<T> a1 = new LinkedHashSet<>(a);
        a1.removeAll(b);
        return a1;
    }

    /**
     * Returns the symmetric set difference
     * (<code>union(complement(a,b),complement(b,a))</code>) on <code>a</code>
     * and <code>b</code>.
     *
     * @param <T> the value type
     * @param a set a
     * @param b set b
     * @return the symmetric set difference
     */
    public static <T> Set<T> symmetricDifference(Set<T> a, Set<T> b) {
        return union(complement(a, b), complement(b, a));
    }
}
