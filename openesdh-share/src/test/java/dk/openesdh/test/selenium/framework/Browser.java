package dk.openesdh.test.selenium.framework;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;


/**
 * Class acting as a danish firefox browser
 * @author Søren Kirkegård
 *
 */
public class Browser {

    private static final FirefoxProfile profile = profile();
    private static final FirefoxBinary binary = binary();

    public static WebDriver Driver = null;

    /**
     * PhantomJSDriver settings/capabilities
     * @return
     */
    public static DesiredCapabilities dCaps() {
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", false);
        dCaps.setCapability("phantomjs.page.customHeaders.Accept-Language", "da-DK");
        return dCaps;
    }

    /**
     * This locates the firefox binary and runs it Xvfb. It assumes that
     * Firefox is installed in the default package.
     * CARVEAT! You need to have Xvfb installed:
     * sudo apt-get install xfvb
     * And running
     * Xvfb :1 -screen 0 1024x768x24 &
     * Information about Xfvb see:
     * http://www.x.org/releases/current/doc/man/man1/Xvfb.1.xhtml
     * Read this blog post for more information:
     * http://www.seleniumtests.com/2012/04/headless-tests-with-firefox-webdriver.html
     * @return FirefoxBinary
     */
    private static FirefoxBinary binary() {
        // Setup firefox binary to start in Xvfb
        String Xport = System.getProperty(
                "lmportal.xvfb.id", ":1");
        FirefoxBinary firefoxBinary = new FirefoxBinary();
        firefoxBinary.setEnvironmentProperty("DISPLAY", Xport);


        return firefoxBinary;
    }

    private static FirefoxProfile profile() {

        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("intl.accept_languages", "da");

        return profile;
    }

    /**
     * Static helper method to create a fresh driver for our tests
     * @return
     */
    public static void initialize() {

        boolean headless = false;
        if(headless) {
            Driver =  new FirefoxDriver(binary, profile); System.err.println("headless");
        } else if(!headless) {
            Driver =  new FirefoxDriver(profile);
        } else {
            Driver = new FirefoxDriver(binary, profile); System.err.println("headless");
        }
        Pages.initialize();
    }

    public static void open(final String url) {
        Driver.get(url);
    }

    public static void close() {
        Driver.close();
    }

    public static String title() {
        return Driver.getTitle();
    }

}
