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
package de.isas.mztab2.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides a wrapper around objects which previously implemented
 * IndexedElement.Due to an incompatibility in swagger-codegen and
 openapi-codegen with inheritance in R and Python, the model has been
 simplified to contain ids directly.
 *
 * @param <T> The adaptee's type.
 * @since 1.0.5
 *
 * @author nilshoffmann
 */
@Slf4j
public class IndexedElementAdapter<T> {

    private final T adaptee;

    public IndexedElementAdapter(T adaptee) {
        this.adaptee = adaptee;
    }
    
    /**
     * Return the adaptee object.
     * @return the adaptee wrapped by this adapter object.
     */
    public T getAdaptee() {
        return this.adaptee;
    }

    /**
     * Returns the indexed element id of this object.
     * @return the id.
     */
    public Integer getId() {
        try {
            Object obj = adaptee.getClass().getMethod("getId").invoke(adaptee);
            if (obj != null) {
                return (Integer) obj;
            }
        } catch (NoSuchMethodException ex) {
            log.warn("Caught NoSuchElementException while trying to invoke getId!", ex);
        } catch (SecurityException ex) {
            log.warn("Caught SecurityException while trying to invoke getId!", ex);
        } catch (IllegalAccessException ex) {
            log.warn("Caught IllegalAccessException while trying to invoke getId!", ex);
        } catch (IllegalArgumentException ex) {
            log.warn("Caught IllegalArgumentException while trying to invoke getId!", ex);
        } catch (InvocationTargetException ex) {
            log.warn("Caught InvocationTargetException while trying to invoke getId!", ex);
        } catch (ClassCastException ex) {
            log.warn("Caught ClassCastException while trying to invoke getId!", ex);
        }
        return null;
    }

    /**
     * Set the indexed element id.
     * @param id 
     */
    public void setId(Integer id) {
        try {
            Method m = adaptee.getClass().getMethod("setId");
            if (m!=null) {
                m.invoke(adaptee, id);
            } else {
                log.warn("setId method on {} was not available!", adaptee);
            }
        } catch (NoSuchMethodException ex) {
            log.warn("Caught NoSuchElementException while trying to invoke getId!", ex);
        } catch (SecurityException ex) {
            log.warn("Caught SecurityException while trying to invoke getId!", ex);
        } catch (IllegalAccessException ex) {
            log.warn("Caught IllegalAccessException while trying to invoke getId!", ex);
        } catch (IllegalArgumentException ex) {
            log.warn("Caught IllegalArgumentException while trying to invoke getId!", ex);
        } catch (InvocationTargetException ex) {
            log.warn("Caught InvocationTargetException while trying to invoke getId!", ex);
        } catch (ClassCastException ex) {
            log.warn("Caught ClassCastException while trying to invoke getId!", ex);
        }
    }

    @Override
    public boolean equals(java.lang.Object o) {
        return this.adaptee.equals(o);
    }

    @Override
    public int hashCode() {
        return this.adaptee.hashCode();
    }

    @Override
    public String toString() {
        return this.adaptee.toString();
    }
}
