import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class LinkCrawl {
	static String fs = System.getProperty("file.separator");

	protected static class kongress {
		protected String url;
		protected String kurzID;

		kongress(String url) {
			this.url = new String(url);
			String[] tokens = new String[10];
			tokens = this.url.split("/");
			this.kurzID = tokens[tokens.length - 2];
		}
	}

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

		File checksum = new File(mainPath + "landingPage" + fs + "content" + fs + "checksum.md5");
		if (checksum.exists()) {
			makeDifference(mainPath + "landingPage" + fs + "content" + fs + "checksum.md5");
		}
		// lade die Webseite herrunter
		MyWget myWget = new MyWget(landingPage, mainPath + "landingPage" + fs, false);
		int res = myWget.getPage();
		// myWget.explainResult();

		File htmlFile = new File(mainPath + "landingPage" + fs + "content" + fs + "index.htm");
		Document doc = Jsoup.parse(htmlFile, "ISO-8859-1", protokoll + hostname);
		Element content = doc.getElementById("content");
		List<kongress> listNew = new ArrayList<kongress>();
		//Füge die URLS in eine Liste ein
		for (int i = 2; i < content.getElementsByTag("a").size(); i++) {
			listNew.add(new kongress(content.getElementsByTag("a").get(i).attr("href")));
		}
		
		//überprüfe ob die kurzIDs ok sind
		for (kongress it : listNew) {
			boolean haveProblem = false;
			for (kongress it2 : listNew) {
				if ((it.kurzID.equals(it2.kurzID)) && (it != it2)) {// Ich gehe stark davon aus, dass ein Kongress
					// anhand der kurzId eindeutig bestimmt ist
					System.out.println("Problem mit url " + it.url + " und " + it2.url);
					haveProblem = true;
					break;
				}
			}
			if (haveProblem) {
				break;
			}
		}
		
		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";
		String password = Utilities.readStringFromProperty(propertypfad, "password");
		SqlManager sqlManager = new SqlManager("jdbc:mariadb://localhost/meetings", "root", password);
		ResultSet resultSet = null;

		String insertSQL = null;
		for (kongress it : listNew ) {
			resultSet = sqlManager.executePreparedSql(
				"INSERT IGNORE INTO urls (ID, URL, Status) VALUES (\"" + it.kurzID + "\", \"" + it.url + "\", 10);");
		}

		// resultSet = sqlManager.executeSql("SELECT * FROM url_status WHERE id=0");

		while (resultSet.next()) {
			System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
		}
	}

}
