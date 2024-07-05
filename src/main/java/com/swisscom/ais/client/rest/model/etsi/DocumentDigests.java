package com.swisscom.ais.client.rest.model.etsi;

import java.util.List;

public class DocumentDigests {
    private String hashAlgorithmOID;
    private List<String> hashes;

    public String getHashAlgorithmOID() {
        return hashAlgorithmOID;
    }

    public void setHashAlgorithmOID(String hashAlgorithmOID) {
        this.hashAlgorithmOID = hashAlgorithmOID;
    }

    public List<String> getHashes() {
        return hashes;
    }

    public void setHashes(List<String> hashes) {
        this.hashes = hashes;
    }
}
