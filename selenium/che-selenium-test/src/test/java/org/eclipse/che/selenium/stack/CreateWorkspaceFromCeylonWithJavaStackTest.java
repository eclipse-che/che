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
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CEYLON_WITH_JAVA_JAVASCRIPT;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromCeylonWithJavaStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "ceylon-hello-world";
  private static final String MODULE_COMPILED_MESSAGE =
      "Note: Created module che.ceylon.samples.helloWorld";
  private static final String MODULE_STARTED_MESSAGE =
      "Hello World from Ceylon on the following backend : ";

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
  public void checkWorkspaceCreationFromCeylonWithJavaStack() {
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithProject(
            CEYLON_WITH_JAVA_JAVASCRIPT, WORKSPACE_NAME, PROJECT_NAME);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(PROJECT_NAME);
  }

  @Test(priority = 1)
  public void checkCeylonHelloWorldProjectCommands() {
    // compile and start project on JVM
    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, BUILD_GOAL, "compile for JVM", MODULE_COMPILED_MESSAGE);
    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, RUN_GOAL, "Run on JVM", MODULE_STARTED_MESSAGE + "jvm !");

    // compile and start project on NodeJS
    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, BUILD_GOAL, "compile for JS", MODULE_COMPILED_MESSAGE);
    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, RUN_GOAL, "Run on NodeJS", MODULE_STARTED_MESSAGE + "js !");

    // compile and start project on Dart
    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, BUILD_GOAL, "compile for Dart", MODULE_COMPILED_MESSAGE);
    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, RUN_GOAL, "Run on Dart", MODULE_STARTED_MESSAGE + "dartvm !");

    // clean all created modules
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitVisibleItem(PROJECT_NAME + "/modules");
    projectExplorer.invokeCommandWithContextMenu(BUILD_GOAL, PROJECT_NAME, "clean module");
    projectExplorer.waitItemIsNotPresentVisibleArea(PROJECT_NAME + "/modules");
  }
}
