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

	public static void insertSelectIntoMetadata(Document doc, String HT, String ID, String selector, String xPathKey)
			throws Exception {
		Elements elems = doc.select(selector);
		for (Element elem : elems) {
			insertIntoMetadata(HT, ID, xPathKey, elem.text());
		}
	}

	public static String getURLoutofDoc(Document doc) throws Exception {
//		for (Element temp : doc.getAllElements()) {
//			System.out.println("Element: " + temp.cssSelector());
//		}
		Elements identifierElements = doc
				.select("searchRetrieveResponse > records > record > recordData > srw_dc|dc > dc|identifier");
//		System.out.println("Anzahl = " + identifierElements.size());
		String URL = null;
		for (Element identifierElement : identifierElements) {
//			System.out.println("title: " + identifierElement.text());
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
	 * schiebt die benötigten Metadaten zu einer HT Nummer von der okeanos
	 * Schnittstelle in die Metadaten Datenbank
	 */
	public static String okeanos2Database(String HT) throws Exception {
		Document doc = UeberordnungMetadataDownloader.getdoc(HT);
		String URL = getURLoutofDoc(doc);

		String ID = Kongress.url2kuerzel(URL);
		HtKuerzelDatenbank.insertIntoHtKuerzelDatenbank(HT, ID);

		Elements elems = doc.select("searchRetrieveResponse > records > record > recordData > srw_dc|dc").first()
				.children();
		for (Element elem : elems) {
			String xPathKey = (elem.nodeName().contentEquals("dc:contributor")) ? "dc:creator" : elem.nodeName();
			insertIntoMetadata(HT, ID, xPathKey, elem.text());
		}
		insertIntoMetadata(HT, ID, "dc:publisher", "Düsseldorf German Medical Science GMS Publishing House");
		insertIntoMetadata(HT, ID, "dc:type", "conferenceObject");
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

	public static void main(String[] args) throws Exception {
		String HT = "HT020674641";
		Document doc = UeberordnungMetadataDownloader.getdoc(HT);
		Elements identifierElements = doc.select("dc|identifier");
		System.out.println("Anzahl = " + identifierElements.size());
		for (Element identifierElement : identifierElements) {
			System.out.println("title: " + identifierElement.text());
		}
	}
}
