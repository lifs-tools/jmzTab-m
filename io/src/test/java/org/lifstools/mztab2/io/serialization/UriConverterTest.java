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
package org.lifstools.mztab2.io.serialization;

import org.lifstools.mztab2.io.serialization.UriConverter;
import org.lifstools.mztab2.model.Uri;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author nilshoffmann
 */
public class UriConverterTest {

    /**
     * Test of convert method, of class UriConverter.
     */
    @Test
    public void testConvert() {
        UriConverter converter = new UriConverter();
        String converted = converter.convert(new Uri().value("https://github.com/HUPO-PSI/mzTab"));
        Assert.assertEquals("https://github.com/HUPO-PSI/mzTab", converted);
    }
    
}
