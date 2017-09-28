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
package org.eclipse.che.selenium.pageobject.dashboard.organization;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This is a page object for navigation bar (the main menu) interaction.
 *
 * @author Ann Shumilova
 */
@Singleton
public class NavigationBar {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public NavigationBar(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public enum MenuItem {
    DASHBOARD("Dashboard"),
    WORKSPACES("Workspaces"),
    STACKS("Stacks"),
    FACTORIES("Factories"),
    USERS("Users"),
    ORGANIZATIONS("Organizations"),
    SETTINGS("Settings"),
    CREATE_TEAM("Create Team");
    private final String title;

    MenuItem(String title) {
      this.title = title;
    }
  }

  private interface Locators {
    String NAVIGATION_BAR_XPATH = "//*[contains(@id,'navbar')]";
    String MENU_ITEM_XPATH = "//md-list-item//span[text()='%s']";
    String COUNTER_XPATH = "/following-sibling::span";
    String TEAMS_LIST_XPATH = "//*[contains(@class, 'navbar-teams-list')]//md-list-item";
  }

  @FindBy(xpath = Locators.NAVIGATION_BAR_XPATH)
  WebElement navigationBar;

  @FindBy(xpath = Locators.TEAMS_LIST_XPATH)
  List<WebElement> teamsList;

  public void waitNavigationBar() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(navigationBar));
  }

  public void clickOnMenu(MenuItem menuItem) {
    String locator = String.format(Locators.MENU_ITEM_XPATH, menuItem.title);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
    seleniumWebDriver.findElement(By.xpath(locator)).click();
  }

  public String getMenuCounterValue(MenuItem menuItem) {
    String locator =
        String.format(Locators.MENU_ITEM_XPATH, menuItem.title) + Locators.COUNTER_XPATH;
    String counter = seleniumWebDriver.findElement(By.xpath(locator)).getText();
    counter = counter.trim().replace("(", "").replace(")", "");
    return counter.equals("") ? "0" : counter;
  }

  public List<WebElement> getTeamListItems() {
    return seleniumWebDriver.findElements(By.xpath(Locators.TEAMS_LIST_XPATH));
  }

  public int getTeamListItemCount() {
    return teamsList.size();
  }

  public void clickOnItemByName(String itemName) {
    String locator = String.format(Locators.MENU_ITEM_XPATH, itemName);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
    seleniumWebDriver.findElement(By.xpath(locator)).click();
  }
}
