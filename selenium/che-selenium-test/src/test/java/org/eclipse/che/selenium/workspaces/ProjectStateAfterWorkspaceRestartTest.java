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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.STOP_WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 10.03.16 */
public class ProjectStateAfterWorkspaceRestartTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final String EXP_TEXT_NOT_PRESENT =
      "@Override\n" + "   public ModelAndView handleRequest";
  private boolean passedState = false;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Consoles consoles;
  @Inject private ToastLoader toastLoader;
  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource =
        ProjectStateAfterWorkspaceRestartTest.this
            .getClass()
            .getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkProjectAfterStopStartWs() {
    // create workspace from dashboard
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/index.jsp");
    editor.waitActive();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    loader.waitOnClosed();

    // stop and start workspace
    menu.runCommand(WORKSPACE, STOP_WORKSPACE);
    loader.waitOnClosed();
    toastLoader.waitToastLoaderIsOpen();
    toastLoader.waitExpectedTextInToastLoader("Workspace is not running");
    loader.waitOnClosed();
    consoles.closeProcessesArea();
    editor.waitTabIsNotPresent("AppController");
    editor.waitTabIsNotPresent("index.jsp");
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME);
    toastLoader.waitExpectedTextInToastLoader("Workspace is not running");
    toastLoader.clickOnStartButton();

    // check state of the project
    ide.waitOpenedWorkspaceIsReadyToUse();
    toastLoader.waitToastLoaderIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
    editor.waitTabIsPresent("AppController");
    editor.waitTabIsPresent("index.jsp");
    projectExplorer.waitItem(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/index.jsp");
    projectExplorer.openItemByPath(PROJECT_NAME + "/README.md");
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(EXP_TEXT_NOT_PRESENT);
    editor.waitTextIntoEditor("Developer Workspace");
  }
}
