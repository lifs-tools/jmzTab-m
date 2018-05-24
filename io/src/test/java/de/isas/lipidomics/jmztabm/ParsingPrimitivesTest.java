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
import uk.ac.ebi.pride.jmztab1_1.model.MZTabColumn;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabUtils;

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
    public void testAdductRegexp() {
        Pattern adductPattern = Pattern.compile(
            "^\\[\\d*M([+-][\\w]*)\\]\\d*[+-]$");
        Assert.assertTrue(adductPattern.matcher("[M+H]1+").
            matches());
    }

    @Test
    public void testVersionRegexp() {
        Pattern versionPattern = Pattern.compile(
            "(?<major>\\d{1})\\.(?<minor>\\d{1})\\.(?<micro>\\d{1})-(?<profile>[A-Z]{1})");
        Matcher m = versionPattern.matcher("2.0.1-M");
        Assert.assertTrue(m.matches());
        Assert.assertEquals("2", m.group("major"));
        Assert.assertEquals("0", m.group("minor"));
        Assert.assertEquals("1", m.group("micro"));
        Assert.assertEquals("M", m.group("profile"));
    }

    @Test
    public void testParserVersionRegexp() {
        String result = MZTabUtils.parseMzTabVersion("2.0.0-M");
        Assert.assertEquals("2.0.0-M", result);
        
        result = MZTabUtils.parseMzTabVersion("2.0.4-M");
        Assert.assertEquals("2.0.4-M", result);
        
        result = MZTabUtils.parseMzTabVersion("2.1.9-M");
        Assert.assertEquals("2.1.9-M", result);
        
        result = MZTabUtils.parseMzTabVersion("3.1.9-M");
        Assert.assertNull(result);
        
        result = MZTabUtils.parseMzTabVersion("1.0.0");
        Assert.assertNull(result);
        
        result = MZTabUtils.parseMzTabVersion("1.0.0-M");
        Assert.assertNull(result);
        
        result = MZTabUtils.parseMzTabVersion("1.1.0-M");
        Assert.assertNull(result);
    }
}
