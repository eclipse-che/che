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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = TestGroup.GITHUB)
public class CheckoutReferenceTest {
  private static final String PROJECT_NAME = NameGenerator.generate("CheckoutReference_", 4);
  // TODO This is bug from JGit side, see https://github.com/eclipse/che/issues/4673
  private static final String DEFAULT_BRANCH = "master";
  private static final String BRANCH_DETACHED_HASH =
      "(detached from 005ae81917608ea7d18f178a0231f285d3357106)";
  private static final String COMMIT_HASH = "005ae819";
  private static final String COMMIT_MESSAGE = "commitrepare";
  private static final String TAG_NAME_1 = "version_1";
  private static final String BRANCH_DETACHED_TAG_1 = "(detached from version_1)";
  private static final String TAG_NAME_2 = "version_2";
  private static final String BRANCH_DETACHED_TAG_2 = "(detached from version_2)";
  private static final String COMMIT_MESSAGE_TAG_1 = "commitcommit-to-first-project";
  private static final String COMMIT_MESSAGE_TAG_2 = "commitcommit-to-second-project";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

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
  @Inject private ImportProjectFromLocation importFromLocation;
  @Inject private Wizard projectWizard;
  @Inject private TestGitHubKeyUploader testGitHubKeyUploader;

  @BeforeClass
  public void prepare() throws Exception {
    testGitHubKeyUploader.updateGithubKey();
    ide.open(ws);
  }

  @Test
  public void checkCheckoutReference() throws Exception {
    // clone test repository
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);

    importProjectFromRemoteRepo(
        "git@github.com:" + gitHubUsername + "/testRepo-2.git", PROJECT_NAME);

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    loader.waitOnClosed();
    events.clickEventLogBtn();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(DEFAULT_BRANCH);
    git.closeBranchesForm();

    // check the 'Cancel' button of the 'Checkout Reference'
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.CHECKOUT_REFERENCE);
    git.waitReferenceFormIsOpened();
    git.clickCheckoutReferenceCancelButton();

    // perform the 'Checkout Reference' by a commit hash
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.CHECKOUT_REFERENCE);
    git.typeReferenceAndConfirm(COMMIT_HASH);
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_DETACHED_HASH);
    git.closeBranchesForm();

    // check the git history
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    git.waitHistoryFormToOpen();
    loader.waitOnClosed();
    git.clickOnHistoryRowInСommitsList(0);
    git.waitCommitInHistoryForm(COMMIT_MESSAGE);
    git.waitCommitInHistoryForm(COMMIT_HASH);
    loader.waitOnClosed();
    git.closeGitHistoryForm();

    // switch to default branch
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_DETACHED_HASH);
    git.selectBranchAndClickCheckoutBtn(DEFAULT_BRANCH);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(DEFAULT_BRANCH);
    git.closeBranchesForm();

    // perform the 'Checkout Reference' by a tag name
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.CHECKOUT_REFERENCE);
    git.typeReferenceAndConfirm(TAG_NAME_1);
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_DETACHED_TAG_1);
    git.closeBranchesForm();

    // check the git history
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    git.waitHistoryFormToOpen();
    loader.waitOnClosed();
    git.waitCommitInHistoryForm(COMMIT_MESSAGE_TAG_1);
    git.closeGitHistoryForm();

    // switch to another tag
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.CHECKOUT_REFERENCE);
    git.typeReferenceAndConfirm(TAG_NAME_2);
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_DETACHED_TAG_2);
    git.closeBranchesForm();

    // Check the git history
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    git.waitHistoryFormToOpen();
    loader.waitOnClosed();
    git.waitCommitInHistoryForm(COMMIT_MESSAGE_TAG_2);
    git.closeGitHistoryForm();

    // Switch to default branch
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_DETACHED_TAG_2);
    git.selectBranchAndClickCheckoutBtn(DEFAULT_BRANCH);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(DEFAULT_BRANCH);
    git.closeBranchesForm();
  }

  private void importProjectFromRemoteRepo(String urlRepo, String projectName) {
    importFromLocation.waitAndTypeImporterAsGitInfo(urlRepo, projectName);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(projectName);
    loader.waitOnClosed();
  }
}
