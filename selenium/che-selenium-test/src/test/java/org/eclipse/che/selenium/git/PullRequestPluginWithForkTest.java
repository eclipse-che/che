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

import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class PullRequestPluginWithForkTest {
  private static final Logger LOG = getLogger(PullRequestPluginWithForkTest.class);

  private static final String PROJECT_NAME = "pull-request-plugin-fork-test";
  private static final String PROJECT_URL =
      "https://github.com/iedexmain1/pull-request-plugin-fork-test.git";
  private static final String NAME_REPO = PROJECT_NAME;
  private static final String FORK_NAME_REPO = PROJECT_NAME;
  private static final String PULL_REQUEST_CREATED = "Your pull request has been created.";
  private static final String PUll_REQUEST_UPDATED = "Your pull request has been updated.";

  private final Long time = new Date().getTime();
  private final String title = "Title-" + time.toString();
  private final String comment = "Comment-" + time.toString();

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject
  @Named("github.auxiliary.username")
  private String githubUserCloneName;

  @Inject
  @Named("github.auxiliary.password")
  private String githubUserClonePassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private ImportProjectFromLocation importWidget;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private ProjectExplorer explorer;
  @Inject private CodenvyEditor editor;
  @Inject private PullRequestPanel pullRequestPanel;
  @Inject private Wizard wizard;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private Preferences preferences;
  @Inject private TestGitHubServiceClient gitHubClientService;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(ws);

    // add committer info
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    // authorize application on GitHub
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.generateAndUploadSshKeyOnGithub(gitHubUsername, gitHubPassword);
  }

  @AfterClass
  public void tearDown() throws Exception {
    try {
      gitHubClientService.deleteRepo(FORK_NAME_REPO, gitHubUsername, gitHubPassword);
    } catch (NotFoundException e) {
      // ignore absent repo to delete
      LOG.debug("Repo {} is not found.", FORK_NAME_REPO);
      return;
    }

    List<String> listPullRequest =
        gitHubClientService.getNumbersOfOpenedPullRequests(
            NAME_REPO, githubUserCloneName, githubUserClonePassword);

    if (!listPullRequest.isEmpty()) {
      gitHubClientService.closePullRequest(
          NAME_REPO,
          Collections.max(listPullRequest),
          githubUserCloneName,
          githubUserClonePassword);
    }
  }

  @Test
  public void createPullRequest() {
    explorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importWidget.waitMainForm();
    importWidget.selectGitSourceItem();
    loader.waitOnClosed();
    importWidget.typeURi(PROJECT_URL);
    importWidget.typeProjectName(PROJECT_NAME);
    importWidget.clickImportBtnWithoutWait();
    wizard.selectTypeProject(Wizard.TypeProject.BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    loader.waitOnClosed();
    wizard.waitCreateProjectWizardFormIsClosed();
    explorer.waitItem(PROJECT_NAME);
    explorer.openItemByPath(PROJECT_NAME);
    explorer.openItemByPath(PROJECT_NAME + "/README.md");

    // change content
    editor.waitActive();
    editor.deleteAllContent();
    editor.goToCursorPositionVisible(1, 1);
    editor.typeTextIntoEditor(time.toString());
    pullRequestPanel.clickPullRequestBtn();
    pullRequestPanel.enterComment(comment);
    pullRequestPanel.enterTitle(title);

    // commit change and create pull request
    pullRequestPanel.clickCreatePRBtn();
    pullRequestPanel.clickOkCommitBtn();
    pullRequestPanel.waitStatusOk(Status.FORK_CREATED);
    pullRequestPanel.waitStatusOk(Status.BRANCH_PUSHED_ON_YOUR_FORK);
    pullRequestPanel.waitMessage(PULL_REQUEST_CREATED);
  }

  @Test(priority = 1)
  void updatePullRequest() {
    editor.closeAllTabs();
    loader.waitOnClosed();
    explorer.openItemByPath(PROJECT_NAME + "/README.md");
    editor.waitActive();
    editor.deleteAllContent();
    editor.goToCursorPositionVisible(1, 1);
    editor.typeTextIntoEditor("Update " + time.toString());
    pullRequestPanel.clickUpdatePRBtn();
    pullRequestPanel.clickOkCommitBtn();
    pullRequestPanel.waitStatusOk(Status.NEW_COMMITS_PUSHED);
    pullRequestPanel.waitStatusOk(Status.PULL_REQUEST_UPDATED);
    pullRequestPanel.waitMessage(PUll_REQUEST_UPDATED);
    pullRequestPanel.clickPullRequestBtn();
    pullRequestPanel.waitClosePanel();
  }
}
