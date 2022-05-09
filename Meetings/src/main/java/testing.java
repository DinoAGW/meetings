import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import SIP.AbstractPacker;
import SIP.UeberordnungPacker;
import html2pdf.AbstractConvert;
import html2pdf.UeberordnungConvert;
import linkCrawl.LinkCrawl;
import linkDownload.AbstractDownload;
import linkDownload.UeberordnungDownload;
import utilities.Clean;
import utilities.Database;
import utilities.Drive;
import utilities.HtKuerzelDatenbank;
import utilities.SqlManager;
import utilities.Utilities;

@SuppressWarnings("unused")
public class testing {
	
	private static void printSonderfallListe() throws SQLException {
		String database = "sonderfaelle";
		ResultSet resultSet = Database.getDatabaseWithStatus(database, 52);
		int count = 0;
		while (resultSet.next()) {
			++count;
			String Ue_ID = resultSet.getString("Ue_ID");
			String Ab_ID = resultSet.getString("Ab_ID");
			String URL = resultSet.getString("URL");
			String LANG = resultSet.getString("LANG");
//			String message = Integer.toString(count) + " | Ue_ID='" + Ue_ID + "' | Ab_ID='" + Ab_ID + "' | LANG='" + LANG + "' | URL='" + URL + "'";
			String message = Integer.toString(count) + " | URL='" + URL + "'";
			System.out.println(message);
		}
	}

	public static void main(String[] args) throws Exception {
		printSonderfallListe();
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status=10 WHERE status = 65;");//invalide SIPs erneut versuchen
//		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status=10 WHERE ID = 'dgnc2019';");//reaktiviere Ueberordnung
		System.out.println("UeberordnungDownload " + LocalDateTime.now());
		UeberordnungDownload.ueberordnungDownload();
//		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status=11 WHERE ID = 'dgnc2019';");//deaktiviere Ueberordnung
		System.out.println("UeberordnungConvert " + LocalDateTime.now());
		UeberordnungConvert.ueberordnungConvert();
		System.out.println("Überordnungen packen " + LocalDateTime.now());
		UeberordnungPacker.databaseWorker();
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 10 WHERE Ab_ID = '18dgrh054';");//reaktiviere Abstract
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 11 WHERE Ab_ID = '19ifssh0674';");//deaktiviere Abstract
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 11 WHERE status = 10 AND Ab_ID != '19dgnc359';");//deaktiviere andere Abstracts
		System.out.println("AbstractDownload " + LocalDateTime.now());
		AbstractDownload.abstractDownload();
		System.out.println("AbstractConvert " + LocalDateTime.now());
		AbstractConvert.abstractConvert();
		Database.printDatabaseWithStatus("abstracts", 65, "");
//		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 50 WHERE status = 65;");
		System.out.println("Abstracts packen " + LocalDateTime.now());
		AbstractPacker.databaseWorker();
		Database.printDatabaseWithStatus("abstracts", 65, "");
		System.out.println("testing Ende " + LocalDateTime.now());
	}

}
