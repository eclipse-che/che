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
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.RecentFiles;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckRecentFilesAndRevealResourceTest {
  private static final String FIRST_PROJECT_NAME = "checkRevealResource";
  private static final String SECOND_PROJECT_NAME = "checkRecentFiles";
  private static final String PATH_TO_FILE_FIRST_PROJECT =
      FIRST_PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String PATH_TO_FILE_SECOND_PROJECT =
      SECOND_PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String PATH_FOR_EXPAND_FIRST_PROJECT =
      FIRST_PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";
  private static final String PATH_FOR_EXPAND_SECOND_PROJECT =
      SECOND_PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private RecentFiles recentFiles;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        FIRST_PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        SECOND_PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void checkRevealResourceTest() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.quickRevealToItemWithJavaScript(PATH_FOR_EXPAND_FIRST_PROJECT);
    projectExplorer.openItemByPath(PATH_TO_FILE_FIRST_PROJECT);
    loader.waitOnClosed();
    editor.waitActive();
    editor.goToCursorPositionVisible(5, 1);
    projectExplorer.collapseProjectTreeByOptionsButton();
    projectExplorer.waitDisappearItemByPath(PATH_TO_FILE_FIRST_PROJECT);
    menu.runCommand(
        TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.REVEAL_RESOURCE);
    projectExplorer.waitItem(PATH_TO_FILE_FIRST_PROJECT);
  }

  @Test
  public void checkRecentFilesTest() {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    projectExplorer.quickRevealToItemWithJavaScript(PATH_FOR_EXPAND_SECOND_PROJECT);
    projectExplorer.openItemByPath(PATH_TO_FILE_SECOND_PROJECT);
    menu.runCommand(
        TestMenuCommandsConstants.Edit.EDIT,
        TestMenuCommandsConstants.Edit.RECENT,
        TestMenuCommandsConstants.Edit.Recent.CLEAR_LIST);
    loader.waitOnClosed();

    projectExplorer.openItemByPath(SECOND_PROJECT_NAME + "/pom.xml");
    projectExplorer.openItemByPath(SECOND_PROJECT_NAME + "/src/main/webapp");
    projectExplorer.openItemByPath(SECOND_PROJECT_NAME + "/src/main/webapp/index.jsp");
    editor.closeAllTabs();
    menu.runCommand(
        TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.OPEN_RECENT_FILE);

    recentFiles.waitRecentFiles();
    recentFiles.closeRecentFiles();

    menu.runCommand(
        TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.OPEN_RECENT_FILE);
    recentFiles.waitItemIsPresent("pom.xml");
    recentFiles.waitItemIsPresent("index.jsp");
    recentFiles.waitItemIsNotPresent("AppController.java");
    recentFiles.openItemByName("index.jsp");

    editor.waitTabIsPresent("index.jsp");
  }
}
