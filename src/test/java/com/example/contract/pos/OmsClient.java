package com.example.contract.pos;


import static io.restassured.RestAssured.given;


import io.restassured.response.Response;



final class OmsClient {


    private final String baseUrl;



    OmsClient(String url){

        this.baseUrl = url;

    }





    Order getOrder(int id){


        Response response =
                given()
                        .baseUri(baseUrl)

                        .when()
                        .get("/orders/" + id);


        return new Order(

                response.statusCode(),

                ((Number) response.path("id")).intValue(),

                response.path("status"),

                ((Number) response.path("total")).doubleValue()

        );

    }

    InvalidOrder invalidgetOrder(int id){


        Response response =
                given()
                        .baseUri(baseUrl)

                        .when()
                        .get("/orders/" + id);


        return new InvalidOrder(

                response.statusCode(),

                response.path("message")
        );

    }


    public record Order(  int statuscode,
                          int orderId,
                          String status,
                          double total) {
    }

    public record InvalidOrder(int statuscode,String error){}
}















