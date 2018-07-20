/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.dashboard;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ALL_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.BOTTOM_CREATE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.CREATE_STACK_DIALOG_FORM_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.DECREMENT_MEMORY_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.EDIT_WORKSPACE_DIALOG_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ERROR_MESSAGE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.FILTERS_INPUT_TAGS_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.FILTERS_INPUT_TAG_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.FILTER_SELECTED_SUGGESTION_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.FILTER_SUGGESTION_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.INCREMENT_MEMORY_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.MULTI_MACHINE_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.OPEN_IN_IDE_DIALOG_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ORGANIZATIONS_LIST_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.QUICK_START_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.SINGLE_MACHINE_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.STACK_ROW_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.TOOLBAR_TITLE_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.TOP_CREATE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.WORKSPACE_CREATED_DIALOG;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.WORKSPACE_CREATED_DIALOG_CLOSE_BUTTON_XPATH;
import static org.openqa.selenium.Keys.BACK_SPACE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.utils.WaitUtils;
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
    String SELECT_ALL_STACKS_TAB = "all-stacks-button";
    String SELECT_QUICK_START_STACKS_TAB = "quick-start-button";
    String SELECT_SINGLE_MACHINE_STACKS_TAB = "single-machine-button";
    String SELECT_MULTI_MACHINE_STACKS_TAB = "multi-machine-button";
    String ADD_STACK_BUTTON = "search-stack-input";
    String FILTERS_STACK_BUTTON = "filter-stacks-button";
    String FILTER_STACK_INPUT = "filter-stack-input";
    String FILTER_SUGGESTION_TEXT =
        "//div[contains(@class,'stack-library-filter-suggestion-text')]";
    String FILTER_SUGGESTION_BUTTON =
        "//div[@name='suggestionText' and text()='%s']/../div[@name='suggestionButton']";
    String FILTER_SELECTED_SUGGESTION_BUTTON = "//button[@class='md-chip-remove ng-scope']";
    String SEARCH_INPUT = "//div[@id='search-stack-input']//input";
    String CLEAR_INPUT = "//div[@id='search-stack-input']//div[@role='button']";
    String STACK_ROW_XPATH = "//div[@data-stack-id='%s']";
    String MACHINE_NAME =
        "//span[contains(@class,'ram-settings-machine-item-item-name') and text()='%s']";
    String MACHINE_IMAGE = "//span[@class='ram-settings-machine-item-secondary-color']";
    String DECREMENT_MEMORY_BUTTON =
        "//*[@id='machine-%s-ram']//button[@aria-label='Decrement memory']";
    String INCREMENT_MEMORY_BUTTON =
        "//*[@id='machine-%s-ram']//button[@aria-label='Increment memory']";
    String MACHINE_RAM_VALUE = "//*[@id='machine-%s-ram']//input";
    String WORKSPACE_CREATED_DIALOG = "//md-dialog/che-popup[@title='Workspace Is Created']";
    String WORKSPACE_CREATED_DIALOG_CLOSE_BUTTON_XPATH =
        "//md-dialog/che-popup[@title='Workspace Is Created']//i";
    String EDIT_WORKSPACE_DIALOG_BUTTON = "//che-button-primary//span[text()='Edit']";
    String OPEN_IN_IDE_DIALOG_BUTTON = "//che-button-default//span[text()='Open In IDE']";
    String ORGANIZATIONS_LIST_ID = "namespace-selector";
    String ORGANIZATION_ITEM = "//md-menu-item[text()='%s']";
    String FILTERS_INPUT_TAGS_XPATH = "//div[@class='md-chip-content']";
    String FILTERS_INPUT_TAG_XPATH_TEMPLATE = "//div[@class='md-chip-content']//div[text()='%s']";
    String CREATE_STACK_DIALOG_FORM_XPATH =
        "//div[contains(@class, 'md-dialog-container ng-scope')]/md-dialog";

    // buttons
    String TOP_CREATE_BUTTON_XPATH = "//button[@name='split-button']";
    String BOTTOM_CREATE_BUTTON_XPATH = "//che-button-save-flat/button[@name='saveButton']";
    String ALL_BUTTON_ID = "all-stacks-button";
    String QUICK_START_BUTTON_ID = "quick-start-button";
    String SINGLE_MACHINE_BUTTON_ID = "single-machine-button";
    String MULTI_MACHINE_BUTTON_ID = "multi-machine-button";
  }

  public enum Stack {
    BLANK("blank-default"),
    JAVA("java-default"),
    JAVA_MYSQL("java-mysql"),
    DOT_NET("dotnet-default"),
    ANDROID("android-default"),
    CPP("cpp-default"),
    CENTOS_BLANK("centos"),
    CENTOS_GO("centos-go"),
    CENTOS_NODEJS("nodejs6"),
    CENTOS_WILDFLY_SWARM("wildfly-swarm"),
    CEYLON_WITH_JAVA_JAVASCRIPT("ceylon-java-javascript-dart-centos"),
    DEBIAN("debian"),
    DEBIAN_LSP("debianlsp"),
    ECLIPSE_CHE("che-in-che"),
    ECLIPSE_VERTX("vert.x"),
    GO("go-default"),
    JAVA_CENTOS("java-centos"),
    JAVA_THEIA_DOCKER("java-theia-docker"),
    JAVA_THEIA_OPENSHIFT("java-theia-openshift"),
    JAVA_MYSQL_CENTOS("java-centos-mysql"),
    JAVA_DEBIAN("java-debian"),
    KOTLIN("kotlin-default"),
    NODE("node-default"),
    OPENSHIFT_SQL("openshift-sql"),
    PHP("php-default"),
    PHP_GAE("php-gae"),
    PHP_5_6("php5.6-default"),
    PLATFORMIO("platformio"),
    PYTHON("python-default"),
    PYTHON_2_7("python-2.7"),
    PYTHON_GAE("python-gae"),
    RAILS("rails-default"),
    OPENSHIFT("openshift-default"),
    HADOOP("hadoop-default"),
    SELENIUM("selenium"),
    SPRING_BOOT("spring-boot"),
    TOM_EE("tomee-default"),
    UBUNTU("ubuntu"),
    ZEND("zend");

    private final String id;

    Stack(String id) {
      this.id = id;
    }

    public String getId() {
      return this.id;
    }
  }

  @FindBy(id = Locators.FILTERS_STACK_BUTTON)
  WebElement filtersStackButton;

  @FindBy(id = Locators.FILTER_STACK_INPUT)
  WebElement filterStackInput;

  @FindBy(id = TOOLBAR_TITLE_ID)
  WebElement toolbarTitle;

  @FindBy(id = Locators.WORKSPACE_NAME_INPUT)
  WebElement workspaceNameInput;

  @FindBy(xpath = BOTTOM_CREATE_BUTTON_XPATH)
  WebElement bottomCreateWorkspaceButton;

  @FindBy(xpath = Locators.SEARCH_INPUT)
  WebElement searchInput;

  @FindBy(xpath = Locators.CLEAR_INPUT)
  WebElement clearInput;

  @FindBy(id = Locators.SELECT_ALL_STACKS_TAB)
  WebElement selectAllStacksTab;

  @FindBy(id = Locators.SELECT_QUICK_START_STACKS_TAB)
  WebElement selectQuickStartStacksTab;

  @FindBy(id = Locators.SELECT_SINGLE_MACHINE_STACKS_TAB)
  WebElement selectSingleMachineStacksTab;

  @FindBy(id = Locators.SELECT_MULTI_MACHINE_STACKS_TAB)
  WebElement selectMultiMachineStacksTab;

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

  public void clickOnIncrementMemoryButton(String machineName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(INCREMENT_MEMORY_BUTTON, machineName)));
  }

  public void clickAndHoldIncrementMemoryButton(String machineName, int holdingTimeout) {
    seleniumWebDriverHelper.clickAndHoldElementDuringTimeout(
        By.xpath(format(INCREMENT_MEMORY_BUTTON, machineName)), holdingTimeout);
  }

  public void clickAndHoldDecrementMemoryButton(String machineName, int holdingTimeout) {
    seleniumWebDriverHelper.clickAndHoldElementDuringTimeout(
        By.xpath(format(DECREMENT_MEMORY_BUTTON, machineName)), holdingTimeout);
  }

  public void waitRamValueInSpecifiedRange(
      String machineName, double lowestValue, double highestValue) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  double ramValue = getRAM(machineName);
                  return ramValue >= lowestValue && ramValue <= highestValue;
                });
  }

  public void clickOnDecrementMemoryButton(String machineName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(DECREMENT_MEMORY_BUTTON, machineName)));
  }

  public double getRAM(String machineName) {
    String amountOfRam =
        seleniumWebDriverHelper.waitVisibilityAndGetValue(
            By.xpath(format(Locators.MACHINE_RAM_VALUE, machineName)));
    return Double.parseDouble(amountOfRam);
  }

  public void waitRamValue(String machineName, double expectedValue) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> expectedValue == getRAM(machineName));
  }

  public void setMachineRAM(String machineName, double value) {
    seleniumWebDriverHelper.setValue(
        By.xpath(format(Locators.MACHINE_RAM_VALUE, machineName)), Double.toString(value));
  }

  public void clickOnFiltersButton() {
    seleniumWebDriverHelper.waitAndClick(filtersStackButton);
  }

  public void typeToFiltersInput(String value) {
    seleniumWebDriverHelper.setValue(filterStackInput, value);
  }

  public void deleteLastTagFromInputTagsField() {
    seleniumWebDriverHelper.waitAndSendKeysTo(filterStackInput, BACK_SPACE.toString());

    // one "backspace" should sent exactly to defined tag (for activating) and next "backspace"
    // should be a simple button pressing
    seleniumWebDriverHelper.sendKeys(BACK_SPACE.toString());
  }

  public void waitFiltersFormOpened() {
    seleniumWebDriverHelper.waitVisibility(filterStackInput);
  }

  public void waitFiltersFormClosed() {
    seleniumWebDriverHelper.waitInvisibility(filterStackInput);
  }

  public String getTextFromFiltersInput() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(filterStackInput);
  }

  private List<WebElement> getTagsFromFiltersInput() {
    String tagsXpath = FILTERS_INPUT_TAGS_XPATH + "/md-chip-template";

    seleniumWebDriverHelper.waitVisibility(By.xpath(tagsXpath));

    return seleniumWebDriverHelper.waitVisibilityOfAllElements(By.xpath(tagsXpath));
  }

  private String getFiltersInputTagName(WebElement tag) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(tag);
  }

  public List<String> getFiltersInputTags() {
    return getTagsFromFiltersInput()
        .stream()
        .map(tag -> getFiltersInputTagName(tag))
        .collect(Collectors.toList());
  }

  public void waitFiltersInputIsEmpty() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(FILTERS_INPUT_TAGS_XPATH));
  }

  public void waitFiltersInputTags(List<String> expectedTags) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> getFiltersInputTags().equals(expectedTags));
  }

  public void deleteTagByRemoveButton(String tagName) {
    String removeButtonXpath =
        format(
            FILTERS_INPUT_TAG_XPATH_TEMPLATE
                + "/parent::md-chip-template/div[@class='stack-library-filter-tag-btn']/i",
            tagName);

    seleniumWebDriverHelper.moveCursorToAndClick(By.xpath(removeButtonXpath));
  }

  public void waitTextContainsInFiltersInput(String expectedText) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getTextFromFiltersInput().contains(expectedText));
  }

  public void waitTextEqualsInFiltersInput(String expectedText) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>) driver -> getTextFromFiltersInput().equals(expectedText));
  }

  public List<String> getFiltersSuggestionsNames() {
    return seleniumWebDriverHelper
        .waitVisibilityOfAllElements(By.xpath("//div[@name='suggestionText']"))
        .stream()
        .map(webElement -> webElement.getText())
        .collect(toList());
  }

  public List<WebElement> getFiltersSuggestions() {
    String filtersSuggestionBody = "//md-chip";
    return seleniumWebDriverHelper.waitVisibilityOfAllElements(By.xpath(filtersSuggestionBody));
  }

  public void waitFiltersSuggestionsNames(List<String> expectedSuggestions) {
    expectedSuggestions.forEach(
        item -> {
          webDriverWaitFactory
              .get()
              .until(
                  (ExpectedCondition<Boolean>)
                      driver -> getFiltersSuggestionsNames().contains(item));
        });
  }

  public WebElement getSelectedSuggestion() {
    return getFiltersSuggestions()
        .stream()
        .filter(
            webElement ->
                webElement
                    .getAttribute("class")
                    .contains("stack-library-filter-suggestion-selected"))
        .collect(toList())
        .get(0);
  }

  public void waitSelectedFiltersSuggestion(String suggestionName) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getSelectedFiltersSuggestionName().equals(suggestionName));
  }

  public String getSelectedFiltersSuggestionName() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(getSelectedSuggestion());
  }

  public WebElement getFiltersSuggestionByName(String suggestionName) {
    String filtersSuggestion =
        format("//div[@name='suggestionText' and text()='%s']", suggestionName);

    return seleniumWebDriverHelper.waitVisibility(By.xpath(filtersSuggestion));
  }

  public String getSuggestionName(WebElement suggestion) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(suggestion);
  }

  public void clearSuggestions() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(FILTER_SELECTED_SUGGESTION_BUTTON));
  }

  public void chooseFilterSuggestionByPlusButton(String suggestion) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(FILTER_SUGGESTION_BUTTON, suggestion)));
  }

  public void clickOnFiltersSuggestions(String suggestionName) {
    seleniumWebDriverHelper.waitAndClick(getFiltersSuggestionByName(suggestionName));
  }

  public void doubleClickOnFiltersSuggestion(String suggestionName) {
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(getFiltersSuggestionByName(suggestionName));
  }

  public void waitToolbar() {
    waitToolbar(WIDGET_TIMEOUT_SEC);
  }

  public void waitToolbar(int timeout) {
    seleniumWebDriverHelper.waitVisibility(By.id(TOOLBAR_TITLE_ID), timeout);
  }

  public String getTextFromSearchInput() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(searchInput);
  }

  public void typeToSearchInput(String value) {
    seleniumWebDriverHelper.setValue(searchInput, value);
  }

  public void typeToRamField(String value) {
    seleniumWebDriverHelper.setValue(By.id("machine--ram"), value);
  }

  public void waitRedRamFieldBorders() {
    seleniumWebDriverHelper.waitAttributeContainsValue(
        By.xpath("//ng-form[@name='ramAmountForm']"), "class", "ng-invalid-required");
  }

  public void waitRedRamFieldBordersDisappearance() {
    seleniumWebDriverHelper.waitAttributeContainsValue(
        By.xpath("//ng-form[@name='ramAmountForm']"), "class", "ng-valid-required");
  }

  public void clearTextInSearchInput() {
    seleniumWebDriverHelper.waitAndClick(clearInput);
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

  public void clickOnAllStacksTab() {
    seleniumWebDriverHelper.waitAndClick(selectAllStacksTab);
  }

  public void clickOnQuickStartTab() {
    seleniumWebDriverHelper.waitAndClick(selectQuickStartStacksTab);
  }

  public void clickOnSingleMachineTab() {
    seleniumWebDriverHelper.waitAndClick(selectSingleMachineStacksTab);
  }

  public void clickOnMultiMachineTab() {
    seleniumWebDriverHelper.waitAndClick(selectMultiMachineStacksTab);
  }

  public void waitWorkspaceCreatedDialogIsVisible() {
    testWebElementRenderChecker.waitElementIsRendered(
        By.xpath(WORKSPACE_CREATED_DIALOG), ELEMENT_TIMEOUT_SEC);
  }

  public void closeWorkspaceCreatedDialog() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(WORKSPACE_CREATED_DIALOG_CLOSE_BUTTON_XPATH));
  }

  public void waitWorkspaceCreatedDialogDisappearance() {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(WORKSPACE_CREATED_DIALOG), ELEMENT_TIMEOUT_SEC);
  }

  public void clickOnEditWorkspaceButton() {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(EDIT_WORKSPACE_DIALOG_BUTTON), ELEMENT_TIMEOUT_SEC);
  }

  public void clickOnOpenInIDEButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(OPEN_IN_IDE_DIALOG_BUTTON), ELEMENT_TIMEOUT_SEC);
  }

  public void clickOnCreateButtonAndOpenInIDE() {
    seleniumWebDriverHelper.waitAndClick(bottomCreateWorkspaceButton);
    waitWorkspaceCreatedDialogIsVisible();
    clickOnOpenInIDEButton();
  }

  public void clickOnCreateButtonAndEditWorkspace() {
    waitBottomCreateWorkspaceButtonEnabled();
    seleniumWebDriverHelper.waitAndClick(bottomCreateWorkspaceButton);
    waitWorkspaceCreatedDialogIsVisible();
    clickOnEditWorkspaceButton();
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

  public WebElement waitAllButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(ALL_BUTTON_ID), timeout);
  }

  public WebElement waitAllButton() {
    return waitAllButton(DEFAULT_TIMEOUT);
  }

  public void clickOnAllButton() {
    waitAllButton().click();
  }

  public WebElement waitQuickStartButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(QUICK_START_BUTTON_ID), timeout);
  }

  public WebElement waitQuickStartButton() {
    return waitQuickStartButton(DEFAULT_TIMEOUT);
  }

  public void clickOnQuickStartButton() {
    waitQuickStartButton().click();
  }

  public WebElement waitSingleMachineButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(SINGLE_MACHINE_BUTTON_ID), timeout);
  }

  public WebElement waitSingleMachineButton() {
    return waitSingleMachineButton(DEFAULT_TIMEOUT);
  }

  public void clickOnSingleMachineButton() {
    waitSingleMachineButton().click();
  }

  public WebElement waitMultiMachineButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(MULTI_MACHINE_BUTTON_ID), timeout);
  }

  public WebElement waitMultiMachineButton() {
    return waitMultiMachineButton(DEFAULT_TIMEOUT);
  }

  public void clickOnMultiMachineButton() {
    waitMultiMachineButton().click();
  }

  public void waitBottomCreateButton(int timeout) {
    seleniumWebDriverHelper.waitVisibility(bottomCreateWorkspaceButton, timeout);
  }

  public void waitBottomCreateButton() {
    waitBottomCreateButton(DEFAULT_TIMEOUT);
  }

  public void waitMainActionButtons(int timeout) {
    waitTopCreateButton(timeout);
    waitAllButton(timeout);
    waitQuickStartButton(timeout);
    waitSingleMachineButton(timeout);
    waitMultiMachineButton(timeout);
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

  public List<String> getAvailableStacks() {
    return seleniumWebDriverHelper
        .waitPresenceOfAllElements(By.xpath("//div[@data-stack-id]"))
        .stream()
        .map(webElement -> webElement.getAttribute("data-stack-id"))
        .collect(toList());
  }

  public void waitStackSelected(Stack stack) {
    String selectedStackXpath =
        format(
            "//div[@data-stack-id='%s' and contains(@class, 'stack-selector-item-selected')]",
            stack.getId());
    seleniumWebDriverHelper.waitVisibility(By.xpath(selectedStackXpath));
  }

  public List<String> getVisibleStacks() {
    return seleniumWebDriverHelper
        .waitPresenceOfAllElements(By.xpath("//div[@data-stack-id]"))
        .stream()
        .filter(seleniumWebDriverHelper::isVisible)
        .map(webElement -> webElement.getAttribute("data-stack-id"))
        .collect(Collectors.toList());
  }

  public void waitVisibleStacks(List<String> expectedVisibleStacks) {
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
                By.xpath(format("//div[@data-stack-id='%s']", stack.getId()))));
  }

  public void waitStacksCount(int expectedCount) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> expectedCount == getAvailableStacksCount());
  }

  public int getAvailableStacksCount() {
    return getAvailableStacks().size();
  }

  public void waitStacksNotPresent(List<String> stacksIdForChecking) {
    stacksIdForChecking.forEach(
        stackId ->
            webDriverWaitFactory
                .get()
                .until(
                    (ExpectedCondition<Boolean>)
                        driver -> !getAvailableStacks().contains(stackId)));
  }

  public void waitStacksOrder(List<String> expectedOrder) {
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

  public void clickOnInputFieldTag(String tagName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(FILTERS_INPUT_TAG_XPATH_TEMPLATE, tagName)));
  }

  public void clickOnAddStackButton() {
    seleniumWebDriverHelper.waitAndClick(By.id("add-stack-button"));
  }

  public void waitCreateStackDialog() {
    // wait should be changed to "TestWebElementRenderChecker" after resolving issue
    // https://github.com/eclipse/che/issues/10087
    WaitUtils.sleepQuietly(2);

    seleniumWebDriverHelper.waitVisibility(By.xpath(CREATE_STACK_DIALOG_FORM_XPATH));
    seleniumWebDriverHelper.waitVisibility(By.xpath("//div[text()='Build stack from recipe']"));
  }

  public void waitCreateStackDialogClosing() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath("//md-dialog"));
  }

  public void clickOnYesButtonInCreateStackDialog() {
    seleniumWebDriverHelper.waitAndClick(By.xpath("//*[@che-button-title='Ok']"));
  }

  public void clickOnNoButtonInCreateStackDialog() {
    seleniumWebDriverHelper.waitAndClick(By.xpath("//*[@che-button-title='Cancel']"));
  }

  public void closeCreateStackDialogByCloseButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath("//md-dialog//i"));
  }
}
