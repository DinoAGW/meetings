package okeanos;

import org.jsoup.nodes.Document;
import utilities.Utilities;

public class UeberordnungMetadataDownloader {
	public static String ht2okeanos(String HT) {
		return "http://okeanos-z39.hbz-nrw.de:5661/hbz01_sru_ros_dc?version=1.1&operation=searchRetrieve&query=dc.identifier=%20"
				.concat(HT).concat("&startRecord=1&maximumRecords=1");
	}
	
	public static String ht2alma(String HT) {
		return "https://eu04.alma.exlibrisgroup.com/view/sru/49HBZ_NETWORK?version=1.2&operation=searchRetrieve&recordSchema=dc&query=alma.other_system_number=(DE-605)".concat(HT);
	}

	public static Document getdoc(String HT) throws Exception {
		String okeanos = ht2okeanos(HT);
		String alma = ht2alma(HT);
		Document doc = Utilities.getWebsite(alma);
		if (doc == null) {
			System.err.println("Keine Metadaten zu der HT Nummer gefunden.");
			throw new Exception();
		} else
			return doc;
	}

}
