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
package org.eclipse.che.selenium.git;

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.GO_BACK;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.GO_INTO;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.GitHub;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.WarningDialog;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = TestGroup.GITHUB)
public class KeepDirectoryGitImportTest {
  public static final String PROJECT_NAME = NameGenerator.generate("KeepDirectoryProject", 4);
  public static final String DIRECTORY_NAME_1 = "my-lib";
  public static final String DIRECTORY_NAME_2 = "my-lib/src/test";
  private String currentWindow;

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
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestGitHubRepository testRepo;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private GitHub gitHub;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    Path entryPath = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    testRepo.addContent(entryPath);

    ide.open(ws);
    projectExplorer.waitProjectExplorer();
    currentWindow = seleniumWebDriver.getWindowHandle();
  }

  @AfterMethod
  public void tearDown() throws Exception {
    projectServiceClient.deleteResource(ws.getId(), PROJECT_NAME);
  }

  @Test(priority = 1)
  public void keepDirectoryImportBySshUrlTest() throws Exception {
    projectExplorer.waitProjectExplorer();

    makeKeepDirectoryFromGitUrl(testRepo.getHtmlUrl(), PROJECT_NAME, DIRECTORY_NAME_1);
    projectExplorer.waitItem(PROJECT_NAME);

    projectExplorer.waitAndSelectItemByName(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/my-webapp");
    projectExplorer.waitItem(PROJECT_NAME + "/my-lib");
    expandDirectoryMyLib(PROJECT_NAME);
  }

  @Test(priority = 2)
  public void keepDirectoryImportByHttpsUrlTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    makeKeepDirectoryFromGitUrl(testRepo.getHtmlUrl(), PROJECT_NAME, DIRECTORY_NAME_2);
    projectExplorer.waitItem(PROJECT_NAME);

    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PROJECT_NAME + "/my-lib/src/test/java/hello", "SayHelloTest.java");

    editor.waitActive();

    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/my-lib/src/main");
    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/my-webapp");
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/my-lib/src/test");
    projectExplorer.clickOnItemInContextMenu(GO_INTO);
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME + "/src/my-lib");
    projectExplorer.waitVisibilityByName("test");
    projectExplorer.waitVisibilityByName("java");
    projectExplorer.waitVisibilityByName("hello");
    projectExplorer.waitVisibilityByName("SayHelloTest.java");
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

    authorizeGitHubApplication();
    loader.waitOnClosed();

    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));

    importProject.selectProjectByName(testRepo.getName());
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
    projectExplorer.waitAndSelectItemByName(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/my-lib");

    projectExplorer.expandPathInProjectExplorer(PROJECT_NAME + "/my-webapp/src/main/webapp");

    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_INTO);
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName("my-webapp");
    projectExplorer.waitItemInvisibility(PROJECT_NAME);
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
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/my-lib/src/main/java/hello", "SayHello.java");
    editor.waitActive();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/my-lib/src/test/java/hello", "SayHelloTest.java");
    editor.waitActive();
  }

  private void authorizeGitHubApplication() {
    try {
      askDialog.waitFormToOpen(25);
    } catch (TimeoutException te) {
      // consider someone has already authorized before
      return;
    }

    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);

    gitHub.waitAuthorizationPageOpened();
    gitHub.typeLogin(gitHubUsername);
    gitHub.typePass(gitHubPassword);
    gitHub.clickOnSignInButton();

    // it is needed for specified case when the github authorize page is not appeared
    sleepQuietly(2);

    if (seleniumWebDriver.getWindowHandles().size() > 1) {
      gitHub.waitAuthorizeBtn();
      gitHub.clickOnAuthorizeBtn();
      seleniumWebDriver.switchTo().window(currentWindow);
    }

    seleniumWebDriver.switchTo().window(currentWindow);
  }
}
