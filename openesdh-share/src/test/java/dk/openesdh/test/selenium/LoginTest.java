package dk.openesdh.test.selenium;

import static org.junit.Assert.*;

import dk.openesdh.test.selenium.framework.Browser;
import dk.openesdh.test.selenium.framework.Pages;
import dk.openesdh.test.selenium.framework.enums.User;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * This testcase requires:
 * -user admin with password admin
 * -user alice with password alice
 * -user invalid with password invalid NOT TO EXIST!
 * @author Søren Kirkegård
 *
 */
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
        Pages.Dashboard.clickCasesMenuItem();
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
