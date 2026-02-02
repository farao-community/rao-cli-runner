package com.farao_community.farao.rao_cli_runner.inter_temporal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonInterTemporalRaoInputs {
    private String parametersFile;
    private String icsFile;
    private List<TimedInput> timedInputs;
    private CracGeneratorParameters cracGeneratorParameters;

    public JsonInterTemporalRaoInputs() {
        // used for deserialization
    }

    static JsonInterTemporalRaoInputs read(Path filePath) throws IOException {
        String jsonContent = new String(Files.readAllBytes(filePath));
        return deserializeFromJson(jsonContent);
    }

    static JsonInterTemporalRaoInputs read(String filePath) throws IOException {
        return read(Paths.get(filePath));
    }

    public JsonInterTemporalRaoInputs(String parametersFile, String icsFile, List<TimedInput> timedInputs, CracGeneratorParameters cracGeneratorParameters) {
        this.parametersFile = parametersFile;
        this.icsFile = icsFile;
        this.timedInputs = timedInputs;
        this.cracGeneratorParameters = cracGeneratorParameters;
    }

    public String getParametersFile() {
        return parametersFile;
    }

    public String getIcsFile() {
        return icsFile;
    }

    public List<TimedInput> getTimedInputs() {
        return timedInputs;
    }

    public CracGeneratorParameters getCracGeneratorParameters() {
        return cracGeneratorParameters;
    }

    public String serializeToJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.writeValueAsString(this);
    }

    public static JsonInterTemporalRaoInputs deserializeFromJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(json, JsonInterTemporalRaoInputs.class);
    }

    public static class TimedInput {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private OffsetDateTime timestamp;
        private String networkFile;
        private String cracFile;

        public TimedInput() {
        }

        public TimedInput(OffsetDateTime timestamp, String networkFile, String cracFile) {
            this.timestamp = timestamp;
            this.networkFile = networkFile;
            this.cracFile = cracFile;
        }

        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public String getNetworkFile() {
            return networkFile;
        }

        public String getCracFile() {
            return cracFile;
        }
    }
}