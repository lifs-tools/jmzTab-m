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
package org.lifstools.mztab2.validation;

import org.lifstools.mztab2.validation.Validator;
import org.lifstools.mztab2.cvmapping.CvMappingUtils;
import org.lifstools.mztab2.cvmapping.CvParameterLookupService;
import org.lifstools.mztab2.cvmapping.JxPathElement;
import org.lifstools.mztab2.cvmapping.RemoveUserParams;
import org.lifstools.mztab2.cvmapping.RuleEvaluationResult;
import org.lifstools.mztab2.io.serialization.ParameterConverter;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.ValidationMessage;
import org.lifstools.mztab2.validation.handlers.AndValidationHandler;
import org.lifstools.mztab2.validation.handlers.EmptyRuleHandler;
import org.lifstools.mztab2.validation.handlers.ExtraParametersValidationHandler;
import org.lifstools.mztab2.validation.handlers.OrValidationHandler;
import org.lifstools.mztab2.validation.handlers.ResolvingCvRuleHandler;
import org.lifstools.mztab2.validation.handlers.RuleMessages;
import org.lifstools.mztab2.validation.handlers.SharedParametersValidationHandler;
import org.lifstools.mztab2.validation.handlers.XorValidationHandler;
import info.psidev.cvmapping.CvMapping;
import info.psidev.cvmapping.CvMappingRule;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

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
        return of(mappingFile, new CvParameterLookupService(),
            errorIfTermNotInRule);
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
            build();
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
        return of(mappingFile, new CvParameterLookupService(),
            errorIfTermNotInRule);
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
            build();
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
            build();
    }

    @Override
    public List<ValidationMessage> validate(MzTab mzTab) {
        final List<ValidationMessage> messages = new LinkedList<>();
        log.debug("Applying {} pre validation steps.", preValidators.size());
        preValidators.stream().
            forEach((validator) ->
            {
                messages.addAll(guarded(validator, mzTab));
            });
        // Always run once here; the factories no longer also register it as a
        // pre validator, which reported every undefined cv twice.
        messages.addAll(guarded(new CvDefinitionValidationHandler(), mzTab));
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
        log.debug("Applying {} post validation steps.", postValidators.size());
        postValidators.stream().
            forEach((validator) ->
            {
                messages.addAll(guarded(validator, mzTab));
            });
        return messages;
    }

    /**
     * Runs a pre or post validator; an exception in it is reported as an
     * error instead of ending the whole semantic validation.
     */
    private static List<ValidationMessage> guarded(Validator<MzTab> validator,
        MzTab mzTab) {
        try {
            return validator.validate(mzTab);
        } catch (RuntimeException re) {
            log.warn("Validator {} failed: {}", validator.getClass().
                getSimpleName(), re.toString());
            log.debug("Validator failure", re);
            return List.of(new MZTabError(
                CrossCheckErrorType.SemanticValidationException, -1,
                validator.getClass().
                    getSimpleName() + ": " + re.getMessage()).
                toValidationMessage());
        }
    }

    /**
     * Applies one rule. An exception while applying it is reported as an
     * error of that rule, after the messages it produced so far, so that the
     * other rules still run.
     */
    private List<ValidationMessage> handleRule(JXPathContext context,
        CvMappingRule rule, boolean errorOnTermNotInRule) {
        final List<ValidationMessage> messages = new ArrayList<>();
        try {
            applyRule(context, rule, errorOnTermNotInRule, messages);
        } catch (RuntimeException re) {
            log.warn("Rule {} could not be applied: {}", rule.getId(), re.
                toString());
            log.debug("Rule failure", re);
            messages.add(RuleMessages.atRulePath(new MZTabError(
                CrossCheckErrorType.SemanticValidationException, -1, re.
                    getMessage()), rule));
        }
        return messages.isEmpty() ? Collections.emptyList() : messages;
    }

    private void applyRule(JXPathContext context, CvMappingRule rule,
        boolean errorOnTermNotInRule, List<ValidationMessage> messages) {
        String path = rule.getCvElementPath();
        List<Pair<Pointer, Parameter>> selection = JxPathElement.
            toList(context, path, Parameter.class);

        messages.addAll(emptyRuleHandler.handleRule(rule, selection));
        if (!messages.isEmpty()) {
            return;
        }

        // a cv parameter whose term cannot be looked up is reported here;
        // RemoveUserParams leaves it out of the rule's evaluation
        for (Pair<Pointer, Parameter> pair : selection) {
            Parameter p = pair.getValue();
            if (RemoveUserParams.isMalformed(p)) {
                messages.add(RuleMessages.atPointer(new MZTabError(
                    CrossCheckErrorType.CvAccessionMalformed, -1,
                    new ParameterConverter().convert(p), p.getCvLabel(), p.
                    getCvAccession()), pair.getKey(), rule));
            }
        }

        final List<Pair<Pointer, Parameter>> filteredSelection = cvTermSelectionHandler.
            handleSelection(selection);

        // and logic means that ALL of the defined terms or their children MUST appear
        // we only compare valid CVParameters here, user Params (no cv accession), are not compared!
        // if combination logic is AND, child expansion needs to be disabled to avoid nonsensical combinations
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
    }

}
