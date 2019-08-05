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

import de.isas.mztab2.model.Parameter;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class ParameterConverterTest {

    public ParameterConverterTest() {
    }

    /**
     * Test of convert method, of class ParameterConverter.
     */
    @Test
    public void testConvert() {

        ParameterConverter conv = new ParameterConverter();
        Parameter p1 = new Parameter().id(1).
            cvLabel("SEP").
            cvAccession("sep:00210").
            name("liquid chromatography");

        assertEquals("[SEP, sep:00210, liquid chromatography, ]", conv.convert(
            p1));

    }

    /**
     * Test of convert method, of class ParameterConverter.
     */
    @Test
    public void testConvertWithValue() {

        ParameterConverter conv = new ParameterConverter();
        Parameter p1 = new Parameter().id(1).
            cvLabel("SEP").
            cvAccession("sep:00210").
            name("liquid chromatography").
            value("89.12");

        assertEquals("[SEP, sep:00210, liquid chromatography, 89.12]", conv.
            convert(
                p1));

    }

    /**
     * Test of convert method, of class ParameterConverter.
     */
    @Test
    public void testConvertUserParam() {

        ParameterConverter conv = new ParameterConverter();
        Parameter p1 = new Parameter().id(1).
            name("liquid chromatography").
            value("89.12");

        assertEquals("[, , liquid chromatography, 89.12]", conv.convert(
            p1));

    }
    
    /**
     * Test of convert method, of class ParameterConverter with escaping for "," in name part.
     */
    @Test
    public void testEscapeStringHandling() {
        //[MOD, MOD:00648, "N,O-diacetylated L-serine",]
        
        ParameterConverter conv = new ParameterConverter();
        Parameter p1 = new Parameter().id(1).
            cvLabel("MOD").
            cvAccession("MOD:00648").
            name("N,O-diacetylated L-serine").
            value("892,123");

        assertEquals("[MOD, MOD:00648, "+"\"N,O-diacetylated L-serine\", \"892,123\"]", conv.convert(
            p1));
    }

}
