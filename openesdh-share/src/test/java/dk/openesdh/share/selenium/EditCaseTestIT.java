package dk.openesdh.share.selenium;


import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.pages.BasePage;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class EditCaseTestIT extends BasePage {

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
        testCaseOwners = new LinkedList<String>(); // Arrays.asList("admin"); current user is set as owner as default now
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

}
