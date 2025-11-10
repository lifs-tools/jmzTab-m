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
import java.net.URI;
import org.lifstools.mztab2.io.AbstractSerializerTest;
import org.lifstools.mztab2.io.TestResources;
import org.lifstools.mztab2.model.Uri;
import org.lifstools.mztab2.model.Metadata;
import static org.lifstools.mztab2.model.Metadata.PrefixEnum.MTD;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class UriSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serializeSingle method, of class ContactSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        ObjectWriter writer = metaDataWriter();

        Metadata mtd = new Metadata();

        String uri1 = "https://github.com/HUPO-PSI/mzTab";
        String uri2 = "https://github.com/lifs-tools";
        
        mtd.addUriItem(new Uri().id(1).
                value(URI.create(uri1)));
        mtd.addUriItem(new Uri().id(2).
            value(URI.create(uri2)));

        assertEqSentry(TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.Properties.uri + "[1]" + TAB_STRING + uri1 + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.uri + "[2]" + TAB_STRING + uri2 + NEW_LINE,
            serializeSingle(writer, mtd));
    }

}
