/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestWorkspaceConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey chizhikov */
public class ProjectStateAfterRefreshTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 5);

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles consoles;
  @Inject private NotificationsPopupPanel notificationsPanel;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Menu menu;
  @Inject private ToastLoader toastLoader;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource =
        ProjectStateAfterRefreshTest.this.getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkRestoreStateOfProjectAfterRefreshTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPanel.waitProgressPopupPanelClose();

    projectExplorer.quickExpandWithJavaScript();
    consoles.closeProcessesArea();
    openFilesInEditor();
    checkFilesAreOpened();
    seleniumWebDriver.navigate().refresh();
    checkFilesAreOpened();
    editor.closeAllTabsByContextMenu();
  }

  @Test(priority = 1)
  public void checkRestoreStateAfterStoppingWorkspaceTest() throws Exception {
    // check state project without snapshot
    projectExplorer.waitProjectExplorer();
    projectExplorer.quickExpandWithJavaScript();
    openFilesInEditor();
    menu.runCommand(WORKSPACE, STOP_WORKSPACE);
    toastLoader.waitToastLoaderIsOpen();
    toastLoader.waitExpectedTextInToastLoader("Workspace is not running");
    toastLoader.clickOnStartButton();
    notificationsPanel.waitExpectedMessageOnProgressPanelAndClosed(
        TestWorkspaceConstants.RUNNING_WORKSPACE_MESS);
    checkFilesAreOpened();
    editor.closeAllTabsByContextMenu();
  }

  @Test(priority = 2)
  public void checkRestoreStateOfProjectIfPomXmlFileOpened() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp");
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    editor.waitActiveEditor();
    projectExplorer.waitProjectExplorer();
    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    editor.waitTabIsPresent("qa-spring-sample");
    projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp");
    editor.closeAllTabsByContextMenu();
  }

  private void checkFilesAreOpened() {
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/index.jsp");
    projectExplorer.waitItem(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitTabIsPresent("index.jsp");
    editor.waitTabIsPresent("AppController");
    editor.waitTabIsPresent("guess_num.jsp");
    editor.waitTabIsPresent("web.xml");
    editor.waitTabIsPresent("qa-spring-sample");
  }

  private void openFilesInEditor() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/index.jsp");
    editor.waitActiveEditor();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActiveEditor();
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    editor.waitActiveEditor();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp" + "/guess_num.jsp");
    editor.waitActiveEditor();
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/WEB-INF" + "/web.xml");
    editor.waitActiveEditor();
  }
}
