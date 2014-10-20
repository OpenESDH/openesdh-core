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

        Pages.CaseMembers.gotoPage(testCaseNodeRef);
        assertTrue(Pages.CaseMembers.isAt());
    }

    @Test
    public void testJournalizeCase() {





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
