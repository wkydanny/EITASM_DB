module AppInJava {
    // Allows your app to talk to SQL Server databases
    requires java.sql;
	requires com.microsoft.sqlserver.jdbc;

    // Allows your app to load the JavaFX graphic user interface
    requires javafx.controls;
    requires javafx.graphics;

    // Grants JavaFX permission to look inside and launch your dashboard
    opens gui to javafx.graphics, javafx.controls;
}