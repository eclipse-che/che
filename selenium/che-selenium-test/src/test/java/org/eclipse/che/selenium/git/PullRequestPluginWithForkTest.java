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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PREFERENCES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PROFILE_MENU;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.BRANCH_PUSHED_ON_YOUR_FORK;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.FORK_CREATED;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.NEW_COMMITS_PUSHED;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.CheSeleniumSuiteModule;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.PullRequestPanel.Status;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
@Test(groups = TestGroup.GITHUB)
public class PullRequestPluginWithForkTest {
  private static final String PROJECT_NAME = "pull-request-plugin-fork-test";
  private static final String PATH_TO_README_FILE = PROJECT_NAME + "/README.md";
  private static final String PULL_REQUEST_CREATED = "Your pull request has been created.";
  private static final String PULL_REQUEST_UPDATED = "Your pull request has been updated.";
  private static final String TITLE = generate("Title-", 8);
  private static final String COMMENT = generate("Comment-", 8);

  @Inject
  @Named("github.username")
  private String githubUserName;

  @Inject
  @Named("github.password")
  private String githubUserPassword;

  @Inject
  @Named(CheSeleniumSuiteModule.AUXILIARY)
  private TestGitHubRepository testAuxiliaryRepo;

  @Inject private Git git;
  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private TestUser testUser;
  @Inject private CodenvyEditor editor;
  @Inject private Preferences preferences;
  @Inject private TestWorkspace testWorkspace;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private PullRequestPanel pullRequestPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    Path entryPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath());
    testAuxiliaryRepo.addContent(entryPath);

    ide.open(testWorkspace);

    // add committer info
    testUserPreferencesServiceClient.addGitCommitter(githubUserName, testUser.getEmail());

    // authorize application on GitHub
    menu.runCommand(PROFILE_MENU, PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.generateAndUploadSshKeyOnGithub(githubUserName, githubUserPassword);
  }

  @Test
  public void createPullRequest() throws Exception {
    // import project
    projectExplorer.waitProjectExplorer();
    git.importJavaApp(testAuxiliaryRepo.getHtmlUrl(), PROJECT_NAME, MAVEN);

    // change content
    projectExplorer.openItemByPath(PROJECT_NAME);
    openFileAndChangeContent(PATH_TO_README_FILE, generate("", 12));

    // change commit and create pull request
    pullRequestPanel.clickPullRequestBtn();
    pullRequestPanel.enterComment(COMMENT);
    pullRequestPanel.enterTitle(TITLE);
    pullRequestPanel.clickCreatePRBtn();
    pullRequestPanel.clickOkCommitBtn();
    pullRequestPanel.waitStatusOk(FORK_CREATED);
    pullRequestPanel.waitStatusOk(BRANCH_PUSHED_ON_YOUR_FORK);
    pullRequestPanel.waitMessage(PULL_REQUEST_CREATED);
  }

  @Test(priority = 1)
  void updatePullRequest() throws Exception {
    editor.closeAllTabs();
    loader.waitOnClosed();

    // change content
    openFileAndChangeContent(PATH_TO_README_FILE, generate("Update ", 12));

    // update PR and check status
    pullRequestPanel.clickUpdatePRBtn();
    pullRequestPanel.clickOkCommitBtn();
    pullRequestPanel.waitStatusOk(NEW_COMMITS_PUSHED);
    pullRequestPanel.waitStatusOk(Status.PULL_REQUEST_UPDATED);
    pullRequestPanel.waitMessage(PULL_REQUEST_UPDATED);
    pullRequestPanel.clickPullRequestBtn();
    pullRequestPanel.waitClosePanel();
  }

  private void openFileAndChangeContent(String filePath, String text) throws Exception {
    projectExplorer.openItemByPath(filePath);
    editor.waitActive();
    testProjectServiceClient.updateFile(testWorkspace.getId(), filePath, text);
  }
}
