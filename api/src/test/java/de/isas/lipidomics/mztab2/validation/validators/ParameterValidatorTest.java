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
package de.isas.lipidomics.mztab2.validation.validators;

import de.isas.mztab2.model.Parameter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class ParameterValidatorTest {
    
    public ParameterValidatorTest() {
    }
    
    /**
     * Test of isValid method, of class ParameterValidator.
     */
    @Test
    public void testWithNullParameter() {
        ParameterValidatorImpl impl = new ParameterValidatorImpl();
        assertTrue(impl.isValid(null, null));
    }
    
    /**
     * Test of isValid method, of class ParameterValidator.
     */
    @Test
    public void testWithUserParameter() {
        ParameterValidatorImpl impl = new ParameterValidatorImpl();
        assertTrue(impl.isValid(new Parameter().name("custom name"), null));
    }
    
    /**
     * Test of isValid method, of class ParameterValidator.
     */
    @Test
    public void testWithUserParameterValue() {
        ParameterValidatorImpl impl = new ParameterValidatorImpl();
        assertTrue(impl.isValid(new Parameter().name("custom name").value("value"), null));
    }
    
    /**
     * Test of isValid method, of class ParameterValidator.
     */
    @Test
    public void testWithCvParameter() {
        ParameterValidatorImpl impl = new ParameterValidatorImpl();
        assertTrue(impl.isValid(new Parameter().cvLabel("MS").cvAccession("MS:192781").name("cv term name name"), null));
    }
    
    /**
     * Test of isValid method, of class ParameterValidator.
     */
    @Test
    public void testWithCvParameterValue() {
        ParameterValidatorImpl impl = new ParameterValidatorImpl();
        assertTrue(impl.isValid(new Parameter().cvLabel("MS").cvAccession("MS:192781").name("cv term name name").value("value"), null));
    }
    
    /**
     * Test of isValid method, of class ParameterValidator.
     */
    @Test
    public void testWithIllegalParameterNoCvLabel() {
        ParameterValidatorImpl impl = new ParameterValidatorImpl();
        assertFalse(impl.isValid(new Parameter().cvAccession("MS:192781").name("cv term name"), null));
    }
    
    /**
     * Test of isValid method, of class ParameterValidator.
     */
    @Test
    public void testWithIllegalParameterNoCvAccession() {
        ParameterValidatorImpl impl = new ParameterValidatorImpl();
        assertFalse(impl.isValid(new Parameter().cvLabel("MS").name("cv term name"), null));
    }

    public class ParameterValidatorImpl implements ParameterValidator {
    }
    
}
