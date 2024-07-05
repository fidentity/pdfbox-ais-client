package com.swisscom.ais.usage.etsi;

import com.swisscom.ais.client.impl.AISETSIClientImpl;
import com.swisscom.ais.client.impl.PdfDocument;
import com.swisscom.ais.client.model.ETSIUserData;
import com.swisscom.ais.client.model.PdfHandle;
import com.swisscom.ais.client.model.VisibleSignatureDefinition;
import com.swisscom.ais.client.rest.RestClientConfiguration;
import com.swisscom.ais.client.rest.RestClientImpl;
import com.swisscom.ais.client.utils.Trace;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Scanner;

public class TestOnDemandSignatureWithETSINoJWTRetrieval {

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(TestOnDemandSignatureWithETSINoJWTRetrieval.class.getResourceAsStream("/etsi-config-no-jwt-retrieval.properties"));

        // Configure AIS client
        RestClientImpl aisRestClient = new RestClientImpl();
        RestClientConfiguration config = new RestClientConfiguration();
        config.setETSIFromProperties(properties);
        aisRestClient.setConfiguration(config);


        try (AISETSIClientImpl aisClient = new AISETSIClientImpl(aisRestClient)) {
            // Set data about signature
            ETSIUserData userData = new ETSIUserData();
            userData.setFromPropertiesForETSI(properties);
            Trace trace = new Trace(userData.getTransactionId());
            PdfHandle document = new PdfHandle();
            document.setInputFromFile(properties.getProperty("local.test.inputFile"));
            document.setOutputToFile(properties.getProperty("local.test.outputFilePrefix") + getTimeNow() + ".pdf");
            document.setVisibleSignatureDefinition(
                    new VisibleSignatureDefinition(200, 200, 150, 150, 0,
                            properties.getProperty("local.test.visibleSignatureFile"),
                            properties.getProperty("local.test.visibleSignatureFile.ttfFontPath")));

            // Prepare document hash
            PdfDocument prepareDocumentForSigning = aisClient.prepareDocumentForSigning(document, userData, trace);
            System.out.println("Document hash: " + prepareDocumentForSigning.getBase64HashToSign());
            // Open browser and get code for JWT
            String jwtToken = waitForJWTToken(); // did it by reading from command line for DEMO purpose

            // Retrieve signature and embed it to PDF
            aisClient.signOnDemandWithETSI(prepareDocumentForSigning, userData, trace, jwtToken);
        }
    }

    private static String waitForJWTToken() {
        System.out.println("Waiting JWT auth: ");
        Scanner keyboard = new Scanner(System.in);
        return keyboard.nextLine();
    }

    private static String getTimeNow() {
        LocalDateTime now = LocalDateTime.now();
        return "on" + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth() + " at " + now.getHour() + "-" + now.getMinute() + "-" + now.getSecond();
    }

}
