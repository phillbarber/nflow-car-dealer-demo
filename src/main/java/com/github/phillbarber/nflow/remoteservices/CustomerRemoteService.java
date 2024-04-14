package com.github.phillbarber.nflow.remoteservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.phillbarber.nflow.domain.Customer;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CustomerRemoteService {
    private HttpClient httpClient;
    private String rootURI;
    private ObjectMapper objectMapper;

    public CustomerRemoteService(HttpClient httpClient,
                                 @Qualifier("objectMapper") ObjectMapper objectMapper,
                                 @Qualifier("serviceRootURI") String serviceRootURI) {
        this.httpClient = httpClient;
        this.rootURI = serviceRootURI;
        this.objectMapper = objectMapper;
    }

    public Customer getCustomer(String customerId) {
        try {
            String uri = rootURI + "/customer-service/api/v1/customer" + customerId;
            String execute = httpClient.execute(new HttpGet(uri), new BasicHttpClientResponseHandler());
            return objectMapper.reader().readValue(execute, Customer.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


