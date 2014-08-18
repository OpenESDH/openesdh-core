package dk.openesdh.test.selenium;

import dk.openesdh.test.selenium.framework.Browser;
import dk.openesdh.test.selenium.framework.Pages;
import dk.openesdh.test.selenium.framework.enums.User;
import dk.openesdh.test.selenium.framework.pages.BasePage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * This testcase requires:
 * -user admin with password admin
 * -user alice with password alice
 * -user invalid with password invalid NOT TO EXIST!
 * @author Søren Kirkegård
 *
 */
public class CaseMenuTest {

    @BeforeClass
    public static void setUpBeforeClass() {
        Browser.initialize();
    }

    @Test
    public void testCaseMenuIsVisibleAndClickable() {
        Pages.Login.loginWith(User.ADMIN);
        assertTrue(Pages.Dashboard.isAt(User.ADMIN));
        WebElement menuItem = Browser.Driver.findElement(By.id("HEADER_CASES_DROPDOWN_text"));
        assertNotNull( menuItem );

        WebElement searchLinkItem = Browser.Driver.findElement(By.id("CASE_MENU_SEARCH_LINK_text"));
        assertNotNull( searchLinkItem );

        WebElement createSimpleCaseItem = Browser.Driver.findElement(By.id("CASE_MENU_CREATE_CASE_CASE_SIMPLE_text"));
        assertNotNull( createSimpleCaseItem );

        menuItem.click();
        createSimpleCaseItem.click();

        String createCaseURL = BasePage.BASE_URL + "/page/create-content";
        assertTrue(Browser.Driver.getCurrentUrl().startsWith(createCaseURL));
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
