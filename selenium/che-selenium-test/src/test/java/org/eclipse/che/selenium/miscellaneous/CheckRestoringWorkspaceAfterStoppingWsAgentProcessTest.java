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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.COMMON_GOAL;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.executor.DockerCliCommandExecutor;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckRestoringWorkspaceAfterStoppingWsAgentProcessTest {
  private static final String PROJECT_NAME = NameGenerator.generate("WsAgentTest", 4);
  private static final int WAITING_NOTIFICATION_TIMEOUT_IN_SEC = 180;
  private static final String nameCommandForKillWsAgent = "killWsAgent";
  private static final String killPIDWSAgentCommand =
      "kill -9 $(ps ax | grep java | grep ws-agent | grep conf | grep -v grep | awk '{print $1}')";
  private static final String EXPECTED_POPUP_MESSAGE =
      "Workspace agent is not running, open editors are switched to read-only mode";
  private static final String TEXT_FOR_TYPING = "text for checking";
  private static final String FILE_NAME = "Aclass";
  private static final String PATH_TO_CHECKING_FILE =
      PROJECT_NAME + "/src/main/java/che.eclipse.sample";
  private static final String EXPECTED_EDITOR_TEXT =
      "/*\n"
          + " * Copyright (c) 2012-2018 Red Hat, Inc.\n"
          + " * All rights reserved. This program and the accompanying materials\n"
          + " * are made available under the terms of the Eclipse Public License v1.0\n"
          + " * which accompanies this distribution, and is available at\n"
          + " * http://www.eclipse.org/legal/epl-v10.html\n"
          + " *\n"
          + " * Contributors:\n"
          + " *   Red Hat, Inc. - initial API and implementation\n"
          + " */\n"
          + "package che.eclipse.sample;\n"
          + "\n"
          + "public class Aclass {\n"
          + "}\n";

  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Ide ide;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private DockerCliCommandExecutor dockerCliCommandExecutor;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private ToastLoader toastLoader;
  @Inject private TestCommandServiceClient testCommandServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    Path projectPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());
    testProjectServiceClient.importProject(
        testWorkspace.getId(), projectPath, PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    testCommandServiceClient.createCommand(
        killPIDWSAgentCommand, nameCommandForKillWsAgent, CUSTOM, testWorkspace.getId());

    ide.open(testWorkspace);
  }

  @Test
  public void editorShouldBeInReadonlyModeAfterUnexpectedWsAgentStopping() throws Exception {
    goToFileAndCheckText();

    // execute kill ws agent command
    projectExplorer.invokeCommandWithContextMenu(
        COMMON_GOAL, PROJECT_NAME, nameCommandForKillWsAgent);

    // check that ws agent has stopped and current editor tab is displayed
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed(
        EXPECTED_POPUP_MESSAGE, WAITING_NOTIFICATION_TIMEOUT_IN_SEC);
    toastLoader.waitToastLoaderIsOpen();
    toastLoader.waitExpectedTextInToastLoader("Workspace agent is not running");
    editor.waitTabIsPresent(FILE_NAME);
    editor.waitTextIntoEditor(EXPECTED_EDITOR_TEXT);

    // check that editor is in read only mode
    editor.waitActive();
    editor.typeTextIntoEditor(TEXT_FOR_TYPING);
    editor.waitTextIntoEditor(EXPECTED_EDITOR_TEXT);

    // check ws agent restoring
    toastLoader.clickOnToastLoaderButton("Restart");
    toastLoader.waitExpectedTextInToastLoader("Starting workspace runtime...");
    waitWorkspaceRunningStatus();
    goToFileAndCheckText();
    editor.waitActive();
    editor.typeTextIntoEditor(TEXT_FOR_TYPING);
    editor.waitTextIntoEditor(TEXT_FOR_TYPING);
  }

  private void waitWorkspaceRunningStatus() throws Exception {
    testWorkspaceServiceClient.waitStatus(
        testWorkspace.getName(), defaultTestUser.getName(), WorkspaceStatus.RUNNING);
  }

  private void goToFileAndCheckText() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PATH_TO_CHECKING_FILE, FILE_NAME + ".java");
    editor.waitActive();
    editor.waitTabIsPresent(FILE_NAME);
    editor.waitTextIntoEditor(EXPECTED_EDITOR_TEXT);
  }
}
