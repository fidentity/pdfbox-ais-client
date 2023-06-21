package com.swisscom.ais.client.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.ais.client.AisClientException;
import com.swisscom.ais.client.utils.Loggers;
import com.swisscom.ais.client.utils.Trace;
import com.swisscom.ais.client.utils.Utils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.ssl.PrivateKeyStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import static com.swisscom.ais.client.utils.Utils.closeResource;

public abstract class AbstractRestClientImpl {

    protected static final Logger logClient = LoggerFactory.getLogger(Loggers.CLIENT);
    protected static final Logger logProtocol = LoggerFactory.getLogger(Loggers.CLIENT_PROTOCOL);
    protected static final Logger logReqResp = LoggerFactory.getLogger(Loggers.REQUEST_RESPONSE);
    protected static final Logger logFullReqResp = LoggerFactory.getLogger(Loggers.FULL_REQUEST_RESPONSE);

    protected RestClientConfiguration config;
    protected ObjectMapper jacksonMapper;
    protected CloseableHttpClient httpClient;

    // ----------------------------------------------------------------------------------------------------

    public void setConfiguration(RestClientConfiguration config) {
        this.config = config;
        Security.addProvider(new BouncyCastleProvider());
        jacksonMapper = new ObjectMapper();
        jacksonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jacksonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        jacksonMapper.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);

