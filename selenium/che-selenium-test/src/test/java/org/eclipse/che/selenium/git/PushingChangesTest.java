/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.git;

import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aleksandr Shmaraev
 * @author Igor Vinokur
 */
@Test(groups = TestGroup.GITHUB)
public class PushingChangesTest {
  private static final String PROJECT_NAME = NameGenerator.generate("PushingChangesTest-", 4);
  private static final String NAME_OF_HARD_RESET_COMMAND = "hardReset";
  private static final String PROJECT_FOLDER_NAME = "plain-files";
  private static final String PUSH_MSG = "Pushed to origin";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private DefaultTestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private org.eclipse.che.selenium.pageobject.git.Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private Wizard projectWizard;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private TestGitHubKeyUploader testGitHubKeyUploader;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestGitHubServiceClient gitHubClientService;
  @Inject private TestGitHubRepository testRepo;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private CommandsPalette commandsPalette;

  @BeforeClass
  public void setUp() throws Exception {
    testGitHubKeyUploader.updateGithubKey();
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    Path entryPath = Paths.get(getClass().getResource("/projects/git-pull-test").getPath());
    testRepo.addContent(entryPath);
    prepareAmendCommitCommandByRestApi();
    ide.open(ws);
    projectExplorer.waitProjectExplorer();
    git.importJavaApp(testRepo.getSshUrl(), PROJECT_NAME, BLANK);
    projectExplorer.waitItem(PROJECT_NAME);
  }

  @Test
  public void pushChangesTest() throws Exception {
    String newFileForPushing = "pushFile.txt";
    String nameOfHtmlFile = "file.html";
    String newContentForFirstPushing = String.valueOf(System.currentTimeMillis());
    String pathToHtmlFile = String.format("%s/%s", PROJECT_NAME, nameOfHtmlFile);

    doChangesInTheProjectFileByProjectServiceClient(
        pathToHtmlFile, newFileForPushing, newContentForFirstPushing);
    git.createNewFileAndPushItToGitHub(PROJECT_NAME, "file.html");
    git.waitGitStatusBarWithMess(String.format("Successfully pushed to %s", testRepo.getSshUrl()));
    consoles.waitProcessInProcessConsoleTree("Git push", LOADER_TIMEOUT_SEC);
    events.clickEventLogBtn();
    loader.waitOnClosed();
    events.waitExpectedMessage(PUSH_MSG);
    assertEquals(testRepo.getFileContent(nameOfHtmlFile), newContentForFirstPushing);
    assertEquals(
        testRepo.getFileContent(PROJECT_FOLDER_NAME + "/" + newFileForPushing),
        newContentForFirstPushing);
  }

  @Test
  public void forcePushTest() throws Exception {
    String expectedMessageAfterGitConflict =
        "failed to push 'master -> master' to '%s'. Try to merge remote changes using pull, and then push again.";
    String contentForCheckingForcePushing = "check force pushing";
    String pathToFileWitChanging =
        String.format("%s/%s/%s", PROJECT_NAME, PROJECT_FOLDER_NAME, "README.md");

    // do conflict with changing file and amend commit
    testProjectServiceClient.updateFile(
        ws.getId(), pathToFileWitChanging, contentForCheckingForcePushing);
    launchAmendCommitCommand();
    git.pushChanges(false);
    git.waitGitStatusBarWithMess(
        String.format(expectedMessageAfterGitConflict, testRepo.getSshUrl()));

    // Make force push and check changes on gitHub side
    git.pushChanges(true);
    git.waitGitStatusBarWithMess(String.format("Successfully pushed to %s", testRepo.getSshUrl()));
    assertEquals(
        testRepo.getFileContent(PROJECT_FOLDER_NAME + "/README.md"),
        contentForCheckingForcePushing);
  }

  private void doChangesInTheProjectFileByProjectServiceClient(
      String pathToItem, String newFileForPushing, String newContent) throws Exception {
    testProjectServiceClient.updateFile(ws.getId(), pathToItem, newContent);
    testProjectServiceClient.createFileInProject(
        ws.getId(), PROJECT_NAME + "/plain-files/", newFileForPushing, newContent);
  }

  private void prepareAmendCommitCommandByRestApi() throws Exception {
    String bashCommand =
        String.format("cd /projects/%s && git commit --all --no-edit --amend", PROJECT_NAME);
    testCommandServiceClient.createCommand(
        bashCommand, NAME_OF_HARD_RESET_COMMAND, CUSTOM, ws.getId());
  }

  private void launchAmendCommitCommand() {
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(NAME_OF_HARD_RESET_COMMAND);
  }
}
