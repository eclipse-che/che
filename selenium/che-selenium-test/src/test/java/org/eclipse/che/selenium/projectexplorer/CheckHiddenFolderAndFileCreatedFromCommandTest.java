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
package org.eclipse.che.selenium.projectexplorer;

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.COMMON;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skorik Sergey */
public class CheckHiddenFolderAndFileCreatedFromCommandTest {
  private static final String PROJECT_NAME =
      CheckHiddenFolderAndFileCreatedFromCommandTest.class.getSimpleName();
  private static final String FILE_NAME = ".hidden";
  private static final String PATH_TO_FILE = PROJECT_NAME + "/" + FILE_NAME;
  private static final String FOLDER_NAME = ".resources";
  private static final String PATH_TO_FOLDER = PROJECT_NAME + "/" + FOLDER_NAME;
  private static final String COMMAND_CREATE_FOLDER_NAME = "createHiddenFolder";
  private static final String COMMAND_FOLDER = "cd " + PROJECT_NAME + " && mkdir " + FOLDER_NAME;
  private static final String COMMAND_CREATE_FILE_NAME = "createHiddenFile";
  private static final String COMMAND_FILE = "cd " + PROJECT_NAME + " && pwd | cat >> " + FILE_NAME;
  private static final String FILE_CONTENT = "/projects/" + PROJECT_NAME;

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    testCommandServiceClient.createCommand(
        COMMAND_FOLDER,
        COMMAND_CREATE_FOLDER_NAME,
        TestCommandsConstants.CUSTOM,
        testWorkspace.getId());

    testCommandServiceClient.createCommand(
        COMMAND_FILE,
        COMMAND_CREATE_FILE_NAME,
        TestCommandsConstants.CUSTOM,
        testWorkspace.getId());
    ide.open(testWorkspace);
  }

  /**
   * The 'checkHiddenFolderAndFile' test: 1. Open project and set 'SHOW_HIDE_HIDDEN_FILES' in the
   * 'Project' menu to show hidden files; 2. Start "createHiddenFolder" command which create a
   * ".res" folder and verify if it exists; 3. Start "createHiddenFile" command which create a
   * ".text" folder and verify if it exists and have text(result of 'pwd').
   */
  @Test
  public void checkHiddenFolderAndFile() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);

    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project
            .SHOW_HIDE_HIDDEN_FILES); // all hidden files and folders are visible
    projectExplorer.invokeCommandWithContextMenu(COMMON, PROJECT_NAME, COMMAND_CREATE_FOLDER_NAME);
    loader.waitOnClosed();
    projectExplorer.waitItem(PATH_TO_FOLDER);
    projectExplorer.openItemByPath(PATH_TO_FOLDER); // the created hidden folder is visible
    projectExplorer.invokeCommandWithContextMenu(COMMON, PROJECT_NAME, COMMAND_CREATE_FILE_NAME);
    loader.waitOnClosed();
    projectExplorer.waitItem(PATH_TO_FILE);
    projectExplorer.openItemByPath(PATH_TO_FILE); // the created hidden file is visible
    editor.waitActive();
    editor.waitTextIntoEditor(FILE_CONTENT);

    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.SHOW_HIDE_HIDDEN_FILES);
    projectExplorer.waitDisappearItemByPath(PATH_TO_FOLDER);
    projectExplorer.waitDisappearItemByPath(
        PATH_TO_FILE); // all hidden files and folders are hidden

    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.SHOW_HIDE_HIDDEN_FILES);
    projectExplorer.waitItem(PATH_TO_FOLDER);
    projectExplorer.waitItem(PATH_TO_FILE); // all hidden files and folders are visible again
  }
}
