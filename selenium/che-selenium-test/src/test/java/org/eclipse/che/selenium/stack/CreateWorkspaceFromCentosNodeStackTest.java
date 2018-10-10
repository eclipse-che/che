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
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.INSTALL_DEPENDENCIES_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.RUN_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_NODEJS;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
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
public class CreateWorkspaceFromCentosNodeStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String ANGULAR_PROJECT = "angular-patternfly-starter";
  private static final String NODE_JS_PROJECT = "nodejs-hello-world";
  private static final String WEB_NODE_JS_PROJECT = "web-nodejs-simple";

  private List<String> projects =
      ImmutableList.of(ANGULAR_PROJECT, NODE_JS_PROJECT, WEB_NODE_JS_PROJECT);

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromCentosNodeStack() {
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithProjects(
            CENTOS_NODEJS, WORKSPACE_NAME, projects);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(ANGULAR_PROJECT);
    projectExplorer.waitProjectInitialization(NODE_JS_PROJECT);
    projectExplorer.waitProjectInitialization(WEB_NODE_JS_PROJECT);
  }

  @Test(priority = 1)
  public void checkAngularPatternfyStarterProjectCommands() {
    By textOnPreviewPage = By.xpath("//span[text()='UNIFIED MANAGEMENT EXPERIENCE']");

    consoles.executeCommandFromProjectExplorer(
        ANGULAR_PROJECT,
        BUILD_GOAL,
        INSTALL_DEPENDENCIES_COMMAND_ITEM.getItem(ANGULAR_PROJECT),
        "bower_components/font-awesome");

    consoles.executeCommandFromProjectExplorer(
        ANGULAR_PROJECT, RUN_GOAL, RUN_COMMAND_ITEM.getItem(ANGULAR_PROJECT), "Waiting...");

    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    consoles.closeProcessTabWithAskDialog(RUN_COMMAND_ITEM.getItem(ANGULAR_PROJECT));
  }

  @Test(priority = 1)
  public void checkNodejsHelloWorldProjectCommands() {
    By textOnPreviewPage = By.xpath("//*[text()='Hello World!']");

    consoles.executeCommandFromProjectExplorer(
        NODE_JS_PROJECT,
        RUN_GOAL,
        RUN_COMMAND_ITEM.getItem(NODE_JS_PROJECT),
        "Example app listening on port 3000!");

    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    consoles.closeProcessTabWithAskDialog(RUN_COMMAND_ITEM.getItem(NODE_JS_PROJECT));
  }

  @Test(priority = 1)
  public void checkWebNodejsSimpleProjectCommands() {
    By textOnPreviewPage = By.xpath("//p[text()=' from the Yeoman team']");

    consoles.executeCommandFromProjectExplorer(
        WEB_NODE_JS_PROJECT,
        BUILD_GOAL,
        INSTALL_DEPENDENCIES_COMMAND_ITEM.getItem(WEB_NODE_JS_PROJECT),
        "bower_components/angular");

    consoles.executeCommandFromProjectExplorer(
        WEB_NODE_JS_PROJECT,
        RUN_GOAL,
        RUN_COMMAND_ITEM.getItem(WEB_NODE_JS_PROJECT),
        "Started connect web server");

    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    consoles.closeProcessTabWithAskDialog(RUN_COMMAND_ITEM.getItem(WEB_NODE_JS_PROJECT));
  }
}
