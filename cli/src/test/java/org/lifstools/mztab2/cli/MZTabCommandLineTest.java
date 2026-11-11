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
package org.lifstools.mztab2.cli;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lifstools.mztab2.cvmapping.local.ResolvedVocabulary;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MzTab;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author nilshoffmann
 */
public class MZTabCommandLineTest {

    /**
     * JSON written by jmztabm-cli 1.0.6 (--toJson) from
     * test-utils/src/main/resources/metabolomics/minimal-m-2.0.mztab.
     * References between metadata elements are written as integer ids, e.g.
     * "sample_ref": 1.
     */
    private static final String JSON_1_0_6 = "/json/minimal-m-2.0-jmztabm-1.0.6.json";

    @TempDir
    Path tempDir;

    @Test
    public void testReadsIntegerReferencesWrittenBy106() throws IOException {
        MzTab mzTab = read106Json();
        Metadata metadata = mzTab.getMetadata();
        Assay assay = metadata.getAssay().get(0);
        // ids resolve to the objects defined in the metadata, not to copies
        assertSame(metadata.getSample().get(0), assay.getSampleRef());
        assertSame(metadata.getMsRun().get(0), assay.getMsRunRef().get(0));
        assertSame(assay, metadata.getStudyVariable().get(0).getAssayRefs().get(0));
    }

    @Test
    public void testWritesReferencesAsIntegerIds() throws IOException {
        JsonNode metadata = MZTabCommandLine.createJsonMapper().
            valueToTree(read106Json()).get("metadata");
        JsonNode assay = metadata.get("assay").get(0);
        assertTrue(assay.get("sample_ref").isInt(), "sample_ref should be written as id: " + assay);
        assertEquals(1, assay.get("sample_ref").asInt());
        assertEquals(1, assay.get("ms_run_ref").get(0).asInt());
        assertEquals(1, metadata.get("study_variable").get(0).get("assay_refs").get(0).asInt());
    }

    @Test
    public void testFromJsonConvertsIntegerReferences() throws Exception {
        File json = tempDir.resolve("minimal.json").toFile();
        try (InputStream is = getClass().getResourceAsStream(JSON_1_0_6)) {
            Files.copy(is, json.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        validateFromJson(json);
        File converted = new File(json.getParentFile(), json.getName() + ".mztab");
        assertTrue(converted.isFile(), "JSON was not converted to mzTab");
        String content = Files.readString(converted.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("assay[1]-sample_ref\tsample[1]"), content);
        assertTrue(content.contains("assay[1]-ms_run_ref\tms_run[1]"), content);
    }

    @Test
    public void testFromJsonFailsOnUnreadableJsonWithoutValidatingItAsText() throws Exception {
        // scan_polarity must be a list of parameters, not a string
        File json = tempDir.resolve("unreadable.json").toFile();
        Files.writeString(json.toPath(),
            "{\"metadata\": {\"ms_run\": [{\"id\": 1, \"scan_polarity\": \"POSITIVE scan\"}]}}",
            StandardCharsets.UTF_8);
        assertTrue(validateFromJson(json), "Unreadable JSON must be reported as failure");
        File converted = new File(json.getParentFile(), json.getName() + ".mztab");
        assertFalse(converted.exists(), "No mzTab file should be written for unreadable JSON");
    }

    @Test
    public void testFromJsonFailsOnJsonNullWithAMessage() throws Exception {
        // --toJson writes "null" for a file that it could not parse
        File json = tempDir.resolve("null.json").toFile();
        Files.writeString(json.toPath(), "null", StandardCharsets.UTF_8);
        assertTrue(validateFromJson(json), "JSON without an mzTab-M object must be reported as failure");
        File converted = new File(json.getParentFile(), json.getName() + ".mztab");
        assertFalse(converted.exists(), "No mzTab file should be written for JSON null");
    }

    private MzTab read106Json() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(JSON_1_0_6)) {
            assertNotNull(is, "Missing test resource " + JSON_1_0_6);
            return MZTabCommandLine.createJsonMapper().readValue(is, MzTab.class);
        }
    }

    private boolean validateFromJson(File json) throws Exception {
        Options options = new Options();
        String checkOpt = MZTabCommandLine.addCheckOption(options);
        String checkSemanticOpt = MZTabCommandLine.addCheckSemanticOption(options);
        CommandLine line = new DefaultParser().parse(options,
            new String[]{"-" + checkOpt, json.getAbsolutePath()});
        return MZTabCommandLine.handleValidation(line, checkOpt, System.out,
            MZTabErrorType.Level.Info, checkSemanticOpt, false, true);
    }

