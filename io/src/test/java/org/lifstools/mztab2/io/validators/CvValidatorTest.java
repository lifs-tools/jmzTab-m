/*
 * Copyright 2019 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package org.lifstools.mztab2.io.validators;

import org.lifstools.mztab2.io.validators.CvValidator;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Metadata;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 *
 * @author nilshoffmann
 */
public class CvValidatorTest {

    /**
     * Test of validateRefine method for null CV, of class CvValidator.
     */
    @Test
    public void testValidateRefineNullCv() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        CvValidator instance = new CvValidator();
        List<MZTabError> expResult = Arrays.asList(new MZTabError(
                LogicalErrorType.NotDefineInMetadata, -1,
                Metadata.Properties.cv + ""));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

    /**
     * Test of validateRefine method for CV with undefined / null elements, of
     * class CvValidator.
     */
    @Test
    public void testValidateCvWithNullElements() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        CvValidator instance = new CvValidator();
        metadata.addCvItem(new CV().id(1));
        List<MZTabError> expResult = Arrays.asList(
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.cv + "[" + 1 + "]-" + CV.Properties.label),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.cv + "[" + 1 + "]-" + CV.Properties.fullName),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.cv + "[" + 1 + "]-" + CV.Properties.version),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.cv + "[" + 1 + "]-" + CV.Properties.uri)
        );
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
        assertEquals(expResult.get(1).toString(), result.get(1).toString());
        assertEquals(expResult.get(2).toString(), result.get(2).toString());
        assertEquals(expResult.get(3).toString(), result.get(3).toString());
    }

    /**
     * Test of validateRefine method for CV with empty elements, of
     * class CvValidator.
     */
    @Test
    public void testValidateCvWithEmptyElements() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        CvValidator instance = new CvValidator();
        metadata.addCvItem(new CV().id(1).fullName("").label("").version("").uri(""));
        List<MZTabError> expResult = Arrays.asList(
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.cv + "[" + 1 + "]-" + CV.Properties.label),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.cv + "[" + 1 + "]-" + CV.Properties.fullName),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.cv + "[" + 1 + "]-" + CV.Properties.version),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.cv + "[" + 1 + "]-" + CV.Properties.uri)
        );
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
        assertEquals(expResult.get(1).toString(), result.get(1).toString());
        assertEquals(expResult.get(2).toString(), result.get(2).toString());
        assertEquals(expResult.get(3).toString(), result.get(3).toString());
    }
}
