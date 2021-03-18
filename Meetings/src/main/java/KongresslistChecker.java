import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import utilities.Utilities;

public class KongresslistChecker {

	private static void report(String url, String fehler) {
		System.out.println("URL '".concat(url).concat("' Fehler: ").concat(fehler));
	}

	public static void main(String[] args) {
		String deKongressList = "https://www.egms.de/static/de/meetings/index.htm";
		String enKongressList = "https://www.egms.de/static/en/meetings/index.htm";
		Document deDoc = Utilities.getWebsite(deKongressList);
		Document enDoc = Utilities.getWebsite(enKongressList);
		Element deContent = deDoc.getElementById("content");
		Element enContent = enDoc.getElementById("content");
		List<String> deIDs = new ArrayList<String>();
		List<String> enIDs = new ArrayList<String>();
		for (Element elem : deContent.getElementsByAttribute("href")) {
			String url = elem.attr("href");
			if (url.equals("http://www.zbmed.de/recherchieren/kongresskalender/")
					|| url.equals("/static/de/meetings/newmeeting.htm"))
				continue;
			int offset = 0;
			if (url.startsWith("/dynamic/"))
				offset = "/dynamic/".length();
			if (url.startsWith("/static/"))
				offset = "/static/".length();
			if (offset == 0) {
				report(url, "de: beginnt nicht mit /dynamic/ oder /static/");
				continue;
			}
			if (url.startsWith("de/", offset)) {
				offset += "de/".length();
			} else {
				report(url, "de: geht nicht mit de/ weiter");
				continue;
			}
			if (url.startsWith("meetings/", offset)) {
				offset += "meetings/".length();
			} else {
				report(url, "de: geht nicht mit meetings/ weiter");
				continue;
			}
			if (!url.endsWith("/index.htm")) {
				report(url, "de: endet nicht auf /index.htm");
				continue;
			}
			String ID = url.substring(offset, url.length() - "/index.htm".length());
			if (ID.contains("/")) {
				report(url, "de: ID hat ein /");
				continue;
			}
			if (deIDs.contains(ID)) {
				report(url, "de: ID war schon drin");
				continue;
			}
			deIDs.add(ID);
		}
		for (Element elem : enContent.getElementsByAttribute("href")) {
			String url = elem.attr("href");
			if (url.equals("http://www.zbmed.de/recherchieren/kongresskalender/")
					|| url.equals("/static/de/meetings/newmeeting.htm")
					|| url.equals("http://www.zbmed.de/en/search-find/conference-calendar/")
					|| url.equals("/static/en/meetings/newmeeting.htm"))
				continue;
			int offset = 0;
			if (url.startsWith("/dynamic/"))
				offset = "/dynamic/".length();
			if (url.startsWith("/static/"))
				offset = "/static/".length();
			if (offset == 0) {
				report(url, "en: beginnt nicht mit /dynamic/ oder /static/");
				continue;
			}
			if (url.startsWith("en/", offset)) {
				offset += "en/".length();
			} else {
				report(url, "en: geht nicht mit en/ weiter");
				continue;
			}
			if (url.startsWith("meetings/", offset)) {
				offset += "meetings/".length();
			} else {
				report(url, "en: geht nicht mit meetings/ weiter");
				continue;
			}
			if (!url.endsWith("/index.htm")) {
				report(url, "en: endet nicht auf /index.htm");
				continue;
			}
			String ID = url.substring(offset, url.length() - "/index.htm".length());
			if (ID.contains("/")) {
				report(url, "en: ID hat ein /");
				continue;
			}
			if (enIDs.contains(ID)) {
				report(url, "en: ID war schon drin");
				continue;
			}
			enIDs.add(ID);
		}
				for (String ID : deIDs) {
					if (!enIDs.contains(ID)) {
						report(ID, "taucht auf der englischen Seite nicht auf");
					}
				}
				for (String ID : enIDs) {
					if (!deIDs.contains(ID)) {
						report(ID, "taucht auf der deutschen Seite nicht auf");
					}
				}
	}

}
