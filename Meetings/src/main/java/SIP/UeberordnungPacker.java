package SIP;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlOptions;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;

import com.exlibris.core.infra.common.util.IOUtil;
import com.exlibris.core.sdk.consts.Enum;
import com.exlibris.core.sdk.consts.Enum.UsageType;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.parser.IEParserException;
import com.exlibris.core.sdk.utils.FileUtil;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentFactory;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.GeneralIECharacteristics;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.PreservationLevel;
import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;
import gov.loc.mets.FileType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import net.lingala.zip4j.ZipFile;
import okeanos.UeberordnungMetadataDownloader;
import utilities.Database;
import utilities.Drive;
import utilities.HtKuerzelDatenbank;
import utilities.Jhove;
import utilities.SqlManager;
import utilities.Tar;
import utilities.Utilities;
import utilities.VeraPDF;

public class UeberordnungPacker {
	static final String fs = System.getProperty("file.separator");

	final static String rosettaInstance = "dev";
	final static String materialflowID = "76661659";
	final static String producerId = "2049290";

	private static final String METS_SCHEMA = "http://www.loc.gov/METS/";
	private static final String ROSETTA_METS_SCHEMA = "http://www.exlibrisgroup.com/xsd/dps/rosettaMets";
	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String XML_SCHEMA_REPLACEMENT = "http://www.exlibrisgroup.com/XMLSchema-instance";
	private static final String ROSETTA_METS_XSD = "mets_rosetta.xsd";

