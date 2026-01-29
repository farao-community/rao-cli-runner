package com.farao_community.farao.rao_cli_runner.inter_temporal;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.commons.TemporalDataImpl;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.CracCreationContext;
import com.powsybl.openrao.data.crac.api.NetworkElement;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkAction;
import com.powsybl.openrao.data.crac.api.rangeaction.InjectionRangeAction;
import com.powsybl.openrao.data.crac.api.rangeaction.RangeAction;
import com.powsybl.openrao.data.intertemporalconstraints.IntertemporalConstraints;
import com.powsybl.openrao.data.intertemporalconstraints.io.JsonIntertemporalConstraints;
import com.powsybl.openrao.data.raoresult.api.InterTemporalRaoResult;
import com.powsybl.openrao.raoapi.InterTemporalRao;
import com.powsybl.openrao.raoapi.InterTemporalRaoInputWithNetworkPaths;
import com.powsybl.openrao.raoapi.RaoInputWithNetworkPaths;
import com.powsybl.openrao.raoapi.json.JsonRaoParameters;
import com.powsybl.openrao.raoapi.parameters.RaoParameters;

import java.io.*;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

public class Main {

    private record CommandLineOptions(String inputsJsonFilePath, String outputPath, Optional<String> algorithm) {

    }

    public static final String CMD_LINE_SYNTAX = "java -jar your.jar"; // TODO
    public static final String ALGORITHM_OPT = "algorithm";
    public static final String HELP_OPT = "help";
    public static final String INPUTS_OPT = "inputs";
    public static final String OUTPUT_OPT = "output";


    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        CommandLineOptions opts = parseCommandLineOptions(args);
        JsonInterTemporalRaoInputs inputs = JsonInterTemporalRaoInputs.read(opts.inputsJsonFilePath);

        if (inputs == null) {
            throw new RuntimeException("Input files could not be read.");
        }

        if (inputs.getTimedInputs().isEmpty()) {
            throw new RuntimeException("TimedInputs is empty.");
        }

