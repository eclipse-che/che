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
package org.eclipse.che.selenium.intelligencecommand;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_SPRING;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsToolbar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckIntelligenceCommandFromToolbarTest {
  protected static final String PROJECT_NAME = generate("project", 2);
  protected String currentWindow;

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject protected ProjectExplorer projectExplorer;
  @Inject protected Consoles consoles;
  @Inject private Menu menu;
  @Inject protected Wizard wizard;
  @Inject protected CommandsToolbar commandsToolbar;
  @Inject protected SeleniumWebDriver seleniumWebDriver;
  @Inject protected SeleniumWebDriverHelper seleniumWebDriverHelper;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
    projectExplorer.waitProjectExplorer();
    consoles.waitExpectedTextIntoConsole("Initialized language server");
    currentWindow = seleniumWebDriver.getWindowHandle();
  }

  @Test
  public void launchClonedWepAppTest() {
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    selectSampleProject();
    wizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    clickAndLaunchCommandInCommandsToolbar();
    waitExpectedTextIntoConsole();
    waitOnAvailablePreviewPage(currentWindow);

    seleniumWebDriver.navigate().refresh();
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitProjectExplorer();

    selectProcessByTabName();
    waitExpectedTextIntoConsole();
    checkTestAppByPreviewUrlAndReturnToIde(currentWindow);
  }

  @Test(
      priority = 1,
      groups = {TestGroup.DOCKER})
  public void checkButtonsOnToolbarOnDocker() {
    checkButtonsOnToolbar("This site can’t be reached");
  }

  @Test(
      priority = 1,
      groups = {TestGroup.OPENSHIFT, TestGroup.K8S})
  public void checkButtonsOnToolbarOnOpenshift() {
    checkButtonsOnToolbar("Application is not available");
  }

  protected void checkButtonsOnToolbar(String expectedText) {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    commandsToolbar.clickExecStopBtn();

    checkTestAppByPreviewUrlAndReturnToIde(currentWindow, expectedText);
    commandsToolbar.clickExecRerunBtn();
    waitExpectedTextIntoConsole();
    consoles.clickOnPreviewUrl();

    waitOnAvailablePreviewPage(currentWindow);
    commandsToolbar.waitTimerValuePattern("\\d\\d:\\d\\d");
    commandsToolbar.waitNumOfProcessCounter(3);

    checkTestAppByPreviewButtonAndReturnToIde(currentWindow);
    commandsToolbar.clickExecStopBtn();
    commandsToolbar.clickWithHoldAndLaunchDebuCmdFromList(PROJECT_NAME + ": debug");
    consoles.waitExpectedTextIntoConsole(
        "Listening for transport dt_socket at address: 8000", LOADER_TIMEOUT_SEC);
    waitExpectedTextIntoConsole();
  }

  protected void checkTestAppByPreviewUrlAndReturnToIde(String currentWindow) {
    String expectedText = "Enter your name:";
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver ->
                    clickOnPreviewUrlAndCheckTextIsPresentInPageBody(currentWindow, expectedText));
  }

  protected void checkTestAppByPreviewUrlAndReturnToIde(String currentWindow, String expectedText) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver ->
                    clickOnPreviewUrlAndCheckTextIsPresentInPageBody(currentWindow, expectedText));
  }

  protected void checkTestAppByPreviewButtonAndReturnToIde(String currentWindow) {
    String expectedText = "Enter your name:";
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver ->
                    clickOnPreviewButtonAndCheckTextIsPresentInPageBody(
                        currentWindow, expectedText));
  }

  protected boolean clickOnPreviewUrlAndCheckTextIsPresentInPageBody(
      String currentWindow, String expectedText) {
    consoles.clickOnPreviewUrl();
    return switchToOpenedWindowAndCheckTextIsPresent(currentWindow, expectedText);
  }

  protected boolean clickOnPreviewButtonAndCheckTextIsPresentInPageBody(
      String currentWindow, String expectedText) {
    commandsToolbar.clickOnPreviewCommandBtnAndSelectUrl("dev-machine:tomcat8");
    return switchToOpenedWindowAndCheckTextIsPresent(currentWindow, expectedText);
  }

  protected boolean switchToOpenedWindowAndCheckTextIsPresent(
      String currentWindow, String expectedText) {
    seleniumWebDriverHelper.switchToNextWindow(currentWindow);
    boolean result = getBodyText().contains(expectedText);
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);

    return result;
  }

  protected void waitOnAvailablePreviewPage(String currentWindow) {
    String expectedTextOnPreviewPage = "Enter your name:";
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> isPreviewPageAvailable(currentWindow, expectedTextOnPreviewPage));
  }

  protected Boolean isPreviewPageAvailable(String currentWindow, String expectedText) {
    consoles.clickOnPreviewUrl();
    seleniumWebDriverHelper.switchToNextWindow(currentWindow);

    if (getBodyText().contains(expectedText)) {
      seleniumWebDriver.close();
      seleniumWebDriver.switchTo().window(currentWindow);
      return true;
    }

    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);
    return false;
  }

  private WebElement getBody() {
    return new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.tagName("body")));
  }

  protected String getBodyText() {
    return new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until((ExpectedCondition<String>) driver -> getBody().getText());
  }

  protected void selectSampleProject() {
    wizard.selectProjectAndCreate(WEB_JAVA_SPRING, PROJECT_NAME);
  }

  protected void clickAndLaunchCommandInCommandsToolbar() {
    commandsToolbar.clickWithHoldAndLaunchCommandFromList(PROJECT_NAME + ": build and run");
  }

  protected void waitExpectedTextIntoConsole() {
    consoles.waitExpectedTextIntoConsole(" Server startup in", EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  protected void selectProcessByTabName() {
    consoles.selectProcessByTabName(PROJECT_NAME + ": build and run");
  }
}
