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

import com.fasterxml.jackson.databind.ObjectWriter;
import de.isas.mztab2.io.AbstractSerializerTest;
import de.isas.mztab2.io.TestResources;
import de.isas.mztab2.model.Instrument;
import de.isas.mztab2.model.Metadata;
import static de.isas.mztab2.model.Metadata.PrefixEnum.MTD;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.Parameter;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class MsRunSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serializeSingle method, of class MsRunSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        Metadata mtd = new Metadata();
        Instrument instrument1 = new Instrument().id(1);
        MsRun msRun1 = new MsRun().id(1).
            location("file://ftp.ebi.ac.uk/path/to/file1.mzml").
            idFormat(new Parameter().cvLabel("MS").
                cvAccession("MS:1001530").
                name(
                    "mzML unique identifier")).
            format(new Parameter().cvLabel("MS").
                cvAccession("MS:1000584").
                name("mzML file")).
            hash("de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3").
            hashMethod(new Parameter().cvLabel("MS").
                cvAccession("MS:1000569").
                name("SHA-1")).
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
        assertEqSentry(TestResources.MZTAB_VERSION_HEADER
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
            serializeSingle(writer, mtd));
    }

}
