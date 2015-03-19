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

public class CaseDocumentsTestIT {

    String testCaseTitle;
    String testCaseStatus;
    List<String> testCaseOwners;

    String testCaseID;

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

        testCaseID = Pages.CreateCase.createCase(testCaseTitle, testCaseStatus,
                testCaseOwners, testCaseStartDate, testCaseEndDate);
        assertNotNull(testCaseID);

        Pages.CaseDocumentsPage.gotoPage(testCaseID);
        assertTrue(Pages.CaseDocumentsPage.isAt());
    }

    @Test
    public void checkAllDashletsExist(){
        Pages.CaseDocumentsPage.addDocRecordClick();
        Pages.CaseDocumentsPage.closeDialogClick();
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
