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
package org.lifstools.mztab2.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.cvmapping.JxPathElement;
import org.lifstools.mztab2.model.Database;
import org.lifstools.mztab2.model.Instrument;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.Sample;
import org.lifstools.mztab2.model.SampleProcessing;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import org.lifstools.mztab2.model.Software;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.StudyVariableGroup;
import org.lifstools.mztab2.model.ValidationLocation;
import org.lifstools.mztab2.model.ValidationLocation.SectionEnum;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;

/**
 * Tests the mapping of model paths to mzTab locations.
 *
 * @author nilshoffmann
 */
public class ModelLocationsTest {

    private MzTab mzTab;

    @BeforeEach
    public void setUp() {
        mzTab = MzTabValidatorTest.createTestFile();
        Metadata metadata = mzTab.getMetadata();
        metadata.addSoftwareItem(new Software().id(1).
            parameter(param("MS", "MS:1001456", "analysis software")));
        metadata.addDatabaseItem(new Database().id(1).prefix("hmdb").
            param(param("MIRIAM", "MIR:00000051", "HMDB")));
        metadata.smallMoleculeQuantificationUnit(
            param("PRIDE", "PRIDE:0000330", "Arbitrary quantification unit"));
        mzTab.addSmallMoleculeSummaryItem(new SmallMoleculeSummary().smlId(1));
        mzTab.addSmallMoleculeSummaryItem(new SmallMoleculeSummary().smlId(2).
            bestIdConfidenceMeasure(param("MS", "MS:1002889", "Fake score")));
    }

    @Test
    public void sampleKeyUsesTheElementIdNotTheListPosition() {
        // first sample in the list has id 3
        mzTab.getMetadata().setSample(new ArrayList<>(List.of(
            new Sample().id(3).name("s3").addSpeciesItem(param("NEWT", "NEWT:9606", "Homo sapiens")))));
        Pair<Pointer, Parameter> p = select(mzTab, "/metadata/sample/@species").get(0);
        ValidationLocation l = ModelLocations.ofPointer(p.getKey());
        assertEquals("sample[3]-species[1]", l.getMetadataKey());
        assertEquals("sample", l.getMetadataGroup());
        assertEquals(SectionEnum.MTD, l.getSection());
        assertEquals(p.getKey().asPath(), l.getElementPath());
    }

    @Test
    public void msRunScalarProperty() {
        assertEquals("ms_run[1]-format", locate("/metadata/msRun/@format").getMetadataKey());
    }

    @Test
    public void selfNamedParameterLists() {
        assertEquals("software[1]", locate("/metadata/software/@parameter").getMetadataKey());
        assertEquals("database[1]", locate("/metadata/database/@param").getMetadataKey());
    }

    @Test
    public void studyVariableGroupParameterIsWrittenUnderTheGroupKey() {
        // written as MTD study_variable_group[2]  [PATO, PATO:0000383, sex, ]
        mzTab.getMetadata().addStudyVariableGroupItem(new StudyVariableGroup().
            id(2).parameter(param("PATO", "PATO:0000383", "sex")).
            description("Sex of the individual"));
        ValidationLocation l = locate("/metadata/studyVariableGroup/@parameter");
        assertEquals("study_variable_group[2]", l.getMetadataKey());
        assertEquals("study_variable_group", l.getMetadataGroup());
        assertEquals("study_variable_group[n]",
            ModelLocations.describeRulePath("/metadata/studyVariableGroup/@parameter"));
    }

    @Test
    public void scalarMetadata() {
        ValidationLocation l = locate("/metadata/@smallMoleculeQuantificationUnit");
        assertEquals("small_molecule-quantification_unit", l.getMetadataKey());
        assertEquals("general", l.getMetadataGroup());
    }

    @Test
    public void tableCell() {
        // two SML rows, second has a bestIdConfidenceMeasure parameter
        ValidationLocation l = locate("/smallMoleculeSummary/@bestIdConfidenceMeasure", 1);
        assertEquals(SectionEnum.SML, l.getSection());
        assertEquals(1, l.getRowIndex());
        assertEquals("best_id_confidence_measure", l.getColumn());
        assertNull(l.getMetadataKey());
    }

