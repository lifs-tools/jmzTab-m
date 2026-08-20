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
package org.lifstools.mztab2.io.serialization;

import com.fasterxml.jackson.databind.ObjectWriter;
import org.lifstools.mztab2.io.AbstractSerializerTest;
import org.lifstools.mztab2.io.TestResources;
import org.lifstools.mztab2.model.Metadata;
import static org.lifstools.mztab2.model.Metadata.PrefixEnum.MTD;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.StudyVariableGroup;
import org.junit.jupiter.api.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.BAR_S;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 * @author nilshoffmann
 */
public class StudyVariableGroupSerializerTest extends AbstractSerializerTest {

    public StudyVariableGroupSerializerTest() {
    }

    @Test
    public void testSerializeMandatoryFieldsOnly() throws Exception {
        Metadata mtd = new Metadata();
        Parameter sexParam = new Parameter().cvLabel("PATO").
            cvAccession("PATO:0000383").
            name("sex");
        StudyVariableGroup group1 = new StudyVariableGroup().
            id(1).
            parameter(sexParam).
            description("Sex of the individual");
        mtd.addStudyVariableGroupItem(group1);

        ObjectWriter writer = metaDataWriter();
        ParameterConverter pc = new ParameterConverter();
        assertEqSentry(
            TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]" + TAB_STRING + pc.convert(sexParam)
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_DESCRIPTION + TAB_STRING + group1.getDescription()
            + NEW_LINE,
            serializeSingle(writer, mtd));
    }

    @Test
    public void testSerializeAllFields() throws Exception {
        Metadata mtd = new Metadata();
        Parameter timeParam = new Parameter().cvLabel("PATO").
            cvAccession("PATO:0000165").
            name("time");
        Parameter typeParam = new Parameter().cvLabel("STATO").
            cvAccession("STATO:0000251").
            name("continuous variable");
        Parameter unitParam = new Parameter().cvLabel("UO").
            cvAccession("UO:0000010").
            name("second");
        StudyVariableGroup group1 = new StudyVariableGroup().
            id(1).
            parameter(timeParam).
            description("Time point after treatment").
            type(typeParam).
            datatype("xsd:decimal").
            unit(unitParam);
        mtd.addStudyVariableGroupItem(group1);

        ObjectWriter writer = metaDataWriter();
        ParameterConverter pc = new ParameterConverter();
        assertEqSentry(
            TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]" + TAB_STRING + pc.convert(timeParam)
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_DESCRIPTION + TAB_STRING + group1.getDescription()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_TYPE + TAB_STRING + pc.convert(typeParam)
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_DATATYPE + TAB_STRING + group1.getDatatype()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_UNIT + TAB_STRING + pc.convert(unitParam)
            + NEW_LINE,
            serializeSingle(writer, mtd));
    }

    @Test
    public void testSerializeMultipleGroups() throws Exception {
        Metadata mtd = new Metadata();
        Parameter sexParam = new Parameter().cvLabel("PATO").
            cvAccession("PATO:0000383").
            name("sex");
        Parameter timeParam = new Parameter().cvLabel("PATO").
            cvAccession("PATO:0000165").
            name("time");
        StudyVariableGroup group1 = new StudyVariableGroup().
            id(1).
            parameter(sexParam).
            description("Sex of the individual").
            type(new Parameter().cvLabel("STATO").
                cvAccession("STATO:0000252").
                name("categorical variable")).
            datatype("xsd:string");
        StudyVariableGroup group2 = new StudyVariableGroup().
            id(2).
            parameter(timeParam).
            description("Time point after treatment").
            type(new Parameter().cvLabel("STATO").
                cvAccession("STATO:0000251").
                name("continuous variable")).
            datatype("xsd:decimal");
        mtd.addStudyVariableGroupItem(group1);
        mtd.addStudyVariableGroupItem(group2);

        ObjectWriter writer = metaDataWriter();
        ParameterConverter pc = new ParameterConverter();
        assertEqSentry(
            TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]" + TAB_STRING + pc.convert(sexParam)
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_DESCRIPTION + TAB_STRING + group1.getDescription()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_TYPE + TAB_STRING + pc.convert(group1.getType())
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_DATATYPE + TAB_STRING + group1.getDatatype()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[2]" + TAB_STRING + pc.convert(timeParam)
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[2]-" + StudyVariableGroup.JSON_PROPERTY_DESCRIPTION + TAB_STRING + group2.getDescription()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[2]-" + StudyVariableGroup.JSON_PROPERTY_TYPE + TAB_STRING + pc.convert(group2.getType())
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[2]-" + StudyVariableGroup.JSON_PROPERTY_DATATYPE + TAB_STRING + group2.getDatatype()
            + NEW_LINE,
            serializeSingle(writer, mtd));
    }

    @Test
    public void testSerializeStudyVariableRefs() throws Exception {
        Metadata mtd = new Metadata();
        Parameter sexParam = new Parameter().cvLabel("PATO").
            cvAccession("PATO:0000383").
            name("sex");
        StudyVariableGroup group1 = new StudyVariableGroup().
            id(1).
            parameter(sexParam).
            description("Sex of the individual").
            addStudyVariableRefsItem(new StudyVariable().id(1).name("Female")).
            addStudyVariableRefsItem(new StudyVariable().id(2).name("Male"));
        mtd.addStudyVariableGroupItem(group1);

        ObjectWriter writer = metaDataWriter();
        ParameterConverter pc = new ParameterConverter();
        assertEqSentry(
            TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]" + TAB_STRING + pc.convert(sexParam)
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_DESCRIPTION + TAB_STRING + group1.getDescription()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_STUDY_VARIABLE_GROUP + "[1]-" + StudyVariableGroup.JSON_PROPERTY_STUDY_VARIABLE_REFS + TAB_STRING
            + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[1]" + BAR_S + Metadata.JSON_PROPERTY_STUDY_VARIABLE + "[2]"
            + NEW_LINE,
            serializeSingle(writer, mtd));
    }
}
