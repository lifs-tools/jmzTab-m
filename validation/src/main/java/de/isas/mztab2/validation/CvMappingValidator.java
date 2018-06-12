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

import de.isas.lipidomics.mztab2.validation.Validator;
import de.isas.mztab2.cvmapping.CvMappingUtils;
import de.isas.mztab2.cvmapping.CvParameterLookupService;
import de.isas.mztab2.cvmapping.JxPathElement;
import de.isas.mztab2.cvmapping.ParameterComparisonResult;
import de.isas.mztab2.cvmapping.Parameters;
import de.isas.mztab2.io.serialization.ParameterConverter;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.ValidationMessage;
import info.psidev.cvmapping.CvMapping;
import info.psidev.cvmapping.CvMappingRule;
import info.psidev.cvmapping.CvTerm;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType.Level;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;

/**
 * Validator implementation that uses a provided xml mapping file with rules for
 * required, recommended and optional CV parameters to assert that an mzTab
 * follows these rules.
 *
 * @author nilshoffmann
 */
@Slf4j
public class CvMappingValidator implements Validator<MzTab> {

    private final CvMapping mapping;
    private final CvParameterLookupService client;
    private final boolean errorIfTermNotInRule;

    public static CvMappingValidator of(File mappingFile,
        boolean errorIfTermNotInRule) throws JAXBException {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        CvParameterLookupService service = new CvParameterLookupService(client);
        return of(mappingFile, service, errorIfTermNotInRule);
    }

    public static CvMappingValidator of(File mappingFile,
        CvParameterLookupService client, boolean errorIfTermNotInRule) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(CvMapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        CvMapping mapping = (CvMapping) u.unmarshal(mappingFile);
        return new CvMappingValidator(mapping, client, errorIfTermNotInRule);
    }

    public static CvMappingValidator of(URL mappingFile,
        boolean errorIfTermNotInRule) throws JAXBException {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        CvParameterLookupService service = new CvParameterLookupService(client);
        return of(mappingFile, service, errorIfTermNotInRule);
    }

