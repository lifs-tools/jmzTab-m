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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorType;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class EbiValidator implements Validator {
    public List<ValidationResult> validate(Path filepath,
            String validationLevel, int maxErrors) throws IllegalStateException, IOException {
        MZTabFileParser parser = new MZTabFileParser(filepath.toFile(),
                System.out, MZTabErrorType.findLevel(validationLevel), maxErrors);
        MZTabErrorList errorList = parser.getErrorList();
        List<ValidationResult> validationResults = new ArrayList<>(errorList.size());
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
        return validationResults;
    }
}
