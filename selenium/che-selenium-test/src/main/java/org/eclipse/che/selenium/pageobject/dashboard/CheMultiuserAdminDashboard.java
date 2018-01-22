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

import static java.util.Collections.singletonList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfAllElements;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.eclipse.che.selenium.pageobject.site.LoginPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page object extends {@link Dashboard} page object to cover elements which are specific for Che
 * Admin dashboard content.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class CheMultiuserAdminDashboard extends Dashboard {

  @Inject
  public CheMultiuserAdminDashboard(
      SeleniumWebDriver seleniumWebDriver,
      TestUser defaultUser,
      TestIdeUrlProvider testIdeUrlProvider,
      TestDashboardUrlProvider testDashboardUrlProvider,
      Entrance entrance,
      LoginPage loginPage,
      TestWebElementRenderChecker testWebElementRenderChecker) {
    super(
        seleniumWebDriver,
        defaultUser,
        testIdeUrlProvider,
        testDashboardUrlProvider,
        entrance,
        loginPage,
        testWebElementRenderChecker);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String SETTINGS_ITEM_XPATH = "//a[@href='#/onprem/administration']";
  }

  @FindBy(xpath = Locators.SETTINGS_ITEM_XPATH)
  private WebElement settingsItem;

  /** Wait appearance of main elements on page */
  public void waitPageOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(settingsItem));
  }

  /** Wait disappearance of main elements on page */
  public void waitPageClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(invisibilityOfAllElements(singletonList(settingsItem)));
  }

  public void clickOnSettingsItem() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(settingsItem))
        .click();
  }
}
