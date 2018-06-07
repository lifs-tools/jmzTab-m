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
package de.isas.mztab2.validator;

import de.isas.mztab2.cvmapping.JxPathElement;
import static de.isas.mztab2.cvmapping.JxPathElement.toStream;
import static de.isas.mztab2.validator.MzTabValidatorTest.createTestFile;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class JxPathTest {

    @Test
    public void testMsRunParameterSelection() {
        MzTab mzTab = createTestFile();
        JXPathContext context = JXPathContext.newContext(mzTab);

        List<?> msRuns = (List<?>) context.getValue("/metadata/msRun",
            List.class);
        assertFalse(msRuns.isEmpty());
        assertEquals("file:///path/to/file1.mzML", toStream(context.
            getPointer("/metadata/msRun/@location"), String.class).
            findFirst().
            map((t) ->
            {
                System.out.println("Path: " + t.getLeft() + " to object: " + t.
                    getRight());
                return t;
            }).
            get().
            getValue());
        Stream<? extends String> stream = toStream(context.iterate(
            "/metadata/msRun/@location"), String.class);
        assertEquals("file:///path/to/file1.mzML", stream.findFirst().
            get());

        //scopePath /metadata/msrun
        Stream<Pair<Pointer, ? extends Parameter>> pointerFormatParameters = toStream(
            context.getPointer(
                "/metadata/msRun/@format"), Parameter.class);
        Pair<Pointer, ? extends Parameter> pair = pointerFormatParameters.
            findFirst().
            get();
        assertEquals("/metadata/msRun[1]/@format", pair.getKey().
            asPath());
        assertEquals("MS:1000584", pair.
            getValue().
            getCvAccession());

        Stream<? extends Parameter> formatParameters = toStream(context.iterate(
            "/metadata/msRun/@format"), Parameter.class);
        assertEquals("MS:1000584", formatParameters.findFirst().
            get().
            getCvAccession());
    }

    @Test
    public void testSampleCustomMultipleElementSelection() {
        MzTab mzTab = createTestFile();
        JXPathContext context = JXPathContext.newContext(mzTab);

        //retrieve multiple children with complete paths
        List<Pair<Pointer, ? extends Parameter>> customParameters = JxPathElement.
            toList(context,
                "/metadata/sample/@custom", Parameter.class);
        assertEquals(2, customParameters.size());
        assertEquals("Extraction date", customParameters.stream().
            findFirst().
            get().
            getRight().
            getName());
        assertEquals("/metadata/sample[1]/custom[1]", customParameters.stream().
            findFirst().
            get().
            getLeft().
            asPath());

        assertEquals("Extraction date", customParameters.get(0).
            getRight().
            getName());
        assertEquals("Extraction reason", customParameters.get(1).
            getRight().
            getName());
        assertEquals("/metadata/sample[1]/custom[1]", customParameters.get(0).
            getKey().
            asPath());
        assertEquals("/metadata/sample[1]/custom[2]", customParameters.get(1).
            getKey().
            asPath());
    }

}
