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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.List;
import javax.annotation.PreDestroy;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestKeycloakSettingsServiceClient;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.eclipse.che.selenium.pageobject.site.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class Dashboard {
  protected final SeleniumWebDriver seleniumWebDriver;
  protected final TestUser defaultUser;

  private final TestDashboardUrlProvider testDashboardUrlProvider;
  private final Entrance entrance;
  private final LoginPage loginPage;
  private final TestWebElementRenderChecker testWebElementRenderChecker;
  private final TestKeycloakSettingsServiceClient testKeycloakSettingsServiceClient;
  private final boolean isMultiuser;

  @Inject
  public Dashboard(
      SeleniumWebDriver seleniumWebDriver,
      TestUser defaultUser,
      TestDashboardUrlProvider testDashboardUrlProvider,
      Entrance entrance,
      LoginPage loginPage,
      TestWebElementRenderChecker testWebElementRenderChecker,
      TestKeycloakSettingsServiceClient testKeycloakSettingsServiceClient,
      @Named("che.multiuser") boolean isMultiuser) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.defaultUser = defaultUser;
    this.testDashboardUrlProvider = testDashboardUrlProvider;
    this.entrance = entrance;
    this.loginPage = loginPage;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
    this.testKeycloakSettingsServiceClient = testKeycloakSettingsServiceClient;
    this.isMultiuser = isMultiuser;
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
    String DASHBOARD_TOOLBAR_TITLE = "navbar";
    String NAVBAR_NOTIFICATION_CONTAINER = "navbar-notification-container";
    String COLLAPSE_DASH_NAVBAR_BTN = "ide-iframe-button-link";
    String NOTIFICATION_CONTAINER = "che-notification-container";
    String DASHBOARD_ITEM = "dashboard-item";
    String WORKSPACES_ITEM = "workspaces-item";
    String STACKS_ITEM = "stacks-item";
    String FACTORIES_ITEM = "factories-item";
    String ADMINISTRATION_ITEM = "administration-item";
    String ORGANIZATIONS_ITEM = "organization-item";
    String RESENT_WS_NAVBAR = "//div[@class='admin-navbar-menu recent-workspaces']";
    String LEFT_SIDE_BAR = "//div[@class='left-sidebar-container']";
    String USER_PANEL = "navbar-user-panel";
    String DEVELOPERS_FACE_XPATH = "developers-face";
    String USER_NAME = "user-name";
    String LICENSE_NAG_MESSAGE_XPATH = "//div[contains(@class, 'license-message')]";
    String TOOLBAR_TITLE_NAME =
        "//div[contains(@class,'che-toolbar')]//span[contains(text(),'%s')]";
  }

  @FindBy(id = Locators.DASHBOARD_TOOLBAR_TITLE)
  WebElement dashboardTitle;

  @FindBy(id = Locators.COLLAPSE_DASH_NAVBAR_BTN)
  WebElement collapseDashNavbarBtn;

  @FindBy(id = Locators.DASHBOARD_ITEM)
  WebElement dashboardItem;

  @FindBy(id = Locators.WORKSPACES_ITEM)
  WebElement workspacesItem;

  @FindBy(id = Locators.STACKS_ITEM)
  WebElement stacksItem;

  @FindBy(id = Locators.FACTORIES_ITEM)
  WebElement factoriesItem;

  @FindBy(id = Locators.DEVELOPERS_FACE_XPATH)
  WebElement developersFace;

  @FindBy(id = Locators.USER_NAME)
  WebElement userName;

  @FindBy(id = Locators.NOTIFICATION_CONTAINER)
  WebElement notificationPopUp;

  @FindBy(xpath = Locators.LICENSE_NAG_MESSAGE_XPATH)
  WebElement licenseNagMessage;

  /** wait button with drop dawn icon (left top corner) */
  public void waitDashboardToolbarTitle() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(dashboardTitle));
  }

  /** click on the 'Dashboard' item on the dashboard */
  public void selectDashboardItemOnDashboard() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(dashboardItem))
        .click();
  }

  /** click on the 'Workspaces' item on the dashboard */
  public void selectWorkspacesItemOnDashboard() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(workspacesItem))
        .click();
  }

  /** click on the 'Stacks' item on the dashboard */
  public void selectStacksItemOnDashboard() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(stacksItem))
        .click();
  }

  /** click on the 'Factories' item on the dashboard */
  public void selectFactoriesOnDashbord() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(factoriesItem))
        .click();
  }

  /**
   * * wait expected text in the pop up notification
   *
   * @param notification
   */
  public void waitNotificationMessage(String notification) {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(textToBePresentInElement(notificationPopUp, notification));
  }

  /** wait closing of notification pop up */
  public void waitNotificationIsClosed() {
    WaitUtils.sleepQuietly(1);
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.id(Locators.NOTIFICATION_CONTAINER)));
  }

  /** wait opening of notification pop up */
  public void waitNotificationIsOpen() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(visibilityOf(notificationPopUp));
  }

  /** Wait developer avatar is present on dashboard */
  public void waitDeveloperFaceImg() {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(visibilityOf(developersFace));
  }

  /**
   * Wait user name is present on dashboard
   *
   * @param name name of user
   */
  public Boolean checkUserName(String name) {
    return new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(visibilityOf(userName))
        .getText()
        .equals(name);
  }

  /**
   * Get text of nag message which displays at the top of Dashboard, or throw an exception if nag
   * message doesn't appear in some time.
   */
  public String getLicenseNagMessage() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(licenseNagMessage))
        .getText();
  }

  /** Wait until nag message disappears. */
  public void waitNagMessageDisappear() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.xpath(Locators.LICENSE_NAG_MESSAGE_XPATH)));
  }

  /** Open dashboard as default uses */
  public void open() {
    seleniumWebDriver.get(testDashboardUrlProvider.get().toString());
    entrance.login(defaultUser);
    waitDashboardToolbarTitle();
  }

  /** Open dashboard with provided username and password */
  public void open(String userName, String userPassword) {
    seleniumWebDriver.get(testDashboardUrlProvider.get().toString());
    if (loginPage.isOpened()) {
      loginPage.login(userName, userPassword);
    }
  }

  public void logout() {
    if (!isMultiuser) {
      return;
    }

    String logoutUrl =
        format(
            "%s?redirect_uri=%s#/workspaces",
            testKeycloakSettingsServiceClient.read().getKeycloakLogoutEndpoint(),
            testDashboardUrlProvider.get());

    seleniumWebDriver.navigate().to(logoutUrl);
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

  /** Return true if workspaces present on the navigation panel */
  public boolean workspacesIsPresent() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.LEFT_SIDE_BAR)));

    List<WebElement> workspaces =
        seleniumWebDriver.findElements(By.xpath(Locators.RESENT_WS_NAVBAR));
    return !(workspaces.size() == 0);
  }

  public void clickOnUsernameButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.USER_NAME)))
        .click();
  }

  public void clickOnAccountItem() {
    testWebElementRenderChecker.waitElementIsRendered(By.id("menu_container_1"));

    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath("//span[text()=' Account']")))
        .click();
  }

  public void clickOnLogoutItem() {
    testWebElementRenderChecker.waitElementIsRendered(By.id("menu_container_1"));

    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath("//span[text()=' Logout']")))
        .click();
  }

  @PreDestroy
  public void close() {
    seleniumWebDriver.quit();
  }
}
