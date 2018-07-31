/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.dashboard.organization;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This is a page object for the list of organizations in User Dashboard.
 *
 * @author Ann Shumilova
 */
@Singleton
public class OrganizationListPage {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public OrganizationListPage(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** The enum of possible headers */
  public enum OrganizationListHeader {
    NAME("NAME", 1),
    MEMBERS("MEMBERS", 2),
    TOTAL_RAM("TOTAL RAM", 3),
    AVAILABLE_RAM("AVAILABLE RAM", 4),
    SUB_ORGANIZATIONS("SUB-ORGANIZATIONS", 5),
    ACTIONS("ACTIONS", 6);
    private final String title;
    private final int index;

    OrganizationListHeader(String title, int index) {
      this.title = title;
      this.index = index;
    }

    public String getTitle() {
      return this.title;
    }

    public int getIndex() {
      return this.index;
    }
  }

  private interface Locators {
    String TOOLBAR_ID = "Organizations";
    String ADD_ORGANIZATION_BUTTON_XPATH = "//che-button-primary//span[text()='Add Organization']";
    String ADD_SUB_ORGANIZATION_BUTTON_XPATH =
        "//che-button-primary//span[text()='Add Sub-Organization']";
    String DELETE_ORGANIZATION_BUTTON_XPATH = "//che-button-primary//span[text()='Delete']";
    String ORGANIZATION_LIST_XPATH = "//list-organizations//md-list";
    String ORGANIZATION_LIST_ITEM_XPATH = "//list-organizations//md-list//md-item";
    String ORGANIZATION_HEADER_XPATH =
        "//list-organizations//div[contains(@class, 'che-list-header-column')]//span";
    String ORGANIZATION_SEARCH_XPATH =
        "//list-organizations//div[contains(@class, 'header-search-input')]/input";
    String ORGANIZATION_CELL_XPATH =
        "//organizations-item//div[contains(@class, 'che-list-item-details')]/div[%s]//span";
    String ORGANIZATION_NAME_XPATH =
        "//organizations-item//div[contains(@class, 'che-list-item-name')]//span[2]";
    String ORGANIZATION_DELETE_XPATH =
        "//organizations-item[%s]//a[@uib-tooltip='Remove organization']/span";
    String ORGANIZATION_EMPTY_LIST_XPATH =
        "//div[@class='che-list-empty']/span[text()='There are no organizations.']";
    String SUB_ORGANIZATION_EMPTY_LIST_XPATH =
        "//div[@class='che-list-empty']/span[text()='There are no sub-organizations.']";
    String ORGANIZATION_CHECKBOX_XPATH =
        "//organizations-item[%s]//md-checkbox/div[contains(@class, 'md-container')]";
    String ORGANIZATION_XPATH =
        "//organizations-item//div[contains(@class, 'che-list-item-name')]//span[text()='%s']";
  }

  @FindBy(id = Locators.TOOLBAR_ID)
  WebElement toolbarTitle;

  @FindBy(xpath = Locators.ADD_ORGANIZATION_BUTTON_XPATH)
  WebElement addOrganizationButton;

  @FindBy(xpath = Locators.ADD_SUB_ORGANIZATION_BUTTON_XPATH)
  WebElement addSubOrganizationButton;

  @FindBy(xpath = Locators.DELETE_ORGANIZATION_BUTTON_XPATH)
  WebElement bulkDeleteButton;

  @FindBy(xpath = Locators.ORGANIZATION_LIST_XPATH)
  WebElement organizationList;

  @FindBy(xpath = Locators.ORGANIZATION_SEARCH_XPATH)
  WebElement searchInput;

  @FindBy(xpath = Locators.ORGANIZATION_EMPTY_LIST_XPATH)
  WebElement emptyList;

  @FindBy(xpath = Locators.SUB_ORGANIZATION_EMPTY_LIST_XPATH)
  WebElement subOrganizationEmptyList;

  /** Wait for organization toolbar to be visible on the page. */
  public void waitForOrganizationsToolbar() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(toolbarTitle));
  }

  /** Wait for organizations list to be visible on the page. */
  public void waitForOrganizationsList() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(organizationList));
  }

  /** Wait for organizations list to be empty with proper message. */
  public void waitForOrganizationsEmptyList() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(emptyList));
  }

  /** Wait for sub-organizations list to be empty with proper message. */
  public void waitForSubOrganizationsEmptyList() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(subOrganizationEmptyList));
  }

  /**
   * Returns the number of organization items in the list.
   *
   * @return number of the organizations in the list
   */
  public int getOrganizationListItemCount() {
    WaitUtils.sleepQuietly(2);
    return seleniumWebDriver.findElements(By.xpath(Locators.ORGANIZATION_LIST_ITEM_XPATH)).size();
  }

  /**
   * Returns the title's text displayed on the toolbar.
   *
   * @return title of the toolbar
   */
  public String getOrganizationsToolbarTitle() {
    return toolbarTitle.getText();
  }

  /**
   * Returns whether Add organization button is visible on the page.
   *
   * @return Add Organization button visibility
   */
  public boolean isAddOrganizationButtonVisible() {
    List<WebElement> elements =
        seleniumWebDriver.findElements(By.xpath(Locators.ADD_ORGANIZATION_BUTTON_XPATH));
    return !elements.isEmpty() && elements.get(0).isDisplayed();
  }

  /**
   * Returns whether Add sub-organization button is visible on the page.
   *
   * @return Add Sub-Organization button visibility
   */
  public boolean isAddSubOrganizationButtonVisible() {
    List<WebElement> elements =
        seleniumWebDriver.findElements(By.xpath(Locators.ADD_SUB_ORGANIZATION_BUTTON_XPATH));
    return !elements.isEmpty() && elements.get(0).isDisplayed();
  }

  /**
   * Returns whether Search organization input is visible on the page.
   *
   * @return Search organization input visibility
   */
  public boolean isSearchInputVisible() {
    return searchInput.isDisplayed();
  }

  /**
   * Returns the list of headers in the list of organizations.
   *
   * @return headers of the organizations list
   */
  public ArrayList<String> getOrganizationListHeaders() {
    List<WebElement> headers =
        seleniumWebDriver.findElements(By.xpath(Locators.ORGANIZATION_HEADER_XPATH));
    ArrayList<String> titles = new ArrayList<>();
    headers.forEach(
        header -> {
          titles.add(header.getText());
        });
    return titles;
  }

  /** Clicks on Add Organization button. */
  public void clickAddOrganizationButton() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(visibilityOf(addOrganizationButton));
    addOrganizationButton.click();
  }

  /**
   * Returns values of the pointed column.
   *
   * @param header header
   * @return list of values in the cells
   */
  public ArrayList<String> getValues(OrganizationListHeader header) {
    String locator = format(Locators.ORGANIZATION_CELL_XPATH, header.getIndex());
    List<WebElement> cells = seleniumWebDriver.findElements(By.xpath(locator));
    ArrayList<String> values = new ArrayList<>();
    cells.forEach(
        cell -> {
          values.add(cell.getText());
        });

    return values;
  }

  public int getOrganizationIndex(String name) {
    List<WebElement> cells =
        seleniumWebDriver.findElements(By.xpath(Locators.ORGANIZATION_NAME_XPATH));
    ArrayList<String> values = new ArrayList<>();
    cells.forEach(
        cell -> {
          values.add(cell.getText());
        });

    return values.indexOf(name) + 1;
  }

  public void clickOnOrganization(String name) {
    String locator = format(Locators.ORGANIZATION_XPATH, name);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(locator)));
    seleniumWebDriver.findElement(By.xpath(locator)).click();
  }

  public void clickOnDeleteButton(String name) {
    String locator = format(Locators.ORGANIZATION_DELETE_XPATH, getOrganizationIndex(name));
    seleniumWebDriver.findElement(By.xpath(locator)).click();
  }

  public void clickCheckbox(String name) {
    String locator = format(Locators.ORGANIZATION_CHECKBOX_XPATH, getOrganizationIndex(name));
    seleniumWebDriver.findElement(By.xpath(locator)).click();
  }

  public void clickBulkDeleteButton() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC).until(visibilityOf(bulkDeleteButton));
    bulkDeleteButton.click();
  }

  public boolean isBulkDeleteVisible() {
    return this.isVisible(Locators.DELETE_ORGANIZATION_BUTTON_XPATH);
  }

  /**
   * Wait until organization item is deleted from list.
   *
   * @param name
   */
  public void waitForOrganizationIsRemoved(String name) {
    String xpath = format(Locators.ORGANIZATION_XPATH, name);
    this.waitForElementDisappearance(xpath);
  }

  public void waitOrganizationInList(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.ORGANIZATION_XPATH, name))));
  }

  /**
   * Types pointed string in the search input.
   *
   * @param value
   */
  public void typeInSearchInput(String value) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC).until(visibilityOf(searchInput));
    searchInput.click();
    searchInput.sendKeys(value);
  }

  /** Clears search input from any values. */
  public void clearSearchInput() {
    searchInput.clear();
  }

  private boolean isVisible(String xpath) {
    List<WebElement> elements = seleniumWebDriver.findElements(By.xpath(xpath));
    return !elements.isEmpty() && elements.get(0).isDisplayed();
  }

  private void waitForElementDisappearance(String xpath) {
    FluentWait<SeleniumWebDriver> wait =
        new FluentWait<>(seleniumWebDriver)
            .withTimeout(REDRAW_UI_ELEMENTS_TIMEOUT_SEC, TimeUnit.SECONDS)
            .pollingEvery(200, TimeUnit.MILLISECONDS)
            .ignoring(StaleElementReferenceException.class);

    wait.until(
        (Function<WebDriver, Boolean>)
            driver -> {
              List<WebElement> elements = seleniumWebDriver.findElements(By.xpath(xpath));
              return elements.isEmpty() || !elements.get(0).isDisplayed();
            });
  }
}
