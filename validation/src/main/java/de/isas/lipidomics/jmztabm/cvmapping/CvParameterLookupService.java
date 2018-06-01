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
package de.isas.lipidomics.jmztabm.cvmapping;

import de.isas.mztab1_1.model.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;

/**
 * Abstraction over OLSClient to autoconvert Terms to Parameters and to allow
 * easy matching of Parameters against parent terms and their children.
 *
 * @author nilshoffmann
 */
public class CvParameterLookupService {

    private final OLSClient client;

    public CvParameterLookupService() {
        this(new OLSClient(new OLSWsConfig()));
    }

    public CvParameterLookupService(OLSClient client) {
        this.client = client;
    }

    public CvParameterLookupService(OLSWsConfig config) {
        this(new OLSClient(config));
    }

    List<Parameter> resolveParents(Parameter parameter, int levels) {
        if (parameter.getCvAccession() == null || parameter.getCvLabel() == null) {
            throw new IllegalArgumentException(
                "Parameter must provide cvAccession and cvLabel!");
        }
        Identifier ident = new Identifier(parameter.getCvAccession(),
            Identifier.IdentifierType.OBO);
        return client.getTermParents(ident, parameter.getCvLabel(), levels).
            stream().
            map(Terms::asParameter).
            collect(Collectors.toList());
    }

    List<Parameter> resolveChildren(Parameter parameter) {
        if (parameter.getCvAccession() == null || parameter.getCvLabel() == null) {
            throw new IllegalArgumentException(
                "Parameter must provide cvAccession and cvLabel!");
        }
        Identifier ident = new Identifier(parameter.getCvAccession(),
            Identifier.IdentifierType.OBO);
        return client.getTermChildren(ident, parameter.getCvLabel(), -1).
            stream().
            map(Terms::asParameter).
            collect(Collectors.toList());
    }

    public ParameterComparisonResult isChildOfOrSame(Parameter parent,
        Parameter potentialChild) {
        if (parent.getCvAccession().
            equals(potentialChild.getCvAccession())) {
            return ParameterComparisonResult.IDENTICAL;
        }
        List<Parameter> childrenOf = resolveChildren(parent);
        boolean result = childrenOf.stream().
            anyMatch((child) ->
            {
                return Parameters.isEqualTo(potentialChild, child);
            });
        if (result) {
            return ParameterComparisonResult.CHILD_OF;
        }
        return ParameterComparisonResult.NOT_RELATED;
    }

}
