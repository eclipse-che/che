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
package org.eclipse.che.selenium.pageobject.intelligent;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MULTIPLE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
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
  private final ActionsFactory actionsFactory;

  @Inject
  public CommandsToolbar(
      SeleniumWebDriver seleniumWebDriver,
      TestWebElementRenderChecker testWebElementRenderChecker,
      ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    redrawWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    appearanceWait = new WebDriverWait(seleniumWebDriver, MULTIPLE);
    loadPageWait =
        new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC);
    this.testWebElementRenderChecker = testWebElementRenderChecker;
    this.actionsFactory = actionsFactory;
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
                By.xpath(format(Locators.EXEC_COMMAND_SELECTOR, commandName))))
        .click();
  }

  /** click on the launch command button */
  public void clickOnChooseCommandBtn(String commandName) {
    redrawWait.until(visibilityOf(commandsToolbarSelect)).click();
  }

  /**
   * click on the launch command button, hold left button of mouse, wait name of Command into
   * dropdown list
   */
  public void clickWithHoldAndLaunchCommandFromList(String nameOfCommand) {
    redrawWait.until(visibilityOf(commandsToolbarSelect));
    Actions action = actionsFactory.createAction(seleniumWebDriver);
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
    redrawWait.until(visibilityOf(debugCommandBtn));
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.clickAndHold(debugCommandBtn).perform();

    waitListIsRenderedAndClickOnItem(nameOfCommand, action);
  }

  /** wait rerun button on exec toolbar command widget and click it */
  public void clickExecRerunBtn() {
    redrawWait.until(visibilityOf(execRerunBtn)).click();
  }

  /** wait stop button on exec toolbar command widget and click it */
  public void clickExecStopBtn() {
    redrawWait.until(visibilityOf(execStopBtn)).click();
  }

  /** click on the 'Execute selected command' on the toolbar */
  public void clickOnExecDropDawn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(executeCommandToolbar))
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
    return redrawWait.until(visibilityOf(execProcessCounter)).getText();
  }

  public void clickOnPreviewsUrlButton() {
    redrawWait.until(visibilityOf(previewsUrlButton)).click();
  }

  /**
   * select preview url from list of intelligence toolbar
   *
   * @param urlCommand an expected command
   */
  public void selectPreviewUrlFromDropDawn(String urlCommand) {
    testWebElementRenderChecker.waitElementIsRendered(
        By.xpath("//div[@id='gwt-debug-dropdown-list-content-panel']/div"));

    clickOnElement(getCommandsToolbarPreviewLink(urlCommand));
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

  /**
   * simple click is does not working well in grid mode on the "CI" in drop-down lists
   *
   * @param element
   */
  private void clickOnElement(WebElement element) {
    actionsFactory.createAction(seleniumWebDriver).moveToElement(element).click().perform();
  }

  /**
   * simple click is does not working well in grid mode on the "CI" in drop-down lists
   *
   * @param element
   */
  private void clickOnElement(WebElement element, Actions action) {
    action.moveToElement(element).click().perform();
  }

  private void waitListIsRenderedAndClickOnItem(String nameOfCommand, Actions action) {
    testWebElementRenderChecker.waitElementIsRendered(By.id("commandsPopup"));
    action.release();
    clickOnElement(getElementFromCommandsDropDown(nameOfCommand), action);
  }

  private WebElement getCommandsToolbarPreviewLink(String urlCommand) {
    return loadPageWait.until(
        visibilityOfElementLocated(By.xpath(format("//div[text()='%s']", urlCommand))));
  }

  private WebElement getElementFromCommandsDropDown(String nameOfCommand) {
    return loadPageWait.until(
        visibilityOfElementLocated(By.xpath(format(Locators.COMMAND_DROPDAWN, nameOfCommand))));
  }
}
