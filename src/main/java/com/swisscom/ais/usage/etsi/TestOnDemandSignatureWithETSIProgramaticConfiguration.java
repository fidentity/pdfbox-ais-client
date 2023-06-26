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
package com.swisscom.ais.usage.etsi;

import com.swisscom.ais.client.impl.AISETSIClientImpl;
import com.swisscom.ais.client.impl.PdfDocument;
import com.swisscom.ais.client.model.ETSIUserData;
import com.swisscom.ais.client.model.PdfHandle;
import com.swisscom.ais.client.rest.ETSIConfigProps;
import com.swisscom.ais.client.rest.RestClientConfiguration;
import com.swisscom.ais.client.rest.RestClientETSIAuthenticationImpl;
import com.swisscom.ais.client.rest.RestClientImpl;
import com.swisscom.ais.client.rest.model.etsi.ETSISignResponse;
import com.swisscom.ais.client.rest.model.etsi.auth.RAXCodeUrlParameters;
import com.swisscom.ais.client.utils.Trace;

import java.time.LocalDateTime;

public class TestOnDemandSignatureWithETSIProgramaticConfiguration {

    public static void main(String[] args) throws Exception {

        RestClientConfiguration config = new RestClientConfiguration();
        config.setAisSigningUrl("https://xxxxx.xxxx/AIS-Server/etsi/standard/rdsc/v1/signatures/signDoc");
        config.setClientKeyFile("/cert/privateKey.key");
        config.setClientKeyPassword("");
        config.setClientCertificateFile("/cert/certificate.crt");

        RestClientConfiguration etsiConfig = new RestClientConfiguration();
        etsiConfig.setClientCertificateFile("cert/rax/certificate.crt");
        etsiConfig.setClientKeyFile("/Users/vacariuionut/work/02_swiss_power/cert/rax/privatekey.key");
        ETSIConfigProps etsiConfigProps = new ETSIConfigProps();
        etsiConfigProps.setOidcUrl("https://xxxxxxxx-xxxxxxxxxx/auth/realms/broker/protocol/openid-connect/token");
        etsiConfigProps.setClientId("xxxxxxxxxxxx");
        etsiConfigProps.setClientSecret("xxxxxxxxxxxx");
        etsiConfig.setEtsiConfigProps(etsiConfigProps);
        RestClientETSIAuthenticationImpl raxRestClient = new RestClientETSIAuthenticationImpl(etsiConfig);

        RestClientImpl aisRestClient = new RestClientImpl();
        aisRestClient.setConfiguration(config);


        try (AISETSIClientImpl aisClient = new AISETSIClientImpl(aisRestClient, raxRestClient)) {
            String credentialID = "static-saphir4-ch";

            ETSIUserData userData = new ETSIUserData();
            userData.setSignatureName("Test Naeme");
            userData.setSignatureReason("Testing signature");
            userData.setSignatureLocation("Testing location");
            userData.setSignatureContactInfo("tester.test@test.com");
            userData.setCredentialID(credentialID);
            userData.setProfile("Xxxxxxxxxxxxx");
            userData.setHashAlgorithmOID("2.16.840.1.101.3.4.2.1");
            userData.setSignatureFormat("P");
            userData.setConformanceLevel("AdES-B-LTA");
            Trace trace = new Trace(userData.getTransactionId());

            PdfHandle document = new PdfHandle();
            document.setInputFromFile("/empty-doc.pdf");
            document.setOutputToFile("signed" + getTimeNow() + ".pdf");

            //prepare document hash
            PdfDocument prepareDocumentForSigning = aisClient.prepareDocumentForSigning(document, userData, trace);
            //open browser and get code for JWT

            RAXCodeUrlParameters urlDetails = new RAXCodeUrlParameters();

            urlDetails.setRaxURL("https://xxxxxxxxxxxxxxx/en/auth/realms/broker/protocol/openid-connect/auth");
            urlDetails.setState("e034ef94-af77-4665-be5a-20f84589ccf6");
            urlDetails.setNonce("cbebefb1-8935-4d86-886b-a0aafc5db814");
            urlDetails.setCode("code");
            urlDetails.setClient_id("Xxxxxxxxxxxxx");
            urlDetails.setScope("sign");
            urlDetails.setRedirectURI("https://Xxxxxxxxxxxxx");
            urlDetails.setChallangeMethod("S256");
            urlDetails.setInputFromFile("/empty-doc.pdf");
            urlDetails.setHashAlgorithmOID("2.16.840.1.101.3.4.2.1");
            urlDetails.setCredentialID(credentialID);
            String code = aisClient.getCodeFromConsole(urlDetails, prepareDocumentForSigning, true);
            //get token with the code

            System.out.println(prepareDocumentForSigning.getBase64HashToSign());
            String jwtToken = aisClient.getJWTToken(code, trace);

            ETSISignResponse result = aisClient.signOnDemandWithETSI(prepareDocumentForSigning, userData, trace, jwtToken);
            System.out.println("Final result: " + result);
        }
    }

    private static String getTimeNow() {
        LocalDateTime now = LocalDateTime.now();
        return "on" + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth() + " at " + now.getHour() + "-" + now.getMinute() + "-" + now.getSecond();
    }

}
