package dk.openesdh.share.selenium.tests;

import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.AdminToolsPage;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.junit.Assert.assertNotNull;

public class AddUserToGroupTestIT extends AdminToolsPage {

    @FindBy(xpath="//div[@id='deleteDialog']")
    WebElement deleteUserDialog;

    @FindBy(xpath="//div[@id='deleteDialog']/div[@class='ft']/span[@class='button-group']/span[1]/span[1]/button")
    WebElement deleteUserDialogDeleteBtn;

    @FindBy(xpath=("//span[@class='newuser-button']/span[1]/span[1]/button") )
    WebElement newUserBtn;

    @FindBy(name ="-" )
    WebElement userSearchBoxInput;

    @FindBy(xpath ="//input[@type='checkbox' and @name='-']")
    WebElement showAllGroupsCheckbox;

    @FindBy(xpath="//button[contains(text(),'Search')]")
    WebElement defUserSearchButton;

    @Test
    public void addUserToCaseCreatorGroup() {
        this.loginAsUser(User.ADMIN);
        this.addUserToGroup(User.HELENA, "CaseSimpleCreator");
    }

}
