/*
 * Copyright 2018 nilshoffmann.
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
package de.isas.lipidomics.jmztabm.cvmapping.rules;

import de.isas.lipidomics.jmztabm.validation.Validator;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.ValidationMessage;
import java.util.List;

/**
 *
 * @author nilshoffmann
 */
public class ParameterValidator implements Validator<Parameter> {

    @Override
    public List<ValidationMessage> validate(Parameter t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
