package dk.openesdh.share.selenium.framework.pages;

import dk.magenta.share.selenium.framework.Browser;
import dk.magenta.share.selenium.framework.pages.BasePage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class CaseMembersPage extends BasePage {
    private static final String URL = BASE_URL + "/page/oe/case/@@ID@@/members";

    /**
     * Headermenu item "Cases"
     */
    @FindBy(id = "CASE_MEMBERS_ADD_AUTHORITIES_text")
    WebElement addAuthoritiesButton;


    public void gotoPage(String caseId) {
        Browser.open(URL.replace("@@ID@@", caseId) );
    }

    public boolean isAt() {
        String tmpUrl = URL.replace("@@ID@@", this.getCaseId());
        return Browser.Driver.getCurrentUrl().startsWith(tmpUrl);
    }


    public void addAuthoritiesToRole(List<String> authorities, String role) {
        addAuthoritiesButton.click();
        WebElement addAuthorityToRole = Browser.Driver.findElement(By.id
                ("CASE_MEMBERS_ADD_AUTHORITY_" + role.toUpperCase() +
                        "_text"));
        addAuthorityToRole.click();

       
       //This will trigger the old authority-picker, and the selenium test code currently works with the new one. 
        selectAuthoritiesInPicker("alf-id1_wrapper-authority", authorities);
    }
}
