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
package org.lifstools.mztab2.validation;

import org.lifstools.mztab2.io.MzTabFileParser;
import org.lifstools.mztab2.model.MzTab;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Utility class to use for parsing of resources that exist in a temporary
 * folder.
 *
 * @see MzTabRawParserTest
 * @see MzTabWriterTest
 *
 * @author nilshoffmann
 */
public class SemanticTestResources {

    public static MzTab parseResource(File baseDir, String resource,
        MZTabErrorType.Level level, Integer expectedErrors) throws URISyntaxException, IOException, MZTabException, MZTabErrorOverflowException {
        File testFile = new File(baseDir, resource);
        assertTrue(testFile.exists() && testFile.isFile());
        MzTabFileParser parser = new MzTabFileParser(testFile);
        parser.parse(System.err, level, 500);
        assertEquals((long) expectedErrors, (long) parser.getErrorList().size(),
            String.format("Expected %d structural or logical errors, found %d! Errors: %s",
                expectedErrors, parser.getErrorList().size(),
                parser.getErrorList().convertToValidationMessages()));
        assertNotNull(parser.getMZTabFile(),
            "Expected parser.getMZTabFile() to return a non null MzTab object");
        return parser.getMZTabFile();
    }
}
