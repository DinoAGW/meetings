import java.io.IOException;
import java.sql.SQLException;

import linkCrawl.LinkCrawl;
import linkDownload.LinkDownload;
import utilities.Clean;

public class testing {

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		Clean.main(args);
		LinkCrawl.main(args);
		LinkDownload.main(args);

	}

}
