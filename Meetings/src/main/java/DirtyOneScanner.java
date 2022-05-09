import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import SIP.AbstractPacker;
import html2pdf.AbstractConvert;
import linkCrawl.AbstractsCrawl;
import linkCrawl.LinkCrawl;
import linkDownload.AbstractDownload;
import utilities.Clean;
import utilities.Database;
import utilities.SqlManager;
import utilities.Utilities;

public class DirtyOneScanner {
	
	private static void cleanBeginning(String jahr) throws Exception {
		System.out.println("Clean");
		Clean.cleanDirs();
		Clean.cleanTables();
		System.out.println("LinkCrawl " + LocalDateTime.now());
		LinkCrawl.processBothLanguages();
		System.out.println("syncToYear " + LocalDateTime.now());
		String csvFilePath = "/home/wutschka/workspace/Kongress_HT_".concat(jahr).concat(".csv");
		
		Database.syncToYear(csvFilePath);
//		Database.printDatabaseWithStatus("ueberordnungen", 10, "");

//		SqlManager.INSTANCE.executeUpdate("DELETE FROM ueberordnungen WHERE ID!='gmds2019';");

		System.out.println("AbstractsCrawl " + LocalDateTime.now());
		AbstractsCrawl.crawl();
//		SqlManager.INSTANCE.executeUpdate("DELETE FROM ueberordnungen WHERE ID!='XXX';");
//		SqlManager.INSTANCE.executeUpdate("DELETE FROM abstracts WHERE Ab_ID!='19gmds210';");
//		SqlManager.INSTANCE.executeUpdate("DELETE FROM abstracts WHERE Ab_ID!='XXX';");
	}

	private static void skipCleanAbstracts() throws SQLException {
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=10;");
		int abstractCount = 0;
		while (resultSet.next()) {
			++abstractCount;
			String Ue_ID = resultSet.getString("Ue_ID");
			String Ab_ID = resultSet.getString("Ab_ID");
			String URL = resultSet.getString("URL");
			String LANG = resultSet.getString("LANG");
			Document doc = Utilities.getWebsite(URL);
			Element content = doc.child(0);
			Elements figureLinks = content.getElementsByClass("link-figure");
			int figureCount = 0;
			boolean allesOk = true;
			for (Element figureLink : figureLinks) {
				++figureCount;
				String href = "https://www.egms.de".concat(figureLink.attr("href"));
				Document docFigure = Utilities.getWebsite(href);
				Element figure = docFigure.getElementsByTag("img").first();
				if (figure == null) {
					System.err.println("Keine Figure beim Abstract " + Ab_ID);
					allesOk = false;
					break;
				}
				String figureSrc = figure.attr("src");
				String expection = "/static/figures/meetings/".concat(Ue_ID).concat("/").concat(Ab_ID).concat(".f").concat(Integer.toString(figureCount)).concat(".png");
				if (!figureSrc.contentEquals(expection)) {
					System.err.println("Ist : ".concat(figureSrc));
					System.err.println("Soll: ".concat(expection));
					allesOk = false;
					break;
				}
			}
			Elements tableLinks = content.getElementsByClass("link-table");
			int tableCount = 0;
			for (Element tableLink : tableLinks) {
				++tableCount;
				String href = "https://www.egms.de".concat(tableLink.attr("href"));
				Document docTable = Utilities.getWebsite(href);
				Element table = docTable.getElementsByTag("img").first();
				if (table == null) {
					System.err.println("Keine Table beim Abstract " + Ab_ID);
					allesOk = false;
					break;
				}
				String tableSrc = table.attr("src");
				String expection = "/static/figures/meetings/".concat(Ue_ID).concat("/").concat(Ab_ID).concat(".t").concat(Integer.toString(tableCount)).concat(".png");
				if (!tableSrc.contentEquals(expection)) {
					System.err.println("Ist : ".concat(tableSrc));
					System.err.println("Soll: ".concat(expection));
					allesOk = false;
					break;
				}
			}
			if (allesOk) {
				SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 9 WHERE Ab_ID = '".concat(Ab_ID).concat("' AND LANG = '").concat(LANG).concat("';"));
			} else {
				System.err.println("Das war #".concat(Integer.toString(abstractCount)).concat(": ").concat(URL));
			}
		}
	}
	
	private static void checkBothLanguages() throws SQLException {
		ResultSet resultSetDe = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=10 AND LANG='de';");
		List<String> de_IDs = new ArrayList<String>();
		while (resultSetDe.next()) {
			de_IDs.add(resultSetDe.getString("Ab_ID"));
		}
		ResultSet resultSetEn = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=10 AND LANG='en';");
		List<String> en_IDs = new ArrayList<String>();
		while (resultSetEn.next()) {
			en_IDs.add(resultSetEn.getString("Ab_ID"));
		}
		for (String Ab_ID : de_IDs) {
			if (!en_IDs.contains(Ab_ID)) {
				System.err.println("Abstract auf Englisch Fehlerhaft, aber nicht auf Deutsch: ".concat(Ab_ID));
			}
		}
		for (String Ab_ID : en_IDs) {
			if (!de_IDs.contains(Ab_ID)) {
				System.err.println("Abstract auf Deutsch Fehlerhaft, aber nicht auf Englisch: ".concat(Ab_ID));
			}
		}
	}
	
	public static void printAbstractKuerzel() throws SQLException {
		ResultSet resultSetDe = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=10 AND LANG='de';");
		int abstractCount = 0;
		while (resultSetDe.next()) {
			++abstractCount;
			System.out.println(Integer.toString(abstractCount).concat(") ").concat(resultSetDe.getString("Ab_ID")));
		}
	}
	
	private static void repeat() throws SQLException {
		Clean.cleanDirs();
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 10 WHERE status > 9;");
	}

	public static void main(String[] args) throws Exception {
		System.out.println("DirtyOneScanner.main Anfang");
//		cleanBeginning("2017_auszug");
//		cleanBeginning("2020");
		repeat();
		
		System.out.println("Skip Clean Abstracts");
		skipCleanAbstracts();
		
		System.out.println("Prüfe ob de und en ist gleich");
		checkBothLanguages();
		
		printAbstractKuerzel();
		
		System.out.println("AbstractDownload " + LocalDateTime.now());
		AbstractDownload.abstractDownload();
		System.out.println("AbstractConvert " + LocalDateTime.now());
		AbstractConvert.abstractConvert();
		System.out.println("Abstracts packen " + LocalDateTime.now());
		AbstractPacker.databaseWorker();
		System.out.println("DirtyOneScanner.main Ende " + LocalDateTime.now());
	}
}
