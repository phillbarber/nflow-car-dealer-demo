package com.github.phillbarber.nflow.remoteservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.phillbarber.nflow.domain.OrderRequest;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BasePriceRemoteService {
    private HttpClient httpClient;
    private final String serviceRootURI;
    private ObjectMapper objectMapper;

    public BasePriceRemoteService(HttpClient httpClient,
                                  @Qualifier("objectMapper") ObjectMapper objectMapper,
                                  @Qualifier("serviceRootURI") String serviceRootURI) {
        this.httpClient = httpClient;
        this.serviceRootURI = serviceRootURI;
        this.objectMapper = objectMapper;
    }

    public BasePriceResponse getBasePrice(OrderRequest orderRequest) {
        HttpPost httpPost = new HttpPost( serviceRootURI + "/price-service/api/v1/price");
        try {
            httpPost.setEntity(new StringEntity(objectMapper.writer().writeValueAsString(orderRequest)));
            String execute = httpClient.execute(httpPost, new BasicHttpClientResponseHandler());
            return objectMapper.reader().readValue(execute, BasePriceResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


