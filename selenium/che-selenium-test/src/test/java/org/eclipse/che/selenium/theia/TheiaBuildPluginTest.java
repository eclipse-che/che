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
import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
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

@Test(groups = {OPENSHIFT, UNDER_REPAIR})
public class TheiaBuildPluginTest {
  private static final String WORKSPACE_NAME = NameGenerator.generate("wksp-", 5);
  private static final String EXPECTED_DEVELOPMENT_HOST_TITLE = "Development Host";

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
    createWorkspaceHelper.createWorkspaceFromStackWithoutProject(Stack.GO, WORKSPACE_NAME);

    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void pluginShouldBeBuilt() {
    final String pluginNameSearchSequence = "hello-world";
    final String yeomanWizardSearchSequence = ">yeom";
    final String helloWorldPluginProposal = "Hello World plug-in";
    final String goToDirectoryCommand = "cd hello-world";
    final String wsTheiaIdeTerminalTitle = "ws/theia-ide terminal 0";
    final String expectedYaomanMessage = "Yeoman generator successfully ended";
    final String expectedTerminalSuccessOutput =
        "Packaging of plugin\n"
            + "\uD83D\uDD0D Validating...✔️\n"
            + "\uD83D\uDDC2  Getting dependencies...✔️\n"
            + "\uD83D\uDDC3  Resolving files...✔️\n"
            + "✂️  Excluding files...✔️\n"
            + "✍️  Generating Assembly...✔️\n"
            + "\uD83C\uDF89 Generated plugin: hello_world.theia";
    final String backendPluginDescription = "Backend plug-in, it will run on the server side.";

    // prepare project tree
    theiaProjectTree.waitFilesTab();
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectsRootItem();
    theiaIde.waitNotificationDisappearance(
        "Che Workspace: Finished cloning projects.", UPDATING_PROJECT_TIMEOUT_SEC);

    // create project by "Yeoman Wizard"
    theiaIde.pressKeyCombination(Keys.LEFT_CONTROL, Keys.LEFT_SHIFT, "p");
    theiaProposalForm.waitForm();
    theiaProposalForm.enterTextToSearchField(yeomanWizardSearchSequence);
    theiaProposalForm.waitProposal("Yeoman Wizard");
    theiaProposalForm.clickOnProposal("Yeoman Wizard");

    try {
      theiaProposalForm.waitForm();
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/12315");
    }

    theiaProposalForm.enterTextToSearchField(pluginNameSearchSequence);
    seleniumWebDriverHelper.pressEnter();
    theiaProposalForm.clickOnProposal(backendPluginDescription);
    theiaProposalForm.clickOnProposal(helloWorldPluginProposal);
    theiaIde.waitNotificationMessageContains(expectedYaomanMessage, UPDATING_PROJECT_TIMEOUT_SEC);
    theiaIde.waitNotificationDisappearance(expectedYaomanMessage, UPDATING_PROJECT_TIMEOUT_SEC);

    // build plugin
    openTerminalByProposal("ws/theia-ide");
    theiaTerminal.waitTab(wsTheiaIdeTerminalTitle);
    theiaTerminal.clickOnTab(wsTheiaIdeTerminalTitle);
    theiaTerminal.performCommand(goToDirectoryCommand);
    theiaTerminal.waitTerminalOutput(goToDirectoryCommand, 0);
    theiaTerminal.clickOnTab(wsTheiaIdeTerminalTitle);
    theiaTerminal.waitTabSelected(wsTheiaIdeTerminalTitle);
    theiaTerminal.performCommand("yarn");
    try {
      theiaTerminal.waitTerminalOutput(expectedTerminalSuccessOutput, 0);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/11624", ex);
    }
  }

