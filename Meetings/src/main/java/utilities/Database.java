package utilities;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import metadata.UeberordnungMetadataParser;

import java.io.File;
import java.io.FileReader;
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
				String message = "Eintrag ID='".concat(ID).concat("', URL='").concat(URL).concat("', LANG ='")
						.concat(LANG).concat("'");
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
				String message = Integer.toString(count) + " | Ue_ID='" + Ue_ID + "' | Ab_ID='" + Ab_ID + "' | LANG='"
						+ LANG + "' | Status=" + Status + " | " + nachwort;
				System.out.println(message);
			} else if (database.contentEquals("sonderfaelle")) {
				++count;
				String Ue_ID = resultSet.getString("Ue_ID");
				String Ab_ID = resultSet.getString("Ab_ID");
				String URL = resultSet.getString("URL");
				String LANG = resultSet.getString("LANG");
				int Status = resultSet.getInt("Status");
				String message = Integer.toString(count) + " | Ue_ID='" + Ue_ID + "' | Ab_ID='" + Ab_ID + "' | LANG='"
						+ LANG + "' | Status=" + Status + " | " + nachwort;
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
		return SqlManager.INSTANCE.executeQuery(
				"SELECT * FROM metadata WHERE ".concat(column).concat(" = '").concat(equals).concat("';"));
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
		String update = "INSERT INTO metadata ( HT, ID, xPathKey, value ) VALUES ('".concat(HT).concat("', '")
				.concat(ID).concat("', '").concat(xPathKey).concat("', '").concat(value).concat("');");
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
	 * Sammelt zu jeder HT Nummer einer csv-Datei die Kuerzel in eine Liste und
	 * setzt dann Status auf 1 für alle Überordnungen im Status 10, die nicht in
	 * dieser Liste enthalten sind
	 */
	public static void syncToYear(String csvFilePath) throws Exception {
		File csvFile = new File(csvFilePath);
		Scanner csvScanner = new Scanner(csvFile);
		List<String> IDs = new ArrayList<String>();
		// Hole alle Metadaten zu den jeweiligen HT Nummern
		while (csvScanner.hasNext()) {
			String HT = csvScanner.next();
			String ID = UeberordnungMetadataParser.okeanos2Database(HT);
			IDs.add(ID);
			// prüfe ob die echten URLS ermittelt werden können
			if (!Database.ueberordnungenDatabaseContainsBothIds(ID)) {
				csvScanner.close();
				System.err.println(
						"ID '".concat(ID).concat("' ist nicht in beiden Sprachen in den Überordnungen vertreten"));
				throw new Exception();
			}
		}
		csvScanner.close();
		ResultSet resultSet = Database.getDatabaseWithStatus("ueberordnungen", 10);
		// Lösche Überordnungen die nicht in diesem Jahr vorkommen
		while (resultSet.next()) {
			String ID = resultSet.getString("ID");
			if (!IDs.contains(ID)) {
				// System.out.println("Alle Einträge mit der ID='".concat(kurzID).concat("'
				// werden wieder gelöscht"));
				updateStatusUeberordnung(ID, 11);
			} else {
				updateStatusUeberordnung(ID, 10);
			}
		}
	}

	public static void findOutOfDate(String bfarmListe, String rosettaListe) throws Exception {
		CSVParser semicolonParser = new CSVParserBuilder().withSeparator(';').build();
		CSVReader readBfarmListe = new CSVReaderBuilder(new FileReader(bfarmListe)).withCSVParser(semicolonParser)
				.withSkipLines(1).build();
		int insg = 0;
		for (String[] bfarmLine : readBfarmListe) {
			String id = bfarmLine[0];
			int dateLast = 0;
			String dlcorr = bfarmLine[2];
			if (!dlcorr.equals("")) {
				int value = Integer.parseInt(dlcorr);
				if (dateLast < value) {
					dateLast = value;
				}
			}
			String dlerr = bfarmLine[3];
			if (!dlerr.equals("")) {
				int value = Integer.parseInt(dlerr);
				if (dateLast < value) {
					dateLast = value;
				}
			}
			if (dateLast == 0) {
				if (updateStatusAbstracts(id, 11) != 1) {
//					System.out.println("Abstract " + id + " ist nicht als HT-Nummer drin");
				}
				continue;
			}
			String uda = "GMSKON_" + id;
			int udb = 0;
			CSVReader readRosettaListe = new CSVReaderBuilder(new FileReader(rosettaListe)).withSkipLines(1).build();
			String[] rosettaLineFound = null;
			for (String[] rosettaLine : readRosettaListe) {
				if (uda.equals(rosettaLine[4])) {
					udb = Integer.parseInt(rosettaLine[5]);
					rosettaLineFound = rosettaLine;
					break;
				}
			}
			if (udb == 0) {
				throw new Exception("IE " + uda + " ist nicht in Rosetta enthalten");
			}
			if (dateLast <= udb) {
				if (updateStatusAbstracts(id, 11) != 1) {
//					System.out.println("Abstract " + id + " ist nicht als HT-Nummer drin");
				}
			} else {
				System.out.println(bfarmLine.length + " " + Arrays.toString(bfarmLine));
				System.out.println(rosettaLineFound.length + " " + Arrays.toString(rosettaLineFound));
				System.out.println("Das ergibt " + dateLast + " > " + udb + " = " + (dateLast > udb));
				ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE Ab_ID = '" + id + "';");
				if (!resultSet.next()) {
					System.out.println("Abstract " + id + " ist nicht als HT-Nummer drin");
				} else {
					System.out.println("Abstract " + id + " ist zu aktualisieren");
				}
				++insg;
			}
		}
		System.out.println("Insgesamt = " + insg);
	}

	public static int updateStatusUeberordnung(String wert, int status) throws SQLException {
		return updateStatus("ueberordnungen", "ID", wert, status);
	}

	public static int updateStatusAbstracts(String wert, int status) throws SQLException {
		return updateStatus("abstracts", "Ab_ID", wert, status);
	}

	private static int updateStatus(String database, String feld, String wert, int status) throws SQLException {
		return SqlManager.INSTANCE
				.executeUpdate("UPDATE ".concat(database).concat(" SET status = ").concat(Integer.toString(status))
						.concat(" WHERE ").concat(feld).concat(" = '").concat(wert).concat("';"));
	}

	public static void printDatabaseOverview(String database) throws Exception {
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM ".concat(database).concat(";"));
		Hashtable<Integer, Integer> tabelle = new Hashtable<Integer, Integer>();
		while (resultSet.next()) {
			int Status = resultSet.getInt("Status");
			if (tabelle.containsKey(Status)) {
				int count = tabelle.get(Status);
				tabelle.replace(Status, count + 1);
			} else {
				tabelle.put(Status, 1);
			}
		}
		while (tabelle.size() > 0) {
			Enumeration<Integer> keys = tabelle.keys();
			int min = keys.nextElement();
			while (keys.hasMoreElements()) {
				int test = keys.nextElement();
				if (test < min) {
					min = test;
				}
			}
			System.out.println("Status " + min + " kommt " + tabelle.get(min) + " mal vor");
			tabelle.remove(min);
		}
	}

	public static void main(String[] args) throws Exception {
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=10;");
		findOutOfDate("/home/wutschka/workspace/Kongresse_GM03_2022-04-18.csv",
				"/home/wutschka/workspace/GMS_Kongresse.csv");
		printDatabaseOverview("abstracts");
	}
}
