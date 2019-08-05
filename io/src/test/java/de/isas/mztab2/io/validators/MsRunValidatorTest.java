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
package de.isas.mztab2.io.validators;

import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.Parameter;
import java.util.Arrays;
import java.util.Collections;
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
public class MsRunValidatorTest {

    /**
     * Test of validateRefine method, of class AssayValidator.
     */
    @Test
    public void testValidateRefineMissingLocation() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        MsRunValidator instance = new MsRunValidator();
        MsRun msRun = new MsRun().id(1).location(null).scanPolarity(Arrays.asList(new Parameter().cvLabel("MS").cvAccession("MS:1000130").name("positive scan")));
        parserContext.addMsRun(metadata, msRun);
        metadata.addMsRunItem(msRun);
        List<MZTabError> expResult = Arrays.asList(new MZTabError(
                LogicalErrorType.NotDefineInMetadata, -1,
                Metadata.Properties.msRun + "[" + 1 + "]-" + MsRun.Properties.location));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

    /**
     * Test of validateRefine method, of class AssayValidator.
     */
    @Test
    public void testValidateRefineMissingScanPolarity() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        MsRunValidator instance = new MsRunValidator();
        MsRun msRun = new MsRun().id(1).location("file:///path/to/my.mzml").scanPolarity(null);
        parserContext.addMsRun(metadata, msRun);
        metadata.addMsRunItem(msRun);
        List<MZTabError> expResult = Arrays.asList(new MZTabError(
                LogicalErrorType.NotDefineInMetadata, -1,
                Metadata.Properties.msRun + "[" + 1 + "]-" + MsRun.Properties.scanPolarity));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
        
        msRun.scanPolarity(Collections.emptyList());
        result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }
}
