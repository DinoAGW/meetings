import metsSipCreator.FILE;
import metsSipCreator.REP;
import metsSipCreator.SIP;

public class testMetsSipCreator {
	private static final String fs = System.getProperty("file.separator");
	
	private static final String home = System.getProperty("user.home").concat(fs);
	private static final String workspace = home.concat("workspace").concat(fs);
	private static final String agmb2020 = workspace.concat("agmb2020").concat(fs);
	private static final String agmb2020contentStreams = agmb2020.concat("content").concat(fs).concat("streams").concat(fs);
	
	private static void createAgmb2020() throws Exception {
		SIP sip = new SIP();
		sip.addMetadata("dc:identifier", "HT020566828");
		sip.addMetadata("dc:identifier", "http://www.egms.de/de/meetings/agmb2020/");
		sip.addMetadata("dc:identifier", "DPS:okeanos-z39:HBZ01:036760060:HT020566828");
		sip.addMetadata("dc:title", "Jahrestagung der Arbeitsgemeinschaft für Medizinisches Bibliothekswesen (AGMB)");
		sip.addMetadata("dc:title", "21.-22.09.2020");
		sip.addMetadata("dc:creator", "AGMB-Jahrestagung 2020 Online (DE-588)1216889694");
		sip.addMetadata("dc:publisher", "Düsseldorf German Medical Science GMS Publishing House 27. August 2020");
		sip.addMetadata("dc:date", "2020");
		sip.addMetadata("dc:type", "conferenceObject");
		sip.addMetadata("dc:format", "1 Online-Ressource");
		
		REP rep1 = sip.newREP(null);
		rep1.setLabel("agmb2020_original");
		FILE file1_1 = rep1.newFile(agmb2020contentStreams.concat("SourceMD").concat(fs).concat("SRU.xml"), "SourceMD".concat(fs), null);
		file1_1.setLabel("SRU Metadaten");
		FILE file2_1 = rep1.newFile(agmb2020contentStreams.concat("1_Master").concat(fs).concat("OriginalHtml_de.tar"), "1_Master".concat(fs), null);
		file2_1.setLabel("OriginalHtml_de");
		FILE file3_1 = rep1.newFile(agmb2020contentStreams.concat("1_Master").concat(fs).concat("OriginalHtml_en.tar"), "1_Master".concat(fs), null);
		file3_1.setLabel("OriginalHtml_en");
		
		REP rep2 = sip.newREP("PRE_INGEST_MODIFIED_MASTER");
		rep2.setLabel("agmb2020_htmlForPdf");
		FILE file1_2 = rep2.newFile(agmb2020contentStreams.concat("2_derivedFrom1").concat(fs).concat("HtmlForPdf_en.tar"), "2_derivedFrom1".concat(fs), null);
		file1_2.setLabel("HtmlForPdf_en");
		FILE file2_2 = rep2.newFile(agmb2020contentStreams.concat("2_derivedFrom1").concat(fs).concat("HtmlForPdf_de.tar"), "2_derivedFrom1".concat(fs), null);
		file2_2.setLabel("HtmlForPdf_de");
		
		REP rep3 = sip.newREP("MODIFIED_MASTER");
		rep3.setLabel("agmb2020_pdf");
		FILE file1_3 = rep3.newFile(agmb2020contentStreams.concat("3_derivedFrom2").concat(fs).concat("agmb2020_de.pdf"), "3_derivedFrom2".concat(fs), null);
		file1_3.setLabel("agmb2020_de");
		FILE file2_3 = rep3.newFile(agmb2020contentStreams.concat("3_derivedFrom2").concat(fs).concat("agmb2020_en.pdf"), "3_derivedFrom2".concat(fs), null);
		file2_3.setLabel("agmb2020_en");
		
		sip.setCMS("HBZ01", "HT020566828");
		
		sip.setUserDefined("A", "ich bins").setUserDefined("B", "20230202");
		
		sip.deploy("bin" + fs + "agmb2020");
	}
	
