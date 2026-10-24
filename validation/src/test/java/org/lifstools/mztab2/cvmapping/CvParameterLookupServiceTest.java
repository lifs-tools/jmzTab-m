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

import org.lifstools.mztab2.model.Parameter;
import java.util.List;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author nilshoffmann
 */
public class CvParameterLookupServiceTest {
    
    @Test
    public void checkChildTermLookup() {
        CvParameterLookupService service = new CvParameterLookupService();
        List<Parameter> children = service.resolveChildren(new Parameter().cvLabel("MS").cvAccession("MS:1000831"));
        assertTrue(children.size() > 0);
    }

    @Test
    public void testTermChildSearch() {
        CvParameterLookupService service = new CvParameterLookupService();
        List<Parameter> children = service.resolveChildren(new Parameter().cvLabel("MS").cvAccession("MS:1000560"));
        assertThat(children.size(), allOf(greaterThan(0),
            greaterThanOrEqualTo(1)));
    }
    

    @Test
    public void testTermParentSearch() {
        CvParameterLookupService service = new CvParameterLookupService();
        List<Parameter> parents = service.resolveParents(new Parameter().cvLabel("MS").cvAccession("MS:1000564"), 1);
        assertThat(parents.size(), is(1));
        assertTrue(CvMappingUtils.isEqualTo(parents.get(0), new Parameter().cvAccession(
            "MS:1000560").
            cvLabel("MS").
            name("mass spectrometer file format")), "Retrieved parent term equals reference");
    }
    
    @Test
    public void testIsChildOf() {
        CvParameterLookupService service = new CvParameterLookupService();
        Parameter msFileFormat = new Parameter().cvLabel("MS").cvAccession("MS:1000560");
        ParameterComparisonResult childResult = service.isChildOfOrSame(msFileFormat, new Parameter().cvLabel("MS").cvAccession("MS:1000564"));
        assertSame(ParameterComparisonResult.CHILD_OF, childResult);
    }
    
    @Test
    public void testIsSame() {
        CvParameterLookupService service = new CvParameterLookupService();
        Parameter msFileFormat = new Parameter().cvLabel("MS").cvAccession("MS:1000560");
        ParameterComparisonResult identityResult = service.isChildOfOrSame(msFileFormat, msFileFormat);
        assertSame(ParameterComparisonResult.IDENTICAL, identityResult);
    }
    
    @Test
    public void testIsUnrelated() {
        CvParameterLookupService service = new CvParameterLookupService();
        Parameter msFileFormat = new Parameter().cvLabel("MS").cvAccession("MS:1000560");
        //compare unrelated branches ms file format and software
        ParameterComparisonResult childResult = service.isChildOfOrSame(msFileFormat, new Parameter().cvLabel("MS").cvAccession("MS:1000539"));
        assertSame(ParameterComparisonResult.NOT_RELATED, childResult);
    }
    
}
