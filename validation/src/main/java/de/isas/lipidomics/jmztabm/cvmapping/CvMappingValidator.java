/*
 * Copyright 2018 nilshoffmann.
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
package de.isas.lipidomics.jmztabm.cvmapping;

import de.isas.lipidomics.jmztabm.validation.Validator;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.ValidationMessage;
import info.psidev.cvmapping.CvMapping;
import info.psidev.cvmapping.CvMappingRule;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.jxpath.JXPathContext;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;

/**
 *
 * @author nilshoffmann
 */
public class CvMappingValidator implements Validator<MzTab> {

    private final CvMapping mapping;
    private final OLSClient client;

    public static CvMappingValidator of(File mappingFile) throws JAXBException {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        return of(mappingFile, client);
    }

    public static CvMappingValidator of(File mappingFile, OLSClient client) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(CvMapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        CvMapping mapping = (CvMapping) u.unmarshal(mappingFile);
        return new CvMappingValidator(mapping, client);
    }
    
    public static CvMappingValidator of(URL mappingFile) throws JAXBException {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        return of(mappingFile, client);
    }

    public static CvMappingValidator of(URL mappingFile, OLSClient client) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CvMapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        CvMapping mapping = (CvMapping) u.unmarshal(mappingFile);
        return new CvMappingValidator(mapping, client);
    }

    public CvMappingValidator(CvMapping mapping, OLSClient client) {
        this.mapping = mapping;
        this.client = client;
    }

    @Override
    public List<ValidationMessage> validate(MzTab mzTab) {
        final List<ValidationMessage> messages = new LinkedList<>();
        JXPathContext context = JXPathContext.newContext(mzTab);
        mapping.getCvMappingRuleList().
            getCvMappingRule().
            forEach((rule) ->
            {
                String path = rule.getCvElementPath();
                Iterator iterator = context.iterate(path);
                CvMappingRule.CvTermsCombinationLogic combinationLogic = rule.
                    getCvTermsCombinationLogic();
                CvMappingRule.RequirementLevel level = rule.
                    getRequirementLevel();
                messages.addAll(handleRule(rule, iterator));
            });
        return messages;
    }

    private Collection<ValidationMessage> handleRule(CvMappingRule rule,
        Iterator iterator) {
        // and logic means that ALL of the defined terms or their children MUST appear
        final List<ValidationMessage> messages = new LinkedList<>();
        final Set<String> specifiedRules = new LinkedHashSet<>();
        rule.getCvTerm().
            forEach((cvTerm) ->
            {
                specifiedRules.add(cvTerm.getTermAccession().
                    toUpperCase());
            });
        final Set<String> foundParameters = new LinkedHashSet<>();
        iterator.forEachRemaining((object) ->
        {
            Parameter p = (Parameter) object;
            if (!p.getCvAccession().
                contains(":")) {
                throw new RuntimeException(
                    "Malformed cv accession. Must be: CV:TERMNUMBER");
            }
            foundParameters.add(p.getCvAccession().
                toUpperCase());
        });
        switch (rule.getCvTermsCombinationLogic()) {
            case AND: // all defined terms or children thereof need to appear
                if (specifiedRules.containsAll(foundParameters) && foundParameters.
                    containsAll(specifiedRules)) {
                    messages.add(new ValidationMessage().code("XXXXXX").
                        messageType(ValidationMessage.MessageTypeEnum.WARN).
                        message(
                            "The following parameters were required but not present: " + specifiedRules));
                }
                break;
            case OR: // any of the terms or their children need to appear
                specifiedRules.removeAll(foundParameters);
                if (!specifiedRules.isEmpty()) {
                    messages.add(new ValidationMessage().code("XXXXXX").
                        messageType(ValidationMessage.MessageTypeEnum.WARN).
                        message(
                            "The following parameters were required but not present: " + specifiedRules));
                }
                break;
            case XOR:

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

    private Collection<ValidationMessage> handleOr(CvMappingRule rule,
        Iterator iterator) {
        // or logic means, any of the defined terms may be present, or none
        return Collections.emptyList();
    }

    private Collection<ValidationMessage> handleXor(CvMappingRule rule,
        Iterator iterator) {
        // xor logic means, if one of the defined terms is set, none of the others is allowed
        return Collections.emptyList();
    }

}
