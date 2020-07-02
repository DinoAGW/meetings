import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

	public ResultSet executePreparedSql(String sql) throws SQLException {
		ResultSet resultSet = null;
		PreparedStatement prepsInsertProduct = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		prepsInsertProduct.execute();
		// Retrieve the generated key from the insert.
		resultSet = prepsInsertProduct.getGeneratedKeys();

		// Print the ID of the inserted row.
		while (resultSet.next()) {
			System.out.println("Generated: " + resultSet.getString(1));
		}
		return resultSet;
	}

}
