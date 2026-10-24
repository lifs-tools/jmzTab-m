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
import java.io.StringWriter;
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
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.MzTabProfile;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Tables without rows: their headers are written only where the file's
 * profile has the table, or where it declares no profile (mzTab-M 2.0).
 */
public class EmptyTableWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void aProfileMFileIsWrittenWithoutTableHeadersAndReadsBackWithoutErrors() throws IOException {
        Path original = tempDir.resolve("profile-m.mztab");
        try (InputStream is = getClass().getResourceAsStream("/metabolomics/official-profile-m.mztab")) {
            Files.copy(is, original);
        }
        MzTabFileParser parser = new MzTabFileParser(original.toFile());
        MZTabErrorList before = parser.parse(new ByteArrayOutputStream(), MZTabErrorType.Level.Error, 100);
        assertFalse(before.hasErrors(), before::toString);
        MzTab mzTab = parser.getMZTabFile();
        assertEquals(MzTabProfile.M, mzTab.getMetadata().getMzTabProfile());

        Path written = tempDir.resolve("written.mztab");
        new MzTabNonValidatingWriter().write(written, mzTab);

        List<String> prefixes = Files.readAllLines(written).stream().
            map(line -> line.length() >= 3 ? line.substring(0, 3) : line).
            distinct().collect(Collectors.toList());
        assertFalse(prefixes.contains("SMH") || prefixes.contains("SFH") || prefixes.contains("SEH"),
            prefixes::toString);
        MzTabFileParser reread = new MzTabFileParser(written.toFile());
        MZTabErrorList after = reread.parse(new ByteArrayOutputStream(), MZTabErrorType.Level.Error, 100);
        assertFalse(after.hasErrors(), after::toString);
        assertNotNull(reread.getMZTabFile());
    }

    @Test
    void tablesWithoutRowsAreWrittenByProfile() throws IOException {
        MzTab mzTab = new MzTab().metadata(new Metadata().mzTabVersion("2.1.0-M").
            mzTabID("empty").mzTabProfile(MzTabProfile.M_S));
        String written = write(mzTab);
        assertTrue(written.contains("\nSMH\t"), written);
        assertFalse(written.contains("\nSFH\t"), written);
        assertFalse(written.contains("\nSEH\t"), written);
    }

    @Test
    void tablesWithoutRowsAreWrittenWithoutAProfile() throws IOException {
        // mzTab-M 2.0 has no profile; its summary table is mandatory
        MzTab mzTab = new MzTab().metadata(new Metadata().mzTabVersion("2.0.0-M").mzTabID("empty"));
        String written = write(mzTab);
        assertTrue(written.contains("\nSMH\t"), written);
        assertTrue(written.contains("\nSFH\t"), written);
        assertTrue(written.contains("\nSEH\t"), written);
    }

    private static String write(MzTab mzTab) throws IOException {
        StringWriter writer = new StringWriter();
        new MzTabNonValidatingWriter().writeMzTab(mzTab, writer);
        return writer.toString();
    }
}
