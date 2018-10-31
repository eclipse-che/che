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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CHE_7_PREVIEW;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.theia.TheiaEditor;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaNewFileDialog;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm;
import org.eclipse.che.selenium.pageobject.theia.TheiaTerminal;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TheiaBuildPluginTest {
  private static final String WORKSPACE_NAME = NameGenerator.generate("wksp-", 5);
  private static final String GIT_CLONE_COMMAND =
      "git clone https://github.com/ws-skeleton/che-dummy-plugin.git";
  private static final String GO_TO_DIRECTORY_COMMAND = "cd che-dummy-plugin";
  private static final String BUILD_COMMAND = "./build.sh";
  private static final String WS_DEV_TERMINAL_TITLE = "ws/dev terminal 0";
  private static final String WS_THEIA_IDE_TERMINAL_TITLE = "ws/theia-ide terminal 1";
  private static final String EXPECTED_CLONE_OUTPUT =
      "Unpacking objects: 100% (27/27), done.\n" + "sh-4.2$";
  private static final String EXPECTED_PLUGIN_OUTPUT = "hello_world_plugin.theia";
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
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TheiaTerminal theiaTerminal;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private TheiaEditor theiaEditor;
  @Inject private TheiaNewFileDialog theiaNewFileDialog;
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

  @Test(groups = OPENSHIFT)
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

  private void openTerminal(String topMenuCommand, String commandName, String proposalText) {
    theiaIde.runMenuCommand(topMenuCommand, commandName);

    theiaProposalForm.waitSearchField();
    theiaProposalForm.waitProposal(proposalText);
    theiaProposalForm.clickOnProposal(proposalText);
    theiaProposalForm.waitFormDisappearance();
  }
}