  @Test(priority = 1)
  public void hostedModeShouldWork() {
    final String projectName = "hello-world";
    final String hostedSearchSequence = ">hosted";
    final String editedSourceFile = "hello-world-backend.ts";
    final String expectedHelloWorldNotification = "Hello World!";
    final String expectedAlohaWorldNotification = "Aloha World!";
    final String suggestionForSelection = "Hosted Plugin: Start Instance";
    final String helloWorldProposal = "Hello World";
    final String helloWorldSearchSequence = ">Hello";
    final String expectedInstanceRunningMessage = "Hosted instance is running at:";
    final String parentWindow = seleniumWebDriver.getWindowHandle();
    final String expectedPluginFolderMessage =
        "Plugin folder is set to: file:///projects/hello-world";
    final String expectedStartingServerMessage = "Starting hosted instance server ...";
    final String editedSourceLine = "theia.window.showInformationMessage('Aloha World!');";
    final String expectedTextAfterDeleting =
        "context.subscriptions.push(theia.commands.registerCommand(informationMessageTestCommand, (...args: any[]) => {\n"
            + " \n"
            + "    }));";

    // run hosted mode
    theiaProjectTree.waitItem(projectName);
    theiaProjectTree.clickOnItem(projectName);
    theiaProjectTree.waitItemSelected(projectName);
    theiaIde.pressKeyCombination(Keys.LEFT_CONTROL, Keys.LEFT_SHIFT, "p");
    theiaProposalForm.waitForm();
    theiaProposalForm.enterTextToSearchField(hostedSearchSequence);
    theiaProposalForm.clickOnProposal(suggestionForSelection);
    hostedPluginSelectPathForm.waitForm();
    hostedPluginSelectPathForm.clickOnProjectItem(projectName);
    hostedPluginSelectPathForm.waitProjectItemSelected(projectName);
    hostedPluginSelectPathForm.clickOnOpenButton();
    hostedPluginSelectPathForm.waitFormClosed();
    waitNotificationEqualsTo(expectedPluginFolderMessage, parentWindow);
    waitNotificationEqualsTo(expectedStartingServerMessage, parentWindow);
    waitNotificationContains(expectedInstanceRunningMessage, parentWindow);

    // check hosted mode availability
    switchToNonParentWindow(parentWindow);
    waitHostedPageReady();
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(projectName);

    // check plugin output
    theiaIde.pressKeyCombination(Keys.LEFT_CONTROL, Keys.LEFT_SHIFT, "p");
    theiaProposalForm.waitSearchField();
    theiaProposalForm.enterTextToSearchField(helloWorldSearchSequence);
    theiaProposalForm.clickOnProposal(helloWorldProposal);
    theiaIde.waitNotificationEqualsTo(expectedHelloWorldNotification);
    theiaIde.waitNotificationDisappearance(expectedHelloWorldNotification);

    // check editing of the source code
    switchToParentWindow(parentWindow);
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(projectName);
    theiaProjectTree.expandPathAndOpenFile(projectName + "/src", editedSourceFile);
    theiaEditor.waitEditorTab(editedSourceFile);
    theiaEditor.waitTabSelecting(editedSourceFile);
    theiaEditor.waitActiveEditor();
    theiaEditor.selectLineText(14);
    seleniumWebDriverHelper.pressDelete();
    theiaEditor.waitEditorText(expectedTextAfterDeleting);
    theiaEditor.enterTextByTypingEachChar(editedSourceLine);
    theiaEditor.waitEditorText(editedSourceLine);
    theiaEditor.waitTabSavedStatus(editedSourceFile);

    // check applying of the changes in hosted mode window
    switchToNonParentWindow(parentWindow);
    waitHostedPageReady();
    seleniumWebDriver.navigate().refresh();
    waitHostedPageReady();
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(projectName);
    theiaIde.pressKeyCombination(Keys.LEFT_CONTROL, Keys.LEFT_SHIFT, "p");
    theiaProposalForm.waitSearchField();
    theiaProposalForm.enterTextToSearchField(helloWorldSearchSequence);
    theiaProposalForm.clickOnProposal(helloWorldProposal);
    theiaIde.waitNotificationEqualsTo(expectedAlohaWorldNotification);
    theiaIde.waitNotificationDisappearance(expectedAlohaWorldNotification);
  }

  private void switchToNonParentWindow(String parentWindowHandle) {
    seleniumWebDriver.switchToNoneCurrentWindow(parentWindowHandle);
  }

  private void switchToParentWindow(String parentWindowHandle) {
    seleniumWebDriver.switchTo().window(parentWindowHandle);
    theiaIde.switchToIdeFrame();
  }

  private void waitHostedPageReady() {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          try {
            theiaIde.waitTheiaIdeTopPanel();
            theiaProjectTree.waitFilesTab();
          } catch (TimeoutException ex) {
            // page should be refreshed for checking of the deploying
            seleniumWebDriver.navigate().refresh();
            return false;
          }

          return true;
        },
        LOADER_TIMEOUT_SEC);
  }

  private void openTerminal(String topMenuCommand, String commandName, String proposalText) {
    theiaIde.runMenuCommand(topMenuCommand, commandName);

    theiaProposalForm.waitSearchField();
    theiaProposalForm.waitProposal(proposalText);
    theiaProposalForm.clickOnProposal(proposalText);
    theiaProposalForm.waitFormDisappearance();
  }

  private void openTerminalByProposal(String proposalText) {
    theiaIde.pressKeyCombination(Keys.LEFT_CONTROL, "`");
    theiaProposalForm.waitProposal(proposalText);
    theiaProposalForm.clickOnProposal(proposalText);
    theiaProposalForm.waitFormDisappearance();
  }

  private void waitNotificationEqualsTo(String notificationMessage, String parentWindowHandle) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          if (seleniumWebDriverHelper.isTwoOrMoreWindowsOpened()) {
            driver.switchTo().window(parentWindowHandle);
            theiaIde.switchToIdeFrame();
          }

          return theiaIde.isNotificationEqualsTo(notificationMessage);
        },
        LOADER_TIMEOUT_SEC);
  }

  private void waitNotificationContains(String notificationMessage, String parentWindowHandle) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          if (seleniumWebDriverHelper.isTwoOrMoreWindowsOpened()) {
            driver.switchTo().window(parentWindowHandle);
            theiaIde.switchToIdeFrame();
          }

          return theiaIde.isNotificationContains(notificationMessage);
        },
        LOADER_TIMEOUT_SEC);
  }
}
