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
package de.isas.mztab2.validation;

import de.isas.mztab2.cvmapping.CvParameterLookupService;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.ValidationMessage;
import de.isas.mztab2.test.utils.ExtractClassPathFiles;
import de.isas.mztab2.test.utils.LogMethodName;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Integration test for semantic validation.
 *
 * @author nilshoffmann
 */
@RunWith(Parameterized.class)
public class ExampleFilesValidationTestIT {

    @Rule
    public LogMethodName methodNameLogger = new LogMethodName();

    public static final CvParameterLookupService LOOKUP_SERVICE = new CvParameterLookupService();

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

    @Parameters(
        name = "{index}: semantic validation of ''{0}'' on level ''{1}'' expecting ''{2}'' structural/logical errors and ''{3}'' cross check/semantic errors.")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"lipidomics-example.mzTab", MZTabErrorType.Level.Info,
                0, 7},
            {"MTBLS263.mztab", MZTabErrorType.Level.Info, 0, 15},
//            {"MouseLiver_negative_mztab.txt", MZTabErrorType.Level.Info, 0, 1},
//            {"MouseLiver_negative_mztab_null-colunit.txt",
//                MZTabErrorType.Level.Info, 1, 3},
            {"StandardMix_negative_exportPositionLevel.mztab.txt",
                MZTabErrorType.Level.Info, 0, 4},
            {"StandardMix_negative_exportSpeciesLevel.mztab.txt",
                MZTabErrorType.Level.Info, 0, 4},
            {"StandardMix_positive_exportPositionLevel.mztab.txt",
                MZTabErrorType.Level.Info, 0, 4},
            {"StandardMix_positive_exportSpeciesLevel.mztab.txt",
                MZTabErrorType.Level.Info, 0, 4},
            {"gcxgc-ms-example.mztab", MZTabErrorType.Level.Info, 0, 5},
            {"lipidomics-example.mzTab", MZTabErrorType.Level.Info, 0, 7}
        });
    }

    @Parameter(0)
    public String resource;
    @Parameter(1)
    public MZTabErrorType.Level validationLevel;
    @Parameter(2)
    public int expectedStructuralLogicalErrors;
    @Parameter(3)
    public int expectedCrossCheckSemanticErrors;

    @Test
    public void testExamples() throws MZTabException, JAXBException {
        testSemanticValidation(TF, resource,
            validationLevel, expectedStructuralLogicalErrors,
            expectedCrossCheckSemanticErrors);
    }

    List<ValidationMessage> testSemanticValidation(TemporaryFolder tf,
        String resource,
        MZTabErrorType.Level level,
        Integer expectedErrors, Integer expectedSemanticErrors) throws MZTabException, JAXBException {
        try {
            MzTab mzTab = SemanticTestResources.parseResource(tf, resource,
                level, expectedErrors);
            Assert.assertNotNull(mzTab);
            Assert.assertNotNull(mzTab.getMetadata());
            CvMappingValidator validator = CvMappingValidator.of(ExampleFilesValidationTestIT.class.getResource(
                    "/mappings/mzTab-M-mapping.xml"), LOOKUP_SERVICE, 
                true);
            List<ValidationMessage> messages = validator.validate(mzTab);
            if (messages.size() != expectedSemanticErrors) {
                Assert.assertEquals(String.format(
                    "Expected %d semantic errors, found %d! ValidationMessages: %s",
                    expectedSemanticErrors, messages.size(), messages),
                    (long) expectedSemanticErrors, (long) messages.size());
            }
            return messages;
        } catch (URISyntaxException ex) {
            Logger.getLogger(ExampleFilesValidationTestIT.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(ExampleFilesValidationTestIT.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(ExampleFilesValidationTestIT.class.getName()).
                log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        } catch (MZTabException e) {
            Logger.getLogger(ExampleFilesValidationTestIT.class.getName()).
                log(Level.SEVERE, null, e);
            throw e;
        } catch (MZTabErrorOverflowException e) {
            Logger.getLogger(ExampleFilesValidationTestIT.class.getName()).
                log(Level.SEVERE, null, e);
            throw e;
        } catch (JAXBException ex) {
            Logger.getLogger(ExampleFilesValidationTestIT.class.getName()).
                log(Level.SEVERE, null, ex);
            throw ex;
        }
        return Collections.emptyList();
    }
}
