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
import com.swisscom.ais.client.model.VisibleSignatureDefinition;
import com.swisscom.ais.client.rest.RestClientConfiguration;
import com.swisscom.ais.client.rest.RestClientETSIAuthenticationImpl;
import com.swisscom.ais.client.rest.RestClientImpl;
import com.swisscom.ais.client.rest.model.etsi.ETSISignResponse;
import com.swisscom.ais.client.rest.model.etsi.auth.RAXCodeUrlParameters;
import com.swisscom.ais.client.utils.Trace;

import java.time.LocalDateTime;
import java.util.Properties;

public class TestOnDemandSignatureWithETSI {

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(TestOnDemandSignatureWithETSI.class.getResourceAsStream("/etsi-config.properties"));

        RestClientConfiguration config = new RestClientConfiguration();
        config.setETSIFromProperties(properties);

        RestClientConfiguration etsiConfig = RestClientConfiguration.createEtsiConfig(properties);
        RestClientETSIAuthenticationImpl raxRestClient = new RestClientETSIAuthenticationImpl(etsiConfig);

        RestClientImpl aisRestClient = new RestClientImpl();
        aisRestClient.setConfiguration(config);


        try (AISETSIClientImpl aisClient = new AISETSIClientImpl(aisRestClient, raxRestClient)) {
            ETSIUserData userData = new ETSIUserData();
            userData.setFromPropertiesForETSI(properties);
            Trace trace = new Trace(userData.getTransactionId());

            PdfHandle document = new PdfHandle();
            document.setInputFromFile(properties.getProperty("local.test.inputFile"));
            document.setOutputToFile(properties.getProperty("local.test.outputFilePrefix") + getTimeNow() + ".pdf");

            // enable visual signature
            document.setVisibleSignatureDefinition(
                    new VisibleSignatureDefinition(200, 200, 150, 150, 0,
                            properties.getProperty("local.test.visibleSignatureFile"),
                            properties.getProperty("local.test.visibleSignatureFile.ttfFontPath")));

            // prepare document hash
            PdfDocument prepareDocumentForSigning = aisClient.prepareDocumentForSigning(document, userData, trace);
            //open browser and get code for JWT
            String code = aisClient.getCodeFromConsole(new RAXCodeUrlParameters().fromProperties(properties),
                    prepareDocumentForSigning.getBase64HashToSign(), true);
            // get token with the code
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
