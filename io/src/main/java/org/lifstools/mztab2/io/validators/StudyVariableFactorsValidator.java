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
package org.lifstools.mztab2.io.validators;

import java.util.LinkedList;
import java.util.List;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.StudyVariable;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * study_variable[1-n]-factors belongs to mzTab-M 2.0; a file that declares a
 * later version gets a warning for it. The check runs once all metadata lines
 * have been read, so the order of the mzTab-version line does not matter.
 */
public class StudyVariableFactorsValidator implements RefiningValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        List<MZTabError> errorList = new LinkedList<>();
        String version = metadata.getMzTabVersion();
        if (version == null || version.startsWith("2.0")) {
            return errorList;
        }
        parserContext.getStudyVariableMap().forEach((id, sv) -> {
            if (sv != null && sv.getFactors() != null && !sv.getFactors().isEmpty()) {
                String element = Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]";
                errorList.add(new MZTabError(
                        LogicalErrorType.StudyVariableFactorsRemoved, -1,
                        element, version).
                        location(Locations.metadataKey(element + "-" + StudyVariable.JSON_PROPERTY_FACTORS)));
            }
        });
        return errorList;
    }

}
