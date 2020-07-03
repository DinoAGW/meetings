package utilities;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Properties;

public class Utilities {

	public static String readline() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String ausgelesen = in.readLine();
		return ausgelesen;
	}

	public static void saveStringToProperty(String ziel, String prop, String value) throws IOException {
		Writer writer = new FileWriter(ziel);
		Properties properties = new Properties();
		properties.setProperty(prop, value);
		properties.store(writer, "default Kommentar");
		writer.close();
	}

	public static String readStringFromProperty(String quelle, String prop) throws IOException {
		Properties properties = new Properties();
		BufferedInputStream reader = new BufferedInputStream(new FileInputStream(quelle));
		properties.load(reader);
		reader.close();
		return properties.getProperty(prop);
	}
}
