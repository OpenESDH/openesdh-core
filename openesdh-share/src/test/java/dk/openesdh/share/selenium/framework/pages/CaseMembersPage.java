package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class CaseMembersPage extends BasePage {
    private static final String URL = BASE_URL + "/page/hdp/ws/dk-openesdh-pages-case-dashboard-members";

    /**
     * Headermenu item "Cases"
     */
    @FindBy(id = "CASE_MEMBERS_ADD_AUTHORITIES_text")
    WebElement addAuthoritiesButton;

    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public void gotoPage(String nodeRef) {
        Browser.open(URL + "?nodeRef=" + nodeRef);
    }

    public void addAuthoritiesToRole(List<String> authorities, String role) {
        addAuthoritiesButton.click();
        WebElement addAuthorityToRole = Browser.Driver.findElement(By.id
                ("CASE_MEMBERS_ADD_AUTHORITY_" + role.toUpperCase() +
                        "_text"));
        addAuthorityToRole.click();

        // TODO: Select an authority in authority picker
    }
}
