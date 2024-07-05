package com.swisscom.ais.client.rest;

import com.swisscom.ais.client.rest.model.etsi.auth.TokenRequest;
import com.swisscom.ais.client.rest.model.etsi.auth.TokenResponse;
import com.swisscom.ais.client.utils.Trace;

import java.io.Closeable;

public interface RestClientETSIAuthentication extends Closeable {

    TokenResponse getToken(TokenRequest tokenRequest, Trace trace);

}
