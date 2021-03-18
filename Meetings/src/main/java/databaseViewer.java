import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import utilities.SqlManager;
import utilities.Utilities;

public class databaseViewer {

	public static void main(String[] args) {
		String URL = "https://www.egms.de/static/de/meetings/index.htm";
		Document doc = Utilities.getWebsite(URL);
		Element content = doc.getElementById("content");
		int first = 0, last = 0;
		for (int i = 2; i < content.getElementsByTag("a").size(); i++) {
			URL = content.getElementsByTag("a").get(i).attr("href");
			if (URL != null && !URL.isEmpty()) {
				URL = "https://www.egms.de".concat(URL);
				ResultSet metadata = null;
				try {
					Statement stmt = SqlManager.INSTANCE.getConnection().createStatement();
					int temp = URL.lastIndexOf("/");
					String ID = URL.substring(0, temp);
					temp = ID.lastIndexOf("/");
					ID = ID.substring(temp+1);
					metadata = stmt.executeQuery("SELECT * FROM metadata WHERE ID='".concat(URL).concat("';"));
					if (metadata.next()) {
						if (first == 0) first = i;
						last = i;
					}
				} catch (SQLException e) {
					System.out.println("Fehler bei der Extraktion der Metadaten.");
					e.printStackTrace();
				}
			}
		}
		int anzahl = 0;
		for (int i = first; i <= last; i++) {
			URL = content.getElementsByTag("a").get(i).attr("href");
			if (URL != null && !URL.isEmpty()) {
				URL = "https://www.egms.de".concat(URL);
				ResultSet metadata = null;
				try {
					Statement stmt = SqlManager.INSTANCE.getConnection().createStatement();
					int temp = URL.lastIndexOf("/");
					String ID  = URL.substring(0, temp);
					temp = ID.lastIndexOf("/");
					ID = ID.substring(temp+1);
					metadata = stmt.executeQuery("SELECT * FROM metadata WHERE ID='".concat(ID).concat("';"));
					if (!metadata.next()) {
						++anzahl;
						System.out.println("Keine Metadaten gefunden zu Kongress #".concat(Integer.toString(i)).concat("'").concat(URL).concat("'"));
					} else {
						metadata = stmt.executeQuery("SELECT * FROM metadata WHERE URL LIKE '%".concat(URL).concat("%' AND value LIKE 'HT%';"));
						metadata.next();
						System.out.println("Kongress #".concat(Integer.toString(i)).concat(" hat die HT Nummer: ").concat(metadata.getString(3)));
					}
				} catch (SQLException e) {
					System.out.println("Fehler bei der Extraktion der Metadaten.");
					e.printStackTrace();
				}
			}
		}
		System.out.println(Integer.toString(anzahl).concat("/").concat(Integer.toString(last-first+1)).concat(" Überordnungen konnten keine Metadaten zugeordnet werden."));
		System.out.println("Von ".concat(Integer.toString(first)).concat(" bis ").concat(Integer.toString(last)));
		URL = "https://www.egms.de/dynamic/de/meetings/eth2014/index.htm";
		ResultSet metadata = null;
		try {
			Statement stmt = SqlManager.INSTANCE.getConnection().createStatement();
			int temp = URL.lastIndexOf("/");
			String ID = URL.substring(0, temp);
			temp = ID.lastIndexOf("/");
			ID = ID.substring(temp+1);
			metadata = stmt.executeQuery("SELECT * FROM metadata WHERE ID='".concat(ID).concat("';"));
			printResults(metadata);
		} catch (SQLException e) {
			System.out.println("Fehler bei der Extraktion der Metadaten.");
			e.printStackTrace();
		}
		anzahl = 0;
		try {
			Statement stmt = SqlManager.INSTANCE.getConnection().createStatement();
			ResultSet resSet = stmt.executeQuery("SELECT * FROM metadata WHERE value LIKE 'HT%';");
			while (resSet.next()) {
				System.out.println("#".concat(Integer.toString(++anzahl)).concat(": ").concat(resSet.getString(3)));
			}
		} catch (SQLException e) {
			System.out.println("Fehler bei der Extraktion der Metadaten.");
			e.printStackTrace();
		}
	}
	
	private static void printResults(ResultSet metadata) {
		try {
			while (metadata.next()) {
				printResult(metadata);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void printResult(ResultSet metadata) {
		try {
			System.out.println("'".concat(metadata.getString(1)).concat("', '").concat(metadata.getString(2)).concat("', '").concat(metadata.getString(3)).concat("'"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
