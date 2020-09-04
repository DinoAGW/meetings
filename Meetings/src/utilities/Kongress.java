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
		if (this.url.endsWith("/"))
			tokens[tokens.length] = "index.html";
		this.kurzID = tokens[tokens.length - 2];
		this.language = tokens[tokens.length - 4];
		this.languageSpec = "";
		if (!this.language.equals("de"))
			languageSpec += "_" + this.language;
	}
}
