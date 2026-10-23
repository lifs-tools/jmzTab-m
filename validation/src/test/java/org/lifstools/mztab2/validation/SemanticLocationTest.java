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

import info.psidev.cvmapping.CvMapping;
import info.psidev.cvmapping.CvMappingRule;
import info.psidev.cvmapping.CvMappingRule.CvTermsCombinationLogic;
import info.psidev.cvmapping.CvMappingRule.RequirementLevel;
import info.psidev.cvmapping.CvReference;
import info.psidev.cvmapping.CvTerm;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.cvmapping.CvMappingUtils;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Instrument;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.Sample;
import org.lifstools.mztab2.model.ValidationMessage;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Tests locations, rule ids and fix texts of semantic validation messages,
 * without network access.
 *
 * @author nilshoffmann
 */
public class SemanticLocationTest {

    private static final StubLookupService LOOKUP = new StubLookupService(
        Map.of("MS:1000031", Set.of("MS:1001742")));

    private MzTab mzTab;
    private List<ValidationMessage> messages;

    @BeforeEach
    public void setUp() {
        mzTab = MzTabValidatorTest.createTestFile();
        // a sample without species
        mzTab.getMetadata().setSample(new ArrayList<>(List.of(
            new Sample().id(1).description("no species"))));
        // instruments 1 and 3 use a term that is not an instrument model,
        // instrument 2 uses an undefined cv label
        mzTab.getMetadata().
            addInstrumentItem(new Instrument().id(1).name(param("MS", "MS:1000040", "m/z"))).
            addInstrumentItem(new Instrument().id(2).name(param("XMS", "MS:1001742", "LTQ Orbitrap Velos"))).
            addInstrumentItem(new Instrument().id(3).name(param("MS", "MS:1000040", "m/z")));
        mzTab.getMetadata().addCvItem(new CV().id(4).label("UO").
            fullName("Units of measurement").version("2018-03-24").
            uri(URI.create("https://www.ebi.ac.uk/ols/ontologies/uo")));
        messages = CvMappingValidator.of(mapping(
            rule("instrument_name_may", "/metadata/instrument/@name",
                CvTermsCombinationLogic.OR,
                term("MS", "MS:1000031", "instrument model", true, true)),
            rule("sample_species_may", "/metadata/sample/@species",
                CvTermsCombinationLogic.OR,
                term("NCBITaxon", "NCBITaxon:1", "root", true, true))),
            LOOKUP, true).validate(mzTab);
    }

    @Test
    public void notAllowedTermIsReportedPerElementWithExactKey() {
        List<ValidationMessage> ms = only(CrossCheckErrorType.CvTermNotAllowed);
        assertEquals(List.of("instrument[1]-name", "instrument[3]-name"), keys(ms));
        ms.forEach(m -> assertEquals("instrument_name_may", m.getRuleId()));
        assertTrue(ms.get(0).getSuggestion().contains("MS:1000031 (instrument model) or one of its child terms"));
        assertTrue(ms.get(0).getSuggestion().contains("[MS, MS:1000040, m/z, ] on instrument[1]-name"));
        assertEquals("/metadata/instrument[1]/name", ms.get(0).getLocation().getElementPath());
    }

    @Test
    public void missingElementPointsAtTheGroup() {
        ValidationMessage m = only(CrossCheckErrorType.RulePointerObjectNullOptional).get(0);
        assertEquals("sample", m.getLocation().getMetadataGroup());
        assertNull(m.getLocation().getMetadataKey());
        assertNull(m.getLocation().getElementPath());
        assertEquals("sample_species_may", m.getRuleId());
        assertTrue(m.getSuggestion().startsWith("Add a value for sample[n]-species (optional)."));
    }

    @Test
    public void undefinedCvPointsAtTheParameter() {
        ValidationMessage m = only(CrossCheckErrorType.CvUndefinedInMetadata).get(0);
        assertEquals("instrument[2]-name", m.getLocation().getMetadataKey());
        assertNull(m.getRuleId());
    }

