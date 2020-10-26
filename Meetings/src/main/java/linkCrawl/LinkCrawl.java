package linkCrawl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
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

	public static void linkCrawl(URL landingPage, String mainPath)
			throws IOException, SQLException {
		// lade die Webseite herrunter
		MyWget myWget = new MyWget(landingPage, mainPath + "landingPage" + fs, false);
		@SuppressWarnings("unused")
		int res = myWget.getPage();
		// myWget.explainResult();

		File htmlFile = new File(mainPath + "landingPage" + fs + "content" + fs + "index.htm");
		Document doc = Jsoup.parse(htmlFile, "ISO-8859-1", landingPage.getProtocol() + landingPage.getHost());
		Element content = doc.getElementById("content");
		List<Kongress> listNew = new ArrayList<Kongress>();
		// F�ge die URLS in eine Liste ein
		for (int i = 2; i < content.getElementsByTag("a").size(); i++) {
			String linRef = content.getElementsByTag("a").get(i).attr("href");
			if (linRef != null && !linRef.isEmpty()) {
				listNew.add(new Kongress(linRef));
			}
		}

		// �berpr�fe ob die kurzIDs ok sind
		for (Kongress it : listNew) {
			for (Kongress it2 : listNew) {
				if ((it.kurzID.equals(it2.kurzID)) && (it != it2)) {// Ich gehe stark davon aus, dass jede kurzId nur
																	// ein mal in der Liste gefunden wird.
					System.err.println("Problem mit url " + it.url + " und " + it2.url);
				}
			}
		}

		ResultSet resultSet = null;

		int Anzahl = 1;
		for (Kongress it : listNew) {
			resultSet = SqlManager.INSTANCE
					.executeSql("SELECT * FROM ueberordnungen WHERE ID = '" + it.kurzID + "_" + it.language + "'");
			// Pr�fe, ob sich bereits ein solcher Eintrag in der Datenbank befindet
			if (resultSet.next()) {
				// War schon drin
			} else {
				// F�ge ein
				System.out.println("Verarbeite: '" + it.kurzID + "', '" + it.url + "'");
				SqlManager.INSTANCE.executeUpdate("INSERT INTO ueberordnungen (ID, URL, Status) VALUES ('" + it.kurzID
						+ "_" + it.language + "', '" + it.url + "', 10);");
				if (0 == --Anzahl)
					break; // tu nicht zu viel
			}
		}
	}

	public static void main(String[] args) throws IOException, SQLException {
		String mainPath = "C:\\Users\\hixel\\workspace\\Meetings\\Ueberordnungen\\";

		linkCrawl(crawlURL("/static/de/meetings/index.htm"), mainPath);
		linkCrawl(crawlURL("/static/en/meetings/index.htm"), mainPath);

		System.out.println("LinkCrawl Ende.");
	}
	
	private static URL crawlURL(final String path) throws MalformedURLException {
		String protokoll = "https://";
		String hostname = "www.egms.de";
		return new URL(protokoll.concat(hostname).concat(path));
	}
}