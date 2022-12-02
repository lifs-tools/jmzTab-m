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

import com.fasterxml.jackson.databind.ObjectWriter;
import org.lifstools.mztab2.io.AbstractSerializerTest;
import org.lifstools.mztab2.io.TestResources;
import org.lifstools.mztab2.io.serialization.ParameterConverter;
import org.lifstools.mztab2.model.Metadata;
import static org.lifstools.mztab2.model.Metadata.PrefixEnum.MTD;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.Software;
import java.util.Arrays;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class SoftwareSerializerTest extends AbstractSerializerTest {

    public SoftwareSerializerTest() {
    }

    /**
     * Test of serializeSingle method, of class SoftwareSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        Metadata mtd = new Metadata();
        Software software1 = new Software().id(1).
            parameter(new Parameter().cvLabel(
                "MS").
                cvAccession("MS:1001207").
                name("Mascot").
                value("2.3")).
            setting(Arrays.asList("Fragment tolerance = 0.1Da",
                "Parent tolerance = 0.5Da"));
        mtd.addSoftwareItem(software1);

        ObjectWriter writer = metaDataWriter();
        assertEqSentry(TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.Properties.software + "[1]" + TAB_STRING + new ParameterConverter().
                convert(software1.
                    getParameter())
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.software + "[1]-" + Software.Properties.setting + "[1]" + TAB_STRING + software1.
                getSetting().
                get(0)
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.software + "[1]-" + Software.Properties.setting + "[2]" + TAB_STRING + software1.
                getSetting().
                get(1)
            + NEW_LINE,
            serializeSingle(writer, mtd));
    }

}
