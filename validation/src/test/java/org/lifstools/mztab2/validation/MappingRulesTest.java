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
package org.lifstools.mztab2.validation;

import info.psidev.cvmapping.CvMapping;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import org.lifstools.mztab2.model.ValidationMessage;

/**
 * Tests the rules of the default mapping file, mzTab-M-mapping.xml, without
 * network access.
 *
 * @author nilshoffmann
 */
public class MappingRulesTest {

    private static final StubLookupService LOOKUP = new StubLookupService(Map.of(
        "MS:1001805", Set.of("MS:1001844", "MS:1002887"),
        "MS:1003303", Set.of("MS:1003306"),
        "MS:1002888", Set.of("MS:1002890"),
        "NCIT:C48207", Set.of("NCIT:C67432"),
        "NCIT:C48453", Set.of("NCIT:C48512"),
        "UO:0000051", Set.of("UO:0000065")));

    private static final List<String> UNIT_RULES = List.of(
        "small_molecule_quantification_unit_must",
        "small_molecule_feature_quantification_unit_must");

    private static final List<String> CONFIDENCE_RULES = List.of(
        "id_confidence_measure_must", "best_id_confidence_measure_must");

    private MzTab mzTab;

    @BeforeEach
    public void setUp() {
        mzTab = MzTabValidatorTest.createTestFile();
        mzTab.getMetadata().
            smallMoleculeQuantificationUnit(param("UO", "UO:0000065", "nanomolar")).
            smallMoleculeFeatureQuantificationUnit(param("UO", "UO:0000065", "nanomolar")).
            addIdConfidenceMeasureItem(param("MS", "MS:1002890", "fragmentation score"));
        mzTab.addSmallMoleculeSummaryItem(new SmallMoleculeSummary().smlId(1).
            bestIdConfidenceMeasure(param("MS", "MS:1002890", "fragmentation score")));
    }

    @Test
    public void quantificationUnitsMayBeUoNcitOrQuantificationDatatypes() {
        for (Parameter unit : List.of(
            param("UO", "UO:0000065", "nanomolar"),
            param("NCIT", "NCIT:C67432", "Nanomole per Liter"),
            param("NCIT", "NCIT:C48512", "Mole"),
            param("MS", "MS:1001844", "MS1 feature area"),
            param("MS", "MS:1002887", "Progenesis QI normalised abundance"))) {
            mzTab.getMetadata().smallMoleculeQuantificationUnit(unit).
                smallMoleculeFeatureQuantificationUnit(unit);
            List<ValidationMessage> ms = messagesOf(UNIT_RULES);
            assertTrue(ms.isEmpty(), () -> unit + ": " + ms);
        }
    }

    @Test
    public void aQuantificationUnitOutsideTheRulesIsStillAnError() {
        Parameter unit = param("MS", "MS:1000040", "m/z");
        mzTab.getMetadata().smallMoleculeQuantificationUnit(unit).
            smallMoleculeFeatureQuantificationUnit(unit);
        List<ValidationMessage> ms = messagesOf(UNIT_RULES);
        assertFalse(ms.isEmpty());
        ms.forEach(m -> assertTrue(m.getCode().startsWith("[Error-"), m.getCode()));
    }

    @Test
    public void spectralSimilarityScoresAreConfidenceMeasures() {
        Parameter score = param("MS", "MS:1003306", "shared peak count");
        mzTab.getMetadata().addIdConfidenceMeasureItem(score);
        mzTab.getSmallMoleculeSummary().get(0).bestIdConfidenceMeasure(score);
        List<ValidationMessage> ms = messagesOf(CONFIDENCE_RULES);
        assertTrue(ms.isEmpty(), () -> ms.toString());
    }

    @Test
    public void rulesOnAnAbsentTableDoNotApply() {
        // a summary-only file: no evidence table
        List<ValidationMessage> ms = messagesOf(List.of("ms_level_must",
            "identification_method_must", "derivatized_form_may"));
        assertTrue(ms.isEmpty(), () -> ms.toString());
        mzTab.getSmallMoleculeSummary().clear();
        List<ValidationMessage> sms = messagesOf(CONFIDENCE_RULES);
        assertTrue(sms.isEmpty(), () -> sms.toString());
    }

    @Test
    public void rulesOnAPresentTableStillApply() {
        mzTab.addSmallMoleculeEvidenceItem(new SmallMoleculeEvidence().smeId(1).
            msLevel(param("MS", "MS:1000040", "m/z")));
        List<ValidationMessage> ms = messagesOf(List.of("ms_level_must"));
        assertEquals(1, ms.size(), () -> ms.toString());
        assertTrue(ms.get(0).getCode().startsWith("[Error-3005]"), ms.get(0).getCode());
    }

    @Test
    public void tableAbsenceIsDecidedByTheElementPath() {
        assertTrue(CvMappingValidator.isTableAbsent(mzTab, "/smallMoleculeEvidence/@msLevel"));
        assertTrue(CvMappingValidator.isTableAbsent(mzTab, "/smallMoleculeFeature/@charge"));
        assertFalse(CvMappingValidator.isTableAbsent(mzTab, "/smallMoleculeSummary/@bestIdConfidenceMeasure"));
        assertFalse(CvMappingValidator.isTableAbsent(mzTab, "/metadata/sample/@species"));
    }

    private List<ValidationMessage> messagesOf(List<String> ruleIds) {
        return CvMappingValidator.of(mapping(), LOOKUP, true).
            validate(mzTab).stream().
            filter(m -> m.getRuleId() != null && ruleIds.contains(m.getRuleId())).
            collect(Collectors.toList());
    }

    private static CvMapping mapping() {
        try {
            return (CvMapping) JAXBContext.newInstance(CvMapping.class).
                createUnmarshaller().
                unmarshal(MappingRulesTest.class.getResource(
                    "/mappings/mzTab-M-mapping.xml"));
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Parameter param(String cvLabel, String accession, String name) {
        return new Parameter().cvLabel(cvLabel).cvAccession(accession).name(name);
    }
}
