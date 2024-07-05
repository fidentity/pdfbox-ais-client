package com.swisscom.ais.client.utils;

import com.swisscom.ais.client.RestClientException;
import com.swisscom.ais.client.impl.PdfDocument;
import com.swisscom.ais.client.model.AbstractUserData;
import com.swisscom.ais.client.model.PdfHandle;
import com.swisscom.ais.client.model.SignatureMode;
import com.swisscom.ais.client.model.VisibleSignatureDefinition;
import com.swisscom.ais.client.rest.model.SignatureType;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DocumentUtils {

    public static PdfDocument prepareOneDocumentForSigning(PdfHandle documentHandle,
                                             SignatureMode signatureMode,
                                             SignatureType signatureType,
                                             AbstractUserData userData,
                                             Trace trace) {
        try {
            FileInputStream fileIn = new FileInputStream(documentHandle.getInputFromFile());
            FileOutputStream fileOut = new FileOutputStream(documentHandle.getOutputToFile());
            VisibleSignatureDefinition signatureDefinition = documentHandle.getVisibleSignatureDefinition();

            PdfDocument newDocument = new PdfDocument(documentHandle.getOutputToFile(), fileIn, fileOut, signatureDefinition, trace);
            newDocument.prepareForSigning(documentHandle.getDigestAlgorithm(), signatureType, userData);
            return newDocument;
        } catch (Exception e) {
            throw new RestClientException("Failed to prepare the document [" +
                    documentHandle.getInputFromFile() + "] for " +
                    signatureMode.getFriendlyName() + " signing", e);
        }
    }

}
