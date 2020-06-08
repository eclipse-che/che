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
package org.eclipse.che.selenium.pageobject.dashboard;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.BOTTOM_CREATE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.DEVFILE_ROW_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ERROR_MESSAGE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ORGANIZATIONS_LIST_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.TOOLBAR_TITLE_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.TOP_CREATE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.TOP_DROPDOWN_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.TOP_EDIT_BUTTON_XPATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.Assert;

/**
 * @author Ann Shumilova
 * @author Ihor Okhrimenko
 */
@Singleton
public class NewWorkspace {

  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;
  private final TestWebElementRenderChecker testWebElementRenderChecker;
  private final AddOrImportForm addOrImportForm;
  private static final int DEFAULT_TIMEOUT = TestTimeoutsConstants.DEFAULT_TIMEOUT;

  @Inject
  public NewWorkspace(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory,
      TestWebElementRenderChecker testWebElementRenderChecker,
      AddOrImportForm addOrImportForm) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.webDriverWaitFactory = webDriverWaitFactory;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
    this.addOrImportForm = addOrImportForm;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String WORKSPACE_NAME_INPUT = "workspace-name-input";
    String ERROR_MESSAGE = "new-workspace-error-message";
    String TOOLBAR_TITLE_ID = "New_Workspace";
    String DEVFILE_ROW_XPATH = "//div[@data-devfile-id='%s']";
    String MACHINE_NAME =
        "//span[contains(@class,'ram-settings-machine-item-item-name') and text()='%s']";
    String ORGANIZATIONS_LIST_ID = "namespace-selector";
    String ORGANIZATION_ITEM = "//md-menu-item[text()='%s']";
    // buttons
    String TOP_CREATE_BUTTON_XPATH = "//button[@name='split-button']";
    String TOP_DROPDOWN_BUTTON_XPATH = "//button[@name='dropdown-toggle']";
    String TOP_EDIT_BUTTON_XPATH = "//span[text()='Create & Proceed Editing']";
    String BOTTOM_CREATE_BUTTON_XPATH = "//*[@id='create-workspace-ready-to-go-button']";
    String CUSTOM_WORKSPACE_TAB_XPATH = "//md-tab-item//span[text()='Custom Workspace']";
    String CUSTOM_WORKSPACE_TITLE_XPATH = "//div[@title='Create Custom Workspace']";
    String WORKSPACE_NAME_INPUT_XPATH = "//input[@name='workspaceName']";
    String OPEN_DIVFILES_LIST_XPATH = "//button[contains(@id,'select-single-typeahead-expanded')]";
    String DEVFILE_ITEM_IN_LIST_XPATH = "//button/span[text()='%s']";
    String SELECTED_DEVFILE_ITEM_XPATH =
        "//div/input[contains(@id,'select-single-typeahead-expanded')]";
    String CREATE_AND_START_BUTTON_XPATH = "//button[@title='Create & Open']";