    @Test
    public void rulePathGroups() {
        ValidationLocation sample = ModelLocations.ofRulePath("/metadata/sample/@species");
        assertEquals("sample", sample.getMetadataGroup());
        assertNull(sample.getElementPath());
        ValidationLocation scalar = ModelLocations.ofRulePath("/metadata/@quantificationMethod");
        assertEquals("general", scalar.getMetadataGroup());
        assertNull(scalar.getElementPath());
        assertEquals("id_confidence_measure", ModelLocations.ofRulePath("/metadata/@idConfidenceMeasure").getMetadataGroup());
        ValidationLocation t = ModelLocations.ofRulePath("/smallMoleculeEvidence/@msLevel");
        assertEquals(SectionEnum.SME, t.getSection());
        assertEquals("ms_level", t.getColumn());
        assertNull(t.getRowIndex());
        assertNull(t.getElementPath());
    }

    @Test
    public void describeTexts() {
        assertEquals("sample[n]-species", ModelLocations.describeRulePath("/metadata/sample/@species"));
        assertEquals("best_id_confidence_measure in small molecule summary row 2",
            ModelLocations.describe(Locations.tableCell(SectionEnum.SML, 1, "best_id_confidence_measure")));
        assertEquals("quantification_method", ModelLocations.describeRulePath("/metadata/@quantificationMethod"));
        assertEquals("derivatization_agent[n]", ModelLocations.describeRulePath("/metadata/@derivatizationAgent"));
        assertEquals("ms_level in small molecule evidence", ModelLocations.describeRulePath("/smallMoleculeEvidence/@msLevel"));
        assertEquals("instrument[2]-name", ModelLocations.describe(Locations.metadataKey("instrument[2]-name")));
    }

