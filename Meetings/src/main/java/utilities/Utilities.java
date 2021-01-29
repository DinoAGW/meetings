package utilities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import myWget.MyUtils;

public class Utilities {
	public final static String fs = System.getProperty("file.separator");
	// private string used as root path for background image replacement
	private final static String imgRootPath = "www.egms.de/static/images/picto/";

	public static Document getWebsite(String URL) {
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).get();
		} catch (IOException e) {
			System.out.println("Jsoup connect failed");
			e.printStackTrace();
		}
		return doc;
	}
	
	public static String readline() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String ausgelesen = in.readLine();
		return ausgelesen;
	}

	public static void saveStringToProperty(String ziel, String prop, String value) throws IOException {
		Writer writer = new FileWriter(ziel);
		Properties properties = new Properties();
		properties.setProperty(prop, value);
		properties.store(writer, "default Kommentar");
		writer.close();
	}

	public static String readStringFromProperty(String quelle, String prop) throws IOException {
		Properties properties = new Properties();
		BufferedInputStream reader = new BufferedInputStream(new FileInputStream(quelle));
		properties.load(reader);
		reader.close();
		return properties.getProperty(prop);
	}

	public static void deleteDir(String path) {
		File filePath = new File(path);
		if (filePath.exists()) deleteDir(filePath);
	}

	public static void deleteFile(String filePath) {
		try {
			File file = new File(filePath);
			if(file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add the external link image child element to all document
	 * elements with class ".link-ext".
	 * 
	 * @param doc the document for image addition
	 */
	public static void addExtLinkImages(final Document doc) {
		appndImgToClss(doc, ".link-ext", "browserfenster-ext.png");
	}

	/**
	 * Add the email link image child element to all document
	 * elements with class ".link-mail".
	 * 
	 * @param doc the document for image addition
	 */
	public static void addMailLinkImages(final Document doc) {
		appndImgToClss(doc, ".link-mail", "umschlag.png");
	}
	
	public static void deleteDir(File path) {
		for (File file : path.listFiles()) {
			if (file.isDirectory())
				deleteDir(file);
			file.delete();
		}
		path.delete();
	}

	private static void appndImgToClss(final Document doc, final String selector, final String imgSrc) {
		// Use the selector to get the elements
		Elements extLinks = doc.select(selector);
		for (Element ele : extLinks) {
			// Add image child to each element found 
			ele.appendElement("img").attr("src", imgRootPath.concat(imgSrc));
		}
	}
	
	public static void replaceFiles(File from, String to, String md5Sum) throws IOException {
		File toFile = new File(to);
		if (!MyUtils.md5_of_file(toFile).equals(md5Sum)) {
			System.err.println("MD5 Summe stimmt nicht");
		}
		Files.copy(from.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
