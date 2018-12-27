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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
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
  private static final String PROJECT_NAME = NameGenerator.generate("project", 2);
  private String currentWindow;

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private CommandsToolbar commandsToolbar;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
    projectExplorer.waitProjectExplorer();
    currentWindow = seleniumWebDriver.getWindowHandle();
  }

  @Test
  public void launchClonedWepAppTest() throws Exception {
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectProjectAndCreate(Wizard.SamplesName.WEB_JAVA_SPRING, PROJECT_NAME);
    wizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    commandsToolbar.clickWithHoldAndLaunchCommandFromList(PROJECT_NAME + ": build and run");
    consoles.waitExpectedTextIntoConsole(" Server startup in");

    waitOnAvailablePreviewPage(currentWindow, "Enter your name");
    consoles.waitExpectedTextIntoConsole(" Server startup in");
    seleniumWebDriver.navigate().refresh();
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitProjectExplorer();
    consoles.selectProcessByTabName(PROJECT_NAME + ": build and run");
    consoles.waitExpectedTextIntoConsole(" Server startup in");
    checkTestAppByPreviewUrlAndReturnToIde(currentWindow, "Enter your name:");
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

  private void checkButtonsOnToolbar(String expectedText) {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    commandsToolbar.clickExecStopBtn();

    checkTestAppByPreviewUrlAndReturnToIde(currentWindow, expectedText);
    commandsToolbar.clickExecRerunBtn();
    consoles.waitExpectedTextIntoConsole(" Server startup in");
    consoles.clickOnPreviewUrl();

    waitOnAvailablePreviewPage(currentWindow, "Enter your name:");
    commandsToolbar.waitTimerValuePattern("\\d\\d:\\d\\d");
    commandsToolbar.waitNumOfProcessCounter(3);

    checkTestAppByPreviewButtonAndReturnToIde(currentWindow, "Enter your name:");
    commandsToolbar.clickExecStopBtn();
    commandsToolbar.clickWithHoldAndLaunchDebuCmdFromList(PROJECT_NAME + ": debug");
    consoles.waitExpectedTextIntoConsole("Listening for transport dt_socket at address: 8000", 60);
    consoles.waitExpectedTextIntoConsole(" Server startup in", 30);
  }

  private void checkTestAppByPreviewUrlAndReturnToIde(String currentWindow, String expectedText) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver ->
                    clickOnPreviewUrlAndCheckTextIsPresentInPageBody(currentWindow, expectedText));
  }

  private void checkTestAppByPreviewButtonAndReturnToIde(
      String currentWindow, String expectedText) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver ->
                    clickOnPreviewButtonAndCheckTextIsPresentInPageBody(
                        currentWindow, expectedText));
  }

  private boolean clickOnPreviewUrlAndCheckTextIsPresentInPageBody(
      String currentWindow, String expectedText) {
    consoles.clickOnPreviewUrl();
    return switchToOpenedWindowAndCheckTextIsPresent(currentWindow, expectedText);
  }

  private boolean clickOnPreviewButtonAndCheckTextIsPresentInPageBody(
      String currentWindow, String expectedText) {
    commandsToolbar.clickOnPreviewCommandBtnAndSelectUrl("dev-machine:tomcat8");
    return switchToOpenedWindowAndCheckTextIsPresent(currentWindow, expectedText);
  }

  private boolean switchToOpenedWindowAndCheckTextIsPresent(
      String currentWindow, String expectedText) {
    seleniumWebDriverHelper.switchToNextWindow(currentWindow);
    boolean result = getBodyText().contains(expectedText);
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);

    return result;
  }

  private void waitOnAvailablePreviewPage(String currentWindow, String expectedTextOnPreviewPage) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> isPreviewPageAvailable(currentWindow, expectedTextOnPreviewPage));
  }

  private Boolean isPreviewPageAvailable(String currentWindow, String expectedText) {
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

  private String getBodyText() {
    return new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until((ExpectedCondition<String>) driver -> getBody().getText());
  }
}
