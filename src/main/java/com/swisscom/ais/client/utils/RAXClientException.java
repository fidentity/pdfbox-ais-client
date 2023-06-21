package com.swisscom.ais.client.utils;

public class RAXClientException extends RuntimeException {

    public RAXClientException(String message) {
        super(message);
    }

    public RAXClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
