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
import utilities.Clean;
import utilities.Resources;
import utilities.SqlManager;

public class AbstractDownload {
	static String fs = System.getProperty("file.separator");

	public static void linkDownload(String mainPath, String protokoll, String hostname)
			throws IOException, SQLException, InterruptedException {
		ResultSet resultSet = null;

		resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=10");

		int Anzahl = 2 * 1;
		while (resultSet.next()) {
			System.out.println("Verarbeite: '".concat(resultSet.getString("Ue_ID")).concat("', '").concat(resultSet.getString("Ab_ID")).concat("', '").concat(resultSet.getString("URL")).concat("'"));
			Abstract it = new Abstract(resultSet.getString("URL"));
			// der eigentliche Aufruf
			String kongressDir = mainPath.concat(it.getPathId()).concat(fs);
			MyWget myWget = new MyWget(it.url, kongressDir, true);
			@SuppressWarnings("unused")
			int res = myWget.getPage();
			String destPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat(hostname).concat(fs);
			File dest = new File(destPath);
			if (dest.exists()) {
				MyUtils.deleteDirectory(dest);
			}
			String sourcePath = kongressDir.concat("content").concat(fs).concat(hostname).concat(fs);
			MyUtils.copyFolder(new File(sourcePath), dest);

			File kongressFile = new File(kongressDir.concat("content").concat(fs).concat("target.html"));
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
			String htmlPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("target.html");
			FileOutputStream fstream = new FileOutputStream(htmlPath);
			OutputStreamWriter out = new OutputStreamWriter(fstream, "windows-1252");
			out.append(doc.html());
			out.close();
			fstream.close();
			String cssPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de").concat(fs).concat("static").concat(fs).concat("css").concat(fs).concat("gms-framework.css");
			replaceFiles(Resources.INSTANCE.getCss(), cssPath, "532d9c009619553ea5841742ac59b2df");
			String logoPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de").concat(fs).concat("static").concat(fs).concat("images").concat(fs).concat("header_logo.png");
			replaceFiles(Resources.INSTANCE.getLogo(), logoPath, "649a32c9a8e49162d2eb48364caa2f20");

			// Den Fortschritt in der Datenbank vermerken
			int updated = SqlManager.INSTANCE
					.executeUpdate("UPDATE abstracts SET Status = 30 WHERE Ab_ID = '".concat(it.Ab_ID).concat("_").concat(it.language).concat("';"));
//			int updated = 1;// zum testen
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID '".concat(it.Ab_ID).concat("_").concat(it.language).concat("', aber es waren: ").concat(Integer.toString(updated)).concat("."));
			// Nicht zu schnell eine Anfrage nach der Anderen
			System.out.flush();
			TimeUnit.SECONDS.sleep(1);

			if (0 == --Anzahl)
				break; // Tue nicht zu viel
		}

	}
	
	private static void replaceFiles(File from, String to, String md5Sum) throws IOException {
		File fromFile = new File(to);
		if (!MyUtils.md5_of_file(fromFile).equals(md5Sum)) {
			System.err.println("MD5 Summe stimmt nicht");
		}
		Files.copy(from.toPath(), fromFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		String absPath = Clean.mainPath.concat("Abstracts").concat(fs);

		String protokoll = "https://";
		String hostname = "www.egms.de";

		linkDownload(absPath, protokoll, hostname);

		System.out.println("AbstractDownload Ende.");
	}
}
