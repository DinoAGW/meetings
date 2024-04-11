package SIP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
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
import com.exlibris.digitool.common.dnx.DnxSection;
import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;
import gov.loc.mets.FileType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import net.lingala.zip4j.ZipFile;
import utilities.Drive;
import utilities.SqlManager;
import utilities.Tar;
import utilities.Utilities;
import utilities.VeraPDF;
import utilities.Jhove;

public class AbstractPacker {
	static final String fs = System.getProperty("file.separator");

	final static String rosettaInstance = "dev";
	final static String materialflowID = "76661659";
	final static String producerId = "2049290";
	public static String userDefinedB = "20240307";

	private static final String ROSETTA_METS_SCHEMA = "http://www.exlibrisgroup.com/xsd/dps/rosettaMets";
	private static final String METS_SCHEMA = "http://www.loc.gov/METS/";
	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String XML_SCHEMA_REPLACEMENT = "http://www.exlibrisgroup.com/XMLSchema-instance";
	private static final String ROSETTA_METS_XSD = "mets_rosetta.xsd";

	public static boolean processSIP(String subDirectoryName, String Ue_ID, String Ab_ID, String URL) throws Exception {
		final String filesRootFolder = subDirectoryName.concat("content").concat(fs).concat("streams").concat(fs);
		final String IEfullFileName = subDirectoryName.concat("content").concat(fs).concat("ie1.xml");

//		System.out.println("Verarbeite: '".concat(Ue_ID).concat("', '").concat(Ab_ID).concat("', '").concat(URL)
//				.concat("', '").concat(subDirectoryName).concat("'."));

		// org.apache.log4j.helpers.LogLog.setQuietMode(true);

		// create parser
		IEParser ie = IEParserFactory.create();

		// add ie dc
		DublinCore dc = ie.getDublinCoreParser();
		boolean isAllValid = addDcMetadata(dc, URL, Ue_ID, Ab_ID);

		// falls jetzt schon ein Problem auftaucht, merke das Abstract
		if (!isAllValid) {
			System.err.println("Metadata error entdeckt.");
			ResultSet resultSet2 = SqlManager.INSTANCE
					.executeQuery("SELECT * FROM sonderfaelle WHERE Ab_ID = '" + Ab_ID + "'");
			if (!resultSet2.first()) {
				System.err.println("Wird der Tabelle sonderfaelle hinzugefügt.");
				SqlManager.INSTANCE
						.executeUpdate("INSERT INTO sonderfaelle (Ue_ID, Ab_ID , URL, LANG, Status) VALUES ('" + Ue_ID
								+ "', '" + Ab_ID + "', '" + URL + "', 'both', 52);");
			} else {
				System.err.println("War schon bekannt.");
			}
		}

		ie.setIEDublinCore(dc);

		List<FileGrp> fGrpList = new ArrayList<FileGrp>();

		FileGrp fGrp1 = makeFileGroup(ie, Enum.UsageType.VIEW, "PRESERVATION_MASTER", "1", Ab_ID.concat("_original"));
		fGrpList.add(fGrp1);
		FileGrp fGrp2 = makeFileGroup(ie, Enum.UsageType.VIEW, "PRE_INGEST_MODIFIED_MASTER", "1",
				Ab_ID.concat("_htmlForPdf"));
		fGrpList.add(fGrp2);
		FileGrp fGrp3 = makeFileGroup(ie, Enum.UsageType.VIEW, "MODIFIED_MASTER", "1", Ab_ID.concat("_pdf"));
		fGrpList.add(fGrp3);

		// prepare validation
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document xmlDocument = documentBuilder.newDocument();
		org.w3c.dom.Element rootElement = xmlDocument.createElement("JHOVE_Validationsergebnisse");
		xmlDocument.appendChild(rootElement);

		List<File> dirs = new ArrayList<>();
		dirs.add(new File(filesRootFolder));

		for (int i = 0; i < dirs.size(); i++) {
			for (File file : dirs.get(i).listFiles()) {
				if (file.isDirectory()) {
					dirs.add(file);
				} else {
					String mimeType = endung2mime(file.getName());
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
						System.out.println("Zum Vergleich: '" + filesRootFolder.concat("SourceMD") + "'");
						throw new Exception();
					}
					FileType fileType = ie.addNewFile(fGrp, mimeType,
							file.getAbsolutePath().substring(filesRootFolder.length()), "was ist das?");

					// add dnx - A new DNX is constructed and added on the file level
					DnxDocument dnx = ie.getFileDnx(fileType.getID());
					DnxDocumentHelper fileDocumentHelper = new DnxDocumentHelper(dnx);

					if (file.getName().equals("OAI.xml")) {
						fileDocumentHelper.getGeneralFileCharacteristics().setLabel("OAI Metadaten");
					} else if (file.getName().equals(Ab_ID.concat(".xml"))) {
						fileDocumentHelper.getGeneralFileCharacteristics().setLabel(Ab_ID.concat(" Metadaten"));
					} else {
						String dateiname = file.getName();
						dateiname = dateiname.substring(0, dateiname.lastIndexOf("."));
						fileDocumentHelper.getGeneralFileCharacteristics().setLabel(dateiname);
					}

					fileDocumentHelper.getGeneralFileCharacteristics()
							.setFileOriginalPath(file.getAbsolutePath().substring(subDirectoryName.length()));

					// if (file.getName().endsWith(".tar")) {
					// PreservationLevel pLevel = fileDocumentHelper.new PreservationLevel();
					// pLevel.setPreservationLevelValue("Bitstream Preservation");
					// fileDocumentHelper.setPreservationLevel(pLevel);
					// }

					ie.setFileDnx(fileDocumentHelper.getDocument(), fileType.getID());

					// validate File
					boolean inclVeraPdf = file.getAbsolutePath()
							.contentEquals(Drive.getAbstractPreSipPdf(Ue_ID, Ab_ID, "de"))
							|| file.getAbsolutePath().contentEquals(Drive.getAbstractPreSipPdf(Ue_ID, Ab_ID, "en"));
					if (!isValid(file, xmlDocument, rootElement, subDirectoryName, inclVeraPdf)) {
						isAllValid = false;
						System.err.println("invalide Datei: '" + file.getAbsolutePath() + "'");
					}
				}
			}
		}

