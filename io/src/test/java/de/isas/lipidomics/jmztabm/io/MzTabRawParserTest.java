/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.isas.lipidomics.jmztabm.io;

import de.isas.mztab1_1.model.MzTab;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab1_1.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabRawParserTest {

    @Test
    public void testLipidomicsExample() {
        try {
            parseResource("metabolomics/lipidomics-example.mzTab");
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
            Assert.fail(e.getMessage());
        } catch (MZTabErrorOverflowException e) {
            Logger.getLogger(MzTabRawParserTest.class.getName()).
                log(Level.SEVERE, null, e);
            Assert.fail(e.getMessage());
        }
    }

    public static MzTab parseResource(String resource) throws URISyntaxException, IOException, MZTabException, MZTabErrorOverflowException {
        URL url = MzTabRawParserTest.class.getClassLoader().
            getResource(resource);
        Assert.assertNotNull(url);
        MZTabFileParser parser = new MZTabFileParser(
            new File(MzTabRawParserTest.class.getClassLoader().
                getResource(resource).
                getFile()), System.err);
        if (parser.getErrorList().
            size() > 0) {
            Assert.fail(parser.getErrorList().
                toString());
        }
        return parser.getMZTabFile();
    }

}
