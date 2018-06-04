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

import de.isas.mztab2.cvmapping.ParameterComparisonResult;
import de.isas.mztab2.cvmapping.Parameters;
import de.isas.mztab2.cvmapping.CvParameterLookupService;
import de.isas.mztab2.model.Parameter;
import java.util.List;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class CvParameterLookupServiceTest {
    
    @Test
    public void checkChildTermLookup() {
        CvParameterLookupService service = new CvParameterLookupService();
        List<Parameter> children = service.resolveChildren(new Parameter().cvLabel("MS").cvAccession("MS:1000831"));
        Assert.assertTrue(children.size() > 0);
    }

    @Test
    public void testTermChildSearch() {
        CvParameterLookupService service = new CvParameterLookupService();
        List<Parameter> children = service.resolveChildren(new Parameter().cvLabel("MS").cvAccession("MS:1000560"));
        Assert.assertThat(children.size(), allOf(greaterThan(0),
            greaterThanOrEqualTo(1)));
    }
    

    @Test
    public void testTermParentSearch() {
        CvParameterLookupService service = new CvParameterLookupService();
        List<Parameter> parents = service.resolveParents(new Parameter().cvLabel("MS").cvAccession("MS:1000564"), 1);
        Assert.assertThat(parents.size(), is(1));
        assertTrue("Retrieved parent term equals reference", Parameters.isEqualTo(parents.get(0), new Parameter().cvAccession(
            "MS:1000560").
            cvLabel("MS").
            name("mass spectrometer file format")));
    }
    
    @Test
    public void testIsChildOf() {
        CvParameterLookupService service = new CvParameterLookupService();
        Parameter msFileFormat = new Parameter().cvLabel("MS").cvAccession("MS:1000560");
        ParameterComparisonResult childResult = service.isChildOfOrSame(msFileFormat, new Parameter().cvLabel("MS").cvAccession("MS:1000564"));
        Assert.assertSame(ParameterComparisonResult.CHILD_OF, childResult);
    }
    
    @Test
    public void testIsSame() {
        CvParameterLookupService service = new CvParameterLookupService();
        Parameter msFileFormat = new Parameter().cvLabel("MS").cvAccession("MS:1000560");
        ParameterComparisonResult identityResult = service.isChildOfOrSame(msFileFormat, msFileFormat);
        Assert.assertSame(ParameterComparisonResult.IDENTICAL, identityResult);
    }
    
    @Test
    public void testIsUnrelated() {
        CvParameterLookupService service = new CvParameterLookupService();
        Parameter msFileFormat = new Parameter().cvLabel("MS").cvAccession("MS:1000560");
        //compare unrelated branches ms file format and software
        ParameterComparisonResult childResult = service.isChildOfOrSame(msFileFormat, new Parameter().cvLabel("MS").cvAccession("MS:1000539"));
        Assert.assertSame(ParameterComparisonResult.NOT_RELATED, childResult);
    }
    
}
