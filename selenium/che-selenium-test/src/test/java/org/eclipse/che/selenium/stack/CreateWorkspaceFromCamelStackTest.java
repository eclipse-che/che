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
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.BUILD_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CAMEL_SPRINGBOOT;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.languageserver.ApacheCamelFileEditingTest;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CreateWorkspaceFromCamelStackTest {

  private static final String WORKSPACE_NAME = generate("workspaceCamel", 4);
  private static final String PROJECT_NAME = "spring-boot-camel-xml";
  private List<String> projects = ImmutableList.of(PROJECT_NAME);
  private static final String CAMEL_FILE_NAME = "camel-context.xml";
  private static final String PATH_TO_CAMEL_FILE =
      PROJECT_NAME + "/src/main/resources/spring/" + CAMEL_FILE_NAME;
  private static final String LS_INIT_MESSAGE =
      "Initialized language server 'org.eclipse.che.plugin.camel.server.languageserver'";

  @Inject private Ide ide;
  @Inject private Dashboard dashboard;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;

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
  public void createWorkspaceFromCamelStackTest() throws Exception {
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithoutProject(
            CAMEL_SPRINGBOOT, WORKSPACE_NAME);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    checkLanguageServerInitialized();

    checkBuildCommandWorking();
  }

  private void checkLanguageServerInitialized() throws Exception {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_CAMEL_FILE);
    editor.waitTabIsPresent(CAMEL_FILE_NAME);

    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  private void checkBuildCommandWorking() {
    consoles.executeCommandFromProjectExplorer(
        PROJECT_NAME, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);
  }
}
