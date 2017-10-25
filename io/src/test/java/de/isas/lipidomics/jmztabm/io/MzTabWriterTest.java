package de.isas.lipidomics.jmztabm.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
import static de.isas.lipidomics.jmztabm.io.MzTabWriter.SEP;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Parameter;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aopalliance.reflect.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class for MzTabWriter.
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabWriterTest {

    static MzTab createTestFile() {
    
        final MzTab mztabfile = new MzTab().metadata(
                new de.isas.mztab1_1.model.Metadata().
                        mzTabVersion("1.1.0").
                        mzTabID("ISAS_2017_M_11451").
                        title("A minimal test file").
                        description("A description of an mzTab file.").
                        addContactsItem(
                                new Contact().
                                        name("Nils Hoffmann").
                                        email("nils.hoffmann_at_isas.de").
                                        affiliation(
                                                "ISAS e.V. Dortmund, Germany")
                        ).
                        addMsrunItem(
                                new MsRun().
                                        location("file:///path/to/file1.mzML").
                                        format(
                                                new Parameter().
                                                        cvLabel("MS").
                                                        cvAccession("MS:1000584").
                                                        name("mzML file")
                                        ).
                                        idFormat(
                                                new Parameter().
                                                        cvLabel("MS").
                                                        cvAccession("MS:1001530").
                                                        name("mzML unique identifier")
                                        )
                        )
        );
	return mztabfile;
    }

    @Test
    void testWriteDefaultToString() {
        try(BufferedWriter bw = Files.newBufferedWriter(File.createTempFile("testWriteDefaultToString", ".txt").toPath(), Charset.forName("UTF-8"), StandardOpenOption.WRITE)) {
            bw.write(createTestFile().toString());
        } catch (IOException ex) {
            Logger.getLogger(MzTabWriterTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    @Test
    void testWriteJsonMapper() {
        try(BufferedWriter bw = Files.newBufferedWriter(File.createTempFile("testWriteJson", ".json").toPath(), Charset.forName("UTF-8"), StandardOpenOption.WRITE)) {
            ObjectMapper mapper = new ObjectMapper();
            bw.write(mapper.writeValueAsString(createTestFile()));
        } catch (IOException ex) {
            Logger.getLogger(MzTabWriterTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
    @Test
    void testWriteCsv() {
	MzTab mztabfile = createTestFile();
        CsvMapper mapper = new CsvMapper();
        CsvSchema metadataSchema = CsvSchema.builder().addColumn("PREFIX", CsvSchema.ColumnType.STRING).addColumn("KEY", CsvSchema.ColumnType.STRING).addColumn("VALUE", CsvSchema.ColumnType.STRING).build();
                metadataSchema.withAllowComments(true).
                withLineSeparator(SEP).
                withUseHeader(false).
                withArrayElementSeparator("|").
                withNullValue("null").
                withColumnSeparator('\t');
        CsvSchema schema = mapper.schemaFor(Metadata.class).
                withAllowComments(true).
                withLineSeparator(SEP).
                withUseHeader(false).
                withArrayElementSeparator("|").
                withNullValue("null").
                withColumnSeparator('\t');
        try {
            System.out.println(mapper.writer(metadataSchema).
                    writeValueAsString(mztabfile.getMetadata()));
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MzTabWriterTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    @Test
    void testCvParameterToString() {
        Parameter p = new Parameter().cvLabel("MS").
                cvAccession("MS:100179").
                name("made up for testing").
                value(null);
        String s = MzTabWriter.parameterToString(p);
        System.out.println(s);
        String expected = "[MS, MS:100179, made up for testing, ]";
        Assertions.assertEquals(expected, s);
        p.value("some value");
        s = MzTabWriter.parameterToString(p);
        System.out.println(s);
        expected = "[MS, MS:100179, made up for testing, some value]";
        Assertions.assertEquals(expected, s);
    }

    @Test
    void testUserParameterToString() {
        Parameter p = new Parameter().name("made up for testing").
                value("some arbitrary value");
        String s = MzTabWriter.parameterToString(p);
        System.out.println(s);
        String expected = "[, , made up for testing, some arbitrary value]";
        Assertions.assertEquals(expected, s);
    }

    @Test
    void testMtdParameterToMzTabLine() {
        Parameter p = new Parameter().cvLabel("MS").
                cvAccession("MS:100179").
                name("made up for testing").
                value(null);
        String s = MzTabWriter.parameterToString(p);
        String actual = MzTabWriter.mtdParameterToMzTabLine(1,
                "sample_processing", p);
        String expected = "MTD\tsample_processing[1]\t" + s + "\n\r";
        System.out.println(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testMtdParameterToMzTabLineException() {
        Parameter p = new Parameter().cvLabel("MS").
                cvAccession("MS:100179").
                name("made up for testing").
                value(null);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
        {
            String actual = MzTabWriter.mtdParameterToMzTabLine(0,
                    "sample_processing", p);
        });
    }
}
