package linkCrawl;

import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utilities.Abstract;
import utilities.Database;
import utilities.SqlManager;
import utilities.Utilities;

public class AbstractsCrawl {

	public static void crawl() throws Exception {
		ResultSet resultSet = Database.getDatabaseWithStatus("ueberordnungen", 10);
		while (resultSet.next()) {
			String ID = resultSet.getString("ID");
			String URL = resultSet.getString("URL");
			String LANG = resultSet.getString("LANG");
			// For each overview record
			System.out
					.println("Verarbeite: '".concat(ID).concat("', '").concat(URL).concat("', '").concat(LANG).concat("'"));
			Document doc = Utilities.getWebsite(URL);
			Element content = doc.child(0);
			Elements sessionlist = content.getElementsByClass("sessionlist").first().getElementsByTag("a");
			for (Element session : sessionlist) {
				String SessionURL = "https://www.egms.de".concat(session.attr("href"));
				boolean done = false;
				Document SessionDoc = null;
				while(!done) {
					try {
						SessionDoc = Utilities.getWebsite(SessionURL);
						done = true;
					} catch (Exception e) {
						System.err.println("Fehler bei Abfragen der Webseite.");
						Thread.sleep(1000);
					}
				}
				
				Elements abstractlist = SessionDoc.child(0).getElementsByClass("hx_link");
				for (Element abstractElement : abstractlist) {
					Abstract aAbstract = new Abstract("https://www.egms.de".concat(abstractElement.attr("href")));
					ResultSet resultSet2 = SqlManager.INSTANCE.executeQuery(
							"SELECT * FROM abstracts WHERE Ab_ID = '" + aAbstract.Ab_ID + "' AND LANG = '" + LANG + "'");
					// Check if the abstract was in the Database before
					// Prüfe, ob sich bereits ein solcher Eintrag in der Datenbank befindet
					if (resultSet2.next()) {
						// skip if yes
						System.err.println("Sollte nicht vorkommen");
						throw new Exception();
					} else {
						// otherwise insert it
						SqlManager.INSTANCE.executeUpdate("INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('"
								+ aAbstract.Ue_ID + "', '" + aAbstract.Ab_ID + "', '" + aAbstract.url + "', '" + LANG + "', 10);");
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		crawl();
	}
}
