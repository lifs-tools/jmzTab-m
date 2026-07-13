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
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.StudyVariableGroup;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 *
 * @author nilshoffmann
 */
public class StudyVariableGroupValidatorTest {

    private static final Parameter PATO_SEX = new Parameter().
        cvLabel("PATO").cvAccession("PATO:0000383").name("sex");
    private static final Parameter STATO_CATEGORICAL = new Parameter().
        cvLabel("STATO").cvAccession("STATO:0000252").name("categorical variable");

    private static StudyVariable studyVariable(int id) {
        return new StudyVariable().id(id).name("sv" + id).description("study variable " + id);
    }

    /** No groups declared → validator is a no-op, no errors expected. */
    @Test
    public void testEmptyGroupMapProducesNoErrors() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();

        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertTrue(result.isEmpty());
    }

    /** Valid group with all mandatory fields referencing a defined study variable → no errors. */
    @Test
    public void testValidGroupReferencingStudyVariable() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();

        StudyVariable sv1 = studyVariable(1);
        parserContext.addStudyVariable(metadata, sv1);
        StudyVariableGroup group = new StudyVariableGroup().id(1).
            parameter(PATO_SEX).
            description("Sex of the individual").
            addStudyVariableRefsItem(sv1);
        parserContext.addStudyVariableGroup(metadata, group);

        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertTrue(result.isEmpty(), result.toString());
    }

    /** Valid group with all optional fields populated → no errors. */
    @Test
    public void testValidGroupWithAllFields() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();

        StudyVariable sv1 = studyVariable(1);
        parserContext.addStudyVariable(metadata, sv1);
        StudyVariableGroup group = new StudyVariableGroup().id(1).
            parameter(PATO_SEX).
            description("Sex of the individual").
            type(STATO_CATEGORICAL).
            datatype("xsd:string").
            unit(new Parameter().cvLabel("UO").cvAccession("UO:0000189").name("count unit")).
            addStudyVariableRefsItem(sv1);
        parserContext.addStudyVariableGroup(metadata, group);

        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertTrue(result.isEmpty(), result.toString());
    }

    /** Group with null parameter → error for missing mandatory field. */
    @Test
    public void testMissingParameterProducesError() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();

        StudyVariable sv1 = studyVariable(1);
        parserContext.addStudyVariable(metadata, sv1);
        StudyVariableGroup group = new StudyVariableGroup().id(1).
            description("Sex of the individual").
            addStudyVariableRefsItem(sv1);
        parserContext.addStudyVariableGroup(metadata, group);

        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(1, result.size(), result.toString());
        assertEquals(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1,
            Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]"
            + "\t<" + StudyVariableGroup.JSON_PROPERTY_PARAMETER + ">").toString(),
            result.get(0).toString());
    }

    /** Group with null description → error for missing mandatory field. */
    @Test
    public void testMissingDescriptionProducesError() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();

        StudyVariable sv1 = studyVariable(1);
        parserContext.addStudyVariable(metadata, sv1);
        StudyVariableGroup group = new StudyVariableGroup().id(1).
            parameter(PATO_SEX).
            addStudyVariableRefsItem(sv1);
        parserContext.addStudyVariableGroup(metadata, group);

        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(1, result.size(), result.toString());
        assertEquals(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1,
            Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-"
            + StudyVariableGroup.JSON_PROPERTY_DESCRIPTION).toString(),
            result.get(0).toString());
    }

    /** Group type with a non-STATO cv label → error. */
    @Test
    public void testTypeWithNonStatoCvLabelProducesError() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();

        StudyVariable sv1 = studyVariable(1);
        parserContext.addStudyVariable(metadata, sv1);
        Parameter invalidType = new Parameter().cvLabel("MS").
            cvAccession("MS:1000001").name("some ms term");
        StudyVariableGroup group = new StudyVariableGroup().id(1).
            parameter(PATO_SEX).
            description("Sex of the individual").
            type(invalidType).
            addStudyVariableRefsItem(sv1);
        parserContext.addStudyVariableGroup(metadata, group);

        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(1, result.size(), result.toString());
        assertEquals(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1,
            Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-"
            + StudyVariableGroup.JSON_PROPERTY_TYPE
            + " (MUST be a STATO ontology term, e.g. [STATO, STATO:0000252, categorical variable, ])").toString(),
            result.get(0).toString());
    }

    /** Group with an unsupported xsd datatype value → error. */
    @Test
    public void testInvalidDatatypeProducesError() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();

        StudyVariable sv1 = studyVariable(1);
        parserContext.addStudyVariable(metadata, sv1);
        StudyVariableGroup group = new StudyVariableGroup().id(1).
            parameter(PATO_SEX).
            description("Sex of the individual").
            datatype("xsd:unsupported").
            addStudyVariableRefsItem(sv1);
        parserContext.addStudyVariableGroup(metadata, group);

        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(1, result.size(), result.toString());
        assertEquals(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1,
            Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-"
            + StudyVariableGroup.JSON_PROPERTY_DATATYPE
            + " (MUST be one of: xsd:string, xsd:integer, xsd:decimal, xsd:boolean, xsd:date, xsd:time, xsd:dateTime, xsd:anyURI)").toString(),
            result.get(0).toString());
    }

    /** Group without any study_variable_refs → error. */
    @Test
    public void testGroupWithoutStudyVariableRefsProducesError() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();

        StudyVariableGroup group = new StudyVariableGroup().id(1).
            parameter(PATO_SEX).
            description("Sex of the individual");
        parserContext.addStudyVariableGroup(metadata, group);

        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(1, result.size(), result.toString());
        assertEquals(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1,
            Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-"
            + StudyVariableGroup.JSON_PROPERTY_STUDY_VARIABLE_REFS).toString(),
            result.get(0).toString());
    }

    /** A study_variable_refs entry pointing to an undefined study variable → error. */
    @Test
    public void testUnknownStudyVariableRefProducesError() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();

        StudyVariableGroup group = new StudyVariableGroup().id(1).
            parameter(PATO_SEX).
            description("Sex of the individual").
            addStudyVariableRefsItem(new StudyVariable().id(99));
        parserContext.addStudyVariableGroup(metadata, group);

        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertTrue(result.stream().anyMatch(e -> e.getMessage().contains("study_variable[99]")),
            result.toString());
    }

    /** A study variable not referenced by any group → error once groups are defined. */
    @Test
    public void testUnreferencedStudyVariableProducesError() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();

        StudyVariable sv1 = studyVariable(1);
        StudyVariable sv2 = studyVariable(2);
        parserContext.addStudyVariable(metadata, sv1);
        parserContext.addStudyVariable(metadata, sv2);
        StudyVariableGroup group = new StudyVariableGroup().id(1).
            parameter(PATO_SEX).
            description("Sex of the individual").
            addStudyVariableRefsItem(sv1);
        parserContext.addStudyVariableGroup(metadata, group);

        StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(1, result.size(), result.toString());
        assertEquals(LogicalErrorType.StudyVariableNotDefined, result.get(0).getType());
        assertTrue(result.get(0).getMessage().contains("study_variable[2]"), result.toString());
    }

    /** All valid xsd datatypes are accepted without error. */
    @Test
    public void testAllValidXsdDatatypesAreAccepted() {
        String[] validTypes = {
            "xsd:string", "xsd:integer", "xsd:decimal", "xsd:boolean",
            "xsd:date", "xsd:time", "xsd:dateTime", "xsd:anyURI"
        };
        for (String datatype : validTypes) {
            Metadata metadata = new Metadata();
            MZTabParserContext parserContext = new MZTabParserContext();

            StudyVariable sv1 = studyVariable(1);
            parserContext.addStudyVariable(metadata, sv1);
            StudyVariableGroup group = new StudyVariableGroup().id(1).
                parameter(PATO_SEX).
                description("Sex of the individual").
                datatype(datatype).
                addStudyVariableRefsItem(sv1);
            parserContext.addStudyVariableGroup(metadata, group);

            StudyVariableGroupValidator instance = new StudyVariableGroupValidator();
            List<MZTabError> result = instance.validateRefine(metadata, parserContext);
            assertTrue(result.isEmpty(), "Expected no errors for datatype: " + datatype + " " + result);
        }
    }
}
