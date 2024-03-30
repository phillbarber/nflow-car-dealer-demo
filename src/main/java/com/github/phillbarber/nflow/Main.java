package com.github.phillbarber.nflow;

import io.nflow.jetty.StartNflow;

public class Main {
    public static void main(String[] args) throws Exception {

        StartNflow startNflow = new StartNflow();
        startNflow.registerSpringContext(CreditApplicationWorkflow.class);
        startNflow.startJetty(7500, "local", "");
    }
}