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
import org.junit.BeforeClass;
import uk.ac.ebi.pride.jmztab2.utils.MZTabProperties;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType.Level;

/**
 *
 * @author nilshoffmann
 */
public class MZTabErrorTypeTest {

    @BeforeClass
    public static void initProperties() {
        MZTabProperties.getProperty("l_code_SingleStudyVariableName");
    }
    
    /**
     * Test of forLevel method, of class MZTabErrorType.
     */
    @Test
    public void testForLevel() {
        MZTabError error = new MZTabError(
                LogicalErrorType.SoftwareVersion, -1, "some string");
        MZTabErrorType expResult = error.getType();
        MZTabErrorType result = MZTabErrorType.forLevel(MZTabErrorType.Category.Logical, Level.Warn, "SoftwareVersion");
        assertEquals(expResult, result);
    }

    /**
     * Test of createError method, of class MZTabErrorType.
     */
    @Test
    public void testCreateError() {
        MZTabError error = new MZTabError(
                LogicalErrorType.SingleStudyVariableName, -1);
        MZTabErrorType expResult = error.getType();
        MZTabErrorType result = MZTabErrorType.createError(MZTabErrorType.Category.Logical, "SingleStudyVariableName");
        assertEquals(expResult, result);
    }

    /**
     * Test of createWarn method, of class MZTabErrorType.
     */
    @Test
    public void testCreateWarn() {
        MZTabError softwareVersion = new MZTabError(LogicalErrorType.SoftwareVersion, -1, "some string");
        MZTabErrorType expResult = softwareVersion.getType();
        MZTabErrorType result = MZTabErrorType.createWarn(MZTabErrorType.Category.Logical, "SoftwareVersion");
        assertEquals(expResult, result);
    }

    /**
     * Test of createInfo method, of class MZTabErrorType.
     */
    @Test
    public void testCreateInfo() {

        MZTabErrorType.Category category = MZTabErrorType.Category.Logical;
        String keyword = "NoSmallMoleculeEvidenceSection";
        MZTabError error = new MZTabError(
                LogicalErrorType.NoSmallMoleculeEvidenceSection, -1);
        MZTabErrorType expResult = error.getType();
        MZTabErrorType result = MZTabErrorType.createInfo(category, keyword);
        assertEquals(expResult, result);
    }

    /**
     * Test of getCode method, of class MZTabErrorType.
     */
    @Test
    public void testGetCode() {
        MZTabError error = new MZTabError(
                LogicalErrorType.SingleStudyVariableName, -1);
        MZTabErrorType instance = error.getType();
        Integer expResult = 2048;
        Integer result = instance.getCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCategory method, of class MZTabErrorType.
     */
    @Test
    public void testGetCategory() {
        MZTabError error = new MZTabError(
                LogicalErrorType.SingleStudyVariableName, -1);
        MZTabErrorType instance = error.getType();
        MZTabErrorType.Category expResult = MZTabErrorType.Category.Logical;
        MZTabErrorType.Category result = instance.getCategory();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLevel method, of class MZTabErrorType.
     */
    @Test
    public void testGetLevel() {
        MZTabError error = new MZTabError(
                LogicalErrorType.SingleStudyVariableName, -1);
        MZTabErrorType instance = error.getType();
        MZTabErrorType.Level expResult = MZTabErrorType.Level.Error;
        MZTabErrorType.Level result = instance.getLevel();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOriginal method, of class MZTabErrorType.
     */
    @Test
    public void testGetOriginal() {
        MZTabError error = new MZTabError(
                LogicalErrorType.SingleStudyVariableName, -1);
        MZTabErrorType instance = error.getType();
        String expResult = "A single study variable MUST be reported with identifier \"undefined\". It must link to all assays.";
        String result = instance.getOriginal();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCause method, of class MZTabErrorType.
     */
    @Test
    public void testGetCause() {
        MZTabError error = new MZTabError(
                LogicalErrorType.SingleStudyVariableName, -1);
        MZTabErrorType instance = error.getType();
        String expResult = "For software that does not capture study variables, a single study variable MUST be reported, linking to all assays. This single study variable MUST have the identifier \"undefined\".";
        String result = instance.getCause();
        assertEquals(expResult, result);
    }

    /**
     * Test of findLevel method, of class MZTabErrorType.
     */
    @Test
    public void testFindLevel() {
        assertEquals(MZTabErrorType.Level.Error, MZTabErrorType.findLevel("Error"));
        assertEquals(MZTabErrorType.Level.Error, MZTabErrorType.findLevel("ERROR"));
        assertEquals(MZTabErrorType.Level.Warn, MZTabErrorType.findLevel("Warn"));
        assertEquals(MZTabErrorType.Level.Warn, MZTabErrorType.findLevel("WARN"));
        assertEquals(MZTabErrorType.Level.Info, MZTabErrorType.findLevel("Info"));
        assertEquals(MZTabErrorType.Level.Info, MZTabErrorType.findLevel("INFO"));
    }

}