	public static boolean processSIP(String subDirectoryName, String HT, String ID) throws Exception {
		final String filesRootFolder = subDirectoryName.concat("content").concat(fs).concat("streams").concat(fs);
		final String IEfullFileName = subDirectoryName.concat(fs).concat("content").concat(fs).concat("ie1.xml");

		System.out.println(
				"Verarbeite: '".concat(HT).concat("', '").concat(ID).concat("', '").concat(subDirectoryName).concat("'."));

		//		org.apache.log4j.helpers.LogLog.setQuietMode(true);

		//create parser
		IEParser ie = IEParserFactory.create();
		//		ie.setIeSourceMd(, arg1);

		// add ie dc
		DublinCore dc = ie.getDublinCoreParser();
		addDcMetadata(dc, HT);
		ie.setIEDublinCore(dc);
		List<FileGrp> fGrpList = new ArrayList<FileGrp>();

		FileGrp fGrp1 = makeFileGroup(ie, Enum.UsageType.VIEW, "PRESERVATION_MASTER", "1", ID.concat("_original"));
		fGrpList.add(fGrp1);
		FileGrp fGrp2 = makeFileGroup(ie, Enum.UsageType.VIEW, "PRE_INGEST_MODIFIED_MASTER", "1",
				ID.concat("_htmlForPdf"));
		fGrpList.add(fGrp2);
		FileGrp fGrp3 = makeFileGroup(ie, Enum.UsageType.VIEW, "MODIFIED_MASTER", "1", ID.concat("_pdf"));
		fGrpList.add(fGrp3);

		//prepare validation
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document xmlDocument = documentBuilder.newDocument();
		org.w3c.dom.Element rootElement = xmlDocument.createElement("JHOVE_Validationsergebnisse");
		xmlDocument.appendChild(rootElement);
		boolean isAllValid = true;

		List<File> dirs = new ArrayList<>();
		dirs.add(new File(filesRootFolder));

		for (int i = 0; i < dirs.size(); i++) {
			for (File file : dirs.get(i).listFiles()) {
				if (file.isDirectory()) {
					//					System.out.println("Füge hinzu: "+file.getAbsolutePath());
					dirs.add(file);
				} else {
					//					System.out.println("Verarbeite Datei: "+file.getAbsolutePath());
					//add file and dnx metadata on file
					String mimeType = endung2mime(file.getName());

					//					FileType fileType = ie.addNewFile(fGrp, mimeType, file.getName(), "file ?");//TODO was ist das?
					FileGrp fGrp = null;
					if (file.getAbsolutePath().startsWith(filesRootFolder.concat("1_Master"))) {
						fGrp = fGrp1;
					} else if (file.getAbsolutePath().startsWith(filesRootFolder.concat("SourceMD"))) {
						fGrp = fGrp1;
					} else if (file.getAbsolutePath().startsWith(filesRootFolder.concat("2_derivedFrom1"))) {
						fGrp = fGrp2;
					} else if (file.getAbsolutePath().startsWith(filesRootFolder.concat("3_derivedFrom2"))) {
						fGrp = fGrp3;
					} else {
						System.err.println("Start '".concat(file.getAbsolutePath()).concat("' nicht erkannt"));
						throw new Exception();
					}
					FileType fileType = ie.addNewFile(fGrp, mimeType,
							file.getAbsolutePath().substring(filesRootFolder.length()), "was ist das?");

					// add dnx - A new DNX is constructed and added on the file level
					DnxDocument dnx = ie.getFileDnx(fileType.getID());
					DnxDocumentHelper fileDocumentHelper = new DnxDocumentHelper(dnx);
					
					if (file.getName().equals("Abstractband.pdf")) {
						fileDocumentHelper.getGeneralFileCharacteristics().setLabel("Abstractband");
					} else if (file.getName().equals("SRU.xml")) {
						fileDocumentHelper.getGeneralFileCharacteristics().setLabel("SRU Metadaten");
					} else {
						String dateiname = file.getName();
						dateiname = dateiname.substring(0, dateiname.lastIndexOf("."));
						fileDocumentHelper.getGeneralFileCharacteristics().setLabel(dateiname);
					}

					fileDocumentHelper.getGeneralFileCharacteristics()
							.setFileOriginalPath(file.getAbsolutePath().substring(subDirectoryName.length()));

					//					if (file.getName().endsWith(".tar")) {
					//						PreservationLevel pLevel = fileDocumentHelper.new PreservationLevel();
					//						pLevel.setPreservationLevelValue("Bitstream Preservation");
					//						fileDocumentHelper.setPreservationLevel(pLevel);
					//					}

					ie.setFileDnx(fileDocumentHelper.getDocument(), fileType.getID());

					//validate File
					boolean inclVeraPdf = file.getAbsolutePath().contentEquals(Drive.getKongressPreSipPdf(ID, "de"))
							|| file.getAbsolutePath().contentEquals(Drive.getKongressPreSipPdf(ID, "en"));
					if (!isValid(file, xmlDocument, rootElement, subDirectoryName, inclVeraPdf)) {
						System.err.println("invalide Datei: '" + file.getAbsolutePath() + "'");
						isAllValid = false;
					}
				}
			}
		}

		//write validationresults
		DOMSource domSource = new DOMSource(xmlDocument);
		File fileOutput = new File(filesRootFolder.concat("SourceMD").concat(fs).concat("JHOVE.xml"));
		StreamResult streamresult = new StreamResult(fileOutput);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer = tf.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.transform(domSource, streamresult);

		ie.generateChecksum(filesRootFolder, Enum.FixityType.MD5.toString());
		ie.updateSize(filesRootFolder);

		DnxDocument ieDnx = DnxDocumentFactory.getInstance().createDnxDocument();
		DnxDocumentHelper ieDnxHelper = new DnxDocumentHelper(ieDnx);

		//Füge UserDefinedA&B hinzu
		GeneralIECharacteristics generalIeCharacteristics = ieDnxHelper.new GeneralIECharacteristics(null, null, null, null, null, "GMSKON_" + ID, "20220811", null);
		ieDnxHelper.setGeneralIECharacteristics(generalIeCharacteristics);
		
		//cms Enrichment verankern
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
		
		//Need to replace manually the namespace with Rosetta Mets schema in order to pass validation against mets_rosetta.xsd
		String xmlRosettaMetsContent = xmlMetsContent.replaceAll(XML_SCHEMA, XML_SCHEMA_REPLACEMENT);
		xmlRosettaMetsContent = xmlMetsContent.replaceAll(METS_SCHEMA, ROSETTA_METS_SCHEMA);
		FileUtil.writeFile(ieXML, xmlRosettaMetsContent);

		//validateXML(ieXML.getAbsolutePath(), xmlRosettaMetsContent, ROSETTA_METS_XSD);
		return isAllValid;
	}

