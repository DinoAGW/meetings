package SIP;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlOptions;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import utilities.Drive;
import utilities.SqlManager;
import utilities.Utilities;

public class AbstractPacker {
	static final String fs = System.getProperty("file.separator");
	
	final static String rosettaInstance = "dev";
	final static int materialflowID = 76661659;

	public static void processSIP(String subDirectoryName, String Ab_ID, String URL) throws Exception {
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
		addDcMetadata(dc, URL);
		ie.setIEDublinCore(dc);
		List<FileGrp> fGrpList = new ArrayList<FileGrp>();

		@SuppressWarnings("deprecation")
		FileGrp fGrp = ie.addNewFileGrp(Enum.UsageType.VIEW, Enum.PreservationType.PRESERVATION_MASTER);

		DnxDocument dnxDocument = ie.getFileGrpDnx(fGrp.getID());
		DnxDocumentHelper documentHelper = new DnxDocumentHelper(dnxDocument);
		documentHelper.getGeneralRepCharacteristics().setRevisionNumber("1");
		documentHelper.getGeneralRepCharacteristics().setLabel(Ab_ID);

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
			FileType fileType = ie.addNewFile(fGrp, mimeType, files[i].getName(), "file " + i);//TODO was ist das letzte Argument?

			// add dnx - A new DNX is constructed and added on the file level
			DnxDocument dnx = ie.getFileDnx(fileType.getID());
			DnxDocumentHelper fileDocumentHelper = new DnxDocumentHelper(dnx);
			if (files[i].getName().equals("OAI.xml")) {
				fileDocumentHelper.getGeneralFileCharacteristics().setLabel("OAI Metadaten");
			} else if (files[i].getName().equals(Ab_ID.concat(".xml"))) {
				fileDocumentHelper.getGeneralFileCharacteristics().setLabel(Ab_ID.concat(" Metadaten"));
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
		//TODO sourceMD sollte noch hinzugefügt werden
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
	
	private static String url2md(String URL) throws Exception {
		if (!URL.endsWith(".shtml")) {
			System.err.println("URL endet nicht auf .shtml: '".concat(URL).concat("'"));
			throw new Exception();
		}
		if (URL.indexOf("/de/")==-1) {
			System.err.println("URL hat kein /de/: ".concat(URL));
			throw new Exception();
		}
		if (URL.indexOf("/de/")!=URL.lastIndexOf("/de/")) {
			System.err.println("URL hat mehr als ein /de/: ".concat(URL));
			throw new Exception();
		}
		return URL.replace(".shtml", ".xml").replace("/de/", "/xml/");
	}
	
	private static String kuerzel2oai (String Kuerzel) {
		return "https://portal.dimdi.de/oai-gms/OAIHandler?verb=GetRecord&metadataPrefix=oai_dc&identifier=oai:oai-gms.dimdi.de:GM"
		.concat(Kuerzel);
	}

	private static void addDcMetadata(DublinCore dc, String URL) throws Exception {
		String metadataURL = url2md(URL);
		Document abstractDoc = Utilities.getWebsite(metadataURL);
		if (abstractDoc == null) {
			System.err.println("Metadaten konnten nicht gefunden werden: '".concat(URL).concat("'"));
			throw new Exception();
		}
		Elements elems = abstractDoc.getElementsByTag("GmsArticle");
		if (elems.size() != 1) {
			System.err.println(
					"Es gibt ungleich 1 GmsArticle in der Abstract Metadaten xml: ".concat(Integer.toString(elems.size())));
			throw new Exception();
		}
		Element elem = elems.first();
		elems = elem.getElementsByTag("MetaData");
		if (elems.size() != 1) {
			System.err.println(
					"Es gibt ungleich 1 MetaData in der Abstract Metadaten xml: ".concat(Integer.toString(elems.size())));
			throw new Exception();
		}
		elem = elems.first();
		elems = elem.getElementsByTag("CreatorList");
		if (elems.size() != 1) {
			System.err.println(
					"Es gibt ungleich 1 CreatorList in der Abstract Metadaten xml: ".concat(Integer.toString(elems.size())));
			throw new Exception();
		}
		elem = elems.first();
		elems = elem.getElementsByTag("Creator");
		if (elems.size() == 0) {
			System.err.println("Abstract Metadaten xml hat keine Creator");
			throw new Exception();
		}
		for (Element creatorElement : elems) {
			elems = creatorElement.getElementsByTag("Firstname");
			if (elems.size() != 1) {
				System.err.println("Creator hat ungleich 1 Firstname: ".concat(Integer.toString(elems.size())));
				throw new Exception();
			}
			String firstname = elems.first().text();
			elems = creatorElement.getElementsByTag("Lastname");
			if (elems.size() != 1) {
				System.err.println("Creator hat ungleich 1 Lastname: ".concat(Integer.toString(elems.size())));
				throw new Exception();
			}
			String lastname = elems.first().text();
			dc.addElement("dc:creator", lastname.concat(", ").concat(firstname));
			dc.addElement("dc:relation@dcterms:isPartOf", URL);
		}

		int letzterSlash = URL.lastIndexOf("/");
		String Kuerzel = URL.substring(letzterSlash + 1, URL.length() - ".shtml".length());
		String OaiUrl = kuerzel2oai(Kuerzel);
		Document oaiDoc = Utilities.getWebsite(OaiUrl);
		elems = oaiDoc.getElementsByTag("oai_dc:dc");
		if (elems.size() != 1) {
			System.err.println("OAI hat ungleich 1 oai_dc:dc Tag Elemente: ".concat(Integer.toString(elems.size())));
			throw new Exception();
		}
		elem = elems.first();
		elems = elem.children();
		if (elems.size() == 0) {
			System.err.println("oai_dc:dc hat keine Kinder");
			throw new Exception();
		}
		for (Element mdElem : elems) {
			if (mdElem.tagName().equals("dc:publisher")) {
				int semi = mdElem.text().indexOf(";");
				if (semi == -1) {
					System.err.println("dc:publisher hat kein Semikolon");
					throw new Exception();
				}
				if (semi != mdElem.text().lastIndexOf(";")) {
					System.err.println("dc:publisher hat mehr als ein Semikolon");
					throw new Exception();
				}
				String value = mdElem.text().replace(";", ",");
				dc.addElement("dc:publisher", value);
			} else if (mdElem.tagName().contentEquals("dc:date")) {
				dc.addElement("dc:date@dcterms:issued", mdElem.text());
			} else {
				dc.addElement(mdElem.tagName(), mdElem.text());
			}
		}
	}

	public static void databaseWorker() throws Exception {
		ResultSet results = SqlManager.INSTANCE
				.executeQuery("SELECT * FROM abstracts WHERE status = 50 AND LANG = 'de';");
		while (results.next()) {
			String Ue_ID = results.getString("Ue_ID");
			String Ab_ID = results.getString("Ab_ID");
			String aURL = results.getString("URL");

			String preSipDir = Drive.getAbstractPreSipDir(Ue_ID, Ab_ID);
			File destDir = new File(preSipDir.concat("content").concat(fs).concat("streams").concat(fs));
			if (destDir.exists()) {
				Utilities.deleteDir(destDir);
			}
			destDir.mkdirs();

			Files.copy(Paths.get(Drive.getAbstractPDF(Ue_ID, Ab_ID, "de")),
					Paths.get(Drive.getAbstractPreSipPdf(Ue_ID, Ab_ID, "de")), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get(Drive.getAbstractPDF(Ue_ID, Ab_ID, "en")),
					Paths.get(Drive.getAbstractPreSipPdf(Ue_ID, Ab_ID, "en")), StandardCopyOption.REPLACE_EXISTING);
			String metadataURL = url2md(aURL);
			FileUtils.copyURLToFile(new URL(metadataURL), new File (Drive.getAbstractPreSipWebXml(Ue_ID, Ab_ID)));
			FileUtils.copyURLToFile(new URL(kuerzel2oai(Ab_ID)), new File (Drive.getAbstractPreSipOaiXml(Ue_ID, Ab_ID)));
			processSIP(preSipDir, Ab_ID, aURL);
			File sipDir = new File(Drive.getAbstractSipDir(rosettaInstance, materialflowID, Ue_ID, Ab_ID));
			sipDir.mkdirs();
			Drive.move(new File(Drive.getAbstractPreSipDir(Ue_ID, Ab_ID)), sipDir);
			int updated = SqlManager.INSTANCE
					.executeUpdate("UPDATE abstracts SET status = 70 WHERE Ab_ID = '".concat(Ab_ID).concat("';"));
			if (updated != 2) {
				System.err.println("Es sollte sich nun genau zwei Zeilen aktualisiert haben unter dem Kürzel '" + Ab_ID
						+ "', aber es waren: " + updated + ".");
			}
		}
	}

	public static void main(String[] args) throws Exception {
		//		final String filesRootFolder = "/home/wutschka/.meetings/Ueberordnungen/gma2016/preSIP/";
		//		String HT = "HT020488506";
		//		String ID = "gma2016";
		//		processSIP(filesRootFolder, HT, ID);
		String sipString = Drive.getAbstractSipDir(rosettaInstance, materialflowID, "gma2016", "16gma001");
		File sipFile = new File(sipString);
		if (sipFile.exists()) {
			Utilities.deleteDir(sipString);
		}
		SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 50 WHERE Ab_ID = '16gma001';");
		databaseWorker();
	}
}
