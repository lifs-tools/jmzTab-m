/*
 * Copyright 2019 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package org.lifstools.mztab2.io.validators;

import org.lifstools.mztab2.io.validators.SpectraRefValidator;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.SpectraRef;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.REGEX_SPECTRA_REF_THERMO_NATIVE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.REGEX_SPECTRA_REF_WATERS_NATIVE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.REGEX_SPECTRA_REF_WIFF_NATIVE;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn;
import static uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn.Stable.SPECTRA_REF;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 *
 * @author nilshoffmann
 */
public class SpectraRefValidatorTest {

    /**
     * Test of validateLine method, of class SpectraRefValidator.
     */
    @Test
    public void testValidateOfEmptyRefList() {
        int lineNumber = 0;
        MZTabParserContext parserContext = new MZTabParserContext();
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        List<MZTabError> result = instance.validateLine(
            lineNumber, 
            parserContext, 
            column, 
            "", 
            Collections.emptyList()
        );
        assertFalse("Did not expect errors here: "+result, result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1026, result.get(0).getType().getCode().longValue());
        System.out.println(result.get(0));
    }
    
    @Test
    public void testValidateOfNullRunLocation() {
        int lineNumber = 0;
        MZTabParserContext parserContext = new MZTabParserContext();
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1000768").name("Thermo nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location(null);
        List<MZTabError> result = instance.validateLine(
            lineNumber, 
            parserContext, 
            column, 
            "ms_run[1]:controllerType=0 controllerNumber=1 scan=1", 
            Arrays.asList(new SpectraRef().msRun(msRun).reference("controllerType=0 controllerNumber=1 scan=1"))
        );
        assertFalse("Did not expect errors here: "+result, result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(2021, result.get(0).getType().getCode().longValue());
        System.out.println(result.get(0));
    }
    
    /**
     * Test of validateLine method, of class SpectraRefValidator.
     */
    @Test
    public void testValidateLineWithUnsupportedRefIdFormat() {
        int lineNumber = 0;
        MZTabParserContext parserContext = new MZTabParserContext();
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1001526").name("spectrum from database integer nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location("file:///some/path/to/file.mzML");
        List<MZTabError> result = instance.validateLine(
            lineNumber, 
            parserContext, 
            column, 
            "ms_run[1]:databasekey=1231", 
            Arrays.asList(new SpectraRef().msRun(msRun).reference("databasekey=1231"))
        );
        assertFalse("Did not expect errors here: "+result, result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(2051, result.get(0).getType().getCode().longValue());
        System.out.println(result.get(0));
    }
    
    /**
     * Test of validateLine method, of class SpectraRefValidator.
     */
    @Test
    public void testValidateLineWithOneSpectraRefThermo() {
        int lineNumber = 0;
        MZTabParserContext parserContext = new MZTabParserContext();
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1000768").name("Thermo nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location("file:///some/path/to/file.mzML");
        List<MZTabError> result = instance.validateLine(
            lineNumber, 
            parserContext, 
            column, 
            "ms_run[1]:controllerType=0 controllerNumber=1 scan=1", 
            Arrays.asList(new SpectraRef().msRun(msRun).reference("controllerType=0 controllerNumber=1 scan=1"))
        );
        assertTrue("Did not expect errors here: "+result, result.isEmpty());
        assertEquals(0, result.size());
        
        result = instance.validateLine(
            lineNumber, 
            parserContext, 
            column, 
            "ms_run[1]:controllerType=0 controllerNumber=1 scan=0", 
            Arrays.asList(new SpectraRef().msRun(msRun).reference("controllerType=0 controllerNumber=1 scan=0"))
        );
        assertFalse("Did not expect this to pass!", result.isEmpty());
        assertEquals(1, result.size());
    }
    
    /**
     * Test of validateLine method, of class SpectraRefValidator.
     */
    @Test
    public void testValidateLineWithOneSpectraRef() {
        int lineNumber = 0;
        MZTabParserContext parserContext = new MZTabParserContext();
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1000776").name("scan number only nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location("file:///some/path/to/file.mzML");
        List<MZTabError> result = instance.validateLine(
            lineNumber, 
            parserContext, 
            column, 
            "ms_run[1]:scan=0", 
            Arrays.asList(new SpectraRef().msRun(msRun).reference("scan=0"))
        );
        assertTrue("Did not expect errors here: "+result, result.isEmpty());
        assertEquals(0, result.size());
        
        result = instance.validateLine(
            lineNumber, 
            parserContext, 
            column, 
            "ms_run[1]:scan=-1", 
            Arrays.asList(new SpectraRef().msRun(msRun).reference("scan=-1"))
        );
        assertFalse("Did not expect this to pass!", result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test()
    public void testValidatePatternThermo() {
        Pattern pattern = Pattern.compile(REGEX_SPECTRA_REF_THERMO_NATIVE);
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1000768").name("Thermo nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location("file:///some/path/to/file.mzML");

        checkThatPatternFails(instance, pattern, "controllerType=1 controllerNumber=22 scan=-1", column, msRun);
        checkThatPatternFails(instance, pattern, "controllerType=1 controllerNumber=22 scan=0", column, msRun);
        checkThatPatternFails(instance, pattern, "controllerType=1 controllerNumber=0 scan=1", column, msRun);
        checkThatPatternFails(instance, pattern, "controllerType=1 controllerNumber=-1 scan=1", column, msRun);
        checkThatPatternFails(instance, pattern, "controllerType=-1 controllerNumber=1 scan=1", column, msRun);
        //controller type can be 0, controllerNumber must be > 0, scan must be > 0
        checkThatPatternMatches(instance, pattern, "controllerType=0 controllerNumber=1 scan=1", column, msRun);
    }

    @Test
    public void testValidatePatternWaters() {
        Pattern pattern = Pattern.compile(REGEX_SPECTRA_REF_WATERS_NATIVE);
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1000769").name("Waters nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location("file:///some/path/to/file.mzML");

        checkThatPatternFails(instance, pattern, "function=0 process=22 scan=0", column, msRun);
        checkThatPatternFails(instance, pattern, "function=1 process=22 scan=-1", column, msRun);
        checkThatPatternFails(instance, pattern, "function=1 process=-1 scan=0", column, msRun);
        //function must be >0, process must be >= 0, scan must be >= 0
        checkThatPatternMatches(instance, pattern, "function=1 process=0 scan=0", column, msRun);
    }

    @Test
    public void testValidatePatternWiff() {
        Pattern pattern = Pattern.compile(REGEX_SPECTRA_REF_WIFF_NATIVE);
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1000770").name("WIFF nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location("file:///some/path/to/file.mzML");

        checkThatPatternFails(instance, pattern, "sample=0 period=22 cycle=0 experiment=-1", column, msRun);
        checkThatPatternFails(instance, pattern, "sample=0 period=22 cycle=-1 experiment=0", column, msRun);
        checkThatPatternFails(instance, pattern, "sample=0 period=-1 cycle=0 experiment=0", column, msRun);
        checkThatPatternFails(instance, pattern, "sample=-1 period=0 cycle=0 experiment=0", column, msRun);
        //sample must be >=0, period must be >= 0, cycle must be >= 0, experiment must be >= 0
        checkThatPatternMatches(instance, pattern, "sample=0 period=0 cycle=0 experiment=1", column, msRun);
    }

    @Test
    public void testValidatePatternSpectraRefIndex() {
        Pattern pattern = Pattern.compile(MZTabConstants.REGEX_SPECTRA_REF_INDEX);
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1000774").name("multiple peak list nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location("file:///some/path/to/file.mzML");

        checkThatPatternFails(instance, pattern, "index=-1", column, msRun);
        //index must be >= 0
        checkThatPatternMatches(instance, pattern, "index=0", column, msRun);
    }

    @Test
    public void testValidatePatternSpectraRefScan() {
        Pattern pattern = Pattern.compile(MZTabConstants.REGEX_SPECTRA_REF_SCAN);
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1000776").name("scan number only nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location("file:///some/path/to/file.mzML");

        checkThatPatternFails(instance, pattern, "scan=-1", column, msRun);
        //scan must be >= 0
        checkThatPatternMatches(instance, pattern, "scan=0", column, msRun);
    }

    @Test
    public void testValidatePatternSpectraRefSpectrum() {
        Pattern pattern = Pattern.compile(MZTabConstants.REGEX_SPECTRA_REF_SPECTRUM);
        IMZTabColumn column = SmallMoleculeEvidenceColumn.Stable.columnFor(SPECTRA_REF);
        SpectraRefValidator instance = new SpectraRefValidator();
        Parameter param = new Parameter().id(1).cvLabel("MS").cvAccession("MS:1000777").name("spectrum identifier nativeID format");
        MsRun msRun = new MsRun().id(1).idFormat(param).format(new Parameter().id(2).cvLabel("MS").cvAccession("MS:1000584").name("mzML file")).location("file:///some/path/to/file.mzML");

        checkThatPatternFails(instance, pattern, "spectrum=-1", column, msRun);
        //spectrum must be >= 0
        checkThatPatternMatches(instance, pattern, "spectrum=0", column, msRun);
    }

    private void checkThatPatternMatches(SpectraRefValidator instance, Pattern pattern, String reference, IMZTabColumn column, MsRun msRun) {
        assertTrue(pattern.matcher(reference).matches());
        Optional<MZTabError> optionalError = instance.validatePattern(pattern, reference, 0, column, "ms_run[1]:" + reference, msRun);
        assertFalse(optionalError.isPresent());
    }

    private void checkThatPatternFails(SpectraRefValidator instance, Pattern pattern, String reference, IMZTabColumn column, MsRun msRun) {
        assertFalse(pattern.matcher(reference).matches());
        Optional<MZTabError> optionalError = instance.validatePattern(pattern, reference, 0, column, "ms_run[1]:" + reference, msRun);
        assertTrue(optionalError.isPresent());
    }

}
