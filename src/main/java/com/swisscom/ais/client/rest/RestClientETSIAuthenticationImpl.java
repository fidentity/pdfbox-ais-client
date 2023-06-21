package com.swisscom.ais.client.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swisscom.ais.client.AisClientException;
import com.swisscom.ais.client.rest.model.etsi.auth.TokenRequest;
import com.swisscom.ais.client.rest.model.etsi.auth.TokenResponse;
import com.swisscom.ais.client.utils.Trace;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestClientETSIAuthenticationImpl extends AbstractRestClientImpl implements RestClientETSIAuthentication {

    public RestClientETSIAuthenticationImpl(RestClientConfiguration clientConfiguration) {
        super.setConfiguration(clientConfiguration);
    }

    public TokenResponse getToken(TokenRequest tokenRequest, Trace trace) {
        String operationName = "GetToken";
        logProtocol.debug("{}: Serializing object of type {} to JSON - {}",
                operationName, tokenRequest.getClass().getSimpleName(), trace.getId());

        String serviceUrl = config.getEtsiConfigProps().getOidcUrl();
        HttpPost httpPost = new HttpPost(serviceUrl);

        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("code", tokenRequest.getCode()));
        nameValuePairList.add(new BasicNameValuePair("client_id", config.getEtsiConfigProps().getClientId()));
        nameValuePairList.add(new BasicNameValuePair("client_secret", config.getEtsiConfigProps().getClientSecret()));
        nameValuePairList.add(new BasicNameValuePair("grant_type", "authorization_code"));
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));

        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        logProtocol.info("{}: Sending request to: [{}] - {}", operationName, serviceUrl, trace.getId());
        logProtocol.info("{}: Sending request payload: {}", operationName, nameValuePairList);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            logProtocol.info("{}: Received HTTP status code: {} - {}", operationName, response.getCode(), trace.getId());
            String responseJson;
            try {
                responseJson = EntityUtils.toString(response.getEntity());
            } catch (ParseException e) {
                throw new AisClientException("Failed to interpret the HTTP response content as a string, for operation " +
                        operationName + " - " + trace.getId(), e);
            }
            if (response.getCode() == 201) {
                logResponse(responseJson, operationName, trace, String.class.getSimpleName());
                try {
                    return jacksonMapper.readValue(responseJson, TokenResponse.class);
                } catch (JsonProcessingException e) {
                    throw new AisClientException("Failed to deserialize JSON content to object of type " +
                            String.class.getSimpleName() + " for operation " +
                            operationName + " - " +
                            trace.getId(), e);
                }
            } else {
                throw new AisClientException("Received fault response: HTTP " +
                        response.getCode() + " " +
                        response.getReasonPhrase() + " - " + trace.getId());
            }
        } catch (SSLException e) {
            throw new AisClientException("TLS/SSL connection failure for " + operationName + " - " + trace.getId(), e);
        } catch (Exception e) {
            throw new AisClientException("Communication failure for " + operationName + " - " + trace.getId(), e);
        }
    }

    @Override
    public void close() throws IOException {
        logClient.debug("Closing the REST client");
        if (httpClient != null) {
            logClient.debug("Closing the embedded HTTP client");
            httpClient.close();
        }
    }
}
