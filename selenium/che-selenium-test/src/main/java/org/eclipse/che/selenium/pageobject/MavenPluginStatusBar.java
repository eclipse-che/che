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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class MavenPluginStatusBar {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public MavenPluginStatusBar(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MAVEN_PLUGIN_STATUS_BAR_INFO_XPATH =
        "//div[@id='gwt-debug-statusPanel']//div[@id='gwt-debug-loaderView-iconPanel']/following-sibling::div";
  }

  @FindBy(xpath = Locators.MAVEN_PLUGIN_STATUS_BAR_INFO_XPATH)
  WebElement infoPanel;

  /**
   * wait expected text on information panel of maven plugin
   *
   * @param expectedText
   */
  public void waitExpectedTextInInfoPanel(String expectedText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElement(infoPanel, expectedText));
  }

  /** wait some text into information panel of maven plugin */
  public void waitInfPanelIsNotEmpty() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver input) {
                return !infoPanel.getText().isEmpty();
              }
            });
  }

  /**
   * visible text from info panel Maven plugin widget
   *
   * @return visible text from the widget
   */
  public String getVisibleTextFromInfoPanel() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(infoPanel))
        .getText();
  }

  /** wait closing of the info panel Maven plugin widget */
  public void waitClosingInfoPanel() {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(Locators.MAVEN_PLUGIN_STATUS_BAR_INFO_XPATH)));
  }

  /**
   * wait closing of the info panel Maven plugin widget
   *
   * @param expectedTimeout timeout defined by user
   */
  public void waitClosingInfoPanel(final int expectedTimeout) {
    new WebDriverWait(seleniumWebDriver, expectedTimeout)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(Locators.MAVEN_PLUGIN_STATUS_BAR_INFO_XPATH)));
  }
}
