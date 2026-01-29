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

    public JsonInterTemporalRaoInputs(String parametersFile, String icsFile, List<TimedInput> timedInputs) {
        this.parametersFile = parametersFile;
        this.icsFile = icsFile;
        this.timedInputs = timedInputs;
    }

    public String getParametersFile() {
        return parametersFile;
    }

    public void setParametersFile(String parametersFile) {
        this.parametersFile = parametersFile;
    }

    public String getIcsFile() {
        return icsFile;
    }

    public void setIcsFile(String icsFile) {
        this.icsFile = icsFile;
    }

    public List<TimedInput> getTimedInputs() {
        return timedInputs;
    }

    public void setTimedInputs(List<TimedInput> timedInputs) {
        this.timedInputs = timedInputs;
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

        // Constructeur par défaut nécessaire pour la désérialisation
        public TimedInput() {
        }

        // Constructeur avec paramètres
        public TimedInput(OffsetDateTime timestamp, String networkFile, String cracFile) {
            this.timestamp = timestamp;
            this.networkFile = networkFile;
            this.cracFile = cracFile;
        }

        // Getters et Setters
        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getNetworkFile() {
            return networkFile;
        }

        public void setNetworkFile(String networkFile) {
            this.networkFile = networkFile;
        }

        public String getCracFile() {
            return cracFile;
        }

        public void setCracFile(String cracFile) {
            this.cracFile = cracFile;
        }
    }
}