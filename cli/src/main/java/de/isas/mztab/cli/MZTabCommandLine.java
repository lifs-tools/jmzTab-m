package de.isas.mztab.cli;

import org.apache.commons.cli.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import uk.ac.ebi.pride.jmztab1_1.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorTypeMap;

/**
 * @author qingwei
 * @author Nils Hoffmann
 * @since 17/09/13
 */
public class MZTabCommandLine {

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        MZTabErrorTypeMap typeMap = new MZTabErrorTypeMap();

        // Definite command line
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        String helpOpt = "help";
        options.addOption("h", helpOpt, false, "Print help message");

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

        // Parse command line
        CommandLine line = parser.parse(options, args);
        if (line.getOptions().length == 0 || line.hasOption(helpOpt)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("mzTabCLI", options);
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
        } else {

            File outFile = null;
            if (line.hasOption(outOpt)) {
                outFile = new File(line.getOptionValue(outOpt));
            }

            OutputStream out = outFile == null ? System.out : new BufferedOutputStream(
                new FileOutputStream(outFile));

            MZTabErrorType.Level level = MZTabErrorType.Level.Error;
            if (line.hasOption(levelOpt)) {
                level = MZTabErrorType.findLevel(line.getOptionValue(levelOpt));
            }

            if (line.hasOption(checkOpt)) {
                String[] values = line.getOptionValues(checkOpt);
                if (values.length != 2) {
                    throw new IllegalArgumentException("Not setting input file!");
                }
                File inFile = new File(values[1].trim());
                System.out.println("Begin check mztab file: " + inFile.
                    getAbsolutePath());
                new MZTabFileParser(inFile, out, level);
            }

            System.out.println("Finish!");
            System.out.println();
            out.close();
        }
    }

}
