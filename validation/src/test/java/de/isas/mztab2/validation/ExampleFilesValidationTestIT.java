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
import de.isas.mztab2.model.ValidationMessage.MessageTypeEnum;
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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
@Slf4j
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

    @Parameters(
        name = "{index}: semantic validation of ''{0}'' on level ''{1}'' expecting ''{2}'' structural/logical errors and ''{3}'' cross check/semantic errors.")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {LIPIDOMICS_EXAMPLE, MZTabErrorType.Level.Info,
                0, 8},
            {MTBLS263, MZTabErrorType.Level.Info, 0, 15},
            //            {MOUSELIVER_NEGATIVE, MZTabErrorType.Level.Info, 0, 1},
            //            {MOUSELIVER_NEGATIVE_MZTAB_NULL_COLUNIT,
            //                MZTabErrorType.Level.Info, 1, 3},
            {STANDARDMIX_NEGATIVE_EXPORTPOSITIONLEVEL,
                MZTabErrorType.Level.Info, 0, 6},
            {STANDARDMIX_NEGATIVE_EXPORTSPECIESLEVEL,
                MZTabErrorType.Level.Info, 0, 6},
            {STANDARDMIX_POSITIVE_EXPORTPOSITIONLEVEL,
                MZTabErrorType.Level.Info, 0, 6},
            {STANDARDMIX_POSITIVE_EXPORTSPECIESLEVEL,
                MZTabErrorType.Level.Info, 0, 6},
            {GCXGC_MS_EXAMPLE, MZTabErrorType.Level.Info, 0, 5},
            {GCXGC_MS_EXAMPLE, MZTabErrorType.Level.Warn, 0, 0},
            {LIPIDOMICS_EXAMPLE, MZTabErrorType.Level.Warn, 0, 0},
            {LIPIDOMICS_EXAMPLE, MZTabErrorType.Level.Error, 0, 0},
        });
    }

    @Parameter(0)
    public ClassPathFile resource;
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
        ClassPathFile resource,
        MZTabErrorType.Level level,
        Integer expectedErrors, Integer expectedSemanticErrors) throws MZTabException, JAXBException {
        try {
            MzTab mzTab = SemanticTestResources.parseResource(tf, resource.
                fileName(),
                level, expectedErrors);
            Assert.assertNotNull(mzTab);
            Assert.assertNotNull(mzTab.getMetadata());
            CvMappingValidator validator = CvMappingValidator.of(
                ExampleFilesValidationTestIT.class.getResource(
                    "/mappings/mzTab-M-mapping.xml"), LOOKUP_SERVICE,
                true);
            List<ValidationMessage> messages = validator.validate(mzTab);
            Map<MessageTypeEnum, List<ValidationMessage>> categorizedMessages = 
                    messages.stream().collect(Collectors.groupingBy(ValidationMessage::getMessageType));
            log.debug("CategorizedMessages: {}", categorizedMessages);
            MessageTypeEnum mt = MessageTypeEnum.fromValue(level.toString().toLowerCase());
            if (Optional.ofNullable(categorizedMessages.get(mt)).orElse(Collections.emptyList()).size() != expectedSemanticErrors) {
                Assert.assertEquals(String.format(
                    "Expected %d semantic errors for level %s, found %d! ValidationMessages: %s",
                    expectedSemanticErrors, level, messages.size(), messages),
                    (long) expectedSemanticErrors, (long) messages.size());
            }
            return messages;
        } catch (URISyntaxException ex) {
            log.error("Failed with exception:", ex);
            Assert.fail(ex.getMessage());
        } catch (IOException | IndexOutOfBoundsException ex) {
            log.error("Failed with exception:", ex);
            Assert.fail(ex.getMessage());
        } catch (MZTabException | MZTabErrorOverflowException | JAXBException e) {
            log.error("Failed with exception:", e);
            throw e;
        }
        return Collections.emptyList();
    }
}
