package dk.openesdh.repo.selenium;

import static org.junit.Assert.*;

import dk.openesdh.repo.selenium.framework.Browser;
import dk.openesdh.repo.selenium.framework.Pages;
import dk.openesdh.repo.selenium.framework.enums.User;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class LoginTest {

    @BeforeClass
    public static void setUpBeforeClass() {
        Browser.initialize();
    }

    @Test
    public void testCanGoToLoginPage() {
        Pages.Login.gotoPage();
        assertTrue(Pages.Login.isAt());
    }

    @Test
    public void testCanLoginWithAdminUser() {
        Pages.Login.loginWith(User.ADMIN);
        assertTrue(Pages.Dashboard.isAt(User.ADMIN));
    }

    @After
    public void tearDown() {
        Pages.Login.logout();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Browser.Driver.close();
    }

}
