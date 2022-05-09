package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VeraPDF {
	
	public static String veraPDF(String flavour, String file) throws IOException {
		String veraPdfCmd = System.getProperty("user.home") + "/verapdf/verapdf";
		String[] veraPdfParams = new String[] {veraPdfCmd, "-f", flavour, "--format", "mrr", file};
		ProcessBuilder pb = new ProcessBuilder(veraPdfParams);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = "";
		String lines = "";
		while ((line = inStreamReader.readLine()) != null) {
			lines = lines.concat(line).concat("\n");
		}
		return lines;
	}
	
	public static String singleValidationReport (String lines) {
		String ret = lines.substring(lines.indexOf("<validationReport") + 18);
		ret = ret.substring(0, ret.indexOf("\n") - 1);
		return ret;
	}
	
	public static boolean singleWellFormedAndValid(String lines) throws Exception {
		return singleValidationReport(lines).contains("isCompliant=\"true\"");
	}
	
	public static void printVeraPdf(String file) throws Exception {
		String lines = veraPDF("0", file);
		System.out.println("Ergebnis:");
		int detailsBegin = lines.indexOf("<details");
		int detailsContentBegin = lines.indexOf(">", detailsBegin) + 1;
		int detailsEnd = lines.indexOf("</details>", detailsContentBegin);
		String lines2 = lines.substring(0, detailsContentBegin).concat("[...]").concat(lines.substring(detailsEnd));
		System.out.println(lines2);
		System.out.println("validationReport:");
		System.out.println(singleValidationReport(lines));
		System.out.println("wellFormedAndValid:");
		System.out.println(singleWellFormedAndValid(lines));
	}

	public static void main(String[] args) throws Exception {
		String filesRootFolder = System.getProperty("user.home") + "/test.pdf";
		printVeraPdf(filesRootFolder);
		filesRootFolder = System.getProperty("user.home") + "/verapdf/wkhtmlto.pdf";
		printVeraPdf(filesRootFolder);
		System.out.println("Jhove.main() Ende");
	}

}

//	public static String errorMessage(String lines) throws Exception {
//		String search = "  ErrorMessage: ";
//		for (String line : lines.split("\n")) {
//			if (line.startsWith(search)) {
//				return line.substring(search.length());
//			}
//		}
//		return "No ErrorMessage";
//	}
