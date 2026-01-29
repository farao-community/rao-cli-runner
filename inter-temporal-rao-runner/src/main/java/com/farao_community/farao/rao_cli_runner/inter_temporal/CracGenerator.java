package com.farao_community.farao.rao_cli_runner.inter_temporal;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.CracFactory;
import com.powsybl.openrao.data.crac.api.InstantKind;
import com.powsybl.openrao.data.intertemporalconstraints.IntertemporalConstraints;

public class CracGenerator {

    static final int MIN_VL = 200;

    private CracGenerator() {
        // should not be used
    }

    public Crac generateCrac(Network network, IntertemporalConstraints intertemporalConstraints) {
        Crac crac = CracFactory.findDefault().create("crac")
            .newInstant("preventive", InstantKind.PREVENTIVE)
            .newInstant("outage", InstantKind.OUTAGE)
            .newInstant("curative", InstantKind.CURATIVE);

        // TODO put code to generate contingencies and cnecs

        intertemporalConstraints.getGeneratorConstraints().forEach(ct -> {
            Generator generator = network.getGenerator(ct.getGeneratorId());
            if (generator == null) {
                System.err.println("No generator found with id " + ct.getGeneratorId() + ". Will not be added to CRAC.");
                return;
            }
            crac.newInjectionRangeAction()
                .withId("RD_" + ct.getGeneratorId())
                .withNetworkElementAndKey(1.0, generator.getId())
                .newRange().withMin(generator.getMinP()).withMax(generator.getMaxP()).add()
                .newOnInstantUsageRule().withInstant(crac.getPreventiveInstant().getId()).add()
                .add();
        });
        return crac;
    }
}
