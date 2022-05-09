package utilities;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import metadata.UeberordnungMetadataParser;

import java.io.File;
import java.sql.ResultSet;

public class Database {

	public static void printDatabaseWithStatus(String database, int status, String nachwort) throws SQLException {
		ResultSet resultSet = getDatabaseWithStatus(database, status);
		int count = 0;
		while (resultSet.next()) {
			if (database.contentEquals("ueberordnungen")) {
				String ID = resultSet.getString("ID");
				String URL = resultSet.getString("URL");
				String LANG = resultSet.getString("LANG");
				String message = "Eintrag ID='".concat(ID).concat("', URL='").concat(URL).concat("', LANG ='").concat(LANG)
						.concat("'");
				if (nachwort != null && nachwort != "") {
					message = message.concat(" ").concat(nachwort);
				}
				System.out.println(message);
			} else if (database.contentEquals("abstracts")) {
				++count;
				String Ue_ID = resultSet.getString("Ue_ID");
				String Ab_ID = resultSet.getString("Ab_ID");
				String URL = resultSet.getString("URL");
				String LANG = resultSet.getString("LANG");
				int Status = resultSet.getInt("Status");
				String message = Integer.toString(count) + " | Ue_ID='" + Ue_ID + "' | Ab_ID='" + Ab_ID + "' | LANG='" + LANG + "' | Status=" + Status + " | " + nachwort;
				System.out.println(message);
			} else if (database.contentEquals("sonderfaelle")) {
				++count;
				String Ue_ID = resultSet.getString("Ue_ID");
				String Ab_ID = resultSet.getString("Ab_ID");
				String URL = resultSet.getString("URL");
				String LANG = resultSet.getString("LANG");
				int Status = resultSet.getInt("Status");
				String message = Integer.toString(count) + " | Ue_ID='" + Ue_ID + "' | Ab_ID='" + Ab_ID + "' | LANG='" + LANG + "' | Status=" + Status + " | " + nachwort;
				System.out.println(message);
			} else {
				System.err.println("Tabelle " + database + " nicht implementiert.");
			}
		}
	}

	/*
	 * entnehme der Datenbank <database> alle Einträge mit Status <status>
	 */
	public static ResultSet getDatabaseWithStatus(String database, int status) throws SQLException {
		return SqlManager.INSTANCE.executeQuery(
				"SELECT * FROM ".concat(database).concat(" WHERE status = ").concat(Integer.toString(status)));
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

	public static void printMetadataFor(String column, String equals, String nachwort) throws SQLException {
		ResultSet resultSet = getMetadataFor(column, equals);
		while (resultSet.next()) {
			String HT = resultSet.getString("HT");
			String ID = resultSet.getString("ID");
			String xPathKey = resultSet.getString("xPathKey");
			String value = resultSet.getString("value");
			String message = "Eintrag HT = '".concat(HT).concat("', ID = '").concat(ID).concat("', xPathKey = '")
					.concat(xPathKey).concat("', value = '").concat(value).concat("'");
			if (nachwort != null && nachwort != "") {
				message = message.concat(" ").concat(nachwort);
			}
			System.out.println(message);
		}
	}

	public static void insertIntoMetadataDatabase(String HT, String ID, String xPathKey, String value)
			throws SQLException {
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
	
	/*
	 * Sammelt zu jeder HT Nummer einer csv-Datei
	 * die Kuerzel in eine Liste und setzt dann Status auf 1 für alle Überordnungen im Status 10,
	 * die nicht in dieser Liste enthalten sind
	 */
	public static void syncToYear(String csvFilePath) throws Exception {
		File csvFile = new File(csvFilePath);
		Scanner csvScanner = new Scanner(csvFile);
		List<String> IDs = new ArrayList<String>();
		//Hole alle Metadaten zu den jeweiligen HT Nummern
		while (csvScanner.hasNext()) {
			String HT = csvScanner.next();
			String ID = UeberordnungMetadataParser.okeanos2Database(HT);
			IDs.add(ID);
			//prüfe ob die echten URLS ermittelt werden können
			if (!Database.ueberordnungenDatabaseContainsBothIds(ID)) {
				csvScanner.close();
				System.err
						.println("ID '".concat(ID).concat("' ist nicht in beiden Sprachen in den Überordnungen vertreten"));
				throw new Exception();
			}
		}
		csvScanner.close();
		ResultSet resultSet = Database.getDatabaseWithStatus("ueberordnungen", 10);
		//Lösche Überordnungen die nicht in diesem Jahr vorkommen
		while (resultSet.next()) {
			String ID = resultSet.getString("ID");
			if (!IDs.contains(ID)) {
				//				System.out.println("Alle Einträge mit der ID='".concat(kurzID).concat("' werden wieder gelöscht"));
				Database.updateStatusUeberordnung(ID, 11);
			}
		}
	}
	
	public static void updateStatusUeberordnung(String wert, int status) throws SQLException {
		updateStatus("ueberordnungen", "ID", wert, status);
	}
	
	public static void updateStatusAbstracts(String wert, int status) throws SQLException {
		updateStatus("abstracts", "Ab_ID", wert, status);
	}
	
	private static void updateStatus(String database, String feld, String wert, int status) throws SQLException {
		SqlManager.INSTANCE.executeUpdate("UPDATE ".concat(database).concat(" SET status = ").concat(Integer.toString(status)).concat(" WHERE ").concat(feld).concat(" = '").concat(wert).concat("';"));
	}

}
