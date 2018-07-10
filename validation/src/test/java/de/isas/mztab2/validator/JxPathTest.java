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
import de.isas.mztab2.model.Instrument;
import static de.isas.mztab2.validator.MzTabValidatorTest.createTestFile;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.Parameter;
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
        Stream<Pair<Pointer, Parameter>> pointerFormatParameters = toStream(
            context.getPointer(
                "/metadata/msRun/@format"), Parameter.class);
        Pair<Pointer, Parameter> pair = pointerFormatParameters.
            findFirst().
            get();
        assertEquals("/metadata/msRun[1]/@format", pair.getKey().
            asPath());
        assertEquals("MS:1000584", pair.
            getValue().
            getCvAccession());
        
        Stream<Parameter> formatParameters = toStream(context.iterate(
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
        List<Pair<Pointer, Parameter>> customParameters = JxPathElement.
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
    
    @Test    
    public void testInstrumentSelection() {
        MzTab mzTab = createTestFile();
        JXPathContext context = JXPathContext.newContext(mzTab);
        /*
        MTD	instrument[1]-name	[MS, MS:1001742, LTQ Orbitrap Velos, ]
MTD	instrument[1]-source	[MS, MS:1000073, Electrospray Ionization, ]
MTD	instrument[1]-analyzer[1]	[MS, MS:1000484, orbitrap, ]
MTD	instrument[1]-analyzer[2]	[MS, MS:1000084, time-of-flight, ] <- just for testing
MTD	instrument[1]-detector	[MS, MS:1000112, Faraday Cup, ]
         */
        Instrument instrument = new Instrument().id(1).
            name(new Parameter().cvLabel("MS").
                cvAccession("MS:1001742").
                name("LTQ Orbitrap Velos")).
            source(new Parameter().cvLabel("MS").
                cvAccession("MS:1000073").
                name("Electrospray Ionization")).
            addAnalyzerItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1000484").
                name("orbitrap")).
            addAnalyzerItem(new Parameter().cvLabel("MS").
                cvAccession("MS:1000084").
                name("time-of-flight")).
            detector(new Parameter().cvLabel("MS").
                cvAccession("MS:1000112").
                name("Faraday Cup"));
        mzTab.getMetadata().addInstrumentItem(instrument);
        
        List<Pair<Pointer, Parameter>> instrumentName = JxPathElement.
            toList(context,
                "/metadata/instrument/@name", Parameter.class);
        
        assertEquals(1, instrumentName.size());
        
        List<Pair<Pointer, Parameter>> instrumentSource = JxPathElement.
            toList(context,
                "/metadata/instrument/@source", Parameter.class);
        assertEquals(1, instrumentSource.size());
        
        List<Pair<Pointer, Parameter>> instrumentAnalyzer = JxPathElement.
            toList(context,
                "/metadata/instrument/@analyzer", Parameter.class);
        assertEquals(2, instrumentAnalyzer.size());
        
        List<Pair<Pointer, Parameter>> instrumentDetector = JxPathElement.
            toList(context,
                "/metadata/instrument/@detector", Parameter.class);
        assertEquals(1, instrumentDetector.size());
        
    }
    
}
