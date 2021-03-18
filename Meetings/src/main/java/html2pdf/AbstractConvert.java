package html2pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.attach.impl.OutlineHandler;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.pdfa.PdfADocument;
import utilities.Drive;
import utilities.Resources;
import utilities.SqlManager;

public class AbstractConvert {
	public static String fs = System.getProperty("file.separator");

	public static void main(String[] args) throws IOException, SQLException {
		abstractConvert();
		System.out.println("AbstractConvert Ende.");
	}

	public static void abstractConvert() throws IOException, SQLException {
		ResultSet resultSet = null;

		resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=30");

		int Anzahl = -2 * 1;
		while (resultSet.next()) {
			String Ue_ID = resultSet.getString("Ue_ID");
			String Ab_ID = resultSet.getString("Ab_ID");
			String URL = resultSet.getString("URL");
			String LANG = resultSet.getString("LANG");
			System.out.println("Verarbeite: '".concat(Ue_ID).concat("', '").concat(Ab_ID).concat("', '").concat(URL)
					.concat("', '").concat(LANG).concat("'"));
			String baseDir = Drive.getAbstractsMergeDir(Ue_ID, Ab_ID, LANG);
			String from = Drive.getAbstractsMergeHtml(Ue_ID, Ab_ID, LANG);
			String to = Drive.getAbstractPDF(Ue_ID, Ab_ID, LANG);

			Document doc = Jsoup.parse(new File(from), "CP1252", baseDir);
			doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
			doc.outputSettings().charset("CP1252");

			ConverterProperties properties = new ConverterProperties();
			properties.setBaseUri(baseDir);// braucht man, weil String übergeben wird, statt File

			OutlineHandler outlineHandler = OutlineHandler.createStandardHandler();
			properties.setOutlineHandler(outlineHandler);

			DefaultFontProvider fontProvider = new DefaultFontProvider(false, true, false);// register... StandardPdf,
			// ShippedFree, System
			// ...Fonts
			properties.setFontProvider(fontProvider);

			PdfWriter writer = new PdfWriter(to, new WriterProperties().addXmpMetadata());

			PdfDocument pdf = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_2A, new PdfOutputIntent("Custom", "",
					"http://www.color.org", "sRGB IEC61966-2.1", new FileInputStream(Resources.INSTANCE.getDisplayIcc())));

			pdf.setDefaultPageSize(PageSize.A4);

			pdf.setTagged();

			PrintStream stderr = System.err;
			System.setErr(new PrintStream(new FileOutputStream("Error.log")));
			HtmlConverter.convertToPdf(doc.html(), pdf, properties);
			System.setErr(stderr);

			int updated = SqlManager.INSTANCE.executeUpdate("UPDATE abstracts SET Status = 50 WHERE Ab_ID = '"
					.concat(Ab_ID).concat("' AND LANG = '").concat(LANG).concat("';"));
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID = '"
						.concat(Ab_ID).concat("' und LANG = '").concat(LANG).concat("', aber es waren: ")
						.concat(Integer.toString(updated)).concat("."));

			if (0 == --Anzahl)
				break; // Tu nicht zu viel
		}
	}

}