    // one MUST rule that is evaluated offline: no sample declares a disease
    private static final String MAPPING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<CvMapping modelName=\"test\" modelURI=\"https://example.org/model\" modelVersion=\"2.0.0\">\n"
        + "  <CvReferenceList><CvReference cvName=\"DOID\" cvIdentifier=\"DOID\"/></CvReferenceList>\n"
        + "  <CvMappingRuleList>\n"
        + "    <CvMappingRule id=\"sample_disease_must\" cvElementPath=\"/metadata/sample/@disease\" requirementLevel=\"MUST\""
        + " scopePath=\"/metadata/sample\" cvTermsCombinationLogic=\"OR\">\n"
        + "      <CvTerm termAccession=\"DOID:4\" useTerm=\"false\" termName=\"disease\" isRepeatable=\"true\" allowChildren=\"true\" cvIdentifierRef=\"DOID\"/>\n"
        + "    </CvMappingRule>\n"
        + "  </CvMappingRuleList>\n"
        + "</CvMapping>\n";

    private File resource(String name) throws IOException {
        File file = tempDir.resolve(name.substring(name.lastIndexOf('/') + 1)).toFile();
        try (InputStream in = getClass().getResourceAsStream(name)) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return file;
    }

    /** Runs a validation with -c and -s, returns whether it failed and what it printed. */
    private boolean validateSemantics(File file, String mapping, ByteArrayOutputStream bytes) throws Exception {
        Options options = new Options();
        String checkOpt = MZTabCommandLine.addCheckOption(options);
        String checkSemanticOpt = MZTabCommandLine.addCheckSemanticOption(options);
        CvLookupOptions.addTo(options);
        CommandLine line = new DefaultParser().parse(options,
            new String[]{"-" + checkOpt, file.getAbsolutePath(), "-" + checkSemanticOpt, mapping,
                "--cvOffline", "--cvCache", tempDir.resolve("cv-cache").toString()});
        return MZTabCommandLine.handleValidation(line, checkOpt,
            new PrintStream(bytes, true, "UTF-8"), MZTabErrorType.Level.Info, checkSemanticOpt, false, false);
    }

    @Test
    public void semanticValidationAlsoRunsAfterStructuralWarnings() throws Exception {
        // an M+S+F file: Warn-2056 from the structural validation
        File file = resource("/metabolomics/xcms-test-export.mztab");
        Path mapping = Files.writeString(tempDir.resolve("mapping.xml"), MAPPING);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        assertTrue(validateSemantics(file, mapping.toString(), bytes));
        String output = bytes.toString("UTF-8");
        assertTrue(output.contains("[Warn-2056]"), output);
        assertTrue(output.contains("sample_disease_must"), "semantic validation must run: " + output);
    }

    @Test
    public void missingMappingFileFailsWithoutAnException() throws Exception {
        File file = resource("/metabolomics/lipidomics-example.mzTab");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        assertTrue(validateSemantics(file, tempDir.resolve("no-such-mapping.xml").toString(), bytes));
    }

    @Test
    public void invalidMappingFileFailsWithoutAnException() throws Exception {
        File file = resource("/metabolomics/lipidomics-example.mzTab");
        Path mapping = Files.writeString(tempDir.resolve("mapping.xml"), "not a mapping");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        assertTrue(validateSemantics(file, mapping.toString(), bytes));
    }

    @Test
    public void semanticValidationIsSkippedForAFileWithoutModel() throws Exception {
        // a data line without its header line: no model
        File file = resource("/metabolomics/lipidomics-example.mzTab");
        Files.writeString(file.toPath(), "SML\t1\n", java.nio.file.StandardOpenOption.APPEND);
        Path mapping = Files.writeString(tempDir.resolve("mapping.xml"), MAPPING);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        assertTrue(validateSemantics(file, mapping.toString(), bytes));
        assertFalse(bytes.toString("UTF-8").contains("sample_disease_must"));
    }

    // ---- the local CV lookup (--cvSource, --cvCache, --cvResources, --cvOffline, --cvForce) ----

