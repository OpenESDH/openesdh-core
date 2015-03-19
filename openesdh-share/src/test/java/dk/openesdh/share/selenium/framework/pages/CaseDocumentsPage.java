package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class CaseDocumentsPage extends BasePage {
    private static final String URL = BASE_URL + "/page/oe/case/@@ID@@/documents";

    /**
     * Dashlet item "DocumentRecord"
     */
    @FindBy(id = "CASE_DOCUMENTS_DASHLET")
    WebElement caseDocumentsDashlet;

    @FindBy(id = "add_document_record_button_label")
    WebElement addDocumentRecordButton;

    /**
     * Dashlet item "Document Attachments"
     */
    @FindBy(id = "CASE_DOCUMENTS_ATTACHMENTS_DASHLET")
    WebElement caseDocumentAttachmentsDashlet;

    /**
     * Dashlet item "Document Record Info"
     */
    @FindBy(id = "DOC_RECORD_INFO")
    WebElement caseDocumentRecordInfoDashlet;

    /**
     * Dashlet item "Document Versions"
     */
    @FindBy(id = "DOCUMENT_PREVIEW_DASHLET")//Change Id
    WebElement caseDocumentVersionsDashlet;


    public void gotoPage(String caseId) {
        Browser.open(URL.replace("@@ID@@", caseId) );
    }

    public boolean isAt() {
        String tmpUrl = URL.replace("@@ID@@", this.getCaseId());
        return Browser.Driver.getCurrentUrl().startsWith(tmpUrl);
    }

    public void addDocRecordClick(){
        addDocumentRecordButton.click();
    }

    public void closeDialogClick(){
        List<WebElement> dialogCloseButtons = Browser.Driver.findElements(By.cssSelector( ".dijitDialogCloseIcon" ));
        WebElement popUpDialogCloseButton = dialogCloseButtons.get(1);
        popUpDialogCloseButton.click();
    }

}
