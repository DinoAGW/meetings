import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class LinkCrawl {

	public static void main(String[] args) throws IOException, SQLException {
		String url = "http://little-football.de/megaMerge/target.html";

		String fs = System.getProperty("file.separator");
		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";
		
		String password = Utilities.readStringFromProperty(propertypfad, "password");
		
		ResultSet resultSet = null;
		Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost/meetings", "root", password);
		Statement statement = connection.createStatement();
		String sqlStatement = "SELECT * FROM status WHERE id=0";
		resultSet = statement.executeQuery(sqlStatement);
		
		while (resultSet.next()) {
			System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
		}
	}

}
