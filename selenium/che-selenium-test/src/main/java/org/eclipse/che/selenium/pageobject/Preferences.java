/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
import org.eclipse.che.selenium.core.client.TestSshServiceClient;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @autor by mmusienko on 9/19/14. */
@Singleton
public class Preferences {
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private final AskDialog askDialog;
  private final AskForValueDialog askForValueDialog;
  private final GitHub gitHub;
  private final SeleniumWebDriver seleniumWebDriver;
  private final TestSshServiceClient testSshServiceClient;

  @Inject
  public Preferences(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      AskDialog askDialog,
      AskForValueDialog askForValueDialog,
      GitHub github,
      TestSshServiceClient testSshServiceClient) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.askDialog = askDialog;
    this.askForValueDialog = askForValueDialog;
    this.gitHub = github;
    this.testSshServiceClient = testSshServiceClient;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private static final Logger LOG = LoggerFactory.getLogger(Preferences.class);

  private interface Locators {
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
    String OK_BUTTON_ID = "window-preferences-storeChanges";
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

  public interface DropDownGitCommitterInformationMenu {
    String COMMITTER = "Committer";
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

  @FindBy(id = Locators.OK_BUTTON_ID)
  private WebElement okBtn;

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

  /** wait preferences form */
  public void waitPreferencesForm() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOf(preferencesForm));
  }

