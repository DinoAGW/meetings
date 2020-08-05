package linkDownload;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
//import java.io.Writer;
import java.nio.charset.StandardCharsets;
//import java.nio.charset.Charset;
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
import utilities.SqlManager;
import utilities.Utilities;

public class AbstractDownload {
	static String fs = System.getProperty("file.separator");

	public static void linkDownload(String mainPath, String protokoll, String hostname)
			throws IOException, SQLException, InterruptedException {
		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";
		String password = Utilities.readStringFromProperty(propertypfad, "password");
		SqlManager sqlManager = new SqlManager("jdbc:mariadb://localhost/meetings", "root", password);
		ResultSet resultSet = null;

		resultSet = sqlManager.executeSql("SELECT * FROM abstracts WHERE status=10");

		while (resultSet.next()) {
			System.out.println("Verarbeite: '" + resultSet.getString("Ue_ID") + "', '" + resultSet.getString("Ab_ID") + "', '" + resultSet.getString("URL") + "'");
			Abstract it = new Abstract(resultSet.getString("URL"));
			// der eigentliche Aufruf
			String kongressDir = mainPath + "kongresse" + fs + it.Ue_ID + fs + it.Ab_ID + fs;
			MyWget myWget = new MyWget(it.url, kongressDir, true);
			@SuppressWarnings("unused")
			int res = myWget.getPage();

			File kongressFile = new File(myWget.getTarget());
			Document doc = Jsoup.parse(kongressFile, "ISO-8859-1", protokoll + hostname);
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
				contentPath[i] = mainPath + "kongresse" + fs + it.Ue_ID + fs + it.Ab_ID + fs + i + fs;
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
			doc = Jsoup.parse(htmlFile, "ISO-8859-1", protokoll + hostname);
			doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
			doc.outputSettings().charset("UTF-8");

			Element content = doc.child(0);
			content.getElementById("navigation_language").remove();
			content.getElementById("navigation").remove();
			content.getElementsByClass("hidden_navigation").first().remove();
			content.getElementById("page").before(content.getElementById("header"));
			content.getElementsByTag("script").remove();
			content.getElementsByClass("floatbox").remove();
			Document[] newDoc = new Document[10];
			Element[] newElement = new Element[10];
			for (int i = 0; i < owner_links.size(); i++) {
				content.getElementById("content").append("<div class=\"clear\">&nbsp;</div>");
				newDoc[i] = Jsoup.parse(new File(contentPath[i] + "content" + fs + "target.html"), "ISO-8859-1",
						protokoll + hostname);
				newElement[i] = newDoc[i].getElementById("owner");
				while (newElement[i].nextElementSibling() != null) {
					newElement[i] = newElement[i].nextElementSibling();
					content.getElementById("content").append(newElement[i].html());
				}
			}
			FileOutputStream fstream = new FileOutputStream(
					kongressDir + "merge" + fs + "content" + fs + "target.html");
			OutputStreamWriter out = new OutputStreamWriter(fstream, StandardCharsets.ISO_8859_1);
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
			File cssFileSrc = new File(mainPath + "gms-framework.css");
			Files.copy(cssFileSrc.toPath(), cssFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Den Fortschritt in der Datenbank vermerken
			int updated = sqlManager
					.executeUpdate("UPDATE abstracts SET Status = 30 WHERE Ab_ID = '" + it.Ab_ID + "';");
//			int updated = 1;// zum testen
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID '"
						+ it.Ab_ID + "', aber es waren: " + updated + ".");
			// Nicht zu schnell eine Anfrage nach der Anderen
			System.out.flush();
			TimeUnit.SECONDS.sleep(1);

			break; // Tue nicht zu viel
		}

	}

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		String mainPath = "C:\\Users\\hixel\\workspace\\Meetings\\Abstracts\\";

		String protokoll = "https://";
		String hostname = "www.egms.de";

		linkDownload(mainPath, protokoll, hostname);

		System.out.println("AbstractDownload Ende.");
	}
}
