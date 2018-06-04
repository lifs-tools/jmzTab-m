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
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.Metadata;
import static de.isas.mztab1_1.model.Metadata.PrefixEnum.MTD;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.Parameter;
import java.util.Arrays;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class MsRunSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serialize method, of class MsRunSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        Metadata mtd = new Metadata();
        Instrument instrument1 = new Instrument().id(1).
            name(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:100049").
                    name("LTQ Orbitrap")).
            source(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000073").
                    name("ESI")).
            analyzer(Arrays.asList(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000291").
                    name("linear ion trap"))
            ).
            detector(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000253").
                    name("electron multiplier")
            );
        mtd.addInstrumentItem(instrument1);
        MsRun msRun1 = new MsRun().id(1).
            location("file://ftp.ebi.ac.uk/path/to/file1.mgf").
            idFormat(new Parameter().cvLabel("MS").
                cvAccession("MS:1001530").
                name(
                    "mzML unique identifier")).
            format(new Parameter().cvLabel("MS").
                cvAccession("MS:1000584").
                name("mzML file")).
            addFragmentationMethodItem(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000133").
                    name("CID")).
            instrumentRef(instrument1).
            addScanPolarityItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1000130").
                name("positive scan")).
            addScanPolarityItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1000129").
                name("negative scan"));
        mtd.addMsRunItem(msRun1);

        ObjectWriter writer = metaDataWriter();
        //[MS, MS:1000584, mzML file, ]
        //id_format [MS, MS:1000530, mzML unique identifier, ]
        assertEqSentry(
            MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-name" + TAB_STRING + new ParameterConverter().
                convert(instrument1.getName()) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-source" + TAB_STRING + new ParameterConverter().
                convert(instrument1.getSource()) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-analyzer[1]" + TAB_STRING + new ParameterConverter().
                convert(instrument1.getAnalyzer().
                    get(0)) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-analyzer[2]" + TAB_STRING + new ParameterConverter().
                convert(instrument1.getAnalyzer().
                    get(1)) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-detector" + TAB_STRING + new ParameterConverter().
                convert(instrument1.getDetector()) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-location" + TAB_STRING + msRun1.
                getLocation() + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-instrument_ref" + TAB_STRING + "instrument[" + instrument1.
                getId() + "]" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-hash" + TAB_STRING + msRun1.
                getHash() + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-hash_method" + TAB_STRING + new ParameterConverter().
                convert(msRun1.getHashMethod()) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-format" + TAB_STRING + new ParameterConverter().
                convert(msRun1.getFormat()) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-fragmentation_method[1]" + TAB_STRING + new ParameterConverter().
                convert(msRun1.getFragmentationMethod().
                    get(0)) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-scan_polarity[1]" + TAB_STRING + new ParameterConverter().
                convert(msRun1.getScanPolarity().
                    get(0)) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-scan_polarity[2]" + TAB_STRING + new ParameterConverter().
                convert(msRun1.getScanPolarity().
                    get(1)) + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-id_format" + TAB_STRING + new ParameterConverter().
                convert(msRun1.getIdFormat()) + NEW_LINE,
            serialize(writer, mtd));
    }

}
