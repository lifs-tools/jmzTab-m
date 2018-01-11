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
package de.isas.lipidomics.mztab.validator.webapp.service;

import de.isas.lipidomics.mztab.validator.webapp.domain.UserSessionFile;
import de.isas.lipidomics.mztab.validator.webapp.domain.ValidationResult;
import de.isas.mztab1_1.model.ValidationMessage;
import java.util.List;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public interface ValidationService {
    static enum MzTabVersion{MZTAB_1_0, MZTAB_1_1};
    List<ValidationMessage> validate(MzTabVersion version, UserSessionFile userSessionFile, int maxErrors);
    
    List<ValidationResult> asValidationResults(List<ValidationMessage> validationMessage);
}