    @Test
    public void unusedCvPointsAtItsLabelKey() {
        ValidationMessage m = only(CrossCheckErrorType.CvUnused).get(0);
        assertEquals("cv[4]-label", m.getLocation().getMetadataKey());
        assertEquals("cv", m.getLocation().getMetadataGroup());
    }

    @Test
    public void everySemanticMessageFromARuleHasItsRuleId() {
        // an AND rule with several child-allowing terms makes rule evaluation fail
        List<ValidationMessage> all = new ArrayList<>(messages);
        all.addAll(CvMappingValidator.of(mapping(
            rule("broken_and", "/metadata/msRun/@format",
                CvTermsCombinationLogic.AND,
                term("MS", "MS:1000560", "mass spectrometer file format", true, true),
                term("MS", "MS:1000767", "native spectrum identifier format", true, true))),
            LOOKUP, true).validate(mzTab));
        ValidationMessage exception = all.stream().
            filter(m -> is(m, CrossCheckErrorType.SemanticValidationException)).
            findFirst().orElseThrow();
        assertEquals("broken_and", exception.getRuleId());
        List<ValidationMessage> fromRules = all.stream().
            filter(m -> !is(m, CrossCheckErrorType.CvUndefinedInMetadata) && !is(m, CrossCheckErrorType.CvUnused)).
            collect(Collectors.toList());
        assertFalse(fromRules.isEmpty());
        fromRules.forEach(m -> assertNotNull(m.getRuleId(), m.getMessage()));
    }

    @Test
    public void allowedTermsTextJoinsByLogic() {
        assertEquals("MS:1000031 (instrument model) or one of its child terms or MS:1000040 (m/z)",
            CvMappingUtils.allowedTermsText(rule("r", "/metadata/@quantificationMethod",
                CvTermsCombinationLogic.XOR,
                term("MS", "MS:1000031", "instrument model", true, true),
                term("MS", "MS:1000040", "m/z", false, true))));
        assertEquals("a child term of MS:1000031 (instrument model) and MS:1000040 (m/z)",
            CvMappingUtils.allowedTermsText(rule("r", "/metadata/@quantificationMethod",
                CvTermsCombinationLogic.AND,
                term("MS", "MS:1000031", "instrument model", true, false),
                term("MS", "MS:1000040", "m/z", false, true))));
    }

    private List<ValidationMessage> only(MZTabErrorType type) {
        return messages.stream().filter(m -> is(m, type)).collect(Collectors.toList());
    }

    private static boolean is(ValidationMessage message, MZTabErrorType type) {
        return message.getCode().startsWith("[" + type.getLevel() + "-" + type.getCode() + "]");
    }

    private static List<String> keys(List<ValidationMessage> messages) {
        return messages.stream().
            map(m -> m.getLocation().getMetadataKey()).
            collect(Collectors.toList());
    }

    private static CvMapping mapping(CvMappingRule... rules) {
        CvMapping mapping = new CvMapping();
        mapping.setCvMappingRuleList(new CvMapping.CvMappingRuleList());
        mapping.getCvMappingRuleList().getCvMappingRule().addAll(List.of(rules));
        return mapping;
    }

    private static CvMappingRule rule(String id, String path,
        CvTermsCombinationLogic logic, CvTerm... terms) {
        CvMappingRule rule = new CvMappingRule();
        rule.setId(id);
        rule.setCvElementPath(path);
        rule.setScopePath(path);
        rule.setRequirementLevel(RequirementLevel.MAY);
        rule.setCvTermsCombinationLogic(logic);
        rule.getCvTerm().addAll(List.of(terms));
        return rule;
    }

    private static CvTerm term(String cv, String accession, String name,
        boolean allowChildren, boolean useTerm) {
        CvReference ref = new CvReference();
        ref.setCvIdentifier(cv);
        CvTerm term = new CvTerm();
        term.setCvIdentifierRef(ref);
        term.setTermAccession(accession);
        term.setTermName(name);
        term.setAllowChildren(allowChildren);
        term.setUseTerm(useTerm);
        term.setIsRepeatable(Boolean.FALSE);
        return term;
    }

    private static Parameter param(String cvLabel, String accession, String name) {
        return new Parameter().cvLabel(cvLabel).cvAccession(accession).name(name);
    }
}
