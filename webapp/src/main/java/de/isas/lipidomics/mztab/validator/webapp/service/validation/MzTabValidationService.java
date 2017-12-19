/*
 * Copyright 2017 Leibniz Institut für Analytische Wissenschaften - ISAS e.V..
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
package de.isas.lipidomics.mztab.validator.webapp.service.validation;

import de.isas.lipidomics.mztab.validator.webapp.service.StorageService;
import de.isas.lipidomics.mztab.validator.webapp.service.ValidationService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorType.Level;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@Service
public class MzTabValidationService implements ValidationService {

    private final StorageService storageService;

    @Autowired
    public MzTabValidationService(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public List<ValidationResult> validate(MzTabVersion mzTabVersion,
            String filename) {
        Path filepath = storageService.load(filename);
        try {
            List<ValidationResult> validationResults = new ArrayList<>();
            switch (mzTabVersion) {
                case MZTAB_1_0:
                    //            Level validationLevel = Level.Error;
                    validate(filepath, Level.Error,
                            validationResults);
                    validate(filepath, Level.Warn,
                            validationResults);
                    validate(filepath, Level.Info,
                            validationResults);
                    return validationResults;
                case MZTAB_1_1:
//            Level validationLevel = Level.Error;
                    validate(mzTabVersion, filepath, Level.Error,
                            validationResults);
                    validate(mzTabVersion, filepath, Level.Warn,
                            validationResults);
                    validate(mzTabVersion, filepath, Level.Info,
                            validationResults);
                    return validationResults;
                default:
                    throw new IllegalArgumentException(
                            "MzTabVersion number: " + mzTabVersion + " is not supported!");
            }
        } catch (IOException ex) {
            Logger.getLogger(MzTabValidationService.class.getName()).
                    log(java.util.logging.Level.SEVERE, null, ex);
        }
        return Collections.emptyList();
    }

    private void validate(MzTabVersion mzTabVersion, Path filepath,
            Level validationLevel, List<ValidationResult> validationResults) throws IllegalStateException, IOException {
        
    }
    
    private void validate(Path filepath,
            Level validationLevel, List<ValidationResult> validationResults) throws IllegalStateException, IOException {
        MZTabFileParser parser = new MZTabFileParser(filepath.toFile(),
                System.out, validationLevel, 1000);
        MZTabErrorList errorList = parser.getErrorList();
        for (MZTabError error : errorList.getErrorList()) {
            de.isas.lipidomics.mztab.validator.webapp.service.validation.Level level = de.isas.lipidomics.mztab.validator.webapp.service.validation.Level.INFO;
            switch (error.getType().
                    getLevel()) {
                case Error:
                    level = de.isas.lipidomics.mztab.validator.webapp.service.validation.Level.ERROR;
                    break;
                case Info:
                    level = de.isas.lipidomics.mztab.validator.webapp.service.validation.Level.INFO;
                    break;
                case Warn:
                    level = de.isas.lipidomics.mztab.validator.webapp.service.validation.Level.WARN;
                    break;
                default:
                    throw new IllegalStateException("State " + error.getType().
                            getLevel() + " is not handled in switch/case statement!");
            }
            ValidationResult vr = new ValidationResult(error.getLineNumber(),
                    level, error.getMessage(), error.toString());
            Logger.getLogger(MzTabValidationService.class.getName()).
                    info(vr.toString());
            validationResults.add(vr);
        }
    }

}
