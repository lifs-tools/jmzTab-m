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
package uk.ac.ebi.pride.jmztab2.model;

import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.OptColumnMapping;
import de.isas.mztab2.model.Parameter;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class OptColumnMappingBuilderTest {

    /**
     * Test of forGlobal method, of class OptColumnMappingBuilder.
     */
    @Test
    public void testForGlobal() {
        OptColumnMappingBuilder builder = new OptColumnMappingBuilder();
        OptColumnMapping ocm = builder.forGlobal().withName("whatever").
            build("1");
        assertEquals("1", ocm.getValue());
        assertEquals("opt_global_whatever", ocm.getIdentifier());
        assertNull(ocm.getParam());
        
        try {
            builder.withParameter(new Parameter());
            Assert.fail("Should throw IllegalStateException");
        } catch (IllegalStateException ise) {
            
        }
    }

    /**
     * Test of forIndexedElement method, of class OptColumnMappingBuilder.
     */
    @Test
    public void testForIndexedElement() {
        OptColumnMappingBuilder builder = new OptColumnMappingBuilder();
        OptColumnMapping ocm = builder.withName("whatever").
            forIndexedElement(new Assay().id(1).
                name("Assay 1")).
            build("1");
        assertEquals("1", ocm.getValue());
        assertEquals("opt_assay[1]_whatever", ocm.getIdentifier());
        assertNull(ocm.getParam());
        
        try {
            builder.withParameter(new Parameter());
            Assert.fail("Should throw IllegalStateException");
        } catch (IllegalStateException ise) {
            
        }
        
    }

    /**
     * Test of withParameter method, of class OptColumnMappingBuilder.
     */
    @Test
    public void testWithParameter() {
        OptColumnMappingBuilder builder = new OptColumnMappingBuilder();
        OptColumnMapping ocm = builder.withParameter(new Parameter().id(
            1).cvLabel("MS").cvAccession("MS:128712").name("made up for testing")).
            forIndexedElement(new Assay().id(1).
                name("Assay 1")).
            build("1");
        assertEquals("1", ocm.getValue());
        assertEquals("opt_assay[1]_cv_MS:128712_made_up_for_testing", ocm.getIdentifier());
        assertNotNull(ocm.getParam());
        
        try {
            builder.forGlobal();
            Assert.fail("Should throw IllegalStateException");
        } catch (IllegalStateException ise) {
            
        }
    }
    
    /**
     * Test of withParameter method, of class OptColumnMappingBuilder with non-cv parameters.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithNonCvParameter() {
        OptColumnMappingBuilder builder = new OptColumnMappingBuilder();
        OptColumnMapping ocm = builder.withParameter(new Parameter().id(
            1).cvLabel("").value("made up for testing")).
            forIndexedElement(new Assay().id(1).
                name("Assay 1")).
            build("1");
    }
}
