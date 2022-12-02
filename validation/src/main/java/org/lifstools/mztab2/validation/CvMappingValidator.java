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

import org.lifstools.mztab2.validation.Validator;
import org.lifstools.mztab2.cvmapping.CvMappingUtils;
import org.lifstools.mztab2.cvmapping.CvParameterLookupService;
import org.lifstools.mztab2.cvmapping.JxPathElement;
import org.lifstools.mztab2.cvmapping.RemoveUserParams;
import org.lifstools.mztab2.cvmapping.RuleEvaluationResult;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.ValidationMessage;
import org.lifstools.mztab2.validation.handlers.AndValidationHandler;
import org.lifstools.mztab2.validation.handlers.EmptyRuleHandler;
import org.lifstools.mztab2.validation.handlers.ExtraParametersValidationHandler;
import org.lifstools.mztab2.validation.handlers.OrValidationHandler;
import org.lifstools.mztab2.validation.handlers.ResolvingCvRuleHandler;
import org.lifstools.mztab2.validation.handlers.SharedParametersValidationHandler;
import org.lifstools.mztab2.validation.handlers.XorValidationHandler;
import info.psidev.cvmapping.CvMapping;
import info.psidev.cvmapping.CvMappingRule;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;

/**
 * Validator implementation that uses a provided xml mapping file with rules for
 * required, recommended and optional CV parameters to assert that an mzTab
 * follows these rules.
 * 
 * First, all preValidators are run, then, the cv parameter validation is executed, before finally, 
 * the postValidators are run. Each validator can add validation messages to the output.
 *
 * @author nilshoffmann
 */
@Slf4j
@lombok.Builder()
public class CvMappingValidator implements Validator<MzTab> {

    private final CvMapping mapping;
    private final CvRuleHandler ruleHandler;
    private final boolean errorIfTermNotInRule;
    private final CvTermValidationHandler andHandler;
    private final CvTermValidationHandler orHandler;
    private final CvTermValidationHandler xorHandler;
    private final CvTermValidationHandler extraHandler;
    private final CvTermValidationHandler sharedHandler;
    private final EmptyRuleHandler emptyRuleHandler;
    private final RemoveUserParams cvTermSelectionHandler;
    private final List<Validator<MzTab>> preValidators = new LinkedList<>();
    private final List<Validator<MzTab>> postValidators = new LinkedList<>();

    /**
     * Create a new instance of CvMappingValidator. 
     * 
     * Uses a default instance of the {@link CvParameterLookupService}.
     * 
     * @param mappingFile the mapping file to use
     * @param errorIfTermNotInRule raise an error if a term is not defined within an otherwise matching rule for the element
     * @return a new CvMappingValidator instance
     * @throws JAXBException if errors occur during unmarshalling of the mapping xml file.
     */
    public static CvMappingValidator of(File mappingFile,
        boolean errorIfTermNotInRule) throws JAXBException {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        CvParameterLookupService service = new CvParameterLookupService(client);
        return of(mappingFile, service, errorIfTermNotInRule);
    }

    /**
     * Create a new instance of CvMappingValidator. 
     * 
     * Uses the provided {@link CvParameterLookupService}.
     * 
     * @param mappingFile the mapping file to use
     * @param client the ontology lookup service client
     * @param errorIfTermNotInRule raise an error if a term is not defined within an otherwise matching rule for the element
     * @return a new CvMappingValidator instance
     * @throws JAXBException if errors occur during unmarshalling of the mapping xml file.
     */
    public static CvMappingValidator of(File mappingFile,
        CvParameterLookupService client, boolean errorIfTermNotInRule) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(CvMapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        CvMapping mapping = (CvMapping) u.unmarshal(mappingFile);
        return new CvMappingValidator.CvMappingValidatorBuilder().mapping(
            mapping).
            ruleHandler(new ResolvingCvRuleHandler(client)).
            errorIfTermNotInRule(errorIfTermNotInRule).
            andHandler(new AndValidationHandler()).
            orHandler(new OrValidationHandler()).
            xorHandler(new XorValidationHandler()).
            extraHandler(new ExtraParametersValidationHandler()).
            sharedHandler(new SharedParametersValidationHandler()).
            cvTermSelectionHandler(new RemoveUserParams()).
            emptyRuleHandler(new EmptyRuleHandler()).
            build().
            withPreValidator(new CvDefinitionValidationHandler());
    }

    /**
     * Add the provided validator implementation to the list of validators that run <b>first</b>.
     * @param preValidator the validator
     * @return an instance of this object
     */
    public CvMappingValidator withPreValidator(Validator<MzTab> preValidator) {
        preValidators.add(preValidator);
        return this;
    }

    /**
     * Add the provided validator implementation to the list of validators that run <b>last</b>.
     * @param postValidator the validator
     * @return an instance of this object
     */
    public CvMappingValidator withPostValidator(Validator<MzTab> postValidator) {
        postValidators.add(postValidator);
        return this;
    }

    /**
     * Create a new instance of CvMappingValidator. 
     * 
     * Uses a default instance of the {@link CvParameterLookupService}.
     * 
     * @param mappingFile the mapping file URL to use
     * @param errorIfTermNotInRule raise an error if a term is not defined within an otherwise matching rule for the element
     * @return a new CvMappingValidator instance
     * @throws JAXBException if errors occur during unmarshalling of the mapping xml file.
     */
    public static CvMappingValidator of(URL mappingFile,
        boolean errorIfTermNotInRule) throws JAXBException {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        CvParameterLookupService service = new CvParameterLookupService(client);
        return of(mappingFile, service, errorIfTermNotInRule);
    }

