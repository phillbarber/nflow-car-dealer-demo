package com.github.phillbarber.nflow.remoteservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.phillbarber.nflow.domain.Order;
import com.github.phillbarber.nflow.domain.OrderRequest;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

public class OrderRemoteService {
    private HttpClient httpClient;
    private String rootURI;
    private ObjectMapper objectMapper;

    public OrderRemoteService(HttpClient httpClient,
                              @Qualifier("objectMapper") ObjectMapper objectMapper,
                              @Qualifier("serviceRootURI") String serviceRootURI) {
        this.httpClient = httpClient;
        this.rootURI = rootURI + "/order-service/api/v1/";
        this.objectMapper = objectMapper;
    }

    public OrderValidationResponse getValidationResponse(OrderRequest orderRequest) {
        HttpPost httpPost = new HttpPost(rootURI + "checkOrder");
        try {
            httpPost.setEntity(new StringEntity(objectMapper.writer().writeValueAsString(orderRequest)));
            String execute = httpClient.execute(httpPost, new BasicHttpClientResponseHandler());
            return objectMapper.reader().readValue(execute, OrderValidationResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String saveOrder(Order order) {
        HttpPost httpPost = new HttpPost(rootURI + "order");
        try {
            httpPost.setEntity(new StringEntity(objectMapper.writer().writeValueAsString(order)));
            String execute = httpClient.execute(httpPost, new BasicHttpClientResponseHandler());
            return (String) objectMapper.reader().readValue(execute, Map.class).get("id");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


