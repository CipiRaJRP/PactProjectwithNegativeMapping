package com.example.contract.oms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;


@Provider("oms-provider")
@PactBroker(url ="http://localhost:9292")
public class OmsProviderVerification {



    @BeforeEach
    void setup(PactVerificationContext context){
        context.setTarget(new HttpTestTarget("localhost", 4010, "/"));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verify(PactVerificationContext context){
        context.verifyInteraction();
    }

    @State("Order 123 exists")
    void isOrderExists(){

    }

    @State("Order created for inventory")
    void isOrderCreated(){

    }

    @State("Sku-9 has stock")
    void isSkuHasStock(){

    }
}