    /**
     * Create a new instance of CvMappingValidator. 
     * 
     * Uses the provided {@link CvParameterLookupService}.
     * 
     * @param mappingFile the mapping file URL to use
     * @param client the ontology lookup service client
     * @param errorIfTermNotInRule raise an error if a term is not defined within an otherwise matching rule for the element
     * @return a new CvMappingValidator instance
     * @throws JAXBException if errors occur during unmarshalling of the mapping xml file.
     */
    public static CvMappingValidator of(URL mappingFile,
        CvParameterLookupService client, boolean errorIfTermNotInRule) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CvMapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        CvMapping mapping = (CvMapping) u.unmarshal(mappingFile);
        return new CvMappingValidator.CvMappingValidatorBuilder().mapping(
            mapping).
            ruleHandler(new ResolvingCvRuleHandler(client)).
            errorIfTermNotInRule(errorIfTermNotInRule).
            andHandler(new AndValidationHandler()).
            orHandler(new OrValidationHandler()).
            xorHandler(new XorValidationHandler()).
            extraHandler(new ExtraParametersValidationHandler()).
            sharedHandler(new SharedParametersValidationHandler()).
            cvTermSelectionHandler(new RemoveUserParams()).
            emptyRuleHandler(new EmptyRuleHandler()).
            build().
            withPreValidator(new CvDefinitionValidationHandler());
    }

    /**
     * Create a new instance of CvMappingValidator. 
     * 
     * Uses the provided {@link CvParameterLookupService}.
     * 
     * @param mapping the cv mapping to use
     * @param client the ontology lookup service client
     * @param errorIfTermNotInRule raise an error if a term is not defined within an otherwise matching rule for the element
     * @return a new CvMappingValidator instance
     */
    public static CvMappingValidator of(CvMapping mapping,
        CvParameterLookupService client,
        boolean errorIfTermNotInRule) {
        return new CvMappingValidator.CvMappingValidatorBuilder().mapping(
            mapping).
            ruleHandler(new ResolvingCvRuleHandler(client)).
            errorIfTermNotInRule(errorIfTermNotInRule).
            andHandler(new AndValidationHandler()).
            orHandler(new OrValidationHandler()).
            xorHandler(new XorValidationHandler()).
            extraHandler(new ExtraParametersValidationHandler()).
            sharedHandler(new SharedParametersValidationHandler()).
            cvTermSelectionHandler(new RemoveUserParams()).
            emptyRuleHandler(new EmptyRuleHandler()).
            build().
            withPreValidator(new CvDefinitionValidationHandler());
    }

    @Override
    public List<ValidationMessage> validate(MzTab mzTab) {
        final List<ValidationMessage> messages = new LinkedList<>();
        log.debug("Applying {} pre validation steps.", preValidators.size());
        preValidators.stream().
            forEach((validator) ->
            {
                messages.addAll(validator.validate(mzTab));
            });
        messages.addAll(new CvDefinitionValidationHandler().validate(mzTab));
        JXPathContext context = JXPathContext.newContext(mzTab);
        log.debug("Applying {} cv rule mapping steps.", mapping.
            getCvMappingRuleList().
            getCvMappingRule().
            size());
        mapping.getCvMappingRuleList().
            getCvMappingRule().
            forEach((rule) ->
            {
                messages.addAll(handleRule(context, rule, errorIfTermNotInRule));
            });
        log.debug("Applying {} post validation steps.", preValidators.size());
        postValidators.stream().
            forEach((validator) ->
            {
                messages.addAll(validator.validate(mzTab));
            });
        return messages;
    }

    private List<ValidationMessage> handleRule(JXPathContext context,
        CvMappingRule rule, boolean errorOnTermNotInRule) {
        String path = rule.getCvElementPath();
        List<Pair<Pointer, Parameter>> selection = JxPathElement.
            toList(context, path, Parameter.class);

        final List<ValidationMessage> messages = emptyRuleHandler.handleRule(
            rule, selection);
        if (!messages.isEmpty()) {
            return messages;
        }

        final List<Pair<Pointer, Parameter>> filteredSelection = cvTermSelectionHandler.
            handleSelection(selection);

        // and logic means that ALL of the defined terms or their children MUST appear
        // we only compare valid CVParameters here, user Params (no cv accession), are not compared!
        // if combination logic is AND, child expansion needs to be disabled to avoid nonsensical combinations
        try {
            RuleEvaluationResult result = ruleHandler.handleRule(rule,
                filteredSelection);

            switch (rule.getCvTermsCombinationLogic()) {
                case AND:
                    messages.addAll(andHandler.handleParameters(result,
                        errorOnTermNotInRule));
                    break;
                case OR: // any of the terms or their children need to appear
                    messages.addAll(orHandler.handleParameters(result,
                        errorOnTermNotInRule));
                    break;
                case XOR:
                    messages.addAll(xorHandler.handleParameters(result,
                        errorOnTermNotInRule));
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unknown combination logic value: " + rule.
                            getCvTermsCombinationLogic() + " on rule " + CvMappingUtils.
                            niceToString(rule) + "! Supported are: " + Arrays.
                        toString(CvMappingRule.CvTermsCombinationLogic.
                            values()));
            }
            //        messages.addAll(sharedHandler.handleParameters(result, errorOnTermNotInRule));
            messages.addAll(extraHandler.handleParameters(result,
                errorOnTermNotInRule));
        } catch (RuntimeException re) {
            log.error(
                "Caught exception while running semantic validation on rule " + CvMappingUtils.
                    niceToString(rule) + " with selection " + filteredSelection,
                re);
            MZTabError error = new MZTabError(
                CrossCheckErrorType.SemanticValidationException, -1, re.
                    getMessage());
            messages.add(error.toValidationMessage());
        }
        return messages.isEmpty() ? Collections.emptyList() : messages;
    }

}
