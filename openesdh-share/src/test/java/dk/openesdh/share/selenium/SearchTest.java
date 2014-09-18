package dk.openesdh.share.selenium;

import dk.openesdh.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.BasePage;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SearchTest {

    String testCaseTitle;
    String testCaseStatus;
    List<String> testCaseOwners;

    String testCaseNodeRef;

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

    @Before
    public void createTestCase() {
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
    }

    @Test
    public void testSearchPage() {
        Pages.Search.gotoPage();

        String field = "cm:title";
        String fieldAsId = field.replace(':', '-');

        assertTrue(Pages.Search.isAt());

        // Click the New filter button
        WebElement addFilters = Browser.Driver.findElement(By.id("new-filter-button"));
        assertTrue(addFilters.isDisplayed());
        addFilters.click();

        // Click to add a Title filter
        WebElement newFilterTitle = Browser.Driver.findElement(By.id
                ("new-filter-" + fieldAsId));
        assertTrue(newFilterTitle.isDisplayed());
        newFilterTitle.click();

        // Make sure the filter is displayed
        WebElement filter = Browser.Driver.findElement(By.className("esdh-filter"));
        assertTrue(filter.isDisplayed());

        // Get the Title filter text box
        WebElement filterTextBox = Browser.Driver.findElement(
                By.cssSelector(".esdh-filter-field-widget input"));

        // Type in a test value for the filter
        filterTextBox.sendKeys(testCaseTitle);

        // Apply the filters
        WebElement applyButton = Browser.Driver.findElement(By.id
                ("apply-filters-button"));
        applyButton.click();

        // Make sure there is a result containing the title
        WebElement firstRowTitleCell = Browser.Driver.findElement(By
                .cssSelector(".dgrid-row-table tr td.field-" +
                        fieldAsId));
        assertTrue(firstRowTitleCell.getText().contains(testCaseTitle));
    }


    @After
    public void tearDown() {
        // Delete the test case
        Pages.CaseDashboard.gotoPage(testCaseNodeRef);
        Pages.CaseDashboard.deleteCase();
        Pages.Login.logout();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Browser.Driver.close();
    }

}
