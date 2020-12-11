import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utilities.SqlManager;

public class databaseViewer {

	public static void main(String[] args) {
		String URL = "https://www.egms.de/de/meetings/dgnc2020/";
		ResultSet metadata = null;
		try {
			Statement stmt = SqlManager.getConnection().createStatement();
			metadata = stmt.executeQuery("SELECT * FROM metadata WHERE URL = '".concat(URL).concat("';"));
			while (metadata.next()) {
				System.out.println("'".concat(metadata.getString(1)).concat("', '").concat(metadata.getString(2)).concat("', '").concat(metadata.getString(3)).concat("'"));
			}
		} catch (SQLException e) {
			System.out.println("Fehler bei der Extraktion der Metadaten.");
			e.printStackTrace();
		}
	}

}
