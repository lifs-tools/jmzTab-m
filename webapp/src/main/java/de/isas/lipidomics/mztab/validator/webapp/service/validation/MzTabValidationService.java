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
            validate(mzTabVersion, filepath, Level.Error,
                validationResults);
            validate(mzTabVersion, filepath, Level.Warn,
                validationResults);
            validate(mzTabVersion, filepath, Level.Info,
                validationResults);
            return validationResults;
        } catch (IOException ex) {
            Logger.getLogger(MzTabValidationService.class.getName()).
                    log(java.util.logging.Level.SEVERE, null, ex);
        }
        return Collections.emptyList();
    }

    private void validate(MzTabVersion mzTabVersion, Path filepath,
            Level validationLevel, List<ValidationResult> validationResults) throws IllegalStateException, IOException {
        switch(mzTabVersion) {
            case MZTAB_1_0:
                new EbiValidator().validate(filepath, validationLevel.name(),
                    validationResults);
                break;
            case MZTAB_1_1:
                new IsasValidator().validate(filepath, validationLevel.name(),
                    validationResults);
                break;
            default:
                throw new IllegalStateException("Unsupported mzTab version: "+mzTabVersion.toString());
        }
        
    }
}
