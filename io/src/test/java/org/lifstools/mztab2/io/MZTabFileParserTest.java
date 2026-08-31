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
package org.lifstools.mztab2.io;

import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.test.utils.ClassPathFile;
import static org.lifstools.mztab2.test.utils.ClassPathFile.GCXGC_MS_EXAMPLE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.LIPIDOMICS_EXAMPLE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.LIPIDOMICS_EXAMPLE_WRONG_MSSCAN_REF;
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
import static org.lifstools.mztab2.test.utils.ClassPathFile.XCMS_NO_SML_EXAMPLE;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Tests for MZTabFileParser
 *
 * @author nilshoffmann
 */
public class MZTabFileParserTest {

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
            LIPIDOMICS_EXAMPLE_WRONG_MSSCAN_REF,
            MINIMAL_EXAMPLE,
            XCMS_EXAMPLE,
            XCMS_NO_SML_EXAMPLE
    );

    static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(MTBLS263, MZTabErrorType.Level.Warn, 0, false),
            Arguments.of(MOUSELIVER_NEGATIVE, MZTabErrorType.Level.Warn, 0, false),
            Arguments.of(MOUSELIVER_NEGATIVE_MZTAB_NULL_COLUNIT, MZTabErrorType.Level.Error, 1, false),
            Arguments.of(STANDARDMIX_NEGATIVE_EXPORTPOSITIONLEVEL, MZTabErrorType.Level.Warn, 0, false),
            Arguments.of(STANDARDMIX_NEGATIVE_EXPORTSPECIESLEVEL, MZTabErrorType.Level.Warn, 0, false),
            Arguments.of(STANDARDMIX_POSITIVE_EXPORTPOSITIONLEVEL, MZTabErrorType.Level.Warn, 0, false),
            Arguments.of(STANDARDMIX_POSITIVE_EXPORTSPECIESLEVEL, MZTabErrorType.Level.Warn, 0, false),
            Arguments.of(LIPIDOMICS_EXAMPLE, MZTabErrorType.Level.Warn, 0, false),
            Arguments.of(LIPIDOMICS_EXAMPLE_WRONG_MSSCAN_REF, MZTabErrorType.Level.Error, 1, true),
            Arguments.of(GCXGC_MS_EXAMPLE, MZTabErrorType.Level.Warn, 0, false),
            Arguments.of(MINIMAL_EXAMPLE, MZTabErrorType.Level.Error, 0, false),
            Arguments.of(MINIMAL_EXAMPLE, MZTabErrorType.Level.Warn, 0, false),
            // MTD-only file: inferred profile M (1 info) plus 5 profile/content
            // mismatch infos for the conditional metadata fields present without
            // their related tables.
            Arguments.of(MINIMAL_EXAMPLE, MZTabErrorType.Level.Info, 6, false),
            // xcms-test-export is an M+S+F file (summary + feature, no evidence):
            // 1 discouraged-combination warning plus 1 database mismatch info.
            Arguments.of(XCMS_EXAMPLE, MZTabErrorType.Level.Info, 2, false),
            Arguments.of(XCMS_NO_SML_EXAMPLE, MZTabErrorType.Level.Warn, 0, false)
        );
    }

    @ParameterizedTest(name = "{index}: semantic validation of ''{0}'' on level ''{1}'' expecting ''{2}'' structural/logical errors and MzTab to be null: ''{3}''")
    @MethodSource("data")
    public void testExamples(ClassPathFile resource, MZTabErrorType.Level validationLevel,
            int expectedStructuralLogicalErrors, boolean mzTabMustBeNull)
            throws MZTabException, JAXBException {
        testExample(EXTRACT_FILES.getBaseDir(), resource,
                validationLevel, expectedStructuralLogicalErrors, mzTabMustBeNull);
    }

    void testExample(File tf, ClassPathFile resource,
            MZTabErrorType.Level level,
            Integer expectedErrors,
            boolean mzTabMustBeNull) throws MZTabException {
        System.out.println("Testing example: " + resource.fileName());
        try {
            MzTab mzTab = TestResources.parseResource(tf, resource.fileName(), level,
                    expectedErrors, mzTabMustBeNull);
            if (mzTabMustBeNull) {
                assertNull(mzTab);
                System.out.println("Example was null");
            } else {
                assertNotNull(mzTab);
                assertNotNull(mzTab.getMetadata());
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
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                    log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        } catch (IOException | IndexOutOfBoundsException ex) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                    log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        } catch (MZTabException | MZTabErrorOverflowException e) {
            Logger.getLogger(MZTabFileParserTest.class.getName()).
                    log(Level.SEVERE, null, e);
            throw e;
        }
    }

}
