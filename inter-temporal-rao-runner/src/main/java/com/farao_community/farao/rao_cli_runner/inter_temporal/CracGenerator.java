package com.farao_community.farao.rao_cli_runner.inter_temporal;

import com.powsybl.iidm.network.*;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.CracFactory;
import com.powsybl.openrao.data.crac.api.InstantKind;
import com.powsybl.openrao.data.crac.api.cnec.FlowCnecAdder;
import com.powsybl.openrao.data.intertemporalconstraints.IntertemporalConstraints;

import java.time.OffsetDateTime;

public class CracGenerator {
    // TODO move to commons module

    // TODO : put the following in parameters
    static final int MIN_VL = 200;
    static final Country COUNTRY = Country.FR;
    static final double PREV_CAPACITY_COEF = 1.0;

    static final String PREVENTIVE_INSTANT_ID = "preventive";
    static final String OUTAGE_INSTANT_ID = "outage";
    static final String CURATIVE_INSTANT_ID = "curative";

    private CracGenerator() {
        // should not be used
    }

    public static Crac generateCrac(OffsetDateTime timestamp,Network network, IntertemporalConstraints intertemporalConstraints) {
        Crac crac = CracFactory.findDefault().create("crac", "crac", timestamp);
        addInstants(crac);
        addPreventiveCnecs(crac, network);
        // TODO add curative CNECs
        addRedispatchActions(network, intertemporalConstraints, crac);
        return crac;
    }

    private static void addRedispatchActions(Network network, IntertemporalConstraints intertemporalConstraints, Crac crac) {
        intertemporalConstraints.getGeneratorConstraints().forEach(ct -> {
            Generator generator = network.getGenerator(ct.getGeneratorId());
            if (generator == null) {
                System.err.println("No generator found with id " + ct.getGeneratorId() + ". Will not be added to CRAC.");
                return;
            }
            crac.newInjectionRangeAction()
                .withId("RD_" + ct.getGeneratorId())
                .withNetworkElementAndKey(1.0, generator.getId())
                .withInitialSetpoint(generator.getTargetP())
                .newRange().withMin(generator.getMinP()).withMax(generator.getMaxP()).add()
                .newOnInstantUsageRule().withInstant(PREVENTIVE_INSTANT_ID).add()
                .add();
        });
    }

    private static void addInstants(Crac crac) {
        crac.newInstant(PREVENTIVE_INSTANT_ID, InstantKind.PREVENTIVE)
        .newInstant(OUTAGE_INSTANT_ID, InstantKind.OUTAGE)
        .newInstant(CURATIVE_INSTANT_ID, InstantKind.CURATIVE);
    }

    private static void addPreventiveCnecs(Crac crac, Network network) {
        // TODO consider implementing REBALANCED_DC
        // TODO allow deactivating country filter by putting null
        network.getBranchStream()
            .filter(branch -> Utils.branchIsInCountry(branch, COUNTRY) && Utils.branchHasHighEnoughTargetV(branch, MIN_VL))
            .forEach(branch -> {
                if (branch.getSelectedOperationalLimitsGroup1().isPresent()) {
                    double limit = PREV_CAPACITY_COEF * ((OperationalLimitsGroup) branch.getSelectedOperationalLimitsGroup1().get())
                        .getCurrentLimits().get().getPermanentLimit();

                    if (limit < 100. || Double.isNaN(limit)) {
                        System.err.println(branch.getId() + " : limit < 100. || Double.isNaN(limit). Skipped.");
                        return;
                    }
                    FlowCnecAdder flowCnecAdder = crac.newFlowCnec()
                        .withNetworkElement(branch.getId())
                        .withId(branch.getId() + "_PREVENTIVE")
                        .withInstant(PREVENTIVE_INSTANT_ID)
                        .withNominalVoltage(branch.getTerminal1().getVoltageLevel().getNominalV())
                        .withOptimized();

                    flowCnecAdder.newThreshold()
                        .withSide(TwoSides.ONE)
                        .withMax(limit)
                        .withMin(-limit)
                        .withUnit(Unit.AMPERE)
                        .add();
                    flowCnecAdder.add();
                } else {
                    System.err.println(branch.getId() + " skipped : it has no operational limits.");
                }
            });
    }
}
