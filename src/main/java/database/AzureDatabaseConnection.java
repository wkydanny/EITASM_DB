package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AzureDatabaseConnection {
    // Replace placeholders with your actual Azure SQL credentials
    private static final String SERVER_NAME = "eitasm-srv-vanier.database.windows.net";
    private static final String DATABASE_NAME = "EITASM_DB";
    private static final String USERNAME = "dbadmin";
    private static final String PASSWORD = "Vanier1234";

    private static final String CONNECTION_URL = String.format(
            "jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;"
                    + "encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
            SERVER_NAME, DATABASE_NAME, USERNAME, PASSWORD
    );

    public static Connection getConnection() throws SQLException {
        try {
            // Force load the Microsoft SQL Server JDBC Driver class
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(CONNECTION_URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Microsoft JDBC Driver not found on classpath.", e);
        }
    }
}