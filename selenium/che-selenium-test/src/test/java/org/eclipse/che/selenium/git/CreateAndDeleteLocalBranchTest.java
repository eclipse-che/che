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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Created by aleksandr shmaraev on 25.11.15 */
@Test(groups = TestGroup.GITHUB)
public class CreateAndDeleteLocalBranchTest {
  private static final String PROJECT_NAME = NameGenerator.generate("CreateLocalBranch", 4);
  private static final String MASTER_BRANCH = "master";
  private static final String TEST_BRANCH = "newbranch";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private DefaultTestUser user;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private Loader loader;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private AskDialog askDialog;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, user.getEmail());
    URL resource =
        CreateAndDeleteLocalBranchTest.class.getResource("/projects/create-local-branch-project");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
  }

  @Test(priority = 1)
  public void createNewLocalBranchTest() {
    initRepo();
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheList(MASTER_BRANCH);
    git.waitDisappearBranchName(TEST_BRANCH);
    git.waitEnabledAndClickCreateBtn();
    git.typeAndWaitNewBranchName(TEST_BRANCH);
    git.waitBranchInTheList(MASTER_BRANCH);
    git.waitBranchInTheList(TEST_BRANCH);
    git.closeBranchesForm();
  }

  @Test(priority = 2)
  public void deleteLocalBranch() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheList(MASTER_BRANCH);
    git.waitBranchInTheList(TEST_BRANCH);
    git.selectBranchAndClickDelBranch(TEST_BRANCH);
    git.waitOpenedAskDelBranch();
    git.confirmeAndCloseAskDelBranch();
    git.waitDisappearBranchName(TEST_BRANCH);
    git.closeBranchesForm();
  }

  private void initRepo() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_INITIALIZED_SUCCESS);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();
  }
}
