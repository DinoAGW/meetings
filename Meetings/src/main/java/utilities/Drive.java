package utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Drive {
	public static final String fs = System.getProperty("file.separator");
	public static final String mainPath = System.getProperty("user.home").concat(fs).concat(".meetings").concat(fs);
	public static final String landPath = mainPath.concat("landingPage").concat(fs);
	public static final String captPath = mainPath.concat("Ueberordnungen").concat(fs);
	public static final String absPath = mainPath.concat("Abstracts").concat(fs);
	public static final String dbPath = mainPath.concat("database");
	public static final String sipPath = System.getProperty("user.home").concat(fs).concat("workspace").concat(fs).concat("metsSIPs").concat(fs);
	public static String getKongressDir(String Kuerzel, String LANG) {
		return captPath.concat(Kuerzel).concat("_").concat(LANG).concat(fs);
	}
	public static String getKongressPDF(String Kuerzel, String LANG) {
		String kongressDir = getKongressDir(Kuerzel, LANG);
		return kongressDir.concat(Kuerzel).concat("_").concat(LANG).concat(".pdf");
	}
	public static String getPreSipPdf(String Kuerzel, String LANG) {
		String preSipDir = getPreSipDir(Kuerzel);
		return preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat(Kuerzel).concat("_").concat(LANG).concat(".pdf");
	}
	public static String getPreSipDir(String Kuerzel) {
		return sipPath.concat("preSIPs").concat(fs).concat(Kuerzel).concat(fs);
	}
	public static String getKongressMergeDir(String Kuerzel, String LANG) {
		String kongressDir = getKongressDir(Kuerzel, LANG);
		return kongressDir.concat("merge").concat(fs).concat("content").concat(fs);
	}
	public static String getKongressMergeHtml(String Kuerzel, String LANG) {
		String kongressMergeDir = getKongressMergeDir(Kuerzel, LANG);
		return kongressMergeDir.concat("target.html");
	}
	public static String getSipDir(String rosettaInstance, int materialflowID, String Kuerzel) {
		return sipPath.concat(rosettaInstance).concat(fs).concat(Integer.toString(materialflowID)).concat(fs).concat(Kuerzel).concat(fs);
	}
	public static void move(File from, File to) throws IOException {
		if (from.isDirectory()) {
//			System.out.println("moving Directory from '".concat(from.getPath()).concat("' to '").concat(to.getPath()).concat("'"));
			if (!to.exists()) {
				to.mkdir();
			}
			for (File file : from.listFiles()) {
				String rel = file.getPath().substring(from.getPath().length());
				String target = to.getPath().concat(rel);
				move (file, new File(target));
			}
			from.delete();
		} else {
//			System.out.println("moving File from '".concat(from.getPath()).concat("' to '").concat(to.getPath()).concat("'"));
			Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
