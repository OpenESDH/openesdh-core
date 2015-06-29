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

public class CaseMembersTestIT extends BasePageAdminLoginTestIT {

    String testCaseTitle;
    String testCaseStatus;
    List<String> testCaseOwners;

    String testCaseID;

    
    /* @Before --TODO : Re-enable this when the code uses the new authority-picker */
    public void setup() {

        // Create a test "case" with a random title
        Pages.CreateCase.gotoPage();
        testCaseTitle = RandomStringUtils.randomAlphanumeric(24);
        testCaseStatus = "Planlagt";
        testCaseOwners = new LinkedList<String>(); //current user set as owner as default now
        String testCaseStartDate = "";
        String testCaseEndDate = "";

        testCaseID = Pages.CreateCase.createCase(testCaseTitle, testCaseStatus,
                testCaseOwners, testCaseStartDate, testCaseEndDate);
        assertNotNull(testCaseID);

        Pages.CaseMembers.gotoPage(testCaseID);
        assertTrue(Pages.CaseMembers.isAt());
    }

   /* @Test --TODO : Re-enable this when the code uses the new authority-picker*/
    public void testAddChangeRemoveUser() {



        String role = "CaseSimpleReader";
        List<String> authorities = new LinkedList<>();
        String testUser = "admin";
        authorities.add(testUser);
        Pages.CaseMembers.addAuthoritiesToRole(authorities, role);

        String memberRoleId = testUser + "-" + role;
        WebElement elem = Browser.Driver.findElement(By.id(memberRoleId));
        assertTrue("Added user role is displayed", elem.isDisplayed());

        JavascriptExecutor js = (JavascriptExecutor) Browser.Driver;
        String roleVal = (String) js.executeScript("return dijit.byId('" +
                memberRoleId  + "-select').get('value')");
        assertEquals("Role is displayed correctly", role, roleVal);

        // Change the role
        String newRole = "CaseSimpleWriter";
        js.executeScript("dijit.byId('" +
                memberRoleId  + "-select').set('value', '" + newRole + "')");

        roleVal = (String) js.executeScript("return dijit.byId('" +
                memberRoleId  + "-select').get('value')");
        assertEquals("Role is changed", newRole, roleVal);

        // Remove the user from the role
        js.executeScript("dijit.byId('" +
                memberRoleId  + "-remove').onClick()");
        Alert alert = Browser.Driver.switchTo().alert();
        alert.accept();

        assertTrue("Removed user role is not displayed",
                Browser.Driver.findElements(By.id(memberRoleId)).isEmpty());
    }


  

}
