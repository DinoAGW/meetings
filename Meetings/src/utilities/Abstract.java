package utilities;

public class Abstract {
	public String url;
	public String Ue_ID;
	public String Ab_ID;
	
	public Abstract(String url) {
		this.url = url;
		int last = url.lastIndexOf("/");
		int preLast = url.lastIndexOf("/", last-1);
		this.Ue_ID = url.substring(preLast+1, last);
		this.Ab_ID = url.substring(last+1);
	}

}
