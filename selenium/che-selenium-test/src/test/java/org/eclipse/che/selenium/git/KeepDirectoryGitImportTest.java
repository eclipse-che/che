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
package org.eclipse.che.selenium.git;

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.GO_BACK;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.GO_INTO;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.WarningDialog;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class KeepDirectoryGitImportTest {
  public static final String PROJECT_NAME = NameGenerator.generate("KeepDirectoryProject", 4);
  public static final String DIRECTORY_NAME_1 = "my-lib";
  public static final String DIRECTORY_NAME_2 = "my-lib/src/test";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private Wizard projectWizard;
  @Inject private CodenvyEditor editor;
  @Inject private AskDialog askDialog;
  @Inject private WarningDialog warningDialog;
  @Inject private Preferences preferences;
  @Inject private TestGitHubServiceClient gitHubClientService;
  @Inject private TestProjectServiceClient projectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    ide.open(ws);

    // authorize application on GitHub
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();
    gitHubClientService.deleteAllGrants(gitHubUsername, gitHubPassword);
    preferences.regenerateAndUploadSshKeyOnGithub(gitHubUsername, gitHubPassword);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    projectServiceClient.deleteResource(ws.getId(), PROJECT_NAME);
  }

  @Test(priority = 1)
  public void keepDirectoryImportBySshUrlTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    makeKeepDirectoryFromGitUrl(
        "git@github.com:" + gitHubUsername + "/java-multimodule.git",
        PROJECT_NAME,
        DIRECTORY_NAME_1);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.selectVisibleItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME + "/my-webapp");
    projectExplorer.waitItem(PROJECT_NAME + "/my-lib");
    expandDirectoryMyLib(PROJECT_NAME);
  }

  @Test(priority = 2)
  public void keepDirectoryImportByHttpsUrlTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    makeKeepDirectoryFromGitUrl(
        "https://github.com/" + gitHubUsername + "/java-multimodule2.git",
        PROJECT_NAME,
        DIRECTORY_NAME_2);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.selectVisibleItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME + "/my-lib");
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-lib");
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-lib/src");
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-lib/src/test");
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-lib/src/test/java");
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-lib/src/test/java/hello");
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-lib/src/test/java/hello/SayHelloTest.java");
    loader.waitOnClosed();
    editor.waitActive();
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME + "/my-lib/src/main");
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME + "/my-webapp");
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/my-lib/src/test");
    projectExplorer.clickOnItemInContextMenu(GO_INTO);
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME + "/src/my-lib");
    projectExplorer.waitItemInVisibleArea("test");
    projectExplorer.waitItemInVisibleArea("java");
    projectExplorer.waitItemInVisibleArea("hello");
    projectExplorer.waitItemInVisibleArea("SayHelloTest.java");
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/my-lib/src/test");
    projectExplorer.clickOnItemInContextMenu(GO_BACK);
    projectExplorer.waitItem(PROJECT_NAME + "/my-lib/src");
  }

  @Test(priority = 3)
  public void keepDirectoryImportFromGitHub() throws Exception {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitHubSourceItem();
    loader.waitOnClosed();
    importProject.waitLoadRepoBtn();
    importProject.clickLoadRepoBtn();
    loader.waitOnClosed();
    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));
    importProject.selectProjectByName("java-multimodule");
    importProject.typeProjectName(PROJECT_NAME);
    importProject.waitKeepDirectoryIsNotSelected();
    importProject.clickOnKeepDirectoryCheckbox();
    importProject.waitKeepDirectoryIsSelected();
    importProject.typeDirectoryName("my-webapp");
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.selectVisibleItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME + "/my-lib");
    projectExplorer.waitItem(PROJECT_NAME + "/my-webapp");
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-webapp");
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-webapp/src");
    loader.waitOnClosed();
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-webapp/src/main");
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/my-webapp/src/main/webapp");
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_INTO);
    loader.waitOnClosed();
    projectExplorer.waitItemInVisibleArea("my-webapp");
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_BACK);
    projectExplorer.waitItem(PROJECT_NAME);
  }

  private void makeKeepDirectoryFromGitUrl(String url, String projectName, String folderName)
      throws Exception {
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitSourceItem();
    loader.waitOnClosed();
    importProject.typeURi(url);
    importProject.typeProjectName(projectName);
    importProject.waitKeepDirectoryIsNotSelected();
    importProject.clickOnKeepDirectoryCheckbox();
    importProject.waitKeepDirectoryIsSelected();
    importProject.typeDirectoryName(folderName);
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
  }

  private void expandDirectoryMyLib(String projectName) throws Exception {
    projectExplorer.openItemByPath(projectName + "/my-lib");
    projectExplorer.openItemByPath(projectName + "/my-lib/src");
    projectExplorer.openItemByPath(projectName + "/my-lib/src/main");
    projectExplorer.openItemByPath(projectName + "/my-lib/src/test");
    projectExplorer.openItemByPath(projectName + "/my-lib/src/main/java");
    projectExplorer.openItemByPath(projectName + "/my-lib/src/test/java");
    projectExplorer.openItemByPath(projectName + "/my-lib/src/main/java/hello");
    projectExplorer.openItemByPath(projectName + "/my-lib/src/test/java/hello");
    projectExplorer.openItemByPath(projectName + "/my-lib/src/main/java/hello/SayHello.java");
    loader.waitOnClosed();
    editor.waitActive();
    projectExplorer.openItemByPath(projectName + "/my-lib/src/test/java/hello/SayHelloTest.java");
    loader.waitOnClosed();
    editor.waitActive();
  }
}
