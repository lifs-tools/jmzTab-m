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

import de.isas.mztab2.io.MzTabNonValidatingWriter;
import de.isas.mztab2.model.MzTab;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import de.isas.mztab2.test.utils.LogMethodName;
import uk.ac.ebi.pride.jmztab2.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 *
 * @author nilshoffmann
 */
public class MzTabRawParserTest {

    @Rule
    public LogMethodName methodNameLogger = new LogMethodName();

    @Test
    public void testLipidomicsExample() throws MZTabException {
        testExample("metabolomics/lipidomics-example.mzTab",
            MZTabErrorType.Level.Warn, 0);
    }

    @Test
    public void testMtbls263Example() throws MZTabException {
        testExample("metabolomics/MTBLS263.mztab", MZTabErrorType.Level.Warn, 0);
    }
    
    @Test
    public void testLda2Example() throws MZTabException {
        testExample("metabolomics/lda2-lipidomics.mztab", MZTabErrorType.Level.Warn, 0);
    }

    @Test
    public void testMetadataOnlyExampleError() throws MZTabException {
        //setting the level to Error includes Error only, missing section is a warning,
        //so we do not expect an exception to be raised here.
        testExample("metabolomics/minimal-m-2.0.mztab",
            MZTabErrorType.Level.Error, 0);
    }

    @Test
    public void testMetadataOnlyExampleWarn() throws MZTabException {
        //setting the level to Warn includes Warn and Error
        testExample("metabolomics/minimal-m-2.0.mztab",
            MZTabErrorType.Level.Warn, 1);
    }

    @Test
    public void testMetadataOnlyExampleInfo() throws MZTabException {
        //setting the level to Info includes Warn and Error
        testExample("metabolomics/minimal-m-2.0.mztab",
            MZTabErrorType.Level.Info, 1);
    }

    void testExample(String resource, MZTabErrorType.Level level,
        Integer expectedErrors) throws MZTabException {
        try {
            MzTab mzTab = parseResource(resource, level, expectedErrors);
            Assert.assertNotNull(mzTab);
            Assert.assertNotNull(mzTab.getMetadata());
            MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
            System.out.println("JACKSON serialized: " + resource);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                try (OutputStreamWriter osw = new OutputStreamWriter(
                    baos, Charset.forName("UTF8"))) {
                    writer.write(osw, mzTab);
                    osw.flush();
                    Logger.getLogger(MzTabRawParserTest.class.getName()).
                        log(Level.INFO, baos.toString());
                }
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(MzTabRawParserTest.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(MzTabRawParserTest.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(MzTabRawParserTest.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (MZTabException e) {
            Logger.getLogger(MzTabRawParserTest.class.getName()).
                log(Level.SEVERE, null, e);
            throw e;
        } catch (MZTabErrorOverflowException e) {
            Logger.getLogger(MzTabRawParserTest.class.getName()).
                log(Level.SEVERE, null, e);
            throw e;
        }
    }

    public static MzTab parseResource(String resource,
        MZTabErrorType.Level level, Integer expectedErrors) throws URISyntaxException, IOException, MZTabException, MZTabErrorOverflowException {
        URL url = MzTabRawParserTest.class.getClassLoader().
            getResource(resource);
        Assert.assertNotNull(url);
        MZTabFileParser parser = new MZTabFileParser(url.toURI());
        parser.parse(System.err, level, 500);
        if (parser.getErrorList().
            size() != expectedErrors) {
            Assert.fail(parser.getErrorList().
                toString());
        }
        if (parser.getMZTabFile()==null) {
            Assert.fail(parser.getErrorList().
                toString());
        }
        return parser.getMZTabFile();
    }

}
