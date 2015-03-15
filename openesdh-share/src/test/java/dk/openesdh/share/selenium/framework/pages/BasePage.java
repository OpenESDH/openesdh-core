package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /*
     * Find the case ID from the URL with the new /page/oe/case/<id>/page scheme
     */
    public String getCaseId() {
        Pattern p = Pattern.compile("\\/oe\\/case\\/(.+)/");
        Matcher matcher = p.matcher(Browser.Driver.getCurrentUrl());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }

    }

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

    public static void selectAuthoritiesInPicker(String id,
                                                  List<String> authorities) {
        WebElement searchInput = Browser.Driver.findElement(By.cssSelector(
                "input[id$='" + id + "-cntrl-picker-searchText']"));

        WebElement searchButton = Browser.Driver.findElement(By.cssSelector(
                "button[id$='" + id + "-cntrl-picker-searchButton-button']"));
        for (String authority : authorities) {
            searchInput.clear();
            searchInput.sendKeys(authority);
            searchButton.click();
            // Wonderfully complicated XPath way to get the Add button for
            // adding the particular authority we want to add.
            WebElement addAuthority = Browser.Driver.findElement(By
                    .xpath("//td[contains(@class, 'yui-dt-col-name') and " +
                            "contains(., " +
                            "'" + authority + "')]/following-sibling::td[contains(@class, 'yui-dt-col-add')]/descendant::a"));
            addAuthority.click();
        }

        WebElement authorityPickerOkButton = Browser.Driver.findElement(By
                .cssSelector("button[id$='" + id + "-cntrl-ok-button']"));
        authorityPickerOkButton.click();



    }

}