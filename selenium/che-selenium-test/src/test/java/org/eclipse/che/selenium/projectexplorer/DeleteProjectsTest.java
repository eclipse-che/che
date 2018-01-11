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

import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.IdeMainDockPanel;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Anderey Chizhikov
 */
public class DeleteProjectsTest {

  private static final List<String> PROJECT_NAMES =
      Arrays.asList(
          "DeleteProjectTest1",
          "DeleteProjectTest2",
          "DeleteProjectTest3",
          "DeleteProjectTest4",
          "DeleteProjectTest5");

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private Loader loader;
  @Inject private IdeMainDockPanel ideMainDockPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    for (String projectName : PROJECT_NAMES) {
      URL resource = getClass().getResource("/projects/ProjectWithDifferentTypeOfFiles");
      testProjectServiceClient.importProject(
          workspace.getId(),
          Paths.get(resource.toURI()),
          projectName,
          ProjectTemplates.MAVEN_SPRING);
    }
    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    waitAllProjectsInProjectExplorer();
    loader.waitOnClosed();
    consoles.selectProcessByTabName("dev-machine");
  }

  @Test
  public void shouldDeleteProjectByContextMenu() {
    projectExplorer.waitItem(PROJECT_NAMES.get(0));
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAMES.get(0));
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.DELETE);
    acceptDeletion(PROJECT_NAMES.get(0));
    checkErrorMessageNotPresentInConsole();
  }

  @Test(priority = 1)
  public void shouldDeleteProjectByMenuFile() {
    projectExplorer.waitItem(PROJECT_NAMES.get(1));
    projectExplorer.selectItem(PROJECT_NAMES.get(1));
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    acceptDeletion(PROJECT_NAMES.get(1));
    checkErrorMessageNotPresentInConsole();
  }

  @Test(priority = 2)
  public void shouldDeleteOpenedProjectByMenuFile() {
    projectExplorer.waitItem(PROJECT_NAMES.get(3));
    projectExplorer.openItemByPath(PROJECT_NAMES.get(3));
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAMES.get(3));
    projectExplorer.selectItem(PROJECT_NAMES.get(3));
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    acceptDeletion(PROJECT_NAMES.get(3));
    projectExplorer.waitDisappearItemByPath(PROJECT_NAMES.get(3));
    checkErrorMessageNotPresentInConsole();
  }

  @Test(priority = 3)
  public void shouldDeleteOpenedProjectFromContextMenu() {
    projectExplorer.waitItem(PROJECT_NAMES.get(4));
    projectExplorer.openItemByPath(PROJECT_NAMES.get(4));
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAMES.get(4));
    projectExplorer.selectItem(PROJECT_NAMES.get(4));
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAMES.get(4));
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.DELETE);
    acceptDeletion(PROJECT_NAMES.get(4));
    projectExplorer.waitDisappearItemByPath(PROJECT_NAMES.get(4));
    checkErrorMessageNotPresentInConsole();
  }

  private void deleteFromDeleteIcon(String pathToProject) {
    loader.waitOnClosed();
    projectExplorer.selectItem(pathToProject);
    ideMainDockPanel.clickDeleteIcon();
    loader.waitOnClosed();
  }

  private void acceptDeletion(String projectName) {
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    loader.waitOnClosed();
    projectExplorer.waitDisappearItemByPath(projectName);
  }

  private void checkErrorMessageNotPresentInConsole() {
    try {
      consoles.waitExpectedTextIntoConsole("[ERROR]", 7);
      fail("Error message is present in console");
    } catch (TimeoutException ex) {
    }
  }

  private void waitAllProjectsInProjectExplorer() {
    PROJECT_NAMES.forEach((String projectName) -> projectExplorer.waitItem(projectName));
  }
}
