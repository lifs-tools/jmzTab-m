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
package de.isas.mztab2.cvmapping;

import de.isas.mztab2.model.Parameter;
import java.util.List;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import static de.isas.mztab2.cvmapping.Terms.isEqualTo;

/**
 *
 * @author nilshoffmann
 */
public class OlsLookupServiceTest {

    @Test
    public void checkChildTermLookup() {
        
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        Identifier ident = new Identifier("MS:1000831",
            Identifier.IdentifierType.OBO);
        List<Term> children = client.getTermChildren(ident, "MS", 5);
        Assert.assertTrue(children.size() > 0);
        System.out.println(children);
    }

    @Test
    public void testTermChildSearch() {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        //ms file format
        Identifier ident = new Identifier("MS:1000560",
            Identifier.IdentifierType.OBO);
        //get immediate children
        List<Term> children = client.getTermChildren(ident, "MS", 1);
        Assert.assertThat(children.size(), allOf(greaterThan(0),
            greaterThanOrEqualTo(1)));
        //query explicitly for child_of relation for given term
    }
    
    @Test
    public void testTermChildSearchUnlimited() {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        //ms file format
        Identifier ident = new Identifier("MS:1000560",
            Identifier.IdentifierType.OBO);
        //get immediate children
        List<Term> children = client.getTermChildren(ident, "MS", -1);
        System.out.println("Found "+children.size()+" child terms of "+ident.getIdentifier());
        Assert.assertThat(children.size(), allOf(greaterThan(0),
            greaterThanOrEqualTo(1)));
        //query explicitly for child_of relation for given term
    }

    @Test
    public void testTermParentSearch() {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        //ms file format
        Identifier ident = new Identifier("MS:1000564",
            Identifier.IdentifierType.OBO);
        //get immediate parent
        List<Term> parents = client.getTermParents(ident, "MS", 1);
        Assert.assertThat(parents.size(), is(1));
        assertTrue("Retrieved parent term equals reference", isEqualTo(parents.get(0), new Parameter().cvAccession(
            "MS:1000560").
            cvLabel("MS").
            name("mass spectrometer file format")));
    }

    
}
