package SIP;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlOptions;
import org.jsoup.nodes.Document;
import com.exlibris.core.sdk.consts.Enum;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.utils.FileUtil;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentFactory;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;
import gov.loc.mets.FileType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import okeanos.UeberordnungMetadataDownloader;
import utilities.Database;
import utilities.Drive;
import utilities.HtKuerzelDatenbank;
import utilities.SqlManager;
import utilities.Utilities;

public class UeberordnungPacker {
	static final String fs = System.getProperty("file.separator");
	
	final static String rosettaInstance = "dev";
	final static int materialflowID = 76661659;

	public static void processSIP(String subDirectoryName, String HT, String ID) throws Exception {
		final String filesRootFolder = subDirectoryName.concat(fs).concat("content").concat(fs).concat("streams")
				.concat(fs);
		final String IEfullFileName = subDirectoryName.concat(fs).concat("content").concat(fs).concat("ie1.xml");

		//		org.apache.log4j.helpers.LogLog.setQuietMode(true);

		//list of files we are depositing
		File streamDir = new File(filesRootFolder);
		File[] files = streamDir.listFiles();

		//create parser
		IEParser ie = IEParserFactory.create();

		// add ie dc
		DublinCore dc = ie.getDublinCoreParser();
		addDcMetadata(dc, HT);
		ie.setIEDublinCore(dc);
		List<FileGrp> fGrpList = new ArrayList<FileGrp>();

		@SuppressWarnings("deprecation")
		FileGrp fGrp = ie.addNewFileGrp(Enum.UsageType.VIEW, Enum.PreservationType.PRESERVATION_MASTER);

		DnxDocument dnxDocument = ie.getFileGrpDnx(fGrp.getID());
		DnxDocumentHelper documentHelper = new DnxDocumentHelper(dnxDocument);
		documentHelper.getGeneralRepCharacteristics().setRevisionNumber("1");
		documentHelper.getGeneralRepCharacteristics().setLabel(ID);

		ie.setFileGrpDnx(documentHelper.getDocument(), fGrp.getID());

		fGrpList.add(fGrp);
		System.out.println("Directory: " + streamDir.getAbsolutePath() + " with " + files.length + " Files");

		for (int i = 0; i < files.length; i++) {

			//add file and dnx metadata on file
			String mimeType = null;
			if (files[i].getName().endsWith(".pdf")) {
				mimeType = "application/pdf";
			} else if (files[i].getName().endsWith(".xml")) {
				mimeType = "text/xml";
			} else {
				System.err.println("Dateiendung nicht erkannt: ".concat(files[i].getName()));
				throw new Exception();
			}
			FileType fileType = ie.addNewFile(fGrp, mimeType, files[i].getName(), "file " + i);//TODO was ist das?

			// add dnx - A new DNX is constructed and added on the file level
			DnxDocument dnx = ie.getFileDnx(fileType.getID());
			DnxDocumentHelper fileDocumentHelper = new DnxDocumentHelper(dnx);
			if (files[i].getName().equals("Abstractband.pdf")) {
				fileDocumentHelper.getGeneralFileCharacteristics().setLabel("Abstractband");
			} else if (files[i].getName().equals("SRU.xml")) {
				fileDocumentHelper.getGeneralFileCharacteristics().setLabel("SRU Metadaten");
			} else {
				String dateiname = files[i].getName();
				dateiname = dateiname.substring(0, dateiname.lastIndexOf("."));
				fileDocumentHelper.getGeneralFileCharacteristics().setLabel(dateiname);
			}
			
			//TODO ist der OriginalPath richtig?
			fileDocumentHelper.getGeneralFileCharacteristics()
					.setFileOriginalPath(files[i].getAbsolutePath().substring(subDirectoryName.length()));
			ie.setFileDnx(fileDocumentHelper.getDocument(), fileType.getID());
		}

		ie.generateChecksum(filesRootFolder, Enum.FixityType.MD5.toString());
		ie.updateSize(filesRootFolder);

		DnxDocument ieDnx = DnxDocumentFactory.getInstance().createDnxDocument();
		DnxDocumentHelper ieDnxHelper = new DnxDocumentHelper(ieDnx);
		DnxDocumentHelper.CMS cms = ieDnxHelper.new CMS();
		cms.setSystem("HBZ01");
		cms.setRecordId(HT);
		ieDnxHelper.setCMS(cms);
		ie.setIeDnx(ieDnxHelper.getDocument());

		//example for adding a logical Struct Map.
		MetsDocument metsDoc = MetsDocument.Factory.parse(ie.toXML());

		//insert IE created in content directory
		File ieXML = new File(IEfullFileName);
		XmlOptions opt = new XmlOptions();
		opt.setSavePrettyPrint();
		String xmlMetsContent = metsDoc.xmlText(opt);
		FileUtil.writeFile(ieXML, xmlMetsContent);
	}

