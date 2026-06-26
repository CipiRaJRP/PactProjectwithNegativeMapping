package com.example.tests;

import com.example.config.TestEnvironment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;


import static org.hamcrest.Matchers.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static io.restassured.RestAssured.given;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class wiremock {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private String baseUrl;

    private static  final String post_data = TestEnvironment.required("POST_USER_DATA");

    ObjectMapper mapper = new ObjectMapper();

    Map<String,Object> post_map = mapper.readValue(post_data, Map.class);

    private HttpClient client;

    public wiremock() throws JsonProcessingException {
    }

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + wm.getPort();
        client = HttpClient.newHttpClient();
    }

    @Test
    @DisplayName("HTTP methods validation")
    void httpmethodsvalidation() {

        wm.stubFor(get(urlEqualTo("/SDET/Training/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id":10,
                                  "name":"Raj",
                                  "role":"Trainee"
                                }
                                """)));

        given()
                .baseUri(baseUrl)
                .when()
                .get("/SDET/Training/")
                .then()
                .statusCode(200)
                .body("name",equalTo("Raj"))
                .body("role",equalTo("Trainee"));


        wm.stubFor(post(urlEqualTo("/SDET/Training/"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id":10,
                                  "name":"CipiRaj",
                                  "role":"Trainee"
                                }
                                """)));

       int id =  given()
                .baseUri(baseUrl)
                .when()
                .post("/SDET/Training/")
                .then()
                .statusCode(201)
                .body("name", equalTo("CipiRaj"))
                .body("role", equalTo("Trainee"))
                .extract().path("id");


        wm.stubFor(put(urlEqualTo("/SDET/Training/"+id))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type","application/json")
                .withBody("""
                                {
                                  "id":10,
                                  "name":"Raj",
                                  "role":"SDET-Trainee"
                                }
                                """)));

        given()
                .baseUri(baseUrl)
                .when()
                .put("/SDET/Training/"+id)
                .then()
                .statusCode(200)
                .body("name",equalTo("Raj"))
                .body("role",equalTo("SDET-Trainee"));

        wm.stubFor(delete(urlEqualTo("/SDET/Training/"))
                .willReturn(aResponse()
                        .withStatus(204)));

        given()
                .baseUri(baseUrl)
                .when()
                .delete("/SDET/Training/")
                .then()
                .statusCode(204);

        wm.verify(exactly(1),getRequestedFor(urlPathEqualTo("/SDET/Training/")));

        wm.verify(exactly(1),postRequestedFor(urlPathEqualTo("/SDET/Training/")));

        wm.verify(exactly(1),putRequestedFor(urlPathEqualTo("/SDET/Training/"+id)));

        wm.verify(exactly(1),deleteRequestedFor(urlPathEqualTo("/SDET/Training/")));
    }


    @Test
    @DisplayName("Make slow")
    void makeaslowbyConfiguringTimeout(){
        HttpRequest getrequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/makewiremock/slow"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();


        wm.stubFor(get(urlPathEqualTo("/makewiremock/slow"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(3000)
                ));

        Assertions.assertThrows(HttpTimeoutException.class,()->{client.send(getrequest, ofString());});

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/makewiremock/slow"))
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build();


        wm.stubFor(get(urlPathEqualTo("/makewiremock/slow"))
                .willReturn(aResponse()
                        .withFault(Fault.CONNECTION_RESET_BY_PEER)
                ));

        Assertions.assertThrows(IOException.class,()->{client.send(request, ofString());});

    }

    @Test
    @DisplayName("Scenario Based")
    void scenarioBasedPathFlow(){

        wm.stubFor(post(urlEqualTo("/retail-app/orders"))
                .inScenario("Catalog")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type","application/json")
                        .withBody("""
                                {
                                   "status":"CREATED"
                                }
                                """))
                .willSetStateTo("ALLOCATED"));

        wm.stubFor(post(urlEqualTo("/retail-app/orders"))
                .inScenario("Catalog")
                .whenScenarioStateIs("ALLOCATED")
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type","application/json")
                        .withBody("""
                                {
                                   "status":"ALLOCATED"
                                }
                                """))
                .willSetStateTo("SHIPPED"));

        wm.stubFor(post(urlEqualTo("/retail-app/orders"))
                .inScenario("Catalog")
                .whenScenarioStateIs("SHIPPED")
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type","application/json")
                        .withBody("""
                                {
                                   "status":"SHIPPED"
                                }
                                """)));


        given()
                .baseUri(baseUrl)
                .when().post("/retail-app/orders")
                .then()
                .body("status",equalTo("CREATED"));


        given()
                .baseUri(baseUrl)
                .when().post("/retail-app/orders")
                .then()
                .body("status",equalTo("ALLOCATED"));

        given()
                .baseUri(baseUrl)
                .when().post("/retail-app/orders")
                .then()
                .body("status",equalTo("SHIPPED"));


    }
}

