/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.Preferences.Locators.EDITOR_CHECKBOX_SPAN_XPATH;
import static org.eclipse.che.selenium.pageobject.Preferences.Locators.EDITOR_INPUT;
import static org.eclipse.che.selenium.pageobject.Preferences.Locators.ERRORS_WARNINGS_RADIO_BUTTON;
import static org.eclipse.che.selenium.pageobject.Preferences.Locators.ERRORS_WARNINGS_RADIO_BUTTON_BLOCK;
import static org.eclipse.che.selenium.pageobject.Preferences.Locators.MENU_IN_EXPANDED_DROPDOWN_XPATH_WITH_PARAM;
import static org.eclipse.che.selenium.pageobject.Preferences.Locators.SSH_DELETE_BUTTON_FOR_HOST;
import static org.openqa.selenium.Keys.ALT;
import static org.openqa.selenium.Keys.COMMAND;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.PlatformUtils;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @autor by mmusienko on 9/19/14. */
@Singleton
public class Preferences {

  private static final String GITHUB_COM = "github.com";
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private final AskDialog askDialog;
  private final AskForValueDialog askForValueDialog;
  private final GitHub gitHub;
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper webDriverHelper;

  @Inject
  public Preferences(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      AskDialog askDialog,
      AskForValueDialog askForValueDialog,
      GitHub github,
      SeleniumWebDriverHelper webDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.askDialog = askDialog;
    this.askForValueDialog = askForValueDialog;
    this.gitHub = github;
    this.webDriverHelper = webDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private static final Logger LOG = LoggerFactory.getLogger(Preferences.class);

  protected interface Locators {
    String PREFERENCES_FORM_ID = "gwt-debug-window-preferences-scrollPanel-preferences";
    String DROP_DOWN_HEADER_XPATH_WITH_PARAM =
        "//div[@id='gwt-debug-window-preferences-scrollPanel-preferences']//span[text()='%s']";
    String MENU_IN_EXPANDED_DROPDOWN_XPATH_WITH_PARAM =
        "//div[@id='gwt-debug-window-preferences-scrollPanel-preferences']//div[text()='%s']";
    String DEFAULT_RAM_MENU_FIELD = "gwt-debug-window-preferences-ramManager-memory";
    String SSH_GENERATE_KEY = "gwt-debug-sshKeys-generate";
    String SSH_UPLOAD_KEY = "//button[@id='gwt-debug-sshKeys-upload']";
    String SSH_GENERATE_AND_ADD_TO_GITHUB = "gwt-debug-gitSshKeys-generateGithubKey";
    String SSH_KEYS_TABLE = "//table[@id='gwt-debug-sshKeys-cellTable-keys']/tbody[1]";
    String SSH_DELETE_BUTTON_FOR_HOST = "/following::button[text()='Delete']";
    String SAVE_BUTTON_ID = "window-preferences-storeChanges";
    String REFRESH_BUTTON_ID = "window-preferences-refresh";
    String CLOSE_BTN_ID = "window-preferences-close";
    String COMMITTER_INPUT_NAME = "gwt-debug-committer-preferences-name";
    String COMMITTER_INPUT_EMAIL = "gwt-debug-committer-preferences-email";
    String ERRORS_WARNINGS_TAB =
        "//div[@id='gwt-debug-window-preferences-scrollPanel-preferences']/following::div[4]";
    String ERRORS_WARNINGS_RADIO_BUTTON = "//div[text()='%s']/following::div[1]//span";
    String ERRORS_WARNINGS_RADIO_BUTTON_BLOCK = "//div[text()='%s']/following::div[1]";
    String EDITOR_PROPERTIES_XPATH = "//div[@id='gwt-debug-editorSectionsPanel']";
    String EDITOR_CHECKBOX_SPAN_XPATH = "//div[text()='%s']/following::div[1]//span";
    String EDITOR_INPUT = "//div[text()='%s']/following::div[1]//input";
    String GENERATE_SSH_KEY_WIDGET_MAIN_FORM = "gwt-debug-askValueDialog-window";
    String TITLE_INPUT_GENERATE_WIDGET =
        "div#gwt-debug-askValueDialog-window input#gwt-debug-askValueDialog-textBox";
    String SHOW_ARTIFACT_CHECKBOX =
        "//input[@id='gwt-debug-window-preferences-plugins-maven-showArtifactId-input']";
    String SET_CONTRIBUTE_CHECKBOX_ID =
        "gwt-debug-preferences-git-contribute-activateByProjectSelection";
    String SHOW_CONTRIBUTE_CHECKBOX_ID =
        "gwt-debug-preferences-git-contribute-activateByProjectSelection-input";
    String ADD_SCHEMA_URL_BUTTON_ID = "gwt-debug-preferences-addUrl";
    String ADD_SCHEMA_URL_INPUT_ID = "gwt-debug-askValueDialog-textBox";
    String DELETE_SCHEMA_BUTTON_XPATH =
        "//table[@id='gwt-debug-preferences-cellTable-keys']//button";
  }

  public interface DropDownListsHeaders {
    String KEYS_SETTINGS = "SSH";
  }

  public interface DropDownIdeSettingsMenus {
    String EDITOR = "Editor";
  }

  public interface DropDownSshKeysMenu {
    String VCS = "VCS";
    String MACHINE = "Machine";
  }

  public interface DropDownLanguageServerSettings {
    String YAML = "Yaml";
  }

  public interface DropDownGitInformationMenu {
    String COMMITTER = "Committer";
    String CONTRIBUTE_PREFERENCES = "Contribute";
  }

  public interface DropDownJavaCompilerMenu {
    String ERRORS_WARNINGS = "Errors/Warnings";
  }

  public interface UploadSSHKey {
    String HOST_INPUT = "//input[@name='name']";
    String FILE_INPUT = "//input[@name='privateKey']";
    String UPLOAD_BUTTON = "//button[@id='sshKeys-upload']";
    String CANCEL_BUTTON = "//button[@id='sshKeys-cancel']";
  }

  public enum DropDownValueForErrorWaitingWidget {
    IGNORE,
    WARNING,
    ERROR
  }

  public enum FlagForEditorWidget {
    CHECK,
    UNCHECK
  }

  @FindBy(id = Locators.PREFERENCES_FORM_ID)
  private WebElement preferencesForm;

  @FindBy(id = Locators.DEFAULT_RAM_MENU_FIELD)
  private WebElement defaulRAMField;

  @FindBy(id = Locators.SAVE_BUTTON_ID)
  private WebElement saveBtn;

  @FindBy(id = Locators.REFRESH_BUTTON_ID)
  private WebElement refreshBtn;

  @FindBy(id = Locators.CLOSE_BTN_ID)
  private WebElement closeBtn;

  @FindBy(xpath = Locators.SSH_KEYS_TABLE)
  private WebElement sshKeysTable;

  @FindBy(id = Locators.SSH_GENERATE_KEY)
  private WebElement generateKeyBtn;

  @FindBy(id = Locators.SSH_GENERATE_AND_ADD_TO_GITHUB)
  private WebElement generateAndUploadBtn;

  @FindBy(id = Locators.COMMITTER_INPUT_NAME)
  private WebElement nameCommitterInput;

  @FindBy(id = Locators.COMMITTER_INPUT_EMAIL)
  private WebElement emailCommitterInput;

  @FindBy(xpath = Locators.ERRORS_WARNINGS_TAB)
  private WebElement errorsWarningsTab;

  @FindBy(xpath = Locators.EDITOR_PROPERTIES_XPATH)
  private WebElement editorProperties;

  @FindBy(id = Locators.GENERATE_SSH_KEY_WIDGET_MAIN_FORM)
  private WebElement genrateSShKeyWidget;

  @FindBy(css = Locators.TITLE_INPUT_GENERATE_WIDGET)
  private WebElement genrateSShKeyTitleInput;

  @FindBy(xpath = Locators.SHOW_ARTIFACT_CHECKBOX)
  private WebElement showArtifactCheckBox;

  @FindBy(id = Locators.SET_CONTRIBUTE_CHECKBOX_ID)
  private WebElement setContributeCheckbox;

  @FindBy(id = Locators.SHOW_CONTRIBUTE_CHECKBOX_ID)
  private WebElement showContributeCheckbox;

  @FindBy(id = Locators.ADD_SCHEMA_URL_BUTTON_ID)
  private WebElement addSchemaUrlButton;

  @FindBy(id = Locators.ADD_SCHEMA_URL_INPUT_ID)
  private WebElement addSchemaUrlInput;

  @FindBy(xpath = Locators.DELETE_SCHEMA_BUTTON_XPATH)
  private WebElement deleteSchemaButton;

  /** wait preferences form */
  public void waitPreferencesForm() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(preferencesForm));
  }

  /** wait closing of the preferences form */
  public void waitPreferencesFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.id(Locators.PREFERENCES_FORM_ID)));
  }

  /**
   * wait appears dropdawn-header with specified mame
   *
   * @param nameMenu name of header (all names describe in public interface )
   */
  public void waitDropDownHeaderMenu(String nameMenu) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            visibilityOfAllElementsLocatedBy(
                By.xpath(format(Locators.DROP_DOWN_HEADER_XPATH_WITH_PARAM, nameMenu))));
  }

  /**
   * wait menu in expanded dropdown list
   *
   * @param menu (all menus describe in public interface )
   */
  public void waitMenuInCollapsedDropdown(String menu) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            visibilityOfAllElementsLocatedBy(
                By.xpath(format(MENU_IN_EXPANDED_DROPDOWN_XPATH_WITH_PARAM, menu))));
  }

  /**
   * wait menu with specified name and select it
   *
   * @param nameMenu
   */
  public void selectDroppedMenuByName(String nameMenu) {
    loader.waitOnClosed();
    waitMenuInCollapsedDropdown(nameMenu);
    seleniumWebDriver
        .findElement(By.xpath(format(MENU_IN_EXPANDED_DROPDOWN_XPATH_WITH_PARAM, nameMenu)))
        .click();
  }

  /** wait ok button click and wait closing the form */
  public void clickOnOkBtn() {
    webDriverHelper.waitAndClick(saveBtn);
    loader.waitOnClosed();
  }

  /**
   * determines whether or not 'Preferences' form opened
   *
   * @return true if the form is opened
   */
  public boolean isPreferencesFormOpened() {
    return webDriverHelper.isVisible(closeBtn);
  }

  /**
   * defines a state the 'Save' button, enabled or not
   *
   * @return true if the button is enabled
   */
  public boolean isSaveButtonIsEnabled() {
    return webDriverHelper.waitVisibilityAndGetEnableState(saveBtn);
  }

  /** wait and click on the 'Refresh' button */
  public void clickRefreshButton() {
    webDriverHelper.waitAndClick(refreshBtn);
  }

  /** click on the 'Close' button */
  public void clickOnCloseButton() {
    webDriverHelper.waitAndClick(closeBtn);
  }

  /** click on the 'Close' button and wait closing the form */
  public void closeForm() {
    webDriverHelper.waitAndClick(closeBtn);
    waitPreferencesFormIsClosed();
  }

  public void clickOnGenerateKeyButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(generateKeyBtn))
        .click();
  }

  public void clickOnAddSchemaUrlButton() {
    webDriverHelper.waitAndClick(addSchemaUrlButton);
  }

  public void addSchemaUrl(String schemaName) {
    webDriverHelper.waitVisibility(addSchemaUrlInput);
    addSchemaUrlInput.sendKeys(schemaName);
    webDriverHelper.waitAndClick(By.id("askValue-dialog-ok"));
  }

  public void deleteSchema() {
    webDriverHelper.waitAndClick(deleteSchemaButton);
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
  }

  public void clickOnGenerateAndUploadToGitHub() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(generateAndUploadBtn))
        .click();
  }

  public boolean isSshKeyIsPresent(String host) {
    try {
      new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
          .until(presenceOfElementLocated(By.xpath(Locators.SSH_KEYS_TABLE)));
      return sshKeysTable.getText().contains(host);
    } catch (TimeoutException e) {
      return false;
    }
  }

  // timeout is changed to 40 sec, is related to running tests on ocp platform
  public void waitSshKeyIsPresent(final String host) {
    new WebDriverWait(seleniumWebDriver, WIDGET_TIMEOUT_SEC)
        .until((ExpectedCondition<Boolean>) driver -> isSshKeyIsPresent(host));
  }

  public void deleteSshKeyByHost(String host) {
    WebElement element =
        seleniumWebDriver.findElement(
            By.xpath("//div[text()='" + host + "']" + SSH_DELETE_BUTTON_FOR_HOST));
    try {
      new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
          .until(visibilityOf(element))
          .click();
    } catch (StaleElementReferenceException e) {
      WaitUtils.sleepQuietly(2);
      new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
          .until(visibilityOf(element))
          .click();
    }

    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
  }

  /**
   * type a name of the committer in the 'Preferences'
   *
   * @param nameCommitter is a name of the committer
   */
  public void typeNameCommitter(String nameCommitter) {
    nameCommitterInput.clear();
    nameCommitterInput.sendKeys(nameCommitter);
  }

  /**
   * wait a name of the committer in the 'Preferences'
   *
   * @param nameCommitter is a name of the committer
   */
  public void waitInputNameCommitter(final String nameCommitter) {
    new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> nameCommitterInput.getAttribute("value").contains(nameCommitter));
  }

  /**
   * type and wait a name of the committer in the 'Preferences'
   *
   * @param nameCommitter is a name of the committer
   */
  public void typeAndWaitNameCommitter(String nameCommitter) {
    typeNameCommitter(nameCommitter);
    waitInputNameCommitter(nameCommitter);
  }

  /**
   * type an email of the committer in the 'Preferences'
   *
   * @param nameCommitter is an email of the committer
   */
  public void typeEmailCommitter(String nameCommitter) {
    emailCommitterInput.clear();
    emailCommitterInput.sendKeys(nameCommitter);
  }

  /**
   * wait an email of the committer in the 'Preferences'
   *
   * @param emailCommitter is an email of the committer
   */
  public void waitInputEmailCommitter(final String emailCommitter) {
    new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> emailCommitterInput.getAttribute("value").contains(emailCommitter));
  }

  /**
   * type and wait an email of the committer in the 'Preferences'
   *
   * @param emailCommitter is an email of the committer
   */
  public void typeAndWaitEmailCommitter(String emailCommitter) {
    typeEmailCommitter(emailCommitter);
    waitInputEmailCommitter(emailCommitter);
  }

  /** clicks on the 'Contribute' checkbox */
  public void clickOnContributeCheckbox() {
    webDriverHelper.waitAndClick(setContributeCheckbox);
  }

  /** call the 'Contribute' by hot key */
  public void callContributeActionByHotKey() {
    webDriverHelper.sendKeys(Keys.chord(CONTROL, PlatformUtils.isMac() ? COMMAND : ALT, "6"));
  }

  /** wait the 'Contribute' checkbox is selected */
  public void waitContributeCheckboxIsSelected() {
    webDriverHelper.waitElementIsSelected(showContributeCheckbox);
  }

  /** wait the 'Contribute' checkbox is not selected */
  public void waitContributeCheckboxIsNotSelected() {
    webDriverHelper.waitElementIsNotSelected(showContributeCheckbox);
  }

  /**
   * Set 'Contribute' checkbox to specified state and wait it state
   *
   * @param state state of checkbox (true if checkbox must be selected)
   */
  public void setContributeCheckbox(boolean state) {
    webDriverHelper.waitAndSetCheckbox(showContributeCheckbox, setContributeCheckbox, state);
  }

  /**
   * return the list of items are presented in the 'Error/Warnings' widget
   *
   * @return list of items
   */
  public List<String> getItemsFromErrorWarningsWidget() {
    List<String> itemList =
        asList(
            new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(visibilityOf(errorsWarningsTab))
                .getText()
                .split("((\n)(warning|ignore|error)(\n))|((\n)(warning|ignore|error))"));
    return itemList;
  }

  /**
   * set all settings in the 'Error/Warnings' widget
   *
   * @param valueOfDropDown value of drop down
   */
  public void setAllSettingsInErrorWaitingWidget(
      DropDownValueForErrorWaitingWidget valueOfDropDown) {

    for (String settingsText : getItemsFromErrorWarningsWidget()) {
      List<WebElement> dropDownList =
          new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
              .until(
                  presenceOfAllElementsLocatedBy(
                      By.xpath(format(ERRORS_WARNINGS_RADIO_BUTTON, settingsText))));

      new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
          .until(
              visibilityOfElementLocated(
                  By.xpath(format(ERRORS_WARNINGS_RADIO_BUTTON_BLOCK, settingsText))))
          .click();

      switch (valueOfDropDown) {
        case IGNORE:
          webDriverHelper.waitAndClick(dropDownList.get(0));
          break;
        case WARNING:
          webDriverHelper.waitAndClick(dropDownList.get(1));
          break;
        default:
          webDriverHelper.waitAndClick(dropDownList.get(2));
          break;
      }
    }
  }

  /**
   * return settings list from the editor settings section
   *
   * @param headerSettings array header settings
   * @return list of settings
   */
  public List<String> getAllSettingsFromEditorWidget(String[] headerSettings) {
    List<String> settingList =
        new ArrayList<>(
            asList(
                new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                    .until(visibilityOf(editorProperties))
                    .getText()
                    .split("\n")));
    settingList.removeAll(asList(headerSettings));
    return settingList;
  }

  /**
   * set all flag in once position (check or uncheck) on the editor settings section
   *
   * @param valueOfFlag value of flag
   * @param settingsList list of settings
   */
  public void setAllCheckboxSettingsInEditorWidget(
      FlagForEditorWidget valueOfFlag, List<String> settingsList) {
    for (String settingsItem : settingsList) {
      if (new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
          .until(presenceOfElementLocated(By.xpath(format(EDITOR_INPUT, settingsItem))))
          .getAttribute("type")
          .equals("checkbox")) {
        WebElement checkbox =
            new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(presenceOfElementLocated(By.xpath(format(EDITOR_INPUT, settingsItem))));
        WebElement spanCheckbox =
            new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(
                    presenceOfElementLocated(
                        By.xpath(format(EDITOR_CHECKBOX_SPAN_XPATH, settingsItem))));
        switch (valueOfFlag) {
          case CHECK:
            if (!checkbox.isSelected()) {
              actionsFactory.createAction(seleniumWebDriver).moveToElement(spanCheckbox).perform();
              spanCheckbox.click();
            }
            break;
          default:
            if (checkbox.isSelected()) {
              actionsFactory.createAction(seleniumWebDriver).moveToElement(spanCheckbox).perform();
              spanCheckbox.click();
            }
            break;
        }
      }
    }
  }

  /**
   * Upload private SSH key
   *
   * @param host
   * @param filePath path to file with ssh key
   */
  public void uploadPrivateSshKey(String host, String filePath) {
    File file = new File(filePath);
    LOG.info("Absolute path to private SSH key: {}", file.getAbsolutePath());
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.SSH_UPLOAD_KEY)))
        .click();
    WebElement hostInput =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(visibilityOfElementLocated(By.xpath(UploadSSHKey.HOST_INPUT)));
    hostInput.clear();
    hostInput.sendKeys(host);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(UploadSSHKey.FILE_INPUT)))
        .sendKeys(file.getAbsolutePath());
    WaitUtils.sleepQuietly(3);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(UploadSSHKey.UPLOAD_BUTTON)))
        .click();
  }

  /** wait appearance of the Generate SSH key widget */
  public void waitGenerateSshWidget() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(genrateSShKeyWidget));
  }

  /**
   * type title for Generate Ssh key form
   *
   * @param title the name of title for created Ssh key
   */
  public void typeTitleOfSshKey(String title) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(genrateSShKeyTitleInput))
        .sendKeys(title);
  }

  /** click on Ok btn of Generate Ssh key form */
  public void clickOnOkBtnOfGenerateSshKeyWidget() {
    loader.waitOnClosed();
    askForValueDialog.clickOkBtn();
    loader.waitOnClosed();
  }

  /**
   * wait opening the Generate Ssh key eneter the title ckick on ok btn and wait closing the widget.
   * Wait that Ssh key presents in the list.
   *
   * @param titleOfKey
   */
  public void generateNewSshKey(String titleOfKey) {
    waitGenerateSshWidget();
    typeTitleOfSshKey(titleOfKey);
    clickOnOkBtnOfGenerateSshKeyWidget();
    askDialog.clickCancelBtn();
    askDialog.waitFormToClose();
    waitSshKeyIsPresent(titleOfKey);
  }

  public void generateAndUploadSshKeyOnGithub(String githubUsername, String githubPassword)
      throws Exception {
    waitMenuInCollapsedDropdown(Preferences.DropDownSshKeysMenu.VCS);
    selectDroppedMenuByName(Preferences.DropDownSshKeysMenu.VCS);

    loader.waitOnClosed();

    // delete github keu if it exists
    if (isSshKeyIsPresent(GITHUB_COM)) {
      deleteSshKeyByHost(GITHUB_COM);
    }

    String ideWin = seleniumWebDriver.getWindowHandle();

    // regenerate key and upload it on the gitHub
    clickOnGenerateAndUploadToGitHub();

    loader.waitOnClosed();

    // check if github key has been uploaded without authorization on github.com
    if (isSshKeyIsPresent(GITHUB_COM)) {
      closeForm();
      waitPreferencesFormIsClosed();
      return;
    }

    // login to github
    askDialog.waitFormToOpen(25);
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    webDriverHelper.switchToNextWindow(ideWin);

    gitHub.waitAuthorizationPageOpened();
    gitHub.typeLogin(githubUsername);
    gitHub.typePass(githubPassword);
    gitHub.clickOnSignInButton();

    // it is needed for specified case when the github authorize page is not appeared
    WaitUtils.sleepQuietly(2);

    // authorize on github.com
    if (seleniumWebDriver.getWindowHandles().size() > 1) {
      gitHub.waitAuthorizeBtn();
      gitHub.clickOnAuthorizeBtn();
      seleniumWebDriver.switchTo().window(ideWin);
    }

    seleniumWebDriver.switchTo().window(ideWin);
    loader.waitOnClosed();
    waitSshKeyIsPresent(GITHUB_COM);
    loader.waitOnClosed();
    closeForm();
    waitPreferencesFormIsClosed();
  }

  public void clickOnShowArtifactCheckBox() {
    Actions actions = new Actions(seleniumWebDriver);
    actions.click(showArtifactCheckBox).build().perform();
  }
}
