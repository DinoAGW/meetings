package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class Tar {
	public static void createTar(String root, String from, String to) throws ArchiveException, IOException {
		OutputStream fos = new FileOutputStream(new File(to));
		TarArchiveOutputStream tarOs = new TarArchiveOutputStream(fos);
		tarOs.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
		addToArchive(root.length(), new File(from), tarOs);
		tarOs.finish();
		tarOs.close();
	}

	private static void addToArchive(int rootLength, File toBeAdded, ArchiveOutputStream my_tar_ball)
			throws IOException {
//		System.out.println("File: " + toBeAdded);
		if (toBeAdded.getName().endsWith(".tar"))
			return;
		if (toBeAdded.isFile()) {
			TarArchiveEntry tar_file = new TarArchiveEntry(toBeAdded.getAbsolutePath().substring(rootLength));
			tar_file.setSize(toBeAdded.length());
			my_tar_ball.putArchiveEntry(tar_file);
			IOUtils.copy(new FileInputStream(toBeAdded), my_tar_ball);
			my_tar_ball.closeArchiveEntry();
		} else {
			for (File tar_input_file : toBeAdded.listFiles()) {
				addToArchive(rootLength, tar_input_file, my_tar_ball);
			}
		}
	}

	public static void main(String[] args) throws ArchiveException, IOException {
		String from = "/home/wutschka/Downloads/";
		String to = "/home/wutschka/Downloads/archiv.tar";
		createTar(from, from, to);
		System.out.println("Tar Ende");
	}
}
