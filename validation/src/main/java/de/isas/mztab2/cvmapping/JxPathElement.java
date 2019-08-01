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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Utility methods for simpler Java 8 compatible handling of JXPath selections
 * and pointers.
 *
 * @author nilshoffmann
 */
public final class JxPathElement {

    /**
     * Wrap the provided pointer as a stream of the given object type.
     * @param <T> the generic object type
     * @param pointer the pointer in the object tree
     * @param type the object's type
     * @return a typed stream of pairs of pointer and typed object
     */
    public static <T> Stream<Pair<Pointer, T>> toStream(
        Pointer pointer, Class<? extends T> type) {
        if (pointer.getValue() instanceof Collection) {
            Collection<Pair<Pointer, T>> coll = JxPathElement.toList(
                pointer,
                type);
            return coll.stream();
        }
        return Stream.of(Pair.of(pointer, type.cast(pointer.getValue())));
    }

    /**
     * Wrap the provided pointer as a list of the given object type.
     * @param <T> the generic object type
     * @param pointer the pointer in the object tree
     * @param type the object's type
     * @return a typed list of pairs of pointer and typed object
     */
    public static <T> List<Pair<Pointer, T>> toList(
        Pointer pointer,
        Class<? extends T> type) {
        if (pointer.getValue() instanceof Collection) {
            Collection<?> c = (Collection) pointer.getValue();
            return c.stream().
                map((o) ->
                {
                    return Pair.of(pointer, (T) type.cast(o));
                }).
                collect(Collectors.toList());
        } else {
            return Arrays.asList(Pair.of(pointer, (T) type.cast(pointer.
                getValue())));
        }
    }

    /**
     * 
     * Returns the elements selected by the xpath expression from the provided JXPathContext as a list of the given object type.
     * @param <T> the generic object type
     * @param context the jxpath object tree context
     * @param xpath the xpath expression used for subtree / element selection
     * @param type the object's type
     * @return a typed list of pairs of pointer and typed object
     */
    public static <T> List<Pair<Pointer, T>> toList(
        JXPathContext context, String xpath, Class<? extends T> type) {
        return toStream(context.iteratePointers(xpath), Pointer.class).
            map((pointer) ->
            {
                return Pair.of(pointer, (T) type.cast(pointer.getValue()));
            }).
            collect(Collectors.toList());
    }

    /**
     * Creates a typed iterator from an untyped (pre Java 6) iterator.
     * @param <T> the generic object type
     * @param iter the original iterator
     * @param type the object's type
     * @return a typed iterator of the requested type, backed by the provided iterator.
     */
    public static <T> Iterator<T> typedIter(final Iterator iter,
        Class<? extends T> type) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public T next() {
                return type.cast(iter.next());
            }
        };
    }

    /**
     * Creates a typed stream from an untyped (pre Java 6) iterator.
     * @param <T> the generic object type
     * @param iter the original iterator
     * @param type the object's type
     * @return a typed iterator of the requested type, backed by the provided iterator.
     * @see typedIter
     */
    public static <T> Stream<T> toStream(Iterator iter,
        Class<? extends T> type) {
        return toStream(typedIter(iter, type));
    }

    /**
     * Wraps the provided typed iterator into a stream, based on Spliterators.
     * @param <T> the generic object type
     * @param iter the original iterator
     * @return a typed (unbounded and ordered) stream, backed by the provided iterator.
     */
    public static <T> Stream<T> toStream(Iterator<? extends T> iter) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED),
            false);
    }
}
