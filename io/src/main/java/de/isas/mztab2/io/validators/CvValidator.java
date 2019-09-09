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

import de.isas.mztab2.model.CV;
import de.isas.mztab2.model.Metadata;
import java.util.LinkedList;
import java.util.List;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Validates that the controlled vocabulary section is present in metadata and
 * that it is correctly populated.
 *
 * @author nilshoffmann
 */
public class CvValidator implements RefiningValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        List<MZTabError> errorList = new LinkedList<>();
        if (metadata.getCv() == null || metadata.getCv().
                isEmpty()) {
            errorList.add(new MZTabError(
                    LogicalErrorType.NotDefineInMetadata, -1,
                    Metadata.Properties.cv + ""));
        } else {
            for (CV cv : metadata.getCv()) {
                if (cv.getLabel() == null || cv.getLabel().isEmpty()) {
                    errorList.add(new MZTabError(
                            LogicalErrorType.NotDefineInMetadata, -1,
                            Metadata.Properties.cv + "[" + cv.getId() + "]-" + CV.Properties.label));
                }
                if (cv.getFullName() == null || cv.getFullName().isEmpty()) {
                    errorList.add(new MZTabError(
                            LogicalErrorType.NotDefineInMetadata, -1,
                            Metadata.Properties.cv + "[" + cv.getId() + "]-" + CV.Properties.fullName));
                }
                if (cv.getVersion() == null || cv.getVersion().isEmpty()) {
                    errorList.add(new MZTabError(
                            LogicalErrorType.NotDefineInMetadata, -1,
                            Metadata.Properties.cv + "[" + cv.getId() + "]-" + CV.Properties.version));
                }
                if (cv.getUri() == null || cv.getUri().isEmpty()) {
                    errorList.add(new MZTabError(
                            LogicalErrorType.NotDefineInMetadata, -1,
                            Metadata.Properties.cv + "[" + cv.getId() + "]-" + CV.Properties.uri));
                }
            }
        }
        return errorList;
    }

}
