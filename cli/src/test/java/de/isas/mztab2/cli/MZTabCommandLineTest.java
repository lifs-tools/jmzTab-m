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
package de.isas.mztab2.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 *
 * @author nilshoffmann
 */
public class MZTabCommandLineTest {

    // scan_polarity must be a list of parameters, not a string
    private static final String UNREADABLE_JSON = "{\"metadata\": {\"ms_run\": [{\"id\": 1, \"location\": \"file:///run1.mzML\", \"scan_polarity\": \"positive scan\"}]}}";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File unreadableJson() throws Exception {
        File json = folder.newFile("unreadable.json");
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

        assertTrue("Unreadable JSON must be reported as a failure", errorsOrWarnings);
        String output = bytes.toString("UTF-8");
        assertFalse("JSON must not be validated as mzTab: " + output, output.contains("[Error-"));
        assertFalse("No mzTab file should be written", new File(json.getParentFile(), json.getName() + ".mztab").exists());
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
            assertTrue("Unreadable JSON must be reported as a failure", errorsOrWarnings);
            capture.print("still open");
            assertFalse("System.out must not be closed by the validator", capture.checkError());
        } finally {
            System.setOut(stdout);
            System.setErr(stderr);
        }
    }
}
