package linkCrawl;

import java.io.File;
import java.io.IOException;
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
import utilities.Database;
import utilities.Drive;
import utilities.Kongress;
import utilities.SqlManager;

public class LinkCrawl {
	static String fs = System.getProperty("file.separator");
	
	private static final int overviewsToGo = -1;

	public static void linkCrawl(URL landingPage, String LANG, String mainPath) throws IOException, SQLException {
		// lade die Webseite herrunter
		MyWget myWget = new MyWget(landingPage, mainPath, false);
		myWget.getPage();

		String htmlFilePath = mainPath.concat("content").concat(fs).concat("index.htm");
		File htmlFile = new File(htmlFilePath);
		Document doc = Jsoup.parse(htmlFile, "ISO-8859-1", landingPage.getProtocol() + landingPage.getHost());
		Element content = doc.getElementById("content");
		List<Kongress> listNew = new ArrayList<Kongress>();
		// Füge die URLS in eine Liste ein
		for (int i = 2; i < content.getElementsByTag("a").size(); i++) {
			String linRef = content.getElementsByTag("a").get(i).attr("href");
			if (linRef != null && !linRef.isEmpty()) {
				listNew.add(new Kongress(linRef));
			}
		}

		// überprüfe ob die kurzIDs ok sind
		for (Kongress it : listNew) {
			for (Kongress it2 : listNew) {
				if ((it.kurzID.equals(it2.kurzID)) && (it != it2)) {// Ich gehe stark davon aus, dass jede kurzId nur
					// ein mal in der Liste gefunden wird.
					System.err.println("Problem mit url " + it.url + " und " + it2.url);
				}
			}
		}

		ResultSet resultSet = null;

		int Anzahl = overviewsToGo;
		for (Kongress it : listNew) {
			resultSet = SqlManager.INSTANCE
					.executeQuery("SELECT * FROM ueberordnungen WHERE ID = '" + it.kurzID + "' AND LANG = '" + LANG + "';");
			// Prüfe, ob sich bereits ein solcher Eintrag in der Datenbank befindet
			if (resultSet.next()) {
				// War schon drin
			} else {
				// Füge ein
//				System.out.println("Verarbeite: '" + it.kurzID + "', '" + it.url + "'");
				SqlManager.INSTANCE.executeUpdate("INSERT INTO ueberordnungen (ID, URL, LANG, Status) VALUES ('" + it.kurzID + "', '"
						+ it.url + "', '" + LANG + "', 10);");
				if (0 == --Anzahl)
					break; // tu nicht zu viel
			}
		}
	}

	public static void processBothLanguages() throws IOException, SQLException {
		String overviewPath = Drive.landPath;

		linkCrawl(crawlURL("/static/de/meetings/index.htm"), "de", overviewPath.concat("de").concat(fs));
		linkCrawl(crawlURL("/static/en/meetings/index.htm"), "en", overviewPath.concat("en").concat(fs));
	}

	public static void main(String[] args) throws IOException, SQLException {
		processBothLanguages();
		System.out.println("LinkCrawl Ende.");
	}

	private static URL crawlURL(final String path) throws MalformedURLException {
		String protokoll = "https://";
		String hostname = "www.egms.de";
		return new URL(protokoll.concat(hostname).concat(path));
	}
	
	public static void printWorkDone() throws SQLException {
		Database.printDatabaseWithStatus("ueberordnungen", 10, "hinzugefügt");
	}
	
}