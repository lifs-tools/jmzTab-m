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
package uk.ac.ebi.pride.jmztab2.utils.errors;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class MZTabErrorListTest {

    /**
     * Test of add method, of class MZTabErrorList.
     */
    @Test
    public void testAdd() {
        MZTabErrorList list = new MZTabErrorList(MZTabErrorType.Level.Warn, 5);
        Assert.assertEquals(0, list.size());
        MZTabError err = new MZTabError(FormatErrorType.MTDLine, 0,
            "MTD\tTEST LINE");
        list.add(err);
        Assert.assertEquals(1, list.size());
        MZTabError err2 = new MZTabError(LogicalErrorType.NotNULL, 0,
            "testcolumn");
        list.add(err2);
        Assert.assertEquals(2, list.size());
        MZTabError err3 = new MZTabError(LogicalErrorType.NoSmallMoleculeFeatureSection, 0);
        list.add(err3);
        //should remain the same size
        Assert.assertEquals(2, list.size());
        
        list = new MZTabErrorList(MZTabErrorType.Level.Info, 5);
        err = new MZTabError(FormatErrorType.MTDLine, 0,
            "MTD\tTEST LINE");
        list.add(err);
        Assert.assertEquals(1, list.size());
        err2 = new MZTabError(LogicalErrorType.NotNULL, 0,
            "testcolumn");
        list.add(err2);
        Assert.assertEquals(2, list.size());
        err3 = new MZTabError(LogicalErrorType.NoSmallMoleculeFeatureSection, 0);
        list.add(err3);
        //should remain the same size
        Assert.assertEquals(3, list.size());
    }

    /**
     * Test of getMaxErrorCount method, of class MZTabErrorList.
     */
    @Test
    public void testGetMaxErrorCount() {
        MZTabErrorList list = new MZTabErrorList(MZTabErrorType.Level.Warn, 1);
        Assert.assertEquals(1, list.getMaxErrorCount());
    }

    /**
     * Test of setMaxErrorCount method, of class MZTabErrorList.
     */
    @Test
    public void testSetMaxErrorCount() {
        MZTabErrorList list = new MZTabErrorList(MZTabErrorType.Level.Warn, 1);
        Assert.assertEquals(1, list.getMaxErrorCount());
        list.setMaxErrorCount(2);
        Assert.assertEquals(2, list.getMaxErrorCount());
    }

    /**
     * Test of getLevel method, of class MZTabErrorList.
     */
    @Test
    public void testGetLevel() {
        MZTabErrorList list = new MZTabErrorList(MZTabErrorType.Level.Warn, 1);
        Assert.assertEquals(MZTabErrorType.Level.Warn, list.getLevel());
    }

    /**
     * Test of setLevel method, of class MZTabErrorList.
     */
    @Test
    public void testSetLevel() {
        MZTabErrorList list = new MZTabErrorList(MZTabErrorType.Level.Warn, 1);
        Assert.assertEquals(MZTabErrorType.Level.Warn, list.getLevel());
        list.setLevel(MZTabErrorType.Level.Info);
        Assert.assertEquals(MZTabErrorType.Level.Info, list.getLevel());
    }

    /**
     * Test of clear method, of class MZTabErrorList.
     */
    @Test
    public void testClear() {
        MZTabErrorList list = new MZTabErrorList(MZTabErrorType.Level.Warn, 1);
        Assert.assertEquals(0, list.size());
        MZTabError err = new MZTabError(FormatErrorType.MTDLine, 0,
            "MTD\tTEST LINE");
        list.add(err);
        Assert.assertEquals(1, list.size());
        list.clear();
        Assert.assertEquals(0, list.size());
    }

    /**
     * Test of size method, of class MZTabErrorList.
     */
    @Test
    public void testSize() {
        MZTabErrorList list = new MZTabErrorList(MZTabErrorType.Level.Warn, 1);
        Assert.assertEquals(0, list.size());
        MZTabError err = new MZTabError(FormatErrorType.MTDLine, 0,
            "MTD\tTEST LINE");
        list.add(err);
        Assert.assertEquals(1, list.size());
    }

    /**
     * Test of getError method, of class MZTabErrorList.
     */
    @Test
    public void testGetError() {
        MZTabErrorList list = new MZTabErrorList(MZTabErrorType.Level.Warn, 1);
        Assert.assertEquals(0, list.size());
        MZTabError err = new MZTabError(FormatErrorType.MTDLine, 0,
            "MTD\tTEST LINE");
        list.add(err);
        Assert.assertEquals(err, list.getError(0));
    }

    /**
     * Test of isEmpty method, of class MZTabErrorList.
     */
    @Test
    public void testIsEmpty() {
        MZTabErrorList list = new MZTabErrorList(MZTabErrorType.Level.Warn, 1);
        Assert.assertTrue(list.isEmpty());
    }

}
