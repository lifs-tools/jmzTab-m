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
package uk.ac.ebi.pride.jmztab1_1.model;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class MZTabStringUtilsTest {
    
    public MZTabStringUtilsTest() {
    }

    /**
     * Test of isEmpty method, of class MZTabStringUtils.
     */
    @Test
    public void testIsEmpty() {
        String nullString = null;
        assertTrue(MZTabStringUtils.isEmpty(nullString));
        String emptyString = "";
        assertTrue(MZTabStringUtils.isEmpty(emptyString));
        String nonEmptyString ="nonEmpty";
        assertFalse(MZTabStringUtils.isEmpty(nonEmptyString));
    }

    /**
     * Test of toCapital method, of class MZTabStringUtils.
     */
    @Test
    public void testToCapital() {
        String emptyString = "";
        assertEquals("", MZTabStringUtils.toCapital(emptyString));
        
        String lengthOneString = "m";
        assertEquals("M", MZTabStringUtils.toCapital(lengthOneString));
        
        String toCapitalize = "mztab-m";
        assertEquals("Mztab-m", MZTabStringUtils.toCapital(toCapitalize));
    }

    /**
     * Test of parseString method, of class MZTabStringUtils.
     */
    @Test
    public void testParseString() {
        String toParse = null;
        Assert.assertNull(MZTabStringUtils.parseString(toParse));
        toParse = "";
        Assert.assertNull(MZTabStringUtils.parseString(toParse));
        toParse = " null ";
        Assert.assertNull(MZTabStringUtils.parseString(toParse));
        toParse = " NULL";
        Assert.assertNull(MZTabStringUtils.parseString(toParse));
        toParse ="anklsa ";
        Assert.assertEquals("anklsa", MZTabStringUtils.parseString(toParse));
    }
    
}
