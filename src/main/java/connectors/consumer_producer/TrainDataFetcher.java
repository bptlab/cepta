package connectors.consumer_producer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class TrainDataFetcher {
    private static String TABLE_NAME;
    private static int last_id = 0;
    private static Connection connection;

    public static void main(String[] args) throws SQLException {
        TrainDataFetcher.connect();
        PreparedStatement getNextEntry = connection.prepareStatement("SELECT *" +
                "FROM ? " +
                "WHERE id > ? " +
                "ORDER BY id ASCENDING" +
                "FIRST 1 ROWS ONLY;");
        getNextEntry.setString(1, TABLE_NAME);
        getNextEntry.setInt(2, last_id);
        last_id++;
    }

    private static void connect() throws SQLException {
        String url = DatabaseConstants.CONNECTOR + ":" + DatabaseConstants.DATABASE_SYSTEM + "://"
                + DatabaseConstants.SERVER + ":"
                + DatabaseConstants.PORT + "/"
                + DatabaseConstants.DATABASE_NAME;

        try {
            connection = DriverManager.getConnection(url, DatabaseConstants.USER, DatabaseConstants.PASSWORD);
        } catch (SQLException e) {
            System.out.println("SQLException: Connection could not be established.");
            return;
        }
    }
}
