package SIP;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;
import SIP.MetadataExtractor;

import gov.loc.mets.MetsType.FileSec.FileGrp;

public class UeberordnungPacker {

	public static void processSIP(String filesRootFolder, String HT) throws Exception {
		//list of files we are depositing
		File streamDir = new File(filesRootFolder);
		File[] files = streamDir.listFiles();

		//create parser
		IEParser ie = IEParserFactory.create();

		// add ie dc
		DublinCore dc = ie.getDublinCoreParser();
		MetadataExtractor.extractMetadata(dc, HT);
		ie.setIEDublinCore(dc);
		List<FileGrp> fGrpList = new ArrayList<FileGrp>();
	}

	public static void main(String[] args) throws Exception {
		final String filesRootFolder = "";
		String HT = "";
		processSIP(filesRootFolder, HT);

	}
}
