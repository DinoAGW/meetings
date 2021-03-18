package utilities;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Metadata {

	public static void insertIntoMetadata(Metadaten metadaten, String xPathKey, String value) {
		Metadatum metadatum = new Metadatum(xPathKey, value);
		metadaten.MetadatenListe.add(metadatum);
	}

	public static void insertSelectIntoMetadata(Document doc, Metadaten metadaten, String selector, String xPathKey) {
		Elements elems = doc.select(selector);
		for (Element elem : elems) {
			insertIntoMetadata(metadaten, xPathKey, elem.text());
		}
	}
}
