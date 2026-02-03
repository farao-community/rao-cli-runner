package com.farao_community.farao.rao_cli_runner.inter_temporal;

import com.powsybl.iidm.network.*;

import java.util.Optional;
import java.util.Set;

public class Utils {

    public static boolean branchIsInVRange(Branch<?> branch, double minV, double maxV) {
        return branch.getTerminal1().getVoltageLevel().getNominalV() >= minV && branch.getTerminal1().getVoltageLevel().getNominalV() <= maxV
            && branch.getTerminal2().getVoltageLevel().getNominalV() >= minV && branch.getTerminal2().getVoltageLevel().getNominalV() <= maxV;
    }

    public static boolean terminalIsInCountries(Terminal terminal, Set<Country> countries) {
        Optional<Substation> optionalSubstation = terminal.getVoltageLevel().getSubstation();
        return optionalSubstation.isPresent() && optionalSubstation.get().getCountry().isPresent() &&
            countries.contains(optionalSubstation.get().getCountry().get());
    }

    public static boolean branchIsInCountries(Branch<?> branch, Set<Country> countries) {
        if (countries == null) {
            return true;
        }
        return terminalIsInCountries(branch.getTerminal1(), countries) || terminalIsInCountries(branch.getTerminal2(), countries);
    }

    public static boolean generatorIsInCountries(Generator generator, Set<Country> countries) {
        if (countries == null) {
            return true;
        }
        Optional<Substation> substationOptional = generator.getTerminal().getVoltageLevel().getSubstation();
        if (substationOptional.isEmpty()) {
            return false;
        }
        Substation substation = substationOptional.get();
        if (substation.getCountry().isEmpty()) {
            return false;
        }
        return countries.contains(substation.getCountry().get());
    }
}
