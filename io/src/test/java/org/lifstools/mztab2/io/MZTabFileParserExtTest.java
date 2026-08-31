/*
 * Copyright 2020 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package org.lifstools.mztab2.io;

import org.lifstools.mztab2.model.MzTab;
import static org.lifstools.mztab2.test.utils.ClassPathFile.MTBLS263;
import org.lifstools.mztab2.test.utils.ExtractClassPathFiles;
import org.lifstools.mztab2.test.utils.LogMethodName;
import java.io.IOException;
import java.net.URISyntaxException;
import jakarta.xml.bind.JAXBException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 *
 * @author nilshoffmann
 */
public class MZTabFileParserExtTest {

    @RegisterExtension
    LogMethodName methodNameLogger = new LogMethodName();

    @RegisterExtension
    static final ExtractClassPathFiles EXTRACT_FILES = new ExtractClassPathFiles(
            MTBLS263
    );

    @Test
    public void testStudyVariableAssayRefs() throws MZTabException, JAXBException, URISyntaxException, IOException {
        MzTab mzTab = TestResources.parseResource(EXTRACT_FILES.getBaseDir(),
                MTBLS263.fileName(),
                MZTabErrorType.Level.Info,
                // MTBLS263 is a legacy full file (no mzTab-profile line): the
                // inferred profile M+S+F+E is reported as a single info message.
                1, false);
        assertEquals(3, mzTab.getMetadata().getStudyVariable().get(0).getAssayRefs().size());
    }
}
