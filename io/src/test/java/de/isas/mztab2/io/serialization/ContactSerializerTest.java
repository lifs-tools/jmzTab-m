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

import de.isas.mztab2.io.AbstractSerializerTest;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.isas.mztab2.model.Contact;
import de.isas.mztab2.model.Metadata;
import static de.isas.mztab2.model.Metadata.PrefixEnum.MTD;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class ContactSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serializeSingle method, of class ContactSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        ObjectWriter writer = metaDataWriter();

        Metadata mtd = new Metadata();
        
        mtd.addContactItem(new Contact().id(1).
            name("James D. Watson").
            affiliation("Cambridge University, UK").
            email("watson@cam.ac.uk"));
        mtd.addContactItem(new Contact().id(2).
            name("Francis Crick").
            affiliation("Cambridge University, UK").
            email("crick@cam.ac.uk"));
        
        assertEqSentry(MTD + TAB_STRING + Metadata.Properties.contact + "[1]-name" + TAB_STRING + "James D. Watson" + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.contact + "[1]-email" + TAB_STRING + "watson@cam.ac.uk" + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.contact + "[1]-affiliation" + TAB_STRING + "Cambridge University, UK" + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.contact + "[2]-name" + TAB_STRING + "Francis Crick" + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.contact + "[2]-email" + TAB_STRING + "crick@cam.ac.uk" + NEW_LINE +
            MTD + TAB_STRING + Metadata.Properties.contact + "[2]-affiliation" + TAB_STRING + "Cambridge University, UK" + NEW_LINE,
            serializeSingle(writer, mtd));
    }
    
}
