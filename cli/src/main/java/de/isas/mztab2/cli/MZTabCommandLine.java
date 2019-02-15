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
package de.isas.mztab2.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.isas.mztab2.io.MzTabNonValidatingWriter;
import de.isas.mztab2.validation.CvMappingValidator;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.ValidationMessage;
import static de.isas.mztab2.model.ValidationMessage.MessageTypeEnum.ERROR;
import static de.isas.mztab2.model.ValidationMessage.MessageTypeEnum.WARN;
import org.apache.commons.cli.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.isas.mztab2.io.MzTabFileParser;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorTypeMap;

/**
 * <p>
 * MZTabCommandLine class.</p>
 *
 * @author qingwei
 * @author nilshoffmann
 * @since 17/09/13
 *
 */
public class MZTabCommandLine {

    private static final Logger logger = LoggerFactory.getLogger(
        MZTabCommandLine.class);

    private static String getAppInfo() throws IOException {
        Properties p = new Properties();
        p.load(MZTabCommandLine.class.getResourceAsStream(
            "/application.properties"));
        StringBuilder sb = new StringBuilder();
        String buildDate = p.getProperty("app.build.date", "no build date");
        if (!"no build date".equals(buildDate)) {
            Instant instant = Instant.ofEpochMilli(Long.parseLong(buildDate));
            buildDate = instant.toString();
        }
        /*
         *Property keys are in src/main/resources/application.properties
         */
        sb.append("Running ").
            append(p.getProperty("app.name", "undefined app")).
            append("\n\r").
            append(" version: '").
            append(p.getProperty("app.version", "unknown version")).
            append("'").
            append("\n\r").
            append(" build-date: '").
            append(buildDate).
            append("'").
            append("\n\r").
            append(" scm-location: '").
            append(p.getProperty("scm.location", "no scm location")).
            append("'").
            append("\n\r").
            append(" commit: '").
            append(p.getProperty("scm.commit.id", "no commit id")).
            append("'").
            append("\n\r").
            append(" branch: '").
            append(p.getProperty("scm.branch", "no branch")).
            append("'").
            append("\n\r");
        return sb.toString();
    }

