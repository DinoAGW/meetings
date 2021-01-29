package utilities;

import java.net.MalformedURLException;
import java.net.URL;

public class Kongress {
	public final URL url;
	public final String kurzID;
	public final String language;
	public final String languageSpec;
	
	public static String url2kuerzel(URL UrlUrl) {
		return url2kuerzel(UrlUrl.toString());
	}
		
	public static String url2kuerzel(String StringUrl) {
		String[] tokens = new String[10];
		tokens = StringUrl.split("/");
		int tokenOffset = (StringUrl.endsWith("/")) ? 1 : 2;
		return tokens[tokens.length - tokenOffset];
	}

	public static String url2language(String StringUrl) {
		String[] tokens = new String[10];
		tokens = StringUrl.split("/");
		int tokenOffset = (StringUrl.endsWith("/")) ? 1 : 2;
		return tokens[tokens.length - 2 - tokenOffset];
		
	}

	public Kongress(String url) throws MalformedURLException {
		this.url = new URL(url);
		this.kurzID = url2kuerzel(url);
		this.language = url2language(url);
		this.languageSpec = (this.language.equals("de")) ? "" :	"_".concat(this.language);
	}

	@Override
	public String toString() {
		return "Kongress [url=" + url + ", kurzID=" + kurzID + ", language=" + language + ", languageSpec="
				+ languageSpec + "]";
	}
}
