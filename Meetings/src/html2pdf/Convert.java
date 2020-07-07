package html2pdf;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.attach.impl.OutlineHandler;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;

import utilities.Kongress;
import utilities.SqlManager;
import utilities.Utilities;

public class Convert {
	public static String fs = System.getProperty("file.separator");
	public static final String ICC = "resources/sRGB_v4_ICC_preference_displayclass.icc";
	public static final String FONT = "resources/OpenSans-Regular.ttf";

	public static void main(String[] args) throws IOException, SQLException {
		String mainPath = "C:\\Users\\hixel\\workspace\\Meetings\\Ueberordnungen\\";

		String propertypfad = System.getProperty("user.home") + fs + "properties.txt";
		String password = Utilities.readStringFromProperty(propertypfad, "password");
		SqlManager sqlManager = new SqlManager("jdbc:mariadb://localhost/meetings", "root", password);
		ResultSet resultSet = null;

		resultSet = sqlManager.executeSql("SELECT * FROM urls WHERE status=30");

		while (resultSet.next()) {
			System.out.println("Verarbeite: '" + resultSet.getString("ID") + "', '" + resultSet.getString("URL") + "'");

			Kongress it = new Kongress(resultSet.getString("URL"));
			String kongressDir = mainPath + "kongresse" + fs + it.kurzID + fs;
			String from = kongressDir + "merge" + fs + "content" + fs + "target.html";
			String url = it.url;
			String to = kongressDir + it.kurzID + ".pdf";

			Document doc = Jsoup.parse(new File(from), "CP1252", url);
			doc.outputSettings().charset("CP1252");

			ConverterProperties properties = new ConverterProperties();
			properties.setBaseUri(url);//braucht man, weil String übergeben wird, statt File

			OutlineHandler outlineHandler = OutlineHandler.createStandardHandler();
			properties.setOutlineHandler(outlineHandler);

			DefaultFontProvider fontProvider = new DefaultFontProvider(false, true, false);//register... StandardPdf, ShippedFree, System ...Fonts
			properties.setFontProvider(fontProvider);

			PdfWriter writer = new PdfWriter(to, new  WriterProperties().addXmpMetadata());

			PdfDocument pdf = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_2B,
					new PdfOutputIntent("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", new FileInputStream(ICC)));

			pdf.setDefaultPageSize(PageSize.A4);

			pdf.setTagged();

			PrintStream stderr = System.err;
			System.setErr(new PrintStream(new FileOutputStream("Error.log")));
			HtmlConverter.convertToPdf(doc.html(), pdf, properties);
			System.setErr(stderr);

			break; // Tu nicht zu viel
		}

		System.out.println("Convert Ende.");
	}

}
