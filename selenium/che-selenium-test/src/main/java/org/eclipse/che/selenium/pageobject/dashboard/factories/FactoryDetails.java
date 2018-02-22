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
package org.eclipse.che.selenium.pageobject.dashboard.factories;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FactoryDetails {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final Dashboard dashboard;
  private final WebDriverWait redrawUiElementsTimeout;

  @Inject
  public FactoryDetails(SeleniumWebDriver seleniumWebDriver, Loader loader, Dashboard dashboard) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.dashboard = dashboard;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String FACTORY_NAME = "//span[contains(@class,'che-toolbar-title-label') and text()='%s']";
    public String BACK_TO_FACTORIES_LIST_BUTTON =
        "//a[contains(@class,'che-toolbar-control-button')]";
  }

  public void waitFactoryName(String factoryName) {
    redrawUiElementsTimeout.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath(format(Locators.FACTORY_NAME, factoryName))));
  }

  public void clickOnBackToFactoriesListButton() {
    redrawUiElementsTimeout
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.BACK_TO_FACTORIES_LIST_BUTTON)))
        .click();
  }
}
