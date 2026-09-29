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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Regression tests for lifs-tools/jmzTab-m#187 covering two behaviours that
 * are specific to the {@code ColumnPosition}-based header parsing:
 * <ul>
 * <li>a duplicated column header in a table section is reported as the
 * logical error {@link LogicalErrorType#DuplicateColumnHeader};</li>
 * <li>stable columns are no longer required to appear before dynamic
 * (abundance/opt) columns in the physical header line.</li>
 * </ul>
 */
public class ColumnOrderRegressionTest {

    private static final double DELTA = 1e-9;

    @Test
    public void duplicateOptColumnHeaderIsReportedAsParserError(@TempDir Path tempDir) throws IOException {
        Path file = writeDuplicateOptHeaderFile(tempDir.resolve("duplicate-opt-header.mztab"));

        MzTabFileParser parser = new MzTabFileParser(file.toFile());
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        MZTabErrorList errors = parser.parse(log, MZTabErrorType.Level.Error, 1000);

        List<MZTabError> duplicateHeaderErrors = errors.getErrorList().stream()
            .filter(e -> e.getType() == LogicalErrorType.DuplicateColumnHeader)
            .toList();
        assertFalse(duplicateHeaderErrors.isEmpty(),
            () -> "Expected a DuplicateColumnHeader error, got: " + errors.getErrorList());
    }

    @Test
    public void stableColumnsAfterDynamicColumnsInSfhParseCorrectly(@TempDir Path tempDir) throws IOException {
        Path file = writeStableColumnsAfterDynamicColumnsFile(tempDir.resolve("stable-after-dynamic.mztab"));

        MzTab mzTab = ColumnStructureAssertions.parseWithoutColumnErrors(file);

        assertEquals(1, mzTab.getSmallMoleculeFeature().size());
        SmallMoleculeFeature smf = mzTab.getSmallMoleculeFeature().get(0);
        assertEquals(2, smf.getAbundanceAssay().size());
        assertEquals(111.5, smf.getAbundanceAssay().get(0), DELTA);
        assertEquals(222.5, smf.getAbundanceAssay().get(1), DELTA);
        assertEquals(1, smf.getOpt().size());
        assertEquals("global_x", smf.getOpt().get(0).getIdentifier());
        assertEquals("v_opt", smf.getOpt().get(0).getValue());
    }

    /**
     * A minimal file whose SMH header line contains {@code opt_global_x}
     * twice, which {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabHeaderLineParser}
     * should reject before any column semantics are checked.
     */
    private static Path writeDuplicateOptHeaderFile(Path file) throws IOException {
        List<String> lines = List.of(
            "MTD\tmzTab-version\t2.0.0-M",
            "MTD\tmzTab-ID\tDUPLICATE-HEADER-187",
            "",
            "SMH\tSML_ID\tSMF_ID_REFS\tdatabase_identifier\tchemical_formula\tsmiles\tinchi\t"
            + "chemical_name\turi\ttheoretical_neutral_mass\tadduct_ions\treliability\t"
            + "best_id_confidence_measure\tbest_id_confidence_value\topt_global_x\topt_global_x");
        Files.write(file, lines, StandardCharsets.UTF_8);
        return file;
    }

    /**
     * A minimal SMF section whose header places an {@code opt_} column
     * first, then {@code abundance_assay[..]} columns, and only then the
     * section's stable columns.
     */
    private static Path writeStableColumnsAfterDynamicColumnsFile(Path file) throws IOException {
        List<String> lines = List.of(
            "MTD\tmzTab-version\t2.0.0-M",
            "MTD\tmzTab-ID\tSTABLE-AFTER-DYNAMIC-187",
            "MTD\tsoftware[1]\t[MS, MS:1001582, XCMS, 3.1.1]",
            "MTD\tms_run[1]-location\tfile:///data/run1.mzML",
            "MTD\tms_run[1]-scan_polarity[1]\t[MS, MS:1000130, positive scan, ]",
            "MTD\tms_run[2]-location\tfile:///data/run2.mzML",
            "MTD\tms_run[2]-scan_polarity[1]\t[MS, MS:1000130, positive scan, ]",
            "MTD\tassay[1]\tassay 1",
            "MTD\tassay[1]-ms_run_ref\tms_run[1]",
            "MTD\tassay[2]\tassay 2",
            "MTD\tassay[2]-ms_run_ref\tms_run[2]",
            "MTD\tstudy_variable[1]\tgroup 1",
            "MTD\tstudy_variable[1]-description\tgroup 1",
            "MTD\tstudy_variable[1]-assay_refs\tassay[1]|assay[2]",
            "MTD\tcv[1]-label\tMS",
            "MTD\tcv[1]-full_name\tPSI-MS controlled vocabulary",
            "MTD\tcv[1]-version\t4.1.138",
            "MTD\tcv[1]-uri\thttps://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo",
            "MTD\tsmall_molecule_feature-quantification_unit\t[MS, MS:1002887, Progenesis QI normalised abundance, ]",
            "",
            "SFH\topt_global_x\tabundance_assay[1]\tabundance_assay[2]\tSMF_ID\tSME_ID_REFS\t"
            + "SME_ID_REF_ambiguity_code\tadduct_ion\tisotopomer\texp_mass_to_charge\tcharge\t"
            + "retention_time_in_seconds\tretention_time_in_seconds_start\tretention_time_in_seconds_end",
            "SMF\tv_opt\t111.5\t222.5\t1\t1\tnull\t[M+H]1+\tnull\t650.6432\t1\t821.2341\t756.0\t954.0");
        Files.write(file, lines, StandardCharsets.UTF_8);
        return file;
    }
}
