package utilities;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class Clean {
	static final String fs = System.getProperty("file.separator");
	public static final String mainPath = System.getProperty("user.home").concat(fs).concat(".meetings").concat(fs);

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		//Ordner leeren
		String captPath = mainPath.concat("Ueberordnungen").concat(fs);
		Utilities.deleteDir(captPath.concat("kongresse"));
		Utilities.deleteDir(captPath.concat("landingPage"));
		
		String absPath = mainPath.concat("Abstracts").concat(fs);
		Utilities.deleteDir(absPath.concat("kongresse"));
		//Utilities.deleteDir(mainPath + "landingPage");

		//urls Tabelle leeren
		// String propertypfad = System.getProperty("user.home") + fs + "properties.txt";
		// String password = Utilities.readStringFromProperty(propertypfad, "password");
		try {
			Statement stmt = SqlManager.getConnection().createStatement();

			stmt.executeUpdate("CREATE TABLE ueberordnungen (ID VARCHAR(20), URL VARCHAR (200), Status INT );");
			stmt.executeUpdate("CREATE TABLE abstracts (Ue_ID VARCHAR(20), Ab_ID VARCHAR(20), URL VARCHAR (200), Status INT );");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Clean Ende.");
	}

}
