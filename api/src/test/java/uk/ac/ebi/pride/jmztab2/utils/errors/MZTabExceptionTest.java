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
package uk.ac.ebi.pride.jmztab2.utils.errors;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class MZTabExceptionTest {

    /**
     * Test of getError method, of class MZTabException.
     */
    @Test
    public void testGetErrorForStringMessage() {
        MZTabException instance = new MZTabException("Just a test message");
        MZTabError expResult = null;
        MZTabError result = instance.getError();
        assertEquals(expResult, result);
    }

    /**
     * Test of getError method, of class MZTabException.
     */
    @Test
    public void testGetErrorForMZTabError() {
        MZTabError expResult = new MZTabError(
                LogicalErrorType.SingleStudyVariableName, -1);
        MZTabException instance = new MZTabException(expResult);
        MZTabError result = instance.getError();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getError method, of class MZTabException.
     */
    @Test
    public void testGetErrorForMZTabErrorThrowable() {
        MZTabError expResult = new MZTabError(
                LogicalErrorType.SingleStudyVariableName, -1);
        Throwable expThrowable = new RuntimeException("Something raised an exception");
        MZTabException instance = new MZTabException(expResult, expThrowable);
        MZTabError result = instance.getError();
        assertEquals(expResult, result);
        assertEquals(expThrowable, instance.getCause());
    }
}
