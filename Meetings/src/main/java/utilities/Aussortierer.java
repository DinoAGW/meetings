package utilities;

import java.io.File;
import java.util.Scanner;

public class Aussortierer {
	
	private static void loescheSIPsViaCsvListe(String csvFilePath) throws Exception {
		File csvFile = new File(csvFilePath);
		Scanner csvScanner = new Scanner(csvFile);
		int existiert = 0;
		int existiertNicht = 0;
		while (csvScanner.hasNext()) {
			String sip = csvScanner.next();
			String testSip = "/home/wutschka/workspace/metsSIPs/validSIPs/upload/" + sip;
			if (new File(testSip).exists()) {
				//System.out.println(sip + " " + testSip);
				String delSip = "/home/wutschka/workspace/metsSIPs/validSIPs/upload/del/" + sip;
				Drive.move(new File(testSip), new File(delSip));
				++existiert;
			} else {
				//System.out.println(sip + " " + testSip);
				++existiertNicht;
			}
		}
		System.out.println("existiert = " + existiert + " | existiert nicht = " + existiertNicht);
		csvScanner.close();
	}

	public static void main(String[] args) throws Exception {
		loescheSIPsViaCsvListe("/home/wutschka/workspace/Uebertragen.csv");
	}
}
