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
package org.eclipse.che.selenium.projectexplorer;

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.NEW;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.FOLDER;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.ConfigureClasspath;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateProjectInSelectedFolderTest {

  private static final String PROJECT_NAME =
      CreateProjectInSelectedFolderTest.class.getSimpleName();
  private static final String PROJECT_NAME_WITH_ARTIFACT_ID = PROJECT_NAME + " [qa-spring-sample]";
  private static final String INNER_PROJECT_NAME = "blank-project";
  private static final String PATH_TO_FOLDER = PROJECT_NAME + "/blank-project";
  private static final String FOLDER_NAME = "blank-project";
  private static final String EXPECTED_TEXT = "You have created a blank project.";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer explorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private Wizard projectWizard;
  @Inject private ConfigureClasspath selectPath;
  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void createProjectInSelectedFolder() {
    explorer.waitProjectExplorer();
    explorer.openItemByPath(PROJECT_NAME);
    explorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
    explorer.clickOnItemInContextMenu(NEW);
    explorer.clickOnNewContextMenuItem(FOLDER);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(FOLDER_NAME);
    askForValueDialog.clickOkBtn();
    loader.waitOnClosed();
    explorer.waitItem(PATH_TO_FOLDER);

    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);

    projectWizard.selectSample(Wizard.TypeProject.BLANK);
    projectWizard.typeProjectNameOnWizard(INNER_PROJECT_NAME);

    projectWizard.clickOnSelectPathForParentBtn();
    selectPath.openItemInSelectPathForm("Workspace");
    selectPath.waitItemInSelectPathForm(PROJECT_NAME);
    selectPath.selectItemInSelectPathForm(PROJECT_NAME);
    selectPath.openItemInSelectPathForm(PROJECT_NAME);
    selectPath.waitItemInSelectPathForm(FOLDER_NAME);
    selectPath.selectItemInSelectPathForm(FOLDER_NAME);
    selectPath.clickSelectBtnSelectPathForm();

    projectWizard.clickCreateButton();
    loader.waitOnClosed();

    projectExplorer.quickExpandWithJavaScript();
    explorer.openItemByPath(
        PROJECT_NAME + "/" + FOLDER_NAME + "/" + INNER_PROJECT_NAME + "/README");
    editor.waitActive();
    editor.waitTextIntoEditor(EXPECTED_TEXT);
  }
}
