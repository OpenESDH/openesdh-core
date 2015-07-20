package dk.openesdh.share.selenium.framework.pages;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BaseCasePage extends BasePage{
    private static final String URL = BASE_URL + "/page/oe/case/@@ID@@/";

    @Before
    public void userSetup() {
        Pages.Login.loginWith(User.ADMIN);
        Pages.AdminToolsPage.addUserToGroup(User.BRIGITTE, "CaseSimpleCreator");
        Pages.Login.logout();
    }


    //<editor-fold desc="WebElements Global to all case pages">
    @FindBy(xpath = "//div[@id='HEADER_CASE_DASHBOARD']//a")
    WebElement navCaseDashboardMenuItem;

    @FindBy(xpath = "//div[@id='HEADER_CASE_DOCUMENTS']//a")
    WebElement navCaseDocumentsMenuItem;

    @FindBy(xpath = "//div[@id='HEADER_CASE_MEMBERS']//a")
    WebElement navCaseMembersMenuItem;

    @FindBy(xpath = "//div[@id='HEADER_CASE_PARTIES']//a")
    WebElement navCasePartiesMenuItem;

    @FindBy(xpath = "//div[@id='HEADER_CASE_CONFIGURATION_DROPDOWN']")
    WebElement navCogMenuItem;
    //</editor-fold>

    //<editor-fold desc="Some webelements that we need">
    @FindBy(id="CASE_INFO_DASHLET")
    WebElement caseInfoDashlet;

    @FindBy(id="CREATE_CASE_DIALOG")
    WebElement createCaseDialog;

    @FindBy(name = "prop_cm_title")
    WebElement caseTitleTextBox;

    @FindBy(name = "prop_cm_description")
    WebElement caseDescriptionField;

    @FindBy(css="#CREATE_CASE_DIALOG .dijitDialogPaneContent .footer .confirmation .dijitButtonNode")
    WebElement createCaseDialogConfirmButton;
    //</editor-fold>

    private User caseCreator;
    WebDriverWait wait = new WebDriverWait(Browser.Driver,10);

    //Find the case ID from the URL with the new /page/oe/case/<id>/page scheme
    public String getCaseId() {
        Pattern p = Pattern.compile("\\/oe\\/case\\/(.+)/");
        Matcher matcher = p.matcher(Browser.Driver.getCurrentUrl());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public User getCaseCreator(){
        return this.caseCreator;
    }

    public boolean isAt() {
        String tmpUrl = URL.replace("@@ID@@", this.getCaseId());
        return Browser.Driver.getCurrentUrl().startsWith(tmpUrl);
    }

    public void gotoCasePage(String page){
        if(!this.isAt()) {
            String tmpUrl = URL+"dashboard";
            Browser.open(tmpUrl.replace("@@ID@@", this.getCaseId()));
            this.wait.until(ExpectedConditions.visibilityOf(caseInfoDashlet));
        }

        switch (page){
            //For the time being these are the only pages we may require within the testing surface area.
            case "dashboard" : navCaseDashboardMenuItem.click(); break;
            case "documents" : navCaseDocumentsMenuItem.click(); break;
            case "members" : navCaseMembersMenuItem.click(); break;
            case "parties" : navCasePartiesMenuItem.click(); break;
        }

    }


    protected String createCaseAsUser(User user){
        /*if(initAsCaseCreator) {
            initAsCaseCreator(user);
            //set the user that has now been set as the case creator so that we can easily
            // retrieve it elsewhere
            this.caseCreator = user;
        }*/
        this.loginAsUser(user);
        String caseTitleText = RandomStringUtils.randomAlphanumeric(12);
        this.clickCasesMenuItem();
        this.clickCreateCaseMenuItem();
        assertNotNull(createCaseDialog);
        this.caseTitleTextBox.clear();
        this.caseTitleTextBox.sendKeys(caseTitleText);
        this.caseDescriptionField.sendKeys(caseTitleText);
        assertNotNull(createCaseDialogConfirmButton);
        createCaseDialogConfirmButton.click();

        WebElement caseDashboardMenuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("HEADER_CASE_DASHBOARD")));
        caseDashboardMenuButton.click();

        assertTrue(Pages.CaseDashboard.isAt());
        return this.getCaseId();
    }

    public static void selectAuthoritiesInPicker(String id, List<String> authorities) {

        WebElement searchInput = Browser.Driver
                .findElement(By
                        .xpath("//div[div[span[@role='heading' and (text()='Select...'  ) ]] and contains(@class,'alfresco-dialog-AlfDialog') ]//input[@name='searchTerm']"));
        WebElement searchButton = Browser.Driver
                .findElement(By
                        .xpath("//span[contains(@class,'confirmationButton')]/span[contains(@class,'dijitButtonNode')]"));
        for (String authority : authorities) {
            searchInput.clear();
            searchInput.sendKeys(authority);
            searchButton.click();
            // Wonderfully complicated XPath way to get the Add button for
            // adding the particular authority we want to add.
            WebElement addAuthority = Browser.Driver
                    .findElement(By
                            .xpath("//td[contains(@class, 'yui-dt-col-name') and "
                                    + "contains(., "
                                    + "'"
                                    + authority
                                    + "')]/following-sibling::td[contains(@class, 'yui-dt-col-add')]/descendant::a"));
            addAuthority.click();
        }

        WebElement authorityPickerOkButton = Browser.Driver.findElement(By
                .cssSelector("button[id$='" + id + "-cntrl-ok-button']"));
        authorityPickerOkButton.click();

    }

}