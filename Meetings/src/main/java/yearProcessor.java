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

import java.time.LocalDateTime;

public class yearProcessor {

	private static boolean vonVorne = false;
	private static String userDefinedB = "20250521";

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
			String csvFilePath = "/home/wutschka/workspace/ToDo.csv";
			
			System.out.println("sync with csv-File '" + csvFilePath + "' " + LocalDateTime.now());
			Database.syncToYear(csvFilePath);
		}
//		Database.printDatabaseWithStatus("ueberordnungen", 10, "");

//		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status=11 WHERE ID!='dgnc2020';");

		System.out.println("UeberordnungDownload " + LocalDateTime.now());
		UeberordnungDownload.ueberordnungDownload();
//		String Ue_ID = "eth2014", Ab_ID = "14eth01";
//		SqlManager.INSTANCE.executeUpdate("INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('"
//				+ Ue_ID + "', '" + Ab_ID + "', 'https://www.egms.de/static/de/meetings/eth2014/14eth01.shtml', 'de', 10);");
//		SqlManager.INSTANCE.executeUpdate("INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('"
//				+ Ue_ID + "', '" + Ab_ID + "', 'https://www.egms.de/static/en/meetings/eth2014/14eth01.shtml', 'en', 10);");

//		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status=11;");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='25dga026';");
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=10 WHERE Ab_ID='22gma146';");

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

	public static void buildUpdates() throws Exception {
		System.out.println("buildUpdates Anfang");
		AbstractPacker.userDefinedB = userDefinedB;
		UeberordnungPacker.userDefinedB = userDefinedB;//wobei Überordnungen eh nicht hierrüber geupdatet werden
		if (vonVorne) {
			System.out.println("Clean");
			Clean.cleanDirs();
			Clean.cleanTables();
			System.out.println("LinkCrawl " + LocalDateTime.now());
			LinkCrawl.processBothLanguages();
			System.out.println("syncToYear " + LocalDateTime.now());
			String csvFilePath = "/home/wutschka/workspace/2016.csv";
			Database.syncToYear(csvFilePath);
			System.out.println("UeberordnungDownload " + LocalDateTime.now());
			UeberordnungDownload.ueberordnungDownload();//ist nötig um die Abstract IDs in die Datenbank zu kriegen
			
//			SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=10 WHERE Ab_ID='22gma146';");
			Database.findOutOfDate("/home/wutschka/workspace/Kongresse_GM03_2022-04-18.csv", "/home/wutschka/workspace/GMS_Kongresse.csv");
		}
		Database.printDatabaseOverview("abstracts");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11;");
		
		System.out.println("AbstractDownload " + LocalDateTime.now());
		AbstractDownload.abstractDownload();
		System.out.println("AbstractConvert " + LocalDateTime.now());
		AbstractConvert.abstractConvert();
		System.out.println("Abstracts packen " + LocalDateTime.now());
		AbstractPacker.databaseWorker();
		System.out.println("buildUpdates Ende " + LocalDateTime.now());
	}

	public static void main(String[] args) throws Exception {
		processYear();
//		buildUpdates();
	}
}
