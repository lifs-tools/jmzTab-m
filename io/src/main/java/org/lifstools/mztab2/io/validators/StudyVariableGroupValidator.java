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

import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.StudyVariableGroup;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Validates that each study_variable_group entry has the required fields, that
 * optional fields conform to spec constraints, and that its study_variable_refs
 * reference defined study variables. Once study variable groups are defined,
 * every study variable MUST belong to exactly one group.
 *
 * @author nilshoffmann
 */
public class StudyVariableGroupValidator implements RefiningValidator<Metadata> {

    private static final String STATO_CV_LABEL = "STATO";
    private static final List<String> VALID_XSD_DATATYPES = Arrays.asList(
        "xsd:string", "xsd:integer", "xsd:decimal", "xsd:boolean",
        "xsd:date", "xsd:time", "xsd:dateTime", "xsd:anyURI"
    );

    @Override
    public List<MZTabError> validateRefine(Metadata metadata,
        MZTabParserContext parserContext) {
        SortedMap<Integer, StudyVariableGroup> svgMap = parserContext.
            getStudyVariableGroupMap();
        SortedMap<Integer, StudyVariable> svMap = parserContext.
            getStudyVariableMap();
        List<MZTabError> errorList = new LinkedList<>();

        if (svgMap.isEmpty()) {
            return errorList;
        }

        // how often each study variable is referenced across all groups
        Map<Integer, Integer> referenceCount = new HashMap<>();

        for (Integer id : svgMap.keySet()) {
            StudyVariableGroup svg = svgMap.get(id);
            if (svg == null) {
                errorList.add(new MZTabError(
                    LogicalErrorType.NotDefineInMetadata, -1,
                    Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[" + id + "]"));
                continue;
            }

            if (svg.getParameter() == null) {
                errorList.add(new MZTabError(
                    LogicalErrorType.NotDefineInMetadata, -1,
                    Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[" + id + "]"
                    + "\t<" + StudyVariableGroup.JSON_PROPERTY_PARAMETER + ">"));
            }

            if (svg.getDescription() == null || svg.getDescription().isEmpty()) {
                errorList.add(new MZTabError(
                    LogicalErrorType.NotDefineInMetadata, -1,
                    Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[" + id + "]-"
                    + StudyVariableGroup.JSON_PROPERTY_DESCRIPTION));
            }

            // type MUST be from STATO if provided
            if (svg.getType() != null
                && (svg.getType().getCvLabel() == null
                    || !STATO_CV_LABEL.equals(svg.getType().getCvLabel()))) {
                errorList.add(new MZTabError(
                    LogicalErrorType.NotDefineInMetadata, -1,
                    Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[" + id + "]-"
                    + StudyVariableGroup.JSON_PROPERTY_TYPE
                    + " (MUST be a STATO ontology term, e.g. [STATO, STATO:0000252, categorical variable, ])"));
            }

            // datatype MUST be one of the xsd: values if provided
            if (svg.getDatatype() != null && !svg.getDatatype().isEmpty()
                && !VALID_XSD_DATATYPES.contains(svg.getDatatype())) {
                errorList.add(new MZTabError(
                    LogicalErrorType.NotDefineInMetadata, -1,
                    Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[" + id + "]-"
                    + StudyVariableGroup.JSON_PROPERTY_DATATYPE
                    + " (MUST be one of: " + String.join(", ", VALID_XSD_DATATYPES) + ")"));
            }

            // every group MUST reference at least one study variable
            List<StudyVariable> refs = svg.getStudyVariableRefs();
            if (refs == null || refs.isEmpty()) {
                errorList.add(new MZTabError(
                    LogicalErrorType.NotDefineInMetadata, -1,
                    Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[" + id + "]-"
                    + StudyVariableGroup.JSON_PROPERTY_STUDY_VARIABLE_REFS));
                continue;
            }

            // every referenced study variable MUST be defined; replace the id-only
            // reference with the resolved study variable.
            for (int i = 0; i < refs.size(); i++) {
                Integer refId = refs.get(i).getId();
                StudyVariable referenced = svMap.get(refId);
                if (referenced == null) {
                    errorList.add(new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[" + id + "]-"
                        + StudyVariableGroup.JSON_PROPERTY_STUDY_VARIABLE_REFS
                        + "\t" + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + refId + "]"));
                } else {
                    refs.set(i, referenced);
                    referenceCount.merge(refId, 1, Integer::sum);
                }
            }
        }

        // once groups are defined, every study variable MUST belong to exactly
        // one group.
        for (Integer svId : svMap.keySet()) {
            int count = referenceCount.getOrDefault(svId, 0);
            if (count == 0) {
                errorList.add(new MZTabError(
                    LogicalErrorType.StudyVariableNotDefined, -1,
                    Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + svId + "]"
                    + " is not referenced by any study_variable_group[n]-study_variable_refs"));
            } else if (count > 1) {
                errorList.add(new MZTabError(
                    LogicalErrorType.DuplicationID, -1,
                    Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + svId + "]"
                    + " is referenced by more than one study_variable_group"));
            }
        }

        return errorList;
    }
}
