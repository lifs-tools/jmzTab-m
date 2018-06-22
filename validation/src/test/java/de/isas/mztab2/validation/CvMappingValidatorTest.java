/*
 * Copyright 2018 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package de.isas.mztab2.validation;

import de.isas.mztab2.cvmapping.CvParameterLookupService;
import de.isas.mztab2.cvmapping.JxPathElement;
import de.isas.mztab2.cvmapping.RuleEvaluationResult;
import de.isas.mztab2.model.Instrument;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.ValidationMessage;
import de.isas.mztab2.validation.handlers.EmptyRuleHandler;
import de.isas.mztab2.validation.handlers.ResolvingCvRuleHandler;
import de.isas.mztab2.validator.MzTabValidatorTest;
import info.psidev.cvmapping.CvMappingRule;
import info.psidev.cvmapping.CvReference;
import info.psidev.cvmapping.CvTerm;
import java.util.List;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class CvMappingValidatorTest {

    /**
     * Test of handleParameters method, of class AndValidationHandler.
     */
    @Test
    public void testHandleParametersWithExistingElement() {
        de.isas.mztab2.validation.handlers.AndValidationHandler handler = new de.isas.mztab2.validation.handlers.AndValidationHandler();
        CvMappingRule instrumentNameMay = prepareRule();

        ResolvingCvRuleHandler ruleHandler = new ResolvingCvRuleHandler(
            new CvParameterLookupService());

        RuleEvaluationResult result = applyRule(createTestData(), ruleHandler,
            instrumentNameMay);
        List<ValidationMessage> messages = handler.
            handleParameters(result, true);
        assertTrue(messages.isEmpty());
    }

    /**
     * Test of handleParameters method, of class AndValidationHandler.
     */
    @Test
    public void testHandleParametersWithMissingElement() {
        de.isas.mztab2.validation.handlers.AndValidationHandler handler = new de.isas.mztab2.validation.handlers.AndValidationHandler();
        CvMappingRule instrumentNameMay = prepareRule();

        ResolvingCvRuleHandler ruleHandler = new ResolvingCvRuleHandler(
            new CvParameterLookupService());
        MzTab mzTab = createTestData();
        mzTab.getMetadata().
            getInstrument().
            get(0).
            setName(null);
        RuleEvaluationResult result = applyRule(mzTab, ruleHandler,
            instrumentNameMay);
        List<ValidationMessage> messages = handler.
            handleParameters(result, true);
        assertEquals(0, messages.size());
        JXPathContext context = JXPathContext.newContext(mzTab);
        List<Pair<Pointer, ? extends Parameter>> selection = JxPathElement.
            toList(context, instrumentNameMay.getCvElementPath(),
                Parameter.class);
        //check that we get a message on the empty selection
        messages = new EmptyRuleHandler().handleRule(instrumentNameMay,
            selection);
        assertEquals(1, messages.size());
        assertEquals(ValidationMessage.MessageTypeEnum.INFO, messages.get(0).
            getMessageType());
        assertEquals(ValidationMessage.CategoryEnum.CROSS_CHECK,
            messages.get(0).
                getCategory());
    }

    protected MzTab createTestData() {
        MzTab mzTab = MzTabValidatorTest.createTestFile();
        /*
        MTD	instrument[1]-name	[MS, MS:1001742, LTQ Orbitrap Velos, ]
        MTD	instrument[1]-source	[MS, MS:1000073, Electrospray Ionization, ]
        MTD	instrument[1]-analyzer[1]	[MS, MS:1000484, orbitrap, ]
        MTD	instrument[1]-analyzer[2]	[MS, MS:1000084, time-of-flight, ] <- just for testing
        MTD	instrument[1]-detector	[MS, MS:1000112, Faraday Cup, ]
         */
        Instrument instrument = new Instrument().id(1).
            name(new Parameter().cvLabel("MS").
                cvAccession("MS:1001742").
                name("LTQ Orbitrap Velos")).
            source(new Parameter().cvLabel("MS").
                cvAccession("MS:1000073").
                name("Electrospray Ionization")).
            addAnalyzerItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1000484").
                name("orbitrap")).
            addAnalyzerItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1000084").
                name("time-of-flight")).
            detector(new Parameter().cvLabel("MS").
                cvAccession("MS:1000112").
                name("Faraday Cup"));
        mzTab.getMetadata().
            addInstrumentItem(instrument);
        return mzTab;
    }

    protected RuleEvaluationResult applyRule(MzTab mzTab,
        ResolvingCvRuleHandler ruleHandler, CvMappingRule rule) {
        JXPathContext context = JXPathContext.newContext(mzTab);
        List<Pair<Pointer, ? extends Parameter>> instrumentName = JxPathElement.
            toList(context, rule.getCvElementPath(), Parameter.class);
        RuleEvaluationResult result = ruleHandler.handleRule(rule,
            instrumentName);
        return result;
    }

    protected CvMappingRule prepareRule() {
        CvMappingRule instrumentNameMay = new CvMappingRule();
        instrumentNameMay.setId("instrument_name_may");
        instrumentNameMay.setCvElementPath(
            "/metadata/instrument/@instrumentName");
        instrumentNameMay.
            setRequirementLevel(CvMappingRule.RequirementLevel.MAY);
        instrumentNameMay.setScopePath("/metadata/instrument");
        instrumentNameMay.setCvTermsCombinationLogic(
            CvMappingRule.CvTermsCombinationLogic.AND);
        CvTerm cvTerm = new CvTerm();
        cvTerm.setTermAccession("MS:1000031");
        cvTerm.setTermName("instrument model");
        cvTerm.setIsRepeatable(Boolean.FALSE);
        cvTerm.setAllowChildren(true);
        CvReference ref = new CvReference();
        ref.setCvIdentifier("MS");
        ref.setCvName("PSI-MS ontology");
        cvTerm.setCvIdentifierRef(ref);
        instrumentNameMay.getCvTerm().
            add(cvTerm);
        return instrumentNameMay;
    }

}
