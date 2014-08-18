package dk.openesdh.test.selenium.framework.pages;

import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.Select;


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
    @FindBy(id = "HEADER_CUSTOM_DROPDOWN_text")
    protected WebElement headerCaseMenu;


    //TODO make it take an enum that represents the values
    public void clickCasesMenuItem() {
        headerCaseMenu.click();
    }



}