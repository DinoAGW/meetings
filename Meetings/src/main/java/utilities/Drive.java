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
	public static final String sipPath = System.getProperty("user.home").concat(fs).concat("workspace").concat(fs)
			.concat("metsSIPs").concat(fs);

	public static String getKongressDir(String Kuerzel, String LANG) {
		return captPath.concat(Kuerzel).concat("_").concat(LANG).concat(fs);
	}

	public static String getAbstractDir(String Ue_ID, String Ab_ID, String LANG) {
		return absPath.concat(Ue_ID).concat("_").concat(LANG).concat(fs).concat(Ab_ID).concat(fs);
	}

	public static String getKongressPDF(String Kuerzel, String LANG) {
		String kongressDir = getKongressDir(Kuerzel, LANG);
		return kongressDir.concat(Kuerzel).concat("_").concat(LANG).concat(".pdf");
	}

	public static String getAbstractPDF(String Ue_ID, String Ab_ID, String LANG) {
		String kongressDir = getAbstractDir(Ue_ID, Ab_ID, LANG);
		return kongressDir.concat(Ab_ID).concat(LANG).concat(".pdf");
	}

	public static String getKongressPreSipPdf(String Kuerzel, String LANG) {
		String preSipDir = getKongressPreSipDir(Kuerzel);
		return preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat("3_derivedFrom2").concat(fs)
				.concat(Kuerzel).concat("_").concat(LANG).concat(".pdf");
	}

	public static String getAbstractPreSipPdf(String Ue_ID, String Ab_ID, String LANG) {
		String preSipDir = getAbstractPreSipDir(Ue_ID, Ab_ID);
		return preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat("3_derivedFrom2").concat(fs)
				.concat(Ab_ID).concat("_").concat(LANG).concat(".pdf");
	}

	public static String getUeberordnungPreSipXml(String Kuerzel) {
		String preSipDir = getKongressPreSipDir(Kuerzel);
		return preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat("SOURCE_MD").concat(fs).concat("SRU.xml");
	}

	public static String getAbstractPreSipWebXml(String Ue_ID, String Ab_ID) {
		String preSipDir = getAbstractPreSipDir(Ue_ID, Ab_ID);
		return preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat("SOURCE_MD").concat(fs).concat(Ab_ID).concat(".xml");
	}

	public static String getAbstractPreSipOaiXml(String Ue_ID, String Ab_ID) {
		String preSipDir = getAbstractPreSipDir(Ue_ID, Ab_ID);
		return preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat("SOURCE_MD").concat(fs).concat("OAI.xml");
	}

	public static String getKongressPreSipDir(String Kuerzel) {
		return sipPath.concat("preSIPs").concat(fs).concat(Kuerzel).concat(fs);
	}

	public static String getAbstractPreSipDir(String Ue_ID, String Ab_ID) {
		return sipPath.concat("preSIPs").concat(fs).concat(Ue_ID.concat("_").concat(Ab_ID)).concat(fs);
	}

	public static String getKongressMergeDir(String Kuerzel, String LANG) {
		String kongressDir = getKongressDir(Kuerzel, LANG);
		return kongressDir.concat("merge").concat(fs).concat("content").concat(fs);
	}

	public static String getAbstractsMergeDir(String Ue_ID, String Ab_ID, String LANG) {
		String kongressDir = Drive.getAbstractDir(Ue_ID, Ab_ID, LANG);
		return kongressDir.concat("merge").concat(fs).concat("content").concat(fs);
	}

	public static String getKongressMergeHtml(String Kuerzel, String LANG) {
		String kongressMergeDir = getKongressMergeDir(Kuerzel, LANG);
		return kongressMergeDir.concat("target.html");
	}

	public static String getAbstractsMergeHtml(String Ue_ID, String Ab_ID, String LANG) {
		String baseDir = getAbstractsMergeDir(Ue_ID, Ab_ID, LANG);
		return baseDir.concat("target.html");
	}

	public static String getKongressSipDir(String rosettaInstance, String materialflowID, String producerId, String Kuerzel) {
		return sipPath.concat(rosettaInstance).concat(fs).concat(materialflowID).concat(fs)
				.concat(producerId).concat(fs).concat(Kuerzel).concat(fs);
	}

	public static String getAbstractSipDir(String rosettaInstance, String materialflowID, String producerId, String Ue_ID, String Ab_ID) {
		return sipPath.concat(rosettaInstance).concat(fs).concat(materialflowID).concat(fs)
				.concat(producerId).concat(fs).concat(Ue_ID.concat("_").concat(Ab_ID)).concat(fs);
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
				move(file, new File(target));
			}
			from.delete();
		} else {
			//			System.out.println("moving File from '".concat(from.getPath()).concat("' to '").concat(to.getPath()).concat("'"));
			Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
