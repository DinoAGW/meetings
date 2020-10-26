package utilities;

import java.net.MalformedURLException;
import java.net.URL;


public class Abstract {
	public final static String fs = System.getProperty("file.separator");

	public final URL url;
	public final String Ue_ID;
	public final String Ab_ID;
	public final String language;
	public final String languageSpec;

	public Abstract(final String url) throws MalformedURLException {
		this.url = new URL(url);
		String[] tokens = new String[10];
		tokens = this.url.toString().split("/");
		this.Ue_ID = tokens[tokens.length - 2];
		this.Ab_ID = tokens[tokens.length - 1].substring(0, tokens[tokens.length - 1].indexOf("."));
		this.language = tokens[tokens.length - 4];
		this.languageSpec = (this.language.equals("de")) ? "" : "_".concat(this.language);
	}

	public String getPathId() {
		return this.Ue_ID.concat(this.languageSpec).concat(fs).concat(this.Ab_ID); 
	}
}
