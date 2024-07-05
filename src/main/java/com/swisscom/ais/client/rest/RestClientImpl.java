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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swisscom.ais.client.RestClientException;
import com.swisscom.ais.client.rest.model.etsi.ETSISignResponse;
import com.swisscom.ais.client.rest.model.etsi.ETSISigningRequest;
import com.swisscom.ais.client.rest.model.pendingreq.AISPendingRequest;
import com.swisscom.ais.client.rest.model.signreq.AISSignRequest;
import com.swisscom.ais.client.rest.model.signresp.AISSignResponse;
import com.swisscom.ais.client.utils.Trace;
import org.apache.commons.codec.CharEncoding;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import javax.net.ssl.SSLException;
import java.io.IOException;

public class RestClientImpl extends AbstractRestClientImpl implements RestClient {

    @Override
    public AISSignResponse requestSignature(AISSignRequest requestWrapper, Trace trace) {
        return sendAndReceive("SignRequest", config.getRestServiceSignUrl(),
                requestWrapper, AISSignResponse.class, trace);
    }

    @Override
    public AISSignResponse pollForSignatureStatus(AISPendingRequest requestWrapper, Trace trace) {
        return sendAndReceive("PendingRequest", config.getRestServicePendingUrl(),
                requestWrapper, AISSignResponse.class, trace);
    }

    @Override
    public ETSISignResponse signETSI(ETSISigningRequest signingRequest, Trace trace) {
        return sendAndReceive(signingRequest, trace);
    }

    private ETSISignResponse sendAndReceive(ETSISigningRequest signingRequest, Trace trace) {
        String operationName = "SignEtsi";
        logProtocol.debug("{}: Serializing object of type {} to JSON - {}",
                operationName, signingRequest.getClass().getSimpleName(), trace.getId());
        String requestJson;
        try {
            requestJson = jacksonMapper.writeValueAsString(signingRequest);
        } catch (JsonProcessingException e) {
            throw new RestClientException("Failed to serialize request object to JSON, for operation " +
                    operationName + " - " + trace.getId(), e);
        }

        String serviceUrl = config.getAisSigningUrl();

        HttpPost httpPost = new HttpPost(serviceUrl);
        httpPost.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON, false));

        logProtocol.info("{}: Sending request to: [{}] - {}", operationName, serviceUrl, trace.getId());
        logReqResp.info("{}: Sending JSON to: [{}], content: [{}] - {}", operationName, serviceUrl, requestJson, trace.getId());
        logFullReqResp.info("{}: Sending JSON to: [{}], content: [{}] - {}", operationName, serviceUrl, requestJson, trace.getId());

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            logProtocol.info("{}: Received HTTP status code: {} - {}", operationName, response.getCode(), trace.getId());
            String responseJson;
            try {
                responseJson = EntityUtils.toString(response.getEntity());
            } catch (ParseException e) {
                throw new RestClientException("Failed to interpret the HTTP response content as a string, for operation " +
                        operationName + " - " + trace.getId(), e);
            }
            logResponse(responseJson, operationName, trace, String.class.getSimpleName());

            if (response.getCode() == 200) {
                try {
                    return jacksonMapper.readValue(responseJson, ETSISignResponse.class);
                } catch (JsonProcessingException e) {
                    throw new RestClientException("Failed to deserialize JSON content to object of type " +
                            String.class.getSimpleName() + " for operation " +
                            operationName + " - " +
                            trace.getId(), e);
                }
            } else {
                throw new RestClientException("Received fault response: HTTP " +
                        response.getCode() + " " +
                        response.getReasonPhrase() + " - " + trace.getId());
            }
        } catch (SSLException e) {
            throw new RestClientException("TLS/SSL connection failure for " + operationName + " - " + trace.getId(), e);
        } catch (Exception e) {
            throw new RestClientException("Communication failure for " + operationName + " - " + trace.getId(), e);
        }
    }

    private <TReq, TResp> TResp sendAndReceive(String operationName,
                                               String serviceUrl,
                                               TReq requestObject,
                                               @SuppressWarnings("SameParameterValue") Class<TResp> responseClass,
                                               Trace trace) {
        logProtocol.debug("{}: Serializing object of type {} to JSON - {}",
                operationName, requestObject.getClass().getSimpleName(), trace.getId());
        String requestJson;
        try {
            requestJson = jacksonMapper.writeValueAsString(requestObject);
        } catch (JsonProcessingException e) {
            throw new RestClientException("Failed to serialize request object to JSON, for operation " +
                    operationName + " - " + trace.getId(), e);
        }

        HttpPost httpPost = new HttpPost(serviceUrl);
        httpPost.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON, CharEncoding.UTF_8, false));
        httpPost.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON);
        logProtocol.info("{}: Sending request to: [{}] - {}", operationName, serviceUrl, trace.getId());
        logReqResp.info("{}: Sending JSON to: [{}], content: [{}] - {}", operationName, serviceUrl, requestJson, trace.getId());
        logFullReqResp.info("{}: Sending JSON to: [{}], content: [{}] - {}", operationName, serviceUrl, requestJson, trace.getId());

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            logProtocol.info("{}: Received HTTP status code: {} - {}", operationName, response.getCode(), trace.getId());
            String responseJson;
            try {
                responseJson = EntityUtils.toString(response.getEntity());
            } catch (ParseException e) {
                throw new RestClientException("Failed to interpret the HTTP response content as a string, for operation " +
                        operationName + " - " + trace.getId(), e);
            }
            if (response.getCode() == 200) {
                logResponse(responseJson, operationName, trace, responseClass.getSimpleName());
                try {
                    return jacksonMapper.readValue(responseJson, responseClass);
                } catch (JsonProcessingException e) {
                    throw new RestClientException("Failed to deserialize JSON content to object of type " +
                            responseClass.getSimpleName() + " for operation " +
                            operationName + " - " +
                            trace.getId(), e);
                }
            } else {
                throw new RestClientException("Received fault response: HTTP " +
                        response.getCode() + " " +
                        response.getReasonPhrase() + " - " + trace.getId());
            }
        } catch (SSLException e) {
            throw new RestClientException("TLS/SSL connection failure for " + operationName + " - " + trace.getId(), e);
        } catch (Exception e) {
            throw new RestClientException("Communication failure for " + operationName + " - " + trace.getId(), e);
        }
    }


    // ----------------------------------------------------------------------------------------------------

    @Override
    public void close() throws IOException {
        logClient.debug("Closing the REST client");
        if (httpClient != null) {
            logClient.debug("Closing the embedded HTTP client");
            httpClient.close();
        }
    }


}
