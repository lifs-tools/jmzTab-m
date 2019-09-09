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
package de.isas.mztab2.io.validators;

import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.SpectraRef;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 *
 * @author nilshoffmann
 */
public class SpectraRefValidator implements FieldValidator<List<SpectraRef>> {

    @Override
    public List<MZTabError> validateLine(int lineNumber, MZTabParserContext parserContext, IMZTabColumn column, String field, List<SpectraRef> refList) {
        List<MZTabError> errorList = new LinkedList<>();
        if (refList.isEmpty()) {
            errorList.add(new MZTabError(FormatErrorType.SpectraRef,
                    lineNumber, column.getHeader(), field));
        } else {
            for (SpectraRef ref : refList) {
                MsRun run = ref.getMsRun();
                if (!Optional.ofNullable(run.getLocation()).isPresent()) {
                    //As the location can be null and the field is mandatory, this is not an error, it is a warning
                    errorList.add(new MZTabError(
                            LogicalErrorType.SpectraRef, lineNumber, column.
                                    getHeader(), field, "ms_run[" + run.
                                    getId() + "]-location"));
                } else {
                    String referenceString = ref.getReference();
                    Parameter idFormatParam = run.getIdFormat();
                    if (idFormatParam == null) {
                        //fall back to scan= if nothing else is given
                    } else {
                        if (idFormatParam.getCvAccession() == null || idFormatParam.getCvAccession().isEmpty()) {
                            //user param
                        } else {
                            //cv param
                            String validationPattern = "";
                            switch (idFormatParam.getCvAccession()) {
                                case "MS:1000768": //Thermo nativeId
                                    // "controllerType=0 controllerNumber=1 scan=1"
                                    validationPattern = MZTabConstants.REGEX_SPECTRA_REF_THERMO_NATIVE;
                                    break;
                                case "MS:1000769": //Waters nativeId
                                    // "function=0 process=0 scan=0"
                                    validationPattern = MZTabConstants.REGEX_SPECTRA_REF_WATERS_NATIVE;
                                    break;
                                case "MS:1000770": //WIFF nativeId
                                    // "sample=0 period=0 cycle=0 experiment=0"
                                    validationPattern = MZTabConstants.REGEX_SPECTRA_REF_WIFF_NATIVE;
                                    break;
                                case "MS:1000774": //multiple peak list nativeID, MGF, PKL, merged DTA
                                    // index=0
                                    validationPattern = MZTabConstants.REGEX_SPECTRA_REF_INDEX;
                                    break;
                                case "MS:1000773": // Bruker FID nativeId
                                case "MS:1000775": //file single peak list nativeId
                                    //file=xsd:IDREF
                                    validationPattern = MZTabConstants.REGEX_SPECTRA_REF_FILE;
                                    break;
                                case "MS:1000777": // spectrum identifier nativeId
                                    //spectrum=0
                                    validationPattern = MZTabConstants.REGEX_SPECTRA_REF_SPECTRUM;
                                    break;
                                case "MS:1001530": // mzML unique identifier
                                    //xsd:string
                                    validationPattern = MZTabConstants.REGEX_SPECTRA_REF_MZML_UNIQUE;
                                    break;
                                //scan instances below
                                case "MS:1000771": //Bruker/Agilent YEP nativeId
                                case "MS:1000772": //Bruker BAF nativeId    
                                case "MS:1000776": //mzML scan ref
                                    // scan=0
                                    validationPattern = MZTabConstants.REGEX_SPECTRA_REF_SCAN;
                                    break;
                                default:
                                    //As the given idFormat may be unsupported by this validator, we issue a warning!
                                    errorList.add(new MZTabError(
                                            LogicalErrorType.SpectraIdFormatNotSupported, lineNumber, referenceString, column.
                                                    getHeader()));
                            }
                            if(!validationPattern.isEmpty()) {
                                Optional<MZTabError> error = validatePattern(Pattern.compile(validationPattern), referenceString, lineNumber, column, field, run);
                                if (error.isPresent()) {
                                    errorList.add(error.get());
                                }
                            }
                        }
                    }
                }
            }
        }
        return errorList;
    }

    protected Optional<MZTabError> validatePattern(Pattern pattern, String reference, int lineNumber, IMZTabColumn column, String field, MsRun run) {
        Matcher matcher = pattern.matcher(reference);

        if (matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(new MZTabError(
                LogicalErrorType.SpectraIdFormatNotValid,
                lineNumber,
                reference,
                column.getHeader(),
                pattern.toString()
            )
        );
    }

}
