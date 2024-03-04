package linkDownload;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Hex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import myWget.MyUtils;
import myWget.MyWget;
import utilities.Abstract;
import utilities.Database;
import utilities.Drive;
import utilities.Kongress;
import utilities.Resources;
import utilities.SqlManager;
import utilities.Utilities;

public class UeberordnungDownload {
	static String fs = System.getProperty("file.separator");
	
	private static final int overviewsToGo = -1; // funktioniert noch nicht
	private static final int abstractsToGo = -1;

	public static void linkDownload(String protokoll, String hostname)
			throws Exception {

		// Get everything from the overview table
		ResultSet resultSet = Database.getDatabaseWithStatus("ueberordnungen", 10);

		// limiting work done for testing purpose
		int Anzahl = 2*overviewsToGo;

		while (resultSet.next()) {
			String ID = resultSet.getString("ID");
			String URL = resultSet.getString("URL");
			String LANG = resultSet.getString("LANG");
			// For each overview record
			System.out
					.println("Verarbeite: '".concat(ID).concat("', '").concat(URL).concat("', '").concat(LANG).concat("'"));
			Kongress it = new Kongress(URL);
			// der eigentliche Aufruf
			String kongressDir = Drive.captPath.concat(it.kurzID).concat("_")
					.concat(LANG).concat(fs);
			MyWget myWget = new MyWget(it.url, kongressDir.concat("original").concat(fs), true);
			myWget.getPage();

			// Extracting which websites give aditional Infos...
			File kongressFile = new File(myWget.getTarget());
			Document doc = Jsoup.parse(kongressFile, "CP1252", protokoll + hostname);//
			Elements elements = doc.getElementById("owner_links").children();
			List<URL> owner_links = new ArrayList<>();
			for (Element listenEintrag : elements) {
				if (!listenEintrag.hasClass("selected")) {
					owner_links.add(new URL(listenEintrag.getElementsByTag("a").first().attr("href")));
				}
			}
			// ... and download them too
			MyWget[] contentMyWget = new MyWget[owner_links.size()];
			String[] contentPath = new String[owner_links.size()];
			for (int i = 0; i < owner_links.size(); i++) {
				// System.out.println("herunterladen: " + owner_links.get(i));
				contentPath[i] = Drive.captPath.concat(it.kurzID).concat("_")
						.concat(LANG).concat(fs).concat("original").concat(fs).concat("owner_links").concat(fs).concat(Integer.toString(i)).concat(fs);
				contentMyWget[i] = new MyWget(owner_links.get(i), contentPath[i], true);
				contentMyWget[i].getPage();
			}

			// mix all those prerequisites, to be able to merge the websites
			File dest = new File(kongressDir.concat(fs).concat("merge").concat(fs).concat("content").concat(fs)
					.concat(hostname).concat(fs));

			if (dest.exists()) {
				MyUtils.deleteDirectory(dest);
			}
			MyUtils.copyFolder(new File(kongressDir.concat("original").concat(fs).concat("content").concat(fs).concat(hostname).concat(fs)), dest);
			for (int i = 0; i < owner_links.size(); i++) {
				String source = contentPath[i].concat("content").concat(fs).concat(hostname).concat(fs);
				MyUtils.copyFolder(new File(source), dest);
			}

			// merge the checksum Files (actually isn't needed yet, just to keep the
			// standard)
			SortedSet<String> lines = new TreeSet<>();
			BufferedReader br = new BufferedReader(
					new FileReader(new File(kongressDir.concat("original").concat(fs).concat("content").concat(fs).concat("checksum.md5"))));
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
			for (int i = 0; i < owner_links.size(); i++) {
				String checksumPath = kongressDir.concat("original").concat(fs).concat("owner_links").concat(fs).concat(Integer.toString(i)).concat(fs).concat("content").concat(fs)
						.concat("checksum.md5");
				br = new BufferedReader(new FileReader(new File(checksumPath)));
				while ((line = br.readLine()) != null) {
					lines.add(line);
				}
				br.close();
			}
			String checksumPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs)
					.concat("checksum.md5");
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(checksumPath)));
			Iterator<String> it2 = lines.iterator();
			while (it2.hasNext()) {
				bw.append(it2.next() + "\n");
			}
			bw.close();

			// take the "landingpage" of the congress
			String htmlPath = kongressDir.concat("original").concat(fs).concat("content").concat(fs).concat("target.html");
			File htmlFile = new File(htmlPath);
			doc = Jsoup.parse(htmlFile, "CP1252", protokoll + hostname);

			// and get rid of all stuff which is for the website, but what we don't want for
			// the PDF
			Element content = doc.child(0);
			content.getElementById("navigation_language").remove();
			content.getElementById("navigation").remove();
			content.getElementsByClass("hidden_navigation").first().remove();
			content.getElementById("page").before(content.getElementById("header"));
			content.getElementsByTag("script").remove();
			content.getElementById("page").child(0).before("<div style=\"height:130px;\"></div>");
			Document[] newDoc = new Document[10];
			Element[] newElement = new Element[10];
			// adding the content from the websites with further Information to this
			// merge-html File created
			for (int i = 0; i < owner_links.size(); i++) {
				content.getElementById("content").append("<div class=\"clear\">&nbsp;</div>");
				newDoc[i] = Jsoup.parse(new File(contentPath[i] + "content" + fs + "target.html"), "CP1252",
						protokoll + hostname);
				newElement[i] = newDoc[i].getElementById("owner");
				while (newElement[i].nextElementSibling() != null) {
					newElement[i] = newElement[i].nextElementSibling();
					content.getElementById("content").append(newElement[i].html());
				}
			}
			Utilities.addExtLinkImages(doc);
			Utilities.addMailLinkImages(doc);
			ersetzeNbspInTitle(doc);
			
			// and save it to harddisk
			String mergePath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("target.html");
			FileOutputStream fstream = new FileOutputStream(mergePath);
			OutputStreamWriter out = new OutputStreamWriter(fstream, "windows-1252");
			out.append(doc.html());
			out.close();
			fstream.close();

			// check if this one css File is the known one and if yes: replace it with a
			// different one, made for PDF purpose
			String cssPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de")
					.concat(fs).concat("static").concat(fs).concat("css").concat(fs).concat("gms-framework.css");
			Utilities.replaceFiles(Resources.INSTANCE.getCss(), cssPath, "532d9c009619553ea5841742ac59b2df");
			String logoPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de")
					.concat(fs).concat("static").concat(fs).concat("images").concat(fs).concat("header_logo.png");
			Utilities.replaceFiles(Resources.INSTANCE.getLogo(), logoPath, "649a32c9a8e49162d2eb48364caa2f20");
			String css2Path = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de")
					.concat(fs).concat("static").concat(fs).concat("css").concat(fs).concat("gms-content.css");
			Utilities.replaceFiles(Resources.INSTANCE.getCss2(), css2Path, "883399C552B79D39AAC20649472AA2E5");//b878eba1c5bc4b50779bebc1b6589ff8

			// The "LandingPage" of the congress always have a sessionlist, with websites,
			// where the links to the abstracts can be found
			Elements sessionlist = content.getElementsByClass("sessionlist").first().getElementsByTag("a");
			int i = 0;
			// again: limit for testing purpose
			int Anzahl2 = abstractsToGo;
			// Go through them, to extract the links of the abstracts (not actually for
			// "landingPage" of the congress download purpose, but because it is needed
			// later)
			for (Element session : sessionlist) {
				String kongressDir2 = Drive.captPath.concat(it.kurzID).concat("_")
						.concat(LANG).concat(fs).concat("original").concat(fs).concat("abstractlists").concat(fs).concat(Integer.toString(++i));
				// download the Sessions from the Sessionlist
				MyWget myWget2 = new MyWget(new URL(session.attr("href")), kongressDir2, true);
				myWget2.getPage();

				File abstractlistFile = new File(myWget2.getTarget());
				Elements abstractlist = Jsoup.parse(abstractlistFile, "CP1252", protokoll + hostname).child(0)
						.getElementsByClass("hx_link");

				// go through the abstracts in a session
				for (Element abstractElement : abstractlist) {
					Abstract aAbstract = new Abstract(abstractElement.attr("href"));
					ResultSet resultSet2 = SqlManager.INSTANCE.executeQuery(
							"SELECT * FROM abstracts WHERE Ab_ID = '" + aAbstract.Ab_ID + "' AND LANG = '" + LANG + "'");
					// Check if the abstract was in the Database before
					// Prüfe, ob sich bereits ein solcher Eintrag in der Datenbank befindet
					if (resultSet2.next()) {
						// skip if yes
					} else {
						// otherwise insert it
						SqlManager.INSTANCE.executeUpdate("INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('"
								+ aAbstract.Ue_ID + "', '" + aAbstract.Ab_ID + "', '" + aAbstract.url + "', '" + LANG + "', 10);");
						if (0 == --Anzahl2)
							break; // don't do too much (for testing purpose)
					}
				}
				if (0 == Anzahl2)
					break; // don't do too much (for testing purpose)
			}

			// assign this congress as downloaded
			int updated = SqlManager.INSTANCE.executeUpdate(
					"UPDATE ueberordnungen SET Status = 30 WHERE ID = '" + it.kurzID + "' AND LANG = '" + LANG + "';");
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID '" + it.kurzID
						+ "_" + it.language + "', aber es waren: " + updated + ".");
			// For less Traffic, wait after every congress
			System.out.flush();
			TimeUnit.SECONDS.sleep(1);

			if (0 == --Anzahl)
				break; // don't do too much (for testing purpose)
		}

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
		elem.text(elem.text().replace("\u00A0", " "));
		
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
	
	public static void ueberordnungDownload() throws Exception {
		String protokoll = "https://";
		String hostname = "www.egms.de";

		linkDownload(protokoll, hostname);
	}

	public static void main(String[] args) throws Exception {
		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status = 10 WHERE ID = 'eth2013';");
		ueberordnungDownload();
		System.out.println("UeberordnungDownload Ende.");
	}
}
