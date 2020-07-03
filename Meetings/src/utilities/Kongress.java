package utilities;

public class Kongress {
	public String url;
	public String kurzID;

	public Kongress(String url) {
		this.url = new String(url);
		String[] tokens = new String[10];
		tokens = this.url.split("/");
		this.kurzID = tokens[tokens.length - 2];
	}
}
