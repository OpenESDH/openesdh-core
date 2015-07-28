package dk.openesdh.share.selenium.framework.pages;

import dk.magenta.share.selenium.framework.Browser;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CaseDashboardCasePage extends BaseCasePage {

    public static final String URL = BASE_URL + "/page/oe/case/@@ID@@/dashboard";

    @FindBy(id = "HEADER_CASE_CONFIGURATION_DROPDOWN")
    WebElement headerCaseConfigDropdown;

    @FindBy(id = "HEADER_CASE_JOURNALIZE_text")
    WebElement headerJournalizeItem;

    @FindBy(id = "HEADER_CASE_EDIT_text")
    WebElement headerCaseEditButton;

    @FindBy(id = "HEADER_CASE_UNJOURNALIZE_text")
    WebElement headerUnJournalizeItem;
    
	@FindBy(xpath = "//div[div[span[@role='heading' and (text()='Journalisér...' or text()='Journalize...' ) ]] and contains(@class,'alfresco-dialog-AlfDialog') ]//span[span[span/text()= 'Vælg' or span/text()='Choose'] and contains(@class,'dijitButtonNode') ]")
	WebElement chooseJounalizeKeyButton;
    
	@FindBy(xpath = "//div[div[span[@role='heading' and (text()='Vælg...' or text()='Choose...' ) ]] and contains(@class,'alfresco-dialog-AlfDialog') ]//a[contains(@class,'item-label') and (text()='Languages' or text()='Sprog')]")
	WebElement selectLanguagesAsJounalizeKeyLbl;
	
	
	@FindBy(xpath = "//div[div[span[@role='heading' and (text()='Journalisér...' or text()='Journalize...' ) ]] and contains(@class,'alfresco-dialog-AlfDialog') ]//span[span[span/text()= 'Ok'] and contains(@class,'dijitButtonNode') ]")
	WebElement confirmJounalizeButton;

    public void gotoPage(String caseId) {
        Browser.open(URL.replace("@@ID@@", caseId) );
    }

    public boolean isAt() {
        String tmpUrl = URL.replace("@@ID@@", this.getCaseId());
        return Browser.Driver.getCurrentUrl().startsWith(tmpUrl);
    }

    public void edit() {
        headerCaseConfigDropdown.click();
        headerCaseEditButton.click();
    }


    public void journalize(String journalKey) {
        headerCaseConfigDropdown.click();        
        headerJournalizeItem.click();
        chooseJounalizeKeyButton.click();	
        selectLanguagesAsJounalizeKeyLbl.click();
        confirmJounalizeButton.click();
         
        Browser.waitForAlert();
        
        Alert alert = Browser.Driver.switchTo().alert();
        alert.accept();
    }

    public void unJournalize() {
        headerCaseConfigDropdown.click();
        headerUnJournalizeItem.click();
       
        Browser.waitForAlert();
        // Accept the confirmation dialog
        Alert alert = Browser.Driver.switchTo().alert();
        alert.accept();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
