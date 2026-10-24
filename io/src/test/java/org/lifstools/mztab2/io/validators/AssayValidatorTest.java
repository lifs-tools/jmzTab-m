/*
 * Copyright 2019 LIFS Tools Consortium.
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

import org.lifstools.mztab2.io.validators.AssayValidator;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 *
 * @author nilshoffmann
 */
public class AssayValidatorTest {

    /**
     * Test of validateRefine method for null Assay, of class AssayValidator.
     */
    @Test
    public void testValidateRefineNullAssay() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        AssayValidator instance = new AssayValidator();
        List<MZTabError> expResult = Arrays.asList(new MZTabError(
                LogicalErrorType.NotDefineInMetadata, -1,
                Metadata.JSON_PROPERTY_ASSAY + ""));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }
    
    @Disabled
    @Test
    public void testValidateMissingName() {
        Metadata metadata = new Metadata();
        metadata.addAssayItem(new Assay().id(1));
        MZTabParserContext parserContext = new MZTabParserContext();
        AssayValidator instance = new AssayValidator();
        List<MZTabError> expResult = Arrays.asList(new MZTabError(
                LogicalErrorType.NotDefineInMetadata, -1,
                Metadata.JSON_PROPERTY_ASSAY + ""));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

    /**
     * Test of validateRefine method for assay requiring a linked ms run, of
     * class AssayValidator.
     */
    @Test
    public void testValidateRefineAssayHasMsRun() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        metadata.addAssayItem(new Assay().id(1));
        assertEquals(1, metadata.getAssay().size());
        parserContext.addAssay(metadata, metadata.getAssay().get(0));
        AssayValidator instance = new AssayValidator();
        List<MZTabError> expResult = Arrays.asList(
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.JSON_PROPERTY_ASSAY + "[" + 1 + "]-" + Assay.JSON_PROPERTY_MS_RUN_REF)
        );
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

}
