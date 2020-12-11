package utilities;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class Clean {
	static final String fs = System.getProperty("file.separator");
	public static final String mainPath = System.getProperty("user.home").concat(fs).concat(".meetings").concat(fs);

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		//Ordner leeren
		String landPath = mainPath.concat("landingPage").concat(fs);
		Utilities.deleteDir(landPath);

		String captPath = mainPath.concat("Ueberordnungen").concat(fs);
		Utilities.deleteDir(captPath);

		String absPath = mainPath.concat("Abstracts").concat(fs);
		Utilities.deleteDir(absPath);

		//Tabellen erstellen
		try {
			Statement stmt = SqlManager.getConnection().createStatement();

			stmt.executeUpdate("DROP TABLE IF EXISTS ueberordnungen;");
			stmt.executeUpdate("CREATE TABLE ueberordnungen ( ID VARCHAR(20), URL VARCHAR (200), Status INT );");
			stmt.executeUpdate("DROP TABLE IF EXISTS abstracts;");
			stmt.executeUpdate("CREATE TABLE abstracts ( Ue_ID VARCHAR(20), Ab_ID VARCHAR(20), URL VARCHAR (200), Status INT );");
			stmt.executeUpdate("DROP TABLE IF EXISTS metadata;");
			stmt.executeUpdate("CREATE TABLE metadata ( URL VARCHAR(70), xPathKey VARCHAR(40), value VARCHAR(2000) );");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Clean Ende.");
	}

}
