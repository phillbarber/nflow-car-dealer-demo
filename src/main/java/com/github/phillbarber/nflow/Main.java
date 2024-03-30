package com.github.phillbarber.nflow;

import io.nflow.jetty.StartNflow;

public class Main {
    public static void main(String[] args) throws Exception {

        new StartNflow().startJetty(7500, "local", "");
    }
}