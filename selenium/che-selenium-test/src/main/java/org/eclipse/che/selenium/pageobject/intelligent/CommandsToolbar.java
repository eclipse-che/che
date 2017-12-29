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
package org.eclipse.che.selenium.pageobject.intelligent;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MULTIPLE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author */
@Singleton
public class CommandsToolbar {
  private WebDriverWait appearanceWait;
  private WebDriverWait redrawWait;
  private WebDriverWait loadPageWait;

  private final SeleniumWebDriver seleniumWebDriver;
  private final TestWebElementRenderChecker testWebElementRenderChecker;

  @Inject
  public CommandsToolbar(
      SeleniumWebDriver seleniumWebDriver,
      TestWebElementRenderChecker testWebElementRenderChecker) {
    this.seleniumWebDriver = seleniumWebDriver;
    redrawWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    appearanceWait = new WebDriverWait(seleniumWebDriver, MULTIPLE);
    loadPageWait =
        new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC);
    this.testWebElementRenderChecker = testWebElementRenderChecker;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** Class introduce Xpath locators for Commands Toolbar */
  private static final class Locators {
    static final String COMMANDS_TOOLBAR_SELECT = "gwt-debug-command_toolbar-button_Run";
    static final String EXECUTE_COMMAND_TOOLBAR = "gwt-debug-dropdown-processes";
    static final String COMMAND_DROPDAWN = "//div[@class='popupContent']//div[text()='%s']";
    static final String EXEC_DROPDAWN_CONATAINER = "gwt-debug-dropdown-list-content-panel";
    static final String EXEC_COMMAND_SELECTOR = EXEC_DROPDAWN_CONATAINER + "//span[text()='%s']";
    static final String EXEC_STOP_PROCESS_BUTTON = "gwt-debug-dropdown-processes-stop";
    static final String EXEC_RERUN_PROCESS_BUTTON = "gwt-debug-dropdown-processes-rerun";
    static final String TIMER_LOCATOR = "gwt-debug-dropdown-processes-label-duration";
    static final String NUM_OF_PROCESS = "gwt-debug-dropdown-processes-label-pid";
    static final String PREVIEWS =
        "//div[@id='gwt-debug-dropdown-preview_url']//*[local-name()='svg']";
    static final String PREVIEWS_DROPDAWN_CONTAINER = "gwt-debug-dropdown-list-content-panel";
    static final String DEBUG_COMMAND_DROPDAWN = "gwt-debug-command_toolbar-button_Debug";

    protected Locators() {}
  }

  @FindBy(id = Locators.COMMANDS_TOOLBAR_SELECT)
  WebElement commandsToolbarSelect;

  @FindBy(id = Locators.EXECUTE_COMMAND_TOOLBAR)
  WebElement executeCommandToolbar;

  @FindBy(id = Locators.EXEC_DROPDAWN_CONATAINER)
  WebElement execDropDawnCommandContainer;

  @FindBy(id = Locators.EXEC_STOP_PROCESS_BUTTON)
  WebElement execStopBtn;

  @FindBy(id = Locators.EXEC_RERUN_PROCESS_BUTTON)
  WebElement execRerunBtn;

  @FindBy(id = Locators.TIMER_LOCATOR)
  WebElement execTimer;

  @FindBy(id = Locators.NUM_OF_PROCESS)
  WebElement execProcessCounter;

  @FindBy(xpath = Locators.PREVIEWS)
  WebElement previewsUrlButton;

  @FindBy(id = Locators.PREVIEWS_DROPDAWN_CONTAINER)
  WebElement previewsDropDawnContainer;

  @FindBy(id = Locators.DEBUG_COMMAND_DROPDAWN)
  WebElement debugCommandBtn;

