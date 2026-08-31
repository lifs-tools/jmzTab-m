/*
 * Copyright 2024 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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

import java.io.File;
import java.io.IOException;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.MzTabProfile;
import org.lifstools.mztab2.test.utils.ClassPathFile;
import static org.lifstools.mztab2.test.utils.ClassPathFile.PROFILE_FORBIDDEN_TABLE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.PROFILE_LEGACY_MFE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.PROFILE_M;
import static org.lifstools.mztab2.test.utils.ClassPathFile.PROFILE_MF;
import static org.lifstools.mztab2.test.utils.ClassPathFile.PROFILE_MFE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.PROFILE_MS;
import static org.lifstools.mztab2.test.utils.ClassPathFile.PROFILE_MISSING_TABLE;
import org.lifstools.mztab2.test.utils.ExtractClassPathFiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Tests for the mzTab-M profile validation implemented by
 * {@link org.lifstools.mztab2.io.validators.MzTabProfileValidator} and wired
 * into {@link MzTabFileParser}.
 *
 * @author nilshoffmann
 */
public class MzTabProfileValidationTest {

    @RegisterExtension
    static final ExtractClassPathFiles EXTRACT_FILES = new ExtractClassPathFiles(
            PROFILE_M,
            PROFILE_MS,
            PROFILE_MF,
            PROFILE_MFE,
            PROFILE_LEGACY_MFE,
            PROFILE_FORBIDDEN_TABLE,
            PROFILE_MISSING_TABLE
    );

    private MzTabFileParser parse(ClassPathFile resource, MZTabErrorType.Level level) throws IOException {
        File testFile = new File(EXTRACT_FILES.getBaseDir(), resource.fileName());
        Assertions.assertTrue(testFile.exists() && testFile.isFile());
        MzTabFileParser parser = new MzTabFileParser(testFile);
        parser.parse(System.err, level, 500);
        return parser;
    }

    private boolean containsType(MZTabErrorList errorList, MZTabErrorType type) {
        for (int i = 0; i < errorList.size(); i++) {
            if (errorList.getError(i).getType() == type) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testProfileMOnly() throws Exception {
        MzTabFileParser parser = parse(PROFILE_M, MZTabErrorType.Level.Warn);
        Assertions.assertEquals(0, parser.getErrorList().size(),
                parser.getErrorList().toString());
        MzTab mzTab = parser.getMZTabFile();
        Assertions.assertNotNull(mzTab);
        Assertions.assertEquals(MzTabProfile.M, mzTab.getMetadata().getMzTabProfile());
    }

    @Test
    public void testProfileMS() throws Exception {
        MzTabFileParser parser = parse(PROFILE_MS, MZTabErrorType.Level.Warn);
        Assertions.assertEquals(0, parser.getErrorList().size(),
                parser.getErrorList().toString());
        MzTab mzTab = parser.getMZTabFile();
        Assertions.assertNotNull(mzTab);
        Assertions.assertEquals(MzTabProfile.M_S, mzTab.getMetadata().getMzTabProfile());
    }

    @Test
    public void testProfileMF() throws Exception {
        MzTabFileParser parser = parse(PROFILE_MF, MZTabErrorType.Level.Warn);
        Assertions.assertEquals(0, parser.getErrorList().size(),
                parser.getErrorList().toString());
        MzTab mzTab = parser.getMZTabFile();
        Assertions.assertNotNull(mzTab);
        Assertions.assertEquals(MzTabProfile.M_F, mzTab.getMetadata().getMzTabProfile());
    }

    @Test
    public void testProfileMFE() throws Exception {
        MzTabFileParser parser = parse(PROFILE_MFE, MZTabErrorType.Level.Warn);
        Assertions.assertEquals(0, parser.getErrorList().size(),
                parser.getErrorList().toString());
        MzTab mzTab = parser.getMZTabFile();
        Assertions.assertNotNull(mzTab);
        Assertions.assertEquals(MzTabProfile.M_F_E, mzTab.getMetadata().getMzTabProfile());
    }

    @Test
    public void testLegacyFileInfersProfile() throws Exception {
        // A file without an mzTab-profile line MUST NOT fail and MUST report its
        // inferred profile as an info message.
        MzTabFileParser parser = parse(PROFILE_LEGACY_MFE, MZTabErrorType.Level.Info);
        MZTabErrorList errorList = parser.getErrorList();
        Assertions.assertTrue(containsType(errorList, LogicalErrorType.ProfileInferred),
                errorList.toString());
        // The metadata profile is not set for a legacy file.
        Assertions.assertNotNull(parser.getMZTabFile());
        Assertions.assertNull(parser.getMZTabFile().getMetadata().getMzTabProfile());
        // At warn level, the inference info is filtered out and no error remains.
        MzTabFileParser warnParser = parse(PROFILE_LEGACY_MFE, MZTabErrorType.Level.Warn);
        Assertions.assertEquals(0, warnParser.getErrorList().size(),
                warnParser.getErrorList().toString());
    }

    @Test
    public void testForbiddenTableIsError() throws Exception {
        // Declares M+F but contains a small molecule summary section.
        MzTabFileParser parser = parse(PROFILE_FORBIDDEN_TABLE, MZTabErrorType.Level.Error);
        Assertions.assertTrue(containsType(parser.getErrorList(),
                LogicalErrorType.ProfileTableForbidden),
                parser.getErrorList().toString());
    }

    @Test
    public void testMissingTableIsError() throws Exception {
        // Declares M+S+F+E but is missing the small molecule evidence section.
        MzTabFileParser parser = parse(PROFILE_MISSING_TABLE, MZTabErrorType.Level.Error);
        Assertions.assertTrue(containsType(parser.getErrorList(),
                LogicalErrorType.ProfileTableMissing),
                parser.getErrorList().toString());
    }
}
