import java.io.File;

import utilities.StatusFile;

public class SipChecker {
	static final String fs = System.getProperty("file.separator");
	public static final String sipPath = System.getProperty("user.home").concat(fs).concat("workspace").concat(fs)
			.concat("metsSIPs").concat(fs);
	
	public static void showNotDones() throws Exception {
		File sipPathFile = new File(sipPath);
		File[] insts = sipPathFile.listFiles();
		if (insts == null) {
			System.err.println("Keine Unterordner unter '".concat(sipPath).concat("' gefunden."));
			throw new Exception();
		}
		int errorSips = 0;
		int doneSips = 0;
		int restSips = 0;
		int nasSips = 0;
		int minSipId = 0;
		int maxSipId = 0;
		for (File inst : insts) {
			if (inst.isDirectory()) {
				String instName = inst.getName();
				String rosettaInstance = null;
				if (instName.equals("dev")) {
					rosettaInstance = "dev";
				} else if (instName.equals("test")) {
					rosettaInstance = "test";
				} else if (instName.equals("prod")) {
					rosettaInstance = "prod";
				} else {
					continue;
				}
				File[] matIds = inst.listFiles();
				for (File matId : matIds) {
					String materialflowId = matId.getName();
					File[] prodIDs = matId.listFiles();
					for (File prodID : prodIDs) {
						String producerId = prodID.getName();
						File[] sips = prodID.listFiles();
						for (File sip : sips) {
							String subDirectoryName = sip.getAbsolutePath().concat("/");
							StatusFile statusFile = new StatusFile(new File(subDirectoryName));
							if(!statusFile.exists()) {
								++nasSips;
								System.out.println(
										"Ohne: '".concat(subDirectoryName).concat("' in '").concat(rosettaInstance).concat("'."));
							} else {
								if (statusFile.status.contentEquals("ERROR")) {
									++errorSips;
									System.err.println(
											"Error: '".concat(subDirectoryName).concat("' in '").concat(rosettaInstance).concat("'."));
								} else if (statusFile.status.contentEquals("DONE")) {
									++doneSips;
									System.out.println(
											"Done: '".concat(subDirectoryName).concat("' in '").concat(rosettaInstance).concat("'."));
									utilities.PropertiesManager prop = new utilities.PropertiesManager(subDirectoryName.concat("DONE.txt"));
									int sipId = Integer.valueOf(prop.readStringFromProperty("sipID"));
									if (minSipId > sipId || minSipId == 0) minSipId = sipId;
									if (maxSipId < sipId || maxSipId == 0) maxSipId = sipId;
								} else {
									++restSips;
									System.out.println(
											"Rest: '".concat(subDirectoryName).concat("' in '").concat(rosettaInstance).concat("'."));
								}
							}
						}
					}
				}
			}
		}
		System.out.println("Insgesamt " + doneSips + " SIPs mit Status DONE");
		System.out.println("Insgesamt " + errorSips + " SIPs mit Status ERROR");
		System.out.println("Insgesamt " + nasSips + " SIPs ohne Status");
		System.out.println("Insgesamt " + restSips + " SIPs mit anderem Status");
		System.out.println("SipIDs reichen von " + minSipId + " bis " + maxSipId + " .");
	}

	
	public static void main(String[] args) throws Exception {
		showNotDones();
	}

}
