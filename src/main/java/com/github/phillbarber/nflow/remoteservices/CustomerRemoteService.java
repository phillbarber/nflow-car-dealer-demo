package com.github.phillbarber.nflow.remoteservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;

public class CustomerRemoteService {
    private HttpClient httpClient;
    private String rootURI;
    private ObjectMapper objectMapper;

    public CustomerRemoteService(HttpClient httpClient,
                                 @Qualifier("objectMapper") ObjectMapper objectMapper,
                                 @Qualifier("serviceRootURI") String serviceRootURI) {
        this.httpClient = httpClient;
        this.rootURI = rootURI;
        this.objectMapper = objectMapper;
    }

    public CustomerResponse getCustomer(String customerId) {
        try {
            String uri = rootURI + "/customer-service/api/v1/customer" + customerId;
            String execute = httpClient.execute(new HttpGet(uri), new BasicHttpClientResponseHandler());
            return objectMapper.reader().readValue(execute, CustomerResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


