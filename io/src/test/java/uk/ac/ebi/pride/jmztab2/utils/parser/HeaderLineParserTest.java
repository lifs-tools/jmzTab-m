/*
 * Copyright 2026 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package uk.ac.ebi.pride.jmztab2.utils.parser;

import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.StudyVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab2.model.ISmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

public class HeaderLineParserTest {

    private static String smh(String... extra) {
        List<String> cells = new ArrayList<>();
        cells.add("SMH");
        for (ISmallMoleculeColumn column : SmallMoleculeColumn.Stable.columns()) {
            cells.add(column.getHeader());
        }
        cells.addAll(List.of(extra));
        return String.join("\t", cells);
    }

    private static String sfh(String... extra) {
        List<String> cells = new ArrayList<>();
        cells.add("SFH");
        for (ISmallMoleculeFeatureColumn column : SmallMoleculeFeatureColumn.Stable.columns()) {
            cells.add(column.getName());
        }
        cells.addAll(List.of(extra));
        return String.join("\t", cells);
    }

    private static List<String> messages(MZTabErrorList errorList, MZTabErrorType type) {
        return errorList.getErrorList().stream()
            .filter(e -> e.getType() == type)
            .map(MZTabError::getMessage)
            .collect(Collectors.toList());
    }

    @Test
    public void allMissingSmlAbundanceColumnsAreReportedAssaysFirst() throws MZTabException {
        MZTabParserContext context = new MZTabParserContext();
        Metadata metadata = new Metadata();
        metadata.setSmallMoleculeQuantificationUnit(new Parameter().name("qty"));
        metadata.setSmallMoleculeIdentificationReliability(new Parameter().name("reliability"));
        context.addAssay(metadata, new Assay().id(1).name("assay 1"));
        context.addAssay(metadata, new Assay().id(2).name("assay 2"));
        context.addStudyVariable(metadata, new StudyVariable().id(1).name("Group A"));
        context.addStudyVariable(metadata, new StudyVariable().id(2).name("Group B"));
        MZTabErrorList errorList = new MZTabErrorList();

        new SMHLineParser(context, metadata).parse(1, smh(
            "abundance_assay[1]", "abundance_study_variable[1]", "abundance_variation_study_variable[1]"),
            errorList);

        List<String> messages = messages(errorList, LogicalErrorType.NotDefineInHeader);
        assertEquals(messages.toString(), 3, messages.size());
        assertTrue(messages.get(0), messages.get(0).contains("abundance_assay[2]"));
        assertTrue(messages.get(1), messages.get(1).contains("abundance_study_variable[2]"));
        assertTrue(messages.get(2), messages.get(2).contains("abundance_variation_study_variable[2]"));
    }

    @Test
    public void missingIdentificationReliabilityDoesNotStopHeaderValidation() throws MZTabException {
        MZTabParserContext context = new MZTabParserContext();
        Metadata metadata = new Metadata();
        metadata.setSmallMoleculeQuantificationUnit(new Parameter().name("qty"));
        context.addAssay(metadata, new Assay().id(1).name("assay 1"));
        context.addAssay(metadata, new Assay().id(2).name("assay 2"));
        MZTabErrorList errorList = new MZTabErrorList(MZTabErrorType.Level.Info);

        new SMHLineParser(context, metadata).parse(1, smh("abundance_assay[1]"), errorList);

        assertEquals(1, messages(errorList, LogicalErrorType.NoSmallMoleculeIdentificationReliability).size());
        List<String> messages = messages(errorList, LogicalErrorType.NotDefineInHeader);
        assertEquals(messages.toString(), 1, messages.size());
        assertTrue(messages.get(0), messages.get(0).contains("abundance_assay[2]"));
    }

    @Test
    public void allMissingSmfAbundanceColumnsAreReported() throws MZTabException {
        MZTabParserContext context = new MZTabParserContext();
        Metadata metadata = new Metadata();
        metadata.setSmallMoleculeFeatureQuantificationUnit(new Parameter().name("qty"));
        context.addAssay(metadata, new Assay().id(1).name("assay 1"));
        context.addAssay(metadata, new Assay().id(2).name("assay 2"));
        context.addAssay(metadata, new Assay().id(3).name("assay 3"));
        MZTabErrorList errorList = new MZTabErrorList();

        new SFHLineParser(context, metadata).parse(1, sfh("abundance_assay[2]"), errorList);

        List<String> messages = messages(errorList, LogicalErrorType.NotDefineInHeader);
        assertEquals(messages.toString(), 2, messages.size());
        assertTrue(messages.get(0), messages.get(0).contains("abundance_assay[1]"));
        assertTrue(messages.get(1), messages.get(1).contains("abundance_assay[3]"));
    }
}
