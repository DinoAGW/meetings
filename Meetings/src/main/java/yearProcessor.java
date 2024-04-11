import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import SIP.AbstractPacker;
import SIP.UeberordnungPacker;
import html2pdf.AbstractConvert;
import html2pdf.UeberordnungConvert;
import linkCrawl.LinkCrawl;
import linkDownload.AbstractDownload;
import linkDownload.UeberordnungDownload;
import metadata.UeberordnungMetadataParser;
import utilities.Clean;
import utilities.Database;
import utilities.SqlManager;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public class yearProcessor {

	private static boolean vonVorne = false;
	private static String userDefinedB = "20240409";

	public static void main(String[] args) throws Exception {
		System.out.println("yearProcessor.main Anfang");
		AbstractPacker.userDefinedB = userDefinedB;
		UeberordnungPacker.userDefinedB = userDefinedB;
		if (vonVorne) {
			System.out.println("Clean");
			Clean.cleanDirs();
			Clean.cleanTables();
			System.out.println("LinkCrawl " + LocalDateTime.now());
			LinkCrawl.processBothLanguages();
			System.out.println("syncToYear " + LocalDateTime.now());
//		String csvFilePath = "/home/wutschka/workspace/Kongress_HT_2017_auszug.csv";
//		String csvFilePath = "/home/wutschka/workspace/Kongress_HT_2021_zweiDavon.csv";
			String csvFilePath = "/home/wutschka/workspace/2024_04_04 zuArchivieren.csv";

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
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID NOT LIKE '%01';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='23dkou015';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='23dgh14';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='23dgh66';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='23degam289';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='23dgrh197';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='22dkou367';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='22dgrh141';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='22dgrh151';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='22altra04';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='22dgnc372';");
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=11 WHERE Ab_ID='21degam204';");

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
		System.out.println("yearProcessor.main Ende " + LocalDateTime.now());
	}

}
