package myWget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

public class MyWget {
	private String fs = System.getProperty("file.separator");
	private String protokoll, pageFrom, dirTo, hostname;
	private boolean context;
	private int result;

	private void makeMD5s(File file, PrintWriter printWriter) throws IOException {
		if (file.isDirectory()) {
			for (File fileEntry : file.listFiles()) {
				// nutze den printWriter auch für den rekursiven Aufruf nach
				this.makeMD5s(fileEntry, printWriter);
			}
		} else if (!file.getPath().endsWith("checksum.md5")) {
			// Füge die jeweilige Datei mit Pfad hinzu, aber relativ zum content Ordner,
			// also ohne den dirTo Pfad und ohne die 8 "content/" Zeichen.
			printWriter.println(MyUtils.md5_of_file(file) + "  " + file.getPath().substring(this.dirTo.length() + 8));
			// lässt sich dann auch mittels #md5sum -c checksum.md5 überprüfen
		}
	}

	/**
	 * Konstruktor
	 * 
	 * @param pageFrom - von welcher Seite ein Snapshot heruntergeladen werden soll.
	 * @param dirTo    - wohin es gespeichert werden soll.
	 * @param context  - ob auch alle weiteren Inhalte zu der Webseite gespeichert
	 *                 werden sollen.
	 */
	public MyWget(String pageFrom, String dirTo, boolean context) {
		if (pageFrom.startsWith("http://")) {
			this.protokoll = "http://";
		} else if (pageFrom.startsWith("https://")) {
			this.protokoll = "https://";
		} else {
			System.err.println("URL muss mit http:// oder mit https:// anfangen!");
		}
		this.pageFrom = pageFrom.substring(this.protokoll.length());
		this.hostname = this.pageFrom.substring(0, this.pageFrom.indexOf("/"));
		this.dirTo = dirTo;
		if (!dirTo.endsWith(fs))
			this.dirTo = this.dirTo + fs;
		this.context = context;
	}

	/**
	 * Schreibe in eine Datei "content/checksum.md5": für jede Datei im Ordner die
	 * Checksumme, den Pfad (relativ zu dirTo) und Dateiname.
	 * Auf diese Weise stimmen die Pfade noch, auch wenn man den content/ Ordner verschiebt oder umbenennt
	 * 
	 * @throws IOException
	 */
	public void buildChecksum() throws IOException {
		File md5File = new File(this.dirTo + "content" + fs +"checksum.md5");
		FileWriter fileWriter = new FileWriter(md5File);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		this.makeMD5s(new File(this.dirTo + "content" + fs), printWriter);
		printWriter.close();
	}

	/**
	 * Ein Programm um die Zahl, die getPage zurÃ¼ckgibt, zu deuten und mit nÃ¤heren
	 * Infos an die Standardausgabe auszugeben
	 * 
	 * @param res
	 * @throws IOException
	 */
	public void explainResult() throws IOException {
		switch (this.result) {
		case 0:
			System.out.println("Ordner wurde neu angelegt");
			break;
		case 1:
			System.out.println("Es hat sich nichts geändert");
			break;
		case 2:
			System.out.println("Es hat sich etwas geändert");
			this.showDifferences();
			break;
		default:
			System.out.println("Das sollte nicht passieren");
		}
	}

