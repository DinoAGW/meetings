package utilities;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class Database {

	public static void printDatabaseWithStatus(String database, int status, String nachwort) throws SQLException {
		ResultSet resultSet = getDatabaseWithStatus(database, status);
		while (resultSet.next()) {
			String ID = resultSet.getString("ID");
			String URL = resultSet.getString("URL");
			String LANG = resultSet.getString("LANG");
			String message = "Eintrag ID='".concat(ID).concat("', URL='").concat(URL).concat("', LANG ='").concat(LANG)
					.concat("'");
			if (nachwort != null && nachwort != "") {
				message = message.concat(" ").concat(nachwort);
			}
			System.out.println(message);
		}
	}

	public static ResultSet getDatabaseWithStatus(String database, int status) throws SQLException {
		return SqlManager.INSTANCE
				.executeQuery("SELECT * FROM ".concat(database).concat(" WHERE status = ").concat(Integer.toString(status)));
	}

	public static ResultSet getMetadataFor(String column, String equals) throws SQLException {
		return SqlManager.INSTANCE
				.executeQuery("SELECT * FROM metadata WHERE ".concat(column).concat(" = '").concat(equals).concat("';"));
	}

	public static void createMetadataDatabase() {
		try {
			Statement stmt = SqlManager.INSTANCE.getConnection().createStatement();
			stmt.executeUpdate("DROP TABLE IF EXISTS metadata;");
			stmt.executeUpdate(
					"CREATE TABLE metadata ( HT VARCHAR(20), ID VARCHAR(20), xPathKey VARCHAR(40), value VARCHAR(2000) );");
		} catch (SQLException e) {
			System.out.println("Table could not been recreated");
			e.printStackTrace();
		}
	}

	public static void insertIntoMetadataDatabase(String HT, String ID, String xPathKey, String value) throws SQLException {
		Statement stmt;
		stmt = SqlManager.INSTANCE.getConnection().createStatement();
		String update = "INSERT INTO metadata ( HT, ID, xPathKey, value ) VALUES ('".concat(HT).concat("', '").concat(ID)
				.concat("', '").concat(xPathKey).concat("', '").concat(value).concat("');");
		stmt.executeUpdate(update);
	}

	public static boolean ueberordnungenDatabaseContainsBothIds(String IDwithoutLangSpec) throws SQLException {
		boolean ret = true;
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery(
				"SELECT * FROM ueberordnungen WHERE ID = '".concat(IDwithoutLangSpec).concat("' AND LANG = 'de'"));
		if (!resultSet.next()) {
			ret = false;
		}
		resultSet = SqlManager.INSTANCE.executeQuery(
				"SELECT * FROM ueberordnungen WHERE ID = '".concat(IDwithoutLangSpec).concat("' AND LANG = 'en'"));
		if (!resultSet.next()) {
			ret = false;
		}
		return ret;
	}

}