	private static boolean isValid(File file, org.w3c.dom.Document xmlDocument, org.w3c.dom.Element rootElement,
			String subDirectoryName, boolean inclVeraPdf) throws Exception {
		String extension = file.getName();
		if (extension.lastIndexOf(".") > 0) {
			extension = extension.substring(extension.lastIndexOf(".") + 1).toLowerCase();
		} else {
			return true;
		}
		if (extension.equalsIgnoreCase("pdf")) {
			return validate(file, extension, "PDF-hul", xmlDocument, rootElement, subDirectoryName, false, inclVeraPdf);
		} else if (extension.equalsIgnoreCase("xml")) {
			return validate(file, extension, "XML-hul", xmlDocument, rootElement, subDirectoryName, true, inclVeraPdf);
		} else if (extension.equalsIgnoreCase("png")) {
			return validate(file, extension, "PNG-gdm", xmlDocument, rootElement, subDirectoryName, false, inclVeraPdf);
		}
		return true;
	}

	private static boolean validate(File file, String extension, String JhoveModul, org.w3c.dom.Document xmlDocument,
			org.w3c.dom.Element rootElement, String subDirectoryName, boolean justWellFormed, boolean inclVeraPdf)
			throws Exception {
		boolean ret;
		String result = Jhove.jhove(JhoveModul, file.getAbsolutePath());
		org.w3c.dom.Element validationsErgebnis = xmlDocument.createElement("ValidationsErgebnis");
		validationsErgebnis.setAttribute("Datei", file.getAbsolutePath().substring(subDirectoryName.length()));
		validationsErgebnis.setAttribute("Validator", "JHOVE");
		validationsErgebnis.setAttribute("Extension", extension);
		validationsErgebnis.setAttribute("JhoveModul", JhoveModul);
		String anonymizedResult = result.replace(subDirectoryName, "");
		validationsErgebnis.setTextContent(anonymizedResult);
		rootElement.appendChild(validationsErgebnis);
		if (justWellFormed) {
			ret = Jhove.wellFormed(result);
		} else {
			ret = Jhove.wellFormedAndValid(result);
		}
		if (ret && inclVeraPdf) {
			result = VeraPDF.veraPDF("2b", file.getAbsolutePath());
			validationsErgebnis = xmlDocument.createElement("ValidationsErgebnis");
			validationsErgebnis.setAttribute("Datei", file.getAbsolutePath().substring(subDirectoryName.length()));
			validationsErgebnis.setAttribute("Validator", "VeraPDF");
			validationsErgebnis.setAttribute("Flavour", "2b");
			anonymizedResult = result.replace(subDirectoryName, "");
			validationsErgebnis.setTextContent(anonymizedResult);
			rootElement.appendChild(validationsErgebnis);
			if (justWellFormed) {
				ret = VeraPDF.singleWellFormedAndValid(result);
			} else {
				ret = VeraPDF.singleWellFormedAndValid(result);
			}
		}
		return ret;
	}

	private static FileGrp makeFileGroup(IEParser ie, UsageType usageType, String preservationType,
			String revisionNumber, String label) throws IEParserException {
		FileGrp fGrp = ie.addNewFileGrp(usageType, preservationType);
		DnxDocument dnxDocument = ie.getFileGrpDnx(fGrp.getID());
		DnxDocumentHelper documentHelper = new DnxDocumentHelper(dnxDocument);
		documentHelper.getGeneralRepCharacteristics().setRevisionNumber(revisionNumber);
		documentHelper.getGeneralRepCharacteristics().setLabel(label);
		ie.setFileGrpDnx(documentHelper.getDocument(), fGrp.getID());
		return fGrp;
	}

