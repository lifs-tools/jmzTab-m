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

import org.lifstools.mztab2.io.validators.StudyVariableValidator;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.StudyVariable;
import java.util.Arrays;
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
public class StudyVariableValidatorTest {

    /**
     * Test of validateRefine method, of class StudyVariableValidator.
     */
    @Test
    public void testValidateRefine() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        StudyVariableValidator instance = new StudyVariableValidator();

        List<MZTabError> expResult = Arrays.asList(new MZTabError(
                LogicalErrorType.SingleStudyVariableName, -1));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

    @Test
    public void testSingleStudyVariableNoNameMultipleAssays() {
        Metadata metadata = new Metadata();
        metadata.addAssayItem(new Assay().id(1));
        metadata.addAssayItem(new Assay().id(2));
        metadata.addStudyVariableItem(new StudyVariable().id(1).
                addAssayRefsItem(metadata.getAssay().get(0)).
                addAssayRefsItem(metadata.getAssay().get(1)));
        MZTabParserContext parserContext = new MZTabParserContext();
        parserContext.addAssay(metadata, metadata.getAssay().get(0));
        parserContext.addAssay(metadata, metadata.getAssay().get(1));
        parserContext.addStudyVariable(metadata, metadata.getStudyVariable().get(0));
        StudyVariableValidator instance = new StudyVariableValidator();
        List<MZTabError> expResult = Arrays.asList(
                new MZTabError(
                        LogicalErrorType.SingleStudyVariableName, -1)
        );
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

    @Test
    public void testTwoStudyVariableMultipleAssaysMissingNamesAndMissingDescription() {
        Metadata metadata = new Metadata();
        metadata.addAssayItem(new Assay().id(1));
        metadata.addAssayItem(new Assay().id(2));
        metadata.addAssayItem(new Assay().id(3));
        metadata.addAssayItem(new Assay().id(4));
        metadata.addStudyVariableItem(new StudyVariable().id(1).description("A description").
                addAssayRefsItem(metadata.getAssay().get(0)).
                addAssayRefsItem(metadata.getAssay().get(1)));
        metadata.addStudyVariableItem(new StudyVariable().id(2).
                addAssayRefsItem(metadata.getAssay().get(2)).
                addAssayRefsItem(metadata.getAssay().get(3)));
        MZTabParserContext parserContext = new MZTabParserContext();
        parserContext.addAssay(metadata, metadata.getAssay().get(0));
        parserContext.addAssay(metadata, metadata.getAssay().get(1));
        parserContext.addAssay(metadata, metadata.getAssay().get(2));
        parserContext.addAssay(metadata, metadata.getAssay().get(3));
        parserContext.addStudyVariable(metadata, metadata.getStudyVariable().get(0));
        parserContext.addStudyVariable(metadata, metadata.getStudyVariable().get(1));
        StudyVariableValidator instance = new StudyVariableValidator();
        List<MZTabError> expResult = Arrays.asList(
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + 1 + "]" + "\t<" + StudyVariable.JSON_PROPERTY_NAME+">"),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + 2 + "]" + "\t<" + StudyVariable.JSON_PROPERTY_NAME+">"),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + 2 + "]-" + StudyVariable.JSON_PROPERTY_DESCRIPTION)
        );
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
        assertEquals(expResult.get(1).toString(), result.get(1).toString());
        assertEquals(expResult.get(2).toString(), result.get(2).toString());
    }

    @Test
    public void testTwoStudyVariableMultipleAssaysMultipleUndefinedStudyVariables() {
        Metadata metadata = new Metadata();
        metadata.addAssayItem(new Assay().id(1));
        metadata.addAssayItem(new Assay().id(2));
        metadata.addAssayItem(new Assay().id(3));
        metadata.addAssayItem(new Assay().id(4));
        metadata.addStudyVariableItem(new StudyVariable().id(1).name("undefined").description("A description").
                addAssayRefsItem(metadata.getAssay().get(0)).
                addAssayRefsItem(metadata.getAssay().get(1)));
        metadata.addStudyVariableItem(new StudyVariable().id(2).name("undefined").description("Another description").
                addAssayRefsItem(metadata.getAssay().get(2)).
                addAssayRefsItem(metadata.getAssay().get(3)));
        MZTabParserContext parserContext = new MZTabParserContext();
        parserContext.addAssay(metadata, metadata.getAssay().get(0));
        parserContext.addAssay(metadata, metadata.getAssay().get(1));
        parserContext.addAssay(metadata, metadata.getAssay().get(2));
        parserContext.addAssay(metadata, metadata.getAssay().get(3));
        parserContext.addStudyVariable(metadata, metadata.getStudyVariable().get(0));
        parserContext.addStudyVariable(metadata, metadata.getStudyVariable().get(1));
        StudyVariableValidator instance = new StudyVariableValidator();
        List<MZTabError> expResult = Arrays.asList(
                new MZTabError(
                        LogicalErrorType.UndefinedStudyVariableNameOnceOnly, -1,
                        Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + 2 + "]" + "\t<" + StudyVariable.JSON_PROPERTY_NAME+">")
        );
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

    /** Assays 1..n and one study variable, all registered with the parser context. */
    private static List<MZTabError> validateSingle(StudyVariable sv, int assays) {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        for (int i = 1; i <= assays; i++) {
            // adds the assay to the metadata, too
            parserContext.addAssay(metadata, new Assay().id(i));
        }
        parserContext.addStudyVariable(metadata, sv);
        return new StudyVariableValidator().validateRefine(metadata, parserContext);
    }

    @Test
    public void testSingleUndefinedStudyVariableWithoutAssayRefs() {
        List<MZTabError> result = validateSingle(new StudyVariable().id(1).name("undefined").description("d"), 2);
        assertEquals(1, result.size(), () -> result.toString());
        assertEquals(LogicalErrorType.AssayRefs, result.get(0).getType());
        assertEquals("study_variable[1]", result.get(0).getLocation().getMetadataKey());
    }

    @Test
    public void testSingleStudyVariableThatIsNotTheFirst() {
        // used to throw a NullPointerException: svMap.get(1) is null
        List<MZTabError> result = validateSingle(new StudyVariable().id(2).name("undefined").description("d"), 1);
        assertEquals(1, result.size(), () -> result.toString());
        assertEquals(LogicalErrorType.AssayRefs, result.get(0).getType());
        assertTrue(result.get(0).getMessage().contains("study_variable[2]-assay_refs"), result.get(0).getMessage());
    }

    @Test
    public void testUndefinedStudyVariableMustLinkAllAssays() {
        StudyVariable sv = new StudyVariable().id(1).name("undefined").description("d").
                addAssayRefsItem(new Assay().id(1));
        List<MZTabError> result = validateSingle(sv, 3);
        assertEquals(1, result.size(), () -> result.toString());
        assertEquals(LogicalErrorType.UndefinedStudyVariableAssayRefs, result.get(0).getType());
        assertTrue(result.get(0).getMessage().contains("assay[2], assay[3]"), result.get(0).getMessage());
        assertEquals("study_variable[1]-assay_refs", result.get(0).getLocation().getMetadataKey());
    }

    @Test
    public void testUndefinedStudyVariableLinkingAllAssaysIsValid() {
        StudyVariable sv = new StudyVariable().id(1).name("undefined").description("d").
                addAssayRefsItem(new Assay().id(1)).addAssayRefsItem(new Assay().id(2));
        assertTrue(validateSingle(sv, 2).isEmpty());
    }

    @Test
    public void testStudyVariableAssayRefs() {
        Metadata metadata = new Metadata();
        metadata.addStudyVariableItem(new StudyVariable().id(1).name("undefined").description("A description"));
        MZTabParserContext parserContext = new MZTabParserContext();
        parserContext.addStudyVariable(metadata, metadata.getStudyVariable().get(0));
        StudyVariableValidator instance = new StudyVariableValidator();
        List<MZTabError> expResult = Arrays.asList(
                new MZTabError(
                        LogicalErrorType.AssayRefs, -1,
                        Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[" + 1 + "]-" + StudyVariable.JSON_PROPERTY_ASSAY_REFS)
        );
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());

    }

}
