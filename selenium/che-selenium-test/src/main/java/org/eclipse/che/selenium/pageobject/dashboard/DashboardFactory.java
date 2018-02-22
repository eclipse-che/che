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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementValue;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class DashboardFactory {

  public enum SourcesTypes {
    GIT("Git Repository"),
    GITHUB("Github"),
    WORKSPACES("Workspace"),
    CONFIG_FILE("Config File"),
    TEMPLATE("Template");

    private final String type;

    SourcesTypes(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return type;
    }
  }

  public enum AddAction {
    RUN_COMMAND("RunCommand"),
    OPEN_FILE("openFile");
    private final String actionName;

    AddAction(String actionName) {
      this.actionName = actionName;
    }
  }

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private final Dashboard dashboard;
  private final String ideUrl;

  @Inject
  public DashboardFactory(
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      Dashboard dashboard,
      TestIdeUrlProvider ideUrlProvider) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.dashboard = dashboard;
    this.ideUrl = ideUrlProvider.get().toString();
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {

    String FACTORY_TAB_XPATH = "//span[text()='Factories']";
    String FACTORY_LIST_CONTAINER_CSS = "md-content.factories-list-factories";
    String WORKSPACE_ITEM_XPATH = "//div[@class='project-name']/a[text()='%s']";
    String JSON_FACTORY_EDITOR_AREA_CSS = "div.json-editor";
    String NEXT_FACTORY_BTN_XPATH = "//button/span[text()='Create']";
    String FACTORY_NAMED_URL_XPATH =
        "//div[@che-href='factoryInformationCtrl.factory.nameURL']//div[@layout]/span";
    String FACTORY_URL_MARKDOWN = "div.create-factory-share-header-widget-markdown";
    String DONE_BUTTON_XPATH = "//button/span[text()='Done']";
    String FACTORY_NAME_FIELD_CSS = "input[placeholder='Name of the factory']";
    String WORKSPACES_TAB_SELECT_XPATH = "//span[text()='Workspace']/parent::md-tab-item";
    String CREATE_FACTORY_ICON_XPATH = "//a/span[text()='Create Factory']";
    String ADD_CONFIGURE_ACTION_XPATH = "//md-card[@che-title='Configure Actions']//button";
    String OPEN_FACTORY_BUTTON_XPATH = "//che-button-default[@che-button-title='Open']";
    String CONFIGURE_ACTION_TITLE_XPATH = "//div[text()='Configure Actions']";

    String FACTORY_ITEM = "//div[@id='%s']";
    String FACTORY_NAME = FACTORY_ITEM + "//span[@name='factory-name']";
    String FACTORY_RAM_LIMIT = FACTORY_ITEM + "//span[@name='factory-ram-limit']";
    String FACTORY_ACTION = FACTORY_ITEM + "//span[@name='open-factory']";
  }

  private interface AddActionWindow {
    String ACTION_XPATH = "//div[@che-label-name='Configure Actions']//div/md-select";
    String ACTION_SELECT_XPATH =
        "//div[@class[contains(.,'md-select-menu-container') ] and @style[contains(.,'display: block; left:')]]//div[contains(text(),'%s')]";
    String PARAM_XPATH = "//input[@placeholder='Enter value']";
    String ADD_XPATH =
        "//div[@che-label-name='Configure Actions']//span[text()='Add']//parent::button";
  }

  @FindBy(xpath = Locators.FACTORY_TAB_XPATH)
  WebElement factoriesNavBarItem;

  @FindBy(css = Locators.FACTORY_LIST_CONTAINER_CSS)
  WebElement factoryListContainer;

  @FindBy(xpath = Locators.CREATE_FACTORY_ICON_XPATH)
  WebElement addFactoryBtn;

  @FindBy(css = Locators.JSON_FACTORY_EDITOR_AREA_CSS)
  WebElement jsonAreaEditor;

  @FindBy(xpath = Locators.NEXT_FACTORY_BTN_XPATH)
  WebElement createFactoryBtn;

  @FindBy(css = Locators.FACTORY_NAME_FIELD_CSS)
  WebElement factoryNameField;

  @FindBy(xpath = Locators.FACTORY_NAMED_URL_XPATH)
  WebElement factoryNamedUrl;

  @FindBy(xpath = Locators.DONE_BUTTON_XPATH)
  WebElement doneBtn;

  @FindBy(xpath = Locators.WORKSPACES_TAB_SELECT_XPATH)
  WebElement workspaceTabSelect;

  @FindBy(xpath = Locators.ADD_CONFIGURE_ACTION_XPATH)
  WebElement addConfigureAction;

  @FindBy(xpath = Locators.OPEN_FACTORY_BUTTON_XPATH)
  WebElement openFactoryButton;

  @FindBy(xpath = Locators.CONFIGURE_ACTION_TITLE_XPATH)
  WebElement configureActionTitle;

  /** wait factory menu in the navigation bar */
  public void waitFactoriesItemOnNavBar() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(factoriesNavBarItem));
  }

  /** wait and click on 'Factory' menu */
  public void selectFactoriesOnNavBar() {
    waitFactoriesItemOnNavBar();
    factoriesNavBarItem.click();
  }

  /** wait 'All factories' page with factories list */
  public void waitAllFactoriesPage() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(factoryListContainer));
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(addFactoryBtn));
  }

  /** click on add factory button ('+' icon) */
  public void clickOnAddFactoryBtn() {
    waitAllFactoriesPage();
    addFactoryBtn.click();
  }

  /**
   * select workspace in the list for creation a factory
   *
   * @param wsNama
   */
  public void selectWorkspaceForCreation(String wsNama) {
    // delay for animation page
    WaitUtils.sleepQuietly(1, TimeUnit.SECONDS);
    String locator = format(Locators.WORKSPACE_ITEM_XPATH, wsNama);
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(locator)))
        .click();
    // delay for redraw and setting selection
    WaitUtils.sleepQuietly(1, TimeUnit.SECONDS);
  }

  /** wait any data in the json editor */
  public void waitJsonFactoryIsNotEmpty() {
    WebElement jsonArea =
        seleniumWebDriverHelper.waitPresence(By.cssSelector(Locators.JSON_FACTORY_EDITOR_AREA_CSS));
    seleniumWebDriverHelper.moveCursorTo(jsonArea);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> jsonArea.getText().length() > LOAD_PAGE_TIMEOUT_SEC);
  }

  /** click on 'Create factory button' */
  public void clickOnCreateFactoryBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(createFactoryBtn))
        .click();
  }

  /** wait any factory url after creation a factory */
  public void waitFactoryUrl() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.partialLinkText(ideUrl + "f?id=")));
  }

  /**
   * wait on the factory name field visibility and enter the name
   *
   * @param name name of factory to enter
   */
  public void setFactoryName(String name) {
    WebElement field =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(visibilityOf(factoryNameField));
    field.clear();
    field.sendKeys(name);
  }

  /**
   * wait Select Source widget and select od source type
   *
   * @param sourceType
   */
  public void waitSelectSourceWidgetAndSelect(String sourceType) {
    String locatorPref =
        "//span[@class[contains(.,'che-tab-label-title')]and text()='" + sourceType + "']";
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(locatorPref)))
        .click();
  }

  /** click on the DOne button of Configure Button page */
  public void clickOnDoneBtn() {
    scrollOnDoneButton();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(doneBtn))
        .click();
  }

  /** click on current factory id Url */
  public void clickFactoryIDUrl() {
    waitFactoryUrl();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.partialLinkText(ideUrl + "f?id=")))
        .click();
  }

  /** click on current factory id Url */
  public void clickNamedFactoryUrl() {
    waitFactoryUrl();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.partialLinkText(ideUrl + "f?name=")))
        .click();
  }

  /** click on current factory id Url */
  public void clickFactoryNamedUrl() {
    waitFactoryUrl();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.partialLinkText(ideUrl + "f?name=")))
        .click();
  }

  /** Click on 'Workspaces' tab on 'Select Source' widget */
  public void clickWorkspacesTabOnSelectSource() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(workspaceTabSelect))
        .click();
  }

  /** Click on 'Add' button on 'Configure Action' widget */
  public void clickOnAddConfigureActions() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(addConfigureAction))
        .click();
  }

  /** Click on 'Open' button on the factory properties widget */
  public void clickOnOpenFactory() {
    dashboard.waitNotificationIsClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(openFactoryButton))
        .click();
  }

  /**
   * Select action on 'Add action' window
   *
   * @param addAction name of Action
   */
  public void selectAction(AddAction addAction) {
    WaitUtils.sleepQuietly(1);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(presenceOfElementLocated(By.xpath(AddActionWindow.ACTION_XPATH)))
        .click();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            presenceOfElementLocated(
                By.xpath(format(AddActionWindow.ACTION_SELECT_XPATH, addAction.actionName))))
        .click();
  }

  /**
   * Enter param value on 'Add action' window
   *
   * @param paramValue value of param
   */
  public void enterParamValue(String paramValue) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(presenceOfElementLocated(By.xpath(AddActionWindow.PARAM_XPATH)))
        .clear();
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(presenceOfElementLocated(By.xpath(AddActionWindow.PARAM_XPATH)))
        .sendKeys(paramValue);
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(textToBePresentInElementValue(By.xpath(AddActionWindow.PARAM_XPATH), paramValue));
  }

  /** click 'Add' button on 'Add Action' window */
  public void clickAddOnAddAction() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(By.xpath(AddActionWindow.ADD_XPATH)))
        .click();
  }

  /** scroll on 'Done' button */
  private void scrollOnDoneButton() {
    actionsFactory.createAction(seleniumWebDriver).moveToElement(doneBtn).perform();
    WaitUtils.sleepQuietly(2);
  }

  public void waitFactoryName(String factoryName) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.FACTORY_ITEM, factoryName))));
  }

  public void selectFactory(String factoryName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.FACTORY_NAME, factoryName))))
        .click();
  }

  public String getFactoryRamLimit(String factoryName) {
    return new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.FACTORY_RAM_LIMIT, factoryName))))
        .getText();
  }

  public void clickOnOpenInIdeButton(String factoryName) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.FACTORY_ACTION, factoryName))))
        .click();
  }
}
