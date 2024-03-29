package utilities;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public enum Resources {
	INSTANCE;
	
	public static final String CSS = "de/zbmed/gms-framework.css";
	public static final String CSS2 = "de/zbmed/gms-content.css";
	public static final String LOGO = "de/zbmed/header_logo.png";
	public static final String ICC = "de/zbmed/sRGB_v4_ICC_preference_displayclass.icc";
	public static final String FONT = "de/zbmed/OpenSans-Regular.ttf";
	
	public File getCss() {
		return getResourceFile(CSS);
	}

	public File getCss2() {
		return getResourceFile(CSS2);
	}

	public File getLogo() {
		return getResourceFile(LOGO);
	}

	public File getDisplayIcc() {
		return getResourceFile(ICC);
	}
	
	public File getFont() {
		return getResourceFile(FONT);
	}
	
	private static File getResourceFile(final String name) {
		URL url = Resources.class.getClassLoader().getResource(name);
		if (url == null) {
			throw new IllegalArgumentException("Resource ".concat(name).concat(" could not be found."));
		}
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Resource ".concat(name).concat(" caused an illegal URI problem."), e);
		}
	}
}
