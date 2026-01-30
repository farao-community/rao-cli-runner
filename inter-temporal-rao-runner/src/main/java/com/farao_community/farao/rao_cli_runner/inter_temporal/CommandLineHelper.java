package com.farao_community.farao.rao_cli_runner.inter_temporal;

import org.apache.commons.cli.*;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Optional;

public class CommandLineHelper {

    public static final String CMD_LINE_SYNTAX = "java -jar " + getJarName();
    public static final String ALGORITHM_OPT = "algorithm";
    public static final String HELP_OPT = "help";
    public static final String INPUTS_OPT = "inputs";
    public static final String OUTPUT_OPT = "output";

    private static String getJarName() {
        ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
        if (protectionDomain != null) {
            java.security.CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource != null) {
                URL location = codeSource.getLocation();
                if (location != null) {
                    Path path = Paths.get(location.getPath());
                    return path.getFileName().toString();
                }
            }
        }
        return "inter-temporal.jar"; // Default name if not running from a JAR
    }


    public static CommandLineOptions parseCommandLineOptions(String[] args) {
        Options options = buildCommandLineOptions();

        CommandLineParser parser = new MyCommandLineParser(HELP_OPT);
        // TODO replace with org.apache.commons.cli.help.HelpFormatter
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp(CMD_LINE_SYNTAX, options);
            System.out.println(e.getMessage());
            System.exit(1);
        }

        if (cmd.hasOption(HELP_OPT)) {
            formatter.printHelp(CMD_LINE_SYNTAX, options);
            System.exit(0);
        }

        String inputsFilePath = cmd.getOptionValue(INPUTS_OPT);
        String outputPath = cmd.getOptionValue(OUTPUT_OPT);

        Optional<String> algorithm = Optional.empty();
        if (cmd.hasOption(ALGORITHM_OPT)) {
            algorithm = Optional.of(cmd.getOptionValue(ALGORITHM_OPT));
        }

        if (inputsFilePath == null) {
            System.err.println("You must define --inputs. Use --help for extra information.");
            System.exit(1);
        }

        return new CommandLineOptions(inputsFilePath, outputPath, algorithm);
    }

    private static Options buildCommandLineOptions() {
        Options options = new Options();
        Option helpOption = new Option(HELP_OPT.substring(0, 1), HELP_OPT, false, "Print this help message");
        options.addOption(helpOption);

        Option inputsOption = new Option(INPUTS_OPT.substring(0, 1), INPUTS_OPT, true, "Path to the inputs JSON file");
        inputsOption.setRequired(true);
        options.addOption(inputsOption);

        Option outputOption = new Option(OUTPUT_OPT.substring(0, 1), OUTPUT_OPT, true, "Path to the desired output directory");
        outputOption.setRequired(true);
        options.addOption(outputOption);

        Option algorithmOption = new Option(ALGORITHM_OPT.substring(0, 1), ALGORITHM_OPT, true, "InterTemporalRao implementation name (optional)");
        algorithmOption.setRequired(false);
        options.addOption(algorithmOption);
        return options;
    }
}
