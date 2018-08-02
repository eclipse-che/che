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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;

/** @author Musienko Maxim */
@Singleton
public class MavenPluginStatusBar {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;

  @Inject
  public MavenPluginStatusBar(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.webDriverWaitFactory = webDriverWaitFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MAVEN_PLUGIN_STATUS_BAR_INFO_XPATH =
        "//div[@id='gwt-debug-statusPanel']//div[@id='gwt-debug-loaderView-iconPanel']/following-sibling::div";
    String MAVEN_RESOLVE_DEPENDENCIES_FORM = "//table[@title='Resolving dependencies']";
  }

  @FindBy(xpath = Locators.MAVEN_PLUGIN_STATUS_BAR_INFO_XPATH)
  WebElement infoPanel;

  @FindBy(xpath = Locators.MAVEN_RESOLVE_DEPENDENCIES_FORM)
  WebElement resolveDependenciesForm;

  /**
   * wait expected text on information panel of maven plugin
   *
   * @param expectedText text which should be presented
   */
  public void waitExpectedTextInInfoPanel(String expectedText) {
    seleniumWebDriverHelper.waitTextContains(infoPanel, expectedText);
  }

  /**
   * wait expected text on information panel of maven plugin
   *
   * @param expectedText text which should be presented
   * @param expectedTimeout timeout defined by user
   */
  public void waitExpectedTextInInfoPanel(String expectedText, int expectedTimeout) {
    seleniumWebDriverHelper.waitTextContains(infoPanel, expectedText, expectedTimeout);
  }

  /** wait some text into information panel of maven plugin */
  public void waitInfoPanelIsNotEmpty() {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until((ExpectedCondition<Boolean>) driver -> !getVisibleTextFromInfoPanel().isEmpty());
  }

  /**
   * visible text from info panel Maven plugin widget
   *
   * @return visible text from the widget
   */
  public String getVisibleTextFromInfoPanel() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(infoPanel);
  }

  /** wait closing of the info panel Maven plugin widget */
  public void waitClosingInfoPanel() {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(Locators.MAVEN_PLUGIN_STATUS_BAR_INFO_XPATH), EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  /**
   * wait closing of the info panel Maven plugin widget
   *
   * @param expectedTimeout timeout defined by user
   */
  public void waitClosingInfoPanel(final int expectedTimeout) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(Locators.MAVEN_PLUGIN_STATUS_BAR_INFO_XPATH), expectedTimeout);
  }

  /** click on the info panel maven plagin */
  public void clickOnInfoPanel() {
    seleniumWebDriverHelper.waitAndClick(infoPanel);
  }

  /** wait open the resolve dependencies form */
  public void waitResolveDependenciesFormToOpen() {
    seleniumWebDriverHelper.waitVisibility(resolveDependenciesForm);
  }

  /** wait close the resolve dependencies form */
  public void waitResolveDependenciesFormToClose() {
    seleniumWebDriverHelper.waitInvisibility(resolveDependenciesForm);
  }

  /**
   * close the resolve dependencies form
   *
   * @param text is the text for sending
   */
  public void closeResolveDependenciesFormByKeys(String text) {
    seleniumWebDriverHelper.sendKeys(text);
    waitResolveDependenciesFormToClose();
  }
}