    @Test
    public void everyDefaultMappingPathMapsToItsKey() {
        Metadata m = mzTab.getMetadata();
        m.getMsRun().get(0).id(7).
            fragmentationMethod(new ArrayList<>(List.of(param("MS", "MS:1000133", "CID")))).
            hashMethod(param("MS", "MS:1000569", "SHA-1"));
        m.addSampleProcessingItem(new SampleProcessing().id(2).
            addSampleProcessingItem(param("MS", "MS:1000001", "a")).
            addSampleProcessingItem(param("MS", "MS:1000002", "b")));
        m.addInstrumentItem(new Instrument().id(5).
            name(param("MS", "MS:1000003", "c")).
            source(param("MS", "MS:1000004", "d")).
            addAnalyzerItem(param("MS", "MS:1000005", "e")).
            detector(param("MS", "MS:1000006", "f")));
        m.setSample(new ArrayList<>(List.of(new Sample().id(4).
            addSpeciesItem(param("NEWT", "NEWT:9606", "Homo sapiens")).
            addTissueItem(param("BTO", "BTO:0000759", "liver")).
            addCellTypeItem(param("CL", "CL:0000182", "hepatocyte")).
            addDiseaseItem(param("DOID", "DOID:684", "hepatocellular carcinoma")))));
        m.addStudyVariableItem(new StudyVariable().id(6).name("sv").
            averageFunction(param("MS", "MS:1002883", "median")).
            variationFunction(param("MS", "MS:1002885", "standard error")));
        m.quantificationMethod(param("MS", "MS:1001834", "LC-MS label-free quantitation analysis"));
        m.smallMoleculeFeatureQuantificationUnit(param("PRIDE", "PRIDE:0000330", "Arbitrary quantification unit"));
        m.smallMoleculeIdentificationReliability(param("MS", "MS:1002896", "compound identification confidence level"));
        // the first agent has no id: numbered by its list position; the second has id 5
        m.addDerivatizationAgentItem(param("XLMOD", "XLMOD:07014", "TMS")).
            addDerivatizationAgentItem(param("XLMOD", "XLMOD:07015", "TBDMS").id(5));
        m.addIdConfidenceMeasureItem(param("MS", "MS:1002889", "Fake score"));
        mzTab.addSmallMoleculeEvidenceItem(new SmallMoleculeEvidence().smeId(1).
            derivatizedForm(param("XLMOD", "XLMOD:07014", "TMS")).
            msLevel(param("MS", "MS:1000511", "ms level")).
            identificationMethod(param("MS", "MS:1001562", "Scaffold")));

        assertEquals("ms_run[7]-format", locate("/metadata/msRun/@format").getMetadataKey());
        assertEquals("ms_run[7]-id_format", locate("/metadata/msRun/@idFormat").getMetadataKey());
        assertEquals("ms_run[7]-hash_method", locate("/metadata/msRun/@hashMethod").getMetadataKey());
        assertEquals("ms_run[7]-scan_polarity[2]", locate("/metadata/msRun/@scanPolarity", 1).getMetadataKey());
        assertEquals("ms_run[7]-fragmentation_method[1]", locate("/metadata/msRun/@fragmentationMethod").getMetadataKey());
        assertEquals("instrument[5]-name", locate("/metadata/instrument/@name").getMetadataKey());
        assertEquals("instrument[5]-source", locate("/metadata/instrument/@source").getMetadataKey());
        assertEquals("instrument[5]-detector", locate("/metadata/instrument/@detector").getMetadataKey());
        assertEquals("instrument[5]-analyzer[1]", locate("/metadata/instrument/@analyzer").getMetadataKey());
        assertEquals("sample[4]-species[1]", locate("/metadata/sample/@species").getMetadataKey());
        assertEquals("sample[4]-tissue[1]", locate("/metadata/sample/@tissue").getMetadataKey());
        assertEquals("sample[4]-cell_type[1]", locate("/metadata/sample/@cellType").getMetadataKey());
        assertEquals("sample[4]-disease[1]", locate("/metadata/sample/@disease").getMetadataKey());
        assertEquals("study_variable[6]-average_function", locate("/metadata/studyVariable/@averageFunction").getMetadataKey());
        assertEquals("study_variable[6]-variation_function", locate("/metadata/studyVariable/@variationFunction").getMetadataKey());
        ValidationLocation sampleProcessing = locate("/metadata/sampleProcessing/@sampleProcessing", 1);
        assertEquals("sample_processing[2]", sampleProcessing.getMetadataKey());
        assertEquals("sample_processing", sampleProcessing.getMetadataGroup());
        assertEquals("quantification_method", locate("/metadata/@quantificationMethod").getMetadataKey());
        assertEquals("small_molecule_feature-quantification_unit", locate("/metadata/@smallMoleculeFeatureQuantificationUnit").getMetadataKey());
        assertEquals("small_molecule-identification_reliability", locate("/metadata/@smallMoleculeIdentificationReliability").getMetadataKey());
        assertEquals("derivatization_agent[1]", locate("/metadata/@derivatizationAgent", 0).getMetadataKey());
        ValidationLocation agent = locate("/metadata/@derivatizationAgent", 1);
        assertEquals("derivatization_agent[5]", agent.getMetadataKey());
        assertEquals("derivatization_agent", agent.getMetadataGroup());
        assertEquals("id_confidence_measure[1]", locate("/metadata/@idConfidenceMeasure").getMetadataKey());
        Map.of("derivatizedForm", "derivatized_form", "msLevel", "ms_level",
            "identificationMethod", "identification_method").forEach((property, column) -> {
            ValidationLocation cell = locate("/smallMoleculeEvidence/@" + property);
            assertEquals(SectionEnum.SME, cell.getSection());
            assertEquals(0, cell.getRowIndex());
            assertEquals(column, cell.getColumn());
        });
    }

    @Test
    public void elementWithoutIdKeepsGroupAndPathButNoKey() {
        // the second sample has no id, so its key cannot be named
        mzTab.getMetadata().addSampleItem(new Sample().
            addCustomItem(param("MS", "MS:1000001", "a")));
        // the first sample has two custom parameters
        ValidationLocation l = locate("/metadata/sample/@custom", 2);
        assertNull(l.getMetadataKey());
        assertEquals("sample", l.getMetadataGroup());
        assertEquals("/metadata/sample[2]/custom[1]", l.getElementPath());
    }

    private ValidationLocation locate(String path) {
        return locate(path, 0);
    }

    private ValidationLocation locate(String path, int index) {
        return ModelLocations.ofPointer(select(mzTab, path).get(index).getKey());
    }

    private static List<Pair<Pointer, Parameter>> select(MzTab mzTab, String path) {
        return JxPathElement.toList(JXPathContext.newContext(mzTab), path, Parameter.class);
    }

    private static Parameter param(String cvLabel, String accession, String name) {
        return new Parameter().cvLabel(cvLabel).cvAccession(accession).name(name);
    }
}
