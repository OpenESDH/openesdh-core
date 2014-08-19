package dk.openesdh.repo.selenium.framework.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


/**
 * All pages must extend this base page to share type
 * and have basic shared configurations
 * @author Søren Kirkegård
 *
 */
public abstract class BasePage {

    public static final String BASE_URL = "http://localhost:8081/share";


    /**
     * Headermenu item "Cases"
     */
    @FindBy(id = "HEADER_CASES_DROPDOWN_text")
    protected WebElement headerCaseMenu;


    //TODO make it take an enum that represents the values
    public void clickCasesMenuItem() {
        headerCaseMenu.click();
    }



}