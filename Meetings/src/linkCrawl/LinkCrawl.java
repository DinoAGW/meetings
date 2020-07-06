package linkCrawl;

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

import myWget.MyWget;
import utilities.Kongress;
import utilities.SqlManager;
import utilities.Utilities;

public class LinkCrawl {
	static String fs = System.getProperty("file.separator");

	@SuppressWarnings("unused")
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
			// makeDifference(mainPath + "landingPage" + fs + "content" + fs +
			// "checksum.md5");
		}
		// lade die Webseite herrunter
		MyWget myWget = new MyWget(landingPage, mainPath + "landingPage" + fs, false);
		@SuppressWarnings("unused")
		int res = myWget.getPage();
		// myWget.explainResult();

		File htmlFile = new File(mainPath + "landingPage" + fs + "content" + fs + "index.htm");
		Document doc = Jsoup.parse(htmlFile, "ISO-8859-1", protokoll + hostname);
		Element content = doc.getElementById("content");
		List<Kongress> listNew = new ArrayList<Kongress>();
		// Füge die URLS in eine Liste ein
		for (int i = 2; i < content.getElementsByTag("a").size(); i++) {
			listNew.add(new Kongress(content.getElementsByTag("a").get(i).attr("href")));
		}

		// überprüfe ob die kurzIDs ok sind
		for (Kongress it : listNew) {
			for (Kongress it2 : listNew) {
				if ((it.kurzID.equals(it2.kurzID)) && (it != it2)) {// Ich gehe stark davon aus, dass ein Kongress
					// anhand der kurzId eindeutig bestimmt ist
					System.err.println("Problem mit url " + it.url + " und " + it2.url);
				}
			}
		}

		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";
		String password = Utilities.readStringFromProperty(propertypfad, "password");
		SqlManager sqlManager = new SqlManager("jdbc:mariadb://localhost/meetings", "root", password);
		ResultSet resultSet = null;

		for (Kongress it : listNew) {
			resultSet = sqlManager.executeSql("SELECT * FROM urls WHERE ID = '" + it.kurzID + "'");
			//Prüfe, ob sich bereits ein solcher Eintrag in der Datenbank befindet
			if (resultSet.next()) {
				// War schon drin
			} else {
				// Füge ein
				System.out.println("Verarbeite: '" + it.kurzID + "', '" + it.url + "'");
				resultSet = sqlManager.executeSql("INSERT INTO urls (ID, URL, Status) VALUES (\"" + it.kurzID
						+ "\", \"" + it.url + "\", 10);");
				break; // tu nicht zu viel
			}
		}

		System.out.println("LinkCrawl Ende.");
	}
}