        Map<OffsetDateTime, RaoInputWithNetworkPaths> timedInputMap = buildInputs(inputs);
        IntertemporalConstraints intertemporalConstraints = JsonIntertemporalConstraints.read(new FileInputStream(inputs.getIcsFile()));
        InterTemporalRaoInputWithNetworkPaths raoInput = new InterTemporalRaoInputWithNetworkPaths(new TemporalDataImpl<>(timedInputMap), intertemporalConstraints);
        RaoParameters parameters = JsonRaoParameters.read(new FileInputStream(inputs.getParametersFile()));
        InterTemporalRaoResult result = InterTemporalRao.find(opts.algorithm.orElse(null)).run(raoInput, parameters);
        writeRaoResultsZip(opts.outputPath, result, raoInput);
        exportNetworksWithPras(opts.outputPath, result, raoInput);
    }

    private static Map<OffsetDateTime, RaoInputWithNetworkPaths> buildInputs(JsonInterTemporalRaoInputs inputs) {
        Map<OffsetDateTime, RaoInputWithNetworkPaths> timedInputMap = new HashMap<>();
        inputs.getTimedInputs().stream().sorted(Comparator.comparing(JsonInterTemporalRaoInputs.TimedInput::getTimestamp))
            .forEach(timedInput -> {
                Network network = Network.read(timedInput.getNetworkFile());
                Crac crac = null;
                if (timedInput.getCracFile() == null) {
                    //crac = CracGenerator
                } else {
                    try {
                        CracCreationContext ccc = Crac.readWithContext(Path.of(timedInput.getCracFile()).getFileName().toString(),
                            new FileInputStream(timedInput.getCracFile()),
                            network
                        );
                        System.out.println(ccc.getCreationReport());
                        crac = ccc.getCrac();
                    } catch (IOException e) {
                        System.err.println("Could not read crac: " + e.getMessage());
                        System.exit(1);
                    }
                }
                // TODO fix this. should use timedInput.ts instead of crac.ts, but it is in UTC

                timedInputMap.put(crac.getTimestamp().orElseThrow(),
                    RaoInputWithNetworkPaths.build(timedInput.getNetworkFile(), timedInput.getNetworkFile(), crac).build());
            });
        return timedInputMap;
    }

    private static void writeRaoResultsZip(String outputPath, InterTemporalRaoResult result, InterTemporalRaoInputWithNetworkPaths raoInput) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(outputPath, "rao_results.zip"));
        Properties properties = new Properties();
        properties.put("rao-result.export.json.flows-in-megawatts", "true");
        properties.put("inter-temporal-rao-result.export.filename-template", "'RAO_RESULT_'yyyy-MM-dd'T'HH:mm:ss'.json'");
        properties.put("inter-temporal-rao-result.export.summary-filename", "summary.json");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            result.write(zipOutputStream, raoInput.getRaoInputs().map(RaoInputWithNetworkPaths::getCrac), properties);
        }
    }

    public static void exportNetworksWithPras(String outputPath, InterTemporalRaoResult result, InterTemporalRaoInputWithNetworkPaths raoInput) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(outputPath, "modified_networks.zip"));
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        for (OffsetDateTime offsetDateTime : result.getTimestamps()) {
            Set<NetworkAction> preventiveNetworkActions = result.getIndividualRaoResult(offsetDateTime).getActivatedNetworkActionsDuringState(raoInput.getRaoInputs().getData(offsetDateTime).get().getCrac().getPreventiveState());
            Set<RangeAction<?>> preventiveRangeActions = result.getIndividualRaoResult(offsetDateTime).getActivatedRangeActionsDuringState(raoInput.getRaoInputs().getData(offsetDateTime).get().getCrac().getPreventiveState());
            Network modifiedNetwork = Network.read(raoInput.getRaoInputs().getData(offsetDateTime).orElseThrow().getPostIcsImportNetworkPath());
            Network initialNetwork = Network.read(raoInput.getRaoInputs().getData(offsetDateTime).orElseThrow().getInitialNetworkPath());

            // Apply PRAs on modified network
            preventiveNetworkActions.forEach(networkAction -> networkAction.apply(initialNetwork));
            preventiveRangeActions.forEach(rangeAction -> {
                double optimizedSetpoint = result.getIndividualRaoResult(offsetDateTime).getOptimizedSetPointOnState(raoInput.getRaoInputs().getData(offsetDateTime).get().getCrac().getPreventiveState(), rangeAction);
                if (rangeAction instanceof InjectionRangeAction) {
                    applyRedispatchingAction((InjectionRangeAction) rangeAction, optimizedSetpoint, initialNetwork);
                } else {
                    rangeAction.apply(initialNetwork, optimizedSetpoint);
                }
            });
            // Write network
            String path = raoInput.getRaoInputs().getData(offsetDateTime).orElseThrow().getPostIcsImportNetworkPath().split(".xiidm")[0].concat("_afterPRA.xiidm");
            String name = path.substring(path.lastIndexOf("/") + 1);
            initialNetwork.write("XIIDM", new Properties(), Path.of(path));

            // Add network to zip
            ZipEntry entry = new ZipEntry(name);
            zipOutputStream.putNextEntry(entry);
            File generatedNetwork = new File(path);
            byte[] fileInByte = FileUtils.readFileToByteArray(generatedNetwork);
            InputStream is = new ByteArrayInputStream(fileInByte);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }
            is.close();
            generatedNetwork.delete();
        }
        zipOutputStream.close();
    }

    private static void applyRedispatchingAction(InjectionRangeAction injectionRangeAction, double optimizedSetpoint, Network initialNetwork) {
        double initialSetpoint = injectionRangeAction.getInitialSetpoint();
        for (NetworkElement networkElement : injectionRangeAction.getNetworkElements()) {
            Generator generator = initialNetwork.getGenerator(networkElement.getId());
            generator.setTargetP(generator.getTargetP()
                + (optimizedSetpoint - initialSetpoint) * injectionRangeAction.getInjectionDistributionKeys().get(networkElement));
        }
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

    private static CommandLineOptions parseCommandLineOptions(String[] args) {
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
}
