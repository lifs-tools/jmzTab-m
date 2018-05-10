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

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

/**
 *
 * @author nilshoffmann
 */
public class OlsLookupTest {
    
    public OlsLookupTest() {
    }

    @Test
    public void checkChildTermLookup() {    
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        Identifier ident = new Identifier("MS:1000831", Identifier.IdentifierType.OBO);
        List<Term> children = client.getTermChildren(ident, "MS", 5);
        Assert.assertTrue(children.size() > 0);
        System.out.println(children);
    }
}
