package com.example.contract.pos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;


@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "oms-provider",pactVersion = PactSpecVersion.V4)
class PosOmsConsumerPactTest {
    @Pact(provider = "oms-provider", consumer = "pos-consumer")
    V4Pact getOrder(PactDslWithProvider builder) {

        return builder
                .given("Order 123 exists")
                .uponReceiving("a request for order 123")
                .path("/orders/123")
                .method("GET")
                .willRespondWith()
                .status(200)
                .matchHeader(
                        "Content-Type",
                        "application/json(;.*)?",
                        "application/json"
                )
                .body(new PactDslJsonBody()
                        .integerType("id", 123)
                        .stringType("status", "CONFIRMED")
                        .numberType("total", 12.0)
                )
                .toPact(V4Pact.class);
    }

    @Pact(provider = "oms-provider",consumer = "pos-consumer")
    V4Pact getInvalidOrder(PactDslWithProvider builder){

        return builder
                .given("Order 9999 does not exists")
                .uponReceiving("a Request of getting an 9999")
                .path("/orders/9999")
                .method("GET")
                .willRespondWith()
                .status(404)
                .matchHeader("Content-Type",
                        "application/json(;.*)?",
                        "application/json"
                )
                .body(new PactDslJsonBody()
                        .stringType("message","Item Not Found"))
                .toPact(V4Pact.class);
    }


    @Test
    @PactTestFor(pactMethod = "getInvalidOrder")
    void testInvalidGetOrder(MockServer mockServer) {

        OmsClient client =
                new OmsClient(mockServer.getUrl());

        OmsClient.InvalidOrder invalid =
                client.invalidgetOrder(9999);

        assertEquals(404, invalid.statuscode());
        assertEquals("Item Not Found", invalid.error());

    }

    @Test
    @PactTestFor(pactMethod = "getOrder")
    void testGetOrder(MockServer mockServer) {

        OmsClient client =
                new OmsClient(mockServer.getUrl());

        OmsClient.Order order =
                client.getOrder(123);

        assertEquals(200, order.statuscode());
        assertEquals(123, order.orderId());
        assertEquals("CONFIRMED", order.status());
        assertEquals(12.0, order.total());
    }



}