	/**
	  * Wenn bei getPage Unterschiede in den Checksummen gefunden wurden, können
	  * diese beide Dateien mithilfe dieser Funktion verglichen und Unterschiede
	  * ausgegeben werden.
	  * 
	  * @throws IOException
	  */
	public void showDifferences() throws IOException {
		BufferedReader checksumOld = new BufferedReader(new FileReader(this.dirTo + "content_temp" + fs + "checksum.md5"));
		BufferedReader checksumNew = new BufferedReader(new FileReader(this.dirTo + "content" + fs + "checksum.md5"));
		String zeileOld, zeileNew;
		ArrayList<String> justOld = new ArrayList<String>();
		ArrayList<String> justNew = new ArrayList<String>();
		while ((zeileOld = checksumOld.readLine()) != null) {
			justOld.add(zeileOld);
		}
		while ((zeileNew = checksumNew.readLine()) != null) {
			if (!justOld.remove(zeileNew)) {
				justNew.add(zeileNew);
			}
		}
		System.out.println("Vorher:");
		for (int i = 0; i < justOld.size(); ++i) {
			if (justOld.get(i).length() < 35) {
				System.out.println(justOld.get(i) + " (zu kurz)");
			} else {
				File file = new File(this.dirTo + "content_temp" + fs + justOld.get(i).substring(34));
				if (!file.exists()) {
					System.out.println(justOld.get(i) + " (Datei nicht gefunden)");
				} else {
					String md5ofFile = MyUtils.md5_of_file(file);
					String md5inFile = justOld.get(i).substring(0, 32);
					if (md5inFile.equals(md5ofFile)) {
						System.out.println(justOld.get(i) + " (verifiziert)");
					} else {
						System.out.println(justOld.get(i) + " (falsche Checksumme)");
					}
				}
			}
		}
		System.out.println("Nachher:");
		for (int i = 0; i < justNew.size(); ++i) {
			if (justNew.get(i).length() < 35) {
				System.out.println(justNew.get(i) + " (zu kurz)");
			} else {
				File file = new File(this.dirTo + "content" + fs + justNew.get(i).substring(34));
				if (!file.exists()) {
					System.out.println(justNew.get(i) + " (Datei nicht gefunden)");
				} else {
					String md5ofFile = MyUtils.md5_of_file(file);
					String md5inFile = justNew.get(i).substring(0, 32);
					if (md5inFile.equals(md5ofFile)) {
						System.out.println(justNew.get(i) + " (verifiziert)");
					} else {
						System.out.println(justNew.get(i) + " (falsche Checksumme)");
					}
				}
			}
		}
		checksumOld.close();
		checksumNew.close();
	}

	/**
	 * Wenn man noch wissen will, wo sich die Datei befindet...
	 * @return Dateiname mit Pfad
	 */
	public String getTarget() {
		if (this.context) {
			return new String(this.dirTo + "content" + fs + this.pageFrom).replace("?", "@");
		} else {
			return new String(this.dirTo + "content" + fs + this.pageFrom.substring(this.pageFrom.lastIndexOf("/") + 1)).replace("?", "@");
		}
	}

