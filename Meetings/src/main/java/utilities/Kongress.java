package utilities;

import java.net.MalformedURLException;
import java.net.URL;

public class Kongress {
	public final URL url;
	public final String kurzID;
	public final String language;
	public final String languageSpec;

	public Kongress(String url) throws MalformedURLException {
		this.url = new URL(url);
		String[] tokens = new String[10];
		tokens = this.url.toString().split("/");
		int tokenOffset = (this.url.toString().endsWith("/")) ? 1 : 2;
		this.kurzID = tokens[tokens.length - tokenOffset];
		this.language = tokens[tokens.length - 2 - tokenOffset];
		this.languageSpec = (this.language.equals("de")) ? "" :	"_".concat(this.language);
	}

	public String getPathId() {
		return this.kurzID.concat(this.languageSpec);
	}

	@Override
	public String toString() {
		return "Kongress [url=" + url + ", kurzID=" + kurzID + ", language=" + language + ", languageSpec="
				+ languageSpec + "]";
	}
}
