package dk.openesdh.share.selenium;


import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.BasePage;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertTrue;

public class CreateCaseTestIT  extends BasePage {


    @FindBy(id="CREATE_CASE_DIALOG")
    WebElement createCaseDialog;

    @FindBy(name = "prop_cm_title")
    WebElement caseTitleTextBox;

    @FindBy(id="CREATE_CASE_DIALOG_STATUS_SELECT_CONTROL")
    WebElement caseStatusSelectButton;

    @FindBy(id="CREATE_CASE_DIALOG_AUTH_PICKER")
    WebElement caseAuthPickerButton;

    @FindBy(name = "prop_base_startDate")
    WebElement caseStartDateField;

    @FindBy(name = "prop_base_endDate")
    WebElement caseEndDateField;

    @FindBy(name = "prop_cm_description")
    WebElement caseDescriptionField;

    @FindBy(css="#CREATE_CASE_DIALOG .dijitDialogPaneContent .footer .confirmation .dijitButtonNode")
    WebElement createCaseDialogConfirmButton;

    @FindBy(css="#CREATE_CASE_DIALOG .dijitDialogPaneContent .footer .cancellation .dijitButtonNode")
    WebElement createCaseDialogCancelButton;

    //
    @Test
    public void createCaseAsAdmin() {
        this.loginAsUser(User.ADMIN);
        WebDriverWait wait = new WebDriverWait(Browser.Driver,10);
        String caseTitleText = RandomStringUtils.randomAlphanumeric(12);
        this.clickCasesMenuItem();
        assertTrue(createCaseDialog != null);
        this.clickCreateCaseMenuItem();
        this.caseTitleTextBox.clear();
        this.caseTitleTextBox.sendKeys(caseTitleText);
        this.caseDescriptionField.sendKeys(caseTitleText);
        assert createCaseDialogConfirmButton != null;
        createCaseDialogConfirmButton.click();

        WebElement caseDashboardMenuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("HEADER_CASE_DASHBOARD")));
        caseDashboardMenuButton.click();

        assertTrue(Pages.CaseDashboard.isAt());
    }

    @Test
    public void createCaseAsNonAdminUser() {
        this.loginAsUser(User.ALICE);
        WebDriverWait wait = new WebDriverWait(Browser.Driver,10);
        String caseTitleText = RandomStringUtils.randomAlphanumeric(12);
        this.clickCasesMenuItem();
        assertTrue(createCaseDialog != null);
        this.clickCreateCaseMenuItem();
        this.caseTitleTextBox.clear();
        this.caseTitleTextBox.sendKeys(caseTitleText);
        this.caseDescriptionField.sendKeys(caseTitleText);
        assert createCaseDialogConfirmButton != null;
        createCaseDialogConfirmButton.click();

        WebElement caseDashboardMenuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("HEADER_CASE_DASHBOARD")));
        caseDashboardMenuButton.click();

        assertTrue(Pages.CaseDashboard.isAt());
    }

}
