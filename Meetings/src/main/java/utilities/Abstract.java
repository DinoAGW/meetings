package utilities;

public class Abstract {
	public String url;
	public String Ue_ID;
	public String Ab_ID;
	public String language;
	public String languageSpec;

	public Abstract(String url) {
		this.url = url;
		String[] tokens = new String[10];
		tokens = this.url.split("/");
		this.Ue_ID = tokens[tokens.length - 2];
		this.Ab_ID = tokens[tokens.length - 1];
		this.Ab_ID = this.Ab_ID.substring(0, this.Ab_ID.indexOf("."));
		this.language = tokens[tokens.length - 4];
		this.languageSpec = "";
		if (!this.language.equals("de"))
			languageSpec += "_" + this.language;
	}

}
