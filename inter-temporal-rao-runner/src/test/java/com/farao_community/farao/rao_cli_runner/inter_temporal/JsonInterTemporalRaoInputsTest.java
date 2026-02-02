package com.farao_community.farao.rao_cli_runner.inter_temporal;

import com.powsybl.iidm.network.Country;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JsonInterTemporalRaoInputsTest {
    @Test
    void testJsonInterTemporalRaoInputs() throws IOException {
        String tempFileS = this.getClass().getResource("/inputs1.json").getPath();
        JsonInterTemporalRaoInputs inputs = JsonInterTemporalRaoInputs.read(tempFileS);

        assertEquals("params.json", inputs.getParametersFile());
        assertEquals("ics.json", inputs.getIcsFile());
        assertEquals(2, inputs.getTimedInputs().size());
        JsonInterTemporalRaoInputs.TimedInput timedInput = inputs.getTimedInputs().get(0);
        assertEquals(OffsetDateTime.of(2023, 10, 1, 11, 0, 0, 0, ZoneOffset.UTC), timedInput.getTimestamp());
        assertEquals("network.xiidm", timedInput.getNetworkFile());
        assertEquals("crac.json", timedInput.getCracFile());
        timedInput = inputs.getTimedInputs().get(1);
        assertEquals(OffsetDateTime.of(2023, 10, 1, 11, 15, 0, 0, ZoneOffset.UTC), timedInput.getTimestamp());
        assertEquals("network2.xiidm", timedInput.getNetworkFile());
        assertEquals("crac2.json", timedInput.getCracFile());

        CracGeneratorParameters cracGeneratorParameters = inputs.getCracGeneratorParameters();
        assertEquals(400, cracGeneratorParameters.getBranchMaxVoltage());
        assertEquals(200, cracGeneratorParameters.getBranchMinVoltage());
        assertEquals(Set.of(Country.FR), cracGeneratorParameters.getBranchFilter());
        assertEquals(0.95, cracGeneratorParameters.getPrevCapacityCoef(), 0.001);
        assertEquals(Set.of(Country.BE), cracGeneratorParameters.getRdFilter());
        assertEquals(Set.of(Country.CH, Country.DE, Country.IT), cracGeneratorParameters.getCtFilter());
        assertEquals(Country.ES, cracGeneratorParameters.getCtHome());
        assertFalse(cracGeneratorParameters.isRdOnAllGenerators());
        assertTrue(cracGeneratorParameters.isAddBalancingAction());
        assertEquals(100, cracGeneratorParameters.getRdActivationCost());
        assertEquals(1, cracGeneratorParameters.getRdUpVariationCost());
        assertEquals(1, cracGeneratorParameters.getRdDownVariationCost());
        assertEquals(1000, cracGeneratorParameters.getCtActivationCost());
        assertEquals(10, cracGeneratorParameters.getCtUpVariationCost());
        assertEquals(10, cracGeneratorParameters.getCtDownVariationCost());
        assertEquals(-1500, cracGeneratorParameters.getCtMinMw());
        assertEquals(1500, cracGeneratorParameters.getCtMaxMw());
        assertEquals(0, cracGeneratorParameters.getBalacingActivationCost());
        assertEquals(1000, cracGeneratorParameters.getBalancingUpVariationCost());
        assertEquals(1000, cracGeneratorParameters.getBalancingDownVariationCost());
        assertEquals(-1000, cracGeneratorParameters.getBalancingMinMw());
        assertEquals(1000, cracGeneratorParameters.getBalancingMaxMw());
    }
}
