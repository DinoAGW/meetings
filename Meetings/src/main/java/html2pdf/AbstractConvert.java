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
import utilities.Clean;
import utilities.Resources;
import utilities.SqlManager;

public class AbstractConvert {
	public static String fs = System.getProperty("file.separator");

	public static void main(String[] args) throws IOException, SQLException {
		String absPath = Clean.mainPath.concat("Abstracts").concat(fs);

		ResultSet resultSet = null;

		resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM abstracts WHERE status=30");

		int Anzahl = 2 * 1;
		while (resultSet.next()) {
			System.out.println("Verarbeite: '".concat(resultSet.getString("Ue_ID")).concat("', '").concat(resultSet.getString("Ab_ID")).concat("', '").concat(resultSet.getString("URL")).concat("'"));

			Abstract it = new Abstract(resultSet.getString("URL"));
			String kongressDir = absPath.concat(it.getPathId()).concat(fs);
			String baseDir = kongressDir.concat("merge").concat(fs).concat("content").concat(fs);
			String from = baseDir.concat("target.html");
			String to = kongressDir.concat(it.Ab_ID).concat(it.languageSpec).concat(".pdf");

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
					"http://www.color.org", "sRGB IEC61966-2.1", new FileInputStream(Resources.INSTANCE.getDisplayIcc())));

			pdf.setDefaultPageSize(PageSize.A4);

			pdf.setTagged();

			PrintStream stderr = System.err;
			System.setErr(new PrintStream(new FileOutputStream("Error.log")));
			HtmlConverter.convertToPdf(doc.html(), pdf, properties);
			System.setErr(stderr);

			int updated = SqlManager.INSTANCE
					.executeUpdate("UPDATE abstracts SET Status = 50 WHERE Ab_ID = '".concat(it.Ab_ID).concat("_").concat(it.language).concat("';"));
			if (updated != 1)
				System.err.println("Es sollte sich nun genau eine Zeile aktualisiert haben unter der KurzID '".concat(it.Ab_ID).concat("_").concat(it.language).concat("', aber es waren: ").concat(Integer.toString(updated)).concat("."));

			if (0 == --Anzahl)
				break; // Tu nicht zu viel
		}

		System.out.println("AbstractConvert Ende.");
	}

}
