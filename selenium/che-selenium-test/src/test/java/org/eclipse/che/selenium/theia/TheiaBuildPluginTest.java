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
package org.eclipse.che.selenium.theia;

import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CHE_7_PREVIEW;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.theia.TheiaEditor;
import org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaNewFileDialog;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm;
import org.eclipse.che.selenium.pageobject.theia.TheiaTerminal;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TheiaBuildPluginTest {
  private static final String WORKSPACE_NAME = NameGenerator.generate("wksp-", 5);
  private static final String PROJECT_NAME = "che-dummy-plugin";
  private static final String HOSTED_PLUGIN_PATH = PROJECT_NAME + "/ui";
  private static final String GIT_CLONE_COMMAND =
      "git clone https://github.com/ws-skeleton/che-dummy-plugin.git";
  private static final String EXPECTED_PLUGIN_FOLDER_MESSAGE =
      "Plugin folder is set to: file:///projects/che-dummy-plugin/ui";
  private static final String EXPECTED_STARTING_SERVER_MESSAGE =
      "Starting hosted instance server ...";
  private static final String EXPECTED_INSTANCE_RUNNING_MESSAGE = "Hosted instance is running at:";
  private static final String GO_TO_DIRECTORY_COMMAND = "cd che-dummy-plugin";
  private static final String BUILD_COMMAND = "./build.sh";
  private static final String WS_DEV_TERMINAL_TITLE = "ws/dev terminal 0";
  private static final String WS_THEIA_IDE_TERMINAL_TITLE = "ws/theia-ide terminal 1";
  private static final String HOSTED_SEARCH_SEQUENCE = ">hosted";
  private static final String HELLO_WORLD_SEARCH_SEQUENCE = ">Hello";
  private static final String HELLO_WORLD_PROPOSAL = "Hello World";
  private static final String EXPECTED_HELLO_WORLD_NOTIFICATION = "Hello World!";
  private static final String SUGGESTION_FOR_SELECTION = "Hosted Plugin: Start Instance";
  private static final String EXPECTED_DEVELOPMENT_HOST_TITLE = "Development Host";
  private static final String EXPECTED_CLONE_OUTPUT =
      "Unpacking objects: 100% (27/27), done.\n" + "sh-4.2$";
  private static final String EXPECTED_PLUGIN_OUTPUT = "Generated plugin: hello_world_plugin.theia";
  private static final String EXPECTED_TERMINAL_OUTPUT =
      "Packaging of plugin\n"
          + "\uD83D\uDD0D Validating...✔️\n"
          + "\uD83D\uDDC2  Getting dependencies...✔️\n"
          + "\uD83D\uDDC3  Resolving files...✔️\n"
          + "✂️  Excluding files...✔️\n"
          + "✍️  Generating Assembly...✔️\n"
          + "\uD83C\uDF89 Generated plugin: hello_world_plugin.theia\n";
  private static final String EXPECTED_TERMINAL_SUCCESS_OUTPUT =
      "Generating Che plug-in file...\n"
          + "hello_world_plugin.theia\n"
          + "./\n"
          + "./che-plugin.yaml\n"
          + "./che-dependency.yaml\n"
          + "Generated in assembly/che-service-plugin.tar.gz";

  @Inject private Dashboard dashboard;
  @Inject private NewWorkspace newWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private TheiaIde theiaIde;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TheiaTerminal theiaTerminal;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private TheiaEditor theiaEditor;
  @Inject private TheiaNewFileDialog theiaNewFileDialog;
  @Inject private TheiaHostedPluginSelectPathForm hostedPluginSelectPathForm;
  @Inject private TheiaProposalForm theiaProposalForm;

  @BeforeClass
  public void prepare() {
    dashboard.open();
    createWorkspaceHelper.createWorkspaceFromStackWithoutProject(CHE_7_PREVIEW, WORKSPACE_NAME);

    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test(groups = {OPENSHIFT})
  public void pluginShouldBeBuilt() {
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectsRootItem();

    openTerminal("File", "Open new multi-machine terminal", "ws/dev");
    theiaTerminal.waitTab(WS_DEV_TERMINAL_TITLE);
    theiaTerminal.clickOnTab(WS_DEV_TERMINAL_TITLE);
    theiaTerminal.performCommand(GIT_CLONE_COMMAND);
    theiaTerminal.waitTerminalOutput(EXPECTED_CLONE_OUTPUT, 0);

    openTerminal("File", "Open new multi-machine terminal", "ws/theia-ide");
    theiaTerminal.waitTab(WS_THEIA_IDE_TERMINAL_TITLE);
    theiaTerminal.clickOnTab(WS_THEIA_IDE_TERMINAL_TITLE);
    theiaTerminal.performCommand(GO_TO_DIRECTORY_COMMAND);
    theiaTerminal.waitTerminalOutput(GO_TO_DIRECTORY_COMMAND, 1);

    theiaTerminal.waitTab(WS_THEIA_IDE_TERMINAL_TITLE);
    theiaTerminal.clickOnTab(WS_THEIA_IDE_TERMINAL_TITLE);
    theiaTerminal.performCommand(BUILD_COMMAND);
    try {
      theiaTerminal.waitTerminalOutput(EXPECTED_TERMINAL_OUTPUT, 1);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      if (theiaTerminal.isTextPresentInTerminalOutput(EXPECTED_PLUGIN_OUTPUT, 1)) {
        fail("Known permanent failure https://github.com/eclipse/che/issues/11624", ex);
      }

      throw ex;
    }

    theiaTerminal.waitTerminalOutput(EXPECTED_TERMINAL_SUCCESS_OUTPUT, 1);
  }

  @Test(priority = 1)
  public void hostedModeShouldWork() {
    final String parentWindow = seleniumWebDriver.getWindowHandle();

    theiaProjectTree.waitItem(PROJECT_NAME);
    WaitUtils.sleepQuietly(5);

    theiaIde.pressKeyCombination(Keys.LEFT_CONTROL, Keys.LEFT_SHIFT, "p");
    theiaProposalForm.waitForm();
    WaitUtils.sleepQuietly(4);
    theiaProposalForm.enterTextToSearchField(HOSTED_SEARCH_SEQUENCE);
    WaitUtils.sleepQuietly(4);

    theiaProposalForm.clickOnProposal(SUGGESTION_FOR_SELECTION);

    hostedPluginSelectPathForm.waitForm();
    hostedPluginSelectPathForm.clickOnItem(PROJECT_NAME);
    hostedPluginSelectPathForm.clickOnItem(HOSTED_PLUGIN_PATH);
    hostedPluginSelectPathForm.waitItemSelected(HOSTED_PLUGIN_PATH);
    hostedPluginSelectPathForm.clickOnOpenButton();
    hostedPluginSelectPathForm.waitFormClosed();

    waitNotificationEqualsTo(EXPECTED_PLUGIN_FOLDER_MESSAGE, parentWindow);
    waitNotificationEqualsTo(EXPECTED_STARTING_SERVER_MESSAGE, parentWindow);
    waitNotificationContains(EXPECTED_INSTANCE_RUNNING_MESSAGE, parentWindow);

    switchToNonParentWindow(parentWindow);
    waitDevelopmentHostTitle();

    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitOpenWorkspaceButton();

    theiaIde.pressKeyCombination(Keys.LEFT_CONTROL, Keys.LEFT_SHIFT, "p");
    theiaProposalForm.waitSearchField();
    theiaProposalForm.enterTextToSearchField(HELLO_WORLD_SEARCH_SEQUENCE);
    theiaProposalForm.clickOnProposal(HELLO_WORLD_PROPOSAL);
    theiaIde.waitNotificationEqualsTo(EXPECTED_HELLO_WORLD_NOTIFICATION);
    theiaIde.waitNotificationDisappearance(EXPECTED_HELLO_WORLD_NOTIFICATION);

    switchToParentWindow(parentWindow);

    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(PROJECT_NAME);
    theiaProjectTree.expandPathAndOpenFile(
        PROJECT_NAME + "/ui/lib", "hello-world-backend-plugin.js");
  }

  private void waitNewBrowserWindowAndSwitchToParent(String parentWindowHandle) {
    seleniumWebDriver.waitOpenedSomeWin();
    switchToParentWindow(parentWindowHandle);
  }

  private void switchToNonParentWindow(String parentWindowHandle) {
    seleniumWebDriver.switchToNoneCurrentWindow(parentWindowHandle);
  }

  private void switchToParentWindow(String parentWindowHandle) {
    seleniumWebDriver.switchTo().window(parentWindowHandle);
    theiaIde.switchToIdeFrame();
  }

  private void waitTitleHaveNotRouteOccurrence() {
    final String textForChecking = "route";

    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> !seleniumWebDriver.getTitle().contains(textForChecking));
  }

  private void waitDevelopmentHostTitle() {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          String title = seleniumWebDriver.getTitle();

          if (EXPECTED_DEVELOPMENT_HOST_TITLE.equals(title)) {
            return true;
          }

          seleniumWebDriver.navigate().refresh();

          // give a time for refreshing
          WaitUtils.sleepQuietly(3);

          return false;
        },
        ELEMENT_TIMEOUT_SEC);
  }

  private void openTerminal(String topMenuCommand, String commandName, String proposalText) {
    theiaIde.runMenuCommand(topMenuCommand, commandName);

    theiaProposalForm.waitSearchField();
    theiaProposalForm.waitProposal(proposalText);
    theiaProposalForm.clickOnProposal(proposalText);
    theiaProposalForm.waitFormDisappearance();
  }

  private void waitNotificationEqualsTo(String notificationMessage, String parentWindowHandle) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          if (seleniumWebDriverHelper.isOpenedSomeWin()) {
            seleniumWebDriver.switchTo().window(parentWindowHandle);
            theiaIde.switchToIdeFrame();
          }

          return theiaIde.isDisplayedNotificationEqualsTo(notificationMessage);
        },
        LOADER_TIMEOUT_SEC);
  }

  private void waitNotificationContains(String notificationMessage, String parentWindowHandle) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          if (seleniumWebDriverHelper.isOpenedSomeWin()) {
            seleniumWebDriver.switchTo().window(parentWindowHandle);
            theiaIde.switchToIdeFrame();
          }

          return theiaIde.isDisplayedNotificationContains(notificationMessage);
        },
        LOADER_TIMEOUT_SEC);
  }
}
