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
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.Sample;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.TABs;
import static de.isas.mztab1_1.model.Metadata.PrefixEnum.MTD;
import de.isas.mztab1_1.model.Parameter;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataElement;
import uk.ac.ebi.pride.jmztab1_1.model.MetadataProperty;

/**
 *
 * @author Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V.
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
            name(MetadataElement.ASSAY + " 1");
        metadata.addAssayItem(assay);

        assertEqSentry(
            MTD + TABs + MetadataElement.ASSAY + "[1]" + TABs + MetadataElement.ASSAY + " 1" + NEW_LINE
            + MTD + TABs + MetadataElement.ASSAY + "[1]-external_uri" + TABs + "http://jus.tf.for.testing.de/really" + NEW_LINE,
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
            name(MetadataElement.ASSAY + " 1").
            sampleRef(sample);
        metadata.addAssayItem(assay);

        assertEqSentry(
            MTD + TABs + MetadataElement.ASSAY + "[" + assay.getId() + "]" + TABs + "assay 1" + NEW_LINE
            + MTD + TABs + MetadataElement.ASSAY + "[" + assay.getId() + "]-" + MetadataProperty.ASSAY_EXTERNAL_URI + TABs + "http://jus.tf.for.testing.de/really" + NEW_LINE
            + MTD + TABs + MetadataElement.ASSAY + "[" + assay.getId() + "]-" + MetadataProperty.ASSAY_SAMPLE_REF + TABs + "sample[1]" + NEW_LINE,
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
            msRunRef(msRun);
        metadata.addAssayItem(assay);

        assertEqSentry(
            MTD + TABs + MetadataElement.ASSAY + "[" + assay.getId() + "]" + TABs + MetadataElement.ASSAY + " 1" + NEW_LINE
            + MTD + TABs + MetadataElement.ASSAY + "[" + assay.getId() + "]-" + MetadataProperty.ASSAY_EXTERNAL_URI + TABs + "http://jus.tf.for.testing.de/really" + NEW_LINE
            + MTD + TABs + MetadataElement.ASSAY + "[" + assay.getId() + "]-" + MetadataProperty.ASSAY_MS_RUN_REF + TABs + MetadataElement.MS_RUN + "[" + msRun.
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
        Assay assay = new Assay().id(1).
            name("assay 1").
            addCustomItem(customParam);
        metadata.addAssayItem(assay);

        assertEqSentry(
            MTD + TABs + MetadataElement.ASSAY + "[" + assay.getId() + "]" + TABs + MetadataElement.ASSAY + " 1" + NEW_LINE
            + MTD + TABs + MetadataElement.ASSAY + "[" + assay.getId() + "]-" + MetadataProperty.ASSAY_CUSTOM + TABs + new ParameterConverter().
            convert(customParam)
            + NEW_LINE,
            serialize(writer, metadata));
    }

}
