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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.COMMON;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Musienko Maxim
 * @author Serhii Skoryk
 */
public class CheckRestoringWorkspaceAfterStoppingWsAgentProcess {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final String nameCommandForKillWsAgent = "killWsAgent";
  private static final String killPIDWSAgentCommand =
      "kill -9 $(ps ax | grep java | grep ws-agent | grep conf | grep -v grep | awk '{print $1}')";

  @Inject private TestWorkspace workspace;
  @Inject private TestUser defaultTestUser;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private MachineTerminal terminal;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);
    testCommandServiceClient.createCommand(
        killPIDWSAgentCommand, nameCommandForKillWsAgent, CUSTOM, workspace.getId());
    ide.open(workspace);
  }

  @Test()
  public void checkRestoreWsAgentByApi() throws Exception {
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);

    projectExplorer.invokeCommandWithContextMenu(COMMON, PROJECT_NAME, nameCommandForKillWsAgent);

    notificationsPopupPanel.waitWorkspaceAgentIsNotRunning();
    notificationsPopupPanel.clickOnRestartWorkspaceButton();
    testWorkspaceServiceClient.waitStatus(workspace.getName(), defaultTestUser.getName(), STOPPING);
    testWorkspaceServiceClient.waitStatus(workspace.getName(), defaultTestUser.getName(), RUNNING);
  }

  @Test(priority = 1)
  public void checkRestoreIdeItems() {
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);
  }
}
