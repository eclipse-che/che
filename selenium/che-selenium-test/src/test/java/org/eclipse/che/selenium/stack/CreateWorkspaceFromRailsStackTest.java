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
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.INSTALL_DEPENDENCIES_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.RUN_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.INSTALL_DEPENDENCIES_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.RUN_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.RAILS;

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
public class CreateWorkspaceFromRailsStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String CONSOLE_RUBY_PROJECT = "console-ruby-simple";
  private static final String WEB_RAILS_PROJECT = "web-rails-simple";

  private List<String> projects = ImmutableList.of(CONSOLE_RUBY_PROJECT, WEB_RAILS_PROJECT);

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
  public void checkWorkspaceCreationFromRailsStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProjects(RAILS, WORKSPACE_NAME, projects);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(CONSOLE_RUBY_PROJECT);
    projectExplorer.waitProjectInitialization(WEB_RAILS_PROJECT);
  }

  @Test(priority = 1)
  public void checkConsoleRubySimpleProjectCommands() {
    consoles.executeCommandFromProjectExplorer(
        CONSOLE_RUBY_PROJECT,
        RUN_GOAL,
        RUN_COMMAND_ITEM.getItem(CONSOLE_RUBY_PROJECT),
        "Hello world!");
  }

  @Test(priority = 1)
  public void checkWebRailsSimpleProjectCommands() {
    By textOnPreviewPage = By.xpath("//h1[text()='Yay! You’re on Rails!']");

    consoles.executeCommandFromProjectExplorer(
        WEB_RAILS_PROJECT, BUILD_GOAL, INSTALL_DEPENDENCIES_COMMAND, "Bundle complete!");
    consoles.executeCommandFromProjectExplorer(
        WEB_RAILS_PROJECT,
        BUILD_GOAL,
        INSTALL_DEPENDENCIES_COMMAND_ITEM.getItem(WEB_RAILS_PROJECT),
        "Bundle complete!");

    consoles.executeCommandFromProjectExplorer(
        WEB_RAILS_PROJECT, RUN_GOAL, RUN_COMMAND, "* Listening on");
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);
    consoles.closeProcessTabWithAskDialog(RUN_COMMAND);

    consoles.executeCommandFromProjectExplorer(
        WEB_RAILS_PROJECT, RUN_GOAL, RUN_COMMAND_ITEM.getItem(WEB_RAILS_PROJECT), "* Listening on");
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);
  }
}
