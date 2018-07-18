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
import de.isas.mztab2.test.utils.ExtractClassPathFiles;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import de.isas.mztab2.test.utils.LogMethodName;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.bind.JAXBException;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
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
    public static final TemporaryFolder TF = new TemporaryFolder();

    @ClassRule
    public static final ExtractClassPathFiles EXTRACT_FILES = new ExtractClassPathFiles(
        TF,
        "/metabolomics/MTBLS263.mztab",
        "/metabolomics/MouseLiver_negative_mztab.txt",
        "/metabolomics/MouseLiver_negative_mztab_null-colunit.txt",
        "/metabolomics/StandardMix_negative_exportPositionLevel.mztab.txt",
        "/metabolomics/StandardMix_negative_exportSpeciesLevel.mztab.txt",
        "/metabolomics/StandardMix_positive_exportPositionLevel.mztab.txt",
        "/metabolomics/StandardMix_positive_exportSpeciesLevel.mztab.txt",
        "/metabolomics/gcxgc-ms-example.mztab",
        "/metabolomics/lipidomics-example.mzTab",
        "/metabolomics/minimal-m-2.0.mztab");

    @Parameterized.Parameters(
        name = "{index}: semantic validation of '{0}' on level '{1}' expecting '{2}' structural/logical errors")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"MTBLS263.mztab", MZTabErrorType.Level.Warn, 0},
            {"MouseLiver_negative_mztab.txt", MZTabErrorType.Level.Warn, 0},
            {"MouseLiver_negative_mztab_null-colunit.txt",
                MZTabErrorType.Level.Error, 1},
            {"StandardMix_negative_exportPositionLevel.mztab.txt",
                MZTabErrorType.Level.Warn, 0},
            {"StandardMix_negative_exportSpeciesLevel.mztab.txt",
                MZTabErrorType.Level.Warn, 0},
            {"StandardMix_positive_exportPositionLevel.mztab.txt",
                MZTabErrorType.Level.Warn, 0},
            {"StandardMix_positive_exportSpeciesLevel.mztab.txt",
                MZTabErrorType.Level.Warn, 0},
            {"lipidomics-example.mzTab", MZTabErrorType.Level.Warn,
                0},
            {"gcxgc-ms-example.mztab", MZTabErrorType.Level.Warn, 0},
            {"minimal-m-2.0.mztab", MZTabErrorType.Level.Error, 1},
            {"minimal-m-2.0.mztab", MZTabErrorType.Level.Warn, 1},
            {"minimal-m-2.0.mztab", MZTabErrorType.Level.Info, 1},});
    }

    @Parameterized.Parameter(0)
    public String resource;
    @Parameterized.Parameter(1)
    public MZTabErrorType.Level validationLevel;
    @Parameterized.Parameter(2)
    public int expectedStructuralLogicalErrors;

    @Test
    public void testExamples() throws MZTabException, JAXBException {
        testExample(TF, resource,
            validationLevel, expectedStructuralLogicalErrors);
    }

    void testExample(TemporaryFolder tf, String resource,
        MZTabErrorType.Level level,
        Integer expectedErrors) throws MZTabException {
        try {
            MzTab mzTab = TestResources.parseResource(tf, resource, level,
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
        } catch (IOException ex) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (MZTabException e) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                log(Level.SEVERE, null, e);
            throw e;
        } catch (MZTabErrorOverflowException e) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                log(Level.SEVERE, null, e);
            throw e;
        }
    }

}
