import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlManager {
	private Connection connection;

	public SqlManager(String jdbc, String name, String password) throws SQLException {
		this.connection = DriverManager.getConnection(jdbc, name, password);
	}

	public ResultSet executeSql(String sql) throws SQLException {
		ResultSet resultSet = null;
		Statement statement = this.connection.createStatement();
		resultSet = statement.executeQuery(sql);
		return resultSet;
	}

}
