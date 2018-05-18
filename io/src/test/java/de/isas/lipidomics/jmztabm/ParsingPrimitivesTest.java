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
package de.isas.lipidomics.jmztabm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class ParsingPrimitivesTest {
    
    @Test
    public void testIntegerParsing() {
        String positiveInteger = "+1";
        Assert.assertEquals(1, Integer.parseInt(positiveInteger));
        String negativeInteger = "-1";
        Assert.assertEquals(-1, Integer.parseInt(negativeInteger));
    }
    
    @Test
    public void testRegexps() {
        Pattern adductPattern = Pattern.compile("^\\[\\d*M([+-][\\w]*)\\]\\d*[+-]$");
        Assert.assertTrue(adductPattern.matcher("[M+H]1+").matches());
        Pattern versionPattern = Pattern.compile("(?<major>\\d{1})\\.(?<minor>\\d{1})\\.(?<micro>\\d{1})-(?<profile>[A-Z]+)");
        Matcher m = versionPattern.matcher("2.0.1-M");
        Assert.assertTrue(m.matches());
        Assert.assertEquals("2", m.group("major"));
        Assert.assertEquals("0", m.group("minor"));
        Assert.assertEquals("1", m.group("micro"));
        Assert.assertEquals("M", m.group("profile"));
    }
}
