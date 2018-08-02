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
package org.eclipse.che.selenium.pageobject.ocp;

import static java.lang.String.format;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.provider.OpenShiftWebConsoleUrlProvider;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class OpenShiftProjectCatalogPage {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;
  private final OpenShiftWebConsoleUrlProvider openShiftWebConsoleUrlProvider;

  protected interface Locators {
    String TITLE_XPATH = "//h1[contains(text(), 'Browse Catalog')]";
    String PROJECT_ITEM_XPATH_TEMPLATE =
        "//div[@id='catalog-projects-summary-list']//a[contains(text(),'%s')]";
  }

  @FindBy(xpath = Locators.TITLE_XPATH)
  private WebElement title;

  @Inject
  public OpenShiftProjectCatalogPage(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      OpenShiftWebConsoleUrlProvider openShiftWebConsoleUrlProvider) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.openShiftWebConsoleUrlProvider = openShiftWebConsoleUrlProvider;

    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitProject(String projectNamePart) {
    waitOnOpen();
    String projectItemXpath = String.format(Locators.PROJECT_ITEM_XPATH_TEMPLATE, projectNamePart);
    seleniumWebDriverHelper.waitVisibility(By.xpath(projectItemXpath));
  }

  public void waitProjectAbsence(String projectNamePart) {
    waitOnOpen();
    String projectItemXpath = String.format(Locators.PROJECT_ITEM_XPATH_TEMPLATE, projectNamePart);
    seleniumWebDriverHelper.waitInvisibility(By.xpath(projectItemXpath));
  }

  public void open() {
    seleniumWebDriver.navigate().to(openShiftWebConsoleUrlProvider.get());
  }

  public void logout() {
    String logoutUrl = format("%sconsole/logout", openShiftWebConsoleUrlProvider.get().toString());
    seleniumWebDriver.navigate().to(logoutUrl);
    waitOnClose();
  }

  private void waitOnOpen() {
    seleniumWebDriverHelper.waitVisibility(title);
  }

  private void waitOnClose() {
    seleniumWebDriverHelper.waitInvisibility(title);
  }
}
