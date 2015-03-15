package dk.openesdh.share.selenium;

import dk.openesdh.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.*;
import static org.junit.Assert.assertTrue;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class EditCaseTest {

    String testCaseTitle;
    String testCaseStatus;
    List<String> testCaseOwners;
    String testCaseStartDate;
    String testCaseEndDate;

    String caseId;

    /**
     * Headermenu item "Cases"
     */
    @FindBy(id = "HEADER_CASES_DROPDOWN_text")
    protected WebElement headerCaseMenu;

    @BeforeClass
    public static void setUpBeforeClass() {
        Browser.initialize();
    }

    @Before
    public void login() {
        Pages.Login.loginWith(User.ADMIN);
    }

    public String createCase() {
        // Create a test "case" with a random title
        Pages.CreateCase.gotoPage();
        return Pages.CreateCase.createCase(testCaseTitle, testCaseStatus,
                testCaseOwners, testCaseStartDate, testCaseEndDate);
    }

    @Test
    public void editCase() {
        testCaseTitle = RandomStringUtils.randomAlphanumeric(24);
        testCaseStatus = "Planlagt";
        testCaseOwners = Arrays.asList("admin");
        testCaseStartDate = "";
        testCaseEndDate = "";
        caseId = createCase();
        Pages.CaseDashboard.edit();

        testCaseTitle = RandomStringUtils.randomAlphanumeric(24);
        Pages.EditCase.editCase(testCaseTitle,
                testCaseStatus, testCaseStartDate, testCaseEndDate);

        assertTrue(Pages.CaseDashboard.isAt());

        // TODO: Check that case dashboard is updated
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
