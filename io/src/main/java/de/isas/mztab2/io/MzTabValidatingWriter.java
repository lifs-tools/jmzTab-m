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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import uk.ac.ebi.pride.jmztab2.utils.MZTabFileParser;

/**
 * <p>
 * MzTabValidatingWriter allows to write MzTab objects after validation with a custom or default validator.
 * Use this if you want to make sure that your fail satisfies the structural and minimal reporting constraints of mzTab.
 * Otherwise, use the MzTabNonValidatingWriter.</p>
 *
 * <p>To create a <b>validating</b> writer using the default checks also applied by the parser,
 * call:</p>
 * {@code MzTabWriter validatingWriter = new MzTabValidatingWriter.Default();}
 * <p>Otherwise, to create a non-validating instance, call:</p>
 * {@code MzTabWriter plainWriter = new MzTabNonValidatingWriter();}
 *
 * @author nilshoffmann
 * @see MzTabValidatingWriter
 */
public class MzTabValidatingWriter implements MzTabWriter<List<ValidationMessage>> {

    private final Validator<MzTab> validator;
    private final boolean skipWriteOnValidationFailure;
    private final MzTabWriterDefaults writerDefaults;
    private List<ValidationMessage> validationMessages = null;

    /**
     * Uses default structural validation based on writing and parsing the
     * written file with the default parsing checks. The output file will not be
     * written, if any validation failures occur.
     */
    public MzTabValidatingWriter() {
        this(new WriteAndParseValidator(), new MzTabWriterDefaults(), true);
    }

    /**
     * Uses the provided validator and writerDefaults.
     *
     * @param validator
     * @param writerDefaults
     * @param skipWriteOnValidationFailure if true, skips writing of the file if
     * validation fails.
     */
    public MzTabValidatingWriter(Validator<MzTab> validator,
        MzTabWriterDefaults writerDefaults, boolean skipWriteOnValidationFailure) {
        this.validator = validator;
        this.writerDefaults = writerDefaults;
        this.skipWriteOnValidationFailure = skipWriteOnValidationFailure;
    }

    public static class WriteAndParseValidator implements Validator<MzTab> {

        @Override
        public List<ValidationMessage> validate(MzTab mzTab) {
            MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
            File mzTabFile = null;
            try {
                mzTabFile = File.createTempFile(UUID.randomUUID().
                    toString(), ".mztab");
                mzTabFile.getAbsoluteFile();
                writer.
                    write(new FileWriter(mzTabFile), mzTab);

                MZTabFileParser parser = new MZTabFileParser(mzTabFile);
                parser.parse(System.out);
                return parser.convertToValidationMessages();
            } catch (IOException ex) {
                Logger.getLogger(MzTabValidatingWriter.class.getName()).
                    log(Level.SEVERE, null, ex);
            } finally {
                if (mzTabFile != null && mzTabFile.exists()) {
                    mzTabFile.delete();
                };
            }
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<List<ValidationMessage>> write(OutputStreamWriter writer,
        MzTab mzTab) throws IOException {
        this.validationMessages = Optional.ofNullable(validator.validate(mzTab)).
            orElse(Collections.emptyList());
        if (skipWriteOnValidationFailure && !this.validationMessages.isEmpty()) {
            return Optional.of(this.validationMessages);
        }
        new MzTabNonValidatingWriter().write(writer, mzTab);
        return Optional.of(this.validationMessages);
    }

    @Override
    public Optional<List<ValidationMessage>> write(Path path, MzTab mzTab) throws IOException {
        this.validationMessages = Optional.ofNullable(validator.validate(mzTab)).
            orElse(Collections.emptyList());
        if (skipWriteOnValidationFailure && !this.validationMessages.isEmpty()) {
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
