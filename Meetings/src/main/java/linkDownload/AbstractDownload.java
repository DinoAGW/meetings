package linkDownload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
//import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import myWget.MyUtils;
import myWget.MyWget;
import utilities.Abstract;
import utilities.SqlManager;

public class AbstractDownload {
	static String fs = System.getProperty("file.separator");

	public static void linkDownload(String mainPath, String protokoll, String hostname)
			throws IOException, SQLException, InterruptedException {
		ResultSet resultSet = null;

		resultSet = SqlManager.INSTANCE.executeSql("SELECT * FROM abstracts WHERE status=10");

		int Anzahl = 2 * 1;
		while (resultSet.next()) {
			System.out.println("Verarbeite: '" + resultSet.getString("Ue_ID") + "', '" + resultSet.getString("Ab_ID")
					+ "', '" + resultSet.getString("URL") + "'");
			Abstract it = new Abstract(resultSet.getString("URL"));
			// der eigentliche Aufruf
			String kongressDir = mainPath + "kongresse" + fs + it.Ue_ID + it.languageSpec + fs + it.Ab_ID + fs;
			MyWget myWget = new MyWget(it.url, kongressDir, true);
			@SuppressWarnings("unused")
			int res = myWget.getPage();
			File dest = new File(kongressDir + fs + "merge" + fs + "content" + fs + hostname + fs);
			dest.mkdirs();
			if (dest.exists()) {
				MyUtils.deleteDirectory(dest);
			}
			MyUtils.copyFolder(new File(kongressDir + "content" + fs + hostname + fs), dest);

			File kongressFile = new File(kongressDir + "content" + fs + "target.html");
			Document doc = Jsoup.parse(kongressFile, "CP1252", protokoll + hostname);
			// doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
			// doc.outputSettings().charset("UTF-8");

			Element content = doc.child(0);
			content.getElementById("navigation_language").remove();
			content.getElementById("navigation").remove();
			content.getElementsByClass("hidden_navigation").first().remove();
			content.getElementById("page").before(content.getElementById("header"));
			content.getElementsByTag("script").remove();
			content.getElementsByClass("floatbox").remove();
			FileOutputStream fstream = new FileOutputStream(
					kongressDir + "merge" + fs + "content" + fs + "target.html");
			OutputStreamWriter out = new OutputStreamWriter(fstream, "windows-1252");
			out.append(doc.html());
			out.close();
			fstream.close();
			File cssFile = new File(kongressDir + "merge" + fs + "content" + fs + "www.egms.de" + fs + "static" + fs
					+ "css" + fs + "gms-framework.css");
			if (!MyUtils.md5_of_file(cssFile).equals("532d9c009619553ea5841742ac59b2df")) {
				System.err.println("gms-framework.css ist anders, als gewohnt.");
			}
			File cssFileSrc = new File(mainPath + "gms-framework.css");
			Files.copy(cssFileSrc.toPath(), cssFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Den Fortschritt in der Datenbank vermerken
			int updated = SqlManager.INSTANCE
					.executeUpdate("UPDATE abstracts SET Status = 30 WHERE Ab_ID = '" + it.Ab_ID + "_" + it.language + "';");
//			int updated = 1;// zum testen
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID '"
						+ it.Ab_ID + "_" + it.language + "', aber es waren: " + updated + ".");
			// Nicht zu schnell eine Anfrage nach der Anderen
			System.out.flush();
			TimeUnit.SECONDS.sleep(1);

			if (0 == --Anzahl)
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
