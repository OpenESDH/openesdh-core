package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCasePage extends BasePage {
    private static final String URL = BASE_URL + "/page/create-content";

    @FindBy(id = "template_x002e_create-content_x002e_create-content_x0023_default_prop_cm_title")
    WebElement titleField;

    @FindBy(id = "template_x002e_create-content_x002e_create-content_x0023_default_prop_oe_status")
    WebElement statusField;

    @FindBy(id = "template_x002e_create-content_x002e_create-content_x0023_default_prop_case_startDate-cntrl-date")
    WebElement startDateField;

    @FindBy(id = "template_x002e_create-content_x002e_create-content_x0023_default_prop_case_endDate-cntrl-date")
    WebElement endDateField;

    @FindBy(css = "#template_x002e_create-content_x002e_create" +
            "-content_x0023_default_assoc_case_owners-cntrl button")
    WebElement ownersFieldButton;

    @FindBy(id = "template_x002e_create-content_x002e_create" +
            "-content_x0023_default-form-submit-button")
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
        statusField.sendKeys(status);
        startDateField.sendKeys(startDate);
        endDateField.sendKeys(endDate);

        ownersFieldButton.click();
        selectAuthoritiesInPicker("template_x002e_create-content_x002e_create-content_x0023_default_assoc_case_owners", owners);

        createButton.click();

        // Wait for the case dashboard to load
        Browser.Driver.findElement(By.cssSelector(".dashlet"));

        return this.getCaseId();
    }
}
