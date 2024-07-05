package com.swisscom.ais.client.rest;


public class ETSIConfigProps {

    private String oidcUrl;
    private String signatureUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUi;

    public String getOidcUrl() {
        return oidcUrl;
    }

    public void setOidcUrl(String oidcUrl) {
        this.oidcUrl = oidcUrl;
    }

    public String getSignatureUrl() {
        return signatureUrl;
    }

    public void setSignatureUrl(String signatureUrl) {
        this.signatureUrl = signatureUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUi() {
        return redirectUi;
    }

    public void setRedirectUi(String redirectUi) {
        this.redirectUi = redirectUi;
    }

}
