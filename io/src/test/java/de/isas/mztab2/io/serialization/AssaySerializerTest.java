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

import de.isas.mztab2.io.serialization.ParameterConverter;
import de.isas.mztab2.io.AbstractSerializerTest;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.Sample;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static de.isas.mztab2.model.Metadata.PrefixEnum.MTD;
import de.isas.mztab2.model.Parameter;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class AssaySerializerTest extends AbstractSerializerTest {

    /**
     * Test of serialize method, of class AssaySerializer.
     */
    @Test
    public void testAssay() throws Exception {
        ObjectWriter writer = metaDataWriter();

        Metadata metadata = new Metadata();
        Assay assay = new Assay().id(1).
            externalUri("http://jus.tf.for.testing.de/really").
            name(Metadata.Properties.assay + " 1");
        metadata.addAssayItem(assay);

        assertEqSentry(MTD + TAB_STRING + Metadata.Properties.assay + "[1]" + TAB_STRING + "assay 1" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.assay + "[1]-external_uri" + TAB_STRING + "http://jus.tf.for.testing.de/really" + NEW_LINE,
            serialize(writer, metadata));

    }

    /**
     * Test of serialize method, of class AssaySerializer.
     */
    @Test
    public void testAssayWithSampleRef() throws Exception {
        ObjectWriter writer = metaDataWriter();

        Metadata metadata = new Metadata();
        //do not add sample to metadata, just test the ref mechanism
        Sample sample = new Sample().id(1);
        Assay assay = new Assay().id(1).
            externalUri("http://jus.tf.for.testing.de/really").
            name(Metadata.Properties.assay + " 1").
            sampleRef(sample);
        metadata.addAssayItem(assay);

        assertEqSentry(MTD + TAB_STRING + Metadata.Properties.assay + "[" + assay.getId() + "]" + TAB_STRING + "assay 1" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.assay + "[" + assay.getId() + "]-" + Assay.Properties.externalUri + TAB_STRING + "http://jus.tf.for.testing.de/really" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.assay + "[" + assay.getId() + "]-" + Assay.Properties.sampleRef + TAB_STRING + "sample[1]" + NEW_LINE,
            serialize(writer, metadata));
    }

    /**
     * Test of serialize method, of class AssaySerializer.
     */
    @Test
    public void testAssayWithMsRunRef() throws Exception {
        ObjectWriter writer = metaDataWriter();

        Metadata metadata = new Metadata();
        //do not add to metadata, just test the ref mechanism
        MsRun msRun = new MsRun().id(1);
        Assay assay = new Assay().id(1).
            externalUri("http://jus.tf.for.testing.de/really").
            name("assay 1").
            addMsRunRefItem(msRun);
        metadata.addAssayItem(assay);

        assertEqSentry(MTD + TAB_STRING + Metadata.Properties.assay + "[" + assay.getId() + "]" + TAB_STRING + "assay 1" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.assay + "[" + assay.getId() + "]-" + Assay.Properties.externalUri + TAB_STRING + "http://jus.tf.for.testing.de/really" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.assay + "[" + assay.getId() + "]-" + Assay.Properties.msRunRef + TAB_STRING + Metadata.Properties.msRun + "[" + msRun.
            getId() + "]" + NEW_LINE,
            serialize(writer, metadata));
    }

    /**
     * Test of serialize method, of class AssaySerializer.
     */
    @Test
    public void testAssayWithCustom() throws Exception {
        ObjectWriter writer = metaDataWriter();

        Metadata metadata = new Metadata();
        //do not add to metadata, just test the ref mechanism
        Parameter customParam = new Parameter().id(1).
            name("custom param").
            value("custom value");
        Parameter customParam2 = new Parameter().id(2).name("custom param2").value("custom value 2");
        Assay assay = new Assay().id(1).
            name("assay 1").
            addCustomItem(customParam).addCustomItem(customParam2);
        metadata.addAssayItem(assay);

        assertEqSentry(
            MTD + TAB_STRING + Metadata.Properties.assay + "[" + assay.getId() + "]" + TAB_STRING + "assay 1" + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.assay + "[" + assay.getId() + "]-" + Assay.Properties.custom+"["+customParam.getId()+"]" + TAB_STRING + new ParameterConverter().convert(customParam)+ NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.assay + "[" + assay.getId() + "]-" + Assay.Properties.custom+"["+customParam2.getId()+"]" + TAB_STRING + new ParameterConverter().convert(customParam2)+ NEW_LINE,
            serialize(writer, metadata));
    }

}
