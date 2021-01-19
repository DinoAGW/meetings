package okeanos;

import org.jsoup.nodes.Document;
import utilities.Utilities;

public class MetadataDownloader {

	public static Document getdoc(String HT) throws Exception {
		String okeanos = "http://okeanos-z39.hbz-nrw.de:5661/hbz01_sru_ros_dc?version=1.1&operation=searchRetrieve&query=dc.identifier=%20"
				.concat(HT).concat("&startRecord=1&maximumRecords=1");
		Document doc = Utilities.getWebsite(okeanos);
		if (doc == null) {
			System.err.println("Keine Metadaten zu der HT Nummer gefunden.");
			throw new Exception();
		} else
			return doc;
	}

}
