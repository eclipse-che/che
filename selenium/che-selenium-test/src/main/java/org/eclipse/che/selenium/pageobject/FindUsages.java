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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** @author Aleksandr Shmaraev on 16.12.15 */
@Singleton
public class FindUsages {

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;

  @Inject
  public FindUsages(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.webDriverWaitFactory = webDriverWaitFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private final String HIGHLIGHTED_IMAGE = "rgb(184, 156, 7)";

  private interface Locators {
    String FIND_USAGES_INFO_PANEL = "gwt-debug-findUsages-panel";
    String FIND_USAGES_ICON = "gwt-debug-partButton-Usages";
    String FIND_USAGES_NODE = "//div[@id='gwt-debug-findUsages-panel']//div[text()='%s']";
    String FIND_USAGES_ICON_NODE =
        "//div[@id='gwt-debug-findUsages-panel']//div[text()='%s']"
            + "/preceding::*[local-name()='svg'][2]";
    String FIND_USAGES_HIGHLIGHTED_ITEM =
        "//div[@id='gwt-debug-findUsages-panel']//span[contains(text(), '%s')]";
  }

  @FindBy(id = Locators.FIND_USAGES_INFO_PANEL)
  WebElement findPanel;

  @FindBy(id = Locators.FIND_USAGES_ICON)
  WebElement findUsagesIcon;

  /** wait the 'find usages' panel is open */
  public void waitFindUsagesPanelIsOpen() {
    seleniumWebDriverHelper.waitVisibility(By.id(Locators.FIND_USAGES_INFO_PANEL));
  }

  /** wait the 'find usages' panel is closed */
  public void waitFindUsagesPanelIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.id(Locators.FIND_USAGES_INFO_PANEL));
  }

  /** click on the find tab */
  public void clickFindUsagesIcon() {
    seleniumWebDriverHelper.waitAndClick(findUsagesIcon);
  }

  /**
   * wait expected text in the 'find usages' panel
   *
   * @param expText expected value
   */
  public void waitExpectedTextInFindUsagesPanel(String expText) {
    seleniumWebDriverHelper.waitTextContains(findPanel, expText);
  }

  /**
   * wait the text is not present in the 'find usages' panel
   *
   * @param expText expected value
   */
  public void waitExpectedTextIsNotPresentInFindUsagesPanel(String expText) {
    seleniumWebDriverHelper.waitTextIsNotPresented(findPanel, expText);
  }

  /**
   * get text - representation content from 'find usages' panel
   *
   * @return text from 'find usages' panel
   */
  public String getTextFromFindUsagesPanel() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(findPanel);
  }

  /**
   * perform click on the icon node in the 'find usages' panel form
   *
   * @param nameNode is name of the node
   */
  public void clickOnIconNodeInFindUsagesPanel(String nameNode) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.FIND_USAGES_ICON_NODE, nameNode)));
  }

  /**
   * perform 'double click' on the certain node in the 'find usages' panel
   *
   * @param node is the name of the node
   */
  public void selectNodeInFindUsagesByDoubleClick(String node) {
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(
        By.xpath(format(Locators.FIND_USAGES_NODE, node)));
  }

  /**
   * perform 'click' on the certain item in the 'find usages' panel
   *
   * @param node is the name of the node
   */
  public void selectNodeInFindUsagesPanel(String node) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(Locators.FIND_USAGES_NODE, node)));
  }

  /**
   * send a keys by keyboard in the 'find usages' panel
   *
   * @param command is the command by keyboard
   */
  public void sendCommandByKeyboardInFindUsagesPanel(String command) {
    seleniumWebDriverHelper.sendKeys(command);
  }

  /**
   * perform 'double click' on the highlighted item in the 'find usages' panel
   *
   * @param numLine is the number line of usage
   */
  public void selectHighlightedItemInFindUsagesByDoubleClick(int numLine) {
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(
        By.xpath(format(Locators.FIND_USAGES_HIGHLIGHTED_ITEM, numLine)));
  }

  /** wait a selected element in the 'find usages' panel */
  public void waitSelectedElementInFindUsagesPanel(String nameElement) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver ->
            getHighlightedItem(nameElement)
                .getCssValue("background-image")
                .contains(HIGHLIGHTED_IMAGE));
  }

  private WebElement getHighlightedItem(String nameElement) {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(Locators.FIND_USAGES_HIGHLIGHTED_ITEM, nameElement)));
  }
}
