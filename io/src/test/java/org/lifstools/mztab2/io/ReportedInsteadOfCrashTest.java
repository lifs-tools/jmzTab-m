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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lifstools.mztab2.model.PublicationItem;
import org.lifstools.mztab2.model.ValidationLocation.SectionEnum;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Values that ended the validation with an exception, or that were reported
 * as a generic "empty cell" (Error-2000), are reported as the message for
 * the value (#249, #251).
 */
public class ReportedInsteadOfCrashTest {

    private static final String BASE_FILE = "/metabolomics/lipidomics-example.mzTab";

    @TempDir
    Path tempDir;

    @Test
    void cvUriWithASpaceIsAnInvalidUri() throws IOException {
        MzTabFileParser parser = parse(replace(
            "https://github.com/HUPO-PSI/psi-ms-CV/blob/master/psi-ms.obo",
            "http://purl.obolibrary.org/obo/my ontology.owl"));
        MZTabError e = single(parser.getErrorList(), FormatErrorType.URI);
        assertEquals(SectionEnum.MTD, e.getLocation().getSection());
        assertEquals("cv[1]-uri", e.getLocation().getMetadataKey());
        assertTrue(e.getMessage().contains("my ontology.owl"), e.getMessage());
        assertNoNullError(parser.getErrorList());
        // the validation ran to the end: the metadata checks after the last
        // line report the cv without a URI
        assertTrue(parser.getErrorList().getErrorList().stream().
            anyMatch(err -> err.getType() == LogicalErrorType.NotDefineInMetadata
                && err.getMessage().contains("cv[1]-uri")),
            () -> parser.getErrorList().toString());
    }

    @Test
    void smlUriWithASpaceIsAnInvalidUri() throws IOException {
        MzTabFileParser parser = parse(replace(
            // the uri after the inchi of the only SML row
            "/s1\thttp://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012\t",
            "/s1\thttp://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP 02010012\t"));
        MZTabError e = single(parser.getErrorList(), FormatErrorType.URI);
        assertEquals(SectionEnum.SML, e.getLocation().getSection());
        assertEquals("uri", e.getLocation().getColumn());
        assertEquals(0, e.getLocation().getRowIndex());
        assertNoNullError(parser.getErrorList());
    }

    @Test
    void cvOptColumnWithoutAccessionIsAnInvalidOptColumn() throws IOException {
        MzTabFileParser parser = parse(replace(
            "\topt_global_lipid_best_id_level",
            "\topt_global_cv_MS:1002217"));
        MZTabError e = single(parser.getErrorList(),
            FormatErrorType.OptionalCVParamColumn);
        assertEquals(SectionEnum.SMH, e.getLocation().getSection());
        assertEquals("opt_global_cv_MS:1002217", e.getLocation().getColumn());
        assertNoNullError(parser.getErrorList());
    }

    @Test
    void emptyPublicationItemIsAnInvalidPublication() throws IOException {
        MzTabFileParser parser = parse(replace(
            "pubmed:29039908 | doi:10.1021/acs.analchem.7b03576",
            "pubmed:1||doi:10.1021/x"));
        MZTabError e = single(parser.getErrorList(), FormatErrorType.Publication);
        assertEquals("publication[1]", e.getLocation().getMetadataKey());
        assertNoNullError(parser.getErrorList());
    }

    @Test
    void doiWithAColonIsValidAndKeepsItsSpelling() throws IOException {
        String doi = "10.1002/(SICI)1097-0258(19980815/30)17:15/16<1661::AID-SIM968>3.0.CO;2-2";
        MzTabFileParser parser = parse(replace(
            "pubmed:29039908 | doi:10.1021/acs.analchem.7b03576",
            "pubmed:29039908 | DOI:" + doi));
        assertFalse(parser.getErrorList().getErrorList().stream().
            anyMatch(err -> err.getType() == FormatErrorType.Publication),
            () -> parser.getErrorList().toString());
        assertNotNull(parser.getMZTabFile(), () -> parser.getErrorList().toString());
        List<PublicationItem> items = parser.getMZTabFile().getMetadata().
            getPublication().get(0).getPublicationItems();
        assertEquals(2, items.size());
        assertEquals(PublicationItem.TypeEnum.DOI, items.get(1).getType());
        assertEquals(doi, items.get(1).getAccession());
    }

    private static MZTabError single(MZTabErrorList errors, MZTabErrorType type) {
        List<MZTabError> found = errors.getErrorList().stream().
            filter(e -> e.getType() == type).
            collect(Collectors.toList());
        assertEquals(1, found.size(), () -> errors.toString());
        return found.get(0);
    }

    private static void assertNoNullError(MZTabErrorList errors) {
        // Error-2000 is what a NullPointerException in a line parser becomes
        assertFalse(errors.getErrorList().stream().
            anyMatch(e -> e.getType() == LogicalErrorType.NULL),
            () -> errors.toString());
    }

    private Path replace(String original, String replacement) throws IOException {
        String text;
        try (InputStream in = getClass().getResourceAsStream(BASE_FILE)) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertEquals(1, text.split(java.util.regex.Pattern.quote(original), -1).length - 1,
            "the base file must contain " + original + " once");
        Path file = tempDir.resolve("variant.mzTab");
        Files.writeString(file, text.replace(original, replacement), StandardCharsets.UTF_8);
        return file;
    }

    private static MzTabFileParser parse(Path file) throws IOException {
        MzTabFileParser parser = new MzTabFileParser(file.toFile());
        // throws if the validation ends with an exception
        parser.parse(new ByteArrayOutputStream(), MZTabErrorType.Level.Info, 500);
        return parser;
    }
}
