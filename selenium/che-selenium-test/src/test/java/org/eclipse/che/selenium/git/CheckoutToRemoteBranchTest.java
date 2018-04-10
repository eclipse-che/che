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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.BRANCHES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.COMMIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.PULL;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.PUSH;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.SHOW_HISTORY;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.STATUS;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class CheckoutToRemoteBranchTest {
  private static final String PROJECT_NAME = NameGenerator.generate("CheckoutToRemoteBranch-", 4);
  private static final String PROJECT_NAME2 = NameGenerator.generate("CheckoutToRemoteBranch2-", 4);

  private static final String MASTER_BRANCH = "master";
  private static final String ORIGIN_MASTER_BRANCH = "origin/master";
  private static final String ORIGIN_SECOND_BRANCH = "origin/second_branch";
  private static final String SECOND_BRANCH = "second_branch";
  private static final String NAME_REMOTE_REPO = "origin";
  private static final String PULL_MSG = "Already up-to-date";
  private static String COMMIT_MESS = "commitchk_remote";
  private static final String GIT_STATUS_MESS =
      " On branch second_branch\n" + " nothing to commit, working directory clean";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;
  @Inject private TestGitHubRepository testRepo;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private CodenvyEditor editor;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestGitHubKeyUploader testGitHubKeyUploader;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    testGitHubKeyUploader.updateGithubKey();

    Path entryPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath());
    testRepo.addContent(entryPath);

    ide.open(ws);

    // create another branch in the test repo
    testRepo.createBranchFromMaster(SECOND_BRANCH);
  }

  @Test
  public void checkoutToRemoteBranch() throws Exception {
    // preconditions and import the test repo
    String pathJavaFile = "/src/main/java/che/eclipse/sample/Aclass.java";
    String pathJspFile = "/src/main/webapp/index.jsp";
    String changeContent =
        String.format("// change_content-%s", String.valueOf(System.currentTimeMillis()));

    projectExplorer.waitProjectExplorer();
    git.importJavaApp(testRepo.getSshUrl(), PROJECT_NAME, MAVEN);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // git checkout to the 'second_branch'
    checkoutToSecondRemoteBranch();

    menu.runCommand(GIT, STATUS);
    git.waitGitStatusBarWithMess(GIT_STATUS_MESS);

    performGitPull();

    // change content of the files, commit and push
    testProjectServiceClient.updateFile(ws.getId(), PROJECT_NAME + pathJavaFile, changeContent);
    testProjectServiceClient.updateFile(ws.getId(), PROJECT_NAME + pathJspFile, changeContent);

    commitFiles();

    performGitPush();

    // import from github to the second project
    git.importJavaApp(testRepo.getHtmlUrl(), PROJECT_NAME2, MAVEN);
    projectExplorer.waitAndSelectItem(PROJECT_NAME2);

    checkoutToSecondRemoteBranch();

    // check the changes are present in the files
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT_NAME2 + "/src/main/java/che/eclipse/sample/Aclass.java");
    editor.waitTextIntoEditor(changeContent);
    projectExplorer.openItemByPath(PROJECT_NAME2 + "/src/main/webapp/index.jsp");
    editor.waitTextIntoEditor(changeContent);

    // Call and checking show history
    projectExplorer.waitAndSelectItem(PROJECT_NAME2 + "/src");
    menu.runCommand(GIT, SHOW_HISTORY);
    git.waitHistoryFormToOpen();
    git.waitCommitInHistoryForm(COMMIT_MESS);
  }

  private void checkoutToSecondRemoteBranch() throws Exception {
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheList(MASTER_BRANCH);
    git.waitBranchInTheList(ORIGIN_MASTER_BRANCH);
    git.waitBranchInTheList(ORIGIN_SECOND_BRANCH);
    git.selectBranchAndClickCheckoutBtn(ORIGIN_SECOND_BRANCH);
    git.waitGitCompareBranchFormIsClosed();
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheListWithCoState(SECOND_BRANCH);
    git.closeBranchesForm();
  }

  private void performGitPull() throws InterruptedException {
    menu.runCommand(GIT, REMOTES_TOP, PULL);
    git.waitPullFormToOpen();
    git.waitPullRemoteRepository(NAME_REMOTE_REPO);
    git.waitPullRemoteBranchName(SECOND_BRANCH);
    git.waitPullLocalBranchName(SECOND_BRANCH);
    git.clickPull();
    git.waitPullFormToClose();
    git.waitGitStatusBarWithMess(PULL_MSG);
  }

  private void commitFiles() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, COMMIT);

    git.waitAndRunCommit(COMMIT_MESS);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
  }

  private void performGitPush() {
    menu.runCommand(GIT, REMOTES_TOP, PUSH);
    git.waitPushFormToOpen();
    git.selectPushRemoteBranchName(SECOND_BRANCH);
    git.clickPush();
    git.waitPushFormToClose();
    git.waitGitStatusBarWithMess("Successfully pushed to " + testRepo.getSshUrl());
  }
}
