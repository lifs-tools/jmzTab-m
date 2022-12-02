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
import org.lifstools.mztab2.io.AbstractSerializerTest;
import org.lifstools.mztab2.io.TestResources;
import org.lifstools.mztab2.model.Database;
import org.lifstools.mztab2.model.Metadata;
import static org.lifstools.mztab2.model.Metadata.PrefixEnum.MTD;
import org.lifstools.mztab2.model.Parameter;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 *
 * @author nilshoffmann
 */
public class DatabaseSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serializeSingle method, of class ContactSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        ObjectWriter writer = metaDataWriter();
        
        Metadata mtd = new Metadata();
        
        mtd.addDatabaseItem(new Database().id(1).
            param(new Parameter().id(1).
                name("no database").
                value("null")).
            version("Unknown"));
        mtd.addDatabaseItem(new Database().id(2).
            param(new Parameter().id(2).
                cvLabel("MIRIAM").
                cvAccession("MIR:00100079").
                name("HMDB")).
            version("3.6").
            prefix("hmdb").
            uri("http://www.hmdb.ca/"));

        //param prefix uri version
        assertEqSentry(TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.Properties.database + "[1]" + TAB_STRING + "[, , no database, null]" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.database + "[1]-prefix" + TAB_STRING + "null" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.database + "[1]-uri" + TAB_STRING + "null" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.database + "[1]-version" + TAB_STRING + "Unknown" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.database + "[2]" + TAB_STRING + "[MIRIAM, MIR:00100079, HMDB, ]" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.database + "[2]-prefix" + TAB_STRING + "hmdb" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.database + "[2]-uri" + TAB_STRING + "http://www.hmdb.ca/" + NEW_LINE
            + MTD + TAB_STRING + Metadata.Properties.database + "[2]-version" + TAB_STRING + "3.6" + NEW_LINE,
            serializeSingle(writer, mtd));
    }
}
