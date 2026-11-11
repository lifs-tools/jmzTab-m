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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.model.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Tests that {@link CvParameterLookupService} takes its ancestors from an
 * {@link AncestorSource}, without any network access.
 */
public class AncestorSourceTest {

    private static final Parameter IONIZATION_TYPE = param("MS", "MS:1000008", "ionization type");
    private static final Parameter ESI = param("MS", "MS:1000073", "electrospray ionization");
    private static final Parameter UNRELATED = param("MS", "MS:1000031", "instrument model");

    private static Parameter param(String label, String accession, String name) {
        return new Parameter().cvLabel(label).cvAccession(accession).name(name);
    }

    /**
     * A source backed by a map, counting the calls it receives.
     */
    private static class FakeSource implements AncestorSource {

        final Map<Parameter, List<Parameter>> ancestors = new HashMap<>();
        final AtomicInteger calls = new AtomicInteger();

        @Override
        public List<Parameter> ancestors(Parameter term) throws HttpClientErrorException {
            calls.incrementAndGet();
            List<Parameter> result = ancestors.get(term);
            if (result == null) {
                throw HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
            }
            return result;
        }
    }

    @Test
    public void isChildOfOrSameUsesTheSource() {
        FakeSource source = new FakeSource();
        source.ancestors.put(ESI, List.of(IONIZATION_TYPE));
        source.ancestors.put(UNRELATED, List.of());
        CvParameterLookupService service = new CvParameterLookupService(source);
        assertEquals(ParameterComparisonResult.CHILD_OF, service.isChildOfOrSame(IONIZATION_TYPE, ESI));
        assertEquals(ParameterComparisonResult.NOT_RELATED, service.isChildOfOrSame(IONIZATION_TYPE, UNRELATED));
        assertEquals(ParameterComparisonResult.IDENTICAL, service.isChildOfOrSame(ESI, ESI));
    }

    @Test
    public void answersAreCachedPerParameter() {
        FakeSource source = new FakeSource();
        source.ancestors.put(ESI, List.of(IONIZATION_TYPE));
        CvParameterLookupService service = new CvParameterLookupService(source);
        List<Parameter> first = service.resolveParents(ESI);
        List<Parameter> second = service.resolveParents(ESI);
        assertSame(first, second);
        assertEquals(1, source.calls.get());
    }

    @Test
    public void aNotFoundFromTheSourceReachesTheCaller() {
        FakeSource source = new FakeSource();
        CvParameterLookupService service = new CvParameterLookupService(source);
        HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () -> service.resolveParents(ESI));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
}
