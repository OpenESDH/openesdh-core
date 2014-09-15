package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.junit.Assert.assertNotNull;


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
    WebElement headerCaseMenu;


    //TODO make it take an enum that represents the values
    public void clickCasesMenuItem() {
        assertNotNull(headerCaseMenu);
        headerCaseMenu.click();
    }

    @FindBy(id = "CASE_MENU_SEARCH_LINK_text")
    WebElement searchLinkItem;

    public void clickCasesMenuSearchItem() {
        assertNotNull(searchLinkItem);
        searchLinkItem.click();
    }

    @FindBy(id = "CASE_MENU_CREATE_CASE_CASE_SIMPLE_text")
    WebElement createSimpleCaseItem;

    public void clickCreateSimpleCaseItem() {
        assertNotNull(createSimpleCaseItem);
        createSimpleCaseItem.click();
    }

}