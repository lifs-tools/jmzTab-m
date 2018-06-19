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
import de.isas.mztab2.model.Metadata;
import static de.isas.mztab2.model.Metadata.PrefixEnum.MTD;
import de.isas.mztab2.model.Publication;
import de.isas.mztab2.model.PublicationItem;
import java.util.Arrays;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.BAR;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class PublicationSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serializeSingle method, of class PublicationSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        Metadata mtd = new Metadata();
        PublicationItem item1_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("21063943");
        PublicationItem item1_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1007/978-1-60761-987-1_6");
        Publication publication1 = new Publication().id(1);
        publication1.setPublicationItems(Arrays.asList(item1_1, item1_2));

        PublicationItem item2_1 = new PublicationItem().type(
            PublicationItem.TypeEnum.PUBMED).
            accession("20615486");
        PublicationItem item2_2 = new PublicationItem().type(
            PublicationItem.TypeEnum.DOI).
            accession("10.1016/j.jprot.2010.06.008");
        Publication publication2 = new Publication().id(2);
        publication2.setPublicationItems(Arrays.asList(item2_1, item2_2));

        mtd.addPublicationItem(publication1).
            addPublicationItem(publication2);
        ObjectWriter writer = metaDataWriter();
        assertEqSentry(MTD + TAB_STRING + Metadata.Properties.publication.
                getPropertyName() + "[1]" + TAB_STRING + "pubmed:21063943" + BAR + "doi:10.1007/978-1-60761-987-1_6" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.publication.
                getPropertyName() + "[2]" + TAB_STRING + "pubmed:20615486" + BAR + "doi:10.1016/j.jprot.2010.06.008"
            + NEW_LINE, serializeSingle(writer, mtd));
    }

}
