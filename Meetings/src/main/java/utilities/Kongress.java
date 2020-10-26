package utilities;

public class Kongress {
	public String url;
	public String kurzID;
	public String language;
	public String languageSpec;

	public Kongress(String url) {
		this.url = new String(url);
		String[] tokens = new String[10];
		tokens = this.url.split("/");
		int tokenOffset = (this.url.endsWith("/")) ? 1 : 2;
		this.kurzID = tokens[tokens.length - tokenOffset];
		this.language = tokens[tokens.length - 2 - tokenOffset];
		this.languageSpec = "";
		if (!this.language.equals("de"))
			languageSpec += "_" + this.language;
	}

	@Override
	public String toString() {
		return "Kongress [url=" + url + ", kurzID=" + kurzID + ", language=" + language + ", languageSpec="
				+ languageSpec + "]";
	}

}