  /** wait closing of the preferences form */
  public void waitPreferencesFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.PREFERENCES_FORM_ID)));
  }

  /**
   * wait appears dropdawn-header with specified mame
   *
   * @param nameMenu name of header (all names describe in public interface )
   */
  public void waitDropDownHeaderMenu(String nameMenu) {
    new WebDriverWait(seleniumWebDriver, 20)
        .until(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath(format(Locators.DROP_DOWN_HEADER_XPATH_WITH_PARAM, nameMenu))));
  }

  /**
   * wait menu in expanded dropdown list
   *
   * @param menu (all menus describe in public interface )
   */
  public void waitMenuInCollapsedDropdown(String menu) {
    new WebDriverWait(seleniumWebDriver, 20)
        .until(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath(format(Locators.MENU_IN_EXPANDED_DROPDOWN_XPATH_WITH_PARAM, menu))));
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
        .findElement(
            By.xpath(format(Locators.MENU_IN_EXPANDED_DROPDOWN_XPATH_WITH_PARAM, nameMenu)))
        .click();
  }

  /** wait ok button click and wait closing the form */
  public void clickOnOkBtn() {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(okBtn));
    okBtn.click();
    loader.waitOnClosed();
  }

  /** wait close button click and wait closing the form */
  public void clickOnCloseBtn() {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(closeBtn));
    closeBtn.click();
    waitPreferencesFormIsClosed();
  }

  public void clickOnGenerateKeyButton() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOf(generateKeyBtn))
        .click();
  }

  public void clickOnGenerateAndUploadToGitHub() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOf(generateAndUploadBtn))
        .click();
  }

  public boolean isSshKeyTableIsEmpty(String expText) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(Locators.SSH_KEYS_TABLE)));
    return sshKeysTable.getAttribute("style").contains(expText);
  }

  public boolean isSshKeyIsPresent(String host) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.SSH_KEYS_TABLE)));
    return sshKeysTable.getText().contains(host);
  }

  public void waitSshKeyIsPresent(final String host) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until((ExpectedCondition<Boolean>) driver -> sshKeysTable.getText().contains(host));
  }

  public void deleteSshKeyByHost(String host) {
    WebElement element =
        seleniumWebDriver.findElement(
            By.xpath("//div[text()='" + host + "']" + Locators.SSH_DELETE_BUTTON_FOR_HOST));
    try {
      new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
          .until(ExpectedConditions.visibilityOf(element))
          .click();
    } catch (StaleElementReferenceException e) {
      WaitUtils.sleepQuietly(2);
      new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
          .until(ExpectedConditions.visibilityOf(element))
          .click();
    }
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
    new WebDriverWait(seleniumWebDriver, 3)
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
    new WebDriverWait(seleniumWebDriver, 3)
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

  /**
   * return the list of items are presented in the 'Error/Warnings' widget
   *
   * @return list of items
   */
  public List<String> getItemsFromErrorWarningsWidget() {
    List<String> itemList =
        Arrays.asList(
            new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(ExpectedConditions.visibilityOf(errorsWarningsTab))
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
      List<WebElement> DropDownList =
          new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
              .until(
                  ExpectedConditions.presenceOfAllElementsLocatedBy(
                      By.xpath(format(Locators.ERRORS_WARNINGS_RADIO_BUTTON, settingsText))));

      new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
          .until(
              ExpectedConditions.visibilityOfElementLocated(
                  By.xpath(format(Locators.ERRORS_WARNINGS_RADIO_BUTTON_BLOCK, settingsText))))
          .click();

      switch (valueOfDropDown) {
        case IGNORE:
          DropDownList.get(0).click();
          break;
        case WARNING:
          DropDownList.get(1).click();
          break;
        default:
          DropDownList.get(2).click();
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
            Arrays.asList(
                new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                    .until(ExpectedConditions.visibilityOf(editorProperties))
                    .getText()
                    .split("\n")));
    settingList.removeAll(Arrays.asList(headerSettings));
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
          .until(
              ExpectedConditions.presenceOfElementLocated(
                  By.xpath(format(Locators.EDITOR_INPUT, settingsItem))))
          .getAttribute("type")
          .equals("checkbox")) {
        WebElement checkbox =
            new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath(format(Locators.EDITOR_INPUT, settingsItem))));
        WebElement spanCheckbox =
            new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath(format(Locators.EDITOR_CHECKBOX_SPAN_XPATH, settingsItem))));
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
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.SSH_UPLOAD_KEY)))
        .click();
    WebElement hostInput =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath(UploadSSHKey.HOST_INPUT)));
    hostInput.clear();
    hostInput.sendKeys(host);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UploadSSHKey.FILE_INPUT)))
        .sendKeys(file.getAbsolutePath());
    WaitUtils.sleepQuietly(3);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UploadSSHKey.UPLOAD_BUTTON)))
        .click();
  }

  /** wait appearance of the Generate SSH key widget */
  public void waitGenerateSshWidget() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(genrateSShKeyWidget));
  }

  /**
   * type title for Generate Ssh key form
   *
   * @param title the name of title for created Ssh key
   */
  public void typeTitleOfSshKey(String title) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(genrateSShKeyTitleInput))
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

  public void regenerateAndUploadSshKeyOnGithub(String githubUsername, String githubPassword)
      throws Exception {
    testSshServiceClient.deleteVCSKey(TestGitHubKeyUploader.GITHUB_COM);

    waitMenuInCollapsedDropdown(Preferences.DropDownSshKeysMenu.VCS);
    selectDroppedMenuByName(Preferences.DropDownSshKeysMenu.VCS);

    loader.waitOnClosed();

    String ideWin = seleniumWebDriver.getWindowHandle();

    // regenerate key and upload it on the gitHub
    clickOnGenerateAndUploadToGitHub();

    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    seleniumWebDriver.switchToNoneCurrentWindow(ideWin);

    gitHub.waitAuthorizationPageOpened();
    gitHub.typeLogin(githubUsername);
    gitHub.typePass(githubPassword);
    gitHub.clickOnSignInButton();

    loader.waitOnClosed();

    if (seleniumWebDriver.getWindowHandles().size() > 1) {
      loader.waitOnClosed();
      gitHub.waitAuthorizeBtn();
      gitHub.clickOnAuthorizeBtn();
      seleniumWebDriver.switchTo().window(ideWin);
    }

    seleniumWebDriver.switchTo().window(ideWin);
    loader.waitOnClosed();
    waitSshKeyIsPresent("github.com");
    loader.waitOnClosed();
    clickOnCloseBtn();
    waitPreferencesFormIsClosed();
  }

  public void clickOnShowArtifactCheckBox() {
    Actions actions = new Actions(seleniumWebDriver);
    actions.click(showArtifactCheckBox).build().perform();
  }
}
