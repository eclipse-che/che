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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.REVERT_COMMIT;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.git.GitRevertCommit;
import org.eclipse.che.selenium.pageobject.git.GitStatusBar;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author aleksandr shmaraev */
public class RevertCommitTest {
  private static final String REPO_NAME = NameGenerator.generate("GitRevert-", 3);
  private static final String PROJECT_NAME = NameGenerator.generate("GitRevertProject-", 4);
  private static final String COMMIT_MSG = "update file.html";

  private GitHub gitHub;
  private GHRepository gitHubRepository;

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private CodenvyEditor editor;
  @Inject private GitRevertCommit gitRevertCommit;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestGitHubServiceClient gitHubClientService;
  @Inject private GitStatusBar gitStatusBar;

  @BeforeClass
  public void prepare() throws Exception {
    gitHub = GitHub.connectUsingPassword(gitHubUsername, gitHubPassword);
    gitHubRepository = gitHub.createRepository(REPO_NAME).create();
    String commitMess = String.format("add-new-content %s ", System.currentTimeMillis());
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    Path entryPath = Paths.get(getClass().getResource("/projects/git-pull-test").getPath());
    gitHubClientService.addContentToRepository(entryPath, commitMess, gitHubRepository);
    ide.open(ws);
  }

  @AfterClass
  public void deleteRepo() throws IOException {
    gitHubRepository.delete();
  }

  @Test
  public void shouldRevertCommit() throws Exception {
    // preconditions and import the test repo
    String jsFile = "app.js";
    String htmlFile = "file.html";
    String changeContent = "<! change content>";
    String pathToJsFile = String.format("%s/%s", PROJECT_NAME, jsFile);
    String pathToHtmlFile = String.format("%s/%s", PROJECT_NAME, htmlFile);

    projectExplorer.waitProjectExplorer();
    String repoUrl = String.format("https://github.com/%s/%s.git", gitHubUsername, REPO_NAME);
    git.importJavaApp(repoUrl, PROJECT_NAME, BLANK);

    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitItem(pathToJsFile);

    // perform git revert and check author and comment
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    performGitRevert();

    // check that 'app.js' is disappear from the project tree
    projectExplorer.waitDisappearItemByPath(pathToJsFile);

    // update the 'file.html' and commit change
    testProjectServiceClient.updateFile(ws.getId(), pathToHtmlFile, changeContent);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    commitFiles();

    // perform revert and check that 'change content' is not present in the editor
    performGitRevert();

    projectExplorer.openItemByPath(pathToHtmlFile);
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(changeContent);
  }

  private void performGitRevert() {
    menu.runCommand(GIT, REVERT_COMMIT);

    String revision = gitRevertCommit.getTopCommitRevision();
    String comment = gitRevertCommit.getTopCommitComment();

    gitRevertCommit.waitRevertPanelOpened();
    gitRevertCommit.selectRevision(revision);
    gitRevertCommit.clickRevertButton();
    gitRevertCommit.waitRevertPanelClosed();

    gitStatusBar.waitMessageInGitTab("Reverted commits: - " + revision);

    menu.runCommand(GIT, REVERT_COMMIT);

    assertEquals(gitRevertCommit.getTopCommitAuthor(), gitHubUsername);
    assertTrue(gitRevertCommit.getTopCommitComment().contains("Revert \"" + comment + "\""));

    gitRevertCommit.clickCancelButton();
  }

  private void commitFiles() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);

    git.waitAndRunCommit(COMMIT_MSG);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
  }
}
