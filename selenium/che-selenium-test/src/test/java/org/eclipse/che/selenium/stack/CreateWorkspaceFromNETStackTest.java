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
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.RUN_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.UPDATE_DEPENDENCIES_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.RUN_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.UPDATE_DEPENDENCIES_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.DOT_NET;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromNETStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "dotnet-web-simple";

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromNETStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProject(
        DOT_NET, WORKSPACE_NAME, PROJECT_NAME);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(PROJECT_NAME);
  }

  @Test(priority = 1)
  public void checkDotnetWebSimpleProjectCommands() {
    By textOnPreviewPage = By.xpath("//pre[text()='Hello World!']");

    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, BUILD_GOAL, UPDATE_DEPENDENCIES_COMMAND, "Restore completed");
    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME,
        BUILD_GOAL,
        UPDATE_DEPENDENCIES_COMMAND_ITEM.getItem(PROJECT_NAME),
        "Restore completed");

    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, RUN_GOAL, RUN_COMMAND, "Application started.");
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);
    consoles.closeProcessTabWithAskDialog("run");

    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, RUN_GOAL, RUN_COMMAND_ITEM.getItem(PROJECT_NAME), "Application started.");
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);
  }
}
