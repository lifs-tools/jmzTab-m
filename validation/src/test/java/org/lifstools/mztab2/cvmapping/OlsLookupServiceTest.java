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
package org.lifstools.mztab2.cvmapping;

import static org.lifstools.mztab2.cvmapping.CvMappingUtils.isEqualTo;
import org.lifstools.mztab2.model.Parameter;
import java.util.List;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

/**
 *
 * @author nilshoffmann
 */
public class OlsLookupServiceTest {

    @Test
    public void checkChildTermLookupInvalidPrefix() {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        Identifier ident = new Identifier("chmo:0000524",
            Identifier.IdentifierType.OBO);
        List<Term> children = client.searchTermById(ident.getIdentifier(), "CHMO");
        assertTrue(children.size() > 0);
        ident.setIdentifier("chmo:0000524");
        children = client.searchTermById(ident.getIdentifier(), "chmo");
        assertTrue(children.size() > 0);
    }

    @Test
    public void checkChildTermLookup() {
        OLSWsConfig config = new OLSWsConfig();
        OLSClient client = new OLSClient(config);
        Identifier ident = new Identifier("MS:1000831",
            Identifier.IdentifierType.OBO);
        List<Term> children = client.getTermChildren(ident, "MS", 5);
        assertTrue(children.size() > 0);
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
        assertThat(children.size(), allOf(greaterThan(0),
            greaterThanOrEqualTo(1)));
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
        assertThat(children.size(), allOf(greaterThan(0),
            greaterThanOrEqualTo(1)));
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
        assertThat(parents.size(), is(1));
        assertTrue(isEqualTo(parents.get(0), new Parameter().cvAccession(
            "MS:1000560").
            cvLabel("MS").
            name("mass spectrometer file format")), "Retrieved parent term equals reference");
    }

    @Test
    public void testDefaultConfigUsesOls4() {
        OLSWsConfig config = CvParameterLookupService.defaultConfig();
        assertThat(config.getProtocol() + "://" + config.getHostName(), is(
            "https://www.ebi.ac.uk/ols4"));
    }

    @Test
    public void testDefaultLookupServiceResolvesParents() {
        List<Parameter> parents = new CvParameterLookupService().
            resolveParents(new Parameter().cvLabel("MS").
                cvAccession("MS:1000564").
                name("PSI mzData format"), 1);
        assertThat(parents.size(), is(1));
        assertThat(parents.get(0).
            getCvAccession(), is("MS:1000560"));
    }

}
