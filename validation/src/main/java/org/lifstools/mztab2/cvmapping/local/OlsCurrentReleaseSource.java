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
package org.lifstools.mztab2.cvmapping.local;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.lifstools.mztab2.cvmapping.OlsAncestorSource;
import org.lifstools.mztab2.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Config;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;

/**
 * The EBI OLS service as a {@link CurrentReleaseSource}: ancestors as
 * {@link OlsAncestorSource} gives them, and the version from the ontology's
 * {@code config.version} of {@code /api/ontologies/{id}}, read with
 * {@link OLSClient#getOntology(String)}.
 */
public final class OlsCurrentReleaseSource implements CurrentReleaseSource {

    private static final Logger LOG = LoggerFactory.getLogger(OlsCurrentReleaseSource.class);

    private final OLSClient client;
    private final OlsAncestorSource ancestors;

    /**
     * A source that asks the given client.
     *
     * @param client the OLS client to ask
     */
    public OlsCurrentReleaseSource(OLSClient client) {
        this.client = client;
        this.ancestors = new OlsAncestorSource(client);
    }

    /**
     * The name in messages.
     *
     * @return {@code OLS}
     */
    @Override
    public String name() {
        return "OLS";
    }

    /**
     * Asks OLS for the ontology whose id is the label in lower case (OLS's
     * ontology ids are lower case, e.g. {@code ms}).
     *
     * @return {@code config.version}; else the version that
     * {@code config.versionIri} names as a versioned OBO PURL
     * ({@code .../obo/ms/4.2.15/ms.owl} or
     * {@code .../obo/doid/releases/2026-10-29/doid.owl}), as OLS4 reports
     * e.g. PSI-MS; else empty: OLS has no such ontology, names no version,
     * or cannot be reached
     */
    @Override
    public Optional<String> currentVersion(String cvLabel) {
        try {
            Ontology ontology = client.getOntology(cvLabel.toLowerCase(Locale.ROOT));
            Config config = ontology == null ? null : ontology.getConfig();
            if (config == null) {
                return Optional.empty();
            }
            Optional<String> version = Optional.ofNullable(config.getVersion()).filter(v -> !v.isBlank());
            return version.isPresent() ? version
                : Optional.ofNullable(config.getVersionIri()).flatMap(VocabularyCache::purlVersion);
        } catch (RestClientException e) {
            LOG.debug("OLS names no current version of {}: {}", cvLabel, e.toString());
            return Optional.empty();
        }
    }

    @Override
    public List<Parameter> ancestors(Parameter term) throws HttpClientErrorException {
        return ancestors.ancestors(term);
    }
}
