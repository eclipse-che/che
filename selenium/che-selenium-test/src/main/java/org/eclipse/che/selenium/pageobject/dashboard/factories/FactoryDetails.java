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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FactoryDetails {

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait redrawUiElementsTimeoutWait;

  @Inject
  public FactoryDetails(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.redrawUiElementsTimeoutWait =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String FACTORY_NAME_XPATH = "//span[contains(@class,'che-toolbar-title-label')]";
    String ALL_FACTORIES_BUTTON_XPATH = "//a[@title='All factories']";
  }

  public void waitFactoryName(String factoryName) {
    redrawUiElementsTimeoutWait.until(
        textToBePresentInElementLocated(By.xpath(Locators.FACTORY_NAME_XPATH), factoryName));
  }

  public void clickOnBackToFactoriesListButton() {
    redrawUiElementsTimeoutWait
        .until(visibilityOfElementLocated(By.xpath(Locators.ALL_FACTORIES_BUTTON_XPATH)))
        .click();
  }
}
