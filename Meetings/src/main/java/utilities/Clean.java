package utilities;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class Clean {

	public static void clean() {
		//Ordner leeren
		Utilities.deleteDir(Drive.landPath);
		Utilities.deleteDir(Drive.captPath);
		Utilities.deleteDir(Drive.absPath);
		//		Utilities.deleteDir(Drive.sipPath);

		//Tabellen erstellen
		try {
			Statement stmt = SqlManager.INSTANCE.getConnection().createStatement();

			stmt.executeUpdate("DROP TABLE IF EXISTS ueberordnungen;");
			stmt.executeUpdate(
					"CREATE TABLE ueberordnungen ( ID VARCHAR(20), URL VARCHAR (200), LANG VARCHAR(5), Status INT );");
			stmt.executeUpdate("DROP TABLE IF EXISTS abstracts;");
			stmt.executeUpdate(
					"CREATE TABLE abstracts ( Ue_ID VARCHAR(20), Ab_ID VARCHAR(20), URL VARCHAR (200), LANG VARCHAR(5), Status INT );");
			Database.createMetadataDatabase();
			HtKuerzelDatenbank.createHtKuerzelDatenbank();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
//		clean();
		try {
			Statement stmt = SqlManager.INSTANCE.getConnection().createStatement();

			stmt.executeUpdate("DROP TABLE IF EXISTS sonderfaelle;");
			stmt.executeUpdate(
					"CREATE TABLE sonderfaelle ( Ue_ID VARCHAR(20), Ab_ID VARCHAR(20), URL VARCHAR (200), LANG VARCHAR(5), Status INT );");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Clean Ende.");
	}

}
