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

import static org.eclipse.che.selenium.pageobject.AssistantFindPanel.Locators.ACTION_NODE_ID_PATTERN;
import static org.eclipse.che.selenium.pageobject.AssistantFindPanel.Locators.ALL_ACTIONS_XPATH;
import static org.eclipse.che.selenium.pageobject.AssistantFindPanel.Locators.PANEL_ID;
import static org.eclipse.che.selenium.pageobject.AssistantFindPanel.Locators.TEXT_FIELD_ID;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_UP;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@Singleton
public class AssistantFindPanel {
  private final Menu menu;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final TestWebElementRenderChecker testWebElementRenderChecker;

  @Inject
  private AssistantFindPanel(
      Menu menu,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      TestWebElementRenderChecker testWebElementRenderChecker) {
    this.menu = menu;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
  }

  public interface Locators {
    String PANEL_ID = "gwt-debug-quickBoxForm";
    String TEXT_FIELD_ID = "gwt-debug-quickBoxNameField";
    String ACTION_NODE_ID_PATTERN = "quick-open-form-action-node-%s";
    String ALL_ACTIONS_XPATH = "//div[contains(@id, 'quick-open-form-action-node-')]";
  }

  public void waitPanel() {
    By findPanelLocator = By.id(PANEL_ID);
    seleniumWebDriverHelper.waitVisibility(findPanelLocator);
    testWebElementRenderChecker.waitElementIsRendered(findPanelLocator);
  }

  public void waitPanelClosing() {
    seleniumWebDriverHelper.waitInvisibility(By.id(PANEL_ID));
  }

  public void waitTextField() {
    seleniumWebDriverHelper.waitVisibility(By.id(TEXT_FIELD_ID));
  }

  public void typeToTextField(String text) {
    seleniumWebDriverHelper.setValue(By.id(TEXT_FIELD_ID), text);
  }

  public String getInputtedText() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.id(TEXT_FIELD_ID));
  }

  public WebElement getActionNode(int index) {
    final String actionNodeId = String.format(ACTION_NODE_ID_PATTERN, index);
    return seleniumWebDriverHelper.waitVisibility(By.id(actionNodeId));
  }

  public int getActionsCount() {
    return getActionsList().size();
  }

  public List<WebElement> getActionsList() {
    return seleniumWebDriverHelper.waitVisibilityOfAllElements(By.xpath(ALL_ACTIONS_XPATH));
  }

  public void waitActionsCount(int expectedCount) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> getActionsCount() == expectedCount);
  }

  public String getActionNodeText(int index) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(getActionNode(index));
  }

  public void waitActionContainsText(int nodeIndex, String expectedText) {
    seleniumWebDriverHelper.waitTextContains(getActionNode(nodeIndex), expectedText);
  }

  public void waitActionContainsText(String expectedText) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          for (int i = 0; i < getActionsCount(); i++) {
            if (isNodeContainsText(i, expectedText)) {
              return true;
            }
          }
          return false;
        });
  }

  public void waitActionTextEqualsTo(int nodeIndex, String expectedText) {
    seleniumWebDriverHelper.waitTextEqualsTo(getActionNode(nodeIndex), expectedText);
  }

  public boolean isNodeContainsText(int index, String expectedText) {
    return getActionNodeText(index).contains(expectedText);
  }

  public void clickOnNodeWithText(String visibleText) {
    for (int i = 0; i < getActionsCount(); i++) {
      if (getActionNodeText(i).contains(visibleText)) {
        getActionNode(i).click();
        return;
      }

      String exceptionMessage =
          String.format(
              "Item with expected visible text: \"%s\" has not been detected", visibleText);
      throw new RuntimeException(exceptionMessage);
    }
  }

  public void clickOnActionNode(int index) {
    getActionNode(index).click();
  }

  public void pressDown() {
    seleniumWebDriverHelper.getAction().sendKeys(ARROW_DOWN).perform();
  }

  public void pressUp() {
    seleniumWebDriverHelper.getAction().sendKeys(ARROW_UP).perform();
  }

  public void pressEnter() {
    seleniumWebDriverHelper.getAction().sendKeys(ENTER).perform();
  }

  public int getSelectedNodeIndex() {
    for (int i = 0; i < getActionsCount(); i++) {
      boolean isAttributePresent = null != getActionNode(i).getAttribute("selected");
      if (isAttributePresent) {
        return i;
      }
    }

    throw new RuntimeException("Any node has not been selected");
  }

  public void waitNodeSelection(int index) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> index == getSelectedNodeIndex());
  }

  public void waitNodeSelection(String visibleText) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          int selectedNodeIndex = getSelectedNodeIndex();
          String selectedNodeText = getActionNodeText(selectedNodeIndex);
          return selectedNodeText.contains(visibleText);
        });
  }
}
