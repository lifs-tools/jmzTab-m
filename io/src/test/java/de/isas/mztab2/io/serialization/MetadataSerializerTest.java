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
import de.isas.mztab2.model.Contact;
import de.isas.mztab2.model.Metadata;
import static de.isas.mztab2.model.Metadata.PrefixEnum.MTD;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.Parameter;
import org.junit.Ignore;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class MetadataSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serialize method, of class MetadataSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        Metadata metadata = new de.isas.mztab2.model.Metadata().mzTabVersion(
            "2.0.0-M").
            mzTabID("ISAS_2017_M_11451").
            title("A minimal test file").
            description("A description of an mzTab file.").
            addContactItem(
                new Contact().id(1).
                    name("Nils Hoffmann").
                    email("nils.hoffmann_at_isas.de").
                    affiliation(
                        "ISAS e.V. Dortmund, Germany")
            ).
            addMsRunItem(
                new MsRun().id(1).
                    location("file:///path/to/file1.mzML").
                    format(
                        new Parameter().
                            cvLabel("MS").
                            cvAccession("MS:1000584").
                            name("mzML file")
                    )
            );
        ObjectWriter writer = metaDataWriter();
        assertEqSentry(
            MTD + TAB_STRING + Metadata.Properties.mzTabVersion + TAB_STRING + "2.0.0-M" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.mzTabID + TAB_STRING + "ISAS_2017_M_11451" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.title + TAB_STRING + "A minimal test file" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.description + TAB_STRING + "A description of an mzTab file." + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.contact + "[1]-name" + TAB_STRING + "Nils Hoffmann" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.contact + "[1]-email" + TAB_STRING + "nils.hoffmann_at_isas.de" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.contact + "[1]-affiliation" + TAB_STRING + "ISAS e.V. Dortmund, Germany" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-location" + TAB_STRING + "file:///path/to/file1.mzML" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.msRun + "[1]-format" + TAB_STRING + "[MS, MS:1000584, mzML file, ]" + NEW_LINE,
            serialize(writer, metadata));
    }

}
