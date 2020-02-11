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
import de.isas.mztab2.model.CV;
import de.isas.mztab2.model.Metadata;
import static de.isas.mztab2.model.Metadata.PrefixEnum.MTD;
import java.net.URI;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class CvSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serializeSingle method, of class ContactSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        ObjectWriter writer = metaDataWriter();

        Metadata mtd = new Metadata();

        mtd.addCvItem(new CV().id(1).
            label("MS").
            fullName("PSI-MS ontology").
            version("3.54.0").
            uri(URI.create("https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo")));
        mtd.addCvItem(new CV().id(2).
            label("CHEBI").
            fullName("Chebi ontology").
            version("164").
            uri(URI.create("ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi.obo")));

        assertEqSentry(TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.Properties.cv + "[1]-label" + TAB_STRING + "MS" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.cv + "[1]-uri" + TAB_STRING + "https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.cv + "[1]-version" + TAB_STRING + "3.54.0" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.cv + "[1]-full_name" + TAB_STRING + "PSI-MS ontology" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.cv + "[2]-label" + TAB_STRING + "CHEBI" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.cv + "[2]-uri" + TAB_STRING + "ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi.obo" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.cv + "[2]-version" + TAB_STRING + "164" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.cv + "[2]-full_name" + TAB_STRING + "Chebi ontology" + NEW_LINE,
            serializeSingle(writer, mtd));
    }

}
