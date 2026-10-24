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
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.StudyVariable;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Validates that every study variable lists its assays
 * (study_variable[1-n]-assay_refs is mandatory in mzTab-M 2.0), and that a
 * single study variable "undefined" links to all assays.
 */
public class StudyVariableAssayRefsValidator implements RefiningValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        SortedMap<Integer, Assay> assayMap = parserContext.getAssayMap();
        SortedMap<Integer, StudyVariable> svMap = parserContext.getStudyVariableMap();
        List<MZTabError> errorList = new LinkedList<>();
        svMap.forEach((id, sv) -> {
            if (sv == null) {
                return;
            }
            String key = Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + id + "]";
            if (sv.getAssayRefs() == null || sv.getAssayRefs().isEmpty()) {
                errorList.add(new MZTabError(
                        LogicalErrorType.AssayRefs, -1,
                        key + "-" + StudyVariable.JSON_PROPERTY_ASSAY_REFS).
                        location(sv.getName() != null
                                ? Locations.metadataKey(key)
                                : Locations.metadataGroup(Metadata.JSON_PROPERTY_STUDY_VARIABLE)));
            } else if (svMap.size() == 1 && "undefined".equals(sv.getName())) {
                checkUndefinedLinksAllAssays(key, sv, assayMap, errorList);
            }
        });
        return errorList;
    }

    /**
     * The single study variable "undefined" stands for software that does not
     * capture study variables and MUST link to all assays.
     */
    private static void checkUndefinedLinksAllAssays(String key,
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
            errorList.add(new MZTabError(
                    LogicalErrorType.UndefinedStudyVariableAssayRefs, -1,
                    key, missing).location(Locations.metadataKey(key + "-" + StudyVariable.JSON_PROPERTY_ASSAY_REFS)));
        }
    }

}
