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
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author nilshoffmann
 */
public final class JxPathElement {

    public static <T> Stream<Pair<Pointer, ? extends T>> toStream(Pointer pointer, Class<? extends T> type) {
        if(pointer.getValue() instanceof Collection) {
            Collection<Pair<Pointer, ? extends T>> coll = toCollection(pointer, type);
            return coll.stream();
        }
        return Stream.of(Pair.of(pointer, type.cast(pointer.getValue())));
    }
    
    public static <T> Collection<Pair<Pointer, ? extends T>> toCollection(Pointer pointer,
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
            throw new IllegalArgumentException(
                "Pointer value must inherit from Collection!");
        }
    }

    public static <T> Iterator<? extends T> typedIter(final Iterator iter,
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

    public static <T> Stream<? extends T> toStream(Iterator iter, Class<? extends T> type) {
        return toStream(typedIter(iter, type));
    }

    public static <T> Stream<? extends T> toStream(Iterator<? extends T> iter) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED),
            false);
    }
}