	private static final String agmb2020_20agmb01 = workspace.concat("agmb2020_20agmb01").concat(fs);
	private static final String agmb2020_20agmb01contentStreams = agmb2020_20agmb01.concat("content").concat(fs).concat("streams").concat(fs);
	
	private static void createAgmb2020_20agmb01() throws Exception {
		SIP sip = new SIP();
		sip.addMetadata("dc:creator", "Schmitz, Jasmin").addMetadata("dcterms:isPartOf", "German Medical Science/Meetings/agmb2020");
		sip.addMetadata("dc:identifier", "20agmb01");
		sip.addMetadata("dc:title", "Was ist Open Science?");
		sip.addMetadata("dc:subject", "ddc: 610");
		sip.addMetadata("dc:publisher", "German Medical Science GMS Publishing House, Düsseldorf");
		sip.addMetadata("dcterms:issued", "2020-08-27");
		sip.addMetadata("dc:type", "conferenceObject");
		sip.addMetadata("dc:identifier", "http://dx.doi.org/10.3205/20agmb01");
		sip.addMetadata("dc:identifier", "http://nbn-resolving.de/urn:nbn:de:0183-20agmb011");
		sip.addMetadata("dc:identifier", "http://www.egms.de/en/meetings/agmb2020/20agmb01.shtml");
		sip.addMetadata("dc:source", "Jahrestagung der Arbeitsgemeinschaft für medizinisches Bibliothekswesen (AGMB); 20200921-20200922; sine loco [digital]; DOC20agmb01 /20200827/");
		sip.addMetadata("dc:language", "deu");
		sip.addMetadata("dc:rights", "http://creativecommons.org/licenses/by/4.0/");
		
		REP rep1 = sip.newREP(null).setLabel("20agmb01_original");
		rep1.newFile(agmb2020_20agmb01contentStreams.concat("SourceMD").concat(fs).concat("OAI.xml"), "SourceMD".concat(fs), null).setLabel("OAI Metadaten");
		rep1.newFile(agmb2020_20agmb01contentStreams.concat("SourceMD").concat(fs).concat("20agmb01.xml"), "SourceMD".concat(fs), null).setLabel("20agmb01 Metadaten");
		rep1.newFile(agmb2020_20agmb01contentStreams.concat("1_Master").concat(fs).concat("OriginalHtml_de.tar"), "1_Master".concat(fs), null).setLabel("OriginalHtml_de");
		rep1.newFile(agmb2020_20agmb01contentStreams.concat("1_Master").concat(fs).concat("OriginalHtml_en.tar"), "1_Master".concat(fs), null).setLabel("OriginalHtml_en");
		
		REP rep2 = sip.newREP("PRE_INGEST_MODIFIED_MASTER").setLabel("20agmb01_htmlForPdf");
		rep2.newFile(agmb2020_20agmb01contentStreams.concat("2_derivedFrom1").concat(fs).concat("HtmlForPdf_en.tar"), "2_derivedFrom1".concat(fs), null).setLabel("HtmlForPdf_en");
		rep2.newFile(agmb2020_20agmb01contentStreams.concat("2_derivedFrom1").concat(fs).concat("HtmlForPdf_de.tar"), "2_derivedFrom1".concat(fs), null).setLabel("HtmlForPdf_de");
		
		REP rep3 = sip.newREP("MODIFIED_MASTER").setLabel("20agmb01_pdf");
		rep3.newFile(agmb2020_20agmb01contentStreams.concat("3_derivedFrom2").concat(fs).concat("20agmb01_en.pdf"), "3_derivedFrom2".concat(fs), null).setLabel("20agmb01_en");
		rep3.newFile(agmb2020_20agmb01contentStreams.concat("3_derivedFrom2").concat(fs).concat("20agmb01_de.pdf"), "3_derivedFrom2".concat(fs), null).setLabel("20agmb01_de");
		
		sip.deploy("bin" + fs + "agmb2020_20agmb01");
	}

	public static void main(String[] args) throws Exception {
		createAgmb2020();
//		createAgmb2020_20agmb01();
		System.out.println("testMetsSipCreator Ende");
	}

}
