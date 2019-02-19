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
package de.isas.mztab2.io.serialization;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import de.isas.mztab2.test.utils.LogMethodName;
import static org.junit.Assert.assertEquals;
import uk.ac.ebi.pride.jmztab2.model.IOptColumnMappingBuilder;
import uk.ac.ebi.pride.jmztab2.model.OptColumnMappingBuilderTest;

/**
 * TODO
 *
 * @author nilshoffmann
 */
public class SerializersTest {

    @Rule
    public LogMethodName methodNameLogger = new LogMethodName();

    @Test
    public void testMixedCamelCaseToUnderscore() {
        String camelCase1 = "camelCase";
        Assert.assertEquals("camel_case", Serializers.
                camelCaseToUnderscoreLowerCase(camelCase1));
    }

    @Test
    public void testCapitalCamelCaseToUnderscore() {
        String camelCase2 = "CamelCase";
        Assert.assertEquals("camel_case", Serializers.
                camelCaseToUnderscoreLowerCase(camelCase2));
    }

    @Test
    public void testUpperCaseCamelCaseMadnessToUnderscore() {
        String camelCase3 = "CAmelCASE";
        Assert.assertEquals("camel_case", Serializers.
                camelCaseToUnderscoreLowerCase(camelCase3));
    }

    /**
     * Test of getReference method, of class Serializers.
     */
    @Test
    public void testGetReference() {
    }

    /**
     * Test of printAbundanceAssay method, of class Serializers.
     */
    @Test
    public void testPrintAbundanceAssay() {
    }

    /**
     * Test of printAbundanceStudyVar method, of class Serializers.
     */
    @Test
    public void testPrintAbundanceStudyVar() {
    }

    /**
     * Test of printAbundanceCoeffVarStudyVar method, of class Serializers.
     */
    @Test
    public void testPrintAbundanceCoeffVarStudyVar() {
    }

    /**
     * Test of printOptColumnMapping method, of class Serializers.
     */
    @Test
    public void testPrintOptColumnMappingGlobal() {
        IOptColumnMappingBuilder builder = OptColumnMappingBuilderTest.FIXTURE_GLOBAL;
        String result = Serializers.printOptColumnMapping(builder.build("1"));
        assertEquals("opt_global_whatever", result);
    }

    @Test
    public void testPrintOptColumnMappingIndexedElement() {
        IOptColumnMappingBuilder builder = OptColumnMappingBuilderTest.FIXTURE_INDEXED_ELEMENT;
        String result = Serializers.printOptColumnMapping(builder.build("1"));
        assertEquals("opt_assay[1]_whatever", result);
    }

    @Test
    public void testPrintOptColumnMappingGlobalCvParameter() {
        IOptColumnMappingBuilder builder = OptColumnMappingBuilderTest.FIXTURE_GLOBAL_PARAM;
        String result = Serializers.printOptColumnMapping(builder.build("1"));
        assertEquals("opt_global_cv_MS:128712_made_up_for_testing", result);
    }

    @Test
    public void testPrintOptColumnMappingIndexedElementCvParameter() {
        IOptColumnMappingBuilder builder = OptColumnMappingBuilderTest.FIXTURE_INDEXED_ELEMENT_PARAM;
        String result = Serializers.printOptColumnMapping(builder.build("1"));
        assertEquals("opt_assay[1]_cv_MS:128712_made_up_for_testing", result);
    }

    /**
     * Test of addIndexedLine method, of class Serializers.
     */
    @Test
    public void testAddIndexedLine_5args_1() {
    }

    /**
     * Test of addIndexedLine method, of class Serializers.
     */
    @Test
    public void testAddIndexedLine_5args_2() {
    }

    /**
     * Test of addLineWithParameters method, of class Serializers.
     */
    @Test
    public void testAddLineWithParameters() {
    }

    /**
     * Test of addLineWithPropertyParameters method, of class Serializers.
     */
    @Test
    public void testAddLineWithPropertyParameters() {
    }

