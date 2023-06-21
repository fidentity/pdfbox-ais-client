package com.swisscom.ais.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.ais.client.AisClientException;
import com.swisscom.ais.client.AisETSIClient;
import com.swisscom.ais.client.model.AbstractUserData;
import com.swisscom.ais.client.model.ETSIUserData;
import com.swisscom.ais.client.model.PdfHandle;
import com.swisscom.ais.client.model.SignatureMode;
import com.swisscom.ais.client.rest.RestClient;
import com.swisscom.ais.client.rest.RestClientETSIAuthentication;
import com.swisscom.ais.client.rest.model.DigestAlgorithm;
import com.swisscom.ais.client.rest.model.SignatureType;
import com.swisscom.ais.client.rest.model.etsi.DocumentDigests;
import com.swisscom.ais.client.rest.model.etsi.ETSISignResponse;
import com.swisscom.ais.client.rest.model.etsi.ETSISigningRequest;
import com.swisscom.ais.client.rest.model.etsi.auth.AuthRequest;
import com.swisscom.ais.client.rest.model.etsi.auth.DocumentsDigests;
import com.swisscom.ais.client.rest.model.etsi.auth.TokenRequest;
import com.swisscom.ais.client.rest.model.etsi.auth.TokenResponse;
import com.swisscom.ais.client.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class AISETSIClientImpl implements AisETSIClient {

    private static final Logger logClient = LoggerFactory.getLogger(Loggers.CLIENT);

    private final RestClientETSIAuthentication raxRestClient;
    private final RestClient aisRestClient;

    public AISETSIClientImpl(RestClient restClient, RestClientETSIAuthentication restClientEtsi) {
        this.aisRestClient = restClient;
        this.raxRestClient = restClientEtsi;
    }

    @Override
    public ETSISignResponse signOnDemandWithETSI(PdfDocument pdfDocument, ETSIUserData userData, Trace trace, String tokenForETSISigning) throws AisClientException {

        ETSISignResponse etsiSignResponse = performSign(pdfDocument, tokenForETSISigning, trace, userData);
        List<byte[]> crlEntries = etsiSignResponse.getEtsiValidationInfo().getCrl().stream().map(crl -> Base64.getDecoder().decode(crl)).collect(Collectors.toList());
        List<byte[]> ocspEntries = etsiSignResponse.getEtsiValidationInfo().getOcsp().stream().map(ocsp -> Base64.getDecoder().decode(ocsp)).collect(Collectors.toList());

        pdfDocument.finishSignature(Base64.getDecoder().decode(etsiSignResponse.getSignatureObject().get(0).getBytes()), crlEntries, ocspEntries);

        return etsiSignResponse;
    }

    @Override
    public PdfDocument prepareDocumentForSigning(PdfHandle documentHandler, AbstractUserData userData, Trace trace) {
        documentHandler.validateYourself(trace);
        documentHandler.setDigestAlgorithm(DigestAlgorithm.SHA256);
        logClient.info("Preparing {} signing for document: {} - {}",
                SignatureMode.ON_DEMAND.getFriendlyName(),
                documentHandler.getInputFromFile(),
                trace.getId());
        return DocumentUtils.prepareOneDocumentForSigning(documentHandler, SignatureMode.ON_DEMAND, SignatureType.CMS, userData, trace);
    }

    @Override
    public String getCodeFromConsole(Properties properties, PdfDocument prepareDocumentForSigning) throws JsonProcessingException {
        String claims = claims(properties, prepareDocumentForSigning);
        String url = createRAXUrl(properties, claims);
        System.out.println("click url to retrieve JWT code: " + url);
        boolean shouldOpenBrowser = Boolean.parseBoolean(properties.getProperty("open.browser"));
        if (shouldOpenBrowser) {
            openBrowserToRAX(url);
        }
        System.out.println("Waiting JWT auth - code: ");
        Scanner keyboard = new Scanner(System.in);
        return keyboard.nextLine();
    }

    @Override
    public String getJWTToken(String code, Trace trace) {
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setCode(code);
        TokenResponse token = raxRestClient.getToken(tokenRequest, trace);
        if (token != null && token.getAccess_token() != null) {
            return token.getAccess_token();
        } else {
            logClient.error("Not able to retreive token for signing.");
            throw new RAXClientException("Not able to retreive token for signing.");
        }
    }

    private ETSISignResponse performSign(PdfDocument documentsToSign, String token, Trace trace, ETSIUserData userData) {

        ETSISigningRequest signingRequest = new ETSISigningRequest();
        signingRequest.setSAD(token);
        signingRequest.setRequestID(Utils.generateRequestId());
        signingRequest.setCredentialID(userData.getCredentialID());
        signingRequest.setProfile(userData.getProfile());
        signingRequest.setSignatureFormat(userData.getSignatureFormat());
        DocumentDigests documentDigests = new DocumentDigests();
        documentDigests.setHashAlgorithmOID(userData.getHashAlgorithmOID());
        documentDigests.setHashes(Collections.singletonList(documentsToSign.getBase64HashToSign()));
        signingRequest.setConformanceLevel(userData.getConformanceLevel());
        signingRequest.setDocumentDigests(documentDigests);

        return aisRestClient.signETSI(signingRequest, trace);
    }

    private static String claims(Properties properties, PdfDocument prepareDocumentForSigning) throws JsonProcessingException {
        String inputFromFile = properties.getProperty("local.test.inputFile");
        AuthRequest raxAuthRequest = new AuthRequest();
        raxAuthRequest.setHashAlgorithmOID(properties.getProperty("etsi.hash.algorithmOID"));
        raxAuthRequest.setCredentialID(properties.getProperty("etsi.credentialID"));
        String[] split = inputFromFile.split("/");
        DocumentsDigests documentsDigests = new DocumentsDigests(prepareDocumentForSigning.getBase64HashToSign()
                , split[split.length - 1]);
        raxAuthRequest.setDocumentDigests(Collections.singletonList(documentsDigests));
        return new ObjectMapper().writeValueAsString(raxAuthRequest);
    }


    private static void openBrowserToRAX(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                System.out.println("Not able to open brower, open it manually");
            }

        }
    }

    private static String createRAXUrl(Properties properties, String claimsJson) {
        ConfigurationProviderPropertiesImpl prov = new ConfigurationProviderPropertiesImpl(properties);
        String raxURL = Utils.getStringNotNull(prov, "etsi.rax.url");
        String state = Utils.getStringNotNull(prov, "etsi.rax.state");
        String nonce = Utils.getStringNotNull(prov, "etsi.rax.nonce");
        String code = getPropOrDefault(prov, "etsi.rax.response_type", "code");
        String client_id = Utils.getStringNotNull(prov, "etsi.rax.client_id");
        String scope = getPropOrDefault(prov, "etsi.rax.scope", "sign");
        String redirectURI = Utils.getStringNotNull(prov, "etsi.rax.redirect_uri");
        String challangeMethod = Utils.getStringNotNull(prov, "etsi.rax.code_challenge_method");

        return raxURL +
                "?" + "state" + "=" + URLEncoder.encode(state) +
                "&" + "nonce" + "=" + URLEncoder.encode(nonce) +
                "&" + "response_type" + "=" + URLEncoder.encode(code) +
                "&" + "client_id" + "=" + URLEncoder.encode(client_id) +
                "&" + "scope" + "=" + URLEncoder.encode(scope) +
                "&" + "redirect_uri" + "=" + URLEncoder.encode(redirectURI) +
                "&" + "code_challenge_method" + "=" + URLEncoder.encode(challangeMethod) +
                "&" + "claims" + "=" + URLEncoder.encode(claimsJson);
    }


    private static String getPropOrDefault(ConfigurationProviderPropertiesImpl configurationProviderProperties, String prop, String defaultVale) {
        return configurationProviderProperties.getProperty(prop) != null ? configurationProviderProperties.getProperty(prop) : defaultVale;
    }

    @Override
    public void close() throws IOException {
        if (aisRestClient != null) {
            aisRestClient.close();
        }
        if (raxRestClient != null) {
            raxRestClient.close();
        }
    }
}