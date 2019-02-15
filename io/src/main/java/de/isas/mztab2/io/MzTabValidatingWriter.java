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

import de.isas.lipidomics.mztab2.validation.MzTabValidator;
import de.isas.lipidomics.mztab2.validation.Validator;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.ValidationMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType.Level;

/**
 * <p>
 * MzTabValidatingWriter allows to write MzTab objects after validation with a
 * custom or default validator. Use this if you want to make sure that your fail
 * satisfies the structural and minimal reporting constraints of mzTab.
 * Otherwise, use the MzTabNonValidatingWriter.</p>
 *
 * <p>
 * To create a <b>validating</b> writer using the default checks also applied by
 * the parser, call:</p>
 * {@code MzTabWriter validatingWriter = new MzTabValidatingWriter.Default();}
 * <p>
 * Otherwise, to create a non-validating instance, call:</p>
 * {@code MzTabWriter plainWriter = new MzTabNonValidatingWriter();}
 *
 * @author nilshoffmann
 * @see MzTabValidatingWriter
 * @see MzTabValidator
 */
public class MzTabValidatingWriter implements MzTabWriter<List<ValidationMessage>> {

    private static final Logger logger = LoggerFactory.getLogger(
        MzTabValidatingWriter.class);

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
        this(new WriteAndParseValidator(System.out, Level.Info, 100),
            new MzTabWriterDefaults(), true);
    }

    /**
     * Uses the provided validator and default writer configuration. The output
     * file will not be written, if any validation failures occur.
     *
     * @param validator the validator instance.
     * @param skipWriteOnValidationFailure if true, skips writing of the file if
     * validation fails.
     */
    public MzTabValidatingWriter(Validator<MzTab> validator,
        boolean skipWriteOnValidationFailure) {
        this(validator, new MzTabWriterDefaults(), skipWriteOnValidationFailure);
    }

    /**
     * Uses the provided validator and writerDefaults.
     *
     * @param validator the validator instance.
     * @param writerDefaults the default writer settings.
     * @param skipWriteOnValidationFailure if true, skips writing of the file if
     * validation fails.
     */
    public MzTabValidatingWriter(Validator<MzTab> validator,
        MzTabWriterDefaults writerDefaults, boolean skipWriteOnValidationFailure) {
        this.validator = validator;
        this.writerDefaults = writerDefaults;
        this.skipWriteOnValidationFailure = skipWriteOnValidationFailure;
    }

    /**
     * A default validator implemenation that first writes and then parses the
     * created temporary file, performing the parser checks.
     */
    public static class WriteAndParseValidator implements Validator<MzTab> {

        private static final Logger logger = LoggerFactory.getLogger(
            WriteAndParseValidator.class);

        private final OutputStream outputStream;
        private final Level level;
        private final int maxErrorCount;

        /**
         * Create a new instance of this validator.
         *
         * @param outputStream the output stream to write to.
         * @param level the error level for validation.
         * @param maxErrorCount the maximum number of errors before an overflow
         * exception while stop further processing.
         */
        public WriteAndParseValidator(OutputStream outputStream, Level level,
            int maxErrorCount) {
            this.outputStream = outputStream;
            this.level = level;
            this.maxErrorCount = maxErrorCount;
        }

        @Override
        public List<ValidationMessage> validate(MzTab mzTab) {
            MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
            File mzTabFile = null;
            try {
                mzTabFile = File.createTempFile(UUID.randomUUID().
                    toString(), ".mztab");
                try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(mzTabFile),
                    "UTF-8")) {
                    writer.
                        write(
                            osw, mzTab);

                    MzTabFileParser parser = new MzTabFileParser(mzTabFile);
                    parser.parse(outputStream, level, maxErrorCount);
                    return parser.getErrorList().
                        convertToValidationMessages();
                }
            } catch (IOException ex) {
                logger.error(
                    "Caught exception while trying to parse " + mzTabFile, ex);
            } finally {
                if (mzTabFile != null && mzTabFile.exists()) {
                    if(!mzTabFile.delete()) {
                        logger.warn("Deletion of "+mzTabFile+" failed!");
                    }
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
        new MzTabNonValidatingWriter(writerDefaults).write(writer, mzTab);
        return Optional.of(this.validationMessages);
    }

    @Override
    public Optional<List<ValidationMessage>> write(Path path, MzTab mzTab) throws IOException {
        this.validationMessages = Optional.ofNullable(validator.validate(mzTab)).
            orElse(Collections.emptyList());
        if (skipWriteOnValidationFailure && !this.validationMessages.isEmpty()) {
            return Optional.of(this.validationMessages);
        }
        new MzTabNonValidatingWriter(writerDefaults).write(path, mzTab);
        return Optional.of(this.validationMessages);
    }

    /**
     * Returns all validation messages ONLY at the given level. E.g. if you
     * provide Info, you will ONLY receive Info messages, even if Warn or Error
     * messages have been produced!
     *
     * @param validationMessages the messages to apply the filter on.
     * @param level the message level.
     * @return the list of validation messages matching the provided level.
     */
    public static List<ValidationMessage> getValidationMessagesForLevel(
        Optional<List<ValidationMessage>> validationMessages,
        ValidationMessage.MessageTypeEnum level) {
        return validationMessages.orElse(Collections.emptyList()).
            stream().
            filter((message) ->
            {
                return message.getMessageType() == level;
            }).
            collect(Collectors.toList());
    }

}
