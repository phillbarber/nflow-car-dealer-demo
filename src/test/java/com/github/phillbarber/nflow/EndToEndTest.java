package com.github.phillbarber.nflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@WireMockTest
public class EndToEndTest {

    public static final String WORKFLOW_JSON_FILE = "workflows/car-order-workflow.json";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String restFacadeURL = "http://localhost:7500";
    public static final String restFacadeOrderURL = restFacadeURL + "/order";


    private final HttpClient httpClient = HttpClientBuilder.create().build();

    private StubServices stubServices = new StubServices();


    @BeforeAll
    public static void start(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        Main.start();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        Main.stop();
    }

    @Test
    public void happyPathOrder() throws IOException {
        stubServices.orderServiceReturnsValidOrderFor("Blista");
        stubServices.saveOrderReturnsOK();
        stubServices.priceServiceReturnsPrice();
        stubServices.customerServiceReturnsCustomerFor("12345");
        stubServices.discountServiceReturns();

        Map order = (Map) submitOrderToRestFacade(getHappyPathInput()).get("order");

        assertNotNull(order.get("id"));
        assertNotNull(order.get("customerId"));
        assertNotNull(order.get("customerName"));
        assertNotNull(order.get("customerLoyaltyPoints"));
        assertEquals(order.get("basePrice"), 60000);
        assertEquals(order.get("totalPrice"), 54000);
        assertEquals(order.get("currency"), "GBP");
        assertEquals(order.get("promotionCode"), "ABCDE1234");
        assertEquals(order.get("discount"), 0.1);
    }

    private Map submitOrderToRestFacade(HashMap happyPathInput) {

        Map order;
        HttpPost httpPost = new HttpPost(restFacadeOrderURL);
        try {

            httpPost.setEntity(new StringEntity(OBJECT_MAPPER.writer().writeValueAsString(happyPathInput)));
            httpPost.setHeader(new BasicHeader("content-type", "application/json"));
            String execute = httpClient.execute(httpPost, new BasicHttpClientResponseHandler());
            order = OBJECT_MAPPER.reader().readValue(execute, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return order;
    }

    private static HashMap getHappyPathInput() throws IOException {
        return new ObjectMapper().readValue("""
                {
                  "order" : {
                    "car" : {
                      "make": "Blista",
                      "model": "Compact"
                    },
                    "customer" :{
                      "id" : "12345"
                    }
                  }
                }
                """, HashMap.class);
    }

    private static HashMap getUnHappyPathInput() throws IOException {
        return new ObjectMapper().readValue("""
                {
                  "order" : {
                    "car" : {
                      "make": "Sentinel",
                      "model": "someModel"
                    },
                    "customer" :{
                      "id" : "12345"
                    }
                  }
                }
                """, HashMap.class);
    }





}
