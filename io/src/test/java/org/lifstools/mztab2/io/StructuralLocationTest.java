/*
 * Copyright 2026 LIFS Tools Consortium.
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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lifstools.mztab2.model.ValidationLocation.SectionEnum;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Parser messages carry the section, row, column and metadata key they refer
 * to.
 */
public class StructuralLocationTest {

    @Test
    void headerErrorCarriesHeaderSectionAndColumn() throws Exception {
        MZTabError e = firstError("missing-abundance-column.mzTab", LogicalErrorType.NotDefineInHeader);
        assertEquals(SectionEnum.SMH, e.getLocation().getSection());
        assertEquals("abundance_assay[1]", e.getLocation().getColumn());
        assertNull(e.getLocation().getRowIndex());
    }

    @Test
    void headerColumnKeepsTheCaseWrittenInTheFile() throws Exception {
        MZTabError e = firstError("mixed-case-abundance-header.mzTab", FormatErrorType.AbundanceColumn);
        assertEquals(SectionEnum.SMH, e.getLocation().getSection());
        assertEquals("abundance_study_variable[A]", e.getLocation().getColumn());
    }

    @Test
    void dataRowErrorCarriesRowIndexAndColumn() throws Exception {
        MZTabError e = firstErrorOnLineStartingWith("bad-row-value.mzTab", "SML\t2\t");
        assertEquals(SectionEnum.SML, e.getLocation().getSection());
        assertEquals(1, e.getLocation().getRowIndex()); // second SML line
        assertEquals("best_id_confidence_value", e.getLocation().getColumn());
    }

    @Test
    void rowIndexHoldsBeyondTheFirstThousandLines(@TempDir Path dir) throws Exception {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 3000; i++) {
            lines.add("COM\tpadding " + i);
        }
        lines.addAll(Files.readAllLines(resource("bad-row-value.mzTab").toPath(), StandardCharsets.UTF_8));
        Path file = dir.resolve("padded-bad-row-value.mzTab");
        Files.write(file, lines, StandardCharsets.UTF_8);
        int lineNumber = lines.indexOf(lines.stream().filter(l -> l.startsWith("SML\t2\t")).findFirst().orElseThrow()) + 1;

        MZTabError e = parse(file.toFile()).getErrorList().getErrorList().stream()
            .filter(err -> err.getLineNumber() == lineNumber)
            .findFirst()
            .orElseThrow();
        assertEquals(SectionEnum.SML, e.getLocation().getSection());
        assertEquals(1, e.getLocation().getRowIndex());
        assertEquals("best_id_confidence_value", e.getLocation().getColumn());
    }

    @Test
    void metadataLineErrorCarriesKeyAndGroup() throws Exception {
        MZTabError e = firstErrorOnLineStartingWith("bad-mtd-value.mzTab", "MTD\tms_run[1]-format");
        assertEquals(SectionEnum.MTD, e.getLocation().getSection());
        assertEquals("ms_run[1]-format", e.getLocation().getMetadataKey());
        assertEquals("ms_run", e.getLocation().getMetadataGroup());
    }

    @Test
    void everyLinedErrorHasASection() throws Exception {
        for (String f : List.of("missing-abundance-column.mzTab", "bad-row-value.mzTab", "bad-mtd-value.mzTab")) {
            parse(f).getErrorList().getErrorList().stream().filter(e -> e.getLineNumber() > 0)
                .forEach(e -> assertNotNull(e.getLocation(), f + ": " + e));
        }
    }

    @Test
    void validatorErrorsNameTheirMetadataGroup() throws Exception {
        MZTabError e = firstError("bad-assay.mzTab", LogicalErrorType.NotDefineInMetadata);
        assertEquals("assay", e.getLocation().getMetadataGroup());
        assertNull(e.getLocation().getMetadataKey()); // the key is missing from the file, so there is no line to link to
    }

    private static File resource(String name) throws URISyntaxException {
        return new File(StructuralLocationTest.class.getResource("/locations/" + name).toURI());
    }

    private static MzTabFileParser parse(String name) throws URISyntaxException, IOException {
        return parse(resource(name));
    }

    private static MzTabFileParser parse(File file) throws IOException {
        MzTabFileParser parser = new MzTabFileParser(file);
        // The parser closes the stream it is given and writes to it afterwards:
        // System.out would be closed for the whole test JVM, and
        // OutputStream.nullOutputStream() throws once closed.
        parser.parse(new ByteArrayOutputStream(), MZTabErrorType.Level.Info, 500);
        return parser;
    }

    private static MZTabError firstError(String name, MZTabErrorType type) throws URISyntaxException, IOException {
        MzTabFileParser parser = parse(name);
        return parser.getErrorList().getErrorList().stream()
            .filter(e -> e.getType() == type)
            .findFirst()
            .orElseGet(() -> fail("No " + type + " error in " + name + ": " + parser.getErrorList()));
    }

    private static MZTabError firstErrorOnLineStartingWith(String name, String prefix) throws URISyntaxException, IOException {
        List<String> lines = Files.readAllLines(resource(name).toPath(), StandardCharsets.UTF_8);
        int lineNumber = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(prefix)) {
                lineNumber = i + 1;
                break;
            }
        }
        if (lineNumber < 0) {
            fail("No line starting with " + prefix + " in " + name);
        }
        int line = lineNumber;
        MzTabFileParser parser = parse(name);
        return parser.getErrorList().getErrorList().stream()
            .filter(e -> e.getLineNumber() == line)
            .findFirst()
            .orElseGet(() -> fail("No error on line " + line + " of " + name + ": " + parser.getErrorList()));
    }
}
