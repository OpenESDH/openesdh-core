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

public class JournalizeCaseTest {

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

        // TODO load a valid journalKey instead of supplying the dummy ref
        Pages.CaseDashboard.journalize("Languages");

        // TODO: Make it language-independent
        WebElement elem = Browser.Driver.findElement(By.xpath("//*[text()='Journaliseret af']//following-sibling::*[@class='value']"));
        assertEquals(User.ADMIN.username(), elem.getText());

        // TODO: Assert that journal key is correct
    }


    @After
    public void tearDown() {
        // Delete the test case
        Pages.DocumentDetails.gotoPage(testCaseNodeRef);
        Pages.DocumentDetails.deleteNode();
        Pages.Login.logout();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Browser.Driver.close();
    }

}
