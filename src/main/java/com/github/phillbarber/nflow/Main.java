package com.github.phillbarber.nflow;

import io.nflow.jetty.JettyServerContainer;
import io.nflow.jetty.StartNflow;

public class Main {

    private static JettyServerContainer local;
    public static void main(String[] args) throws Exception {
        start();
    }

    public static void start() throws Exception {
        StartNflow startNflow = new StartNflow();
        startNflow.registerSpringContext(CreditApplicationWorkflow.class, OrderResource.class);
        local = startNflow.startJetty(7500, "local", "");
    }

    public static void stop() throws Exception {
        local.stop();
    }

}