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
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.databind.ObjectWriter;
import de.isas.lipidomics.jmztabm.io.AbstractSerializerTest;
import de.isas.mztab1_1.model.Metadata;
import static de.isas.mztab1_1.model.Metadata.PrefixEnum.MTD;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.TAB_STRING;

/**
 * TODO
 * @author nilshoffmann
 */
public class SampleSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serialize method, of class SampleSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
         Metadata mtd = new Metadata();
         
         ObjectWriter writer = metaDataWriter();
//        assertEqSentry(
//            MTD + TAB_STRING + Metadata.Properties.sampleProcessing.
//                getPropertyName() + "[1]" + TAB_STRING + new ParameterConverter().
//                convert(sp.getSampleProcessing().
//                    get(0))
//            + NEW_LINE, serialize(writer, mtd));
    }
    
}
