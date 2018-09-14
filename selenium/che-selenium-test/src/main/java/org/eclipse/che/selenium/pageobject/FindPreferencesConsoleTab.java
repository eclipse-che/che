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
import static org.eclipse.che.selenium.pageobject.FindPreferencesConsoleTab.Locators.ALL_FIND_REFERENCES_CONSOLE_NODES_XPATH;
import static org.eclipse.che.selenium.pageobject.FindPreferencesConsoleTab.Locators.FIND_PREFERENCES_CONSOLE_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.FindPreferencesConsoleTab.Locators.SELECTED_NODE_XPATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@Singleton
public class FindPreferencesConsoleTab {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  private FindPreferencesConsoleTab(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  public interface Locators {
    String FIND_PREFERENCES_CONSOLE_BUTTON_ID = "gwt-debug-partButton-Find References";
    String ALL_FIND_REFERENCES_CONSOLE_NODES_XPATH =
        "//div[@id='gwt-debug-LS-open-location-panel']//div[@id='content-Tree']/div";
    String SELECTED_NODE_XPATH =
        ALL_FIND_REFERENCES_CONSOLE_NODES_XPATH + "//div[contains(@class, 'selected')]";
  }

  public void clickOnPreferencesConsoleButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(FIND_PREFERENCES_CONSOLE_BUTTON_ID));
  }

  public List<WebElement> getNodes() {
    return seleniumWebDriverHelper.waitVisibilityOfAllElements(
        By.xpath(ALL_FIND_REFERENCES_CONSOLE_NODES_XPATH));
  }

  private boolean isNodeContainsText(WebElement node, String expectedText) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(node).contains(expectedText);
  }

  public void waitNodeWithText(String expectedText) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> getNodes().stream().anyMatch(node -> isNodeContainsText(node, expectedText)));
  }

  public void clickOnNode(String visibleText) {
    for (WebElement node : getNodes()) {
      if (isNodeContainsText(node, visibleText)) {
        seleniumWebDriverHelper.waitAndClick(node);
      }
    }

    String errorMessage =
        format("Any node with specified visible text: \"%s\" has not been detected", visibleText);
    throw new RuntimeException(errorMessage);
  }

  public boolean isNodeSelected(String visibleText) {
    return seleniumWebDriverHelper
        .waitVisibilityAndGetText(By.xpath(SELECTED_NODE_XPATH))
        .contains(visibleText);
  }

  public void waitNodeSelection(String visibleText) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isNodeSelected(visibleText));
  }
}
