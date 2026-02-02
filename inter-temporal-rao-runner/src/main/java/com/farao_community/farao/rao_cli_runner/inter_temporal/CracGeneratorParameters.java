package com.farao_community.farao.rao_cli_runner.inter_temporal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.Country;
import java.util.Set;
import java.io.Serializable;

public class CracGeneratorParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("branchMaxVoltage")
    private int branchMaxVoltage;

    @JsonProperty("branchMinVoltage")
    private int branchMinVoltage;

    @JsonProperty("branchFilter")
    private Set<Country> branchFilter;

    @JsonProperty("prevCapacityCoef")
    private double prevCapacityCoef;

    @JsonProperty("rdFilter")
    private Set<Country> rdFilter;

    @JsonProperty("ctFilter")
    private Set<Country> ctFilter;

    @JsonProperty("ctHome")
    private Country ctHome;

    @JsonProperty("rdOnAllGenerators")
    private boolean rdOnAllGenerators;

    @JsonProperty("addBalancingAction")
    private boolean addBalancingAction;

    // Constructeur par défaut
    public CracGeneratorParameters() {
    }

    // Constructeur avec arguments
    @JsonCreator
    public CracGeneratorParameters(@JsonProperty("branchMaxVoltage") int branchMaxVoltage,
                                   @JsonProperty("branchMinVoltage") int branchMinVoltage,
                                   @JsonProperty("branchFilter") Set<Country> branchFilter,
                                   @JsonProperty("prevCapacityCoef") double prevCapacityCoef,
                                   @JsonProperty("rdFilter") Set<Country> rdFilter,
                                   @JsonProperty("ctFilter") Set<Country> ctFilter,
                                   @JsonProperty("ctHome") Country ctHome,
                                   @JsonProperty("rdOnAllGenerators") boolean rdOnAllGenerators,
                                   @JsonProperty("addBalancingAction") boolean addBalancingAction) {
        this.branchMaxVoltage = branchMaxVoltage;
        this.branchMinVoltage = branchMinVoltage;
        this.branchFilter = branchFilter;
        this.prevCapacityCoef = prevCapacityCoef;
        this.rdFilter = rdFilter;
        this.ctFilter = ctFilter;
        this.ctHome = ctHome;
        this.rdOnAllGenerators = rdOnAllGenerators;
        this.addBalancingAction = addBalancingAction;
    }

    // Getters et Setters
    public int getBranchMaxVoltage() {
        return branchMaxVoltage;
    }

    public void setBranchMaxVoltage(int branchMaxVoltage) {
        this.branchMaxVoltage = branchMaxVoltage;
    }

    public int getBranchMinVoltage() {
        return branchMinVoltage;
    }

    public void setBranchMinVoltage(int branchMinVoltage) {
        this.branchMinVoltage = branchMinVoltage;
    }

    public Set<Country> getBranchFilter() {
        return branchFilter;
    }

    public void setBranchFilter(Set<Country> branchFilter) {
        this.branchFilter = branchFilter;
    }

    public double getPrevCapacityCoef() {
        return prevCapacityCoef;
    }

    public void setPrevCapacityCoef(double prevCapacityCoef) {
        this.prevCapacityCoef = prevCapacityCoef;
    }

    public Set<Country> getRdFilter() {
        return rdFilter;
    }

    public void setRdFilter(Set<Country> rdFilter) {
        this.rdFilter = rdFilter;
    }

    public Set<Country> getCtFilter() {
        return ctFilter;
    }

    public void setCtFilter(Set<Country> ctFilter) {
        this.ctFilter = ctFilter;
    }

    public Country getCtHome() {
        return ctHome;
    }

    public void setCtHome(Country ctHome) {
        this.ctHome = ctHome;
    }

    public boolean isRdOnAllGenerators() {
        return rdOnAllGenerators;
    }

    public void setRdOnAllGenerators(boolean rdOnAllGenerators) {
        this.rdOnAllGenerators = rdOnAllGenerators;
    }

    public boolean isAddBalancingAction() {
        return addBalancingAction;
    }

    public void setAddBalancingAction(boolean addBalancingAction) {
        this.addBalancingAction = addBalancingAction;
    }

    // Méthode pour sérialiser en JSON
    public String toJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    // Méthode pour désérialiser à partir de JSON
    public static CracGeneratorParameters fromJson(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, CracGeneratorParameters.class);
    }
}
