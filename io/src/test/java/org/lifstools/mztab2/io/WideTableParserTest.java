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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.lifstools.mztab2.io.WideTableFixture.*;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.OptColumnMapping;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import org.lifstools.mztab2.model.SmallMoleculeSummary;

/**
 * Regression tests for lifs-tools/jmzTab-m#187: dynamic columns beyond
 * physical position 99 must keep unique positions and their values.
 */
public class WideTableParserTest {

    private static final double DELTA = 1e-9;

    @TempDir
    static Path tempDir;

    private static MzTab mzTab;

    @BeforeAll
    static void parseWideTables() throws IOException {
        Path file = WideTableFixture.write(tempDir.resolve("wide-tables.mztab"));
        mzTab = ColumnStructureAssertions.parseWithoutColumnErrors(file);
    }

    @Test
    public void smlKeepsAssayStudyVariableAndOptValues() {
        assertEquals(ROWS, mzTab.getSmallMoleculeSummary().size());
        for (int r = 1; r <= ROWS; r++) {
            final int row = r;
            SmallMoleculeSummary sml = mzTab.getSmallMoleculeSummary().stream()
                .filter(s -> s.getSmlId() == row).findFirst().orElseThrow();
            assertEquals(ASSAYS, sml.getAbundanceAssay().size());
            for (int a = 1; a <= ASSAYS; a++) {
                assertEquals(assayAbundance(r, a), sml.getAbundanceAssay().get(a - 1), DELTA);
            }
            assertEquals(STUDY_VARIABLES, sml.getAbundanceStudyVariable().size());
            assertEquals(STUDY_VARIABLES, sml.getAbundanceVariationStudyVariable().size());
            for (int j = 1; j <= STUDY_VARIABLES; j++) {
                assertEquals(studyVariableAbundance(r, j), sml.getAbundanceStudyVariable().get(j - 1), DELTA);
                assertEquals(studyVariableVariation(r, j), sml.getAbundanceVariationStudyVariable().get(j - 1), DELTA);
            }
            assertOpt(r, SML_OPT, sml.getOpt());
        }
    }

    @Test
    public void smfKeepsAssayAndOptValuesAfterManyAssays() {
        assertEquals(ROWS, mzTab.getSmallMoleculeFeature().size());
        for (int r = 1; r <= ROWS; r++) {
            final int row = r;
            SmallMoleculeFeature smf = mzTab.getSmallMoleculeFeature().stream()
                .filter(s -> s.getSmfId() == row).findFirst().orElseThrow();
            assertEquals(ASSAYS, smf.getAbundanceAssay().size());
            for (int a = 1; a <= ASSAYS; a++) {
                assertEquals(assayAbundance(r, a), smf.getAbundanceAssay().get(a - 1), DELTA);
            }
            assertOpt(r, SMF_OPT, smf.getOpt());
        }
    }

    @Test
    public void smeKeepsIdConfidenceRankAndOptValues() {
        assertEquals(ROWS, mzTab.getSmallMoleculeEvidence().size());
        for (int r = 1; r <= ROWS; r++) {
            final int row = r;
            SmallMoleculeEvidence sme = mzTab.getSmallMoleculeEvidence().stream()
                .filter(s -> s.getSmeId() == row).findFirst().orElseThrow();
            assertEquals(ID_CONFIDENCE_MEASURES, sme.getIdConfidenceMeasure().size());
            for (int k = 1; k <= ID_CONFIDENCE_MEASURES; k++) {
                assertEquals(idConfidence(r, k), sme.getIdConfidenceMeasure().get(k - 1), DELTA);
            }
            assertEquals(1, sme.getRank());
            assertOpt(r, SME_OPT, sme.getOpt());
        }
    }

    private static void assertOpt(int row, List<String> headers, List<OptColumnMapping> opt) {
        assertEquals(headers.size(), opt.size(), () -> "opt columns in row " + row + ": " + opt);
        for (int i = 0; i < headers.size(); i++) {
            assertEquals(headers.get(i).substring("opt_".length()), opt.get(i).getIdentifier());
            assertEquals(optValue(headers.get(i), row), opt.get(i).getValue());
        }
    }
}
