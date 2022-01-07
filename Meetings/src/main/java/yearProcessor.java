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

	private static String processHT(String HT) throws Exception {
		System.out.println(HT);
		return UeberordnungMetadataParser.okeanos2Database(HT);
	}

	private static void syncToYear(String csvFilePath) throws Exception {
		File csvFile = new File(csvFilePath);
		Scanner csvScanner = new Scanner(csvFile);
		List<String> HTnummern = new ArrayList<String>();
		List<String> IDs = new ArrayList<String>();
		//Hole alle Metadaten zu den jeweiligen HT Nummern
		while (csvScanner.hasNext()) {
			String HT = csvScanner.next();
			HTnummern.add(HT);
			String ID = processHT(HT);
			IDs.add(ID);
			//prüfe ob die echten URLS ermittelt werden können
			if (!Database.ueberordnungenDatabaseContainsBothIds(ID)) {
				csvScanner.close();
				System.err
						.println("ID '".concat(ID).concat("' ist nicht in beiden Sprachen in den Überordnungen vertreten"));
				throw new Exception();
			}
		}
		csvScanner.close();
		ResultSet resultSet = Database.getDatabaseWithStatus("ueberordnungen", 10);
		//Lösche Überordnungen die nicht in diesem Jahr vorkommen
		while (resultSet.next()) {
			String ID = resultSet.getString("ID");
			if (!IDs.contains(ID)) {
				//				System.out.println("Alle Einträge mit der ID='".concat(kurzID).concat("' werden wieder gelöscht"));
				Statement stmt;
				stmt = SqlManager.INSTANCE.getConnection().createStatement();
				String update = "DELETE FROM ueberordnungen WHERE ID='".concat(ID).concat("';");
				stmt.executeUpdate(update);
			}
		}

	}

	public static void main(String[] args) throws Exception {
		System.out.println("yearProcessor.main Anfang");
		System.out.println("Clean");
		Clean.clean();
		System.out.println("LinkCrawl " + LocalDateTime.now());
		LinkCrawl.processBothLanguages();
		System.out.println("syncToYear " + LocalDateTime.now());
//		String csvFilePath = "/home/wutschka/workspace/Kongress_HT_2017_auszug.csv";
		String csvFilePath = "/home/wutschka/workspace/Kongress_HT_2018.csv";
		
		syncToYear(csvFilePath);
//		Database.printDatabaseWithStatus("ueberordnungen", 10, "");

		SqlManager.INSTANCE.executeUpdate("DELETE FROM ueberordnungen WHERE ID!='dkou2018';");

		System.out.println("UeberordnungDownload " + LocalDateTime.now());
		UeberordnungDownload.ueberordnungDownload();
//		String Ue_ID = "eth2014", Ab_ID = "14eth01";
//		SqlManager.INSTANCE.executeUpdate("INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('"
//				+ Ue_ID + "', '" + Ab_ID + "', 'https://www.egms.de/static/de/meetings/eth2014/14eth01.shtml', 'de', 10);");
//		SqlManager.INSTANCE.executeUpdate("INSERT INTO abstracts (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('"
//				+ Ue_ID + "', '" + Ab_ID + "', 'https://www.egms.de/static/en/meetings/eth2014/14eth01.shtml', 'en', 10);");

		SqlManager.INSTANCE.executeUpdate("DELETE FROM ueberordnungen WHERE ID!='XXX';");
		SqlManager.INSTANCE.executeUpdate("DELETE FROM abstracts WHERE Ab_ID!='18dkou747';");
//		SqlManager.INSTANCE.executeUpdate("DELETE FROM abstracts WHERE Ab_ID!='XXX';");

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
