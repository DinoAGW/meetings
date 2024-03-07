package linkDownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
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
import utilities.Reference;

public class AbstractDownload {
	private static String fs = System.getProperty("file.separator");

	public static void linkDownload(String absPath, String protokoll, String hostname)
			throws Exception {
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=10");

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
//			checkFor65533(kongressFile);
			Document doc = Jsoup.parse(kongressFile, "CP1252", protokoll.concat(hostname));
//			System.out.println(doc.getElementsByClass("contentTextblock").last().getElementsByTag("em").text().charAt(2)+0+String.format("%X", 65533));
//			System.out.println(doc.getElementsByClass("contentTextblock").last().getElementsByTag("em").toString().charAt(15)+0);
//			System.out.println(doc.getElementsByClass("contentTextblock").last().getElementsByTag("em").toString().contains(String.format("%X", 65533)));
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
			ersetzeNbspInTitle(doc);

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
			Utilities.replaceFiles(Resources.INSTANCE.getCss2(), css2Path, "883399c552b79d39aac20649472aa2e5");

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
	
	private static void checkFor65533(File kongressFile) throws IOException {
		InputStreamReader readerFrom = new InputStreamReader(new FileInputStream(kongressFile), "CP1252");
//		FileReader readerFrom = new FileReader(kongressFile);
		int i, count65533 = 0, intBuf, zeile=1, spalte=1;
		for (i = 0;; i++) {
			intBuf = readerFrom.read();
			if (intBuf == -1) {
				System.out.println(i + " Zeichen gelesen. Davon " + count65533 + " " + (char) 65533);
				break;
			}
			if (intBuf == 65533) {
				System.out.printf("Hier: %5d (%5d, %5d)%n", i, zeile, spalte);
				count65533++;
			}
			if (intBuf == 10) {
				++zeile;
				spalte = 1;
			} else {
				++spalte;
			}
		}
		readerFrom.close();
	}

	public static void ersetzeNbspInTitle(final Document doc) throws Exception {
		Element elem = doc.head();
		if (elem == null) {
			System.err.println("Dokument hat kein head");
			throw new Exception();
		}
		elem = elem.getElementsByTag("title").first();
		if (elem == null) {
			System.err.println("Head hat kein title");
			throw new Exception();
		}
		//ersetze Text durch whitespace normalisierte Version
		elem.text(elem.text().replace("\u00A0", " "));
		
		elem = doc.head().getElementsByAttributeValue("name", "DC.Title").first();
		if (elem == null) {
			System.err.println("Head hat kein DC.Title");
			throw new Exception();
		}
		//ersetze Text durch whitespace normalisierte Version
		elem.attr("content", elem.attr("content").replace("\u00A0", " "));
		
		elem = doc.getElementById("owner_description");
		if (elem == null) {
			System.err.println("Dokument hat keine owner_description");
			throw new Exception();
		}
		elem = elem.getElementsByTag("h2").first();
		if (elem == null) {
			System.err.println("owner_description hat kein h2");
			throw new Exception();
		}
		elem.text(elem.text().replace("\u00A0", " "));
	}
	
	private static void processFigures(Element content, String abstractDir, String LANG) throws Exception {
		Elements figureLinks = content.getElementsByClass("link-figure");
		int figuresIncluded = 0;
		LinkedList<Reference> figureList = new LinkedList<Reference>();
		String appandage = null;
		if (LANG.equalsIgnoreCase("de")) {
			appandage = "<h3>Abbildungsverzeichnis</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Verweis</th><th scope=\"col\">Dateiname</th><th scope=\"col\">Beschreibung</th></tr></thead><tbody>";
		} else {
			appandage = "<h3>List of Figures</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Reference</th><th scope=\"col\">Filename</th><th scope=\"col\">Description</th></tr></thead><tbody>";
		}
		for (Element figureLink : figureLinks) {
			//collect Infos
			Reference figure = new Reference();
			figure.href = figureLink.attr("href");
			figure.text = figureLink.text();
			Document doc = Utilities.getWebsite(figure.href);
			Element figureElem = doc.getElementsByTag("img").first();
			figure.src = figureElem.attr("src");
			figure.beschreibung = figureElem.nextElementSibling().text();
			
			//check for duplicates
			boolean isNew = true;
			for (Reference anFig : figureList) {
				boolean isNewText, isNewHref;
				isNewText = !figure.text.contentEquals(anFig.text);
				isNewHref = !figure.href.contentEquals(anFig.href);
				if (isNewText != isNewHref) {
					System.err.println("Inkonsistenz entdeckt zwischen neuer Referenz Text und Link '" + figure.text + "', '" + anFig.text + "', '" + figure.href + "', '" + anFig.href + '"');
					throw new Exception();
				}
				if (!isNewHref) {
					isNew = false;
				}
			}
			//download if new
			if (isNew) {
				++figuresIncluded;
				figure.dateiname = "Abb" + figuresIncluded + ".png";
				figureList.add(figure);
				Utilities.downloadUrlToFile("https://www.egms.de".concat(figure.src),
						abstractDir.concat("Supplementals").concat(fs).concat(figure.dateiname));
				appandage = appandage.concat(
						"<tr><td>" + figure.text + "</td><td>" + figure.dateiname + "</td><td>" + figure.beschreibung + "</td></tr>");
			}
		}
		if (figuresIncluded > 0) {
			content.getElementById("content").append(appandage + "</tbody></table></div>");
		}
	}

	private static void processTables(Element content, String abstractDir, String LANG) throws Exception {
		Elements tableLinks = content.getElementsByClass("link-table");
		int tablesIncluded = 0;
		LinkedList<Reference> tableList = new LinkedList<Reference>();
		String appandage = null;
		if (LANG.equalsIgnoreCase("de")) {
			appandage = "<h3>Tabellenverzeichnis</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Verweis</th><th scope=\"col\">Dateiname</th><th scope=\"col\">Beschreibung</th></tr></thead><tbody>";
		} else {
			appandage = "<h3>List of Tables</h3><div><table align=\"left\" width=\"670\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\"><thdead><tr><th scope=\"col\">Reference</th><th scope=\"col\">Filename</th><th scope=\"col\">Description</th></tr></thead><tbody>";
		}
		for (Element tableLink : tableLinks) {
			// collect Infos
			Reference table = new Reference();
			table.href = tableLink.attr("href");
			table.text = tableLink.text();
			Document doc = Utilities.getWebsite(table.href);
			Element tableElem = doc.getElementsByTag("img").first();
			table.src = tableElem.attr("src");
			table.beschreibung = tableElem.nextElementSibling().text();
			
			//check for duplicates
			boolean isNew = true;
			for (Reference anTab : tableList) {
				boolean isNewText, isNewHref;
				isNewText = !table.text.contentEquals(anTab.text);
				isNewHref = !table.href.contentEquals(anTab.href);
				if (isNewText != isNewHref) {
					System.err.println("Inkonsistenz entdeckt zwischen neuer Referenz Text und Link '" + table.text + "', '" + anTab.text + "', '" + table.href + "', '" + anTab.href + '"');
					throw new Exception();
				}
				isNew = isNewText;
			}
			//download if new
			if (isNew) {
				++tablesIncluded;
				table.dateiname = "Tab" + tablesIncluded + ".png";
				tableList.add(table);
				Utilities.downloadUrlToFile("https://www.egms.de".concat(table.src),
						abstractDir.concat("Supplementals").concat(fs).concat(table.dateiname));
				appandage = appandage.concat(
						"<tr><td>" + table.text + "</td><td>" + table.dateiname + "</td><td>" + table.beschreibung + "</td></tr>");
			}
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

	public static void abstractDownload() throws Exception {
		String absPath = Drive.absPath;

		String protokoll = "https://";
		String hostname = "www.egms.de";

		linkDownload(absPath, protokoll, hostname);
	}

	public static void main(String[] args) throws Exception {
//		String UeO = "dav2016";String Ab = "16dav01";String LANG="de";
		String Ab = "21dkou091";String LANG="de";//17dgpp15
		boolean zuBearbeiten = false; boolean bothLanguages = true;
		
		if (bothLanguages) {
			if (zuBearbeiten) {
				SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 10 WHERE Ab_ID = '" + Ab + "';");
			} else {
				SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 11 WHERE Ab_ID = '" + Ab + "';");
			}
		} else {
			if (zuBearbeiten) {
				SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 10 WHERE Ab_ID = '" + Ab + "' AND LANG = '" + LANG + "';");
			} else {
				SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 11 WHERE Ab_ID = '" + Ab + "' AND LANG = '" + LANG + "';");
			}
		}
		
		abstractDownload();
		System.out.println("AbstractDownload Ende.");
	}
}
