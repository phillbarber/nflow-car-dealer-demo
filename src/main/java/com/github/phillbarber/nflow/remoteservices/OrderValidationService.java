package com.github.phillbarber.nflow.remoteservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.phillbarber.nflow.domain.OrderRequest;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderValidationService {
    private HttpClient httpClient;
    private String rootURI;
    private ObjectMapper objectMapper;

    public OrderValidationService(HttpClient httpClient,
                                  @Qualifier("objectMapper") ObjectMapper objectMapper,
                                  @Qualifier("serviceRootURI") String serviceRootURI) {
        this.httpClient = httpClient;
        this.rootURI = serviceRootURI + "/order-service/api/v1/";
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


}


