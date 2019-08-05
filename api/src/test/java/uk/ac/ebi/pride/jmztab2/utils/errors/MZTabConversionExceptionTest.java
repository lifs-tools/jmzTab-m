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
public class MZTabConversionExceptionTest {

    @Test
    public void testMessage() {
        MZTabConversionException exception = new MZTabConversionException("test message");
        assertEquals("test message", exception.getMessage());
    }

    @Test
    public void testMessageAndThrowable() {
        RuntimeException ex = new RuntimeException("Some cause");
        MZTabConversionException exception = new MZTabConversionException("test message", ex);
        assertEquals("test message", exception.getMessage());
        assertEquals(ex, exception.getCause());
    }

    @Test
    public void testThrowable() {
        RuntimeException ex = new RuntimeException("Some cause");
        MZTabConversionException exception = new MZTabConversionException(ex);
        assertEquals(ex, exception.getCause());
    }

}
