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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.PULL;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestSshServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = TestGroup.GITHUB)
public class GitPullTest {
  private static final String PROJECT_NAME = NameGenerator.generate("FirstProject-", 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;
  @Inject private TestGitHubRepository testRepo;

  @Inject(optional = true)
  @Named("github.username")
  private String gitHubUsername;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private org.eclipse.che.selenium.pageobject.git.Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private TestSshServiceClient testSshServiceClient;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestGitHubServiceClient gitHubClientService;

  @BeforeClass
  public void prepare() throws Exception {
    String commitMess = String.format("new_content_was_added %s ", System.currentTimeMillis());
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    Path entryPath = Paths.get(getClass().getResource("/projects/git-pull-test").getPath());
    testRepo.addContent(entryPath, commitMess);

    ide.open(ws);
  }

  @AfterClass
  public void deleteRepo() throws IOException {
    testRepo.delete();
  }

  @Test
  public void pullTest() throws Exception {
    String jsFileName = "app.js";
    String htmlFileName = "file.html";
    String readmeTxtFileName = "readme-txt";
    String folderWithPlainFilesPath = "plain-files";

    String currentTimeInMillis = Long.toString(System.currentTimeMillis());
    projectExplorer.waitProjectExplorer();
    git.importJavaApp(testRepo.getHtmlUrl(), PROJECT_NAME, BLANK);

    prepareFilesForTest(jsFileName);
    prepareFilesForTest(htmlFileName);
    prepareFilesForTest(folderWithPlainFilesPath + "/" + readmeTxtFileName);

    changeContentOnGithubSide(jsFileName, currentTimeInMillis);
    changeContentOnGithubSide(htmlFileName, currentTimeInMillis);
    changeContentOnGithubSide(
        String.format("%s/%s", folderWithPlainFilesPath, readmeTxtFileName), currentTimeInMillis);

    performPull();

    git.waitGitStatusBarWithMess(
        String.format("Successfully pulled from %s", testRepo.getHtmlUrl()));

    checkPullAfterUpdatingContent(readmeTxtFileName, currentTimeInMillis);
    checkPullAfterUpdatingContent(htmlFileName, currentTimeInMillis);
    checkPullAfterUpdatingContent(readmeTxtFileName, currentTimeInMillis);

    testRepo.deleteFolder(Paths.get(folderWithPlainFilesPath), "remove file");

    performPull();
    checkPullAfterRemovingContent(
        readmeTxtFileName,
        String.format("/%s/%s/%s", PROJECT_NAME, folderWithPlainFilesPath, readmeTxtFileName));
    checkPullAfterRemovingContent(
        readmeTxtFileName,
        String.format("/%s/%s/%s", PROJECT_NAME, folderWithPlainFilesPath, "README.md"));
  }

  private void performPull() {
    menu.runCommand(GIT, REMOTES_TOP, PULL);
    git.waitPullFormToOpen();
    git.clickPull();
    git.waitPullFormToClose();
  }

  private void prepareFilesForTest(String fileName) {
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(String.format(PROJECT_NAME + "/%s", fileName));
    editor.waitActive();
  }

  private void checkPullAfterUpdatingContent(String tabNameOpenedFile, String expectedMessage) {
    editor.selectTabByName(tabNameOpenedFile);
    editor.waitTextIntoEditor(expectedMessage);
  }

  private void checkPullAfterRemovingContent(
      String tabNameOpenedFile, String pathToItemInProjectExplorer) {
    editor.waitTextNotPresentIntoEditor(tabNameOpenedFile);
    projectExplorer.waitLibrariesAreNotPresent(pathToItemInProjectExplorer);
  }

  private void changeContentOnGithubSide(String pathToContent, String content) throws IOException {
    testRepo
        .getFileContent(String.format("/%s", pathToContent))
        .update(content, "add " + NameGenerator.generate(content, 3));
  }
}
