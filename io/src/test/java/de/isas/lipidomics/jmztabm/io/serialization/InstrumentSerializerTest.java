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

import de.isas.lipidomics.jmztabm.io.AbstractSerializerTest;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.Metadata;
import static de.isas.mztab1_1.model.Metadata.PrefixEnum.MTD;
import de.isas.mztab1_1.model.Parameter;
import java.util.Arrays;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class InstrumentSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serialize method, of class AssaySerializer.
     */
    @Test
    public void testInstrument() throws Exception {
        ObjectWriter writer = metaDataWriter();

        Metadata metadata = new Metadata();
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
                    name("linear ion trap"),
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000079").
                    name("fourier transform ion cyclotron resonance mass spectrometer"))
            ).
            detector(
                new Parameter().cvLabel("MS").
                    cvAccession("MS:1000253").
                    name("electron multiplier")
            );
        metadata.addInstrumentItem(instrument1);

        assertEqSentry(
            MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-name" + TAB_STRING + new ParameterConverter().convert(instrument1.getName()) + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-source" + TAB_STRING + new ParameterConverter().convert(instrument1.getSource()) + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-analyzer[1]" + TAB_STRING + new ParameterConverter().convert(instrument1.getAnalyzer().get(0)) + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-analyzer[2]" + TAB_STRING + new ParameterConverter().convert(instrument1.getAnalyzer().get(1)) + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.instrument + "[1]-detector" + TAB_STRING + new ParameterConverter().convert(instrument1.getDetector()) + NEW_LINE,
            serialize(writer, metadata));

    }

}
