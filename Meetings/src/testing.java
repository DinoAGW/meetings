import java.io.IOException;
import java.sql.SQLException;

import html2pdf.AbstractConvert;
import html2pdf.UeberordnungConvert;
import linkCrawl.LinkCrawl;
import linkDownload.AbstractDownload;
import linkDownload.UeberordnungDownload;
import utilities.Clean;

public class testing {

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		Clean.main(args);
		LinkCrawl.main(args);
		UeberordnungDownload.main(args);
		AbstractDownload.main(args);
		UeberordnungConvert.main(args);
		AbstractConvert.main(args);
	}

}
