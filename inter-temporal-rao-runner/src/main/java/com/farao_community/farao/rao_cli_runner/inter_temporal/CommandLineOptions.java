package com.farao_community.farao.rao_cli_runner.inter_temporal;

import java.util.Optional;

public record CommandLineOptions(String inputsJsonFilePath, String outputPath, Optional<String> algorithm) {

}
