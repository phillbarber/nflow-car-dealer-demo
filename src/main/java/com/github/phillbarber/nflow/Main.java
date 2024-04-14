package com.github.phillbarber.nflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.phillbarber.nflow.remoteservices.BasePriceRemoteService;
import io.nflow.engine.workflow.executor.WorkflowLogContextListener;
import io.nflow.jetty.JettyServerContainer;
import io.nflow.jetty.StartNflow;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

public class Main {

    private static JettyServerContainer local;

    private static String remoteServiceBaseURI;

    public static void main(String[] args) throws Exception {
        start("");
    }

    public static void start(String remoteServiceBaseURI) throws Exception {
        Main.remoteServiceBaseURI = remoteServiceBaseURI;
        StartNflow startNflow = new StartNflow();
        //startNflow.registerSpringContext(CreditApplicationWorkflow.class, OrderResource.class, CarOrderWorkflow.class);
        startNflow.registerSpringContext(CarOrderWorkflowConfiguration.class, CarOrderWorkflow.class);
        local = startNflow.startJetty(7500, "local", "");
    }

    public static void stop() throws Exception {
        local.stop();
    }

    @Configuration
    @ComponentScan("com.github.phillbarber.nflow.remoteservices")
    static class CarOrderWorkflowConfiguration {
        @Bean
        public WorkflowLogContextListener logContextListener() {
            return new WorkflowLogContextListener("context");
        }

        @Bean("objectMapper")
        public ObjectMapper getObjectMapper(){
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper;
        }

        @Bean
        public HttpClient getHttpClient(){
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            return httpClient;
        }

        @Bean("serviceRootURI")
        public String getServiceRootURI(){
            return Main.remoteServiceBaseURI;
        }


    }

}