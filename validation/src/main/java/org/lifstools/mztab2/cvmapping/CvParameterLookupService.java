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
package org.lifstools.mztab2.cvmapping;

import org.lifstools.mztab2.model.Parameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;

/**
 * Abstraction over OLSClient to autoconvert Terms to Parameters and to allow
 * easy matching of Parameters against parent terms and their children.
 *
 * @author nilshoffmann
 */
@Slf4j
public class CvParameterLookupService {

    private final OLSClient client;
    private final Map<Parameter, List<Parameter>> childCache;
    private final Map<Parameter, List<Parameter>> parentCache;

    private static <K, V> Map<K, V> lruCache(final int maxSize) {
        return new LinkedHashMap<K, V>(maxSize * 4 / 3, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
    }

    /**
     * Create a new instance of the lookup service with default OLS configuration.
     */
    public CvParameterLookupService() {
        this(new OLSClient(new OLSWsConfig()));
    }

    /**
     * Create a new instance of the lookup service with a custom OLSClient.
     * @param client the custom OLS client
     */
    public CvParameterLookupService(OLSClient client) {
        this.client = client;
        this.childCache = lruCache(4096);
        this.parentCache = lruCache(4096);
    }

    /**
     * Create a new instance of the lookup service with a custom OLSWsConfig configuration.
     * @param config the custom configuration
     */
    public CvParameterLookupService(OLSWsConfig config) {
        this(new OLSClient(config));
    }
    
    /**
     * Clears all query result caches (parent and child).
     */
    public void clearCaches() {
        this.childCache.clear();
        this.parentCache.clear();
    }

    /**
     * Resolve all parents of parameter up to an arbitrary depth (actually height, since we go from bottom to top).
     * Use at your own risk, the OLS service may terminate your connection if the response is too large or takes too long.
     * @param parameter the parameter to start from
     * @return a list of all parent parameters for the given parameter
     * @throws org.springframework.web.client.HttpClientErrorException on http related errors
     */
    public List<Parameter> resolveParents(Parameter parameter) throws org.springframework.web.client.HttpClientErrorException {
        return resolveParents(parameter, -1);
    }

    /**
     * Resolve all parents of a parameter up to a given maximum depth (1 meaning the immediate parents, -1 meaning all).
     * @param parameter the parameter to start from
     * @param levels maximum levels to query
     * @return a list of all parent parameters for the given parameter
     * @throws org.springframework.web.client.HttpClientErrorException on http related errors
     */
    public List<Parameter> resolveParents(Parameter parameter, int levels) throws org.springframework.web.client.HttpClientErrorException {
        if (parameter.getCvAccession() == null || parameter.getCvLabel() == null) {
            throw new IllegalArgumentException(
                "Parameter must provide cvAccession and cvLabel!");
        }
        if(parentCache.containsKey(parameter)) {
            log.debug("Cache hit for parameter "+parameter+" in parent cache!");
            return parentCache.get(parameter);
        }
        Identifier ident = new Identifier(parameter.getCvAccession(),
            Identifier.IdentifierType.OBO);
        List<Parameter> parents = client.getTermParents(ident, parameter.getCvLabel(), levels).
            stream().
            map(CvMappingUtils::asParameter).
            collect(Collectors.toList());
        parentCache.put(parameter, parents);
        return parents;
    }

    /**
     * Resolve all children of a parameter up to a given maximum depth (1 meaning immediate children, -1 meaning all).
     * @param parameter the parameter to start from
     * @param levels maximum levels to query
     * @return a list of all child parameters for the given parameter
     * @throws org.springframework.web.client.HttpClientErrorException on http related errors
     */
    public List<Parameter> resolveChildren(Parameter parameter, int levels) throws org.springframework.web.client.HttpClientErrorException {
        if (parameter.getCvAccession() == null || parameter.getCvLabel() == null) {
            throw new IllegalArgumentException(
                "Parameter must provide cvAccession and cvLabel!");
        }
        if(childCache.containsKey(parameter)) {
            log.debug("Cache hit for parameter "+parameter+" in child cache!");
            return childCache.get(parameter);
        }
        Identifier ident = new Identifier(parameter.getCvAccession(),
            Identifier.IdentifierType.OBO);
        List<Parameter> children = client.getTermChildren(ident, parameter.getCvLabel(), levels).
            stream().
            map(CvMappingUtils::asParameter).
            collect(Collectors.toList());
        childCache.put(parameter, children);
        return children;
    }

    /**
     * Resolve all children of a parameter up to an arbitrary depth.
     * Use at your own risk, the OLS service may terminate your connection if the response is too large or takes too long.
     * @param parameter the parameter to start from
     * @return a list of all child parameters for the given parameter
     * @throws org.springframework.web.client.HttpClientErrorException on http related errors
     */
    public List<Parameter> resolveChildren(Parameter parameter) throws org.springframework.web.client.HttpClientErrorException {
        return resolveChildren(parameter, -1);
    }

    /**
     * Compares two parameters for their parent to child relationship. The result can be one of: IDENTICAL, if parent and potential child are the same node,
     * CHILD_OF, if potentialChild is a child of parent (at least 1 level away), or NOT_RELATED, if there is no path from child to parent.
     * @param parent the parent term to start from
     * @param potentialChild the potential child term to check against parent
     * @return the comparison result
     * @throws org.springframework.web.client.HttpClientErrorException on http related errors
     */
    public ParameterComparisonResult isChildOfOrSame(Parameter parent,
        Parameter potentialChild) throws org.springframework.web.client.HttpClientErrorException {
        if (parent.getCvAccession().
            toUpperCase().
            equals(potentialChild.getCvAccession().
                toUpperCase())) {
            return ParameterComparisonResult.IDENTICAL;
        }
        List<Parameter> parentsOf = resolveParents(potentialChild);
        boolean result = parentsOf.stream().
            anyMatch((potentialParent) ->
            {
                return CvMappingUtils.isEqualTo(potentialParent, parent);
            });
        if (result) {
            return ParameterComparisonResult.CHILD_OF;
        }
        return ParameterComparisonResult.NOT_RELATED;
    }

}
