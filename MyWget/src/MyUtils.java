import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.codec.digest.DigestUtils;

public class MyUtils {
	public static String md5_of_file(File file) throws IOException {
		return DigestUtils.md5Hex(new FileInputStream(file));
	}

	public static boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	/**
	 * gibt aus wie oft ein of Zeichen innerhalb einer Zeichenkette in vorkommt
	 * 
	 * @param in Zeichenkette
	 * @param of Zeichen
	 * @return count
	 */
	public static int countOccrences(String in, char of) {
		int ret = 0;
		for (int i = 0; i < in.length(); i++) {
			if (in.charAt(i) == of)
				ret++;
		}
		return ret;
	}
	
	/**
	 * Kopiert rekursiv ganze Ordnerstrukturen und überschreibt dabei alles, was es schon gibt
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdirs();
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				
				copyFolder(srcFile, destFile);
			}
		} else {
			Files.copy(src.toPath(),  dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

}
