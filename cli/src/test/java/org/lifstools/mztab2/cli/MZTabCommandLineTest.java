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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 *
 * @author nilshoffmann
 */
public class MZTabCommandLineTest {

    // scan_polarity must be a list of parameters, not a string
    private static final String UNREADABLE_JSON = "{\"metadata\": {\"ms_run\": [{\"id\": 1, \"location\": \"file:///run1.mzML\", \"scan_polarity\": \"positive scan\"}]}}";

    @TempDir
    Path folder;

    private File unreadableJson() throws Exception {
        File json = folder.resolve("unreadable.json").toFile();
        Files.write(json.toPath(), UNREADABLE_JSON.getBytes(StandardCharsets.UTF_8));
        return json;
    }

    @Test
    public void unreadableJsonFailsWithoutValidatingTheJsonAsMzTab() throws Exception {
        File json = unreadableJson();
        Options options = new Options();
        String checkOpt = MZTabCommandLine.addCheckOption(options);
        String checkSemanticOpt = MZTabCommandLine.addCheckSemanticOption(options);
        MZTabCommandLine.addDeserializeOption(options);
        CommandLine line = new PosixParser().parse(options, new String[]{"-c", json.getAbsolutePath(), "--fromJson"});
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        boolean errorsOrWarnings = MZTabCommandLine.handleValidation(line, checkOpt,
            new PrintStream(bytes, true, "UTF-8"), MZTabErrorType.Level.Info, checkSemanticOpt, false, true);

        assertTrue(errorsOrWarnings, "Unreadable JSON must be reported as a failure");
        String output = bytes.toString("UTF-8");
        assertFalse(output.contains("[Error-"), "JSON must not be validated as mzTab: " + output);
        assertFalse(new File(json.getParentFile(), json.getName() + ".mztab").exists(), "No mzTab file should be written");
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

    private File resource(String name) throws Exception {
        File file = folder.resolve(name.substring(name.lastIndexOf('/') + 1)).toFile();
        try (InputStream in = getClass().getResourceAsStream(name)) {
            Files.copy(in, file.toPath());
        }
        return file;
    }

    /** Runs a validation with -c and -s, returns whether it failed and what it printed. */
    private boolean validateSemantics(File file, String mapping, ByteArrayOutputStream bytes) throws Exception {
        Options options = new Options();
        String checkOpt = MZTabCommandLine.addCheckOption(options);
        String checkSemanticOpt = MZTabCommandLine.addCheckSemanticOption(options);
        CommandLine line = new PosixParser().parse(options, new String[]{"-c", file.getAbsolutePath(), "-s", mapping});
        return MZTabCommandLine.handleValidation(line, checkOpt,
            new PrintStream(bytes, true, "UTF-8"), MZTabErrorType.Level.Info, checkSemanticOpt, false, false);
    }

    @Test
    public void semanticValidationAlsoRunsAfterStructuralWarnings() throws Exception {
        // an M+S+F file: Warn-2056 from the structural validation
        File file = resource("/metabolomics/xcms-test-export.mztab");
        Path mapping = Files.writeString(folder.resolve("mapping.xml"), MAPPING);
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

        assertTrue(validateSemantics(file, folder.resolve("no-such-mapping.xml").toString(), bytes));
    }

    @Test
    public void invalidMappingFileFailsWithoutAnException() throws Exception {
        File file = resource("/metabolomics/lipidomics-example.mzTab");
        Path mapping = Files.writeString(folder.resolve("mapping.xml"), "not a mapping");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        assertTrue(validateSemantics(file, mapping.toString(), bytes));
    }

    @Test
    public void semanticValidationIsSkippedForAFileWithoutModel() throws Exception {
        // a data line without its header line: no model
        File file = resource("/metabolomics/lipidomics-example.mzTab");
        Files.writeString(file.toPath(), "SML\t1\n", java.nio.file.StandardOpenOption.APPEND);
        Path mapping = Files.writeString(folder.resolve("mapping.xml"), MAPPING);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        assertTrue(validateSemantics(file, mapping.toString(), bytes));
        assertFalse(bytes.toString("UTF-8").contains("sample_disease_must"));
    }

    @Test
    public void validationOptionsReportIOErrorsAndKeepStdoutOpen() throws Exception {
        File json = unreadableJson();
        Options options = new Options();
        String outOpt = MZTabCommandLine.addOutFileOption(options);
        String checkOpt = MZTabCommandLine.addCheckOption(options);
        String levelOpt = MZTabCommandLine.addLevelOption(options);
        String serializeOpt = MZTabCommandLine.addSerializeOption(options);
        String deserializeOpt = MZTabCommandLine.addDeserializeOption(options);
        String checkSemanticOpt = MZTabCommandLine.addCheckSemanticOption(options);
        CommandLine line = new PosixParser().parse(options, new String[]{"-c", json.getAbsolutePath(), "--fromJson"});
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(bytes, true, "UTF-8");
        System.setOut(capture);
        try {
            boolean errorsOrWarnings = MZTabCommandLine.handleValidationOptions(line, outOpt, levelOpt,
                serializeOpt, deserializeOpt, checkOpt, checkSemanticOpt);
            assertTrue(errorsOrWarnings, "Unreadable JSON must be reported as a failure");
            capture.print("still open");
            assertFalse(capture.checkError(), "System.out must not be closed by the validator");
        } finally {
            System.setOut(stdout);
            System.setErr(stderr);
        }
    }
}
