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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.model.Parameter;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;

/**
 * The OLS current-release source against a local server in the shape of
 * OLS's {@code /api/ontologies/{id}}.
 */
public class OlsCurrentReleaseSourceTest {

    private TestServer server;
    private OlsCurrentReleaseSource source;

    @BeforeEach
    public void start() throws Exception {
        server = new TestServer();
        String host = server.base().substring("http://".length()) + "/ols4";
        source = new OlsCurrentReleaseSource(new OLSClient(new OLSWsConfig("http", host)));
    }

    @AfterEach
    public void stop() {
        server.close();
    }

    @Test
    public void theCurrentVersionIsTheOntologysConfigVersion() {
        server.route("/ols4/api/ontologies/ms", x -> {
            x.getResponseHeaders().add("Content-Type", "application/json");
            TestServer.send(x, 200, """
                {"ontologyId": "ms", "version": null, "status": "LOADED",
                 "config": {"id": "ms", "preferredPrefix": "MS", "version": "4.1.300",
                            "versionIri": "http://purl.obolibrary.org/obo/ms/4.1.300/ms.owl"}}
                """.getBytes(StandardCharsets.UTF_8));
        });

        assertEquals("OLS", source.name());
        assertEquals(Optional.of("4.1.300"), source.currentVersion("MS"));
    }

    @Test
    public void anUnknownOntologyOrOneWithoutAVersionHasNone() {
        server.route("/ols4/api/ontologies/nov", x -> {
            x.getResponseHeaders().add("Content-Type", "application/json");
            TestServer.send(x, 200, "{\"ontologyId\": \"nov\", \"config\": {\"id\": \"nov\"}}"
                .getBytes(StandardCharsets.UTF_8));
        });

        assertEquals(Optional.empty(), source.currentVersion("NOV"));
        assertEquals(Optional.empty(), source.currentVersion("XYZ"));
    }

    @Test
    public void withoutAConfigVersionTheVersionIriNamesIt() {
        // OLS4 reports PSI-MS this way: no config.version, a versioned PURL as versionIri
        ontology("ms", "\"versionIri\": \"http://purl.obolibrary.org/obo/ms/4.2.15/ms.owl\"");
        ontology("doid", "\"version\": \"\", \"versionIri\": \"http://purl.obolibrary.org/obo/doid/releases/2026-10-29/doid.owl\"");
        ontology("nov", "\"versionIri\": \"http://purl.obolibrary.org/obo/nov.owl\"");

        assertEquals(Optional.of("4.2.15"), source.currentVersion("MS"));
        assertEquals(Optional.of("2026-10-29"), source.currentVersion("DOID"));
        // a versionIri that names no version: none, as without one
        assertEquals(Optional.empty(), source.currentVersion("NOV"));
    }

    @Test
    public void anOfflineSourceNamesNothingAndIsNeverAsked() {
        CurrentReleaseSource offline = CurrentReleaseSource.offline("OLS");

        assertEquals("OLS (not asked offline)", offline.name());
        assertEquals(Optional.empty(), offline.currentVersion("MS"));
        assertThrows(IllegalStateException.class,
            () -> offline.ancestors(new Parameter().cvLabel("MS").cvAccession("MS:1000073")));
    }

    private void ontology(String id, String config) {
        server.route("/ols4/api/ontologies/" + id, x -> {
            x.getResponseHeaders().add("Content-Type", "application/json");
            TestServer.send(x, 200, ("{\"ontologyId\": \"" + id + "\", \"config\": {\"id\": \"" + id + "\", "
                + config + "}}").getBytes(StandardCharsets.UTF_8));
        });
    }
}
