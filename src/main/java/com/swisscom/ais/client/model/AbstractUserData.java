package com.swisscom.ais.client.model;

import java.util.UUID;

public abstract class AbstractUserData {

    protected String signatureName;
    protected String signatureReason;
    protected String signatureLocation;
    protected String signatureContactInfo;
    protected String transactionId;

    public AbstractUserData() {
        setTransactionIdToRandomUuid();
    }

    public void setTransactionIdToRandomUuid() {
        this.transactionId = UUID.randomUUID().toString();
    }


    public String getSignatureName() {
        return signatureName;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public String getSignatureReason() {
        return signatureReason;
    }

    public void setSignatureReason(String signatureReason) {
        this.signatureReason = signatureReason;
    }

    public String getSignatureLocation() {
        return signatureLocation;
    }

    public void setSignatureLocation(String signatureLocation) {
        this.signatureLocation = signatureLocation;
    }

    public String getSignatureContactInfo() {
        return signatureContactInfo;
    }

    public void setSignatureContactInfo(String signatureContactInfo) {
        this.signatureContactInfo = signatureContactInfo;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

}
