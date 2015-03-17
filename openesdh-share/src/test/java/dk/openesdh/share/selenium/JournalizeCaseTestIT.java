package dk.openesdh.share.selenium;

import dk.openesdh.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class JournalizeCaseTestIT {

    String testCaseTitle;
    String testCaseStatus;
    List<String> testCaseOwners;

    String testCaseNodeRef;

    @BeforeClass
    public static void setUpBeforeClass() {
        Browser.initialize();
    }

    @Before
    public void setup() {
        Pages.Login.loginWith(User.ADMIN);

        // Create a test "case" with a random title
        Pages.CreateCase.gotoPage();
        testCaseTitle = RandomStringUtils.randomAlphanumeric(24);
        testCaseStatus = "Planlagt";
        testCaseOwners = Arrays.asList("admin");
        String testCaseStartDate = "";
        String testCaseEndDate = "";
        testCaseNodeRef = Pages.CreateCase.createCase(testCaseTitle, testCaseStatus,
                testCaseOwners, testCaseStartDate, testCaseEndDate);
        assertNotNull(testCaseNodeRef);

        Pages.CaseDashboard.gotoPage(testCaseNodeRef);
        assertTrue(Pages.CaseDashboard.isAt());
    }

    @Test
    public void testJournalizeCase() {

        WebElement warning = Browser.Driver.findElement(By.id("HEADER_CASE_JOURNALIZED_WARNING"));
        assertFalse(warning.isDisplayed());

        // TODO load a valid journalKey instead of supplying the dummy ref
        // Journalize
        Pages.CaseDashboard.journalize("Languages");

        // TODO: Make it language-independent
        WebElement elem = Browser.Driver.findElement(By.xpath("//*[text()='Journaliseret af']//following-sibling::*[@class='value']"));
        assertEquals(User.ADMIN.username(), elem.getText());

        warning = Browser.Driver.findElement(By.id("HEADER_CASE_JOURNALIZED_WARNING"));
        assertTrue(warning.isDisplayed());

        // TODO: Assert that journal key is correct


        // Unjournalize
        Pages.CaseDashboard.unJournalize();


        warning = Browser.Driver.findElement(By.id("HEADER_CASE_JOURNALIZED_WARNING"));
        assertFalse(warning.isDisplayed());
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
