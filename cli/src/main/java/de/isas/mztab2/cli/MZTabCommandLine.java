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

import de.isas.mztab2.validation.CvMappingValidator;
import de.isas.mztab2.model.ValidationMessage;
import org.apache.commons.cli.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import uk.ac.ebi.pride.jmztab2.utils.MZTabFileParser;
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

        // Definite command line
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        String helpOpt = "help";
        options.addOption("h", helpOpt, false, "Print help message.");

        String versionOpt = "version";
        options.addOption("v", versionOpt, false, "Print version information.");

        String msgOpt = "message";
        String codeOpt = "code";
        Option msgOption = OptionBuilder.withArgName(codeOpt).
            hasArgs(2).
            withValueSeparator().
            withDescription(
                "Example: -message code=1002. Print Error/Warn detail message based on code number.").
            create(msgOpt);
        options.addOption(msgOption);

        String outOpt = "outFile";
        options.addOption(outOpt, true,
            "Record error/warn messages into outfile. If not set, print message on the screen.");

        String checkOpt = "check";
        String inFileOpt = "inFile";
        Option checkOption = OptionBuilder.withArgName(inFileOpt).
            hasArgs(2).
            withValueSeparator('=').
            withDescription(
                "Example: -check inFile=/path/to/file.mztab. Choose a file from input directory. This parameter should not be null!").
            create(checkOpt);
        options.addOption(checkOption);

        String levelOpt = "level";
        options.addOption(levelOpt, true,
            "Choose validate level(Info, Warn, Error), default level is Error!");

        String checkSemanticOpt = "checkSemantic";
        String mappingFileOpt = "mappingFile";
        Option mappingFileOption = OptionBuilder.withArgName(mappingFileOpt).
            hasArgs(2).
            withValueSeparator('=').
            withDescription(
                "Example: -checkSemantic mappingFile=/path/to/mappingFile.xml. Use the provided mapping file for semantic validation. This parameter may be null.").
            create(checkSemanticOpt);
        options.addOption(mappingFileOption);
        
        //TODO add option to set whether extra terms not defined in mapping file create a warning or error
//        options.addOption()

        // Parse command line
        CommandLine line = parser.parse(options, args);
        if (line.getOptions().length == 0 || line.hasOption(helpOpt)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("jmztab-cli", options);
        } else if (line.hasOption(msgOpt)) {
            String[] values = line.getOptionValues(msgOpt);
            Integer code = new Integer(values[1]);
            MZTabErrorType type = typeMap.getType(code);

            if (type == null) {
                System.out.println(
                    "Not found MZTabErrorType, the code is :" + code);
            } else {
                System.out.println(type);
            }
        } else if (line.hasOption(versionOpt)) {
            System.out.println(getAppInfo());
        } else {
            File outFile = null;
            if (line.hasOption(outOpt)) {
                outFile = new File(line.getOptionValue(outOpt));
            }

            MZTabErrorType.Level level = MZTabErrorType.Level.Error;
            if (line.hasOption(levelOpt)) {
                level = MZTabErrorType.findLevel(line.getOptionValue(levelOpt));
                System.out.println("Valdiator set to level '" + level + "'");
            }

            if (line.hasOption(checkOpt)) {
                String[] values = line.getOptionValues(checkOpt);
                if (values.length != 2) {
                    throw new IllegalArgumentException("Not setting input file!");
                }
                File inFile = new File(values[1].trim());
                System.out.println(
                    "Beginning validation of mztab file: " + inFile.
                        getAbsolutePath());
                try (OutputStream out = outFile == null ? System.out : new BufferedOutputStream(
                    new FileOutputStream(outFile))) {
                    MZTabFileParser mzTabParser = new MZTabFileParser(inFile);
                    MZTabErrorList errorList = mzTabParser.parse(out, level);
                    if (!errorList.isEmpty()) {
                        //these are reported to std.err already.
                        System.out.println(
                            "There were errors while processing your file, please check the output for details!");
                    }
                    if (line.hasOption(checkSemanticOpt)) {
                        String[] semValues = line.getOptionValues(
                            checkSemanticOpt);
                        URI mappingFile;
                        if (values.length == 2) {
                            // read file from path
                            mappingFile = new File(values[1].trim()).getAbsoluteFile().toURI();
                        } else {
                            // read default file
                            mappingFile = CvMappingValidator.class.getResource("/mappings/mzTab-M-mapping.xml").toURI();
                        }
                        System.out.println(
                            "Beginning semantic validation of mztab file: " + inFile.
                                getAbsolutePath() + " with mapping file: " +mappingFile.toASCIIString());
                        CvMappingValidator cvMappingValidator = CvMappingValidator.of(mappingFile.toURL(), true);
                        List<ValidationMessage> validationMessages = cvMappingValidator.
                            validate(mzTabParser.getMZTabFile());
                        for(ValidationMessage message:validationMessages) {
                            System.err.println(message);
                        }
                        if (!validationMessages.isEmpty()) {
                            System.out.println(
                                "There were errors during semantic validation of your file, please check the output for details!");
                        }
                    }
                } catch (IOException e) {
                    System.out.println(
                        "Caught an IO Exception: " + e.getMessage());
                }
            }

            System.out.println("Finished validation!");
            System.out.println();
        }
    }

}
