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
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FactoryDetails {

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait redrawUiElementsTimeout;

  @Inject
  public FactoryDetails(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String FACTORY_NAME = "//span[contains(@class,'che-toolbar-title-label') and text()='%s']";
    String BACK_TO_FACTORIES_LIST_BUTTON = "//a[contains(@class,'che-toolbar-control-button')]";
  }

  public void waitFactoryName(String factoryName) {
    redrawUiElementsTimeout.until(
        visibilityOfElementLocated(By.xpath(format(Locators.FACTORY_NAME, factoryName))));
  }

  public void clickOnBackToFactoriesListButton() {
    redrawUiElementsTimeout
        .until(visibilityOfElementLocated(By.xpath(Locators.BACK_TO_FACTORIES_LIST_BUTTON)))
        .click();
  }
}
