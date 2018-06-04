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
package de.isas.mztab2.io;

import de.isas.lipidomics.jmztabm.validation.Validator;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.ValidationMessage;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author nilshoffmann
 */
public class MzTabValidatingWriter implements MzTabWriter<List<ValidationMessage>> {

    private final Validator<MzTab> validator;
    private final boolean failFast;
    private List<ValidationMessage> validationMessages = null;

    public MzTabValidatingWriter(Validator<MzTab> validator, boolean failFast) {
        this.validator = validator;
        this.failFast = failFast;
    }

    @Override
    public Optional<List<ValidationMessage>> write(OutputStreamWriter writer,
        MzTab mzTab) throws IOException {
        this.validationMessages = Optional.ofNullable(validator.validate(mzTab)).
            orElse(Collections.emptyList());
        if (failFast && !this.validationMessages.isEmpty()) {
            return Optional.of(this.validationMessages);
        }
        new MzTabNonValidatingWriter().write(writer, mzTab);
        return Optional.of(this.validationMessages);
    }

    @Override
    public Optional<List<ValidationMessage>> write(Path path, MzTab mzTab) throws IOException {
        this.validationMessages = Optional.ofNullable(validator.validate(mzTab)).
            orElse(Collections.emptyList());
        if (failFast && !this.validationMessages.isEmpty()) {
            return Optional.of(this.validationMessages);
        }
        new MzTabNonValidatingWriter().write(path, mzTab);
        return Optional.of(this.validationMessages);
    }

    /**
     * Returns all validation messages ONLY at the given level. E.g. if you
     * provide Info, you will ONLY receive Info messages, even if Warn or Error
     * messages have been produced!
     *
     * @param level the message level.
     * @return the list of validation messages matching the provided level.
     */
    public static List<ValidationMessage> getValidationMessagesForLevel(
        Optional<List<ValidationMessage>> validationMessages,
        ValidationMessage.MessageTypeEnum level) {
        return validationMessages.get().
            stream().
            filter((message) ->
            {
                return message.getMessageType() == level;
            }).
            collect(Collectors.toList());
    }

}
