package utilities;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.io.File;

import org.junit.Test;

public class ResourcesTest {
    @Test
    public void testGetResourceMissingFile() {
        assertThrows("IllegalArgumentException expected",
                IllegalArgumentException.class,
                () -> {
                    Resources.getResourceFile("bad/path.css");
                });
    }

    @Test
    public void testGetResourceNullFile() {
        assertThrows("IllegalArgumentException expected",
                IllegalArgumentException.class,
                () -> {
                    Resources.getResourceFile(null);
                });
    }

    @Test
    public void testGetCss() {
        File css = Resources.INSTANCE.getCss();
        assertNotNull(css);
    }

    @Test
    public void testGetCss2() {
        File css = Resources.INSTANCE.getCss2();
        assertNotNull(css);
    }

    @Test
    public void testGetDisplayIcc() {
        File icc = Resources.INSTANCE.getDisplayIcc();
        assertNotNull(icc);
    }

    @Test
    public void testGetFont() {
        File font = Resources.INSTANCE.getFont();
        assertNotNull(font);
    }

    @Test
    public void testGetLogo() {
        File logo = Resources.INSTANCE.getLogo();
        assertNotNull(logo);
    }
}
