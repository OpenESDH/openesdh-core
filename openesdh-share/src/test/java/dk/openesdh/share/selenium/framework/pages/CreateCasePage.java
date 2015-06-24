package dk.openesdh.share.selenium.framework.pages;


import dk.magenta.share.selenium.framework.Browser;
import dk.magenta.share.selenium.framework.pages.BasePage;
import dk.openesdh.share.selenium.framework.Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCasePage extends BasePage {
    private static final String URL = BASE_URL + "/page/oe/case/create-case";

    @FindBy(name = "prop_cm_title")
    WebElement titleField;

    //@FindBy(xpath = "//table[@id='prop_oe_status_CONTROL']/tbody/tr/td")
    @FindBy(css = "#prop_oe_status_CONTROL tbody tr td")
    WebElement statusControl;

    @FindBy(id = "prop_case_startDate_CONTROL")
    WebElement startDateField;

    @FindBy(id = "prop_case_endDate_CONTROL")
    WebElement endDateField;

    @FindBy(id = "create_case_dialog_auth_picker_button")
    WebElement ownersFieldButton;

	@FindBy(xpath = "//div[div[span[@role='heading' and (text()='Opret sag' or text()='Create case' ) ]] and contains(@class,'alfresco-dialog-AlfDialog') ]//span[span[span/text()= 'Opret' or span/text()='Create'] and contains(@class,'dijitButtonNode') ]")
	WebElement createButton;

    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public void gotoPage() {
        // TODO: Take parameter for case type
        clickCasesMenuItem();
        clickCreateSimpleCaseItem();
    }


    /**
     * Create a case with the given parameters.
     * @param title
     * @param status
     * @param owners
     * @param startDate
     * @param endDate
     * @return NodeRef of the case or null if failed
     */
    public String createCase(String title, String status, List<String> owners,
                       String startDate, String endDate) {
        titleField.sendKeys(title);



        statusControl.click();
        status = "Afventer";


        //List<WebElement> statusOptions = statusControl.findElements(By.cssSelector( "td[id^=dijit_MenuItem_]" ));

        List<WebElement> statusOptions = Browser.Driver.findElements(By.cssSelector( "#prop_oe_status_CONTROL_dropdown td[id^=dijit_MenuItem_]" ));
        for (WebElement opt : statusOptions) {
            if (opt.getText().equals( status ) ) {
                opt.click();
                break;
            }
        }

        statusControl.sendKeys("Afventer");
        startDateField.sendKeys(startDate);
        endDateField.sendKeys(endDate);

        if (owners != null && owners.size() > 0) {
        ownersFieldButton.click();
        selectAuthoritiesInPicker("assoc_case_owners_added", owners);
        }
        createButton.click();

        // Wait for the case dashboard to load
        Browser.Driver.findElement(By.cssSelector(".dashlet"));

        return this.getCaseId();
    }
}
