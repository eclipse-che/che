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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.FindReferencesConsoleTab.Locators.FIND_REFERENCES_CONSOLE_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.FindReferencesConsoleTab.Locators.FOUND_REFERENCES_XPATH;
import static org.eclipse.che.selenium.pageobject.FindReferencesConsoleTab.Locators.SELECTED_REFERENCE_XPATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@Singleton
public class FindReferencesConsoleTab {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final ActionsFactory actionsFactory;

  @Inject
  private FindReferencesConsoleTab(
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      SeleniumWebDriver seleniumWebDriver,
      ActionsFactory actionsFactory) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
  }

  public interface Locators {
    String LS_INFO_PANEL_ID = "gwt-debug-LS-open-location-panel";
    String FIND_REFERENCES_CONSOLE_BUTTON_ID = "gwt-debug-partButton-Find References";
    String FOUND_REFERENCES_XPATH =
        "//div[@id='gwt-debug-LS-open-location-panel']//div[@id='content-Tree']/div";
    String SELECTED_REFERENCE_XPATH =
        FOUND_REFERENCES_XPATH + "//div[contains(@class, 'selected')]";
  }

  public void clickOnReferencesConsoleButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(FIND_REFERENCES_CONSOLE_BUTTON_ID));
  }

  public List<WebElement> getReferences() {
    return seleniumWebDriverHelper.waitVisibilityOfAllElements(By.xpath(FOUND_REFERENCES_XPATH));
  }

  private boolean isReferenceContainsText(WebElement reference, String expectedText) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(reference).contains(expectedText);
  }

  private void waitReferenceWithText(String expectedText) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver ->
            getReferences()
                .stream()
                .anyMatch(reference -> isReferenceContainsText(reference, expectedText)));
  }

  public void waitAllReferencesWithText(String... expectedText) {
    asList(expectedText).forEach(text -> waitReferenceWithText(text));
  }

  public void clickOnReference(String visibleText) {
    for (WebElement reference : getReferences()) {
      if (isReferenceContainsText(reference, visibleText)) {
        seleniumWebDriverHelper.waitAndClick(reference);
        return;
      }
    }

    String errorMessage = format("No reference with text \"%s\" has been detected", visibleText);
    throw new RuntimeException(errorMessage);
  }

  public void doubleClickOnReference(String visibleText) {
    clickOnReference(visibleText);
    waitReferenceSelection(visibleText);
    seleniumWebDriverHelper.doubleClick();
  }

  public boolean isReferenceSelected(String visibleText) {
    return seleniumWebDriverHelper
        .waitVisibilityAndGetText(By.xpath(SELECTED_REFERENCE_XPATH))
        .contains(visibleText);
  }

  public void waitReferenceSelection(String visibleText) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isReferenceSelected(visibleText));
  }

  /** use to check the text when nothing to show */
  public void waitExpectedTextInLsPanel(String text) {
    seleniumWebDriverHelper.waitTextContains(By.id(Locators.LS_INFO_PANEL_ID), text);
  }
}
