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

import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.OptColumnMapping;
import org.lifstools.mztab2.model.Parameter;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab2.model.OptColumnMappingBuilder.GlobalOptColumnMappingBuilder;
import uk.ac.ebi.pride.jmztab2.model.OptColumnMappingBuilder.IndexedElementOptColumnMappingBuilder;

/**
 *
 * @author nilshoffmann
 */
public class OptColumnMappingBuilderTest {

    public static GlobalOptColumnMappingBuilder FIXTURE_GLOBAL = OptColumnMappingBuilder.forGlobal().withName("whatever");
    public static IndexedElementOptColumnMappingBuilder FIXTURE_INDEXED_ELEMENT = OptColumnMappingBuilder.forIndexedElement(new Assay().id(1).
                name("Assay 1")).
        withName("whatever");
    public static IndexedElementOptColumnMappingBuilder FIXTURE_INDEXED_ELEMENT_PARAM = OptColumnMappingBuilder.forIndexedElement(new Assay().id(1).
                name("Assay 1")).
        withParameter(new Parameter().cvLabel("MS").cvAccession("MS:128712").name("made up for testing"));
    public static GlobalOptColumnMappingBuilder FIXTURE_GLOBAL_PARAM = OptColumnMappingBuilder.forGlobal().withParameter(new Parameter().cvLabel("MS").cvAccession("MS:128712").name("made up for testing"));
    
    /**
     * Test of forGlobal method, of class OptColumnMappingBuilder.
     */
    @Test
    public void testForGlobal() {
        OptColumnMapping ocm = FIXTURE_GLOBAL.build("1");
        assertEquals("1", ocm.getValue());
        assertEquals("opt_global_whatever", ocm.getIdentifier());
        assertNull(ocm.getParam());
        
        try {
            FIXTURE_GLOBAL.withParameter(new Parameter());
            Assert.fail("Should throw IllegalStateException");
        } catch (IllegalStateException ise) {
            
        }
    }

    /**
     * Test of forIndexedElement method, of class OptColumnMappingBuilder.
     */
    @Test
    public void testForIndexedElement() {
        OptColumnMapping ocm = FIXTURE_INDEXED_ELEMENT.
            build("1");
        assertEquals("1", ocm.getValue());
        assertEquals("opt_assay[1]_whatever", ocm.getIdentifier());
        assertNull(ocm.getParam());
        
        try {
            FIXTURE_INDEXED_ELEMENT.withParameter(new Parameter());
            Assert.fail("Should throw IllegalStateException");
        } catch (IllegalStateException ise) {
            
        }
        
    }

    /**
     * Test of withParameter method, of class OptColumnMappingBuilder.
     */
    @Test
    public void testWithParameterIndexedElement() {
        OptColumnMapping ocm = FIXTURE_INDEXED_ELEMENT_PARAM.
            build("1");
        assertEquals("1", ocm.getValue());
        assertEquals("opt_assay[1]_cv_MS:128712_made_up_for_testing", ocm.getIdentifier());
        assertNotNull(ocm.getParam());
    }
    
    /**
     * Test of withParameter method for global scope, of class OptColumnMappingBuilder.
     */
    @Test
    public void testWithParameterGlobal() {
        OptColumnMapping ocm = FIXTURE_GLOBAL_PARAM.
            build("1");
        assertEquals("1", ocm.getValue());
        assertEquals("opt_global_cv_MS:128712_made_up_for_testing", ocm.getIdentifier());
        assertNotNull(ocm.getParam());
    }
    
    /**
     * Test of withParameter method without cv label, of class OptColumnMappingBuilder with non-cv parameters.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithCvParameterWithoutCvLabel() {
        OptColumnMapping ocm = OptColumnMappingBuilder.forIndexedElement(new Assay().id(1).
                name("Assay 1")).withParameter(new Parameter().cvAccession("MS").cvLabel("").value("made up for testing")).
            build("1");
    }
    
    /**
     * Test of withParameter method without cv accession, of class OptColumnMappingBuilder with non-cv parameters.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithCvParameterWithoutCvAccession() {
        OptColumnMapping ocm = OptColumnMappingBuilder.forIndexedElement(new Assay().id(1).
                name("Assay 1")).withParameter(new Parameter().cvAccession("").value("made up for testing")).
            build("1");
    }
    
    /**
     * Test of withParameter method without parameter name, of class OptColumnMappingBuilder with non-cv parameters.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithCvParameterWithoutParamName() {
        OptColumnMappingBuilder.forIndexedElement(new Assay().id(1).
                name("Assay 1")).withParameter(new Parameter().cvAccession("MS:19872").cvLabel("MS").name("").value("made up for testing")).
            build("1");
    }
}
