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
package org.lifstools.mztab2.cli;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
}
