package com.github.phillbarber.nflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@WireMockTest
public class EndToEndTest {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String LOCAL_NFLOW_ADDRESS = "http://localhost:7500";
    public static final String NFLOW_API_WORKFLOW_INSTANCE = LOCAL_NFLOW_ADDRESS + "/nflow/api/v1/workflow-instance";
    public static final String NFLOW_API_WORKFLOW_INSTANCE_WITH_ID = NFLOW_API_WORKFLOW_INSTANCE + "/id";


    private final HttpClient httpClient = HttpClientBuilder.create().build();

    private StubServices stubServices = new StubServices();


    @BeforeAll
    public static void start(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        Main.start(wmRuntimeInfo.getHttpBaseUrl());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        Main.stop();
    }

    @Test
    public void happyPathOrder(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        stubServices.orderServiceReturnsValidOrderFor("Blista");
        stubServices.saveOrderReturnsOK();
        stubServices.priceServiceReturnsPrice();
        stubServices.customerServiceReturnsCustomerFor("12345");
        stubServices.discountServiceReturns();

        Map response =  submitOrderToRestFacade(getHappyPathInput());

        Map workflowById = getWorkflowById((Integer) response.get("id"));

        assertNotNull(response.get("id"));
        assertNotNull(response.get("customerId"));
        assertNotNull(response.get("customerName"));
        assertNotNull(response.get("customerLoyaltyPoints"));
        assertEquals(response.get("basePrice"), 60000);
        assertEquals(response.get("totalPrice"), 54000);
        assertEquals(response.get("currency"), "GBP");
        assertEquals(response.get("promotionCode"), "ABCDE1234");
        assertEquals(response.get("discount"), 0.1);
    }

    private Map submitOrderToRestFacade(HashMap happyPathInput) {

        Map order;
        HttpPut httpPut = new HttpPut(NFLOW_API_WORKFLOW_INSTANCE);
        try {
            httpPut.setEntity(new StringEntity(OBJECT_MAPPER.writer().writeValueAsString(happyPathInput)));
            httpPut.setHeader(new BasicHeader("content-type", "application/json"));
            order = httpClient.execute(httpPut, response -> {
                HttpEntity responseBody = response.getEntity();
                return OBJECT_MAPPER.reader().readValue(responseBody.getContent(), Map.class);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return order;
    }


    private Map getWorkflowById(Integer id){
        Map workflow;


        try {
            URI uri = new URIBuilder(NFLOW_API_WORKFLOW_INSTANCE_WITH_ID + "/" + id)
                    .addParameter("includes", "currentStateVariables").build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader(new BasicHeader("accept", "application/json"));
            workflow = httpClient.execute(httpGet, response -> {
                HttpEntity responseBody = response.getEntity();
                return OBJECT_MAPPER.reader().readValue(responseBody.getContent(), Map.class);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return workflow;
    }

    private static HashMap getHappyPathInput() throws IOException {
        return new ObjectMapper().readValue("""
                {
                  "type": "carOrderWorkflow",
                  "businessKey": "123",
                  "stateVariables": {
                    "requestData": {
                        "customerId": "12345",
                        "amount": 123
                    }
                  }
                }
                """, HashMap.class);

//        return new ObjectMapper().readValue("""
//                {
//                  "order" : {
//                    "car" : {
//                      "make": "Blista",
//                      "model": "Compact"
//                    },
//                    "customer" :{
//                      "id" : "12345"
//                    }
//                  }
//                }
//                """, HashMap.class);
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
