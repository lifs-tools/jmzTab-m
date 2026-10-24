/*
 * Copyright 2018 LIFS Tools Consortium.
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
package org.lifstools.mztab2.validation;

import org.lifstools.mztab2.cvmapping.CvParameterLookupService;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.ValidationMessage;
import org.lifstools.mztab2.model.ValidationMessage.MessageTypeEnum;
import org.lifstools.mztab2.test.utils.ClassPathFile;
import static org.lifstools.mztab2.test.utils.ClassPathFile.GCXGC_MS_EXAMPLE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.LIPIDOMICS_EXAMPLE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.MINIMAL_EXAMPLE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.MOUSELIVER_NEGATIVE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.MOUSELIVER_NEGATIVE_MZTAB_NULL_COLUNIT;
import static org.lifstools.mztab2.test.utils.ClassPathFile.MTBLS263;
import static org.lifstools.mztab2.test.utils.ClassPathFile.STANDARDMIX_NEGATIVE_EXPORTPOSITIONLEVEL;
import static org.lifstools.mztab2.test.utils.ClassPathFile.STANDARDMIX_NEGATIVE_EXPORTSPECIESLEVEL;
import static org.lifstools.mztab2.test.utils.ClassPathFile.STANDARDMIX_POSITIVE_EXPORTPOSITIONLEVEL;
import static org.lifstools.mztab2.test.utils.ClassPathFile.STANDARDMIX_POSITIVE_EXPORTSPECIESLEVEL;
import org.lifstools.mztab2.test.utils.ExtractClassPathFiles;
import static org.lifstools.mztab2.test.utils.ClassPathFile.XCMS_EXAMPLE;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Integration test for semantic validation.
 *
 * @author nilshoffmann
 */
@Slf4j
public class ExampleFilesValidationTestIT {

    public static final CvParameterLookupService LOOKUP_SERVICE = new CvParameterLookupService();

    @RegisterExtension
    static final ExtractClassPathFiles EXTRACT_FILES = new ExtractClassPathFiles(
        MTBLS263,
        MOUSELIVER_NEGATIVE,
        MOUSELIVER_NEGATIVE_MZTAB_NULL_COLUNIT,
        STANDARDMIX_NEGATIVE_EXPORTPOSITIONLEVEL,
        STANDARDMIX_NEGATIVE_EXPORTSPECIESLEVEL,
        STANDARDMIX_POSITIVE_EXPORTPOSITIONLEVEL,
        STANDARDMIX_POSITIVE_EXPORTSPECIESLEVEL,
        GCXGC_MS_EXAMPLE,
        LIPIDOMICS_EXAMPLE,
        MINIMAL_EXAMPLE,
        XCMS_EXAMPLE
    );

    static Stream<Arguments> data() {
        return Stream.of(
            // no evidence table: its three rules (ms_level, identification_method,
            // derivatized_form) don't apply
            Arguments.of(XCMS_EXAMPLE, MZTabErrorType.Level.Info, 2, 12),
            Arguments.of(LIPIDOMICS_EXAMPLE, MZTabErrorType.Level.Info, 1, 6),
            Arguments.of(MTBLS263, MZTabErrorType.Level.Info, 1, 11),
            Arguments.of(STANDARDMIX_NEGATIVE_EXPORTPOSITIONLEVEL, MZTabErrorType.Level.Info, 1, 5),
            Arguments.of(STANDARDMIX_NEGATIVE_EXPORTSPECIESLEVEL, MZTabErrorType.Level.Info, 1, 5),
            Arguments.of(STANDARDMIX_POSITIVE_EXPORTPOSITIONLEVEL, MZTabErrorType.Level.Info, 1, 5),
            Arguments.of(STANDARDMIX_POSITIVE_EXPORTSPECIESLEVEL, MZTabErrorType.Level.Info, 1, 5),
            Arguments.of(GCXGC_MS_EXAMPLE, MZTabErrorType.Level.Info, 1, 4),
            Arguments.of(GCXGC_MS_EXAMPLE, MZTabErrorType.Level.Warn, 0, 0),
            Arguments.of(LIPIDOMICS_EXAMPLE, MZTabErrorType.Level.Warn, 0, 0),
            Arguments.of(LIPIDOMICS_EXAMPLE, MZTabErrorType.Level.Error, 0, 0)
        );
    }

    @ParameterizedTest(name = "{index}: semantic validation of ''{0}'' on level ''{1}'' expecting ''{2}'' structural/logical errors and ''{3}'' cross check/semantic errors.")
    @MethodSource("data")
    public void testExamples(ClassPathFile resource, MZTabErrorType.Level validationLevel,
            int expectedStructuralLogicalErrors, int expectedCrossCheckSemanticErrors)
            throws MZTabException, JAXBException {
        testSemanticValidation(EXTRACT_FILES.getBaseDir(), resource,
            validationLevel, expectedStructuralLogicalErrors,
            expectedCrossCheckSemanticErrors);
    }

    List<ValidationMessage> testSemanticValidation(File baseDir,
        ClassPathFile resource,
        MZTabErrorType.Level level,
        Integer expectedErrors, Integer expectedSemanticErrors) throws MZTabException, JAXBException {
        try {
            MzTab mzTab = SemanticTestResources.parseResource(baseDir, resource.fileName(),
                level, expectedErrors);
            assertNotNull(mzTab);
            assertNotNull(mzTab.getMetadata());
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
                assertEquals((long) expectedSemanticErrors, (long) messages.size(),
                    String.format("Expected %d semantic errors for level %s, found %d! ValidationMessages: %s",
                        expectedSemanticErrors, level, messages.size(), messages));
            }
            return messages;
        } catch (URISyntaxException ex) {
            log.error("Failed with exception:", ex);
            fail(ex.getMessage());
        } catch (IOException | IndexOutOfBoundsException ex) {
            log.error("Failed with exception:", ex);
            fail(ex.getMessage());
        } catch (MZTabException | MZTabErrorOverflowException | JAXBException e) {
            log.error("Failed with exception:", e);
            throw e;
        }
        return Collections.emptyList();
    }
}
