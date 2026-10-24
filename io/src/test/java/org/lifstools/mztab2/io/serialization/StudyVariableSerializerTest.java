/*
 * Copyright 2018 LIFS Tools Consortium.
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
package org.lifstools.mztab2.io.serialization;

import com.fasterxml.jackson.databind.ObjectWriter;
import org.lifstools.mztab2.io.AbstractSerializerTest;
import org.lifstools.mztab2.io.TestResources;
import org.lifstools.mztab2.io.serialization.ParameterConverter;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import static org.lifstools.mztab2.model.Metadata.PrefixEnum.MTD;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.StudyVariable;
import org.junit.jupiter.api.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.BAR;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 * @author nilshoffmann
 */
public class StudyVariableSerializerTest extends AbstractSerializerTest {

    public StudyVariableSerializerTest() {
    }

    /**
     * Test of serializeSingle method, of class StudyVariableSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        Metadata mtd = new Metadata();
        Assay assay1 = new Assay().id(1);
        Assay assay2 = new Assay().id(2);
        StudyVariable studyVariable1 = new StudyVariable().
            id(1).
            name("Study Variable 1").
            description(
                "Group A").
            addAssayRefsItem(
                assay1).
            addAssayRefsItem(assay2).
            variationFunction(new Parameter().cvLabel("MS").
                cvAccession("MS:1002885").
                name("standard error")).
            averageFunction(new Parameter().cvLabel("MS").
                cvAccession("MS:1002883").
                name("median"));
        mtd.addStudyVariableItem(studyVariable1);
        StudyVariable studyVariable2 = new StudyVariable().
            id(2).
            name("Study Variable 2").
            description("Group B").
            addAssayRefsItem(assay1).
            addAssayRefsItem(assay2).
            variationFunction(new Parameter().cvLabel("MS").
                cvAccession("MS:1002885").
                name("standard error")).
            averageFunction(new Parameter().cvLabel("MS").
                cvAccession("MS:1002883").
                name("median"));

        mtd.addStudyVariableItem(studyVariable2);

        ObjectWriter writer = metaDataWriter();
        assertEqSentry(TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[1]" + TAB_STRING + studyVariable1.
                getName()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[1]-" + StudyVariable.JSON_PROPERTY_DESCRIPTION + TAB_STRING + studyVariable1.
                getDescription()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[1]-" + StudyVariable.JSON_PROPERTY_AVERAGE_FUNCTION + TAB_STRING + new ParameterConverter().
                convert(studyVariable1.getAverageFunction())
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[1]-" + StudyVariable.JSON_PROPERTY_VARIATION_FUNCTION + TAB_STRING + new ParameterConverter().
                convert(studyVariable1.getVariationFunction())
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[1]-" + StudyVariable.JSON_PROPERTY_ASSAY_REFS + TAB_STRING + Metadata.JSON_PROPERTY_ASSAY + "[1]" + BAR + Metadata.JSON_PROPERTY_ASSAY + "[2]"
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[2]" + TAB_STRING + studyVariable2.
                getName()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[2]-" + StudyVariable.JSON_PROPERTY_DESCRIPTION + TAB_STRING + studyVariable2.
                getDescription()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[2]-" + StudyVariable.JSON_PROPERTY_AVERAGE_FUNCTION + TAB_STRING + new ParameterConverter().
                convert(studyVariable2.getAverageFunction())
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[2]-" + StudyVariable.JSON_PROPERTY_VARIATION_FUNCTION + TAB_STRING + new ParameterConverter().
                convert(studyVariable2.getVariationFunction())
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[2]-" + StudyVariable.JSON_PROPERTY_ASSAY_REFS + TAB_STRING + Metadata.JSON_PROPERTY_ASSAY + "[1]" + BAR + Metadata.JSON_PROPERTY_ASSAY + "[2]"
            + NEW_LINE,
            serializeSingle(writer, mtd));
    }

}
