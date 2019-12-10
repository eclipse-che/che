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

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.AssistantFindPanel.Locators.ACTION_NODE_ID_PATTERN;
import static org.eclipse.che.selenium.pageobject.AssistantFindPanel.Locators.ALL_ACTIONS_XPATH;
import static org.eclipse.che.selenium.pageobject.AssistantFindPanel.Locators.PANEL_ID;
import static org.eclipse.che.selenium.pageobject.AssistantFindPanel.Locators.TEXT_FIELD_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.List;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
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

  public void waitForm() {
    By findPanelLocator = By.id(PANEL_ID);
    seleniumWebDriverHelper.waitVisibility(findPanelLocator);
    testWebElementRenderChecker.waitElementIsRendered(findPanelLocator);
  }

  public void waitFormIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.id(PANEL_ID));
  }

  public void waitInputField() {
    seleniumWebDriverHelper.waitVisibility(By.id(TEXT_FIELD_ID));
  }

  public void clickOnInputField() {
    seleniumWebDriverHelper.waitAndClick(By.id(TEXT_FIELD_ID));
  }

  public void typeToInputField(String text) {
    seleniumWebDriverHelper.setValue(By.id(TEXT_FIELD_ID), text);
  }

  public String getInputtedText() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.id(TEXT_FIELD_ID));
  }

  public WebElement getActionNode(int index) {
    final String actionNodeId = String.format(ACTION_NODE_ID_PATTERN, index);
    return seleniumWebDriverHelper.waitVisibility(By.id(actionNodeId));
  }

  public int getActionNodesCount() {
    return getActionNodesCount(LOAD_PAGE_TIMEOUT_SEC);
  }

  public int getActionNodesCount(int timeout) {
    try {
      return getActionNodesList(timeout).size();
    } catch (TimeoutException ex) {
      return 0;
    }
  }

  public List<WebElement> getActionNodesList() {
    return getActionNodesList(LOAD_PAGE_TIMEOUT_SEC);
  }

  public List<WebElement> getActionNodesList(int timeout) {
    return seleniumWebDriverHelper.waitVisibilityOfAllElements(
        By.xpath(ALL_ACTIONS_XPATH), timeout);
  }

  public void waitActionNodesCount(int expectedCount) {
    waitActionNodesCount(expectedCount, LOAD_PAGE_TIMEOUT_SEC);
  }

  public void waitActionNodesCount(int expectedCount, int timeout) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> getActionNodesCount(timeout) == expectedCount, timeout);
  }

  public String getActionNodeText(int index) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(getActionNode(index));
  }

  public void waitNode(int index, String expectedText) {
    waitNode(index, expectedText, LOAD_PAGE_TIMEOUT_SEC);
  }

  public void waitNode(int index, String expectedText, int timeout) {
    seleniumWebDriverHelper.waitTextContains(getActionNode(index), expectedText, timeout);
  }

  public void waitNode(String expectedText) {
    waitNode(expectedText, WIDGET_TIMEOUT_SEC * 2);
  }

  public void waitNode(String expectedText, int timeout) {
    seleniumWebDriverHelper.waitNoExceptions(
        () ->
            seleniumWebDriverHelper.waitSuccessCondition(
                driver -> {
                  for (int i = 0; i < getActionNodesCount(timeout); i++) {
                    if (isActionNodeContainsText(i, expectedText)) {
                      return true;
                    }
                  }
                  return false;
                }),
        timeout,
        StaleElementReferenceException.class);
  }

  public void waitAllNodes(String... expectedNodesText) {
    asList(expectedNodesText).forEach(nodeText -> waitNode(nodeText));
  }

  public void waitAllNodes(Collection<String> expectedNodesText) {
    expectedNodesText.forEach(this::waitNode);
  }

  public void waitActionNodeTextEqualsTo(int nodeIndex, String expectedText) {
    seleniumWebDriverHelper.waitTextEqualsTo(getActionNode(nodeIndex), expectedText);
  }

  public boolean isActionNodeContainsText(int index, String expectedText) {
    return getActionNodeText(index).contains(expectedText);
  }

  public void clickOnActionNodeWithTextContains(String visibleText) {
    for (int i = 0; i < getActionNodesCount(); i++) {
      if (getActionNodeText(i).contains(visibleText)) {
        getActionNode(i).click();
        return;
      }
    }

    String exceptionMessage =
        String.format("Item with expected visible text: \"%s\" has not been detected", visibleText);
    throw new RuntimeException(exceptionMessage);
  }

  public void clickOnActionNodeWithTextEqualsTo(String visibleText) {
    for (int i = 0; i < getActionNodesCount(); i++) {
      if (getActionNodeText(i).equals(visibleText)) {
        getActionNode(i).click();
        return;
      }
    }

    String exceptionMessage =
        String.format("Item with expected visible text: \"%s\" has not been detected", visibleText);
    throw new RuntimeException(exceptionMessage);
  }

  public void clickOnActionNode(int index) {
    getActionNode(index).click();
  }

  public int getSelectedNodeIndex() {
    for (int i = 0; i < getActionNodesCount(); i++) {
      boolean isAttributePresent = null != getActionNode(i).getAttribute("selected");
      if (isAttributePresent) {
        return i;
      }
    }

    throw new RuntimeException("Any node has not been selected");
  }

  public void waitActionNodeSelection(int index) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> index == getSelectedNodeIndex());
  }

  public void waitActionNodeSelection(String visibleText) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          int selectedNodeIndex = getSelectedNodeIndex();
          String selectedNodeText = getActionNodeText(selectedNodeIndex);
          return selectedNodeText.contains(visibleText);
        });
  }
}
