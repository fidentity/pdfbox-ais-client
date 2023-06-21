package com.swisscom.ais.client.rest.model.etsi.auth;

public class DocumentsDigests {

    private String hash;
    private String label;

    public DocumentsDigests(String hash, String label) {
        this.hash = hash;
        this.label = label;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
