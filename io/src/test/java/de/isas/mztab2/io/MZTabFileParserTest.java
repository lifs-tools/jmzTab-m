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

import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.test.utils.ClassPathFile;
import static de.isas.mztab2.test.utils.ClassPathFile.GCXGC_MS_EXAMPLE;
import static de.isas.mztab2.test.utils.ClassPathFile.LIPIDOMICS_EXAMPLE;
import static de.isas.mztab2.test.utils.ClassPathFile.MINIMAL_EXAMPLE;
import static de.isas.mztab2.test.utils.ClassPathFile.MOUSELIVER_NEGATIVE;
import static de.isas.mztab2.test.utils.ClassPathFile.MOUSELIVER_NEGATIVE_MZTAB_NULL_COLUNIT;
import static de.isas.mztab2.test.utils.ClassPathFile.MTBLS263;
import static de.isas.mztab2.test.utils.ClassPathFile.STANDARDMIX_NEGATIVE_EXPORTPOSITIONLEVEL;
import static de.isas.mztab2.test.utils.ClassPathFile.STANDARDMIX_NEGATIVE_EXPORTSPECIESLEVEL;
import static de.isas.mztab2.test.utils.ClassPathFile.STANDARDMIX_POSITIVE_EXPORTPOSITIONLEVEL;
import static de.isas.mztab2.test.utils.ClassPathFile.STANDARDMIX_POSITIVE_EXPORTSPECIESLEVEL;
import de.isas.mztab2.test.utils.ExtractClassPathFiles;
import de.isas.mztab2.test.utils.LogMethodName;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Tests for MZTabFileParser
 *
 * @author nilshoffmann
 */
@RunWith(Parameterized.class)
public class MZTabFileParserTest {

    @Rule
    public LogMethodName methodNameLogger = new LogMethodName();

    @ClassRule
    public static final ExtractClassPathFiles EXTRACT_FILES = new ExtractClassPathFiles(
        MTBLS263,
        MOUSELIVER_NEGATIVE,
        MOUSELIVER_NEGATIVE_MZTAB_NULL_COLUNIT,
        STANDARDMIX_NEGATIVE_EXPORTPOSITIONLEVEL,
        STANDARDMIX_NEGATIVE_EXPORTSPECIESLEVEL,
        STANDARDMIX_POSITIVE_EXPORTPOSITIONLEVEL,
        STANDARDMIX_POSITIVE_EXPORTSPECIESLEVEL,
        GCXGC_MS_EXAMPLE,
        LIPIDOMICS_EXAMPLE,
        MINIMAL_EXAMPLE);

    @Parameterized.Parameters(
        name = "{index}: semantic validation of '{0}' on level '{1}' expecting '{2}' structural/logical errors")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {MTBLS263, MZTabErrorType.Level.Warn, 0},
            {MOUSELIVER_NEGATIVE, MZTabErrorType.Level.Warn, 0},
            {MOUSELIVER_NEGATIVE_MZTAB_NULL_COLUNIT,
                MZTabErrorType.Level.Error, 1},
            {STANDARDMIX_NEGATIVE_EXPORTPOSITIONLEVEL,
                MZTabErrorType.Level.Warn, 0},
            {STANDARDMIX_NEGATIVE_EXPORTSPECIESLEVEL,
                MZTabErrorType.Level.Warn, 0},
            {STANDARDMIX_POSITIVE_EXPORTPOSITIONLEVEL,
                MZTabErrorType.Level.Warn, 0},
            {STANDARDMIX_POSITIVE_EXPORTSPECIESLEVEL,
                MZTabErrorType.Level.Warn, 0},
            {LIPIDOMICS_EXAMPLE, MZTabErrorType.Level.Warn,
                0},
            {GCXGC_MS_EXAMPLE, MZTabErrorType.Level.Warn, 0},
            {MINIMAL_EXAMPLE, MZTabErrorType.Level.Error, 1},
            {MINIMAL_EXAMPLE, MZTabErrorType.Level.Warn, 1},
            {MINIMAL_EXAMPLE, MZTabErrorType.Level.Info, 1},});
    }

    @Parameterized.Parameter(0)
    public ClassPathFile resource;
    @Parameterized.Parameter(1)
    public MZTabErrorType.Level validationLevel;
    @Parameterized.Parameter(2)
    public int expectedStructuralLogicalErrors;

    @Test
    public void testExamples() throws MZTabException, JAXBException {
        testExample(EXTRACT_FILES.getBaseDir(), resource,
            validationLevel, expectedStructuralLogicalErrors);
    }

    void testExample(File tf, ClassPathFile resource,
        MZTabErrorType.Level level,
        Integer expectedErrors) throws MZTabException {
        System.out.println("Testing example: " + resource.fileName());
        try {
            MzTab mzTab = TestResources.parseResource(tf, resource.fileName(), level,
                expectedErrors);
            Assert.assertNotNull(mzTab);
            Assert.assertNotNull(mzTab.getMetadata());
            MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
            System.out.println("JACKSON serialized: " + resource);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                try (OutputStreamWriter osw = new OutputStreamWriter(
                    baos, Charset.forName("UTF8"))) {
                    writer.write(osw, mzTab);
                    osw.flush();
                    Logger.getLogger(MZTabFileParserTest.class.getName()).
                        log(Level.INFO, baos.toString());
                }
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (IOException | IndexOutOfBoundsException ex) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (MZTabException | MZTabErrorOverflowException e) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                log(Level.SEVERE, null, e);
            throw e;
        }
    }

}
