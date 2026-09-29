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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Generates an mzTab-M 2.0 file whose SML, SMF and SME sections each have
 * more than 99 columns, with dynamic columns placed where issue #187
 * reported failures:
 * <ul>
 * <li>SML: 13 stable, 120 assays, 3 x (study variable + variation), 5 opt.</li>
 * <li>SMF: 10 stable, 120 assays, 4 opt.</li>
 * <li>SME: 16 stable, 110 id_confidence_measure, rank, 2 opt.</li>
 * </ul>
 */
final class WideTableFixture {

    static final int ASSAYS = 120;
    static final int STUDY_VARIABLES = 3;
    static final int ID_CONFIDENCE_MEASURES = 110;
    static final int ROWS = 5;
    static final List<String> SML_OPT = List.of("opt_global_a", "opt_global_b",
        "opt_global_c", "opt_global_d", "opt_global_e");
    static final List<String> SMF_OPT = List.of("opt_global_featureId",
        "opt_global_CCS", "opt_global_mzmin", "opt_global_mzmax");
    static final List<String> SME_OPT = List.of("opt_global_score_a",
        "opt_global_score_b");

    private WideTableFixture() {
    }

    static double assayAbundance(int row, int assay) {
        return row * 1000 + assay + 0.5;
    }

    static double studyVariableAbundance(int row, int sv) {
        return row * 100 + sv + 0.25;
    }

    static double studyVariableVariation(int row, int sv) {
        return row * 100 + sv + 0.75;
    }

    static double idConfidence(int row, int k) {
        return row * 1000 + k + 0.125;
    }

    static String optValue(String header, int row) {
        return header + "_row" + row;
    }

