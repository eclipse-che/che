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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ERROR_MESSAGE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ORGANIZATIONS_LIST_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.STACK_ROW_XPATH;
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
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;

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
    String STACK_ROW_XPATH = "//div[@data-devfile-id='%s']";
    String MACHINE_NAME =
        "//span[contains(@class,'ram-settings-machine-item-item-name') and text()='%s']";
    String ORGANIZATIONS_LIST_ID = "namespace-selector";
    String ORGANIZATION_ITEM = "//md-menu-item[text()='%s']";
    // buttons
    String TOP_CREATE_BUTTON_XPATH = "//button[@name='split-button']";
    String TOP_DROPDOWN_BUTTON_XPATH = "//button[@name='dropdown-toggle']";
    String TOP_EDIT_BUTTON_XPATH = "//span[text()='Create & Proceed Editing']";
    String BOTTOM_CREATE_BUTTON_XPATH = "//che-button-save-flat/button[@name='saveButton']";
  }

  public enum Stack {
    APACHE_CAMEL("Apache Camel based projects on Che 7"),
    DOT_NET(".NET Core with Theia IDE"),
    GO("Go with Theia IDE"),
    JAVA_GRADLE("Java Gradle"),
    JAVA_MAVEN("Java Maven"),
    PYTHON("Python with Theia IDE");

    // wsnext-helloworld-openshift
    private final String id;

    Stack(String id) {
      this.id = id;
    }

    public static Stack getById(String id) {
      Optional<Stack> first =
          asList(values()).stream().filter(stack -> stack.getId().equals(id)).findFirst();
      first.orElseThrow(
          () -> new RuntimeException(String.format("Stack with id '%s' not found.", id)));
      return first.get();
    }

    public String getId() {
      return this.id;
    }
  }

  @FindBy(id = TOOLBAR_TITLE_ID)
  WebElement toolbarTitle;

  @FindBy(id = Locators.WORKSPACE_NAME_INPUT)
  WebElement workspaceNameInput;

  @FindBy(xpath = BOTTOM_CREATE_BUTTON_XPATH)
  WebElement bottomCreateWorkspaceButton;

  @FindBy(xpath = TOP_CREATE_BUTTON_XPATH)
  WebElement topCreateWorkspaceButton;

  @FindBy(xpath = TOP_DROPDOWN_BUTTON_XPATH)
  WebElement topDropdownButton;

  @FindBy(xpath = TOP_EDIT_BUTTON_XPATH)
  WebElement topEditWorkspaceButton;

  private void waitButtonDisableState(WebElement button, boolean state) {
    seleniumWebDriverHelper.waitAttributeEqualsTo(button, "aria-disabled", Boolean.toString(state));
  }

  private WebElement waitElementByNameAttribute(String nameAttribute) {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format("//input[@name='%s']", nameAttribute)));
  }

  public void typeWorkspaceName(String name) {
    seleniumWebDriverHelper.setValue(workspaceNameInput, name);
  }

  public String getWorkspaceNameValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(workspaceNameInput);
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

  public boolean isStackVisible(Stack stack) {
    return seleniumWebDriver.findElements(By.xpath(format(STACK_ROW_XPATH, stack.getId()))).size()
        > 0;
  }

  public void selectStack(Stack stack) {
    waitStacks(asList(stack));
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(STACK_ROW_XPATH, stack.getId())));
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

  public List<Stack> getAvailableStacks() {
    return seleniumWebDriverHelper
        .waitPresenceOfAllElements(By.xpath("//div[@data-devfile-id]"))
        .stream()
        .map(webElement -> Stack.getById(webElement.getAttribute("data-devfile-id")))
        .collect(toList());
  }

  public void waitStackSelected(Stack stack) {
    String selectedStackXpath =
        format(
            "//div[@data-devfile-id='%s' and contains(@class, 'devfile-selector-item-selected')]",
            stack.getId());
    seleniumWebDriverHelper.waitVisibility(By.xpath(selectedStackXpath));
  }

  public List<Stack> getVisibleStacks() {
    return seleniumWebDriverHelper
        .waitPresenceOfAllElements(By.xpath("//div[@data-devfile-id]"))
        .stream()
        .filter(seleniumWebDriverHelper::isVisible)
        .map(webElement -> Stack.getById(webElement.getAttribute("data-devfile-id")))
        .collect(Collectors.toList());
  }

  public void waitVisibleStacks(List<Stack> expectedVisibleStacks) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getVisibleStacks().equals(expectedVisibleStacks));
  }

  public void waitStacks(List<Stack> expectedStacks) {
    expectedStacks.forEach(
        stack ->
            seleniumWebDriverHelper.waitPresence(
                By.xpath(format("//div[@data-devfile-id='%s']", stack.getId()))));
  }

  public void waitStacksCount(int expectedCount) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> expectedCount == getAvailableStacksCount());
  }

  public int getAvailableStacksCount() {
    return getAvailableStacks().size();
  }

  public void waitStacksNotPresent(List<Stack> stacksIdForChecking) {
    stacksIdForChecking.forEach(
        stack ->
            webDriverWaitFactory
                .get()
                .until(
                    (ExpectedCondition<Boolean>) driver -> !getAvailableStacks().contains(stack)));
  }

  public void waitStacksOrder(List<Stack> expectedOrder) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> expectedOrder.equals(getAvailableStacks()));
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
