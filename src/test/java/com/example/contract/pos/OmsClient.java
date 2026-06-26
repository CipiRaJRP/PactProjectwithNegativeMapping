package com.example.contract.pos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.function.BooleanSupplier;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OmsClient {


    private String baseUrl;
    private HttpClient client;


    private static final String BASE_URL = System.getProperty(
            "baseUrl",
            System.getenv().getOrDefault("BASE_URL", "http://localhost:4010/")
    );

    public OmsClient(String url) {

    }

    @BeforeEach
    void setup() {
        this.baseUrl = BASE_URL;
        this.client = HttpClient.newHttpClient();
    }

    public Order getOrder(int id) {

        Response response = given()
                .baseUri(baseUrl)
                .basePath("/orders/" + id)
                .get();

        response.then().statusCode(200);

        int orderId = response.then().extract().path("id");
        String status = response.then().extract().path("status");
        double total = response.then().extract().path("total");

        return new Order(response.statusCode(),orderId,status,total);
    }

    public CreateOrder createOrder(String sku,int quantity){

         String jsonbody = """
                  {
                     "sku": sku,
                     "quantity":quantity
                 }
                 """;

         Response response = given()
                 .baseUri(baseUrl)
                 .basePath("/orders")
                 .header("Content-Type","application/json")
                 .post();

         response.then().statusCode(201);

        ResponseBody body = response.getBody();

        return new CreateOrder(response.statusCode(),response.getBody().path("id"),response.getBody().path("status"));
    }

    record Order(int statuscode,int orderId,String status,double total){}

    record CreateOrder(int statuscode,int orderId,String status){}

}
