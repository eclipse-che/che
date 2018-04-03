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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.PUSH;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
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
import org.eclipse.che.selenium.core.user.TestUser;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aleksandr Shmaraev
 * @author Igor Vinokur
 */
@Test(groups = TestGroup.GITHUB)
public class PushingChangesTest {
  private static final String PROJECT_NAME = NameGenerator.generate("PushingChangesTest-", 4);
  private static final String DEFAULT_COMMIT_SSH = "f99b08d23946ac4dc2749650e67875b4672e339c";
  private static final String COMMIT_MESSAGE = "edited and removed";
  private static final String REPO_NAME = "pushChangesTest";
  private static final String NEW_CONTENT_JSP = "<!-- JSP change -->";
  private static final String NEW_CONTENT_JAVA = "/* Java change */";
  private static final String PUSH_MSG = "Pushed to origin";
  private static final String PUSH_NOTHING = "Everything up-to-date";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

  @Inject(optional = true)
  @Named("github.username")
  private String gitHubUsername;

  @Inject(optional = true)
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private org.eclipse.che.selenium.pageobject.git.Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private NotificationsPopupPanel          notifications;
  @Inject private Wizard                           projectWizard;
  @Inject private ImportProjectFromLocation        importProject;
  @Inject private TestGitHubKeyUploader            testGitHubKeyUploader;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestGitHubServiceClient          gitHubClientService;
  @Inject private TestGitHubRepository             testRepo;
  @Inject private TestProjectServiceClient         testProjectServiceClient;
  @Inject private   TestCommandServiceClient         testCommandServiceClient;
  @BeforeClass
  public void prepare() throws Exception {


    testGitHubKeyUploader.updateGithubKey();
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    Path entryPath = Paths.get(getClass().getResource("/projects/git-pull-test").getPath());
    testRepo.addContent(entryPath);

    ide.open(ws);
    projectExplorer.waitProjectExplorer();
    git.importJavaApp(testRepo.getSshUrl(), PROJECT_NAME, BLANK);
    projectExplorer.waitItem(PROJECT_NAME);
  }

  @AfterClass
  public void deleteRepo() throws IOException {
    testRepo.delete();
  }

  // @Test
  public void pushChangesTest() throws Exception {
    String subFolderName = "plain-files";
    String newFileForPushing = "pushFile.txt";
    String nameOfHtmlFile = "file.html";
    String newContentForFirstPushing = String.valueOf(System.currentTimeMillis());
    String pathToHtmlFile = String.format("%s/%s", PROJECT_NAME, nameOfHtmlFile);

    changeAndCreateNewFileByProjectServiceClient(
        pathToHtmlFile, newFileForPushing, newContentForFirstPushing);
    git.createNewFileAndPushItToGitHub(PROJECT_NAME, "file.html");
    git.waitGitStatusBarWithMess(String.format("Successfully pushed to %s", testRepo.getSshUrl()));
    consoles.waitProcessInProcessConsoleTree("Git push", LOADER_TIMEOUT_SEC);
    events.clickEventLogBtn();
    loader.waitOnClosed();
    events.waitExpectedMessage(PUSH_MSG);
    assertEquals(testRepo.getFileContent(nameOfHtmlFile), newContentForFirstPushing);
    assertEquals(
        testRepo.getFileContent(subFolderName + "/" + newFileForPushing),
        newContentForFirstPushing);

    // Call Push again and check Everything up-to-date message
    menu.runCommand(GIT, REMOTES_TOP, PUSH);
    loader.waitOnClosed();
    git.waitPushFormToOpen();
    git.clickPush();
    git.waitPushFormToClose();
    notifications.waitExpectedMessageOnProgressPanelAndClosed(PUSH_NOTHING);
    git.waitGitStatusBarWithMess(PUSH_NOTHING);
    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);
    events.clearAllMessages();
  }

  @Test
  public void forcePushTest() {
    // Force push
    menu.runCommand(GIT, REMOTES_TOP, PUSH);
    loader.waitOnClosed();
    git.waitPushFormToOpen();
    git.selectForcePushCheckBox();
    git.clickPush();
    git.waitPushFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git push", LOADER_TIMEOUT_SEC);
    git.waitGitStatusBarWithMess("Successfully pushed");
    git.waitGitStatusBarWithMess("to git@github.com:" + gitHubUsername + "/pushChangesTest.git");
    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);
  }

  private void changeAndCreateNewFileByProjectServiceClient(
      String pathToItem, String newFileForPushing, String newContent) throws Exception {
    testProjectServiceClient.updateFile(ws.getId(), pathToItem, newContent);
    testProjectServiceClient.createFileInProject(
        ws.getId(), PROJECT_NAME + "/plain-files/", newFileForPushing, newContent);
  }
  private void prerpareHardResetCommand(){
    String bashCommand = String.format("cd /%s"
    testCommandServiceClient.createCommand();
  }
}