    String DEFVILE_ITEM_XPATH2 = "//div[@devfile='devfile']//b[text()='%s']";
    String GET_STARTED_TAB_XPATH = "//md-tab-item//span[text()='Get Started']";
    String GET_STARTED_TITLE_XPATH = "//div[@title='Getting Started with Eclipse Che']";
  }

  public enum Devfile {
    APACHE_CAMEL("Apache Camel based projects on Che 7"),
    DOT_NET(".NET Core with Theia IDE"),
    GO("Go with Theia IDE"),
    JAVA_GRADLE("Java Gradle"),
    JAVA_MAVEN("Java Maven"),
    PYTHON("Python with Theia IDE");

    // wsnext-helloworld-openshift
    private final String id;

    Devfile(String id) {
      this.id = id;
    }

    public static Devfile getById(String id) {
      Optional<Devfile> first =
          asList(values()).stream().filter(devfile -> devfile.getId().equals(id)).findFirst();
      first.orElseThrow(
          () -> new RuntimeException(String.format("Devfile with id '%s' not found.", id)));
      return first.get();
    }

    public String getId() {
      return this.id;
    }
  }

  @FindBy(id = TOOLBAR_TITLE_ID)
  WebElement toolbarTitle;

  @FindBy(id = Locators.WORKSPACE_NAME_INPUT_XPATH)
  WebElement workspaceNameInput;

  @FindBy(xpath = BOTTOM_CREATE_BUTTON_XPATH)
  WebElement bottomCreateWorkspaceButton;

  @FindBy(xpath = TOP_CREATE_BUTTON_XPATH)
  WebElement topCreateWorkspaceButton;

  @FindBy(xpath = TOP_DROPDOWN_BUTTON_XPATH)
  WebElement topDropdownButton;

  @FindBy(xpath = TOP_EDIT_BUTTON_XPATH)
  WebElement topEditWorkspaceButton;

  public void clickOnGetStartedTab() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.GET_STARTED_TAB_XPATH));
  }

  public void waitGetStartedTabActive() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.GET_STARTED_TAB_XPATH));
  }

  public void selectDevfileFromGetStartedList(Devfile devfile) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.DEFVILE_ITEM_XPATH2, devfile.getId())));
  }

  public void clickOnCustomWorkspacesTab() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.CUSTOM_WORKSPACE_TAB_XPATH));
  }

  public void waitCustomWorkspacesTab() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.CUSTOM_WORKSPACE_TITLE_XPATH));
  }

  public void typeWorkspaceName(String name) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.WORKSPACE_NAME_INPUT_XPATH));
    seleniumWebDriverHelper.setValue(By.xpath(Locators.WORKSPACE_NAME_INPUT_XPATH), name);
    Assert.assertEquals(getWorkspaceNameValue(), name);
  }

  public String getWorkspaceNameValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(
        By.xpath(Locators.WORKSPACE_NAME_INPUT_XPATH));
  }

  public void openDevfilesList() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.OPEN_DIVFILES_LIST_XPATH));
  }

  public void selectDevfileFromList(Devfile devfile) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.DEVFILE_ITEM_IN_LIST_XPATH, devfile.getId())));
    WaitUtils.sleepQuietly(5);
  }

  public String getSelectedDevfileName() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(
        By.xpath(Locators.SELECTED_DEVFILE_ITEM_XPATH));
  }

  public void clickOnCreateAndOpenButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.CREATE_AND_START_BUTTON_XPATH));
  }

  private void waitButtonDisableState(WebElement button, boolean state) {
    seleniumWebDriverHelper.waitAttributeEqualsTo(button, "aria-disabled", Boolean.toString(state));
  }

  private WebElement waitElementByNameAttribute(String nameAttribute) {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format("//input[@name='%s']", nameAttribute)));
  }

  public void waitWorkspaceNameFieldValue(String expectedName) {
    seleniumWebDriverHelper.waitSuccessCondition(
        (driver) -> getWorkspaceNameValue().equals(expectedName));
  }

  public void waitErrorMessage(String message) {
    seleniumWebDriverHelper.waitTextEqualsTo(By.id(ERROR_MESSAGE), message);
  }

  public void waitErrorMessageDisappearance() {
    seleniumWebDriverHelper.waitInvisibility(By.id(ERROR_MESSAGE));
  }

  public boolean isMachineExists(String machineName) {
    return seleniumWebDriver
            .findElements(By.xpath(format(Locators.MACHINE_NAME, machineName)))
            .size()
        > 0;
  }

  public void waitToolbar() {
    waitToolbar(WIDGET_TIMEOUT_SEC);
  }

  public void waitToolbar(int timeout) {
    seleniumWebDriverHelper.waitVisibility(By.id(TOOLBAR_TITLE_ID), timeout);
  }

  public boolean isDevfileVisible(Devfile devfile) {
    return seleniumWebDriver
            .findElements(By.xpath(format(DEVFILE_ROW_XPATH, devfile.getId())))
            .size()
        > 0;
  }

  public void selectDevfile(Devfile devfile) {
    waitDevfiles(asList(devfile));
    try {
      seleniumWebDriverHelper.waitAndClick(By.xpath(format(DEVFILE_ROW_XPATH, devfile.getId())));
      waitDevfileSelected(devfile);
    } catch (TimeoutException ex) {
      seleniumWebDriverHelper.waitAndClick(By.xpath(format(DEVFILE_ROW_XPATH, devfile.getId())));
    }
  }

  public boolean isCreateWorkspaceButtonEnabled() {
    return !Boolean.valueOf(
        seleniumWebDriverHelper.waitVisibilityAndGetAttribute(
            By.xpath(BOTTOM_CREATE_BUTTON_XPATH), "aria-disabled"));
  }

  public void waitTopCreateWorkspaceButtonDisabled() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(TOP_CREATE_BUTTON_XPATH), "aria-disabled", "true");
  }

  public void waitTopCreateWorkspaceButtonEnabled() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(TOP_CREATE_BUTTON_XPATH), "aria-disabled", "false");
  }

  public void waitBottomCreateWorkspaceButtonEnabled() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(BOTTOM_CREATE_BUTTON_XPATH), "aria-disabled", "false");
  }

  public void waitBottomCreateWorkspaceButtonDisabled() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(BOTTOM_CREATE_BUTTON_XPATH), "aria-disabled", "true");
  }

  public void clickOnCreateButtonAndOpenInIDE() {
    // need to move cursor to button to avoid failure https://github.com/eclipse/che/issues/12309
    seleniumWebDriverHelper.waitVisibility(bottomCreateWorkspaceButton);
    seleniumWebDriverHelper.moveCursorTo(bottomCreateWorkspaceButton);
    bottomCreateWorkspaceButton.click();
  }

  public void clickOnCreateButtonAndEditWorkspace() {
    waitTopCreateButton();

    seleniumWebDriverHelper.waitAndClick(topDropdownButton);
    seleniumWebDriverHelper.waitAndClick(topEditWorkspaceButton);
  }

  public void clickOnBottomCreateButton() {
    seleniumWebDriverHelper.waitAndClick(bottomCreateWorkspaceButton);
  }

  public void openOrganizationsList() {
    seleniumWebDriverHelper.waitAndClick(By.id(ORGANIZATIONS_LIST_ID));
  }

  public void selectOrganizationFromList(String organizationName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.ORGANIZATION_ITEM, organizationName)));
  }

  public WebElement waitTopCreateButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(TOP_CREATE_BUTTON_XPATH), timeout);
  }

  public WebElement waitTopCreateButton() {
    return waitTopCreateButton(DEFAULT_TIMEOUT);
  }

  public void clickOnTopCreateButton() {
    waitTopCreateButton().click();
  }

  public WebElement waitNameField(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(workspaceNameInput, timeout);
  }

  public WebElement waitNameField() {
    return waitNameField(DEFAULT_TIMEOUT);
  }

  public void waitBottomCreateButton(int timeout) {
    seleniumWebDriverHelper.waitVisibility(bottomCreateWorkspaceButton, timeout);
  }

  public void waitBottomCreateButton() {
    waitBottomCreateButton(DEFAULT_TIMEOUT);
  }

  public void waitMainActionButtons(int timeout) {
    waitTopCreateButton(timeout);
    addOrImportForm.waitAddOrImportProjectButton(timeout);
    waitBottomCreateButton(timeout);
  }

  public void waitMainActionButtons() {
    waitMainActionButtons(DEFAULT_TIMEOUT);
  }

  public void waitPageLoad(int timeout) {
    waitToolbar(timeout);
    waitNameField(timeout);
    waitMainActionButtons(timeout);
  }

  public void waitPageLoad() {
    waitPageLoad(DEFAULT_TIMEOUT);
  }

  public List<Devfile> getAvailableDevfiles() {
    return seleniumWebDriverHelper
        .waitPresenceOfAllElements(By.xpath("//div[@data-devfile-id]"))
        .stream()
        .map(webElement -> Devfile.getById(webElement.getAttribute("data-devfile-id")))
        .collect(toList());
  }

  public void waitDevfileSelected(Devfile devfile) {
    String selectedDevfileXpath =
        format(
            "//div[@data-devfile-id='%s' and contains(@class, 'devfile-selector-item-selected')]",
            devfile.getId());
    seleniumWebDriverHelper.waitVisibility(By.xpath(selectedDevfileXpath));
  }

  public List<Devfile> getVisibleDevfiles() {
    return seleniumWebDriverHelper
        .waitPresenceOfAllElements(By.xpath("//div[@data-devfile-id]"))
        .stream()
        .filter(seleniumWebDriverHelper::isVisible)
        .map(webElement -> Devfile.getById(webElement.getAttribute("data-devfile-id")))
        .collect(Collectors.toList());
  }

  public void waitVisibleDevfiles(List<Devfile> expectedVisibleDevfiles) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getVisibleDevfiles().equals(expectedVisibleDevfiles));
  }

  public void waitDevfiles(List<Devfile> expectedDevfiles) {
    expectedDevfiles.forEach(
        devfile ->
            seleniumWebDriverHelper.waitPresence(
                By.xpath(format("//div[@data-devfile-id='%s']", devfile.getId()))));
  }

  public void waitDevfilesCount(int expectedCount) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> expectedCount == getAvailableDevfilesCount());
  }

  public int getAvailableDevfilesCount() {
    return getAvailableDevfiles().size();
  }

  public void waitDevfilesNotPresent(List<Devfile> devfilesIdForChecking) {
    devfilesIdForChecking.forEach(
        devfile ->
            webDriverWaitFactory
                .get()
                .until(
                    (ExpectedCondition<Boolean>)
                        driver -> !getAvailableDevfiles().contains(devfile)));
  }

  public void waitDevfilesOrder(List<Devfile> expectedOrder) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> expectedOrder.equals(getAvailableDevfiles()));
  }

  public void clickNameButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath("//div[@che-column-title='Name']/div"));
  }

  public void clickOnTitlePlaceCoordinate() {
    seleniumWebDriverHelper.moveCursorTo(
        seleniumWebDriverHelper.waitPresence(By.id(TOOLBAR_TITLE_ID)));
    seleniumWebDriverHelper.getAction().click().perform();
  }

  public void clickOnAddStackButton() {
    seleniumWebDriverHelper.waitAndClick(By.id("add-stack-button"));
  }
}
