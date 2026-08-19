module AppInJava {

    // SQL Server
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;

    // JavaFX
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    // JSON
    requires com.google.gson;

    // Built-in Java HTTP server
    requires jdk.httpserver;

    // JavaFX access
    opens gui to javafx.graphics, javafx.controls, javafx.fxml;

    // Allow Gson to work with API data if needed
    opens api to com.google.gson;

    // Export packages used by the application
    exports api;
    exports dao;
    exports database;
    exports gui;
}