    /**
     * Test of addLineWithMetadataProperty method, of class Serializers.
     */
    @Test
    public void testAddLineWithMetadataProperty() {
    }

    /**
     * Test of addLineWithProperty method, of class Serializers.
     */
    @Test
    public void testAddLineWithProperty() {
    }

    /**
     * Test of addLine method, of class Serializers.
     */
    @Test
    public void testAddLine() {
    }

    /**
     * Test of getElementName method, of class Serializers.
     */
    @Test
    public void testGetElementName() {
    }

    /**
     * Test of getPropertyNames method, of class Serializers.
     */
    @Test
    public void testGetPropertyNames() {
    }

    /**
     * Test of asMap method, of class Serializers.
     */
    @Test
    public void testAsMap() {
    }

    /**
     * Test of camelCaseToUnderscoreLowerCase method, of class Serializers.
     */
    @Test
    public void testCamelCaseToUnderscoreLowerCase() {
    }

    /**
     * Test of addSubElementStrings method, of class Serializers.
     */
    @Test
    public void testAddSubElementStrings() {
    }

    /**
     * Test of addSubElementParameter method, of class Serializers.
     */
    @Test
    public void testAddSubElementParameter() {
    }

    /**
     * Test of addSubElementParameters method, of class Serializers.
     */
    @Test
    public void testAddSubElementParameters() {
    }

    /**
     * Test of checkForNull method, of class Serializers.
     */
    @Test
    public void testCheckForNull() {
    }

    /**
     * Test of writeString method, of class Serializers.
     */
    @Test
    public void testWriteString_3args_1() throws Exception {
    }

    /**
     * Test of writeString method, of class Serializers.
     */
    @Test
    public void testWriteString_3args_2() throws Exception {
    }

    /**
     * Test of writeObject method, of class Serializers.
     */
    @Test
    public void testWriteObject_4args_1() throws Exception {
    }

    /**
     * Test of writeObject method, of class Serializers.
     */
    @Test
    public void testWriteObject_4args_2() throws Exception {
    }

    /**
     * Test of writeAsNumberArray method, of class Serializers.
     */
    @Test
    public void testWriteAsNumberArray_3args_1() {
    }

    /**
     * Test of writeAsNumberArray method, of class Serializers.
     */
    @Test
    public void testWriteAsNumberArray_3args_2() {
    }

    /**
     * Test of writeAsStringArray method, of class Serializers.
     */
    @Test
    public void testWriteAsStringArray_3args_1() {
    }

    /**
     * Test of writeAsStringArray method, of class Serializers.
     */
    @Test
    public void testWriteAsStringArray_JsonGenerator_List() {
    }

    /**
     * Test of writeAsStringArray method, of class Serializers.
     */
    @Test
    public void testWriteAsStringArray_3args_2() {
    }

    /**
     * Test of writeNumber method, of class Serializers.
     */
    @Test
    public void testWriteNumber_3args_1() throws Exception {
    }

    /**
     * Test of writeNumber method, of class Serializers.
     */
    @Test
    public void testWriteNumber_3args_2() throws Exception {
    }

    /**
     * Test of writeNumber method, of class Serializers.
     */
    @Test
    public void testWriteNumber_3args_3() throws Exception {
    }

    /**
     * Test of writeNumber method, of class Serializers.
     */
    @Test
    public void testWriteNumber_3args_4() throws Exception {
    }

    /**
     * Test of writeNumber method, of class Serializers.
     */
    @Test
    public void testWriteNumber_JsonGenerator_Integer() throws Exception {
    }

    /**
     * Test of writeNumber method, of class Serializers.
     */
    @Test
    public void testWriteNumber_JsonGenerator_Double() throws Exception {
    }

    /**
     * Test of writeOptColumnMappings method, of class Serializers.
     */
    @Test
    public void testWriteOptColumnMappings() throws Exception {
    }

    /**
     * Test of writeIndexedDoubles method, of class Serializers.
     */
    @Test
    public void testWriteIndexedDoubles() {
    }

}
