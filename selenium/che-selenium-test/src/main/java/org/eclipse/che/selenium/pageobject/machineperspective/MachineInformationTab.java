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
package org.eclipse.che.selenium.pageobject.machineperspective;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class MachineInformationTab {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public MachineInformationTab(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String INFORMATION_TAB_XPATH = "//div[@id='gwt-debug-editorPanel']//div[text()='Information']";
  }

  @FindBy(xpath = Locators.INFORMATION_TAB_XPATH)
  WebElement informationTab;

  /** wait appearance the main terminal container */
  public void waitInformationTab() {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(informationTab));
  }

  /** select Information tab */
  public void selectInformationTab() {
    waitInformationTab();
    informationTab.click();
  }

  /**
   * wait expected text into Information tab
   *
   * @param expectedText
   */
  public void waitTextIntoInfoTab(String expectedText) {
    String locatorXpath = "//div[@id='gwt-debug-editorPanel']//div";
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.textToBePresentInElementLocated(
                By.xpath(locatorXpath), expectedText));
  }
}
