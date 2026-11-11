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
package org.lifstools.mztab2.cvmapping;

import java.util.List;
import java.util.stream.Collectors;
import org.lifstools.mztab2.model.Parameter;
import org.springframework.web.client.HttpClientErrorException;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;

/**
 * An {@link AncestorSource} that asks the EBI OLS service.
 */
public class OlsAncestorSource implements AncestorSource {

    private final OLSClient client;

    /**
     * A source that asks OLS through the given client.
     *
     * @param client the OLS client to ask
     */
    public OlsAncestorSource(OLSClient client) {
        this.client = client;
    }

    @Override
    public List<Parameter> ancestors(Parameter term) throws HttpClientErrorException {
        Identifier ident = new Identifier(term.getCvAccession(),
            Identifier.IdentifierType.OBO);
        return client.getTermParents(ident, term.getCvLabel(), -1).
            stream().
            map(CvMappingUtils::asParameter).
            collect(Collectors.toList());
    }
}
