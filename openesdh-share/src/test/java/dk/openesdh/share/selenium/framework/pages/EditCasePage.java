package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditCasePage extends BasePage {
    private static final String URL = BASE_URL + "/page/edit-metadata";

@FindBy(id = "template_x002e_edit-metadata_x002e_edit-metadata_x0023_default_prop_cm_title")
    WebElement titleField;

    @FindBy(id = "template_x002e_edit-metadata_x002e_edit-metadata_x0023_default_prop_oe_status")
    WebElement statusField;

    @FindBy(id = "template_x002e_edit-metadata_x002e_edit-metadata_x0023_default_prop_case_startDate-cntrl-date")
    WebElement startDateField;

    @FindBy(id = "template_x002e_edit-metadata_x002e_edit-metadata_x0023_default_prop_case_endDate-cntrl-date")
    WebElement endDateField;

    @FindBy(css = "#template_x002e_edit-metadata_x002e_edit" +
            "-metadata_x0023_default_assoc_case_owners-cntrl button")
    WebElement ownersFieldButton;

    @FindBy(id = "template_x002e_edit-metadata_x002e_edit" +
            "-metadata_x0023_default-form-submit-button")
    WebElement submitButton;

    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public void gotoPage(String nodeRef) {
        // TODO: Navigate to the page from the case dashboard
        Browser.Driver.get(URL + "?nodeRef=" + nodeRef);
    }


    /**
     * Edit a case setting properties to the given parameters.
     * @param title
     * @param status
     * @param owners
     * @param startDate
     * @param endDate
     */
    public void editCase(String title, String status,
                           String startDate, String endDate) {
        titleField.clear();
        titleField.sendKeys(title);
        statusField.sendKeys(status);
        startDateField.clear();
        startDateField.sendKeys(startDate);
        endDateField.clear();
        endDateField.sendKeys(endDate);

        // TODO: Support changing owner.

        submitButton.click();
    }
}
