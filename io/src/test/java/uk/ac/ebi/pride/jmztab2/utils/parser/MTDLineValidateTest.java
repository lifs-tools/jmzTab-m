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
package uk.ac.ebi.pride.jmztab2.utils.parser;

import uk.ac.ebi.pride.jmztab2.utils.parser.MTDLineParser;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;
import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.Sample;
import org.slf4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import de.isas.mztab2.test.utils.LogMethodName;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Test all parser error.
 *
 * @author qingwei
 * @since 09/09/13
 */
public class MTDLineValidateTest {

    @Rule
    public LogMethodName methodNameLogger = new LogMethodName();
    private static Logger LOGGER = LoggerFactory.getLogger(
        MTDLineValidateTest.class);

    private Metadata metadata;
    private MTDLineParser parser;
    private MZTabErrorList errorList;
    private MZTabParserContext context;

    @Before
    public void setUp() throws Exception {
        context = new MZTabParserContext();
        parser = new MTDLineParser(context);
        metadata = parser.getMetadata();
        metadata.setDescription("test...");
        errorList = new MZTabErrorList();
    }

    @Test
    public void testMZTabDescription() throws Exception {
        try {
            parser.parse(1, "MTD\tmzTab-ver\t1.0 rc5", errorList);
            assertTrue(false);
        } catch (MZTabException e) {
            assertTrue(e.getError().
                getType() == FormatErrorType.MTDDefineLabel);
            LOGGER.debug(e.getMessage());
        }

//        try {
//            parser.parse(1, "MTD\tmzTab-mode\tUnknow", errorList);
//            assertTrue(false);
//        } catch (MZTabException e) {
//            assertTrue(e.getError().getType() == FormatErrorType.MZTabMode);
//            LOGGER.debug(e.getMessage());
//        }
//
//        try {
//            parser.parse(1, "MTD\tmzTab-type\tUnknow", errorList);
//            assertTrue(false);
//        } catch (MZTabException e) {
//            assertTrue(e.getError().getType() == FormatErrorType.MZTabType);
//            LOGGER.debug(e.getMessage());
//        }
    }

    @Test
    public void testIndexElement() throws Exception {
        // param name can not set empty.
        parser.
            parse(1, "MTD\tsample_processing[1]\t[, SEP:00173, ,]", errorList);
        assertTrue(errorList.size() == 1);
        assertTrue(errorList.getError(0).
            getType() == FormatErrorType.ParamList);

        // second param name can not set empty.
        parser.parse(1,
            "MTD\tsample_processing[1]\t[SEP, SEP:00142, enzyme digestion, ]|[MS, MS:1001251, , ]",
            errorList);
        assertTrue(errorList.size() == 2);
        assertTrue(errorList.getError(1).
            getType() == FormatErrorType.ParamList);

        // split char error.
        parser.parse(1,
            "MTD\tsample_processing[1]\t[SEP, SEP:00142, enzyme digestion, ]/[MS, MS:1001251, Trypsin, ]",
            errorList);
        assertTrue(errorList.size() == 3);
        assertTrue(errorList.getError(2).
            getType() == FormatErrorType.ParamList);

        try {
            // split char error.
            parser.parse(1,
                "MTD\tsample_processing[x]\t[SEP, SEP:00142, enzyme digestion, ]",
                errorList);
            assertTrue(false);
        } catch (MZTabException e) {
            assertTrue(e.getError().
                getType() == LogicalErrorType.IdNumber);
            LOGGER.debug(e.getMessage());
        }

        // param error.
        try {
            parser.parse(1,
                "MTD\tinstrument[1]-analyzer[1]\t[MS, MS:1000291, ,]", errorList);
            assertTrue(false);
        } catch (MZTabException e) {
            assertTrue(e.getError().
                getType() == LogicalErrorType.NULL);
            LOGGER.debug(e.getMessage());
        }
        assertTrue(errorList.size() == 4);
        assertTrue(errorList.getError(3).
            getType() == FormatErrorType.Param);
    }

    @Test
    public void testPublication() throws Exception {

        // no error.
        parser.parse(1,
            "MTD\tpublication[1]\tpubmed:21063943|doi:10.1007/978-1-60761-987-1_6",
            errorList);
        assertTrue(errorList.size() == 0);

        // split char error.
        parser.parse(1,
            "MTD\tpublication[1]\tpubmed:21063943/doi:10.1007/978-1-60761-987-1_6",
            errorList);
        assertTrue(errorList.size() == 1);
        assertTrue(errorList.getError(0).
            getType() == FormatErrorType.Publication);

        // error publication item
        parser.parse(1, "MTD\tpublication[1]\tpub:21063943", errorList);
        assertTrue(errorList.size() == 2);
        assertTrue(errorList.getError(1).
            getType() == FormatErrorType.Publication);

        // split char error.
        parser.parse(1, "MTD\tpublication[1]\tdoi:21063943/pubmed:21063943",
            errorList);
        assertTrue(errorList.size() == 3);
        assertTrue(errorList.getError(2).
            getType() == FormatErrorType.Publication);
    }

