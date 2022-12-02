/*
 * Copyright 2020 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package org.lifstools.mztab2.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import javax.validation.ValidationException;

/**
 * Indexed elements (IDs) define a unique ID for a collection of multiple
 * metadata elements of the same type within the mzTab-M document, e.g. for
 * sample, assay, study variable etc.
 *
 * @author nilshoffmann
 */
public interface IndexedElement {

    /**
     * Returns the id of the indexed element. May be null.
     *
     * @return the id.
     */
    public Integer getId();

    /**
     * Create a new indexed element as a proxy of the provided element.
     *
     * @param element the element with a getId, hashCode and toString method to
     * proxy.
     * @return the indexed element proxy.
     * @throws ValidationException if the passed in element can not be proxied.
     */
    public static IndexedElement of(Object element) {
        IndexedElement proxyInstance = (IndexedElement) Proxy.newProxyInstance(
                IndexedElement.class.getClassLoader(),
                new Class[]{IndexedElement.class},
                (proxy, method, methodArgs) -> {
                    if (method.getName().equals("getId")) {

                        try {
                            Integer id = (Integer) element.getClass().getMethod("getId").invoke(element);
                            return id;
                        } catch (NoSuchMethodException ex) {
                            throw new ValidationException(
                                    "'id' field of " + element.toString() + " was not accessible by method getId! Missing method!");
                        } catch (SecurityException ex) {
                            throw new ValidationException(
                                    "'id' field of " + element.toString() + " was not accessible by method getId! Access denied!");
                        } catch (IllegalAccessException ex) {
                            throw new ValidationException(
                                    "'id' field of " + element.toString() + " was not accessible by method getId! Illegal access!");
                        } catch (IllegalArgumentException ex) {
                            throw new ValidationException(
                                    "'id' field of " + element.toString() + " was not accessible by method getId! Illegal argument!");
                        } catch (InvocationTargetException ex) {
                            throw new ValidationException(
                                    "'id' field of " + element.toString() + " was not accessible by method getId! Invalid invocation target!");
                        }
                    } else if (method.getName().equals("hashCode")) {
                        return element.getClass().getMethod("hashCode").invoke(element);
                    } else if (method.getName().equals("toString")) {
                        return element.getClass().getMethod("toString").invoke(element);
                    } else {
                        throw new ValidationException(
                                "Unsupported method: " + method.getName() + " on element of type " + element.getClass().getName());
                    }
                });
        return proxyInstance;
    }
}