	/**
	 * Diese Funktion läst den Aufruf aus. Die Inhalte werden im "Content/" Ordner
	 * gespeichert, ggf entsteht auch ein "Content_temp/" Ordner als Backup des
	 * letzten Ordners und in "aufruf.txt" steht, was dort in dem Ordner getan
	 * wurde.
	 * <p>
	 * Falls context==true, wird auch noch eine target.html
	 * im content/ Ordner erstellt (mit geänderten relativen Links) 
	 * <p>
	 * Rückgabewert ist:
	 * <p>
	 * <ul>
	 * <li>0, falls der Ordner neu angelegt wurde
	 * <li>1, falls sich seit dem letzten Aufruf nichts geÃ¤ndert hat
	 * <li>2, falls sich etwas geÃ¤ndert hat
	 * </ul>
	 * <p>
	 * Diese Ergebnisse können dann noch mittels explainResult(Status) oder
	 * showDifferences() an die Standardausgabe ausgegeben werden.
	 * 
	 * @return Status über Änderung
	 * @throws IOException
	 */
	public int getPage() throws IOException {
		File dir = new File(this.dirTo);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File aufruf = new File(this.dirTo + "aufruf.txt");
		if (!aufruf.exists()) {
			aufruf.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(aufruf);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		// Dokumentation über den Aufruf
		printWriter.println("'" + this.dirTo + "', '" + this.protokoll + this.pageFrom + "', " + this.context);
		printWriter.close();

		int ret = 1;
		dir = new File(this.dirTo + "content");
		// falls der Ordner bereits existiert...
		if (dir.exists() && dir.isDirectory()) {
			File dest = new File(this.dirTo + "content_temp" + fs);
			if (dest.exists()) {
				MyUtils.deleteDirectory(dest);
			}
			// ...sichere ihn erstmal weg.
			if (!dir.renameTo(dest)) {
				System.err.println("Ordner konnte nicht von '" + dir + "' in '" + dest + "' umbenannt werden.");
			}
			//Files.move(Paths.get(dir.getAbsolutePath()), Paths.get(dest.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
		} else {
			ret = 0;
		}
		dir.mkdirs();
		
		/*if (this.context) {
			System.err.println("Zurzeit nicht supported.");
		} else {
			JWGet.get(this.protokoll + this.pageFrom, getTarget());
		}*/
		
		String cmd = null;
		if (SystemUtils.IS_OS_LINUX) {
			if (this.context) {
				// Parameter -p bewirkt, dass auch alles heruntergeladen wird, was dazu gehÃ¶rt
				cmd = "wget -p -k -q -N -P " + this.dirTo + "content/ " + this.protokoll + this.pageFrom;
			} else {
				cmd = "wget    -k -q -N -P " + this.dirTo + "content/ " + this.protokoll + this.pageFrom;
			}
			// führe den wget Befehl aus
			Runtime run = Runtime.getRuntime();
			Process pr = run.exec(cmd);
			try {
				pr.waitFor();
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));

				String line = "";

				while ((line = buf.readLine()) != null) {
					System.out.println(line);
				}
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
		} else {
			ProcessBuilder pb = null;
			if (this.context) {
				// Parameter -p bewirkt, dass auch alles heruntergeladen wird, was dazu gehÃ¶rt
				pb = new ProcessBuilder("C:\\wget-1.20.3-win64\\wget.exe", "-p", "-k", "-P", this.dirTo + "content" + fs, this.protokoll + this.pageFrom);
			} else {
				pb = new ProcessBuilder("C:\\wget-1.20.3-win64\\wget.exe", "-k", "-P", this.dirTo + "content" + fs, this.protokoll + this.pageFrom);
			}
			// führe den wget Befehl aus
			pb.redirectErrorStream(true);
			Process process = pb.start();
			BufferedReader inStreamReader = new BufferedReader ( new InputStreamReader(process.getInputStream()));
			while (inStreamReader.readLine() != null) {
				// do something with commandline output
			}
		}

		this.buildChecksum();

		//System.out.println("hostname = '" + this.hostname + "'");
		if (this.context) {
			File target = new File(this.getTarget());
			//System.out.println(this.getTarget());
			Files.copy(target.toPath(), new File(this.dirTo + "content" + fs + "target.html").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			target = new File(this.dirTo + "content" + fs + "target.html");
			Document doc = Jsoup.parse(target, "ISO-8859-1", this.hostname);
			doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
			doc.outputSettings().charset("UTF-8");
			
			Elements select = doc.select("a");
			for (Element e : select) {
				String url = e.attr("href");
				if (url.contentEquals(this.pageFrom.substring(this.pageFrom.lastIndexOf("/") + 1))) {
					url = this.protokoll + this.pageFrom;
				}
				e.attr("href", url);
			}
			// now we process the imgs
			select = doc.select("img");
			for (Element e : select) {
				String url = e.attr("src");
				int up = MyUtils.countOccrences(this.pageFrom, '/') - 1;
				if (up > 0) {
					String[] tokens = pageFrom.split("/");
					for (int i = up; i >= 0; i--) {
						if (url.startsWith("../")) {
							url = url.substring(3);
						} else {
							url = tokens[i] + "/" + url;
						}
					}
				}
				e.attr("src", url);
			}
			// now we process the link's
			select = doc.select("link");
			for (Element e : select) {
				String url = e.attr("href");
				int up = MyUtils.countOccrences(this.pageFrom, '/') - 1;
				if (up > 0) {
					String[] tokens = pageFrom.split("/");
					for (int i = up; i >= 0; i--) {
						if (url.startsWith("../")) {
							url = url.substring(3);
						} else {
							url = tokens[i] + "/" + url;
						}
					}
				}
				e.attr("href", url);
			}
			FileUtils.writeStringToFile(target, doc.outerHtml(), "ISO-8859-1");
		}

		if (ret == 1) {
			if (FileUtils.contentEquals(new File(this.dirTo + "content" + fs + "checksum.md5"),
					new File(this.dirTo + "content_temp" + fs + "checksum.md5"))) {
				// Falls sich nichts geändert hat...
				File dest = new File(this.dirTo + "content_temp" + fs);
				if (dest.exists()) {
					// ...wird die Kopie nicht mehr gebraucht
					MyUtils.deleteDirectory(dest);
				}
			} else {
				// es hat sich etwas geÃ¤ndert
				ret = 2;
			}
		} else {
			// Ordner wurde neu angelegt
		}
		this.result = ret;
		return ret;
	}
}
