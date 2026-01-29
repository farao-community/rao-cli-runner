package com.farao_community.farao.rao_cli_runner.inter_temporal;

import com.powsybl.iidm.network.*;

import java.util.Optional;

public class Utils {

    public static boolean branchHasHighEnoughTargetV(Branch<?> branch, double minV) {
        return branch.getTerminal1().getVoltageLevel().getNominalV() > minV && branch.getTerminal2().getVoltageLevel().getNominalV() > minV;
    }

    public static boolean terminalIsInCountry(Terminal terminal, Country country) {
        Optional<Substation> optionalSubstation = terminal.getVoltageLevel().getSubstation();
        return optionalSubstation.isPresent() && optionalSubstation.get().getCountry().isPresent() && optionalSubstation.get().getCountry().get().equals(country);
    }

    public static boolean branchIsInCountry(Branch<?> branch, Country country) {
        return true;
        // TODO allow activating / deactivating this filter
        // return terminalIsInCountry(branch.getTerminal1(), country) || terminalIsInCountry(branch.getTerminal2(), country);
    }
}
