import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkDownload {
	static String fs = System.getProperty("file.separator");

	public static void main(String[] args) throws IOException, SQLException {
		String mainPath = "C:\\Users\\hixel\\workspace\\Meetings\\Ueberordnungen\\";

		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";
		String password = Utilities.readStringFromProperty(propertypfad, "password");
		SqlManager sqlManager = new SqlManager("jdbc:mariadb://localhost/meetings", "root", password);
		ResultSet resultSet = null;

		resultSet = sqlManager.executeSql("SELECT * FROM urls WHERE status=10");

		while (resultSet.next()) {
			System.out.println(resultSet.getString(1) + ", " + resultSet.getString(2));
		}
	}
}
