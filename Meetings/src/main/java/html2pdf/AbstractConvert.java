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

import utilities.Abstract;
import utilities.SqlManager;

public class AbstractConvert {
	public static String fs = System.getProperty("file.separator");
	public static final String ICC = "resources/sRGB_v4_ICC_preference_displayclass.icc";
	public static final String FONT = "resources/OpenSans-Regular.ttf";

	public static void main(String[] args) throws IOException, SQLException {
		String mainPath = "C:\\Users\\hixel\\workspace\\Meetings\\Abstracts\\";

		ResultSet resultSet = null;

		resultSet = SqlManager.INSTANCE.executeSql("SELECT * FROM abstracts WHERE status=30");

		int Anzahl = 2 * 1;
		while (resultSet.next()) {
			System.out.println("Verarbeite: '" + resultSet.getString("Ue_ID") + "', '" + resultSet.getString("Ab_ID")
					+ "', '" + resultSet.getString("URL") + "'");

			Abstract it = new Abstract(resultSet.getString("URL"));
			String kongressDir = mainPath + "kongresse" + fs + it.Ue_ID + it.languageSpec + fs + it.Ab_ID + fs;
			String baseDir = kongressDir + "merge" + fs + "content" + fs;
			String from = baseDir + "target.html";
			String to = kongressDir + it.Ab_ID + it.languageSpec + ".pdf";

			Document doc = Jsoup.parse(new File(from), "CP1252", baseDir);
			doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
			doc.outputSettings().charset("CP1252");

			ConverterProperties properties = new ConverterProperties();
			properties.setBaseUri(baseDir);// braucht man, weil String �bergeben wird, statt File

			OutlineHandler outlineHandler = OutlineHandler.createStandardHandler();
			properties.setOutlineHandler(outlineHandler);

			DefaultFontProvider fontProvider = new DefaultFontProvider(false, true, false);// register... StandardPdf,
																							// ShippedFree, System
																							// ...Fonts
			properties.setFontProvider(fontProvider);

			PdfWriter writer = new PdfWriter(to, new WriterProperties().addXmpMetadata());

			PdfDocument pdf = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_2A, new PdfOutputIntent("Custom", "",
					"http://www.color.org", "sRGB IEC61966-2.1", new FileInputStream(ICC)));

			pdf.setDefaultPageSize(PageSize.A4);

			pdf.setTagged();

			PrintStream stderr = System.err;
			System.setErr(new PrintStream(new FileOutputStream("Error.log")));
			HtmlConverter.convertToPdf(doc.html(), pdf, properties);
			System.setErr(stderr);

			int updated = SqlManager.INSTANCE
					.executeUpdate("UPDATE abstracts SET Status = 50 WHERE Ab_ID = '" + it.Ab_ID + "_" + it.language + "';");
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID '"
						+ it.Ab_ID + "_" + it.language + "', aber es waren: " + updated + ".");

			if (0 == --Anzahl)
				break; // Tu nicht zu viel
		}

		System.out.println("AbstractConvert Ende.");
	}

}
