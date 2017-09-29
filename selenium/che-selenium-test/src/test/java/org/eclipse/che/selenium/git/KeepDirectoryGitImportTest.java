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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Random;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
//TODO Test is disabled. See issue - https://github.com/eclipse/che/issues/1853
public class KeepDirectoryGitImportTest {
  private static final Logger LOG = LoggerFactory.getLogger(KeepDirectoryGitImportTest.class);
  public static final String PROJECT_NAME_1 = "KeepDirectoryProject_1_" + new Random().nextInt(999);
  public static final String PROJECT_NAME_2 = "KeepDirectoryProject_2_" + new Random().nextInt(999);
  public static final String PROJECT_NAME_3 = "KeepDirectoryProject_3_" + new Random().nextInt(999);
  public static final String PROJECT_NAME_4 = "KeepDirectoryProject_4_" + new Random().nextInt(999);
  public static final String DIRECTORY_NAME_1 = "my-lib";
  public static final String DIRECTORY_NAME_2 = "my-lib/src/test";
  private static final String GO_INTO_ID = "gwt-debug-contextMenu/goInto";

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

  @BeforeClass
  public void prepare() throws Exception {
    ide.open(ws);

    // authorize application on GitHub
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();
    gitHubClientService.deleteAllGrants(gitHubPassword, gitHubPassword);
    preferences.regenerateAndUploadSshKeyOnGithub(gitHubUsername, gitHubPassword);
  }

  @Test(
    enabled = false,
    description = "Need to fix the issue https://github.com/eclipse/che/issues/1853"
  )
  public void keepDirectoryGitImport() throws Exception {
    // Check the 'keep directory' from SSH Git url
    projectExplorer.waitProjectExplorer();
    makeKeepDirectoryFromGitUrl(
        "git@github.com:" + gitHubUsername + "/java-multimodule.git",
        PROJECT_NAME_1,
        DIRECTORY_NAME_1);
    projectExplorer.waitItem(PROJECT_NAME_1);
    projectExplorer.selectVisibleItem(PROJECT_NAME_1);
    projectExplorer.openItemByPath(PROJECT_NAME_1);
    loader.waitOnClosed();
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME_1 + "/my-webapp");
    projectExplorer.waitItem(PROJECT_NAME_1 + "/my-lib");
    expandDirectoryMyLib(PROJECT_NAME_1);

    // Check the 'keep directory' for configured project
    loader.waitOnClosed();
    makeKeepDirectoryFromGitUrl(
        "git@github.com:" + gitHubUsername + "/java-multimodule2.git",
        PROJECT_NAME_2,
        DIRECTORY_NAME_1);
    projectExplorer.waitItem(PROJECT_NAME_2);
    projectExplorer.selectVisibleItem(PROJECT_NAME_2);
    projectExplorer.openItemByPath(PROJECT_NAME_2);
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME_2 + "/my-webapp");
    projectExplorer.waitItemInVisibleArea("my-lib");
    projectExplorer.waitItem(PROJECT_NAME_2 + "/my-lib");
    projectExplorer.openItemByPath(PROJECT_NAME_2 + "/my-lib");
    projectExplorer.waitItem(PROJECT_NAME_2 + "/my-lib/src");

    // Check the 'keep directory' from https git url
    makeKeepDirectoryFromGitUrl(
        "https://github.com/" + gitHubUsername + "/java-multimodule2.git",
        PROJECT_NAME_3,
        DIRECTORY_NAME_2);
    projectExplorer.waitItem(PROJECT_NAME_3);
    projectExplorer.selectVisibleItem(PROJECT_NAME_3);
    projectExplorer.openItemByPath(PROJECT_NAME_3);
    projectExplorer.waitItem(PROJECT_NAME_3 + "/my-lib");
    projectExplorer.openItemByPath(PROJECT_NAME_3 + "/my-lib");
    projectExplorer.openItemByPath(PROJECT_NAME_3 + "/my-lib/src");
    projectExplorer.openItemByPath(PROJECT_NAME_3 + "/my-lib/src/test");
    projectExplorer.openItemByPath(PROJECT_NAME_3 + "/my-lib/src/test/java");
    projectExplorer.openItemByPath(PROJECT_NAME_3 + "/my-lib/src/test/java/hello");
    projectExplorer.openItemByPath(
        PROJECT_NAME_3 + "/my-lib/src/test/java/hello/SayHelloTest.java");
    loader.waitOnClosed();
    editor.waitActiveEditor();
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME_3 + "/my-lib/src/main");
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME_3 + "/my-webapp");
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME_3 + "/my-lib/src/test");
    projectExplorer.clickOnItemInContextMenu(GO_INTO_ID);
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME_3 + "/src/my-lib");
    projectExplorer.waitItemInVisibleArea("test");
    projectExplorer.waitItemInVisibleArea("java");
    projectExplorer.waitItemInVisibleArea("hello");
    projectExplorer.waitItemInVisibleArea("SayHelloTest.java");

    // Check the 'keep directory' from GitHub
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
    importProject.typeProjectName(PROJECT_NAME_4);
    importProject.waitKeepDirectoryIsNotSelected();
    importProject.clickOnKeepDirectoryCheckbox();
    importProject.waitKeepDirectoryIsSelected();
    importProject.typeDirectoryName("my-webapp");
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME_4);
    projectExplorer.selectVisibleItem(PROJECT_NAME_4);
    projectExplorer.openItemByPath(PROJECT_NAME_4);
    loader.waitOnClosed();
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME_4 + "/my-lib");
    projectExplorer.waitItem(PROJECT_NAME_4 + "/my-webapp");
    projectExplorer.openItemByPath(PROJECT_NAME_4 + "/my-webapp");
    projectExplorer.openItemByPath(PROJECT_NAME_4 + "/my-webapp/src");
    loader.waitOnClosed();
    projectExplorer.openItemByPath(PROJECT_NAME_4 + "/my-webapp/src/main");
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME_4 + "/my-webapp/src/main/webapp");
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME_4 + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_INTO_ID);
    loader.waitOnClosed();
    projectExplorer.waitItemInVisibleArea("my-webapp");
    projectExplorer.waitItemIsDisappeared(PROJECT_NAME_4);
  }

  public void makeKeepDirectoryFromGitUrl(String url, String projectName, String folderName)
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

  public void expandDirectoryMyLib(String projectName) throws Exception {
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
    editor.waitActiveEditor();
    projectExplorer.openItemByPath(projectName + "/my-lib/src/test/java/hello/SayHelloTest.java");
    loader.waitOnClosed();
    editor.waitActiveEditor();
  }
}
