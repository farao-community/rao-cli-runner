package com.farao_community.farao.rao_cli_runner.inter_temporal;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
    }
}
