package utilities;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public enum SqlManager {
	INSTANCE;

	private static final String filePath = Clean.mainPath.concat("database");

	private static final String sqlConn = "jdbc:h2:file:".concat(filePath);
	private static Connection connection;

	static {
		try {
			connection = DriverManager.getConnection(sqlConn);
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to initiate SQL connection", e);
		}
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		ResultSet resultSet = null;
		Statement statement = SqlManager.connection.createStatement();
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
			//System.out.println("Generated: " + resultSet.getString(1));
		}
		return resultSet;
	}

	public int executeUpdate(String sql) throws SQLException {
		Statement statement = SqlManager.connection.createStatement();
		int ret = statement.executeUpdate(sql);
		return ret;
	}
	
	static public Connection getConnection() {
		return SqlManager.connection;
	}

	public static String getDbFilepath() {
		return SqlManager.filePath;
	}
}
