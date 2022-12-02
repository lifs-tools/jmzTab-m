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

import org.lifstools.mztab2.io.validators.MzTabVersionValidator;
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
public class MzTabVersionValidatorTest {

    /**
     * Test of validateRefine method, of class MzTabVersionValidator.
     */
    @Test
    public void testValidateRefine() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        MzTabVersionValidator instance = new MzTabVersionValidator();
        List<MZTabError> expResult = Arrays.asList(new MZTabError(
                LogicalErrorType.NotDefineInMetadata, -1,
                Metadata.Properties.mzTabVersion.getPropertyName(),
                metadata.getMzTabVersion()));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

}
