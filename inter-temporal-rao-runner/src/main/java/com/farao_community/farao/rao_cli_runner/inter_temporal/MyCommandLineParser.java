package com.farao_community.farao.rao_cli_runner.inter_temporal;

import org.apache.commons.cli.*;

public class MyCommandLineParser extends DefaultParser {
    private final String helpOption;

    public MyCommandLineParser(final String helpOption) {
        this.helpOption = helpOption;
    }

    @Override
    protected void checkRequiredOptions() throws MissingOptionException {
        // throw if there are required options that have not been processed
        // unless help option has been invoked
        if (!expectedOpts.isEmpty() && !cmd.hasOption(this.helpOption)) {
            throw new MissingOptionException(expectedOpts);
        }
    }
}
