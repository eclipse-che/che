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
package org.eclipse.che.selenium.intelligencecommand;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MULTIPLE;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsToolbar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckIntelligenceCommandFromToolbarTest {

  private static final String PROJECT_NAME = NameGenerator.generate("project", 2);

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private CommandsToolbar commandsToolbar;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private NotificationsPopupPanel notificationsPanel;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test(priority = 1)
  public void launchClonedWepAppTest() throws Exception {
    String currentWindow = seleniumWebDriver.getWindowHandle();
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectProjectAndCreate(Wizard.SamplesName.WEB_JAVA_SPRING, PROJECT_NAME);
    wizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    commandsToolbar.clickWithHoldAndLaunchCommandFromList(PROJECT_NAME + ": build and run");
    consoles.waitExpectedTextIntoConsole(" Server startup in");

    waitOnAvailablePreviewPage(currentWindow, "Enter your name");
    consoles.waitExpectedTextIntoConsole(" Server startup in");
    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitProjectExplorer();
    consoles.selectProcessByTabName(PROJECT_NAME + ": build and run");
    consoles.waitExpectedTextIntoConsole(" Server startup in");
    consoles.clickOnPreviewUrl();
    checkTestAppAndReturnToIde(currentWindow, "Enter your name:");
  }

  @Test(priority = 2)
  public void checkButtonsOnToolbar() {
    projectExplorer.waitProjectExplorer();
    String currentWindow = seleniumWebDriver.getWindowHandle();
    commandsToolbar.clickExecStopBtn();

    // it needs when the test is running on the ocp platform
    String previewUrl = consoles.getPreviewUrl();
    String expectedText =
        previewUrl.contains("route")
            ? "Application is not available"
            : "This site can’t be reached";

    consoles.clickOnPreviewUrl();
    checkTestAppAndReturnToIde(currentWindow, expectedText);
    commandsToolbar.clickExecRerunBtn();
    consoles.waitExpectedTextIntoConsole(" Server startup in");
    consoles.clickOnPreviewUrl();

    waitOnAvailablePreviewPage(currentWindow, "Enter your name:");
    Assert.assertTrue(commandsToolbar.getTimerValue().matches("\\d\\d:\\d\\d"));
    Assert.assertTrue(commandsToolbar.getNumOfProcessCounter().equals("#2"));

    commandsToolbar.clickOnPreviewCommandBtnAndSelectUrl("dev-machine:tomcat8");
    checkTestAppAndReturnToIde(currentWindow, "Enter your name:");
    commandsToolbar.clickExecStopBtn();
    commandsToolbar.clickWithHoldAndLaunchDebuCmdFromList(PROJECT_NAME + ": debug");
    consoles.waitExpectedTextIntoConsole("Listening for transport dt_socket at address: 8000", 60);
    consoles.waitExpectedTextIntoConsole(" Server startup in", 30);
  }

  private void checkTestAppAndReturnToIde(String currentWindow, String expectedTextOnTestAppPage) {
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);
    new WebDriverWait(seleniumWebDriver, MULTIPLE)
        .until(
            ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), expectedTextOnTestAppPage));
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);
  }

  private void waitOnAvailablePreviewPage(String currentWindow, String expectedTextOnPreviewPage) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> isPreviewPageAvailable(currentWindow, expectedTextOnPreviewPage));
  }

  private Boolean isPreviewPageAvailable(String currentWindow, String expectedText) {
    consoles.clickOnPreviewUrl();
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);

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
        .until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
  }

  private String getBodyText() {
    return new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until((ExpectedCondition<String>) driver -> getBody().getText());
  }
}