		// write validationresults
		DOMSource domSource = new DOMSource(xmlDocument);
		File fileOutput = new File(filesRootFolder.concat("SourceMD").concat(fs).concat("Validation.xml"));
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
		// TODO sourceMD sollte noch hinzugefügt werden
		// Füge UserDefinedA&B hinzu
		GeneralIECharacteristics generalIeCharacteristics = ieDnxHelper.new GeneralIECharacteristics(null, null, null,
				null, null, "GMSKON_" + Ab_ID, userDefinedB, null);
		ieDnxHelper.setGeneralIECharacteristics(generalIeCharacteristics);
		ie.setIeDnx(ieDnxHelper.getDocument());

		// example for adding a logical Struct Map.
		MetsDocument metsDoc = MetsDocument.Factory.parse(ie.toXML());

		// insert IE created in content directory
		File ieXML = new File(IEfullFileName);
		XmlOptions opt = new XmlOptions();
		opt.setSavePrettyPrint();
		String xmlMetsContent = metsDoc.xmlText(opt);

		// Need to replace manually the namespace with Rosetta Mets schema in order to
		// pass validation against mets_rosetta.xsd
		String xmlRosettaMetsContent = xmlMetsContent.replaceAll(XML_SCHEMA, XML_SCHEMA_REPLACEMENT);
		xmlRosettaMetsContent = xmlMetsContent.replaceAll(METS_SCHEMA, ROSETTA_METS_SCHEMA);
		FileUtil.writeFile(ieXML, xmlRosettaMetsContent);

