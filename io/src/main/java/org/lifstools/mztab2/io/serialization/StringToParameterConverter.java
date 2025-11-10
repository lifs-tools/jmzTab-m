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
package org.lifstools.mztab2.io.serialization;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.lifstools.mztab2.model.Parameter;
import uk.ac.ebi.pride.jmztab2.model.MZTabUtils;

/**
 * <p>
 * String to Parameter converter implementation for
 * {@link org.lifstools.mztab2.model.Parameter}.</p>
 *
 * @author nilshoffmann
 */
public class StringToParameterConverter extends StdConverter<String, Parameter> {

    @Override
    public Parameter convert(String in) {
        return MZTabUtils.parseParam(in);
    }
}
