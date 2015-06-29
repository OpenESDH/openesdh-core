package dk.openesdh.share.selenium;


import dk.openesdh.share.selenium.framework.BasePageAdminLoginTestIT;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateCaseTestIT  extends BasePageAdminLoginTestIT {

    String testCaseTitle;
    String testCaseStatus;
    List<String> testCaseOwners;
    String testCaseStartDate;
    String testCaseEndDate;

    String testCaseNodeRef;

    /**
     * Headermenu item "Cases"
     */
    @FindBy(id = "HEADER_CASES_DROPDOWN_text")
    protected WebElement headerCaseMenu;

  
  /*  @Test*/
    public void createCase() {
        // Create a test "case" with a random title
        Pages.CreateCase.gotoPage();
        testCaseTitle = RandomStringUtils.randomAlphanumeric(24);
        testCaseStatus = "Planlagt";
        testCaseOwners = new LinkedList<String>();//the current user is now add as owner as default 
        testCaseStartDate = "";
        testCaseEndDate = "";
        testCaseNodeRef = Pages.CreateCase.createCase(testCaseTitle, testCaseStatus,
                testCaseOwners, testCaseStartDate, testCaseEndDate);
        assertNotNull(testCaseNodeRef);

        assertTrue(Pages.CaseDashboard.isAt());

        // TODO: Check that case dashboard appears as desired
    }

   

}
