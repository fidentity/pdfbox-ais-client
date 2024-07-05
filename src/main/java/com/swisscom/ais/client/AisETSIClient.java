package com.swisscom.ais.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swisscom.ais.client.impl.PdfDocument;
import com.swisscom.ais.client.model.*;
import com.swisscom.ais.client.rest.model.etsi.ETSISignResponse;
import com.swisscom.ais.client.rest.model.etsi.auth.RAXCodeUrlParameters;
import com.swisscom.ais.client.utils.Trace;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;

public interface AisETSIClient extends Closeable {

    ETSISignResponse signOnDemandWithETSI(PdfDocument documentHandles,
                                          ETSIUserData etsiUserData,
                                          Trace trace,
                                          String code) throws RestClientException;

    PdfDocument prepareDocumentForSigning(PdfHandle documentHandler, AbstractUserData userData, Trace trace);

    String getCodeFromConsole(RAXCodeUrlParameters urlDetails, String prepareDocumentForSigning, boolean shouldOpenBrowser) throws JsonProcessingException, UnsupportedEncodingException;

    String getJWTToken(String code, Trace trace) throws JsonProcessingException;
}
