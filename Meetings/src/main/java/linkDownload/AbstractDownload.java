package linkDownload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import myWget.MyUtils;
import myWget.MyWget;
import utilities.Abstract;
import utilities.Drive;
import utilities.Resources;
import utilities.SqlManager;
import utilities.Utilities;

public class AbstractDownload {
	static String fs = System.getProperty("file.separator");

	public static void linkDownload(String absPath, String protokoll, String hostname)
			throws IOException, SQLException, InterruptedException {
		ResultSet resultSet = null;

		resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=10");

		int Anzahl = -2 * 1;
		while (resultSet.next()) {
			String Ue_ID = resultSet.getString("Ue_ID");
			String Ab_ID = resultSet.getString("Ab_ID");
			String URL = resultSet.getString("URL");
			String LANG = resultSet.getString("LANG");
			System.out.println("Verarbeite: '".concat(Ue_ID).concat("', '").concat(Ab_ID).concat("', '").concat(URL).concat("', '").concat(LANG).concat("'"));
			Abstract it = new Abstract(URL);
			// der eigentliche Aufruf
			String kongressDir = absPath.concat(it.Ue_ID).concat("_").concat(LANG).concat(fs).concat(Ab_ID).concat(fs);
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
			Utilities.addExtLinkImages(doc);
			Utilities.addMailLinkImages(doc);
			
			String htmlPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("target.html");
			FileOutputStream fstream = new FileOutputStream(htmlPath);
			OutputStreamWriter out = new OutputStreamWriter(fstream, "windows-1252");
			out.append(doc.html());
			out.close();
			fstream.close();
			String cssPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de").concat(fs).concat("static").concat(fs).concat("css").concat(fs).concat("gms-framework.css");
			Utilities.replaceFiles(Resources.INSTANCE.getCss(), cssPath, "532d9c009619553ea5841742ac59b2df");
			String logoPath = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de").concat(fs).concat("static").concat(fs).concat("images").concat(fs).concat("header_logo.png");
			Utilities.replaceFiles(Resources.INSTANCE.getLogo(), logoPath, "649a32c9a8e49162d2eb48364caa2f20");
			String css2Path = kongressDir.concat("merge").concat(fs).concat("content").concat(fs).concat("www.egms.de").concat(fs).concat("static").concat(fs).concat("css").concat(fs).concat("gms-content.css");
			Utilities.replaceFiles(Resources.INSTANCE.getCss2(), css2Path, "b878eba1c5bc4b50779bebc1b6589ff8");

			// Den Fortschritt in der Datenbank vermerken
			int updated = SqlManager.INSTANCE
					.executeUpdate("UPDATE abstracts SET Status = 30 WHERE Ab_ID = '".concat(it.Ab_ID).concat("' AND LANG = '").concat(LANG).concat("';"));
//			int updated = 1;// zum testen
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID = '".concat(it.Ab_ID).concat("' und LANG = '").concat(it.language).concat("', aber es waren: ").concat(Integer.toString(updated)).concat("."));
			// Nicht zu schnell eine Anfrage nach der Anderen
			System.out.flush();
			TimeUnit.SECONDS.sleep(1);

			if (0 == --Anzahl)
				break; // Tue nicht zu viel
		}

	}

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		abstractDownload();
		System.out.println("AbstractDownload Ende.");
	}
	
	public static void abstractDownload() throws IOException, SQLException, InterruptedException {
		String absPath = Drive.absPath;

		String protokoll = "https://";
		String hostname = "www.egms.de";

		linkDownload(absPath, protokoll, hostname);
	}
}
