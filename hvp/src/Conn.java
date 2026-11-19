import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class Conn {

    private static final String URL =
            "jdbc:mysql://localhost:3306/hvp_gatagara"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC"
            + "&connectTimeout=10000"
            + "&socketTimeout=30000";

    private static final String USER = "root";
    private static final String PASSWORD = "zxc90#";


    private Conn() {
    }


    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException error) {
            throw new SQLException(
                    "MySQL JDBC driver was not found. "
                    + "Add mysql-connector-j to the project libraries.",
                    error
            );
        }

        Properties properties = new Properties();
        properties.setProperty("user", USER);
        properties.setProperty("password", PASSWORD);

        Connection connection =
                DriverManager.getConnection(URL, properties);

        if (connection == null || connection.isClosed()) {
            throw new SQLException(
                    "Could not establish a database connection."
            );
        }

        return connection;
    }
}