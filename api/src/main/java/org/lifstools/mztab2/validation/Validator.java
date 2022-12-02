/*
 * Copyright 2017 Leibniz Institut für Analytische Wissenschaften - ISAS e.V..
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
package org.lifstools.mztab2.validation;

import org.lifstools.mztab2.model.ValidationMessage;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Validator interface.</p>
 *
 * @author nilshoffmann
 * @param <T> the return type of the validator's validate methods.
 *
 */
public interface Validator<T> {

    /**
     * <p>
     * validate.</p>
     * 
     * Please make sure that all exceptions are caught within the validate method!
     *
     * @param t the object to validate.
     * @return a {@link java.util.List} object.
     */
    public default List<ValidationMessage> validate(T t) {
        return Collections.emptyList();
    }

}