    /** One MAY rule on the instrument sources, all in PSI-MS: only MS is looked up. */
    private static final String MS_MAPPING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<CvMapping modelName=\"test\" modelURI=\"https://example.org/model\" modelVersion=\"2.0.0\">\n"
        + "  <CvReferenceList><CvReference cvName=\"MS\" cvIdentifier=\"MS\"/></CvReferenceList>\n"
        + "  <CvMappingRuleList>\n"
        + "    <CvMappingRule id=\"instrument_source_may\" cvElementPath=\"/metadata/instrument/@source\" requirementLevel=\"MAY\""
        + " scopePath=\"/metadata/instrument\" cvTermsCombinationLogic=\"OR\">\n"
        + "      <CvTerm termAccession=\"MS:1000458\" useTerm=\"false\" termName=\"source\" isRepeatable=\"false\" allowChildren=\"true\" cvIdentifierRef=\"MS\"/>\n"
        + "    </CvMappingRule>\n"
        + "  </CvMappingRuleList>\n"
        + "</CvMapping>\n";

    /** The version of PSI-MS that minimal-m-2.0.mztab declares. */
    private static final String DECLARED_MS = "3.54.0";
    /** The release a forced run substitutes: the only one the test resources list. */
    private static final String LISTED_MS = "4.1.200";

    /** A user's MS entry: one listed release, at a URL that is never asked (the runs are offline). */
    private static final String USER_MS = """
        {"vocabularies": {"MS": {
          "labels": ["MS"],
          "oboPrefix": "MS",
          "location": {"format": "OBO", "urlTemplate": "http://localhost.invalid/MS/{version}/psi-ms.obo"},
          "scheme": "%s",
          "releases": {"kind": "listed", "releases": [{"version": "%s"}]},
          "latest": {"kind": "newestRelease"}
        }}}""";

    @Test
    public void localLookupIsTheDefault() throws Exception {
        Path cache = prefilledMs(DECLARED_MS);

        Run run = run("-c", minimal(), "-s", msMapping(), "--cvOffline", "--cvCache", cache.toString());

        // the pre-filled release was adopted: its index was built and its record written
        assertTrue(Files.isRegularFile(cache.resolve("MS").resolve(DECLARED_MS).resolve("release.json")), run.output);
        assertFalse(run.output.contains("-3017]"), run.output);
        // ESI is a source in the cached release, ETD is not a term of it
        assertTrue(run.output.contains("MS:1000598"), run.output);
        assertFalse(run.output.contains("MS:1000073"), run.output);
    }

    @Test
    public void theReportListsTheVocabulariesUsed() throws Exception {
        Path cache = prefilledMs(DECLARED_MS);

        Run run = run("-c", minimal(), "-s", msMapping(), "--cvOffline", "--cvCache", cache.toString());

        assertTrue(run.lines().contains("CV MS: " + DECLARED_MS + " (FILE) from "
            + "https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/v" + DECLARED_MS + "/psi-ms.obo"), run.output);
    }

    @Test
    public void theReportMarksAnOlsAnswerAsNotReRunnable() {
        ResolvedVocabulary ols = new ResolvedVocabulary("MS", DECLARED_MS, Optional.of(DECLARED_MS),
            Optional.empty(), "4.2.15", "OLS", Optional.empty(), Optional.of("4.2.15"),
            Optional.of(Instant.parse("2026-11-11T10:00:00Z")), false);

        assertEquals("CV MS: 4.2.15 (OLS) from OLS (version 4.2.15), not re-runnable",
            CvLookupOptions.reportLine(ols));
    }

    @Test
    public void cvForceTurnsAMissingReleaseIntoAWarning() throws Exception {
        Path cache = prefilledMs(LISTED_MS);
        Path resources = Files.writeString(tempDir.resolve("resources.json"), USER_MS.formatted("DOTTED", LISTED_MS));

        Run strict = run("-c", minimal(), "-s", msMapping(), "--cvOffline", "--cvCache", cache.toString(),
            "--cvResources", resources.toString());
        assertTrue(strict.failed);
        assertTrue(strict.output.contains("[Error-3017]"), strict.output);

        Run forced = run("-c", minimal(), "-s", msMapping(), "--cvOffline", "--cvCache", cache.toString(),
            "--cvResources", resources.toString(), "--cvForce");
        assertFalse(forced.output.contains("-3017]"), forced.output);
        String substituted = forced.lines().stream().filter(l -> l.contains("[Warn-3018]")).findFirst()
            .orElseThrow(() -> new AssertionError(forced.output));
        assertTrue(substituted.contains(DECLARED_MS) && substituted.contains(LISTED_MS), substituted);
        assertTrue(forced.lines().contains("CV MS: " + LISTED_MS + " (SUBSTITUTE_NEWER) from "
            + "http://localhost.invalid/MS/" + LISTED_MS + "/psi-ms.obo"), forced.output);
    }

