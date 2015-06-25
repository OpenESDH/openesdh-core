package dk.openesdh.share.selenium;


import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.BasePageAdminLoginTestIT;
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

public class JournalizeCaseTestIT extends BasePageAdminLoginTestIT {

    String testCaseTitle;
    String testCaseStatus;
    List<String> testCaseOwners;

    String testCaseNodeRef;

    
    @Before
    public void setup()  {
        // Create a test "case" with a random title
        Pages.CreateCase.gotoPage();
        testCaseTitle = RandomStringUtils.randomAlphanumeric(24);
        testCaseStatus = "Planlagt";
        testCaseOwners = new LinkedList<String>(); //current user is now per default set as owner
        String testCaseStartDate = "";
        String testCaseEndDate = "";
        testCaseNodeRef = Pages.CreateCase.createCase(testCaseTitle, testCaseStatus,
                testCaseOwners, testCaseStartDate, testCaseEndDate);
        assertNotNull(testCaseNodeRef);

        Pages.CaseDashboard.gotoPage(testCaseNodeRef);
        assertTrue(Pages.CaseDashboard.isAt());
    }

    //@Test --TODO Re-enable test when KLE part is in place
    public void testJournalizeCase() {

        WebElement warning = Browser.Driver.findElement(By.id("HEADER_CASE_JOURNALIZED_WARNING"));
        assertFalse(warning.isDisplayed());

        // TODO load a valid journalKey instead of supplying the dummy ref
        // Journalize
        Pages.CaseDashboard.journalize("Languages");

        //wait for page refresh to begin
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
                
        //wait for page refresh completion
        assertTrue(Browser.waitForPageToLoad());
        
        //TODO: Make it language-independent
     		WebElement elem = Browser.Driver.findElement(By
     						.xpath("//div[contains(@class,'warnings')]//span[text()='Denne sag er journaliseret.']"));
     	assertTrue(elem!=null);

        warning = Browser.Driver.findElement(By.id("HEADER_CASE_JOURNALIZED_WARNING"));
        
        assertTrue( Browser.waitForElementToBeDisplayed(warning));
        
        // TODO: Assert that journal key is correct

        // Unjournalize
        Pages.CaseDashboard.unJournalize();
        
        
        warning = Browser.Driver.findElement(By.id("HEADER_CASE_JOURNALIZED_WARNING"));
        assertTrue( Browser.waitForElementToDisapear(warning));
    }



}
