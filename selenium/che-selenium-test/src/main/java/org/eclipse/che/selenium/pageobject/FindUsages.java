/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev on 16.12.15 */
@Singleton
public class FindUsages {

  private final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;

  @Inject
  public FindUsages(SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
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
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.FIND_USAGES_INFO_PANEL)));
  }

  /** wait the 'find usages' panel is closed */
  public void waitFindUsagesPanelIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.id(Locators.FIND_USAGES_INFO_PANEL)));
  }

  /** click on the find tab */
  public void clickFindUsagesIcon() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(findUsagesIcon))
        .click();
  }

  /**
   * wait expected text in the 'find usages' panel
   *
   * @param expText expected value
   */
  public void waitExpectedTextInFindUsagesPanel(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> getTextFromFindUsagesPanel().contains(expText));
  }

  /**
   * wait the text is not present in the 'find usages' panel
   *
   * @param expText expected value
   */
  public void waitExpectedTextIsNotPresentInFindUsagesPanel(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> !(getTextFromFindUsagesPanel().contains(expText)));
  }

  /**
   * get text - representation content from 'find usages' panel
   *
   * @return text from 'find usages' panel
   */
  public String getTextFromFindUsagesPanel() {
    return findPanel.getText();
  }

  /**
   * perform click on the icon node in the 'find usages' panel form
   *
   * @param nameNode is name of the node
   */
  public void clickOnIconNodeInFindUsagesPanel(String nameNode) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.FIND_USAGES_ICON_NODE, nameNode))))
        .click();
  }

  /**
   * perform 'double click' on the certain node in the 'find usages' panel
   *
   * @param node is the name of the node
   */
  public void selectNodeInFindUsagesByDoubleClick(String node) {
    WebElement findUsagesNode =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(visibilityOfElementLocated(By.xpath(format(Locators.FIND_USAGES_NODE, node))));
    findUsagesNode.click();
    actionsFactory.createAction(seleniumWebDriver).doubleClick(findUsagesNode).perform();
  }

  /**
   * perform 'click' on the certain item in the 'find usages' panel
   *
   * @param node is the name of the node
   */
  public void selectNodeInFindUsagesPanel(String node) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.FIND_USAGES_NODE, node))))
        .click();
  }

  /**
   * send a keys by keyboard in the 'find usages' panel
   *
   * @param command is the command by keyboard
   */
  public void sendCommandByKeyboardInFindUsagespanel(String command) {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(command).perform();
    // waiting for ending animation in the widget
    WaitUtils.sleepQuietly(500, TimeUnit.MILLISECONDS);
  }

  /**
   * perform 'double click' on the highlighted item in the 'find usages' panel
   *
   * @param numLine is the number line of usage
   */
  public void selectHighlightedItemInFindUsagesByDoubleClick(int numLine) {
    WebElement findHighlightedItem =
        seleniumWebDriver.findElement(
            By.xpath(format(Locators.FIND_USAGES_HIGHLIGHTED_ITEM, numLine)));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(findHighlightedItem));
    findHighlightedItem.click();
    actionsFactory.createAction(seleniumWebDriver).doubleClick(findHighlightedItem).perform();
  }

  /** wait a selected element in the 'find usages' panel */
  public void waitSelectedElementInFindUsagesPanel(String nameElement) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.FIND_USAGES_HIGHLIGHTED_ITEM, nameElement))));
    final WebElement item =
        seleniumWebDriver.findElement(
            By.xpath(format(Locators.FIND_USAGES_HIGHLIGHTED_ITEM, nameElement)));
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (WebDriver webDriver) ->
                item.getCssValue("background-image").contains(HIGHLIGHTED_IMAGE));
  }
}
