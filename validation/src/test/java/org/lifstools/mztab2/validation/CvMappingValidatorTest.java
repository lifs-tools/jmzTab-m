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
package org.lifstools.mztab2.validation;

import org.lifstools.mztab2.cvmapping.CvParameterLookupService;
import org.lifstools.mztab2.cvmapping.JxPathElement;
import org.lifstools.mztab2.cvmapping.RuleEvaluationResult;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Instrument;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.ValidationMessage;
import org.lifstools.mztab2.validation.handlers.EmptyRuleHandler;
import org.lifstools.mztab2.validation.handlers.ResolvingCvRuleHandler;
import info.psidev.cvmapping.CvMappingRule;
import info.psidev.cvmapping.CvReference;
import info.psidev.cvmapping.CvTerm;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

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
        org.lifstools.mztab2.validation.handlers.AndValidationHandler handler = new org.lifstools.mztab2.validation.handlers.AndValidationHandler();
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
        org.lifstools.mztab2.validation.handlers.AndValidationHandler handler = new org.lifstools.mztab2.validation.handlers.AndValidationHandler();
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
        List<Pair<Pointer, Parameter>> selection = JxPathElement.
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

    @Test
    public void testCheckCvDefinitions() {
        MzTab mzTabFile = createTestData();
        mzTabFile.getMetadata().
            setCv(new ArrayList());
        CvDefinitionValidationHandler handler = new CvDefinitionValidationHandler();
        List<ValidationMessage> messages = handler.validate(mzTabFile);
        Assert.assertEquals(0, mzTabFile.getMetadata().
            getCv().
            size());
        Assert.assertEquals(15, messages.size());

        mzTabFile.getMetadata().
            addCvItem(new CV().id(1).
                fullName(
                    "Mass spectrometer output files and spectra interpretation").
                label("MS").
                version("4.1.11").
                uri("https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo"));

        Assert.assertEquals(1, mzTabFile.getMetadata().
            getCv().
            size());
        messages = handler.validate(mzTabFile);
        Assert.assertEquals(6, messages.size());

        mzTabFile.getMetadata().
            addCvItem(
                new CV().id(2).
                    fullName("Human Disease Ontology").
                    label("DOID").
                    version("2018-07-05").
                    uri("https://www.ebi.ac.uk/ols/ontologies/doid")
            ).
            addCvItem(
                new CV().id(3).
                    fullName("Cell Ontology").
                    label("CL").
                    version("2017-12-11").
                    uri("https://www.ebi.ac.uk/ols/ontologies/cl")).
            addCvItem(new CV().id(4).
                fullName("BRENDA tissue / enzyme source").
                label("BTO").
                version("2016-05-05").
                uri("https://www.ebi.ac.uk/ols/ontologies/bto")).
            addCvItem(new CV().id(5).
                fullName("NCBI organismal classification").
                label("NCBITaxon").
                version("2018-03-02").
                uri("https://www.ebi.ac.uk/ols/ontologies/ncbitaxon"));
        messages = handler.validate(mzTabFile);
        Assert.assertEquals(0, messages.size());
        mzTabFile.getMetadata().
            addCvItem(
                new CV().id(6).
                    fullName("Units of measurement").
                    label("UO").
                    version("2018-03-24").
                    uri("https://www.ebi.ac.uk/ols/ontologies/uo")
            );
        messages = handler.validate(mzTabFile);
        Assert.assertEquals(1, messages.size());
        Assert.assertTrue(messages.get(0).getMessageType()==ValidationMessage.MessageTypeEnum.WARN);
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
        List<Pair<Pointer, Parameter>> instrumentName = JxPathElement.
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
