import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import html2pdf.AbstractConvert;
import html2pdf.UeberordnungConvert;
import linkCrawl.LinkCrawl;
import linkDownload.AbstractDownload;
import linkDownload.UeberordnungDownload;
import metadata.MetadataParser;
import utilities.Clean;
import utilities.Database;
import utilities.SqlManager;

import java.sql.ResultSet;
import java.sql.Statement;

public class yearProcessor {

	private static String processHT(String HT) throws Exception {
		System.out.println(HT);
		return MetadataParser.okeanos2Database(HT);
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
				System.err.println("ID '".concat(ID).concat("' ist nicht in beiden Sprachen in den Überordnungen vertreten"));
				throw new Exception();
			}
		}
		csvScanner.close();
		ResultSet resultSet = Database.getDatabaseWithStatus("ueberordnungen", 10);
		//Lösche Überordnungen die nicht in diesem Jahr vorkommen
		while (resultSet.next()) {
			String ID = resultSet.getString("ID");
			String kurzID = ID.substring(0, ID.length()-3);
			if (!IDs.contains(kurzID)) {
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
		System.out.println("LinkCrawl");
		LinkCrawl.processBothLanguages();
		System.out.println("syncToYear");
		String csvFilePath = "/home/wutschka/workspace/Kongress_HT_test.csv";
//		String csvFilePath = "/home/wutschka/workspace/Kongress_HT_2016.csv";
		syncToYear(csvFilePath);
		System.out.println("UeberordnungDownload");
		UeberordnungDownload.ueberordnungDownload();
		System.out.println("UeberordnungConvert");
		UeberordnungConvert.ueberordnungConvert();
		System.out.println("AbstractDownload");
		AbstractDownload.abstractDownload();
		System.out.println("AbstractConvert");
		AbstractConvert.abstractConvert();
		System.out.println("yearProcessor.main Ende");
	}

}