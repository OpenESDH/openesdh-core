package dk.openesdh.share.selenium;

import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.BasePage;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CaseDocumentsTestIT extends BasePage {

    String testCaseTitle;
    String testCaseStatus;
    List<String> testCaseOwners;

    String testCaseID;

  //TKR: Test comment

    @Before
    public void setup() {
        Pages.Login.loginWith(User.ADMIN);

        // Create a test "case" with a random title
        Pages.CreateCase.gotoPage();
        testCaseTitle = RandomStringUtils.randomAlphanumeric(24);
        testCaseStatus = "Planlagt";
        testCaseOwners = new LinkedList<String>(); // Arrays.asList("admin"); current user is set as owner as default now
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


 

}
