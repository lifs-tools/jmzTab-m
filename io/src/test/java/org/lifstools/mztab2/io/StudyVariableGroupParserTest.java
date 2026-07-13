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
import java.util.List;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.StudyVariableGroup;
import static org.lifstools.mztab2.test.utils.ClassPathFile.STUDY_VARIABLE_GROUP;
import static org.lifstools.mztab2.test.utils.ClassPathFile.STUDY_VARIABLE_GROUP_OFFICIAL_EXAMPLE;
import org.lifstools.mztab2.test.utils.ExtractClassPathFiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * End-to-end tests for parsing and validation of the study_variable_group
 * section using the official mzTab-M 2.1 design, where each group lists its
 * member study variables via study_variable_group[n]-study_variable_refs.
 *
 * @author nilshoffmann
 */
public class StudyVariableGroupParserTest {

    @RegisterExtension
    static final ExtractClassPathFiles EXTRACT_FILES = new ExtractClassPathFiles(
            STUDY_VARIABLE_GROUP,
            STUDY_VARIABLE_GROUP_OFFICIAL_EXAMPLE);

    private MzTabFileParser parse(String fileName, MZTabErrorType.Level level) throws IOException {
        File testFile = new File(EXTRACT_FILES.getBaseDir(), fileName);
        Assertions.assertTrue(testFile.exists() && testFile.isFile());
        MzTabFileParser parser = new MzTabFileParser(testFile);
        parser.parse(System.err, level, 500);
        return parser;
    }

    @Test
    public void testStudyVariableGroupParsesAndValidates() throws Exception {
        MzTabFileParser parser = parse(STUDY_VARIABLE_GROUP.fileName(),
            MZTabErrorType.Level.Warn);
        Assertions.assertEquals(0, parser.getErrorList().size(),
            parser.getErrorList().toString());
        MzTab mzTab = parser.getMZTabFile();
        Assertions.assertNotNull(mzTab);

        Assertions.assertEquals(1, mzTab.getMetadata().getStudyVariableGroup().size());
        StudyVariableGroup group = mzTab.getMetadata().getStudyVariableGroup().get(0);
        Assertions.assertEquals("treatment", group.getParameter().getName());
        Assertions.assertEquals("STATO", group.getType().getCvLabel());
        Assertions.assertEquals("xsd:string", group.getDatatype());

        // study_variable_refs are resolved to the referenced study variables
        List<StudyVariable> refs = group.getStudyVariableRefs();
        Assertions.assertEquals(2, refs.size());
        Assertions.assertEquals(Integer.valueOf(1), refs.get(0).getId());
        Assertions.assertEquals("Untreated", refs.get(0).getName());
        Assertions.assertEquals(Integer.valueOf(2), refs.get(1).getId());
        Assertions.assertEquals("Treated", refs.get(1).getName());
    }

    @Test
    public void testOfficialExampleValidates() throws Exception {
        // The official example from the HUPO-PSI/mzTab-M examples/2.1 directory.
        // It uses the top-down study_variable_group[n]-study_variable_refs design
        // and declares study variables before the assays they reference, exercising
        // both the study_variable_refs and the deferred assay_refs resolution.
        MzTabFileParser parser = parse(STUDY_VARIABLE_GROUP_OFFICIAL_EXAMPLE.fileName(),
            MZTabErrorType.Level.Warn);
        Assertions.assertEquals(0, parser.getErrorList().size(),
            parser.getErrorList().toString());
        MzTab mzTab = parser.getMZTabFile();
        Assertions.assertNotNull(mzTab);

        Assertions.assertEquals(2, mzTab.getMetadata().getStudyVariableGroup().size());
        // sex group references study_variable[1] and [2]
        StudyVariableGroup sex = mzTab.getMetadata().getStudyVariableGroup().get(0);
        Assertions.assertEquals("sex", sex.getParameter().getName());
        Assertions.assertEquals(2, sex.getStudyVariableRefs().size());
        Assertions.assertEquals("Female", sex.getStudyVariableRefs().get(0).getName());
        // timepoint group references study_variable[3], [4] and [5]
        StudyVariableGroup timepoint = mzTab.getMetadata().getStudyVariableGroup().get(1);
        Assertions.assertEquals("timepoint", timepoint.getParameter().getName());
        Assertions.assertEquals(3, timepoint.getStudyVariableRefs().size());
    }
}