    @Test
    public void olsWithoutForceFailsTheValidation() throws Exception {
        Path cache = tempDir.resolve("untouched");

        Run run = run("-c", minimal(), "-s", msMapping(), "--cvSource", "ols", "--cvCache", cache.toString());

        assertTrue(run.failed);
        String unavailable = run.lines().stream().filter(l -> l.contains("[Error-3017]")).findFirst()
            .orElseThrow(() -> new AssertionError(run.output));
        assertTrue(unavailable.contains("OLS"), unavailable);
        assertFalse(Files.exists(cache), "an OLS run uses no cache");
    }

    @Test
    public void anInvalidResourcesFileStopsWithItsEntry() throws Exception {
        Path resources = Files.writeString(tempDir.resolve("resources.json"), USER_MS.formatted("semver", LISTED_MS));

        Run run = run("-c", minimal(), "-s", msMapping(), "--cvOffline", "--cvCache",
            tempDir.resolve("cv-cache").toString(), "--cvResources", resources.toString());

        assertTrue(run.failed);
        assertTrue(run.output.contains("MS") && run.output.contains("/vocabularies/MS/scheme"), run.output);
        // nothing was validated
        assertFalse(run.output.contains("Beginning validation"), run.output);
        assertFalse(run.output.contains("instrument_source_may"), run.output);
    }

    @Test
    public void anUnknownCvSourceStopsTheRun() throws Exception {
        Run run = run("-c", minimal(), "-s", msMapping(), "--cvSource", "elsewhere");

        assertTrue(run.failed);
        assertTrue(run.output.contains("elsewhere"), run.output);
        assertFalse(run.output.contains("Beginning validation"), run.output);
    }

    /** What a run printed with -o, and whether it failed (a non-zero exit). */
    private record Run(boolean failed, String output) {

        List<String> lines() {
            return output.lines().toList();
        }
    }

    /** Runs the CLI as main does, with -o, so that messages and log lines all land in one file. */
    private Run run(String... args) throws Exception {
        Options options = new Options();
        String outOpt = MZTabCommandLine.addOutFileOption(options);
        String checkOpt = MZTabCommandLine.addCheckOption(options);
        String levelOpt = MZTabCommandLine.addLevelOption(options);
        String serializeOpt = MZTabCommandLine.addSerializeOption(options);
        String deserializeOpt = MZTabCommandLine.addDeserializeOption(options);
        String checkSemanticOpt = MZTabCommandLine.addCheckSemanticOption(options);
        CvLookupOptions.addTo(options);
        Path out = tempDir.resolve("out-" + System.nanoTime() + ".txt");
        List<String> all = new java.util.ArrayList<>(List.of(args));
        all.addAll(List.of("-o", out.toString()));
        CommandLine line = new DefaultParser().parse(options, all.toArray(String[]::new));
        boolean failed = MZTabCommandLine.handleValidationOptions(line, outOpt, levelOpt, serializeOpt,
            deserializeOpt, checkOpt, checkSemanticOpt);
        return new Run(failed, Files.readString(out, StandardCharsets.UTF_8));
    }

    private String minimal() throws IOException {
        return resource("/metabolomics/minimal-m-2.0.mztab").getAbsolutePath();
    }

    private String msMapping() throws IOException {
        return Files.writeString(tempDir.resolve("ms-mapping.xml"), MS_MAPPING).toString();
    }

    /** An operator's pre-fill: the mini PSI-MS alone in the release folder, under its download name. */
    private Path prefilledMs(String version) throws IOException {
        Path root = tempDir.resolve("cv-cache");
        Path folder = Files.createDirectories(root.resolve("MS").resolve(version));
        try (InputStream in = getClass().getResourceAsStream("/cv/mini-ms.obo")) {
            Files.copy(in, folder.resolve("psi-ms.obo"));
        }
        return root;
    }
}
