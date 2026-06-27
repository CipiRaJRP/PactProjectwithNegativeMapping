package com.example.contract.oms;
 
import au.com.dius.pact.provider.junitsupport.loader.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;

@Provider("oms-provider")
@PactBroker(

        url = "${pact.broker.url}",
        authentication = @PactBrokerAuth(token = "${pact.broker.token}")

)

public class OmsProviderVerification {
    @RegisterExtension
    private static final WireMockExtension wireMock =
        WireMockExtension.newInstance()
            .options(wireMockConfig().port(4010))
            .build();
 
 
    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder
            consumerVersionSelectors() {
        return new SelectorBuilder()
                    .mainBranch()
                    .deployedOrReleased();
    }
 
 
    @BeforeEach
    void setup(PactVerificationContext context) {                                    
        context.setTarget(new HttpTestTarget("127.0.0.1", 4010));
    }
 
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verify(PactVerificationContext context) {
        context.verifyInteraction();
    }
 
 
    @State("Order 123 exists")
    void isOrderExists() {
        wireMock.stubFor(get(urlEqualTo("/orders/123"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"id": 123, "status": "CONFIRMED", "total": 12.0}
                """)));
    }

    @State("Order 9999 does not exists")
    void isOrderNotExists(){
         wireMock.stubFor(get(urlEqualTo("/orders/9999"))
                 .willReturn(aResponse()
                         .withStatus(404)
                         .withHeader("Content-Type","application/json")
                         .withBody("""
                                 {"message":"Item Not Found"}
                                 """)));
    }

}