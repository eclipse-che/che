/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.RUN_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.RUN_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_GO;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
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
public class CreateWorkspaceFromCentosGoStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String DESKTOP_GO_PROJECT = "desktop-go-simple";
  private static final String WEB_GO_PROJECT = "web-go-simple";

  private List<String> projects = ImmutableList.of(DESKTOP_GO_PROJECT, WEB_GO_PROJECT);
  private By textOnPreviewPage = By.xpath("//pre[contains(text(),'Hello there')]");

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
  public void checkWorkspaceCreationFromCentosGoStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProjects(CENTOS_GO, WORKSPACE_NAME, projects);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(DESKTOP_GO_PROJECT);
    projectExplorer.waitProjectInitialization(WEB_GO_PROJECT);
  }

  @Test(priority = 1)
  public void checkDesktopGoSimpleProjectCommands() {
    consoles.executeCommandFromProjectExplorer(
        DESKTOP_GO_PROJECT,
        RUN_GOAL,
        RUN_COMMAND_ITEM.getItem(DESKTOP_GO_PROJECT),
        "Hello, world. Sqrt(2) = 1.4142135623730951");
  }

  @Test(priority = 1)
  public void checkWebGoSimpleProjectCommands() {
    consoles.executeCommandFromProjectExplorer(
        WEB_GO_PROJECT, RUN_GOAL, RUN_COMMAND, "listening on");

    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    consoles.closeProcessTabWithAskDialog(RUN_COMMAND);

    consoles.executeCommandFromProjectExplorer(
        WEB_GO_PROJECT, RUN_GOAL, RUN_COMMAND_ITEM.getItem(WEB_GO_PROJECT), "listening on");

    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    consoles.closeProcessTabWithAskDialog(RUN_COMMAND_ITEM.getItem(WEB_GO_PROJECT));
  }
}