	private static void addDcMetadata(DublinCore dc, String HT) throws SQLException {
		ResultSet results = Database.getMetadataFor("HT", HT);
		while (results.next()) {
			String xPathKey = results.getString("xPathKey");
			String value = results.getString("value");
			dc.addElement(xPathKey, value);
		}
	}

	public static void databaseWorker() throws Exception {
		ResultSet results = SqlManager.INSTANCE
				.executeQuery("SELECT * FROM ueberordnungen WHERE status = 50 AND LANG = 'de';");
		while (results.next()) {
			String ID = results.getString("ID");
			String aURL = results.getString("URL");
			
			String HT = HtKuerzelDatenbank.kuerzel2ht(ID);

			String preSipDir = Drive.getKongressPreSipDir(ID);
			File destDir = new File(preSipDir.concat("content").concat(fs).concat("streams").concat(fs));
			//			if (!destDir.exists()) {
			destDir.mkdirs();
			//			}

			Document kongressDoc = Utilities.getWebsite(aURL);
			String abstractbandUrl = kongressDoc.getElementById("owner_tabs")
					.getElementsByAttributeValueContaining("href", ".pdf").first().attr("href");
			if (abstractbandUrl != null) {
				abstractbandUrl = "https://www.egms.de".concat(abstractbandUrl);
				System.out.println(abstractbandUrl);
				FileUtils.copyURLToFile(new URL(abstractbandUrl), new File(preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat("Abstractband.pdf")));
			}
			
			Files.copy(Paths.get(Drive.getKongressPDF(ID, "de")), Paths.get(Drive.getKongressPreSipPdf(ID, "de")),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get(Drive.getKongressPDF(ID, "en")), Paths.get(Drive.getKongressPreSipPdf(ID, "en")),
					StandardCopyOption.REPLACE_EXISTING);
			String metadataURL = UeberordnungMetadataDownloader.ht2okeanos(HT);
			FileUtils.copyURLToFile(new URL(metadataURL), new File (Drive.getUeberordnungPreSipXml(ID)));
			processSIP(preSipDir, HtKuerzelDatenbank.kuerzel2ht(ID), ID);
			File sipDir = new File(Drive.getKongressSipDir(rosettaInstance, materialflowID, ID));
			sipDir.mkdirs();
			Drive.move(new File(Drive.getKongressPreSipDir(ID)), sipDir);
			int updated = SqlManager.INSTANCE
					.executeUpdate("UPDATE ueberordnungen SET status = 70 WHERE ID = '".concat(ID).concat("';"));
			if (updated != 2) {
				System.err.println("Es sollte sich nun genau zwei Zeilen aktualisiert haben unter dem Kürzel '" + ID
						+ "', aber es waren: " + updated + ".");
			}
		}
	}

	public static void main(String[] args) throws Exception {
		//		final String filesRootFolder = "/home/wutschka/.meetings/Ueberordnungen/gma2016/preSIP/";
		//		String HT = "HT020488506";
		//		String ID = "gma2016";
		//		processSIP(filesRootFolder, HT, ID);
		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status = 50 WHERE ID = 'gma2016';");
		databaseWorker();
	}
}
