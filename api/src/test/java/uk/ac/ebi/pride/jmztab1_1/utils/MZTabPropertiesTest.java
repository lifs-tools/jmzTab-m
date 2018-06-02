/*
 * Copyright 2018 nilshoffmann.
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
package uk.ac.ebi.pride.jmztab1_1.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class MZTabPropertiesTest {

    /**
     * Test of getProperty method, of class MZTabProperties.
     */
    @Test
    public void testGetProperty() {
        Assert.assertEquals("2.0.0-M",MZTabProperties.getProperty("mztab.version"));
        Assert.assertEquals("UTF-8", MZTabProperties.ENCODE);
        Assert.assertEquals("UTF-8", MZTabProperties.getProperty("mztab.encode"));
        Assert.assertEquals("200", MZTabProperties.getProperty("mztab.max_error_count"));
        Assert.assertEquals("Info", MZTabProperties.getProperty("mztab.level"));
    }
    
}
