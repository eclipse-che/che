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
package org.eclipse.che.selenium.refactor.move;

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.BUILD_PATH;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.NEW;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuBuildPath.USE_AS_SOURCE_FOLDER;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.JAVA_SOURCE_FOLDER;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class MoveJavaFileInNewSourceFolderTest {

  private static final String PROJECT_NAME =
      "MoveJavaFileInNewSourceFolder" + new Random().nextInt(999);
  private static final String PATH_TO_FILE = PROJECT_NAME + "/src/com/company/nba/ATest.java";
  private static final String NEW_FOLDER_NAME = "test";
  private static final String NEW_SOURCE_FOLDER = "java";
  private static final String PATH_NEW_SOURCE_FOLDER = PROJECT_NAME + "/test/java";
  private static final String NEW_PACKAGE_NAME = "com.org.ltd";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Refactor refactor;
  @Inject private Menu menu;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private ToastLoader toastLoader;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/plain-java-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.PLAIN_JAVA);
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
  }

  @Test
  public void checkMoveJavaClassInNewSourceFolder() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_FILE);

    // create new folder and configure as source
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    createNewFolder(NEW_FOLDER_NAME);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/" + NEW_FOLDER_NAME);
    createNewFolder(NEW_SOURCE_FOLDER);
    projectExplorer.waitAndSelectItem(PATH_NEW_SOURCE_FOLDER);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/test/" + NEW_SOURCE_FOLDER);
    projectExplorer.clickOnItemInContextMenu(BUILD_PATH);
    projectExplorer.clickOnItemInContextMenu(USE_AS_SOURCE_FOLDER);
    projectExplorer.waitDefinedTypeOfFolder(PATH_NEW_SOURCE_FOLDER, JAVA_SOURCE_FOLDER);
    projectExplorer.waitAndSelectItem(PATH_NEW_SOURCE_FOLDER);
    projectExplorer.openContextMenuByPathSelectedItem(PATH_NEW_SOURCE_FOLDER);
    createPackage();

    // move java file into new source folder
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitAndSelectItem(PATH_TO_FILE);
    projectExplorer.launchRefactorMoveByKeyboard();
    refactor.waitMoveItemFormIsOpen();
    refactor.clickOnExpandIconTree(PROJECT_NAME);
    refactor.clickOnExpandIconTree("/test/java");
    loader.waitOnClosed();
    refactor.chooseDestinationForItem(NEW_PACKAGE_NAME);
    refactor.clickOkButtonRefactorForm();
    refactor.waitMoveItemFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/test/java/com/org/ltd/ATest.java");
  }

  private void createNewFolder(String folderName) {
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.FOLDER);

    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(folderName);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
  }

  private void createPackage() {
    loader.waitOnClosed();
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.clickOnNewContextMenuItem(
        TestProjectExplorerContextMenuConstants.SubMenuNew.JAVA_PACKAGE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(NEW_PACKAGE_NAME);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    projectExplorer.waitVisibilityByName(NEW_PACKAGE_NAME);
  }
}
