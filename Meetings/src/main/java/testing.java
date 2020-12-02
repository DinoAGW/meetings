import java.io.IOException;
import java.sql.SQLException;

import html2pdf.AbstractConvert;
import html2pdf.UeberordnungConvert;
import linkCrawl.LinkCrawl;
import linkDownload.AbstractDownload;
import linkDownload.UeberordnungDownload;
import utilities.Clean;

public class testing {

	public static void main(String[] args) throws IOException, SQLException, InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		System.out.println("testing Started");
//		System.out.println("Clean");
//		Clean.main(args);
		System.out.println("LinkCrawl");
		LinkCrawl.main(args);
		System.out.println("UeberordnungDownload");
		UeberordnungDownload.main(args);
		System.out.println("UeberordnungConvert");
		UeberordnungConvert.main(args);
		System.out.println("AbstractDownload");
		AbstractDownload.main(args);
		System.out.println("AbstractConvert");
		AbstractConvert.main(args);
		System.out.println("testing Ende");
	}

}
