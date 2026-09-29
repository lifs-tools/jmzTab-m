/*
 * Copyright 2026 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.OptColumnMapping;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import static org.lifstools.mztab2.test.utils.ClassPathFile.XCMS_MSIO_MTBLS4381_ONLY_SMF_TRIMMED;
import org.lifstools.mztab2.test.utils.ExtractClassPathFiles;

/**
 * lifs-tools/jmzTab-m#187: "Key 1000000 for column opt_global_mzmax is
 * already assigned to: opt_global_mzmin".
 */
public class Issue187XcmsExampleTest {

    @RegisterExtension
    static final ExtractClassPathFiles EXTRACT_FILES = new ExtractClassPathFiles(
        XCMS_MSIO_MTBLS4381_ONLY_SMF_TRIMMED);

    @Test
    public void parsesOptColumnsAfter213AssayColumns() throws IOException {
        File file = new File(EXTRACT_FILES.getBaseDir(), XCMS_MSIO_MTBLS4381_ONLY_SMF_TRIMMED.fileName());
        MzTab mzTab = ColumnStructureAssertions.parseWithoutColumnErrors(file.toPath(), true);
        assertEquals(25, mzTab.getSmallMoleculeFeature().size());

        String[] firstRow;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            firstRow = lines.filter(l -> l.startsWith("SMF\t")).findFirst().orElseThrow().split("\t");
        }
        int smfId = Integer.parseInt(firstRow[1].trim());
        SmallMoleculeFeature smf = mzTab.getSmallMoleculeFeature().stream()
            .filter(s -> s.getSmfId() == smfId).findFirst().orElseThrow();
        assertEquals(213, smf.getAbundanceAssay().size());
        List<OptColumnMapping> opt = smf.getOpt();
        assertEquals(2, opt.size());
        assertEquals("global_mzmin", opt.get(0).getIdentifier());
        assertEquals(firstRow[firstRow.length - 2].trim(), opt.get(0).getValue());
        assertEquals("global_mzmax", opt.get(1).getIdentifier());
        assertEquals(firstRow[firstRow.length - 1].trim(), opt.get(1).getValue());
    }
}