    static Path write(Path file) throws IOException {
        List<String> lines = new ArrayList<>(metadata());
        lines.add("");
        lines.add(tsv(smlHeader()));
        for (int r = 1; r <= ROWS; r++) {
            lines.add(tsv(smlRow(r)));
        }
        lines.add("");
        lines.add(tsv(smfHeader()));
        for (int r = 1; r <= ROWS; r++) {
            lines.add(tsv(smfRow(r)));
        }
        lines.add("");
        lines.add(tsv(smeHeader()));
        for (int r = 1; r <= ROWS; r++) {
            lines.add(tsv(smeRow(r)));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
        return file;
    }

    private static String tsv(List<String> cells) {
        return String.join("\t", cells);
    }

    private static List<String> metadata() {
        List<String> m = new ArrayList<>(List.of(
            "MTD\tmzTab-version\t2.0.0-M",
            "MTD\tmzTab-ID\tWIDE-TABLES-187",
            "MTD\tdescription\tSynthetic file with more than 99 columns per table section (lifs-tools/jmzTab-m#187).",
            "MTD\tsoftware[1]\t[MS, MS:1001582, XCMS, 3.1.1]",
            "MTD\tquantification_method\t[MS, MS:1001834, LC-MS label-free quantitation analysis, ]"));
        for (int a = 1; a <= ASSAYS; a++) {
            m.add("MTD\tms_run[" + a + "]-location\tfile:///data/run" + a + ".mzML");
            m.add("MTD\tms_run[" + a + "]-scan_polarity[1]\t[MS, MS:1000130, positive scan, ]");
        }
        for (int a = 1; a <= ASSAYS; a++) {
            m.add("MTD\tassay[" + a + "]\tassay " + a);
            m.add("MTD\tassay[" + a + "]-ms_run_ref\tms_run[" + a + "]");
        }
        for (int j = 1; j <= STUDY_VARIABLES; j++) {
            final int sv = j;
            String refs = IntStream.rangeClosed(1, ASSAYS)
                .filter(a -> (a - 1) % STUDY_VARIABLES == sv - 1)
                .mapToObj(a -> "assay[" + a + "]")
                .collect(Collectors.joining(", "));
            m.add("MTD\tstudy_variable[" + j + "]\tgroup " + j);
            m.add("MTD\tstudy_variable[" + j + "]-description\tgroup " + j);
            m.add("MTD\tstudy_variable[" + j + "]-assay_refs\t" + refs);
        }
        m.addAll(List.of(
            "MTD\tcv[1]-label\tMS",
            "MTD\tcv[1]-full_name\tPSI-MS controlled vocabulary",
            "MTD\tcv[1]-version\t4.1.138",
            "MTD\tcv[1]-uri\thttps://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo",
            "MTD\tdatabase[1]\t[,, \"no database\", null ]",
            "MTD\tdatabase[1]-prefix\tnull",
            "MTD\tdatabase[1]-version\tUnknown",
            "MTD\tdatabase[1]-uri\tnull",
            "MTD\tsmall_molecule-quantification_unit\t[MS, MS:1002887, Progenesis QI normalised abundance, ]",
            "MTD\tsmall_molecule_feature-quantification_unit\t[MS, MS:1002887, Progenesis QI normalised abundance, ]",
            "MTD\tsmall_molecule-identification_reliability\t[MS, MS:1002896, compound identification confidence level, ]"));
        for (int k = 1; k <= ID_CONFIDENCE_MEASURES; k++) {
            m.add("MTD\tid_confidence_measure[" + k + "]\t[, , confidence measure " + k + ", ]");
        }
        return m;
    }

    private static List<String> smlHeader() {
        List<String> h = new ArrayList<>(List.of("SMH", "SML_ID", "SMF_ID_REFS",
            "database_identifier", "chemical_formula", "smiles", "inchi",
            "chemical_name", "uri", "theoretical_neutral_mass", "adduct_ions",
            "reliability", "best_id_confidence_measure", "best_id_confidence_value"));
        for (int a = 1; a <= ASSAYS; a++) {
            h.add("abundance_assay[" + a + "]");
        }
        for (int j = 1; j <= STUDY_VARIABLES; j++) {
            h.add("abundance_study_variable[" + j + "]");
            h.add("abundance_variation_study_variable[" + j + "]");
        }
        h.addAll(SML_OPT);
        return h;
    }

    private static List<String> smlRow(int r) {
        List<String> c = new ArrayList<>(List.of("SML", "" + r, "" + r, "null",
            "null", "null", "null", "null", "null", "null", "null", "2", "null",
            "null"));
        for (int a = 1; a <= ASSAYS; a++) {
            c.add(Double.toString(assayAbundance(r, a)));
        }
        for (int j = 1; j <= STUDY_VARIABLES; j++) {
            c.add(Double.toString(studyVariableAbundance(r, j)));
            c.add(Double.toString(studyVariableVariation(r, j)));
        }
        for (String opt : SML_OPT) {
            c.add(optValue(opt, r));
        }
        return c;
    }

    private static List<String> smfHeader() {
        List<String> h = new ArrayList<>(List.of("SFH", "SMF_ID", "SME_ID_REFS",
            "SME_ID_REF_ambiguity_code", "adduct_ion", "isotopomer",
            "exp_mass_to_charge", "charge", "retention_time_in_seconds",
            "retention_time_in_seconds_start", "retention_time_in_seconds_end"));
        for (int a = 1; a <= ASSAYS; a++) {
            h.add("abundance_assay[" + a + "]");
        }
        h.addAll(SMF_OPT);
        return h;
    }

    private static List<String> smfRow(int r) {
        List<String> c = new ArrayList<>(List.of("SMF", "" + r, "" + r, "null",
            "[M+H]1+", "null", "650.6432", "1", "821.2341", "756.0", "954.0"));
        for (int a = 1; a <= ASSAYS; a++) {
            c.add(Double.toString(assayAbundance(r, a)));
        }
        for (String opt : SMF_OPT) {
            c.add(optValue(opt, r));
        }
        return c;
    }

    private static List<String> smeHeader() {
        List<String> h = new ArrayList<>(List.of("SEH", "SME_ID",
            "evidence_input_id", "database_identifier", "chemical_formula",
            "smiles", "inchi", "chemical_name", "uri", "derivatized_form",
            "adduct_ion", "exp_mass_to_charge", "charge",
            "theoretical_mass_to_charge", "spectra_ref", "identification_method",
            "ms_level"));
        for (int k = 1; k <= ID_CONFIDENCE_MEASURES; k++) {
            h.add("id_confidence_measure[" + k + "]");
        }
        h.add("rank");
        h.addAll(SME_OPT);
        return h;
    }

    private static List<String> smeRow(int r) {
        List<String> c = new ArrayList<>(List.of("SME", "" + r, "" + r, "null",
            "null", "null", "null", "null", "null", "null", "[M+H]1+",
            "650.6432", "1", "650.6446", "ms_run[1]:scan=" + r,
            "[, , qualifier ions exact mass, ]", "[MS, MS:1000511, ms level, 1]"));
        for (int k = 1; k <= ID_CONFIDENCE_MEASURES; k++) {
            c.add(Double.toString(idConfidence(r, k)));
        }
        c.add("1");
        for (String opt : SME_OPT) {
            c.add(optValue(opt, r));
        }
        return c;
    }
}
