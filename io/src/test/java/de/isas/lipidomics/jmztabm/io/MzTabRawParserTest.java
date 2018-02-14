/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.isas.lipidomics.jmztabm.io;

import de.isas.mztab1_1.model.MzTab;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab1_1.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabRawParserTest {

    @Test
    public void testLipidomicsExample() throws MZTabException {
        testExample("metabolomics/lipidomics-example.mzTab",
            MZTabErrorType.Level.Error, 0);
    }

    @Test
    public void testMtbls263Example() throws MZTabException {
        testExample("metabolomics/MTBLS263.mztab", MZTabErrorType.Level.Error, 0);
    }

    @Test
    public void testMetadataOnlyExampleError() throws MZTabException {
        //setting the level to Error includes Error only, missing section is a warning,
        //so we do not expect an exception to be raised here.
        testExample("metabolomics/minimal-m-1.1.mztab",
            MZTabErrorType.Level.Error, 0);
    }

    @Test
    public void testMetadataOnlyExampleWarn() throws MZTabException {
        //setting the level to Warn includes Warn and Error
        testExample("metabolomics/minimal-m-1.1.mztab",
            MZTabErrorType.Level.Warn, 1);
    }

    @Test
    public void testMetadataOnlyExampleInfo() throws MZTabException {
        //setting the level to Info includes Warn and Error
        testExample("metabolomics/minimal-m-1.1.mztab",
            MZTabErrorType.Level.Info, 1);
    }

    void testExample(String resource, MZTabErrorType.Level level,
        Integer expectedErrors) throws MZTabException {
        try {
            MzTab mzTab = parseResource(resource, level, expectedErrors);
            MzTabWriter writer = new MzTabWriter();
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
        return parser.getMZTabFile();
    }

}
