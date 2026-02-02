package com.farao_community.farao.rao_cli_runner.inter_temporal;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
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

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        CommandLineOptions opts = CommandLineHelper.parseCommandLineOptions(args);
        JsonInterTemporalRaoInputs inputs = JsonInterTemporalRaoInputs.read(opts.inputsJsonFilePath());

        if (inputs == null) {
            throw new RuntimeException("Input files could not be read.");
        }

        if (inputs.getTimedInputs().isEmpty()) {
            throw new RuntimeException("TimedInputs is empty.");
        }

        IntertemporalConstraints intertemporalConstraints = JsonIntertemporalConstraints.read(new FileInputStream(inputs.getIcsFile()));
        Map<OffsetDateTime, RaoInputWithNetworkPaths> timedInputMap = buildInputs(inputs, intertemporalConstraints, opts.outputPath());
        InterTemporalRaoInputWithNetworkPaths raoInput = new InterTemporalRaoInputWithNetworkPaths(new TemporalDataImpl<>(timedInputMap), intertemporalConstraints);
        RaoParameters parameters = JsonRaoParameters.read(new FileInputStream(inputs.getParametersFile()));
        InterTemporalRaoResult result = InterTemporalRao.find(opts.algorithm().orElse(null)).run(raoInput, parameters);
        writeRaoResultsZip(opts.outputPath(), result, raoInput);
        exportNetworksWithPras(opts.outputPath(), result, raoInput);
    }

    private static Map<OffsetDateTime, RaoInputWithNetworkPaths> buildInputs(JsonInterTemporalRaoInputs inputs, IntertemporalConstraints intertemporalConstraints, String outputPath) {
        Map<OffsetDateTime, RaoInputWithNetworkPaths> timedInputMap = new HashMap<>();
        inputs.getTimedInputs().stream().sorted(Comparator.comparing(JsonInterTemporalRaoInputs.TimedInput::getTimestamp))
            .forEach(timedInput -> {
                Network network = Network.read(timedInput.getNetworkFile());
                //preprocess(network);
                //network.write("XIIDM", new Properties(), "/tmp", "preprocessed_" + Path.of(timedInput.getNetworkFile()).getFileName().toString());
                //String preprocessedNetworkFile = "/tmp/preprocessed_" + Path.of(timedInput.getNetworkFile()).getFileName().toString() + ".xiidm";
                Crac crac = null;
                if (timedInput.getCracFile() == null) {
                    if (inputs.getCracGeneratorParameters() == null) {
                        throw new RuntimeException("Crac or Crac generator parameters must be defined in input json.");
                    }
                    crac = new CracGenerator(inputs.getCracGeneratorParameters()).generateCrac(timedInput.getTimestamp(), network, intertemporalConstraints);
                    try {
                        OutputStream os = new FileOutputStream(new File(outputPath, "generated_crac_" + timedInput.getTimestamp() + ".json"));
                        crac.write("JSON", os);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
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

    private static void preprocess(Network network) {
        for (HvdcLine hvdcLine : network.getHvdcLines()) {
            if (hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class) != null && hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class).isEnabled()) {
                System.out.println(hvdcLine.getId() + " : deactivating AC emulation");
                hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class).setEnabled(false);
            }
        }
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
}
