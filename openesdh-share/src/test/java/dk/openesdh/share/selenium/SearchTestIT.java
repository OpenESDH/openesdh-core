package dk.openesdh.share.selenium;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.pages.BasePage;
import dk.openesdh.share.selenium.framework.Pages;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class SearchTestIT extends BasePage {

    String testCaseTitle;
    String testCaseStatus;
    List<String> testCaseOwners;

    String testCaseNodeRef;


   /* @Before */
    public void createTestCase() {
        // Create a test "case" with a random title
        Pages.CreateCase.gotoPage();
        testCaseTitle = RandomStringUtils.randomAlphanumeric(24);
        testCaseStatus = "Planlagt";
        testCaseOwners = new LinkedList<String>();
        String testCaseStartDate = "";
        String testCaseEndDate = "";
        testCaseNodeRef = Pages.CreateCase.createCase(testCaseTitle, testCaseStatus,
                testCaseOwners, testCaseStartDate, testCaseEndDate);
        assertNotNull(testCaseNodeRef);
    }

   /* @Test */
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


}
