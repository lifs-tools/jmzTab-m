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

import org.lifstools.mztab2.io.validators.StudyVariableValidator;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.StudyVariable;
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
                        Metadata.Properties.studyVariable + "[" + 1 + "]" + "\t" + "<NAME>"),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.studyVariable + "[" + 2 + "]" + "\t" + "<NAME>"),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.studyVariable + "[" + 2 + "]-" + StudyVariable.Properties.description + "\t" + "<DESCRIPTION>")
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
                        Metadata.Properties.studyVariable + "[" + 2 + "]" + "\t" + "<NAME>")
        );
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
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
                        Metadata.Properties.studyVariable + "[" + 1 + "]-" + StudyVariable.Properties.assayRefs)
        );
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());

    }

}
