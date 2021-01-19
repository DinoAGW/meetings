package metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import okeanos.MetadataDownloader;
import utilities.Database;

public class MetadataParser {

	private static void insertIntoMetadata(String ID, String xPathKey, String value) throws SQLException {
		Database.insertIntoMetadataDatabase(ID, xPathKey, value);
	}

	public static void insertSelectIntoMetadata(Document doc, String ID, String selector, String xPathKey) throws SQLException {
		Elements elems = doc.select(selector);
		for (Element elem : elems) {
			insertIntoMetadata(ID, xPathKey, elem.text());
		}
	}
	
	public static String getURLoutofDoc(Document doc) throws Exception{
		Elements identifierElements = doc.select("identifier");
		String URL = null;
		for (Element identifierElement : identifierElements) {
			if (identifierElement.text().startsWith("http")) {
				if (URL != null) {
					System.err.println("In den Metadaten mehr als einen Identifier mit http am Anfang gefunden");
					throw new Exception();
				}
				URL = identifierElement.text();
			}
		}
		if (URL == null) {
			System.err.println("In den Metadaten keinen identifier mit http am Anfang gefunden");
			throw new Exception();
		} else
			return URL;
	}

	/*
	 * schiebt die benötigten Metadaten zu einer HT Nummer von der okeanos Schnittstelle in die Metadaten Datenbank
	 */
	public static String okeanos2Database(String HT) throws Exception {
		Document doc = MetadataDownloader.getdoc(HT);
		String URL = getURLoutofDoc(doc);

		String[] tokens = new String[10];
		tokens = URL.split("/");
		int tokenOffset = (URL.endsWith("/")) ? 1 : 2;
		String ID = tokens[tokens.length - tokenOffset];
		
		Elements identifierElements = doc.select("identifier");
		
		for (Element identifierElement : identifierElements) {
			String xPathKey = identifierElement.text().startsWith("http") ? "dc:identifier@dcterms:URI" : "dc:identifier";
			insertIntoMetadata(ID, xPathKey, identifierElement.text());
		}

		insertSelectIntoMetadata(doc, ID, "title", "dc:title");
		insertSelectIntoMetadata(doc, ID, "creator", "dc:creator");
		insertSelectIntoMetadata(doc, ID, "publisher", "dc:publisher");
		insertSelectIntoMetadata(doc, ID, "date", "dc:date");
		insertIntoMetadata(ID, "dc:type", "conference object");
		insertIntoMetadata(ID, "dc:format", "1 Online-Ressource");
		
		return ID;
	}

	public static void csv2Database(File csvFile) throws Exception {
		try {
			Scanner csvScanner = new Scanner(csvFile);
			while (csvScanner.hasNext()) {
				okeanos2Database(csvScanner.next());
			}
			csvScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("csvScanner could not load File");
			e.printStackTrace();
		}
	}

}
