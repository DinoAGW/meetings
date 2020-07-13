package utilities;

import java.io.IOException;
import java.sql.SQLException;

public class Clean {
	static String fs = System.getProperty("file.separator");

	public static void main(String[] args) throws IOException, SQLException {
		//Ordner leeren
		String mainPath = "C:\\Users\\hixel\\workspace\\Meetings\\Ueberordnungen\\";
		Utilities.deleteDir(mainPath + "kongresse");
		Utilities.deleteDir(mainPath + "landingPage");

		//urls Tabelle leeren
		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";
		String password = Utilities.readStringFromProperty(propertypfad, "password");
		SqlManager sqlManager = new SqlManager("jdbc:mariadb://localhost/meetings", "root", password);

		sqlManager.executeUpdate("DELETE FROM ueberordnungen;");
		
		System.out.println("Clean Ende.");
	}

}
