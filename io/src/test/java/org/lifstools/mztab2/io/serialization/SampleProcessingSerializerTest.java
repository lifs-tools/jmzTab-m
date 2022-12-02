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
import org.lifstools.mztab2.model.SampleProcessing;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 * @author nilshoffmann
 */
public class SampleProcessingSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serializeSingle method, of class SampleProcessingSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        Metadata mtd = new Metadata();
        SampleProcessing sp = new SampleProcessing().id(1);
        sp.addSampleProcessingItem(new Parameter().id(1).
            cvLabel("SEP").
            cvAccession("sep:00210").
            name("liquid chromatography"));
        mtd.addSampleProcessingItem(sp);

        ObjectWriter writer = metaDataWriter();
        assertEqSentry(TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.Properties.sampleProcessing.
                getPropertyName() + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sp.getSampleProcessing().
                    get(0))
            + NEW_LINE, serializeSingle(writer, mtd));
    }

}