        SSLConnectionSocketFactory sslConnectionSocketFactory;
        try {
            SSLContextBuilder sslContextBuilder = SSLContexts.custom()
                    .loadKeyMaterial(produceTheKeyStore(config),
                            keyToCharArray(config.getClientKeyPassword()), produceAPrivateKeyStrategy());
            if (Utils.notEmpty(config.getServerCertificateFile())) {
                sslContextBuilder.loadTrustMaterial(produceTheTrustStore(config), null);
            }
            if (config.isSSLCheckDisabled()) {
                sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());
            }
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
        } catch (Exception e) {
            throw new AisClientException("Failed to configure the TLS/SSL connection factory for the AIS client", e);
        }

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(config.getMaxTotalConnections())
                .setMaxConnPerRoute(config.getMaxConnectionsPerRoute())
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();
        RequestConfig httpClientRequestConfig = RequestConfig.custom()
                .setConnectTimeout(config.getConnectionTimeoutInSec(), TimeUnit.SECONDS)
                .setResponseTimeout(config.getResponseTimeoutInSec(), TimeUnit.SECONDS)
                .build();

        setUpRestClient(config, connectionManager, httpClientRequestConfig);
    }

    private void setUpRestClient(RestClientConfiguration config, PoolingHttpClientConnectionManager connectionManager, RequestConfig httpClientRequestConfig) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();

        if (config.isEnableProxy()) {
            enableProxy(httpClientBuilder);
        }

        this.httpClient = httpClientBuilder
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(httpClientRequestConfig)
                .build();
    }

    private void enableProxy(HttpClientBuilder httpClientBuilder) {

        String proxyHost = this.getProxyHost();
        int port = this.getProxyPortNumber();

        setRestClientProxy(httpClientBuilder, proxyHost, port);

        if (config.isEnableProxyAuth()) {
            enableProxyAuthentication(httpClientBuilder, proxyHost, port);
        }
    }

    private void enableProxyAuthentication(HttpClientBuilder httpClientBuilder, String proxyHost, int port) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        String username = this.getProxyUserName();
        char[] password = this.getProxyPassword();

        AuthScope authScope = new AuthScope(proxyHost, port);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        credentialsProvider.setCredentials(authScope, credentials);

        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }

    private void setRestClientProxy(HttpClientBuilder httpClientBuilder, String proxyHost, int port) {
        if (config.getProxyUsername() != null && config.getProxyUsername().length() > 0) {
            httpClientBuilder.setProxy(new HttpHost(new URIAuthority(config.getProxyUsername(), proxyHost, port)));
        } else {
            httpClientBuilder.setProxy(new HttpHost(new URIAuthority(proxyHost, port)));
        }
    }

    private String getProxyHost() {
        String proxyHost = config.getProxyHost();
        if (proxyHost == null || proxyHost.length() == 0) {
            throw new IllegalStateException("Invalid configuration. The server proxy host is missing, empty or invalid.");
        }
        return proxyHost;
    }

    private char[] getProxyPassword() {
        String proxyHost = config.getProxyPassword();
        if (proxyHost == null || proxyHost.length() == 0) {
            throw new IllegalStateException("Invalid configuration. The server proxy password is missing or is empty.");
        }
        return proxyHost.toCharArray();
    }

    private String getProxyUserName() {
        String proxyHost = config.getProxyUsername();
        if (proxyHost == null || proxyHost.length() == 0) {
            throw new IllegalStateException("Invalid configuration. The server proxy username is missing or is empty.");
        }
        return proxyHost;
    }

    private Integer getProxyPortNumber() {
        try {
            return Integer.parseInt(config.getProxyPort());
        } catch (Exception e) {
            throw new IllegalStateException("Invalid configuration. The server proxy port number is missing, empty or invalid.");
        }

    }

    protected void logResponse(String responseJson, String operationName, Trace trace, String SimpleName) {
        if (logReqResp.isInfoEnabled()) {
            String strippedResponse = Utils.stripInnerLargeBase64Content(responseJson, '"', '"');
            logReqResp.info("{}: Received JSON content: {} - {}", operationName, strippedResponse, trace.getId());
        }
        if (logFullReqResp.isInfoEnabled()) {
            logFullReqResp.info("{}: Received JSON content: {} - {}", operationName, responseJson, trace.getId());
        }
        logProtocol.debug("{}: Deserializing JSON to object of type {} - {}", operationName, SimpleName, trace.getId());
    }

    private KeyStore produceTheKeyStore(RestClientConfiguration config) {
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            FileInputStream is = new FileInputStream(config.getClientCertificateFile());
            X509Certificate certificate = (X509Certificate) fact.generateCertificate(is);
            PrivateKey privateKey = getPrivateKey(config.getClientKeyFile(), config.getClientKeyPassword());

            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(null, null);
            keyStore.setKeyEntry("main", privateKey, keyToCharArray(config.getClientKeyPassword()), new Certificate[]{certificate});

            closeResource(is, null);
            return keyStore;
        } catch (Exception e) {
            throw new AisClientException("Failed to initialize the TLS keystore", e);
        }
    }

    private KeyStore produceTheTrustStore(RestClientConfiguration config) {
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            FileInputStream is = new FileInputStream(config.getServerCertificateFile());
            X509Certificate certificate = (X509Certificate) fact.generateCertificate(is);

            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("main", certificate);

            closeResource(is, null);
            return keyStore;
        } catch (Exception e) {
            throw new AisClientException("Failed to initialize the TLS truststore", e);
        }
    }

    public static PrivateKey getPrivateKey(String fileName, String keyPassword) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            // if we read a X509 key we will get immediately a PrivateKeyInfo
            // if the key is a RSA key it is necessary to create a PEMKeyPair first
            PrivateKeyInfo privateKeyInfo;
            PEMParser pemParser;
            try {
                pemParser = new PEMParser(br);
                privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
            } catch (Exception ignored) {
                br.close();
                br = new BufferedReader(new FileReader(fileName));
                pemParser = new PEMParser(br);
                Object pemKeyPair = pemParser.readObject();
                if (pemKeyPair instanceof PEMEncryptedKeyPair) {
                    if (Utils.isEmpty(keyPassword)) {
                        throw new AisClientException("The client private key is encrypted but there is no key password provided " +
                                "(check field 'client.auth.keyPassword' from the config.properties or from " +
                                "the REST client configuration)");
                    }
                    PEMDecryptorProvider decryptionProv = new JcePEMDecryptorProviderBuilder().build(keyPassword.toCharArray());
                    PEMKeyPair decryptedKeyPair = ((PEMEncryptedKeyPair) pemKeyPair).decryptKeyPair(decryptionProv);
                    privateKeyInfo = decryptedKeyPair.getPrivateKeyInfo();
                } else if (pemKeyPair instanceof PKCS8EncryptedPrivateKeyInfo) {
                    InputDecryptorProvider decryptionProv = new JcePKCSPBEInputDecryptorProviderBuilder()
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(keyPassword.toCharArray());
                    privateKeyInfo = ((PKCS8EncryptedPrivateKeyInfo) pemKeyPair).decryptPrivateKeyInfo(decryptionProv);
                } else {
                    privateKeyInfo = ((PEMKeyPair) pemKeyPair).getPrivateKeyInfo();
                }
            }

            pemParser.close();
            br.close();

            JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();
            return jcaPEMKeyConverter.getPrivateKey(privateKeyInfo);
        } catch (Exception e) {
            throw new AisClientException("Failed to initialize the client private key", e);
        }
    }

    private PrivateKeyStrategy produceAPrivateKeyStrategy() {
        return (aliases, sslParameters) -> "main";
    }

    private char[] keyToCharArray(String key) {
        return Utils.isEmpty(key) ? new char[0] : key.toCharArray();
    }

}
