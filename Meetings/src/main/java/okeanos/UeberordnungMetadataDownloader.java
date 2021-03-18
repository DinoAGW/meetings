package okeanos;

import org.jsoup.nodes.Document;
import utilities.Utilities;

public class UeberordnungMetadataDownloader {
	public static String ht2okeanos(String HT) {
		return "http://okeanos-z39.hbz-nrw.de:5661/hbz01_sru_ros_dc?version=1.1&operation=searchRetrieve&query=dc.identifier=%20"
				.concat(HT).concat("&startRecord=1&maximumRecords=1");
	}

	public static Document getdoc(String HT) throws Exception {
		String okeanos = ht2okeanos(HT);
		Document doc = Utilities.getWebsite(okeanos);
		if (doc == null) {
			System.err.println("Keine Metadaten zu der HT Nummer gefunden.");
			throw new Exception();
		} else
			return doc;
	}

}
