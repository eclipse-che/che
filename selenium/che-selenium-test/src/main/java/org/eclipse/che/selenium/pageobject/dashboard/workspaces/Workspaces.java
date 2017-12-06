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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Workspaces {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Dashboard dashboard;

  @Inject
  public Workspaces(SeleniumWebDriver seleniumWebDriver, Dashboard dashboard) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.dashboard = dashboard;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String WORKSPACES_LIST = "//ng-transclude[@class='che-list-content']";
    String WORKSPACE_ITEM_NAME =
        "//div[contains(@class, 'workspace-name-clip')]//div[contains(@data-str, '%s')]";
    String TOOLBAR_TITLE_NAME =
        "//div[contains(@class,'che-toolbar')]//span[contains(text(),'%s')]";
    String ADD_WORKSPACE_BTN =
        "//a[contains(@ng-href, 'create-workspace')]/span[text()='Add Workspace']";
  }

  @FindBy(xpath = Locators.WORKSPACES_LIST)
  WebElement listOfWorkspaces;

  @FindBy(xpath = Locators.ADD_WORKSPACE_BTN)
  WebElement addWorkspaceBtn;

  public void waitListWorkspacesOnDashboard() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(listOfWorkspaces));
  }

  public String getTextFromListWorkspaces() {
    return listOfWorkspaces.getText();
  }

  public void waitExpTextFromListWsOnDashboard(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> getTextFromListWorkspaces().contains(expText));
  }

  public void selectWorkspaceItemName(String wsName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.WORKSPACE_ITEM_NAME, wsName))))
        .click();
  }

  /** wait the workspace is not present on dashboard */
  public void waitWorkspaceIsNotPresent(String nameWorkspace) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            invisibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_NAME, nameWorkspace))));
  }

  /**
   * Wait toolbar name is present on dashboard
   *
   * @param titleName name of user
   */
  public void waitToolbarTitleName(String titleName) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.TOOLBAR_TITLE_NAME, titleName))));
  }

  // Click on the Add Workspace button
  public void clickOnNewWorkspaceBtn() {
    dashboard.waitNotificationIsClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(addWorkspaceBtn))
        .click();
  }
}
