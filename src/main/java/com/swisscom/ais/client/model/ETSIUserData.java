package com.swisscom.ais.client.model;

import com.swisscom.ais.client.utils.ConfigurationProvider;
import com.swisscom.ais.client.utils.ConfigurationProviderPropertiesImpl;

import java.util.Properties;

public class ETSIUserData extends AbstractUserData {


    private String credentialID;
    private String profile;
    private String hashAlgorithmOID;
    private String signatureFormat;
    private String conformanceLevel;

    public void setFromPropertiesForETSI(Properties properties) {
        setFromConfigurationProviderForETSI(new ConfigurationProviderPropertiesImpl(properties));
    }

    private void setFromConfigurationProviderForETSI(ConfigurationProvider provider) {
        signatureName = provider.getProperty("signature.name");
        signatureReason = provider.getProperty("signature.reason");
        signatureLocation = provider.getProperty("signature.location");
        signatureContactInfo = provider.getProperty("signature.contactInfo");
        credentialID = provider.getProperty("etsi.credentialID");
        profile = provider.getProperty("etsi.profile");
        hashAlgorithmOID = provider.getProperty("etsi.hash.algorithmOID");
        signatureFormat = provider.getProperty("etsi.signature.format");
        conformanceLevel = provider.getProperty("etsi.signature.conformance.level");
    }

    public String getCredentialID() {
        return credentialID;
    }

    public String getProfile() {
        return profile;
    }

    public String getHashAlgorithmOID() {
        return hashAlgorithmOID;
    }

    public String getSignatureFormat() {
        return signatureFormat;
    }

    public String getConformanceLevel() {
        return conformanceLevel;
    }
}
