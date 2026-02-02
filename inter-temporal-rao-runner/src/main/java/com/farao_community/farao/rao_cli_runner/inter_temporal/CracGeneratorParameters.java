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

    @JsonProperty("rdActivationCost")
    private double rdActivationCost;

    @JsonProperty("rdUpVariationCost")
    private double rdUpVariationCost;

    @JsonProperty("rdDownVariationCost")
    private double rdDownVariationCost;

    @JsonProperty("ctActivationCost")
    private double ctActivationCost;

    @JsonProperty("ctUpVariationCost")
    private double ctUpVariationCost;

    @JsonProperty("ctDownVariationCost")
    private double ctDownVariationCost;

    @JsonProperty("ctMinMw")
    private double ctMinMw;

    @JsonProperty("ctMaxMw")
    private double ctMaxMw;

    @JsonProperty("balacingActivationCost")
    private double balacingActivationCost;

    @JsonProperty("balancingUpVariationCost")
    private double balancingUpVariationCost;

    @JsonProperty("balancingDownVariationCost")
    private double balancingDownVariationCost;

    @JsonProperty("balancingMinMw")
    private double balancingMinMw;

    @JsonProperty("balancingMaxMw")
    private double balancingMaxMw;

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
                                   @JsonProperty("addBalancingAction") boolean addBalancingAction,
                                   @JsonProperty("rdActivationCost") double rdActivationCost,
                                   @JsonProperty("rdUpVariationCost") double rdUpVariationCost,
                                   @JsonProperty("rdDownVariationCost") double rdDownVariationCost,
                                   @JsonProperty("ctActivationCost") double ctActivationCost,
                                   @JsonProperty("ctUpVariationCost") double ctUpVariationCost,
                                   @JsonProperty("ctDownVariationCost") double ctDownVariationCost,
                                   @JsonProperty("ctMinMw") double ctMinMw,
                                   @JsonProperty("ctMaxMw") double ctMaxMw,
                                   @JsonProperty("balacingActivationCost") double balacingActivationCost,
                                   @JsonProperty("balancingUpVariationCost") double balancingUpVariationCost,
                                   @JsonProperty("balancingDownVariationCost") double balancingDownVariationCost,
                                   @JsonProperty("balancingMinMw") double balancingMinMw,
                                   @JsonProperty("balancingMaxMw") double balancingMaxMw) {
        this.branchMaxVoltage = branchMaxVoltage;
        this.branchMinVoltage = branchMinVoltage;
        this.branchFilter = branchFilter;
        this.prevCapacityCoef = prevCapacityCoef;
        this.rdFilter = rdFilter;
        this.ctFilter = ctFilter;
        this.ctHome = ctHome;
        this.rdOnAllGenerators = rdOnAllGenerators;
        this.addBalancingAction = addBalancingAction;
        this.rdActivationCost = rdActivationCost;
        this.rdUpVariationCost = rdUpVariationCost;
        this.rdDownVariationCost = rdDownVariationCost;
        this.ctActivationCost = ctActivationCost;
        this.ctUpVariationCost = ctUpVariationCost;
        this.ctDownVariationCost = ctDownVariationCost;
        this.ctMinMw = ctMinMw;
        this.ctMaxMw = ctMaxMw;
        this.balacingActivationCost = balacingActivationCost;
        this.balancingUpVariationCost = balancingUpVariationCost;
        this.balancingDownVariationCost = balancingDownVariationCost;
        this.balancingMinMw = balancingMinMw;
        this.balancingMaxMw = balancingMaxMw;
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

    public double getRdActivationCost() {
        return rdActivationCost;
    }

    public void setRdActivationCost(int rdActivationCost) {
        this.rdActivationCost = rdActivationCost;
    }

    public double getRdUpVariationCost() {
        return rdUpVariationCost;
    }

    public void setRdUpVariationCost(int rdUpVariationCost) {
        this.rdUpVariationCost = rdUpVariationCost;
    }

    public double getRdDownVariationCost() {
        return rdDownVariationCost;
    }

    public void setRdDownVariationCost(int rdDownVariationCost) {
        this.rdDownVariationCost = rdDownVariationCost;
    }

    public double getCtActivationCost() {
        return ctActivationCost;
    }

    public void setCtActivationCost(int ctActivationCost) {
        this.ctActivationCost = ctActivationCost;
    }

    public double getCtUpVariationCost() {
        return ctUpVariationCost;
    }

    public void setCtUpVariationCost(int ctUpVariationCost) {
        this.ctUpVariationCost = ctUpVariationCost;
    }

    public double getCtDownVariationCost() {
        return ctDownVariationCost;
    }

    public void setCtDownVariationCost(int ctDownVariationCost) {
        this.ctDownVariationCost = ctDownVariationCost;
    }

    public double getCtMinMw() {
        return ctMinMw;
    }

    public void setCtMinMw(int ctMinMw) {
        this.ctMinMw = ctMinMw;
    }

    public double getCtMaxMw() {
        return ctMaxMw;
    }

    public void setCtMaxMw(int ctMaxMw) {
        this.ctMaxMw = ctMaxMw;
    }

    public double getBalacingActivationCost() {
        return balacingActivationCost;
    }

    public void setBalacingActivationCost(int balacingActivationCost) {
        this.balacingActivationCost = balacingActivationCost;
    }

    public double getBalancingUpVariationCost() {
        return balancingUpVariationCost;
    }

    public void setBalancingUpVariationCost(int balancingUpVariationCost) {
        this.balancingUpVariationCost = balancingUpVariationCost;
    }

    public double getBalancingDownVariationCost() {
        return balancingDownVariationCost;
    }

    public void setBalancingDownVariationCost(int balancingDownVariationCost) {
        this.balancingDownVariationCost = balancingDownVariationCost;
    }

    public double getBalancingMinMw() {
        return balancingMinMw;
    }

    public void setBalancingMinMw(int balancingMinMw) {
        this.balancingMinMw = balancingMinMw;
    }

    public double getBalancingMaxMw() {
        return balancingMaxMw;
    }

    public void setBalancingMaxMw(int balancingMaxMw) {
        this.balancingMaxMw = balancingMaxMw;
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
