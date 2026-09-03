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
import static org.lifstools.mztab2.test.utils.ClassPathFile.OFFICIAL_PROFILE_M;
import static org.lifstools.mztab2.test.utils.ClassPathFile.OFFICIAL_PROFILE_MF;
import static org.lifstools.mztab2.test.utils.ClassPathFile.OFFICIAL_PROFILE_MFE;
import static org.lifstools.mztab2.test.utils.ClassPathFile.OFFICIAL_PROFILE_MS;
import org.lifstools.mztab2.test.utils.ExtractClassPathFiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Validates the reference mzTab-M 2.1 profile example files taken from the
 * HUPO-PSI/mzTab-M "Profiles specification" PR
 * (examples/2.1/pending-validator/): one file per profile, each declaring its
 * mzTab-profile.
 *
 * @author nilshoffmann
 */
public class MzTabProfileOfficialExamplesTest {

    @RegisterExtension
    static final ExtractClassPathFiles EXTRACT_FILES = new ExtractClassPathFiles(
            OFFICIAL_PROFILE_M,
            OFFICIAL_PROFILE_MS,
            OFFICIAL_PROFILE_MF,
            OFFICIAL_PROFILE_MFE);

    static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(OFFICIAL_PROFILE_M, MzTabProfile.M),
            Arguments.of(OFFICIAL_PROFILE_MS, MzTabProfile.M_S),
            Arguments.of(OFFICIAL_PROFILE_MF, MzTabProfile.M_F),
            Arguments.of(OFFICIAL_PROFILE_MFE, MzTabProfile.M_F_E)
        );
    }

    @ParameterizedTest(name = "{0} declares and validates as profile {1}")
    @MethodSource("data")
    public void testOfficialProfileExampleValidates(ClassPathFile resource,
        MzTabProfile expectedProfile) throws IOException {
        File testFile = new File(EXTRACT_FILES.getBaseDir(), resource.fileName());
        Assertions.assertTrue(testFile.exists() && testFile.isFile());
        MzTabFileParser parser = new MzTabFileParser(testFile);
        parser.parse(System.err, MZTabErrorType.Level.Warn, 500);
        Assertions.assertEquals(0, parser.getErrorList().size(),
            parser.getErrorList().toString());
        MzTab mzTab = parser.getMZTabFile();
        Assertions.assertNotNull(mzTab);
        Assertions.assertEquals(expectedProfile,
            mzTab.getMetadata().getMzTabProfile());
    }
}