  /** expand exec dropdawn list */
  public void selectProcessFromExecDropDawnAndStop(String commandName) {
    clickOnExecDropDawn();
    appearanceWait
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locators.EXEC_COMMAND_SELECTOR, commandName))))
        .click();
  }

  /** click on the launch command button */
  public void clickOnChooseCommandBtn(String commandName) {
    redrawWait.until(ExpectedConditions.visibilityOf(commandsToolbarSelect)).click();
  }

  /**
   * click on the launch command button, hold left button of mouse, wait name of Command into
   * dropdown list
   */
  public void clickWithHoldAndLaunchCommandFromList(String nameOfCommand) {
    redrawWait.until(ExpectedConditions.visibilityOf(commandsToolbarSelect));
    Actions action = new Actions(seleniumWebDriver);
    action.clickAndHold(commandsToolbarSelect).perform();

    waitListIsRenderedAndClickOnItem(nameOfCommand, action);
  }

  /**
   * click on the launch command button, hold left button of mouse, wait name of Command into
   * dropdown list
   *
   * @param nameOfCommand an expected command in the dropdawn
   */
  public void clickWithHoldAndLaunchDebuCmdFromList(String nameOfCommand) {
    redrawWait.until(ExpectedConditions.visibilityOf(debugCommandBtn));
    Actions action = new Actions(seleniumWebDriver);
    action.clickAndHold(debugCommandBtn).perform();

    waitListIsRenderedAndClickOnItem(nameOfCommand, action);
  }

  /** wait rerun button on exec toolbar command widget and click it */
  public void clickExecRerunBtn() {
    redrawWait.until(ExpectedConditions.visibilityOf(execRerunBtn)).click();
  }

  /** wait stop button on exec toolbar command widget and click it */
  public void clickExecStopBtn() {
    redrawWait.until(ExpectedConditions.visibilityOf(execStopBtn)).click();
  }

  /** click on the 'Execute selected command' on the toolbar */
  public void clickOnExecDropDawn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(executeCommandToolbar))
        .click();
  }

  /** wait appearance of process timer on commands toolbar and try to get value of the timer */
  public String getTimerValue() {
    Wait<WebDriver> wait =
        new FluentWait<WebDriver>(seleniumWebDriver)
            .withTimeout(REDRAW_UI_ELEMENTS_TIMEOUT_SEC, TimeUnit.SECONDS)
            .pollingEvery(200, TimeUnit.MILLISECONDS)
            .ignoring(StaleElementReferenceException.class);

    return wait.until(driver -> driver.findElement(By.id(Locators.TIMER_LOCATOR)).getText());
  }

  public String getNumOfProcessCounter() {
    return redrawWait.until(ExpectedConditions.visibilityOf(execProcessCounter)).getText();
  }

  public void clickOnPreviewsUrlButton() {
    redrawWait.until(ExpectedConditions.visibilityOf(previewsUrlButton)).click();
  }

  /**
   * select preview url from list of intelligence toolbar
   *
   * @param urlCommand an expected command
   */
  public void selectPreviewUrlFromDropDawn(String urlCommand) {
    redrawWait.until(ExpectedConditions.visibilityOf(previewsDropDawnContainer));
    WebElement element =
        seleniumWebDriver.findElement(By.xpath(String.format("//div[text()='%s']", urlCommand)));
    WaitUtils.sleepQuietly(1);
    redrawWait.until(ExpectedConditions.visibilityOf(element)).click();
  }

  /**
   * click with holding on preview button of intelligence toolbar, wait dropdown list, select
   * expected url
   *
   * @param urlCommand an expected url
   */
  public void clickOnPreviewCommandBtnAndSelectUrl(String urlCommand) {
    clickOnPreviewsUrlButton();
    selectPreviewUrlFromDropDawn(urlCommand);
  }

  private void waitListIsRenderedAndClickOnItem(String nameOfCommand, Actions action) {
    testWebElementRenderChecker.waitElementIsRendered(By.id("commandsPopup"));
    action.release();
    loadPageWait
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.COMMAND_DROPDAWN, nameOfCommand))))
        .click();
  }
}
