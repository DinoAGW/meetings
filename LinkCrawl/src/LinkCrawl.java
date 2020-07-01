import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkCrawl {
	static String fs = System.getProperty("file.separator");

	private static void makeDifference(String fileName) throws IOException {
		FileWriter fileWriter = new FileWriter(fileName, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		PrintWriter printWriter = new PrintWriter(bufferedWriter);
		printWriter.println("X");
		printWriter.close();
	}

	public static void main(String[] args) throws IOException, SQLException {
		String protokoll = "https://";
		String hostname = "www.egms.de";
		String landingPage = protokoll + hostname + "/static/de/meetings/index.htm";

		String mainPath = "C:\\Users\\hixel\\workspace\\Meetings\\Ueberordnungen\\";

		File checksum = new File(mainPath + "landingPage"+fs+"content"+fs+"checksum.md5");
		if (checksum.exists()) {
			makeDifference(mainPath + "landingPage"+fs+"content"+fs+"checksum.md5");
		}
		//lade die Webseite herrunter
		MyWget myWget = new MyWget(landingPage, mainPath + "landingPage"+fs, false);
		int res = myWget.getPage();

		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";

		String password = Utilities.readStringFromProperty(propertypfad, "password");

		SqlManager sqlManager = new SqlManager("jdbc:mariadb://localhost/meetings", "root", password);
		/*
		 * ResultSet resultSet =
		 * sqlManager.executeSql("SELECT * FROM status WHERE id=0");
		 * 
		 * while (resultSet.next()) { System.out.println(resultSet.getString(1) + " " +
		 * resultSet.getString(2)); }
		 */
	}

}