    @Test
    public void testInvalidColUnitLabel() throws Exception {
        parser.parse(1,
            "MTD\tcolunit-unknown\tretention_time=[UO,UO:0000031, minute,]",
            errorList);
        assertTrue(errorList.getError(0).
            getType() == FormatErrorType.MTDDefineLabel);
        LOGGER.debug(errorList.getError(0).
            getMessage());
    }

//    @Test
//    public void testNoColumnForColUnit() throws Exception {
//        parser.parse(1, "MTD\tprotein_search_engine_score[1]\t[MS, MS:1001171, Mascot:score,]", errorList);
//        parser.parse(1, "MTD\tcolunit-protein\tretention_time=[UO,UO:0000031, minute,]", errorList);
//
//        PRHLineParser prtParser = new PRHLineParser(metadata);
//        String headerLine = "PRH\t" +
//                "accession\t" +
//                "description\t" +
//                "taxid\t" +
//                "species\t" +
//                "database\t" +
//                "database_version\t" +
//                "search_engine\t" +
//                "best_search_engine_score[1]\t" +
//                "ambiguity_members\t" +
//                "modifications\t" +
//                "uri\t" +
//                "go_terms\t" +
//                "protein_coverage";
//
//        prtParser.parse(1, headerLine, errorList);
//        assertTrue(errorList.getError(0).getType() == FormatErrorType.ColUnit);
//        LOGGER.debug(errorList.getError(0).getMessage());
//    }
//    @Test
//    public void testColUnitParamParseError() throws Exception {
//        parser.parse(1, "MTD\tpeptide_search_engine_score[1]\t[MS, MS:1001171, Mascot:score,]", errorList);
//        parser.parse(1, "MTD\tcolunit-peptide\tretention_time=[UO,UO:0000031, ,]", errorList);
//        PEHLineParser pehParser = new PEHLineParser(metadata);
//        String headerLine = "PEH\t" +
//                "sequence\t" +
//                "accession\t" +
//                "unique\t" +
//                "database\t" +
//                "database_version\t" +
//                "search_engine\t" +
//                "best_search_engine_score[1]\t" +
//                "reliability\t" +
//                "modifications\t" +
//                "retention_time\t" +
//                "retention_time_window\t" +
//                "charge\t" +
//                "mass_to_charge\t" +
//                "uri\t" +
//                "spectra_ref";
//        pehParser.parse(1, headerLine, errorList);
//        assertTrue(errorList.getError(0).getType() == FormatErrorType.Param);
//        LOGGER.debug(errorList.getError(0).getMessage());
//    }
//    @Test
//    public void testDuplicationDefine() throws Exception {
//        parser.parse(1, "MTD\tprotein-quantification_unit\t[PRIDE, PRIDE:0000395, Ratio, ]", errorList);
//        assertTrue(true);
//
//        try {
//            // not allow duplicate twice.
//            parser.parse(1, "MTD\tprotein-quantification_unit\t[PRIDE, PRIDE:0000395, Ratio, ]", errorList);
//            assertTrue(false);
//        } catch (MZTabException e) {
//            assertTrue(e.getError().getType() == LogicalErrorType.DuplicationDefine);
//            LOGGER.debug(e.getMessage());
//        }
//    }
    @Test
    public void testDuplicationID() throws Exception {
        parser.parse(1,
            "MTD\tstudy_variable[1]-description\tdescription Group B (spike-in 0.74 fmol/uL)",
            errorList);
        assertTrue(errorList.isEmpty());

        Sample sample1 = new Sample();
        sample1.id(1);
        Sample sample2 = new Sample();
        sample2.id(2);
        context.addSample(metadata, sample1);
        context.addSample(metadata, sample2);
        Assay assay1 = new Assay();
        assay1.id(1);
        Assay assay2 = new Assay();
        assay2.id(2);
        context.addAssay(metadata, assay1);
        context.addAssay(metadata, assay2);

        parser.parse(1, "MTD\tstudy_variable[1]-assay_refs\tassay[1], assay[2]",
            errorList);
        assertTrue(errorList.isEmpty());

        parser.parse(1, "MTD\tstudy_variable[1]-assay_refs\tassay[1], assay[1]",
            errorList);
        assertTrue(errorList.getError(0).
            getType() == LogicalErrorType.DuplicationID);
    }

    @Test
    public void testNullMsRun() throws Exception {

        MZTabErrorList errorList = new MZTabErrorList(MZTabErrorType.Level.Warn);

        parser.parse(1, "MTD\tms_run[1]-location\tnull\n", errorList);
        assertTrue(!errorList.isEmpty());
        assertEquals(errorList.size(), 1);
        assertEquals(LogicalErrorType.NotNULL, errorList.getError(0).
            getType());

    }
}
