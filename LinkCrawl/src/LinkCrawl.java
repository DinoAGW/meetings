import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkCrawl {

	public static void main(String[] args) throws IOException, SQLException {
		String url = "http://little-football.de/megaMerge/target.html";

		String fs = System.getProperty("file.separator");
		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";

		String password = Utilities.readStringFromProperty(propertypfad, "password");

		SqlManager sqlManager = new SqlManager("jdbc:mariadb://localhost/meetings", "root", password);
		ResultSet resultSet = sqlManager.executeSql("SELECT * FROM status WHERE id=0");

		while (resultSet.next()) {
			System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
		}
	}

}
