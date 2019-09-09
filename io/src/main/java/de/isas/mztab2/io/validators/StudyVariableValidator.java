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

import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.StudyVariable;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Validates that the study variable section is present in metadata and is
 * linked to all assays and ms runs.
 *
 * @author nilshoffmann
 */
public class StudyVariableValidator implements RefiningValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        SortedMap<Integer, Assay> assayMap = parserContext.getAssayMap();
        SortedMap<Integer, StudyVariable> svMap = parserContext.getStudyVariableMap();
        List<MZTabError> errorList = new LinkedList<>();
        if (svMap.isEmpty()) {
            errorList.add(new MZTabError(
                    LogicalErrorType.SingleStudyVariableName, -1));
        } else {
            if (svMap.size() == 1 && assayMap.size() > 0) {
                StudyVariable sv = svMap.get(Integer.valueOf(1));
                if (sv.getName() == null || sv.getName().isEmpty()) {
                    errorList.add(new MZTabError(
                            LogicalErrorType.SingleStudyVariableName, -1));
                }
            } else {
                boolean undefinedDefined = false;
                for (Integer id : svMap.keySet()) {
                    StudyVariable sv = svMap.get(id);
                    if (sv == null) {
                        errorList.add(new MZTabError(
                                LogicalErrorType.NotDefineInMetadata, -1,
                                Metadata.Properties.studyVariable + "[" + id + "]" + "\t" + "<NAME>"));
                    } else {
                        if (sv.getName() == null) {
                            errorList.add(new MZTabError(
                                    LogicalErrorType.NotDefineInMetadata, -1,
                                    Metadata.Properties.studyVariable + "[" + id + "]" + "\t" + "<NAME>"));
                        } else {
                            if (sv.getName().equals("undefined")) {
                                if (undefinedDefined) {
                                    errorList.add(new MZTabError(
                                            LogicalErrorType.UndefinedStudyVariableNameOnceOnly, -1,
                                            Metadata.Properties.studyVariable + "[" + id + "]" + "\t" + "<NAME>"));
                                } else {
                                    undefinedDefined = true;
                                }
                            }
                        }
                        if (sv.
                                getDescription() == null) {
                            errorList.add(new MZTabError(
                                    LogicalErrorType.NotDefineInMetadata, -1,
                                    Metadata.Properties.studyVariable + "[" + id + "]-" + StudyVariable.Properties.description + "\t" + "<DESCRIPTION>"));
                        }
                        if (sv.
                                getAssayRefs() == null || sv.getAssayRefs().
                                        isEmpty()) {
                            errorList.add(new MZTabError(
                                    LogicalErrorType.AssayRefs, -1,
                                    Metadata.Properties.studyVariable + "[" + id + "]-" + StudyVariable.Properties.assayRefs));
                        }
                    }
                }
            }
        }

        return errorList;
    }

}
