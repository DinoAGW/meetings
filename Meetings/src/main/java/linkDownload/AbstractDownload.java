package linkDownload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import myWget.MyUtils;
import myWget.MyWget;
import utilities.Abstract;
import utilities.Drive;
import utilities.Resources;
import utilities.SqlManager;
import utilities.Utilities;

public class AbstractDownload {
	private static String fs = System.getProperty("file.separator");

	public static void linkDownload(String absPath, String protokoll, String hostname)
			throws Exception {
		ResultSet resultSet = null;

		resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=10");

		int Anzahl = -2 * 1;
		while (resultSet.next()) {
			String Ue_ID = resultSet.getString("Ue_ID");
			String Ab_ID = resultSet.getString("Ab_ID");
			String URL = resultSet.getString("URL");
			String LANG = resultSet.getString("LANG");
			System.out.println("Verarbeite: '".concat(Ue_ID).concat("', '").concat(Ab_ID).concat("', '").concat(URL)
					.concat("', '").concat(LANG).concat("'"));
			Abstract it = new Abstract(URL);
			// der eigentliche Aufruf
			String kongressDir = Drive.getAbstractDir(it.Ue_ID, Ab_ID, LANG);
			MyWget myWget = new MyWget(it.url, kongressDir.concat("original").concat(fs), true);
			@SuppressWarnings("unused")
			int res = myWget.getPage();
			String destPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat(hostname)
					.concat(fs);
			File dest = new File(destPath);
			if (dest.exists()) {
				MyUtils.deleteDirectory(dest);
			}
			String sourcePath = kongressDir.concat("original").concat(fs).concat("content").concat(fs).concat(hostname)
					.concat(fs);
			MyUtils.copyFolder(new File(sourcePath), dest);

			File kongressFile = new File(
					kongressDir.concat("original").concat(fs).concat("content").concat(fs).concat("target.html"));
			Document doc = Jsoup.parse(kongressFile, "CP1252", protokoll.concat(hostname));
			// doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
			// doc.outputSettings().charset("UTF-8");

			Element content = doc.child(0);
			content.getElementById("navigation_language").remove();
			content.getElementById("navigation").remove();
			content.getElementsByClass("hidden_navigation").first().remove();
			content.getElementById("page").before(content.getElementById("header"));
			content.getElementsByTag("script").remove();
			content.getElementsByClass("floatbox").remove();
			Utilities.addExtLinkImages(doc);
			Utilities.addMailLinkImages(doc);
			processFigures(content, kongressDir, LANG);
			processTables(content, kongressDir, LANG);
			processAttachments(content, kongressDir, LANG);
			content.getElementById("page").child(0).before("<div style=\"height:130px;\"></div>");

			String htmlPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("target.html");
			FileOutputStream fstream = new FileOutputStream(htmlPath);
			OutputStreamWriter out = new OutputStreamWriter(fstream, "windows-1252");
			out.append(doc.html());
			out.close();
			fstream.close();
			String cssPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de")
					.concat(fs).concat("static").concat(fs).concat("css").concat(fs).concat("gms-framework.css");
			Utilities.replaceFiles(Resources.INSTANCE.getCss(), cssPath, "532d9c009619553ea5841742ac59b2df");
			String logoPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de")
					.concat(fs).concat("static").concat(fs).concat("images").concat(fs).concat("header_logo.png");
			Utilities.replaceFiles(Resources.INSTANCE.getLogo(), logoPath, "649a32c9a8e49162d2eb48364caa2f20");
			String css2Path = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de")
					.concat(fs).concat("static").concat(fs).concat("css").concat(fs).concat("gms-content.css");
			Utilities.replaceFiles(Resources.INSTANCE.getCss2(), css2Path, "b878eba1c5bc4b50779bebc1b6589ff8");

			// Den Fortschritt in der Datenbank vermerken
			int updated = SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET Status = 30 WHERE Ab_ID = '"
					.concat(it.Ab_ID).concat("' AND LANG = '").concat(LANG).concat("';"));
			//			int updated = 1;// zum testen
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID = '"
						.concat(it.Ab_ID).concat("' und LANG = '").concat(it.language).concat("', aber es waren: ")
						.concat(Integer.toString(updated)).concat("."));
			// Nicht zu schnell eine Anfrage nach der Anderen
			System.out.flush();
			TimeUnit.SECONDS.sleep(1);

