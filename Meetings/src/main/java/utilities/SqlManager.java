package utilities;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public enum SqlManager {
	INSTANCE;

	private static final String sqlConn = "jdbc:h2:file:".concat(Drive.dbPath);
	private static Connection connection;
	
	static {
		try {
			connection = DriverManager.getConnection(sqlConn);
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to initiate SQL connection", e);
		}
		try {
			INSTANCE.executeUpdate("CREATE TABLE IF NOT EXISTS ueberordnungen (ID VARCHAR(20), URL VARCHAR (200), Status INT );");
			INSTANCE.executeUpdate("CREATE TABLE IF NOT EXISTS abstracts (Ue_ID VARCHAR(20), Ab_ID VARCHAR(20), URL VARCHAR (200), Status INT );");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
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
		return resultSet;
	}

	public int executeUpdate(String sql) throws SQLException {
		Statement statement = SqlManager.connection.createStatement();
		int ret = statement.executeUpdate(sql);
		return ret;
	}
	
	public Connection getConnection() {
		return SqlManager.connection;
	}

	public String getDbFilepath() {
		return Drive.dbPath;
	}
}
