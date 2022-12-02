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
package org.lifstools.mztab2.validation;

import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.ValidationMessage;
import static org.lifstools.mztab2.model.ValidationMessage.MessageTypeEnum.ERROR;
import static org.lifstools.mztab2.model.ValidationMessage.MessageTypeEnum.INFO;
import static org.lifstools.mztab2.model.ValidationMessage.MessageTypeEnum.WARN;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Delegating validator implementation that forwards validation to the provided
 * validator implementations.
 *
 * @author nilshoffmann
 */
public class MzTabValidator implements Validator<MzTab> {

    private final List<Validator<MzTab>> validators;

    public MzTabValidator(Validator<MzTab>... validator) {
        this.validators = Arrays.asList(validator);
    }

    @Override
    public List<ValidationMessage> validate(MzTab mzTab) {
        return validators.stream().
            map(validator ->
            {
                return validator.validate(mzTab);
            }).
            flatMap(Collection::stream).
            collect(Collectors.toList());
    }

    /**
     * Validate the given mzTab object, filtering messages that are of a lower
     * level than the given one. Thus, for validationLevel=ERROR, only ERROR
     * messages will be returned, for WARN, ERROR and WARN messages will be
     * returned. For INFO, INFO, WARN and ERROR messages will be returned.
     *
     * @param mzTab the mzTab object to validate.
     * @param validationLevel the validation level, used as a message filter.
     * @param validators the validators to apply in sequence to the mzTab
     * object.
     * @return the filtered list of validation messages according to the giben
     * validationLevel.
     */
    public static List<ValidationMessage> validate(MzTab mzTab,
        ValidationMessage.MessageTypeEnum validationLevel,
        Validator<MzTab>... validators) {
        MzTabValidator validator = new MzTabValidator(validators);
        return validator.validate(mzTab).
            stream().
            filter(validationMessage ->
            {
                switch (validationLevel) {
                    case INFO:
                        return (validationMessage.getMessageType() == INFO || validationMessage.getMessageType() == WARN || validationMessage.getMessageType() == ERROR);
                    case WARN:
                        return (validationMessage.getMessageType() == WARN || validationMessage.getMessageType() == ERROR);
                    case ERROR:
                        return (validationMessage.getMessageType() == ERROR);
                    default:
                        throw new IllegalArgumentException(
                            "Unknown message type for validationLevel '"+validationLevel+"' and message: '" + validationMessage.
                                getMessage() + "'!");
                }
            }).
            collect(Collectors.toList());

    }

}
