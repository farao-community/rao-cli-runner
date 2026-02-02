package com.farao_community.farao.rao_cli_runner.inter_temporal;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SwitchPredicates;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.CracFactory;
import com.powsybl.openrao.data.crac.api.Identifiable;
import com.powsybl.openrao.data.crac.api.InstantKind;
import com.powsybl.openrao.data.crac.api.RemedialAction;
import com.powsybl.openrao.data.crac.api.cnec.FlowCnecAdder;
import com.powsybl.openrao.data.crac.api.rangeaction.InjectionRangeActionAdder;
import com.powsybl.openrao.data.crac.api.rangeaction.VariationDirection;
import com.powsybl.openrao.data.intertemporalconstraints.IntertemporalConstraints;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CracGenerator {
    // TODO move to commons module
    final CracGeneratorParameters parameters;

    private static final String PREVENTIVE_INSTANT_ID = "preventive";
    private static final String OUTAGE_INSTANT_ID = "outage";
    private static final String CURATIVE_INSTANT_ID = "curative";

    public CracGenerator(CracGeneratorParameters parameters) {
        this.parameters = parameters;
    }

    public Crac generateCrac(OffsetDateTime timestamp, Network network, IntertemporalConstraints intertemporalConstraints) {
        Crac crac = CracFactory.findDefault().create("crac", "crac", timestamp);
        addInstants(crac);
        addPreventiveCnecs(crac, network);
        // TODO add outage & curative CNECs
        if (parameters.isRdOnAllGenerators()) {
            addRedispatchActionsOnAllGenerators(network, crac);
        } else {
            addRdActionsFromInterTemporalCts(network, intertemporalConstraints, crac);
        }
        addCtActions(crac, network);
        if (parameters.isAddBalancingAction()) {
            addBalancingAction(crac, network);
        }
        return crac;
    }

    private static void addRdActionsFromInterTemporalCts(Network network, IntertemporalConstraints intertemporalConstraints, Crac crac) {
        intertemporalConstraints.getGeneratorConstraints().forEach(ct -> {
            Generator generator = network.getGenerator(ct.getGeneratorId());
            if (generator == null) {
                System.err.println("No generator found with id " + ct.getGeneratorId() + ". Will not be added to CRAC.");
                return;
            }
            double initialP = Math.round(generator.getTargetP()); // TODO round it in network too
            crac.newInjectionRangeAction()
                .withId("RD_" + generator.getId())
                .withNetworkElementAndKey(1.0, generator.getId())
                .newRange()
                .withMin(Math.min(generator.getMinP(), generator.getTargetP()))
                .withMax(Math.max(generator.getMaxP(), generator.getTargetP())).add()
                .newOnInstantUsageRule().withInstant(PREVENTIVE_INSTANT_ID).add()
                .withInitialSetpoint(initialP)
                .withVariationCost(1., VariationDirection.DOWN)
                .withVariationCost(1., VariationDirection.UP)
                .withActivationCost(100.)
                .add();
            // connect the generator
            generator.connect(SwitchPredicates.IS_OPEN);
        });
    }

    private void addRedispatchActionsOnAllGenerators(Network network, Crac crac) {
        for (Generator generator : network.getGenerators()) {
            double initialP = Math.round(generator.getTargetP()); // TODO round it in network too
            if (initialP < 300 || !Utils.generatorIsInCountries(generator, parameters.getRdFilter())) {
                continue;
            }
            crac.newInjectionRangeAction()
                .withId("RD_" + generator.getId())
                .withNetworkElementAndKey(1.0, generator.getId())
                .newRange()
                .withMin(Math.min(generator.getMinP(), generator.getTargetP()))
                .withMax(Math.max(generator.getMaxP(), generator.getTargetP())).add()
                .newOnInstantUsageRule().withInstant(PREVENTIVE_INSTANT_ID).add()
                .withInitialSetpoint(initialP)
                .withVariationCost(1., VariationDirection.DOWN)
                .withVariationCost(1., VariationDirection.UP)
                .withActivationCost(100.)
                .add();
            // connect the generator
            generator.connect(SwitchPredicates.IS_OPEN);
        }
    }


    private void addCtActions(Crac crac, Network network) {
        parameters.getCtFilter().forEach(country -> {
            if (!country.equals(parameters.getCtHome())) {
                Set<Generator> consideredGenerators = network.getGeneratorStream()
                    .filter(generator -> Utils.generatorIsInCountries(generator, Set.of(country)))
                    .filter(generator -> generator.getTargetP() >= 25.)
                    .collect(Collectors.toSet());

                double initialTotalP = Math.round(consideredGenerators.stream()
                    .mapToDouble(Generator::getTargetP).sum());

                InjectionRangeActionAdder injectionRangeActionAdder = crac.newInjectionRangeAction()
                    .withId("CT_" + country.getName())
                    .newRange()
                    .withMin(initialTotalP - 1500.)
                    .withMax(initialTotalP + 1500.)
                    .add()
                    .withInitialSetpoint(initialTotalP)
                    .withVariationCost(10., VariationDirection.DOWN)
                    .withVariationCost(10., VariationDirection.UP)
                    .withActivationCost(1000.)
                    .newOnInstantUsageRule().withInstant(PREVENTIVE_INSTANT_ID).add();

                AtomicReference<Double> s = new AtomicReference<>((double) 0);
                consideredGenerators.forEach(generator -> {
                    s.updateAndGet(v -> v + generator.getTargetP() / initialTotalP);
                    injectionRangeActionAdder.withNetworkElementAndKey(generator.getTargetP() / initialTotalP, generator.getId());
                });

                if (initialTotalP >= 1.) {
                    injectionRangeActionAdder.add();
                    crac.getInjectionRangeAction("CT_" + country.getName()).apply(network, initialTotalP);
                }
            }
        });
    }

    private static void addInstants(Crac crac) {
        crac.newInstant(PREVENTIVE_INSTANT_ID, InstantKind.PREVENTIVE)
            .newInstant(OUTAGE_INSTANT_ID, InstantKind.OUTAGE)
            .newInstant(CURATIVE_INSTANT_ID, InstantKind.CURATIVE);
    }

    private void addPreventiveCnecs(Crac crac, Network network) {
        // TODO consider implementing REBALANCED_DC
        network.getBranchStream()
            .filter(branch -> Utils.branchIsInCountries(branch, parameters.getBranchFilter()) && Utils.branchIsInVRange(branch, parameters.getBranchMinVoltage(), parameters.getBranchMaxVoltage()))
            .forEach(branch -> {
                if (branch.getSelectedOperationalLimitsGroup1().isPresent()) {
                    double limit = parameters.getPrevCapacityCoef() * ((OperationalLimitsGroup) branch.getSelectedOperationalLimitsGroup1().get())
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
                    //System.err.println(branch.getId() + " skipped : it has no operational limits.");
                }
            });
    }

    private void addBalancingAction(Crac crac, Network network) {
        Set<String> generatorsWithRedispatching = crac.getInjectionRangeActions().stream().map(RemedialAction::getNetworkElements).flatMap(Collection::stream)
            .map(Identifiable::getId).collect(Collectors.toSet());
        final double[] s = {0.};
        Set<Generator> generators = new HashSet<>();
        network.getGenerators().forEach(generator -> {
            if (Utils.generatorIsInCountries(generator, parameters.getRdFilter())
                && generator.getTargetP() > 100.
                && !generatorsWithRedispatching.contains(generator.getId())) {
                generators.add(generator);
                s[0] += generator.getTargetP();
            }
        });
        InjectionRangeActionAdder injectionRangeActionAdder = crac.newInjectionRangeAction()
            .withId("BALANCING")
            .withInitialSetpoint(s[0])
            .withVariationCost(1000., VariationDirection.UP)
            .withVariationCost(1000., VariationDirection.DOWN)
            .newOnInstantUsageRule()
            .withInstant(PREVENTIVE_INSTANT_ID)
            .add()
            .newRange()
            .withMin(s[0] - 1000.)
            .withMax(s[0] + 1000.)
            .add();

        generators.forEach(generator -> injectionRangeActionAdder.withNetworkElementAndKey(generator.getTargetP() / s[0], generator.getId()));

        injectionRangeActionAdder.add();
    }
}
