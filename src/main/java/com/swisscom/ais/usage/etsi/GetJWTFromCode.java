package com.swisscom.ais.usage.etsi;

import com.swisscom.ais.client.impl.AISETSIClientImpl;
import com.swisscom.ais.client.model.ETSIUserData;
import com.swisscom.ais.client.rest.RestClientConfiguration;
import com.swisscom.ais.client.rest.RestClientETSIAuthenticationImpl;
import com.swisscom.ais.client.rest.RestClientImpl;
import com.swisscom.ais.client.rest.model.etsi.auth.RAXCodeUrlParameters;
import com.swisscom.ais.client.utils.Trace;

import java.util.Properties;
import java.util.Scanner;

public class GetJWTFromCode {

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

            System.out.println("Waiting Document hash: ");
            Scanner keyboard = new Scanner(System.in);
            String documentHash = keyboard.nextLine();

            String code = aisClient.getCodeFromConsole(new RAXCodeUrlParameters().fromProperties(properties), documentHash, true);

            String jwtToken = aisClient.getJWTToken(code, trace);
            System.out.println(jwtToken);
        }
    }
}