    /**
     * <p>
     * Runs the command line parser for mzTab, including validation.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        MZTabErrorTypeMap typeMap = new MZTabErrorTypeMap();
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        String helpOpt = addHelpOption(options);
        String versionOpt = addVersionOption(options);
        String msgOpt = addMessageOption(options);
        String outOpt = addOutFileOption(options);
        String checkOpt = addCheckOption(options);
        String levelOpt = addLevelOption(options);
        String serializeOpt = addSerializeOption(options);
        String deserializeOpt = addDeserializeOption(options);
        String checkSemanticOpt = addCheckSemanticOption(options);

        //TODO add option to set whether extra terms not defined in mapping file create a warning or error
//        options.addOption()
        // Parse command line
        CommandLine line = parser.parse(options, args);
        if (line.getOptions().length == 0 || line.hasOption(helpOpt)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("jmztab-cli", options);
        } else if (line.hasOption(msgOpt)) {
            handleMsgOption(line, msgOpt, typeMap);
        } else if (line.hasOption(versionOpt)) {
            logger.info(getAppInfo());
        } else {
            boolean hadErrorsOrWarnings = handleValidationOptions(line, outOpt,
                levelOpt, serializeOpt,
                deserializeOpt, checkOpt, checkSemanticOpt);
            if (hadErrorsOrWarnings) {
                System.exit(1);
            }
        }
    }

    protected static String addVersionOption(Options options) {
        String versionOpt = "version";
        options.addOption("v", versionOpt, false, "Print version information.");
        return versionOpt;
    }

    protected static String addHelpOption(Options options) {
        String helpOpt = "help";
        options.addOption("h", helpOpt, false, "Print help message.");
        return helpOpt;
    }

    protected static String addCheckSemanticOption(Options options) throws IllegalArgumentException {
        String checkSemanticOpt = "checkSemantic";
        String mappingFileOpt = "mappingFile";
        Option mappingFileOption = OptionBuilder.withArgName(mappingFileOpt).
            hasOptionalArgs(2).
            withValueSeparator('=').
            withDescription(
                "Example: -checkSemantic mappingFile=/path/to/mappingFile.xml. Use the provided mapping file for semantic validation. This parameter may be null. Requires an active internet connection!").
            create(checkSemanticOpt);
        options.addOption(mappingFileOption);
        return checkSemanticOpt;
    }

    protected static String addDeserializeOption(Options options) {
        String deserializeOpt = "fromJson";
        options.addOption(deserializeOpt, false,
            "Example: -fromJson. Will parse inFile as JSON and write mzTab representation to disk. Requires validation to be successful!");
        return deserializeOpt;
    }

    protected static String addSerializeOption(Options options) {
        String serializeOpt = "toJson";
        options.addOption(serializeOpt, false,
            "Example: -toJson. Will write a json representation of inFile to disk. Requires validation to be successful!");
        return serializeOpt;
    }

    protected static String addLevelOption(Options options) {
        String levelOpt = "level";
        options.addOption(levelOpt, true,
            "Choose validation level (Info, Warn, Error), default level is Info!");
        return levelOpt;
    }

    protected static String addCheckOption(Options options) throws IllegalArgumentException {
        String checkOpt = "check";
        String inFileOpt = "inFile";
        Option checkOption = OptionBuilder.withArgName(inFileOpt).
            hasArgs(2).
            withValueSeparator('=').
            withDescription(
                "Example: -check inFile=/path/to/file.mztab. Choose a file from input directory. This parameter should not be null!").
            create(checkOpt);
        options.addOption(checkOption);
        return checkOpt;
    }

    protected static String addOutFileOption(Options options) {
        String outOpt = "outFile";
        options.addOption(outOpt, true,
            "Record validation messages into outfile. If not set, print validation messages to stdout/stderr.");
        return outOpt;
    }

    protected static String addMessageOption(Options options) throws IllegalArgumentException {
        String msgOpt = "message";
        String codeOpt = "code";
        Option msgOption = OptionBuilder.withArgName(codeOpt).
            hasArgs(2).
            withValueSeparator().
            withDescription(
                "Example: -message code=1002. Print validation message detail information based on error code.").
            create(msgOpt);
        options.addOption(msgOption);
        return msgOpt;
    }

    protected static boolean handleValidationOptions(CommandLine line,
        String outOpt, String levelOpt, String serializeOpt,
        String deserializeOpt, String checkOpt, String checkSemanticOpt) throws JAXBException, IllegalArgumentException, URISyntaxException {
        File outFile = null;
        if (line.hasOption(outOpt)) {
            outFile = new File(line.getOptionValue(outOpt));
            logger.info("Redirecting validator output to file {}", outFile);
        }

        try (PrintStream out = outFile == null ? System.out : new PrintStream(
            new BufferedOutputStream(
                new FileOutputStream(outFile, false)), true, "UTF8")) {
            System.setOut(out);
            System.setErr(out);
            logger.info(getAppInfo());
            MZTabErrorType.Level level = MZTabErrorType.Level.Info;
            if (line.hasOption(levelOpt)) {
                level = MZTabErrorType.findLevel(line.getOptionValue(
                    levelOpt));
                logger.info("Validator set to level '{}'", level);
            } else {
                logger.info(
                    "Validator set to default level '{}'", level);
            }
            boolean serializeToJson = false;
            if (line.hasOption(serializeOpt)) {
                serializeToJson = true;
            }

            boolean deserializeFromJson = false;
            if (line.hasOption(deserializeOpt)) {
                deserializeFromJson = true;
            }
            return handleValidation(line, checkOpt, out, level,
                checkSemanticOpt,
                serializeToJson, deserializeFromJson);
        } catch (IOException ex) {
            logger.error(
                "Caught an IO Exception: ", ex);
            return false;
        }
    }

    protected static void handleMsgOption(CommandLine line, String msgOpt,
        MZTabErrorTypeMap typeMap) throws NumberFormatException {
        String[] values = line.getOptionValues(msgOpt);
        Integer code = new Integer(values[1]);
        MZTabErrorType type = typeMap.getType(code);

        if (type == null) {
            logger.warn(
                "Could not find MZTabErrorType for code:" + code);
        } else {
            logger.info("MZTabErrorType for code {}: {}", code, type);
        }
    }

    protected static boolean handleValidation(CommandLine line, String checkOpt,
        PrintStream outFile, MZTabErrorType.Level level, String checkSemanticOpt,
        boolean toJson, boolean fromJson) throws URISyntaxException, JAXBException, IllegalArgumentException, IOException {
        boolean errorsOrWarnings = false;
        if (line.hasOption(checkOpt)) {
            String[] values = line.getOptionValues(checkOpt);
            if (values.length != 2) {
                throw new IllegalArgumentException("Not setting input file!");
            } else {
                if (!"inFile".equals(values[0])) {
                    logger.error("Please use the check option as follows, if you want to supply an mzTab file for basic validation: '-check inFile=<path/to/mzTabMfile.mzTab>'");
                    return true;
                }
            }
            File inFile = new File(values[1].trim());
            if (fromJson) {
                File tmpFile = new File(inFile.getParentFile(),
                    inFile.getName() + ".mztab");
                MzTabNonValidatingWriter w = new MzTabNonValidatingWriter();
                ObjectMapper mapper = new ObjectMapper();
                MzTab mzTab = mapper.readValue(inFile, MzTab.class);
                logger.info("Writing JSON as mzTab to file: {}", tmpFile.
                    getAbsolutePath());
                w.write(tmpFile.toPath(), mzTab);
                inFile = tmpFile;
            }
            logger.info("Beginning validation of mztab file: {}", inFile.
                getAbsolutePath());
            MzTabFileParser mzTabParser = new MzTabFileParser(inFile);
            MZTabErrorList errorList = mzTabParser.parse(outFile, level);
            if (!errorList.isEmpty()) {
                long nErrorsOrWarnings = errorList.getErrorList().
                    stream().
                    filter((error) ->
                    {
                        MZTabError e = error;
                        return e.getType().
                                getLevel() == MZTabErrorType.Level.Error || e.
                                        getType().
                                        getLevel() == MZTabErrorType.Level.Warn;
                    }).
                    count();
                errorsOrWarnings = nErrorsOrWarnings > 0;
                //these are reported to std.err already.
                logger.error(
                    "There were " + errorList.size() + " validation messages including " + nErrorsOrWarnings + " warnings or errors during validation your file, please check the output for details!");
            }
            if (toJson) {
                File jsonFile = new File(inFile.getName() + ".json");
                logger.error(
                    "Writing mzTab object as json to " + jsonFile.
                        getAbsolutePath());
                ObjectMapper objectMapper = new ObjectMapper().enable(
                    SerializationFeature.INDENT_OUTPUT);
                objectMapper.
                    writeValue(jsonFile, mzTabParser.getMZTabFile());
            }
            errorsOrWarnings = errorsOrWarnings || handleSemanticValidation(line,
                checkSemanticOpt, inFile, outFile,
                mzTabParser, level);
            logger.info("Finished validation!");
        }
        return errorsOrWarnings;
    }

    protected static boolean handleSemanticValidation(CommandLine line,
        String checkSemanticOpt, File inFile, PrintStream outFile,
        MzTabFileParser mzTabParser,
        MZTabErrorType.Level level) throws JAXBException, MalformedURLException, URISyntaxException {
        boolean errorsOrWarnings = false;
        if (line.hasOption(checkSemanticOpt)) {
            String[] semValues = line.getOptionValues(
                checkSemanticOpt);
            URI mappingFile;
            if (semValues != null && semValues.length == 2) {
                if (!"mappingFile".equals(semValues[0])) {
                    logger.error("Please use the checkSemantic option as follows, if you want to supply a custom mapping file: '-checkSemantic mappingFile=<path/to/mappingfile.xml>'");
                    return true;
                }
                // read file from path
                mappingFile = new File(semValues[1].trim()).
                    getAbsoluteFile().
                    toURI();
            } else {
                logger.info(
                    "Using default mapping file from classpath: /mappings/mzTab-M-mapping.xml");
                // read default file
                mappingFile = CvMappingValidator.class.getResource(
                    "/mappings/mzTab-M-mapping.xml").
                    toURI();
            }
            logger.info(
                "Beginning semantic validation of mztab file: " + inFile.
                    getAbsolutePath() + " with mapping file: " + mappingFile.
                    toASCIIString());
            CvMappingValidator cvMappingValidator = CvMappingValidator.of(
                mappingFile.toURL(), true);
            List<ValidationMessage> validationMessages = cvMappingValidator.
                validate(mzTabParser.getMZTabFile()).
                stream().
                filter((message) ->
                {
                    switch (level) {
                        case Error:
                            return message.getMessageType() == ERROR;
                        case Warn:
                            return message.getMessageType() == ERROR || message.
                                getMessageType() == WARN;
                        case Info:
                            return true;
                    }
                    return false;
                }).
                collect(Collectors.toList());
            long nErrorsOrWarnings = validationMessages.stream().
                filter((validationMessage) ->
                {
                    return validationMessage.getMessageType() == ValidationMessage.MessageTypeEnum.ERROR || validationMessage.
                        getMessageType() == ValidationMessage.MessageTypeEnum.WARN;
                }).
                count();
            errorsOrWarnings = nErrorsOrWarnings > 0;
            if (outFile != null) {
                for (ValidationMessage message : validationMessages) {
                    outFile.print(message);
                    outFile.println();
                }
            } else {
                for (ValidationMessage message : validationMessages) {
                    logger.error("{}", message);
                }
            }
            if (!validationMessages.isEmpty()) {
                logger.error(
                    "There were " + validationMessages.size() + " validation messages including " + nErrorsOrWarnings + " warnings or errors during semantic validation of your file, please check the output for details!");
            } else {
                logger.info(
                    "No errors found for semantic validation on level " + level);
            }
        }
        return errorsOrWarnings;
    }

}
