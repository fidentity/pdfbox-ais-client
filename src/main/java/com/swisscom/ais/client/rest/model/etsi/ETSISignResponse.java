package com.swisscom.ais.client.rest.model.etsi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ETSISignResponse {

    @JsonProperty("SignatureObject")
    private List<String> signatureObject;

    @JsonProperty("validationInfo")
    private EtsiValidationInfo etsiValidationInfo;

    public List<String> getSignatureObject() {
        return signatureObject;
    }

    @JsonProperty("SignatureObject")
    public void setSignatureObject(List<String> signatureObject) {
        this.signatureObject = signatureObject;
    }

    public EtsiValidationInfo getEtsiValidationInfo() {
        return etsiValidationInfo;
    }
    @JsonProperty("validationInfo")
    public void setEtsiValidationInfo(EtsiValidationInfo etsiValidationInfo) {
        this.etsiValidationInfo = etsiValidationInfo;
    }
}
