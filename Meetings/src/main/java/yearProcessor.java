import SIP.AbstractPacker;
import SIP.UeberordnungPacker;
import html2pdf.AbstractConvert;
import html2pdf.UeberordnungConvert;
import linkCrawl.LinkCrawl;
import linkDownload.AbstractDownload;
import linkDownload.UeberordnungDownload;
import utilities.Clean;
import utilities.Database;
import utilities.SqlManager;

import java.io.File;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Scanner;

public class yearProcessor {

	private static boolean vonVorne = false;
	private static String userDefinedB = "20250627";

	static void syncToAbstractList(String csvFilePath) throws Exception {
		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status=11;");
		File csvFile = new File(csvFilePath);
		Scanner csvScanner = new Scanner(csvFile);
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11;");
		while (csvScanner.hasNext()) {
			SqlManager.INSTANCE
					.executeUpdate("UPDATE abstracts SET status=10 WHERE Ab_ID='" + csvScanner.next() + "';");
		}
		csvScanner.close();
	}

	public static void processYear() throws Exception {
		System.out.println("processYear Anfang");
		AbstractPacker.userDefinedB = userDefinedB;
		UeberordnungPacker.userDefinedB = userDefinedB;
		if (vonVorne) {
			System.out.println("Clean");
			Clean.cleanDirs();
			Clean.cleanTables();
			System.out.println("LinkCrawl " + LocalDateTime.now());
			LinkCrawl.processBothLanguages();
//		String csvFilePath = "/home/wutschka/workspace/Kongress_HT_2017_auszug.csv";
//		String csvFilePath = "/home/wutschka/workspace/Kongress_HT_2021_zweiDavon.csv";
//			String csvFilePath = "/home/wutschka/workspace/HTNummer.csv";
//			String csvFilePath = "/home/wutschka/workspace/ToDo.csv";
			String csvFilePath = "/home/wutschka/workspace/AlleHTNummern.csv";

			System.out.println("sync with csv-File '" + csvFilePath + "' " + LocalDateTime.now());
			Database.syncToYear(csvFilePath);
		}
//		Database.printDatabaseWithStatus("ueberordnungen", 10, "");

//		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status=11 WHERE ID!='dgpp2023';");

		System.out.println("UeberordnungDownload " + LocalDateTime.now());
		UeberordnungDownload.ueberordnungDownload();
//		String Ue_ID = "eth2014", Ab_ID = "14eth01";
//		SqlManager.INSTANCE.executeUpdate("INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('"
//				+ Ue_ID + "', '" + Ab_ID + "', 'https://www.egms.de/static/de/meetings/eth2014/14eth01.shtml', 'de', 10);");
//		SqlManager.INSTANCE.executeUpdate("INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('"
//				+ Ue_ID + "', '" + Ab_ID + "', 'https://www.egms.de/static/en/meetings/eth2014/14eth01.shtml', 'en', 10);");

//		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status=11;");
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='25dga026';");
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID!='25wdhno30';");
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=10 WHERE Ab_ID='25dga026';");
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11;");

//		syncToAbstractList("/home/wutschka/workspace/GMS veraltete Abstracts.csv");

		System.out.println("UeberordnungConvert " + LocalDateTime.now());
		UeberordnungConvert.ueberordnungConvert();
		System.out.println("Überordnungen packen " + LocalDateTime.now());
		UeberordnungPacker.databaseWorker();

		System.out.println("AbstractDownload " + LocalDateTime.now());
		AbstractDownload.abstractDownload();
		System.out.println("AbstractConvert " + LocalDateTime.now());
		AbstractConvert.abstractConvert();
		System.out.println("Abstracts packen " + LocalDateTime.now());
		AbstractPacker.databaseWorker();
		System.out.println("processYear Ende " + LocalDateTime.now());
	}

	static void syncToFehlende(String csvFilePath) throws Exception {
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11;");
		File csvFile = new File(csvFilePath);
		Scanner csvScanner = new Scanner(csvFile);
		int num = 0;
		while (csvScanner.hasNext()) {
			String id = csvScanner.next();
			ResultSet res = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE Ab_ID = '" + id + "'");
			if (res.next()) {
//				System.out.println((++num) + ") " + id);
//				System.out.println(id);++num;
				int jahr = Integer.parseInt(id.substring(0, 2));
				if (jahr < 15) {
					System.out.println("Id " + id + " wird ignoriert");
				} else {
					SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=10 WHERE Ab_ID='" + id + "';");
					++num;
				}
			} else {
				System.err.println("Id " + id + " wird vermisst");
			}
		}
		csvScanner.close();
		System.out.println("sync complete. Insgesamt: " + num);
	}

	public static void buildUpdates() throws Exception {
		System.out.println("buildUpdates Anfang");
		AbstractPacker.userDefinedB = userDefinedB;
		if (vonVorne) {
			System.out.println("Clean");
			Clean.cleanDirs();
			Clean.cleanTables();
			System.out.println("LinkCrawl " + LocalDateTime.now());
			LinkCrawl.processBothLanguages();
			System.out.println("UeberordnungDownload " + LocalDateTime.now());
			UeberordnungDownload.ueberordnungDownload_nur_fuer_abstracts();// ist nötig um die Abstract IDs in die
																			// Datenbank
																			// zu kriegen
			SqlManager.INSTANCE.executeUpdate(
					"INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('sportmed2003', '03sportmed01', 'https://www.egms.de/static/de/meetings/sportmed2003/03sportmed1.shtml', 'de', 10);");
			SqlManager.INSTANCE.executeUpdate(
					"INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('sportmed2003', '03sportmed01', 'https://www.egms.de/static/en/meetings/sportmed2003/03sportmed1.shtml', 'en', 10);");
			SqlManager.INSTANCE.executeUpdate(
					"INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('eth2014', '14eth01', 'https://www.egms.de/static/de/meetings/eth2014/14eth01.shtml', 'de', 10);");
			SqlManager.INSTANCE.executeUpdate(
					"INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('eth2014', '14eth01', 'https://www.egms.de/static/en/meetings/eth2014/14eth01.shtml', 'en', 10);");
			syncToFehlende("/home/wutschka/workspace/alteVeraltete.csv");
		}

		System.out.println("AbstractDownload " + LocalDateTime.now());
		AbstractDownload.abstractDownload();
		System.out.println("AbstractConvert " + LocalDateTime.now());
		AbstractConvert.abstractConvert();
		System.out.println("Abstracts packen " + LocalDateTime.now());
		AbstractPacker.databaseWorker();
		System.out.println("buildUpdates Ende " + LocalDateTime.now());
	}

	public static void main(String[] args) throws Exception {
//		processYear();
		buildUpdates();
	}
}
