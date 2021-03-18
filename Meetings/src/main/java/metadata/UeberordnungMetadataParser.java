package metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import okeanos.UeberordnungMetadataDownloader;
import utilities.Database;
import utilities.HtKuerzelDatenbank;
import utilities.Kongress;

public class UeberordnungMetadataParser {

	private static void insertIntoMetadata(String HT, String ID, String xPathKey, String value) throws Exception {
		Database.insertIntoMetadataDatabase(HT, ID, xPathKey, value);
	}

	public static void insertSelectIntoMetadata(Document doc, String HT, String ID, String selector, String xPathKey) throws Exception {
		Elements elems = doc.select(selector);
		for (Element elem : elems) {
			insertIntoMetadata(HT, ID, xPathKey, elem.text());
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
		Document doc = UeberordnungMetadataDownloader.getdoc(HT);
		String URL = getURLoutofDoc(doc);

		String ID = Kongress.url2kuerzel(URL);
		HtKuerzelDatenbank.insertIntoHtKuerzelDatenbank(HT, ID);
		
		Elements identifierElements = doc.select("identifier");
		
		for (Element identifierElement : identifierElements) {
			String xPathKey = identifierElement.text().startsWith("http") ? "dc:identifier@dcterms:URI" : "dc:identifier";
			insertIntoMetadata(HT, ID, xPathKey, identifierElement.text());
		}

		insertSelectIntoMetadata(doc, HT, ID, "title", "dc:title");
		insertSelectIntoMetadata(doc, HT, ID, "creator", "dc:creator");
		insertSelectIntoMetadata(doc, HT, ID, "publisher", "dc:publisher");
		insertSelectIntoMetadata(doc, HT, ID, "date", "dc:date");
		insertIntoMetadata(HT, ID, "dc:type", "conference object");
		insertIntoMetadata(HT, ID, "dc:format", "1 Online-Ressource");
		
		return ID;
	}
	
	public static void csv2Database(String csvFilePath) throws Exception {
		File csvFile = new File(csvFilePath);
		csv2Database(csvFile);
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