	private static String endung2mime(String dateiname) throws Exception {
		String mimeType = null;
		if (dateiname.endsWith(".pdf")) {
			mimeType = "application/pdf";
		} else if (dateiname.endsWith(".xml")) {
			mimeType = "text/xml";
		} else if (dateiname.endsWith(".zip")) {
			mimeType = "application/zip";
		} else if (dateiname.endsWith(".tar")) {
			mimeType = "application/x-tar";
		} else {
			System.err.println("Dateiendung nicht erkannt: ".concat(dateiname));
			throw new Exception();
		}
		return mimeType;
	}

	private static void validateXML(String fileFullName, String xml, String xsdName) throws Exception {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setSchema(getSchema(xsdName));
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.parse(new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			System.out.println("XML '" + fileFullName + "' doesn't pass validation by :" + xsdName
					+ " with the following validation error: " + e.getMessage());
		}
	}

	private static Schema getSchema(String xsdName) throws Exception {
		Map<String, Schema> schemas = new HashMap<String, Schema>();
		if (schemas.get(xsdName) == null) {
			InputStream inputStream = null;
			try {
				File xsd = new File("src/xsd/mets_rosetta.xsd");
				Source xsdFile = new StreamSource(xsd);
				SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
				schemas.put(xsdName, schemaFactory.newSchema(xsdFile));
			} catch (Exception e) {
				System.out.println("Failed to create Schema with following error: " + e.getMessage());
			} finally {
				IOUtil.closeQuietly(inputStream);
			}
		}
		return schemas.get(xsdName);
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
			if (destDir.exists()) {
				FileUtils.deleteDirectory(destDir);
			}
			destDir.mkdirs();

			Document kongressDoc = Utilities.getWebsite(aURL);
			Elements abstractbandElements = kongressDoc.getElementById("owner_tabs")
					.getElementsByAttributeValueContaining("href", ".pdf");
			String abstractbandUrl = null;
			Element abstractbandElement = null;
			if (abstractbandElements != null) {
				abstractbandElement = abstractbandElements.first();
			}
			if (abstractbandElement != null) {
				abstractbandUrl = abstractbandElement.attr("href");
			}
			if (abstractbandUrl != null) {
				abstractbandUrl = "https://www.egms.de".concat(abstractbandUrl);
				System.out.println(abstractbandUrl);
				String destination = preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat("1_Master")
						.concat(fs).concat("Supplementals").concat(fs).concat("Abstractband.pdf");
				FileUtils.copyURLToFile(new URL(abstractbandUrl), new File(destination));
				String destination2 = preSipDir.concat("content").concat(fs).concat("streams").concat(fs)
						.concat("2_derivedFrom1").concat(fs).concat("Supplementals").concat(fs).concat("Abstractband.pdf");
				FileUtils.copyFile(new File(destination), new File(destination2));
				destination2 = preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat("3_derivedFrom2")
						.concat(fs).concat("Supplementals").concat(fs).concat("Abstractband.pdf");
				FileUtils.copyFile(new File(destination), new File(destination2));
			}

			String destString = Drive.getKongressPreSipPdf(ID, "");
			destString = destString.substring(0, destString.lastIndexOf(fs));
			new File(destString).mkdirs();
			Files.copy(Paths.get(Drive.getKongressPDF(ID, "de")), Paths.get(Drive.getKongressPreSipPdf(ID, "de")),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get(Drive.getKongressPDF(ID, "en")), Paths.get(Drive.getKongressPreSipPdf(ID, "en")),
					StandardCopyOption.REPLACE_EXISTING);

			String metadataURL = UeberordnungMetadataDownloader.ht2okeanos(HT);
			System.out.println("Lade Metadaten herunter von: " + metadataURL);
			FileUtils.copyURLToFile(new URL(metadataURL), new File(Drive.getUeberordnungPreSipXml(ID)));

			// add originals as zip
			destString = Drive.getKongressPreSipDir(ID).concat("content").concat(fs).concat("streams").concat(fs)
					.concat("1_Master").concat(fs);
			new File(destString).mkdirs();
			String fromString = Drive.getKongressDir(ID, "de").concat("original").concat(fs);
			//			new ZipFile(destString.concat("OriginalHtml_de.zip")).addFolder(new File(fromString));
			Tar.createTar(fromString, fromString, destString.concat("OriginalHtml_de.tar"));
			fromString = Drive.getKongressDir(ID, "en").concat("original").concat(fs);
			//			new ZipFile(destString.concat("OriginalHtml_en.zip")).addFolder(new File(fromString));
			Tar.createTar(fromString, fromString, destString.concat("OriginalHtml_en.tar"));

			// add merged content as zip
			destString = Drive.getKongressPreSipDir(ID).concat("content").concat(fs).concat("streams").concat(fs)
					.concat("2_derivedFrom1").concat(fs);
			new File(destString).mkdirs();
			fromString = Drive.getKongressDir(ID, "de").concat("merge").concat(fs);
			//			new ZipFile(destString.concat("HtmlForPdf_de.zip")).addFolder(new File(fromString));
			Tar.createTar(fromString, fromString, destString.concat("HtmlForPdf_de.tar"));
			fromString = Drive.getKongressDir(ID, "en").concat("merge").concat(fs);
			//			new ZipFile(destString.concat("HtmlForPdf_en.zip")).addFolder(new File(fromString));
			Tar.createTar(fromString, fromString, destString.concat("HtmlForPdf_en.tar"));

			// finish completion of SIP
			boolean isAllValid = processSIP(preSipDir, HtKuerzelDatenbank.kuerzel2ht(ID), ID);

			if (isAllValid) {
				// move finished SIP to SIP-Directory
				File sipDir = new File(Drive.getKongressSipDir(rosettaInstance, materialflowID, producerId, ID));
				if (sipDir.exists())
					sipDir.delete();
				sipDir.mkdirs();
				Drive.move(new File(preSipDir), sipDir);

				// update Status
				int updated = SqlManager.INSTANCE
						.executeUpdate("UPDATE ueberordnungen SET status = 70 WHERE ID = '".concat(ID).concat("';"));
				if (updated != 2) {
					System.err.println("Es sollte sich nun genau zwei Zeilen aktualisiert haben unter dem Kürzel '" + ID
							+ "', aber es waren: " + updated + ".");
				}
			} else {
				// move finished SIP to SIP-Directory
				System.err.println("SIP invalide");
				File sipDir = new File(Drive.getInvalidKongressSipDir(rosettaInstance, materialflowID, producerId, ID));
				if (sipDir.exists())
					sipDir.delete();
				sipDir.mkdirs();
				Drive.move(new File(Drive.getKongressPreSipDir(ID)), sipDir);

				// update Status
				int updated = SqlManager.INSTANCE
						.executeUpdate("UPDATE ueberordnungen SET status = 65 WHERE ID = '".concat(ID).concat("';"));
				if (updated != 2) {
					System.err.println("Es sollte sich nun genau zwei Zeilen aktualisiert haben unter dem Kürzel '" + ID
							+ "', aber es waren: " + updated + ".");
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		//		final String filesRootFolder = "/home/wutschka/.meetings/Ueberordnungen/gma2016/preSIP/";
		//		String HT = "HT020488506";
		//		String ID = "gma2016";
		//		processSIP(filesRootFolder, HT, ID);
		SqlManager.INSTANCE.executeUpdate("UPDATE ueberordnungen SET status = 50 WHERE ID = 'dgpraec2016';");
		databaseWorker();
		System.out.println("UeberordnungPacker Ende");
	}
}
