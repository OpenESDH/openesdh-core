package dk.openesdh.share.selenium.tests;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.BasePage;
import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LoginTestIT extends BasePage {

    @Test
    public void testCanGoToLoginPage() {
        Pages.Login.gotoPage();
        assertTrue(Pages.Login.isAt());
    }

    @Test
    public void testCanLoginWithAdminUser() {
        Pages.Login.loginWith(User.ADMIN);
        assertTrue(Pages.Dashboard.isAt(User.ADMIN));
        Pages.Login.logout();
    }

//    @Test
    public  souvoid testCanLoginWithNormalUser() {
        Pages.Login.loginWith(User.ALICE);
        assertTrue(Pages.Dashboard.isAt(User.ALICE));
        Pages.Login.logout();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Browser.Driver.close();
    }


}
