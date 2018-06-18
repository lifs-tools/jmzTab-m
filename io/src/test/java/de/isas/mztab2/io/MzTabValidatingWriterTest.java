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
package de.isas.mztab2.io;

import de.isas.mztab2.model.ValidationMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class MzTabValidatingWriterTest {

    /**
     * Test of write method, of class MzTabValidatingWriter.
     */
    @Test
    public void testWrite_OutputStreamWriter_MzTab() throws Exception {
        MzTabValidatingWriter writer = new MzTabValidatingWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Optional<List<ValidationMessage>> messages = writer.write(
            new OutputStreamWriter(baos), MzTabTestData.create2_0TestFile());
        assertValidationMessages(messages);
    }
    
    private void assertValidationMessages(Optional<List<ValidationMessage>> messages) {
        System.out.println("Validation messages: " + messages.get().
            toString());
        Assert.assertEquals(messages.get().
            toString(), 6, messages.get().
                size());
        Assert.assertEquals(messages.get().
            get(0).
            getMessageType(), ValidationMessage.MessageTypeEnum.ERROR);
        Assert.assertTrue(messages.get().
            get(0).
            getMessage().
            contains("quantification_method"));
        Assert.assertEquals(messages.get().
            get(1).
            getMessageType(), ValidationMessage.MessageTypeEnum.ERROR);
        Assert.assertTrue(messages.get().
            get(1).
            getMessage().
            contains("study_variable[1]"));
        Assert.assertEquals(messages.get().
            get(2).
            getMessageType(), ValidationMessage.MessageTypeEnum.ERROR);
        Assert.assertTrue(messages.get().
            get(2).
            getMessage().
            contains("study_variable[2]"));
        Assert.assertEquals(messages.get().
            get(3).
            getMessageType(), ValidationMessage.MessageTypeEnum.ERROR);
        Assert.assertTrue(messages.get().
            get(3).
            getMessage().
            contains("ms_run[1]-scan_polarity"));
        Assert.assertEquals(messages.get().
            get(4).
            getMessageType(), ValidationMessage.MessageTypeEnum.ERROR);
        Assert.assertTrue(messages.get().
            get(4).
            getMessage().
            contains("ms_run[2]-scan_polarity"));
        Assert.assertEquals(messages.get().
            get(5).
            getMessageType(), ValidationMessage.MessageTypeEnum.ERROR);
        Assert.assertTrue(messages.get().
            get(5).
            getMessage().
            contains("database"));
    }

    /**
     * Test of write method, of class MzTabValidatingWriter.
     */
    @Test
    public void testWrite_Path_MzTab() throws Exception {
        MzTabValidatingWriter writer = new MzTabValidatingWriter();
        File f = File.createTempFile(UUID.randomUUID().
            toString(), ".mztab");
        Optional<List<ValidationMessage>> messages = writer.write(f.toPath(),
            MzTabTestData.create2_0TestFile());
        assertValidationMessages(messages);
    }

}
