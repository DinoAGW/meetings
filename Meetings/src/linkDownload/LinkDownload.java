package linkDownload;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import myWget.MyWget;
import utilities.Kongress;
import utilities.SqlManager;
import utilities.Utilities;

public class LinkDownload {
	static String fs = System.getProperty("file.separator");

	public static void main(String[] args) throws IOException, SQLException {
		String mainPath = "C:\\Users\\hixel\\workspace\\Meetings\\Ueberordnungen\\";

		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";
		String password = Utilities.readStringFromProperty(propertypfad, "password");
		SqlManager sqlManager = new SqlManager("jdbc:mariadb://localhost/meetings", "root", password);
		ResultSet resultSet = null;

		resultSet = sqlManager.executeSql("SELECT * FROM urls WHERE status=10");

		int i = 0;
		List<Kongress> urlList = new ArrayList<Kongress>();
		while (resultSet.next()) {
			if (i++ == 3)
				break; // tu nicht zu viel
			System.out.println(resultSet.getString("ID") + ", " + resultSet.getString("URL"));
			urlList.add(new Kongress(resultSet.getString("URL")));
		}

		for (Kongress it : urlList) {
			String kongressDir = mainPath + "kongresse/" + it.kurzID + "/";
			MyWget myWget = new MyWget(it.url, kongressDir, true);
			int res = myWget.getPage();
		}
	}
}