    public static CvMappingValidator of(URL mappingFile,
        CvParameterLookupService client, boolean errorIfTermNotInRule) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CvMapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        CvMapping mapping = (CvMapping) u.unmarshal(mappingFile);
        return new CvMappingValidator(mapping, client, errorIfTermNotInRule);
    }

    public CvMappingValidator(CvMapping mapping, CvParameterLookupService client,
        boolean errorIfTermNotInRule) {
        this.mapping = mapping;
        this.client = client;
        this.errorIfTermNotInRule = errorIfTermNotInRule;
    }

    @Override
    public List<ValidationMessage> validate(MzTab mzTab) {
        final List<ValidationMessage> messages = new LinkedList<>();
        JXPathContext context = JXPathContext.newContext(mzTab);
        mapping.getCvMappingRuleList().
            getCvMappingRule().
            forEach((rule) ->
            {
                log.debug("Evaluating rule " + rule.getId() + " on " + rule.
                    getCvElementPath());
                if (rule.getCvTermsCombinationLogic() == CvMappingRule.CvTermsCombinationLogic.AND) {
                    if (rule.getCvTerm().
                        size() > 1) {
                        for (CvTerm term : rule.getCvTerm()) {
                            if (term.isAllowChildren()) {
                                throw new IllegalArgumentException(
                                    CvMappingUtils.toString(rule) + " uses 'AND' combination logic with multiple CvTerms and allowChildren=true! Please change to OR or XOR logic!");
                            }
                        }
                    }
                }
                messages.addAll(handleRule(context, rule, errorIfTermNotInRule));
            });
        return messages;
    }

    private List<ValidationMessage> handleRule(JXPathContext context,
        CvMappingRule rule, boolean errorOnTermNotInRule) {
        String path = rule.getCvElementPath();
        List<Pair<Pointer, ? extends Parameter>> selection = JxPathElement.
            toList(context, path, Parameter.class);
        if (selection.isEmpty()) {
            log.debug(
                "Evaluating rule " + rule.getId() + " on " + rule.
                getCvElementPath() + " did not yield any selected elements!");
            if (rule.getRequirementLevel() == CvMappingRule.RequirementLevel.MUST) {
                MZTabError error = new MZTabError(
                    CrossCheckErrorType.RulePointerObjectNull, -1,
                    rule.getCvElementPath(), rule.getId());
                return Arrays.asList(error.toValidationMessage());
            }
            return Collections.emptyList();
        }

        List<Pair<Pointer, ? extends Parameter>> filteredSelection = selection.
            stream().
            filter((pair) ->
            {
                if (pair.getValue().
                    getCvAccession() == null || pair.getValue().
                        getCvAccession().
                        isEmpty()) {
                    //user parameter
                    log.debug("Removing user parameter for path " + pair.
                        getKey().
                        asPath() + " with value " + pair.getValue());
                    return false;
                }
                return true;
            }).
            collect(Collectors.toList());

        filteredSelection.forEach((pair) ->
        {
            Parameter p = pair.getValue();
            if (!p.getCvAccession().
                contains(":")) {
                throw new RuntimeException(
                    "Malformed cv accession '" + p.getCvAccession() + "' for " + pair.
                    getKey().
                    asPath() + ". Must be: <CV>:<TERMNUMBER>");
            }
        });

        // and logic means that ALL of the defined terms or their children MUST appear
        // we only compare valid CVParameters here, user Params (no cv accession), are not compared!
        // if combination logic is AND, child expansion needs to be disabled to avoid nonsensical combinations
        final List<ValidationMessage> messages = new ArrayList<>();
        final Map<String, Parameter> allowedParameters = new LinkedHashMap<>();
        final Map<String, Pair<Pointer, ? extends Parameter>> foundParameters = new LinkedHashMap<>();
        rule.getCvTerm().
            forEach((cvTerm) ->
            {
                for (Pair<Pointer, ? extends Parameter> pair : filteredSelection) {
                    if (cvTerm.isAllowChildren()) {
                        log.debug("Resolving children of " + cvTerm.
                            getTermAccession() + " against " + pair.getValue().
                                getCvAccession());
                        //resolve children
                        try {
                            ParameterComparisonResult result = client.
                                isChildOfOrSame(Parameters.asParameter(cvTerm),
                                    pair.getValue());
                            switch (result) {
                                case CHILD_OF:
                                    log.debug(pair.getValue().
                                        getCvAccession() + " is a child of " + cvTerm.
                                            getTermAccession());
                                    //use key of found parameter, since it is a child of cvTerm / cvTerm is a parent of the child
                                    allowedParameters.put(pair.getValue().
                                        getCvAccession().
                                        toUpperCase(), Parameters.asParameter(
                                            cvTerm));
                                    foundParameters.put(pair.getValue().
                                        getCvAccession().
                                        toUpperCase(), pair);
                                    break;
                                case IDENTICAL:
                                    if (cvTerm.isUseTerm()) {
                                        log.debug(pair.getValue().
                                            getCvAccession() + " is identical to " + cvTerm.
                                                getTermAccession());
                                        //use key of found parameter, since both are identical
                                        allowedParameters.put(pair.getValue().
                                            getCvAccession().
                                            toUpperCase(), Parameters.
                                                asParameter(cvTerm));
                                        foundParameters.put(pair.getValue().
                                            getCvAccession().
                                            toUpperCase(), pair);
                                    }
                                    break;
                                case NOT_RELATED:
                                    log.debug(pair.getValue().
                                        getCvAccession() + " is not related to " + cvTerm.
                                            getTermAccession());
                                    //add term from rule as is
                                    allowedParameters.put(cvTerm.
                                        getTermAccession().
                                        toUpperCase(), Parameters.asParameter(
                                            cvTerm));
                                    //add found parameter as is
                                    foundParameters.put(pair.getValue().
                                        getCvAccession().
                                        toUpperCase(), pair);
                                    break;
                            }
                        } catch (org.springframework.web.client.HttpClientErrorException ex) {
                            throw new IllegalArgumentException(
                                "Could not retrieve parents for term " + pair.
                                    getValue().
                                    getCvAccession() + "! Please check, whether the term accession is correct!",
                                ex);
                        }
                    } else if (cvTerm.isUseTermName()) {
                        throw new IllegalArgumentException(
                            "isUseTermName on cvTerm is not supported!");
                    } else {
                        if (cvTerm.getTermAccession().
                            toUpperCase().
                            equals(pair.getValue().
                                getCvAccession().
                                toUpperCase())) {
                            //TODO repeatable terms
                            allowedParameters.put(pair.getValue().
                                getCvAccession().
                                toUpperCase(), Parameters.asParameter(cvTerm));
                            allowedParameters.put(cvTerm.getTermAccession().
                                toUpperCase(), Parameters.asParameter(cvTerm));
                        }
                    }
                }
            });

        switch (rule.getCvTermsCombinationLogic()) {
            case AND:
                messages.addAll(handleAnd(allowedParameters, foundParameters,
                    rule,
                    errorOnTermNotInRule));
                break;
            case OR: // any of the terms or their children need to appear
                messages.addAll(handleOr(allowedParameters, foundParameters,
                    rule,
                    errorOnTermNotInRule));
                break;
            case XOR:
                messages.addAll(handleXor(allowedParameters, foundParameters,
                    rule,
                    errorOnTermNotInRule));
                break;
            default:
                throw new IllegalArgumentException(
                    "Unknown combination logic value: " + rule.
                        getCvTermsCombinationLogic() + "! Supported are: " + Arrays.
                        toString(CvMappingRule.CvTermsCombinationLogic.
                            values()));
        }

        return messages.isEmpty() ? Collections.emptyList() : messages;
    }

    protected List<ValidationMessage> handleAnd(
        final Map<String, Parameter> allowedParameters,
        final Map<String, Pair<Pointer, ? extends Parameter>> foundParameters,
        CvMappingRule rule, boolean errorOnTermNotInRule) throws IllegalStateException {
        final List<ValidationMessage> messages = new ArrayList<>();
        // all defined terms or children thereof need to appear
        Set<String> unmatchedParameters = new HashSet<String>();
        unmatchedParameters.addAll(allowedParameters.keySet());
        unmatchedParameters.removeAll(foundParameters.keySet());
        if (!unmatchedParameters.isEmpty()) {
            for (String s : unmatchedParameters) {
                MZTabErrorType errorType = null;
                switch (rule.getRequirementLevel()) {
                    case MAY:
                        errorType = CrossCheckErrorType.CvTermOptional;
                        break;
                    case SHOULD:
                        errorType = CrossCheckErrorType.CvTermRecommended;
                        break;
                    case MUST:
                        errorType = CrossCheckErrorType.CvTermRequired;
                        break;
                }
                MZTabError error = new MZTabError(errorType, -1,
                    new ParameterConverter().convert(allowedParameters.get(
                        s)), rule.getCvElementPath(), rule.getId());
                messages.add(error.toValidationMessage());
            }
        }
        handleExtraParameters(allowedParameters, foundParameters,
            errorOnTermNotInRule, rule, messages);
        return messages;
    }

    protected List<ValidationMessage> handleOr(
        final Map<String, Parameter> allowedParameters,
        final Map<String, Pair<Pointer, ? extends Parameter>> foundParameters,
        CvMappingRule rule, boolean errorOnTermNotInRule) {
        // or logic means, any of the defined terms may be present, or none
        final List<ValidationMessage> messages = new ArrayList<>();
        // all defined terms or children thereof need to appear
        Set<String> matchedParameters = new HashSet<String>();
        matchedParameters.addAll(allowedParameters.keySet());
        matchedParameters.retainAll(foundParameters.keySet());
        if (matchedParameters.isEmpty()) {
            for (String s : allowedParameters.keySet()) {
                MZTabErrorType errorType = null;
                switch (rule.getRequirementLevel()) {
                    case MAY:
                        errorType = CrossCheckErrorType.CvTermOptional;
                        break;
                    case SHOULD:
                        errorType = CrossCheckErrorType.CvTermRecommended;
                        break;
                    case MUST:
                        errorType = CrossCheckErrorType.CvTermRequired;
                        break;
                }
                MZTabError error = new MZTabError(errorType, -1,
                    new ParameterConverter().convert(allowedParameters.get(
                        s)), rule.getCvElementPath(), rule.getId());
                messages.add(error.toValidationMessage());
            }
        }

        handleExtraParameters(allowedParameters, foundParameters,
            errorOnTermNotInRule, rule, messages);
        return messages;
    }

    protected void handleExtraParameters(
        final Map<String, Parameter> allowedParameters,
        final Map<String, Pair<Pointer, ? extends Parameter>> foundParameters,
        boolean errorOnTermNotInRule,
        CvMappingRule rule, final List<ValidationMessage> messages) throws IllegalStateException {
        //handle (non-user) parameters that were found at the object but are not defined in the rule
        Set<String> extraParameters = new HashSet<String>();
        extraParameters.addAll(foundParameters.keySet());
        extraParameters.removeAll(allowedParameters.keySet());
        if (!extraParameters.isEmpty()) {
            for (String s : extraParameters) {
                Pair<Pointer, ? extends Parameter> p = foundParameters.
                    get(s);
                MZTabError error;
                if (errorOnTermNotInRule) {
                    error = new MZTabError(CrossCheckErrorType.CvTermNotAllowed,
                        -1,
                        new ParameterConverter().convert(foundParameters.get(
                            s).
                            getValue()), rule.getCvElementPath(), rule.getId());
                } else {
                    error = new MZTabError(CrossCheckErrorType.CvTermNotInRule,
                        -1,
                        new ParameterConverter().convert(foundParameters.get(
                            s).
                            getValue()), rule.getCvElementPath(), rule.getId());
                }
                messages.add(error.toValidationMessage());
            }
        }
    }

    protected List<ValidationMessage> handleXor(
        final Map<String, Parameter> allowedParameters,
        final Map<String, Pair<Pointer, ? extends Parameter>> foundParameters,
        CvMappingRule rule, boolean errorOnTermNotInRule) {
        // xor logic means, if one of the defined terms or its children is set, none of the others is allowed
        final List<ValidationMessage> messages = new ArrayList<>();
        // all defined terms or children thereof need to appear
        Set<String> matchedParameters = new HashSet<String>();
        matchedParameters.addAll(allowedParameters.keySet());
        matchedParameters.retainAll(foundParameters.keySet());
        if (matchedParameters.isEmpty()) {
            for (String s : allowedParameters.keySet()) {
                MZTabErrorType errorType = null;
                switch (rule.getRequirementLevel()) {
                    case MAY:
                        errorType = CrossCheckErrorType.CvTermOptional;
                        break;
                    case SHOULD:
                        errorType = CrossCheckErrorType.CvTermRecommended;
                        break;
                    case MUST:
                        errorType = CrossCheckErrorType.CvTermRequired;
                        break;
                }
                MZTabError error = new MZTabError(errorType, -1,
                    new ParameterConverter().convert(allowedParameters.get(
                        s)), rule.getCvElementPath(), rule.getId());
                messages.add(error.toValidationMessage());
            }
        } else if (matchedParameters.size() > 1) {
            //Only one of the provided cv terms for "{1}" {0} be reported. You defined terms "{2}". Allowed terms are defined in rule "{3}".
            String definedParameters = matchedParameters.stream().
                collect(Collectors.joining(", "));
            MZTabErrorType xorErrorType = MZTabErrorType.forLevel(MZTabErrorType.Category.CrossCheck,
                toErrorLevel(rule.getRequirementLevel()), "CvTermXor");
            MZTabError error = new MZTabError(xorErrorType, -1, rule.getRequirementLevel().value(), rule.getCvElementPath(), definedParameters, rule.getId());
            messages.add(error.toValidationMessage());
        }

        handleExtraParameters(allowedParameters, foundParameters,
            errorOnTermNotInRule, rule, messages);
        return messages;
    }

    private Level toErrorLevel(
        CvMappingRule.RequirementLevel requirementLevel) {
        switch (requirementLevel) {
            case MAY:
                return Level.Info;
            case SHOULD:
                return Level.Warn;
            case MUST:
                return Level.Error;
            default:
                throw new IllegalArgumentException(
                    "Unhandled case: " + requirementLevel);
        }
    }

}