			if (0 == --Anzahl)
				break; // Tue nicht zu viel
		}

	}

	private static void processFigures(Element content, String abstractDir, String LANG) throws IOException {
		Elements figureLinks = content.getElementsByClass("link-figure");
		int figuresIncluded = 0;
		//700 ist zu viel, 650 könnte bisschen größer sein
		String appandage = null;
		if (LANG.equalsIgnoreCase("de")) {
			appandage = "<h3>Abbildungsverzeichnis</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Verweis</th><th scope=\"col\">Dateiname</th><th scope=\"col\">Beschreibung</th></tr></thead><tbody>";
		} else {
			appandage = "<h3>List of Figures</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Reference</th><th scope=\"col\">Filename</th><th scope=\"col\">Description</th></tr></thead><tbody>";
		}
		for (Element figureLink : figureLinks) {
			++figuresIncluded;
			String href = figureLink.attr("href");
			Document doc = Utilities.getWebsite(href);
			Element figure = doc.getElementsByTag("img").first();
			String figureSrc = figure.attr("src");
			String beschreibung = figure.nextElementSibling().text();
			String dateiname = "Abb" + figuresIncluded + ".png";
			Utilities.downloadUrlToFile("https://www.egms.de".concat(figureSrc),
					abstractDir.concat("Supplementals").concat(fs).concat(dateiname));
			appandage = appandage.concat(
					"<tr><td>" + figureLink.text() + "</td><td>" + dateiname + "</td><td>" + beschreibung + "</td></tr>");
		}
		if (figuresIncluded > 0) {
			content.getElementById("content").append(appandage + "</tbody></table></div>");
		}
	}

	private static void processTables(Element content, String abstractDir, String LANG) throws IOException {
		Elements tableLinks = content.getElementsByClass("link-table");
		int tablesIncluded = 0;
		//700 ist zu viel, 650 könnte bisschen größer sein
		String appandage = null;
		if (LANG.equalsIgnoreCase("de")) {
			appandage = "<h3>Tabellenverzeichnis</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Verweis</th><th scope=\"col\">Dateiname</th><th scope=\"col\">Beschreibung</th></tr></thead><tbody>";
		} else {
			appandage = "<h3>List of Tables</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Reference</th><th scope=\"col\">Filename</th><th scope=\"col\">Description</th></tr></thead><tbody>";
		}
		for (Element figureLink : tableLinks) {
			++tablesIncluded;
			String href = figureLink.attr("href");
			Document doc = Utilities.getWebsite(href);
			Element table = doc.getElementsByTag("img").first();
			String figureSrc = table.attr("src");
			String beschreibung = table.nextElementSibling().text();
			String dateiname = "Tab" + tablesIncluded + ".png";
			Utilities.downloadUrlToFile("https://www.egms.de".concat(figureSrc),
					abstractDir.concat("Supplementals").concat(fs).concat(dateiname));
			appandage = appandage.concat(
					"<tr><td>" + figureLink.text() + "</td><td>" + dateiname + "</td><td>" + beschreibung + "</td></tr>");
		}
		if (tablesIncluded > 0) {
			content.getElementById("content").append(appandage + "</tbody></table></div>");
		}
	}

	private static void processAttachments(Element content, String abstractDir, String LANG) throws Exception {
		Elements listOfAttachmentsElements = content.getElementsByClass("listOfAttachments");
		int count = 0;
		//700 ist zu viel, 650 könnte bisschen größer sein
		if (listOfAttachmentsElements.size() == 0) {
			return;
		} else if (listOfAttachmentsElements.size() > 1) {
			System.err.println("Ich hätte hier höchstens ein 'listOfAttachments' erwartet. Schade :-(");
			throw new Exception();
		}
		String appandage = null;
		if (LANG.equalsIgnoreCase("de")) {
			appandage = "<h3>Anhangsverzeichnis</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Verweis</th><th scope=\"col\">Dateiname</th><th scope=\"col\">Beschreibung</th></tr></thead><tbody>";
		} else {
			appandage = "<h3>List of Attachments</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Reference</th><th scope=\"col\">Filename</th><th scope=\"col\">Description</th></tr></thead><tbody>";
		}
		Elements dlEntries = listOfAttachmentsElements.first().children();
		Iterator<Element> itr = dlEntries.iterator();
		while (itr.hasNext()) {
			++count;
			Element dt = itr.next();
			Element dd = itr.next();
			String href = dd.child(0).attr("href");
			if (href.startsWith("/")) {
				href = "https://www.egms.de".concat(href);
			}
			String dateiname = "Att" + count + href.substring(href.lastIndexOf("."));
			System.out.println(dt.text());
			System.out.println(href);
			Utilities.downloadUrlToFile(href, abstractDir.concat("Supplementals").concat(fs).concat(dateiname));
			appandage = appandage
					.concat("<tr><td>" + dt.text() + "</td><td>" + dateiname + "</td><td>" + dd.text() + "</td></tr>");
		}
		if (count > 0) {
			content.getElementById("content").append(appandage + "</tbody></table></div>");
		}
	}

	public static void main(String[] args) throws Exception {
		//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET Status = 10 WHERE Ab_ID = '11iis03' AND LANG = 'de';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET Status = 10 WHERE Ab_ID = '09ri10' AND LANG = 'de';");
		abstractDownload();
		System.out.println("AbstractDownload Ende.");
	}

	public static void abstractDownload() throws Exception {
		String absPath = Drive.absPath;

		String protokoll = "https://";
		String hostname = "www.egms.de";

		linkDownload(absPath, protokoll, hostname);
	}
}
