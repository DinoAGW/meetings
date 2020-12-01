package linkDownload;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import myWget.MyUtils;
import myWget.MyWget;
import utilities.Abstract;
import utilities.Clean;
import utilities.Kongress;
import utilities.SqlManager;

public class UeberordnungDownload {
	static String fs = System.getProperty("file.separator");

	public static void linkDownload(String protokoll, String hostname)
			throws IOException, SQLException, InterruptedException {
		ResultSet resultSet = null;

		resultSet = SqlManager.INSTANCE.executeSql("SELECT * FROM ueberordnungen WHERE status=10");

		int Anzahl = 2;
		while (resultSet.next()) {
			System.out.println("Verarbeite: '" + resultSet.getString("ID") + "', '" + resultSet.getString("URL") + "'");
			Kongress it = new Kongress(resultSet.getString("URL"));
			// der eigentliche Aufruf
			String kongressDir = Clean.mainPath + "kongresse" + fs + it.kurzID + it.languageSpec + fs;
			MyWget myWget = new MyWget(it.url, kongressDir, true);
			@SuppressWarnings("unused")
			int res = myWget.getPage();

			File kongressFile = new File(myWget.getTarget());
			Document doc = Jsoup.parse(kongressFile, "CP1252", protokoll + hostname);//
			Elements elements = doc.getElementById("owner_links").children();
			List<String> owner_links = new ArrayList<String>();
			for (Element listenEintrag : elements) {
				if (!listenEintrag.hasClass("selected")) {
					owner_links.add(listenEintrag.getElementsByTag("a").first().attr("href"));
				}
			}
			MyWget[] contentMyWget = new MyWget[owner_links.size()];
			String[] contentPath = new String[owner_links.size()];
			for (int i = 0; i < owner_links.size(); i++) {
				// System.out.println("herunterladen: " + owner_links.get(i));
				contentPath[i] = Clean.mainPath + "kongresse" + fs + it.kurzID + it.languageSpec + fs + i + fs;
				contentMyWget[i] = new MyWget(owner_links.get(i), contentPath[i], true);
				contentMyWget[i].getPage();
			}

			File dest = new File(kongressDir + fs + "merge" + fs + "content" + fs + hostname + fs);
			dest.mkdirs();
			if (dest.exists()) {
				MyUtils.deleteDirectory(dest);
			}
			MyUtils.copyFolder(new File(kongressDir + "content" + fs + hostname + fs), dest);
			for (int i = 0; i < owner_links.size(); i++) {
				MyUtils.copyFolder(new File(contentPath[i] + "content" + fs + hostname + fs), dest);
			}

			SortedSet<String> lines = new TreeSet<>();
			BufferedReader br = new BufferedReader(
					new FileReader(new File(kongressDir + "content" + fs + "checksum.md5")));
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
			for (int i = 0; i < owner_links.size(); i++) {
				br = new BufferedReader(
						new FileReader(new File(kongressDir + i + fs + "content" + fs + "checksum.md5")));
				while ((line = br.readLine()) != null) {
					lines.add(line);
				}
				br.close();
			}
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(new File(kongressDir + "merge" + fs + "content" + fs + "checksum.md5")));
			Iterator<String> it2 = lines.iterator();
			while (it2.hasNext()) {
				bw.append(it2.next() + "\n");
			}
			bw.close();

			File htmlFile = new File(kongressDir + "content" + fs + "target.html");
			doc = Jsoup.parse(htmlFile, "CP1252", protokoll + hostname);// vorher: "ISO-8859-1"
//			doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);//wof�r wa das?
//			doc.outputSettings().charset("CP1252");//vorher: "UTF-8"
//			doc.outputSettings().charset("ISO-8859-1");

			Element content = doc.child(0);
			content.getElementById("navigation_language").remove();
			content.getElementById("navigation").remove();
			content.getElementsByClass("hidden_navigation").first().remove();
			content.getElementById("page").before(content.getElementById("header"));
			content.getElementsByTag("script").remove();
			Document[] newDoc = new Document[10];
			Element[] newElement = new Element[10];
			for (int i = 0; i < owner_links.size(); i++) {
				content.getElementById("content").append("<div class=\"clear\">&nbsp;</div>");
				newDoc[i] = Jsoup.parse(new File(contentPath[i] + "content" + fs + "target.html"), "CP1252", // vorher:
																												// "ISO-8859-1"
						protokoll + hostname);
				newElement[i] = newDoc[i].getElementById("owner");
				while (newElement[i].nextElementSibling() != null) {
					newElement[i] = newElement[i].nextElementSibling();
					content.getElementById("content").append(newElement[i].html());
				}
			}
			FileOutputStream fstream = new FileOutputStream(
					kongressDir + "merge" + fs + "content" + fs + "target.html");
			OutputStreamWriter out = new OutputStreamWriter(fstream, "windows-1252");
			out.append(doc.html());
			out.close();
			fstream.close();
			/*
			 * bw = new BufferedWriter( // new FileWriter(kongressDir +
			 * "merge/content/target.html", // Charset.forName("ISO-8859-1"))); new
			 * FileWriter(kongressDir + "merge" + fs + "content" + fs + "target.html"));
			 * bw.append(doc.html()); bw.close();
			 */

			File cssFile = new File(kongressDir + "merge" + fs + "content" + fs + "www.egms.de" + fs + "static" + fs
					+ "css" + fs + "gms-framework.css");
			if (!MyUtils.md5_of_file(cssFile).equals("532d9c009619553ea5841742ac59b2df")) {
				System.err.println("gms-framework.css ist anders, als gewohnt.");
			}
			File cssFileSrc = new File(Clean.mainPath + "gms-framework.css");
			Files.copy(cssFileSrc.toPath(), cssFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			Elements sessionlist = content.getElementsByClass("sessionlist").first().getElementsByTag("a");
			int i = 0;
			int Anzahl2 = 1;
			for (Element session : sessionlist) {
				String kongressDir2 = Clean.mainPath + "kongresse" + fs + it.kurzID + it.languageSpec + fs + "abstractlist"
						+ ++i;
				MyWget myWget2 = new MyWget(session.attr("href"), kongressDir2, true);
				@SuppressWarnings("unused")
				int res2 = myWget2.getPage();

				File abstractlistFile = new File(myWget2.getTarget());
				Elements abstractlist = Jsoup.parse(abstractlistFile, "CP1252", protokoll + hostname).child(0)// vorher:
																												// "ISO-8859-1"
						.getElementsByClass("hx_link");

				// System.out.println("hier:");
				// System.out.println(Jsoup.parse(abstractlistFile, "ISO-8859-1", protokoll +
				// hostname).child(0));
				for (Element abstractElement : abstractlist) {
					Abstract aAbstract = new Abstract(abstractElement.attr("href"));
					ResultSet resultSet2 = SqlManager.INSTANCE.executeSql("SELECT * FROM abstracts WHERE Ab_ID = '"
							+ aAbstract.Ab_ID + "_" + aAbstract.language + "'");
					// Pr�fe, ob sich bereits ein solcher Eintrag in der Datenbank befindet
					if (resultSet2.next()) {
						// War schon drin
					} else {
						// F�ge ein
						// System.out.println("Verarbeite: '" + it.kurzID + "', '" + it.url + "'");
						resultSet2 = SqlManager.INSTANCE
								.executeSql("INSERT INTO abstracts (Ue_ID, Ab_ID , URL, Status) VALUES (\""
										+ aAbstract.Ue_ID + "_" + aAbstract.language + "\", \"" + aAbstract.Ab_ID + "_"
										+ aAbstract.language + "\", \"" + aAbstract.url + "\", 10);");
						if (0 == --Anzahl2)
							break; // tu nicht zu viel
					}
				}
				if (0 == Anzahl2)
					break; // tu nicht zu viel
			}

			// Den Fortschritt in der Datenbank vermerken
			int updated = SqlManager.INSTANCE
					.executeUpdate("UPDATE ueberordnungen SET Status = 30 WHERE ID = '" + it.kurzID + "_" + it.language + "';");
			// int updated = 1;// zum testen
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID '"
						+ it.kurzID + "_" + it.language + "', aber es waren: " + updated + ".");
			// Nicht zu schnell eine Anfrage nach der Anderen
			System.out.flush();
			TimeUnit.SECONDS.sleep(1);

			if (0 == --Anzahl)
				break; // Tue nicht zu viel
		}

	}

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		String protokoll = "https://";
		String hostname = "www.egms.de";

		linkDownload(protokoll, hostname);

		System.out.println("UeberordnungDownload Ende.");
	}
}
