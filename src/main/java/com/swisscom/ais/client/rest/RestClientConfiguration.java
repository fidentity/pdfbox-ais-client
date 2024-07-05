/*
 * Copyright 2021 Swisscom Trust Services (Schweiz) AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.swisscom.ais.client.rest;

import com.swisscom.ais.client.RestClientException;
import com.swisscom.ais.client.utils.ConfigurationProvider;
import com.swisscom.ais.client.utils.ConfigurationProviderPropertiesImpl;

import java.io.IOException;
import java.util.Properties;

import static com.swisscom.ais.client.utils.Utils.*;

public class RestClientConfiguration {

    private static final int CLIENT_MAX_CONNECTION_TOTAL = 20;
    private static final int CLIENT_MAX_CONNECTIONS_PER_ROUTE = 10;
    private static final int CLIENT_SOCKET_TIMEOUT_IN_SEC = 10;
    private static final int CLIENT_RESPONSE_TIMEOUT_IN_SEC = 20;

    // ----------------------------------------------------------------------------------------------------

    private String restServiceSignUrl = "https://ais.swisscom.com/AIS-Server/rs/v1.0/sign";
    private String restServicePendingUrl = "https://ais.swisscom.com/AIS-Server/rs/v1.0/pending";

    private String aisSigningUrl;

    private String clientKeyFile;
    private String clientKeyPassword;

    private String clientCertificateFile;
    private String serverCertificateFile;

    private boolean isSSLCheckDisabled;

    private int maxTotalConnections = CLIENT_MAX_CONNECTION_TOTAL;
    private int maxConnectionsPerRoute = CLIENT_MAX_CONNECTIONS_PER_ROUTE;
    private int connectionTimeoutInSec = CLIENT_SOCKET_TIMEOUT_IN_SEC;
    private int responseTimeoutInSec = CLIENT_RESPONSE_TIMEOUT_IN_SEC;

    // ----------------------------------------------------------------------------------------------------

    private boolean enableProxy;
    private String proxyHost;
    private String proxyPort;

    private boolean enableProxyAuth;
    private String proxyUsername;
    private String proxyPassword;

    // ----------------------------------------------------------------------------------------------------

    private ETSIConfigProps etsiConfigProps;

    // ----------------------------------------------------------------------------------------------------


    public String getClientKeyFile() {
        return clientKeyFile;
    }

    public void setClientKeyFile(String clientKeyFile) {
        valueNotEmpty(clientKeyFile,
                "The clientKeyFile parameter of the REST client configuration must not be empty", null);
        this.clientKeyFile = clientKeyFile;
    }

    public String getClientKeyPassword() {
        return clientKeyPassword;
    }

    public void setClientKeyPassword(String clientKeyPassword) {
        this.clientKeyPassword = clientKeyPassword;
    }

    public String getClientCertificateFile() {
        return clientCertificateFile;
    }

    public void setClientCertificateFile(String clientCertificateFile) {
        valueNotEmpty(clientCertificateFile,
                "The clientCertificateFile parameter of the REST client configuration must not be empty", null);
        this.clientCertificateFile = clientCertificateFile;
    }

    public String getServerCertificateFile() {
        return serverCertificateFile;
    }

    public void setServerCertificateFile(String serverCertificateFile) {
        this.serverCertificateFile = serverCertificateFile;
    }

    public String getRestServiceSignUrl() {
        return restServiceSignUrl;
    }

    public void setRestServiceSignUrl(String restServiceSignUrl) {
        valueNotEmpty(restServiceSignUrl,
                "The restServiceSignUrl parameter of the REST client configuration must not be empty", null);
        this.restServiceSignUrl = restServiceSignUrl;
    }

    public String getRestServicePendingUrl() {
        return restServicePendingUrl;
    }

    public void setRestServicePendingUrl(String restServicePendingUrl) {
        valueNotEmpty(restServicePendingUrl,
                "The restServicePendingUrl parameter of the REST client configuration must not be empty", null);
        this.restServicePendingUrl = restServicePendingUrl;
    }

    public String getAisSigningUrl() {
        return aisSigningUrl;
    }

    public void setAisSigningUrl(String aisSigningUrl) {
        this.aisSigningUrl = aisSigningUrl;
    }

    public boolean isSSLCheckDisabled() {
        return isSSLCheckDisabled;
    }

    public void setSSLCheckDisabled(boolean SSLCheckDisabled) {
        isSSLCheckDisabled = SSLCheckDisabled;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        valueBetween(maxTotalConnections, 2, 100,
                "The maxTotalConnections parameter of the REST client configuration must be between 2 and 100", null);
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        valueBetween(maxConnectionsPerRoute, 2, 100,
                "The maxConnectionsPerRoute parameter of the REST client configuration must be between 2 and 100", null);
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public int getConnectionTimeoutInSec() {
        return connectionTimeoutInSec;
    }

    public void setConnectionTimeoutInSec(int connectionTimeoutInSec) {
        valueBetween(connectionTimeoutInSec, 2, 100,
                "The connectionTimeoutInSec parameter of the REST client configuration must be between 2 and 100", null);
        this.connectionTimeoutInSec = connectionTimeoutInSec;
    }

    public int getResponseTimeoutInSec() {
        return responseTimeoutInSec;
    }

    public void setResponseTimeoutInSec(int responseTimeoutInSec) {
        valueBetween(responseTimeoutInSec, 2, 100,
                "The responseTimeoutInSec parameter of the REST client configuration must be between 2 and 100", null);
        this.responseTimeoutInSec = responseTimeoutInSec;
    }

    public boolean isEnableProxy() {
        return enableProxy;
    }

    public void setEnableProxy(boolean enableProxy) {
        this.enableProxy = enableProxy;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public boolean isEnableProxyAuth() {
        return enableProxyAuth;
    }

    public void setEnableProxyAuth(boolean enableProxyAuth) {
        this.enableProxyAuth = enableProxyAuth;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public ETSIConfigProps getEtsiConfigProps() {
        return etsiConfigProps;
    }

    public void setEtsiConfigProps(ETSIConfigProps etsiConfigProps) {
        this.etsiConfigProps = etsiConfigProps;
    }


    // ----------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public void setFromPropertiesClasspathFile(String fileName) {
        Properties properties;
        try {
            properties = new Properties();
            properties.load(this.getClass().getResourceAsStream(fileName));
        } catch (IOException exception) {
            throw new RestClientException("Failed to load REST client properties from classpath file: [" + fileName + "]", exception);
        }
        setFromProperties(properties);
    }

    public void setFromProperties(Properties properties) {
        setFromConfigurationProvider(new ConfigurationProviderPropertiesImpl(properties));
    }

    public void setFromConfigurationProvider(ConfigurationProvider provider) {
        setRestServiceSignUrl(getStringNotNull(provider, "server.rest.signUrl"));
        setRestServicePendingUrl(getStringNotNull(provider, "server.rest.pendingUrl"));
        setClientKeyFile(getStringNotNull(provider, "client.auth.keyFile"));
        setClientKeyPassword(provider.getProperty("client.auth.keyPassword"));
        setClientCertificateFile(getStringNotNull(provider, "client.cert.file"));
        setServerCertificateFile(provider.getProperty("server.cert.file"));
        setMaxTotalConnections(getIntNotNull(provider, "client.http.maxTotalConnections"));
        setMaxConnectionsPerRoute(getIntNotNull(provider, "client.http.maxConnectionsPerRoute"));
        setConnectionTimeoutInSec(getIntNotNull(provider, "client.http.connectionTimeoutInSeconds"));
        setResponseTimeoutInSec(getIntNotNull(provider, "client.http.responseTimeoutInSeconds"));

        setEnableProxy(getBooleanNotNull(provider, "server.rest.proxy.enableProxy"));
        setProxyHost(provider.getProperty("server.rest.proxy.host"));
        setProxyPort(provider.getProperty("server.rest.proxy.port"));

        setEnableProxyAuth(getBooleanNotNull(provider, "server.rest.proxy.enableAuthentication"));
        setProxyPassword(provider.getProperty("server.rest.proxy.password"));
        setProxyUsername(provider.getProperty("server.rest.proxy.username"));
        setSSLCheckDisabled(Boolean.parseBoolean(provider.getProperty("ais.jvm.ssl.check.disabled")));

    }

    public void setETSIFromProperties(Properties properties) {
        setETSIAIS(new ConfigurationProviderPropertiesImpl(properties));
    }

    private void setETSIAIS(ConfigurationProvider provider) {
        setAisSigningUrl(getStringNotNull(provider, "etsi.ais.sign.url"));
        setClientKeyFile(getStringNotNull(provider, "etsi.crt.key"));
        setClientKeyPassword(provider.getProperty("client.auth.keyPassword"));
        setClientCertificateFile(getStringNotNull(provider, "etsi.crt.file"));
        setSSLCheckDisabled(Boolean.parseBoolean(provider.getProperty("rax.jvm.ssl.check.disabled")));
    }

    public void setEtsiFromProperties(Properties properties) {
        ConfigurationProviderPropertiesImpl provider = new ConfigurationProviderPropertiesImpl(properties);
        setClientCertificateFile(getStringNotNull(provider, "client.cert.file"));
        setClientKeyFile(getStringNotNull(provider, "client.auth.keyFile"));
        setClientKeyPassword(provider.getProperty("etsi.client.auth.keyPassword"));
        ETSIConfigProps etsiConfigProps = new ETSIConfigProps();
        etsiConfigProps.setOidcUrl(getStringNotNull(provider, "etsi.oidcUrl"));
        etsiConfigProps.setClientId(getStringNotNull(provider, "etsi.clientId"));
        etsiConfigProps.setClientSecret(getStringNotNull(provider, "etsi.client.secret"));
        setEtsiConfigProps(etsiConfigProps);
    }

    public static RestClientConfiguration createEtsiConfig(Properties properties) {
        RestClientConfiguration restClientConfiguration = new RestClientConfiguration();
        restClientConfiguration.setEtsiFromProperties(properties);
        return restClientConfiguration;
    }
}
