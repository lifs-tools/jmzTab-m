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
package de.isas.lipidomics.jmztabm.cvmapping;

import de.isas.mztab1_1.model.Parameter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V.
 */
public class ParametersTest {

    
    /**
     * Test of isEqual method, of class Parameters.
     */
    @Test
    public void testIsEqual() {
        Parameter parameter1 = new Parameter().id(1).
            cvLabel("MS").
            cvAccession("879123").
            name("jklajsd").
            value("klhasd");
        Parameter parameter2 = new Parameter().id(1).
            cvLabel("MS").
            cvAccession("879123").
            name("jklajsd").
            value("klhasd");
        assertTrue("Parameters should be equal", Parameters.isEqualTo(parameter1, parameter2));
    }
    
    @Test
    public void testIsNotEqual() {
        Parameter parameter1 = new Parameter().id(1).
            cvLabel("MS").
            cvAccession("214873").
            name("jklajsd").
            value("klhasd");
        Parameter parameter2 = new Parameter().id(2).
            cvLabel("MS").
            cvAccession("879123").
            name("jklajsd").
            value("klhasd");
        //differences in cvAccession should violate equality
        assertFalse("Parameters should NOT be equal", Parameters.isEqualTo(parameter1, parameter2));
        
        //differences in cvLabel should violate equality
        parameter2.cvLabel("CLI").cvAccession("214873");
        assertFalse("Parameters should NOT be equal", Parameters.isEqualTo(parameter1, parameter2));
        
        //differences in name should violate equality
        parameter2.cvLabel("MS").name("lklakshd");
        assertFalse("Parameters should NOT be equal", Parameters.isEqualTo(parameter1, parameter2));
    }
    
    @Test
    public void testIsEqualWithDifferentValues() {
        Parameter parameter1 = new Parameter().id(1).
            cvLabel("MS").
            cvAccession("214873").
            name("jklajsd").
            value("klhasd");
        Parameter parameter2 = new Parameter().id(2).
            cvLabel("MS").
            cvAccession("214873").
            name("jklajsd").
            value("klhasd");
        //ignore differences in value
        parameter2.value("pouiopzuqwe");
        assertTrue("Parameters should be equal", Parameters.isEqualTo(parameter1, parameter2));
    }
        
    
}