		// validateXML(ieXML.getAbsolutePath(), xmlRosettaMetsContent,
		// ROSETTA_METS_XSD);
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
		} else if (dateiname.endsWith(".png")) {
			mimeType = "image/png";
		} else if (dateiname.endsWith(".avi")) {
			mimeType = "video/x-msvideo";
		} else if (dateiname.endsWith(".mp4")) {
			mimeType = "video/mp4";
		} else {
			System.err.println("Dateiendung nicht erkannt: ".concat(dateiname));
			throw new Exception();
		}
		return mimeType;
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

	private static String url2md(String URL) throws Exception {
		if (!URL.endsWith(".shtml")) {
			System.err.println("URL endet nicht auf .shtml: '".concat(URL).concat("'"));
			throw new Exception();
		}
		if (URL.indexOf("/de/") == -1) {
			System.err.println("URL hat kein /de/: ".concat(URL));
			throw new Exception();
		}
		if (URL.indexOf("/de/") != URL.lastIndexOf("/de/")) {
			System.err.println("URL hat mehr als ein /de/: ".concat(URL));
			throw new Exception();
		}
		return URL.replace(".shtml", ".xml").replace("/de/", "/xml/");
	}

	private static String kuerzel2oai(String Kuerzel) {
		return "https://portal.dimdi.de/oai-gms/OAIHandler?verb=GetRecord&metadataPrefix=oai_dc&identifier=oai:oai-gms.dimdi.de:GM"
				.concat(Kuerzel);
	}

	private static boolean addDcMetadata(DublinCore dc, String URL, String Ue_ID, String Ab_ID) throws Exception {
		boolean ret = true;
		String metadataURL = url2md(URL);
		Document abstractDoc = Utilities.getWebsite(metadataURL);
		if (abstractDoc == null) {
			System.err.println("Metadaten konnten nicht gefunden werden: '".concat(URL).concat("'"));
			throw new Exception();
		}
		Elements elems = abstractDoc.getElementsByTag("GmsArticle");
		if (elems.size() != 1) {
			System.err.println("Es gibt ungleich 1 GmsArticle in der Abstract Metadaten xml: "
					.concat(Integer.toString(elems.size())));
			throw new Exception();
		}
		Element elem = elems.first();
		elems = elem.getElementsByTag("MetaData");
		if (elems.size() != 1) {
			System.err.println("Es gibt ungleich 1 MetaData in der Abstract Metadaten xml: "
					.concat(Integer.toString(elems.size())));
			throw new Exception();
		}
		elem = elems.first();
		elems = elem.getElementsByTag("CreatorList");
		if (elems.size() != 1) {
			System.err.println("Es gibt ungleich 1 CreatorList in der Abstract Metadaten xml: "
					.concat(Integer.toString(elems.size())));
			throw new Exception();
		}
		elem = elems.first();
		elems = elem.getElementsByTag("Creator");
		if (elems.size() == 0) {
			System.err.println("Abstract Metadaten xml hat keine Creator");
			throw new Exception();
		}
		for (Element creatorElement : elems) {
			boolean hasBoth = true;
			String firstname = null;
			elems = creatorElement.getElementsByTag("Firstname");
			if (elems.size() > 1) {
				System.err.println("Creator hat mehr als 1 Firstname: ".concat(Integer.toString(elems.size())));
				throw new Exception();
			} else if (elems.size() == 0) {
				hasBoth = false;
			} else {
				firstname = elems.first().text();
			}
			String lastname = null;
			elems = creatorElement.getElementsByTag("Lastname");
			if (elems.size() > 1) {
				System.err.println("Creator hat mehr als 1 Lastname: ".concat(Integer.toString(elems.size())));
				throw new Exception();
			} else if (elems.size() == 0) {
				hasBoth = false;
			} else {
				lastname = elems.first().text();
			}
			if (hasBoth) {
				dc.addElement("dc:creator", lastname.concat(", ").concat(firstname));
			} else {
				elems = creatorElement.getElementsByTag("Corporation");
				if (elems.size() > 1) {
					System.err.println("Creator hat mehr als 1 Corporation: ".concat(Integer.toString(elems.size())));
					throw new Exception();
				} else if (elems.size() == 0) {
					System.err.println("Creator hat weder Firstname und Lastname, noch Corporation.");
					throw new Exception();
				}
				elem = elems.first();
				elems = elem.getElementsByTag("Corporatename");
				if (elems.size() != 1) {
					System.err.println("Es gibt ungleich 1 Corporatename in der Abstract Metadaten xml: "
							.concat(Integer.toString(elems.size())));
					throw new Exception();
				}
				dc.addElement("dc:creator", elems.first().text());
			}
		}
		dc.addElement("dcterms:isPartOf", "German Medical Science/Meetings/".concat(Ue_ID));
		dc.addElement("dc:identifier", Ab_ID);

		int letzterSlash = URL.lastIndexOf("/");
		String Kuerzel = URL.substring(letzterSlash + 1, URL.length() - ".shtml".length());
		String OaiUrl = kuerzel2oai(Kuerzel);
		Document oaiDoc = null;
		try {
			oaiDoc = Utilities.getWebsite(OaiUrl);
		} catch (Exception e) {
			throw new Exception("Konnte die Metadaten von " + Ab_ID + " nicht abrufen: " + OaiUrl);
		}
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
				dc.addElement("dcterms:issued", mdElem.text());
			} else if (mdElem.tagName().contentEquals("dc:creator")) {
				// nicht übernehmen
			} else if (mdElem.tagName().contentEquals("dc:description")) {
				// nicht übernehmen
			} else {
				dc.addElement(mdElem.tagName(), mdElem.text());
			}
		}
		return ret;
	}

	public static void databaseWorker() throws Exception {
		ResultSet results = SqlManager.INSTANCE
				.executeQuery("SELECT * FROM abstracts WHERE status = 50 AND LANG = 'de';");
		while (results.next()) {
			String Ue_ID = results.getString("Ue_ID");
			String Ab_ID = results.getString("Ab_ID");
			String aURL = results.getString("URL");

			System.out.println("Verarbeite: '".concat(Ab_ID).concat("', URL: '").concat(aURL).concat("'."));

			if (differentSupplements(
					new File(Drive.getAbstractDir(Ue_ID, Ab_ID, "de").concat("Supplementals").concat(fs)),
					Drive.getAbstractDir(Ue_ID, Ab_ID, "en").concat("Supplementals").concat(fs))) {
				System.err.println(
						"Unterschiedliche Supplementals in der deutschen und englischen Version entdeckt. Pfad:");
				System.err.println(Drive.getAbstractDir(Ue_ID, Ab_ID, "de").concat("Supplementals").concat(fs));
				throw new Exception();
			}

			String preSipDir = Drive.getAbstractPreSipDir(Ue_ID, Ab_ID);
			String master = preSipDir.concat("content").concat(fs).concat("streams").concat(fs).concat("1_Master")
					.concat(fs);
			String preIngestModifiedMaster = preSipDir.concat("content").concat(fs).concat("streams").concat(fs)
					.concat("2_derivedFrom1").concat(fs);
			String modifiedMaster = preSipDir.concat("content").concat(fs).concat("streams").concat(fs)
					.concat("3_derivedFrom2").concat(fs);
			File destDir = new File(preSipDir.concat("content").concat(fs).concat("streams").concat(fs));
			if (destDir.exists()) {
				Utilities.deleteDir(destDir);
			}
			destDir.mkdirs();
			new File(master).mkdir();
			new File(preIngestModifiedMaster).mkdir();
			new File(modifiedMaster).mkdir();

			Files.copy(Paths.get(Drive.getAbstractPDF(Ue_ID, Ab_ID, "de")),
					Paths.get(Drive.getAbstractPreSipPdf(Ue_ID, Ab_ID, "de")), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get(Drive.getAbstractPDF(Ue_ID, Ab_ID, "en")),
					Paths.get(Drive.getAbstractPreSipPdf(Ue_ID, Ab_ID, "en")), StandardCopyOption.REPLACE_EXISTING);
			File supps = new File(Drive.getAbstractDir(Ue_ID, Ab_ID, "de").concat("Supplementals").concat(fs));
			if (supps.exists()) {
				Drive.copy(supps, new File(master.concat("Supplementals").concat(fs)));
			}
			supps = new File(Drive.getAbstractDir(Ue_ID, Ab_ID, "de").concat("Supplementals").concat(fs));
			if (supps.exists()) {
				Drive.copy(supps, new File(preIngestModifiedMaster.concat("Supplementals").concat(fs)));
			}
			supps = new File(Drive.getAbstractDir(Ue_ID, Ab_ID, "en").concat("Supplementals").concat(fs));
			if (supps.exists()) {
				Drive.copy(supps, new File(preIngestModifiedMaster.concat("Supplementals").concat(fs)));
			}
			supps = new File(preIngestModifiedMaster.concat("Supplementals").concat(fs));
			if (supps.exists()) {
				Drive.copy(supps, new File(modifiedMaster.concat("Supplementals").concat(fs)));
			}

			String metadataURL = url2md(aURL);
			FileUtils.copyURLToFile(new URL(metadataURL), new File(Drive.getAbstractPreSipWebXml(Ue_ID, Ab_ID)));
			FileUtils.copyURLToFile(new URL(kuerzel2oai(Ab_ID)), new File(Drive.getAbstractPreSipOaiXml(Ue_ID, Ab_ID)));

			// add originals as tar
			String fromString = Drive.getAbstractDir(Ue_ID, Ab_ID, "de").concat("original").concat(fs);
			Tar.createTar(fromString, fromString, master.concat("OriginalHtml_de.tar"));
			// new ZipFile(master.concat("OriginalHtml_de.zip")).addFolder(new
			// File(fromString));
			fromString = Drive.getAbstractDir(Ue_ID, Ab_ID, "en").concat("original").concat(fs);
			// new ZipFile(master.concat("OriginalHtml_en.zip")).addFolder(new
			// File(fromString));
			Tar.createTar(fromString, fromString, master.concat("OriginalHtml_en.tar"));

			// add merged content as tar

			fromString = Drive.getAbstractDir(Ue_ID, Ab_ID, "de").concat("merge").concat(fs);
			// new
			// ZipFile(preIngestModifiedMaster.concat("HtmlForPdf_de.zip")).addFolder(new
			// File(fromString));
			Tar.createTar(fromString, fromString, preIngestModifiedMaster.concat("HtmlForPdf_de.tar"));
			fromString = Drive.getAbstractDir(Ue_ID, Ab_ID, "en").concat("merge").concat(fs);
			// new
			// ZipFile(preIngestModifiedMaster.concat("HtmlForPdf_en.zip")).addFolder(new
			// File(fromString));
			Tar.createTar(fromString, fromString, preIngestModifiedMaster.concat("HtmlForPdf_en.tar"));

			// finish completion of SIP
			boolean isAllValid = processSIP(preSipDir, Ue_ID, Ab_ID, aURL);

			if (isAllValid) {
				// move finished SIP to SIP-Directory
				File sipDir = new File(
						Drive.getAbstractSipDir(rosettaInstance, materialflowID, producerId, Ue_ID, Ab_ID));
				if (sipDir.exists())
					sipDir.delete();
				sipDir.mkdirs();
				Drive.move(new File(preSipDir), sipDir);

				// update Status
				int updated = SqlManager.INSTANCE
						.executeUpdate("UPDATE abstracts SET status = 70 WHERE Ab_ID = '".concat(Ab_ID).concat("';"));
				if (updated != 2) {
					System.err.println("Es sollte sich nun genau zwei Zeilen aktualisiert haben unter dem Kürzel '"
							+ Ab_ID + "', aber es waren: " + updated + ".");
				}
			} else {
				// move finished SIP to SIP-Directory
				System.err.println("SIP invalide");
				File sipDir = new File(
						Drive.getInvalidAbstractSipDir(rosettaInstance, materialflowID, producerId, Ue_ID, Ab_ID));
				if (sipDir.exists())
					sipDir.delete();
				sipDir.mkdirs();
				Drive.move(new File(preSipDir), sipDir);

				// update Status
				int updated = SqlManager.INSTANCE
						.executeUpdate("UPDATE abstracts SET status = 65 WHERE Ab_ID = '".concat(Ab_ID).concat("';"));
				if (updated != 2) {
					System.err.println("Es sollte sich nun genau zwei Zeilen aktualisiert haben unter dem Kürzel '"
							+ Ab_ID + "', aber es waren: " + updated + ".");
				}
			}
		}
	}

	private static boolean differentSupplements(File folder1, String folder2) throws IOException {
		if (!folder1.exists()) {
			return false;
		}
		for (File dat1 : folder1.listFiles()) {
			String dateiname = dat1.getName();
			File dat2 = new File(folder2.concat(dateiname));
			if (!dat2.exists())
				continue;
			if (!FileUtils.contentEquals(dat1, dat2)) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		// final String filesRootFolder =
		// "/home/wutschka/.meetings/Ueberordnungen/gma2016/preSIP/";
		// String HT = "HT020488506";
		// String ID = "gma2016";
		// processSIP(filesRootFolder, HT, ID);
//		String UeO = "dgpraec2016";String Ab = "16dgpraec001";
		String UeO = "dgpraec2015";
		String Ab = "15dgpraec004";
		boolean zuBearbeiten = true;

		if (zuBearbeiten) {
			String sipString = Drive.getAbstractSipDir(rosettaInstance, materialflowID, producerId, UeO, Ab);
			File sipFile = new File(sipString);
			if (sipFile.exists()) {
				Utilities.deleteDir(sipString);
			}
			SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 50 WHERE Ab_ID = '" + Ab + "';");
		} else {
			SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET status = 51 WHERE Ab_ID = '" + Ab + "';");
		}
		databaseWorker();
		System.out.println("AbstractPacker Ende");
	}
}
