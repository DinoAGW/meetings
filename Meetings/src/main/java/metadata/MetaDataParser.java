package metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utilities.SqlManager;
import utilities.Resources;

public class MetaDataParser {
	public static enum returnCode {success, error} 
	
	private static Document getWebsite(String URL) {
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).get();
		} catch (IOException e) {
			System.out.println("Jsoup connect failed");
			e.printStackTrace();
		}
		return doc;
	}
	
	private static void insertIntoMetadata(String ID, String xPathKey, String value) {
		Statement stmt;
		try {
			stmt = SqlManager.getConnection().createStatement();
			String update = "INSERT INTO metadata ( URL, xPathKey, value ) VALUES ('".concat(ID).concat("', '").concat(xPathKey).concat("', '").concat(value).concat("');");
			stmt.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void insertSelectIntoMetadata(Document doc, String URL, String selector, String xPathKey) {
		Elements elems = doc.select(selector);
		for (Element elem : elems) {
			insertIntoMetadata(URL, xPathKey, elem.text());
		}
	}
	
	public static returnCode okeanos2Database(String HT){
		String okeanos = "http://okeanos-z39.hbz-nrw.de:5661/hbz01_sru_ros_dc?version=1.1&operation=searchRetrieve&query=dc.identifier=%20".concat(HT).concat("&startRecord=1&maximumRecords=1");
		Document doc = getWebsite(okeanos);
		if (doc == null) {
			return returnCode.error;
		}
		
		Elements identifierElements = doc.select("identifier");
		String URL = null;
		for (Element identifierElement: identifierElements) {
			if(identifierElement.text().startsWith("http")) {
				URL = identifierElement.text();
			}
		}
		for (Element identifierElement: identifierElements) {
			String xPathKey = identifierElement.text().startsWith("http") ? "dc:identifier@dcterms:URI" : "dc:identifier";
			insertIntoMetadata(URL, xPathKey, identifierElement.text());
		}

		insertSelectIntoMetadata(doc, URL, "title", "dc:title");
		insertSelectIntoMetadata(doc, URL, "creator", "dc:creator");
		insertSelectIntoMetadata(doc, URL, "publisher", "dc:publisher");
		insertSelectIntoMetadata(doc, URL, "date", "dc:date");
		insertIntoMetadata(URL, "dc:type", "conference object");
		insertIntoMetadata(URL, "dc:format", "1 Online-Ressource");

		return returnCode.success;
	}
	
	public static void csv2Database (File csvFile) {
		try {
			Scanner csvScanner = new Scanner(csvFile);
			while(csvScanner.hasNext()) {
				okeanos2Database(csvScanner.next());
			}
			csvScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("csvScanner could not load File");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		try {
			Statement stmt = SqlManager.getConnection().createStatement();
			stmt.executeUpdate("DROP TABLE IF EXISTS metadata;");
			stmt.executeUpdate("CREATE TABLE metadata ( URL VARCHAR(70), xPathKey VARCHAR(40), value VARCHAR(2000) );");
		} catch (SQLException e) {
			System.out.println("Table could not been recreated");
			e.printStackTrace();
		}
		File htcsvFile = Resources.INSTANCE.getHTCSV();
		csv2Database(htcsvFile);
		System.out.println("Main Ende");
	}
}
