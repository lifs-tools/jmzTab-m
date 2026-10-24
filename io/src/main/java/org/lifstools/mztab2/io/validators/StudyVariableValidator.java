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

import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.StudyVariable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;
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
                    LogicalErrorType.SingleStudyVariableName, -1).location(Locations.metadataGroup(Metadata.JSON_PROPERTY_STUDY_VARIABLE)));
        } else {
            // Resolve deferred assay_refs against the (now complete) assay map.
            // Referenced assays may have been defined after the study variable.
            for (StudyVariable sv : svMap.values()) {
                if (sv == null || sv.getAssayRefs() == null) {
                    continue;
                }
                List<Assay> refs = sv.getAssayRefs();
                for (int i = 0; i < refs.size(); i++) {
                    Integer refId = refs.get(i).getId();
                    Assay resolved = assayMap.get(refId);
                    if (resolved == null) {
                        errorList.add(new MZTabError(
                                LogicalErrorType.NotDefineInMetadata, -1,
                                Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + sv.getId() + "]-"
                                + StudyVariable.JSON_PROPERTY_ASSAY_REFS
                                + "\t" + Metadata.JSON_PROPERTY_ASSAY + "[" + refId + "]").location(Locations.metadataGroup(Metadata.JSON_PROPERTY_STUDY_VARIABLE)));
                    } else {
                        refs.set(i, resolved);
                    }
                }
            }
            if (svMap.size() == 1 && assayMap.size() > 0) {
                // the only study variable need not be study_variable[1]
                Integer id = svMap.firstKey();
                StudyVariable sv = svMap.get(id);
                if (sv == null || sv.getName() == null || sv.getName().isEmpty()) {
                    errorList.add(new MZTabError(
                            LogicalErrorType.SingleStudyVariableName, -1).location(Locations.metadataGroup(Metadata.JSON_PROPERTY_STUDY_VARIABLE)));
                } else if (sv.getAssayRefs() == null || sv.getAssayRefs().isEmpty()) {
                    errorList.add(new MZTabError(
                            LogicalErrorType.AssayRefs, -1,
                            Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]-" + StudyVariable.JSON_PROPERTY_ASSAY_REFS).location(Locations.metadataKey(Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]")));
                } else if ("undefined".equals(sv.getName())) {
                    checkUndefinedLinksAllAssays(id, sv, assayMap, errorList);
                }
            } else {
                boolean undefinedDefined = false;
                for (Integer id : svMap.keySet()) {
                    StudyVariable sv = svMap.get(id);
                    if (sv == null) {
                        errorList.add(new MZTabError(
                                LogicalErrorType.NotDefineInMetadata, -1,
                                Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]" + "\t<" + StudyVariable.JSON_PROPERTY_NAME + ">").location(Locations.metadataGroup(Metadata.JSON_PROPERTY_STUDY_VARIABLE)));
                    } else {
                        if (sv.getName() == null) {
                            errorList.add(new MZTabError(
                                    LogicalErrorType.NotDefineInMetadata, -1,
                                    Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]" + "\t<" + StudyVariable.JSON_PROPERTY_NAME + ">").location(Locations.metadataGroup(Metadata.JSON_PROPERTY_STUDY_VARIABLE)));
                        } else {
                            if (sv.getName().equals("undefined")) {
                                if (undefinedDefined) {
                                    errorList.add(new MZTabError(
                                            LogicalErrorType.UndefinedStudyVariableNameOnceOnly, -1,
                                            Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]" + "\t<" + StudyVariable.JSON_PROPERTY_NAME
                                            + ">").location(Locations.metadataKey(Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]")));
                                } else {
                                    undefinedDefined = true;
                                }
                            }
                        }
                        if (sv.
                                getDescription() == null) {
                            errorList.add(new MZTabError(
                                    LogicalErrorType.NotDefineInMetadata, -1,
                                    Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]-" + StudyVariable.JSON_PROPERTY_DESCRIPTION).location(Locations.metadataGroup(Metadata.JSON_PROPERTY_STUDY_VARIABLE)));
                        }
                        if (sv.
                                getAssayRefs() == null || sv.getAssayRefs().
                                        isEmpty()) {
                            errorList.add(new MZTabError(
                                    LogicalErrorType.AssayRefs, -1,
                                    Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]-" + StudyVariable.JSON_PROPERTY_ASSAY_REFS).location(Locations.metadataGroup(Metadata.JSON_PROPERTY_STUDY_VARIABLE)));
                        }
                    }
                }
            }
        }

        return errorList;
    }

    /**
     * The single study variable "undefined" stands for software that does not
     * capture study variables and MUST link to all assays.
     */
    private static void checkUndefinedLinksAllAssays(Integer id,
            StudyVariable sv, SortedMap<Integer, Assay> assayMap,
            List<MZTabError> errorList) {
        Set<Integer> referenced = sv.getAssayRefs().stream().
                map(Assay::getId).
                collect(Collectors.toSet());
        String missing = assayMap.keySet().stream().
                filter(assayId -> !referenced.contains(assayId)).
                map(assayId -> Metadata.JSON_PROPERTY_ASSAY + "[" + assayId + "]").
                collect(Collectors.joining(", "));
        if (!missing.isEmpty()) {
            String key = Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]";
            errorList.add(new MZTabError(
                    LogicalErrorType.UndefinedStudyVariableAssayRefs, -1,
                    key, missing).location(Locations.metadataKey(key + "-" + StudyVariable.JSON_PROPERTY_ASSAY_REFS)));
        }
    }

}
