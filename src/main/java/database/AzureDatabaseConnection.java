package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AzureDatabaseConnection {

    private static final String SERVER_NAME =
            "eitasm-srv-vanier.database.windows.net";

    private static final String DATABASE_NAME =
            "EITASM_DB";

    private static final String USERNAME =
            System.getenv("EITASM_DB_USER");

    private static final String PASSWORD =
            System.getenv("EITASM_DB_PASSWORD");

    private static final String CONNECTION_URL =
            "jdbc:sqlserver://" + SERVER_NAME + ":1433;" +
            "databaseName=" + DATABASE_NAME + ";" +
            "encrypt=true;" +
            "trustServerCertificate=false;" +
            "hostNameInCertificate=*.database.windows.net;" +
            "loginTimeout=30;";

    public static Connection getConnection() throws SQLException {

        if (USERNAME == null || PASSWORD == null) {
            throw new SQLException(
                    "Database username/password environment variables are not configured."
            );
        }

        return DriverManager.getConnection(
                CONNECTION_URL,
                USERNAME,
                PASSWORD
        );
    }
}