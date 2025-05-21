package utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HtKuerzelDatenbank {
	public static void createHtKuerzelDatenbank() {
		try {
			Statement stmt = SqlManager.INSTANCE.getConnection().createStatement();
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS htkuerzel ( HT VARCHAR(20), Kuerzel VARCHAR(20), UNIQUE(HT), UNIQUE(Kuerzel) );");
		} catch (SQLException e) {
			System.out.println("Table could not been created");
			e.printStackTrace();
		}
	}
	
	public static ResultSet getEntry(String Key, String value) throws SQLException {
		return SqlManager.INSTANCE.executeQuery("SELECT * FROM htkuerzel WHERE ".concat(Key).concat(" = '").concat(value).concat("';"));
	}
	
	public static String ht2kuerzel(String HT) throws Exception {
		ResultSet results = getEntry("HT", HT);
		if (results.next()) {
			return results.getString("Kuerzel");
		} else {
			System.err.println("Kein Kürzel zur HT-Nummer '".concat(HT).concat("' gefunden"));
			throw new Exception();
		}
	}
	
	public static String kuerzel2ht(String Kuerzel) throws Exception {
		ResultSet results = getEntry("Kuerzel", Kuerzel);
		if (results.next()) {
			return results.getString("HT");
		} else {
			System.err.println("Keine HT-Nummer zum Kürzel '".concat(Kuerzel).concat("' gefunden"));
			throw new Exception();
		}
	}
	
	public static boolean checkKeyExists(String Key, String value) throws SQLException {
		ResultSet results = getEntry(Key, value);
		return results.next();
	}
	
	public static boolean checkHtExists(String value) throws SQLException {
		return checkKeyExists("HT", value);
	}
	
	public static boolean checkKuerzelExists(String value) throws SQLException {
		return checkKeyExists("Kuerzel", value);
	}

	public static void insertIntoHtKuerzelDatenbank(String HT, String Kuerzel) throws Exception {
		boolean HtExists = checkHtExists(HT);
		boolean KuerzelExists = checkKuerzelExists(Kuerzel);
		if (HtExists != KuerzelExists) {
			if (HtExists) {
				System.err.println("HT-Nummer '".concat(HT).concat("' ist bereits mit einem anderen Kürzel verknüpft"));
			} else {
				System.err.println("Kürzel '".concat(Kuerzel).concat("' ist bereits mit einer anderen HT-Nummer verknüpft"));
			}
			throw new Exception("HT Nummer: " + HT + " Kürzel: " + Kuerzel);
		}
		if (!HtExists) {
			SqlManager.INSTANCE.executeUpdate("INSERT INTO htkuerzel (HT, Kuerzel) VALUES ('".concat(HT).concat("', '").concat(Kuerzel).concat("');"));
		}
	}
	
	public static void printHTnummer(String kuerzel) throws Exception{
		ResultSet resultSet = getEntry("Kuerzel", kuerzel);
		while (resultSet.next()) {
			String HT = resultSet.getString("HT");
			System.out.println(HT);
		}
	}
	
	public static int removeEntry(String Key, String value ) throws Exception {
		return SqlManager.INSTANCE.executeUpdate("DELETE FROM htkuerzel WHERE ".concat(Key).concat(" = '").concat(value).concat("';"));
	}
	
	public static void main(String[] args) throws Exception {
//		printHTnummer("wdhno2022");
//		System.out.println("Gelöscht = " + removeEntry("HT", "HT021591441"));
	}
}
