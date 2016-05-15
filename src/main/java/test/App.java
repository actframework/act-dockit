package test;

import act.boot.app.RunApp;

public class App {
    public static void main(String[] args) throws Exception {
        RunApp.start("test-dockit", "0.1", App.class);
    }
}
