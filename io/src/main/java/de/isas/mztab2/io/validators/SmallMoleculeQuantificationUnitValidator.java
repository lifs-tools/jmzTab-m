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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Validates that smallMoleculeQuantificationUnit is neither null nor empty.
 *
 * @author nilshoffmann
 */
public class SmallMoleculeQuantificationUnitValidator implements MetadataValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        if (metadata.getSmallMoleculeQuantificationUnit() == null) {
            return Arrays.asList(new MZTabError(
                    LogicalErrorType.NotDefineInMetadata, -1,
                    Metadata.Properties.smallMoleculeQuantificationUnit + ""));
        }
        return Collections.emptyList();
    }

}
